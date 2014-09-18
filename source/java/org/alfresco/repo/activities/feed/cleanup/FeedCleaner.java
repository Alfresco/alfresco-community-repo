/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.activities.feed.cleanup;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.activities.ActivitiesDAO;
import org.alfresco.repo.domain.activities.ActivityFeedDAO;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.JobLockService.JobLockRefreshCallback;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionException;

/**
 * The feed cleaner component is responsible for purging 'obsolete' activity feed entries
 * 
 * @author janv
 * @since 3.0
 */
public class FeedCleaner implements NodeServicePolicies.BeforeDeleteNodePolicy
{
    private static Log logger = LogFactory.getLog(FeedCleaner.class);
    
    private static String KEY_DELETED_SITE_IDS = "feedCleaner.deletedSites";
    private static String KEY_DELETED_USER_IDS = "feedCleaner.deletedUsers";
    
    private int maxIdRange = 1000000;
    private int maxAgeMins = 0;
    private int maxFeedSize = 100;
    private boolean userNamesAreCaseSensitive = false;
    
    private ActivityFeedDAO feedDAO;
    
    private JobLockService jobLockService;
    private NodeService nodeService;
    private TenantService tenantService;
    private PolicyComponent policyComponent;
    private TransactionService transactionService;
    
    private FeedCleanerDeleteSiteTransactionListener deleteSiteTransactionListener;
    private FeedCleanerDeletePersonTransactionListener deletePersonTransactionListener;
    
    public void setUserNamesAreCaseSensitive(boolean userNamesAreCaseSensitive)
    {
        this.userNamesAreCaseSensitive = userNamesAreCaseSensitive;
    }
    
    public void setFeedDAO(ActivityFeedDAO feedDAO)
    {
        this.feedDAO = feedDAO;
    }
    
    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * 
     * @param maxIdRange            maximum difference between lowest and highest ID
     */
    public void setMaxIdRange(int maxIdRange)
    {
        this.maxIdRange = maxIdRange;
    }
    
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    public void setMaxAgeMins(int mins)
    {
        this.maxAgeMins = mins;
    }
    
    // note: this relates to user feed size (across all sites) or site feed size - for each format
    public void setMaxFeedSize(int size)
    {
        this.maxFeedSize = size;
    }
    
    public int getMaxFeedSize()
    {
        return this.maxFeedSize;
    }
    
    
    /**
     * Perform basic checks to ensure that the necessary dependencies were injected.
     */
    private void checkProperties()
    {
        PropertyCheck.mandatory(this, "feedDAO", feedDAO);
        PropertyCheck.mandatory(this, "policyComponent", policyComponent);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "jobLockService", jobLockService);
        
        // check the max age and max feed size
        if ((maxAgeMins <= 0) && (maxFeedSize <= 0))
        {
            logger.warn("Neither maxAgeMins or maxFeedSize set - feeds will not be cleaned");
        }
    }
    
    public void init()
    {
        policyComponent.bindClassBehaviour(BeforeDeleteNodePolicy.QNAME,
                                           ContentModel.TYPE_PERSON,
                                           new JavaBehaviour(this, "beforeDeleteNodePerson"));
        
        deletePersonTransactionListener = new FeedCleanerDeletePersonTransactionListener();
        
        policyComponent.bindClassBehaviour(BeforeDeleteNodePolicy.QNAME,
                                           SiteModel.TYPE_SITE,
                                           new JavaBehaviour(this, "beforeDeleteNodeSite"));
        
        deleteSiteTransactionListener = new FeedCleanerDeleteSiteTransactionListener();
    }

    private static final long LOCK_TTL = 60000L;        // 1 minute
    private static final QName LOCK_QNAME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "org.alfresco.repo.activities.feed.cleanup.FeedCleaner");
    public int execute() throws JobExecutionException
    {
        checkProperties();
        
        final AtomicBoolean keepGoing = new AtomicBoolean(true);
        String lockToken = null;
        try
        {
            // Lock
            lockToken = jobLockService.getLock(LOCK_QNAME, LOCK_TTL);
            // Refresh to get callbacks
            JobLockRefreshCallback callback = new JobLockRefreshCallback()
            {
                @Override
                public void lockReleased()
                {
                    keepGoing.set(false);
                }
                
                @Override
                public boolean isActive()
                {
                    return keepGoing.get();
                }
            };
            jobLockService.refreshLock(lockToken, LOCK_QNAME, LOCK_TTL, callback);
            int cleaned = executeWithLock(keepGoing);
            if (logger.isDebugEnabled())
            {
                logger.debug("Cleaned " + cleaned + " feed entries.");
            }
        }
        catch (LockAcquisitionException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Skipping feed cleaning.  " + e.getMessage());
            }
        }
        finally
        {
            keepGoing.set(false);           // Notify the refresh callback that we are done
            if (lockToken != null)
            {
                jobLockService.releaseLock(lockToken, LOCK_QNAME);
            }
        }
        return 0;
    }
    
    /**
     * Does the actual cleanup, expecting the lock to be maintained
     * 
     * @param keepGoing <tt>true</tt> to continue but will switch to <tt>false</tt> to stop
     * @return          number of entries deleted through whatever means
     */
    private int executeWithLock(final AtomicBoolean keepGoing) throws JobExecutionException
    {
        int maxIdRangeDeletedCount = 0;
        int maxAgeDeletedCount = 0;
        int maxSizeDeletedCount = 0;
        
        try
        {
            /*
             * ALF-15383 (DH 15/08/2012)
             * Previously, we allowed maxFeedSize entries per user per site per format.
             * This scaled badly because some users (especially under test conditions)
             * were able to perform actions across many thousands of sites.  If the size
             * limit was 100 and the user belonged to 50K sites, we allowed 5M feed entries
             * for that user.  This may have been OK but for the fact that the queries
             * doing the work are not covered by appropriate indexes to support the where
             * and sort by clauses.
             * In fact, give the current state of indexes, it is necessary to limit the absolute
             * number of feed entries.  We can't use count() queries (they are poor) and cannot
             * reasonably sort by date and trim by count.  Therefore I have introduced an
             * absolute ID range trim that runs before everything else.
             */
            
            if (maxIdRange > 0 && keepGoing.get())
            {
                maxIdRangeDeletedCount = feedDAO.deleteFeedEntries(maxIdRange);
                if (logger.isTraceEnabled())
                {
                    logger.trace("Cleaned " + maxIdRangeDeletedCount + " entries to keep ID range of " + maxIdRange + ".");
                }
            }

            if (maxAgeMins > 0 && keepGoing.get())
            {
                // clean old entries based on maxAgeMins
                
                long nowTimeOffset = new Date().getTime();
                long keepTimeOffset = nowTimeOffset - ((long)maxAgeMins*60000L); // millsecs = mins * 60 secs * 1000 msecs
                Date keepDate = new Date(keepTimeOffset);
                
                maxAgeDeletedCount = feedDAO.deleteFeedEntries(keepDate);
                if (logger.isTraceEnabled())
                {
                    logger.trace("Cleaned " + maxAgeDeletedCount + " entries (upto " + keepDate + ", max age " + maxAgeMins + " mins)");
                }
            }
            
            // TODO:    ALF-15511
            if (maxFeedSize > 0 && keepGoing.get())
            {
                // Get user+format feeds exceeding the required maximum
                if (logger.isTraceEnabled())
                {
                    logger.trace("Selecting user+format feeds exceeding the required maximum of " + maxFeedSize + " entries.");
                }
                List<ActivityFeedEntity> userFeedsTooMany = feedDAO.selectUserFeedsToClean(maxFeedSize);
                for (ActivityFeedEntity userFeedTooMany : userFeedsTooMany)
                {
                    if (!keepGoing.get())
                    {
                        if (logger.isTraceEnabled())
                        {
                            logger.trace("Stopping cleaning the feeds.");
                        }
                        break;
                    }
                    if (logger.isTraceEnabled())
                    {
                        logger.trace("Found user activity feed entity: " + userFeedTooMany.toString());
                    }
                    String feedUserId = userFeedTooMany.getFeedUserId();
                    // Rather than filter out the two usernames that indicate site-specific
                    // feed entries, we can just filter them out now.
                    if (ActivitiesDAO.KEY_ACTIVITY_NULL_VALUE.equals(feedUserId))
                    {
                        if (logger.isTraceEnabled())
                        {
                            logger.trace("Found site-specific feed entries, filtering.");
                        }
                        continue;
                    }
                    // Get the feeds to keep
                    if (logger.isTraceEnabled())
                    {
                        logger.trace("Get the feeds to keep for user for all sites, not exluding users.");
                    }
                    List<ActivityFeedEntity> feedsToKeep = feedDAO.selectUserFeedEntries(feedUserId, null, false, false, -1L, maxFeedSize);
                    if (logger.isTraceEnabled())
                    {
                        for(ActivityFeedEntity feedToKeep : feedsToKeep)
                        {
                            logger.trace("Found user activity feed entity to keep: " + feedToKeep.toString());
                        }
                    }
                    // If the feeds have been removed, then ignore
                    if (feedsToKeep.size() < maxFeedSize)
                    {
                        if (logger.isTraceEnabled())
                        {
                            logger.trace("Found less then " + maxFeedSize + " .The feeds were removed, ignoring.");
                        }
                        continue;
                    }
                    // Get the last one
                    Date oldestFeedEntry = feedsToKeep.get(maxFeedSize-1).getPostDate();
                    
                    if (logger.isTraceEnabled())
                    {
                        logger.trace("Deleting the oldest feed entry: " + oldestFeedEntry.toString());
                    }
                    int deletedCount = feedDAO.deleteUserFeedEntries(feedUserId, oldestFeedEntry);
                    if (logger.isTraceEnabled())
                    {
                        logger.trace("Cleaned " + deletedCount + " entries for user '" + feedUserId + "'.");
                    }
                    maxSizeDeletedCount += deletedCount;
                }
                
                // Get site+format feeds exceeding the required maximum
                if (logger.isTraceEnabled())
                {
                    logger.trace("Selecting site+format feeds exceeding the required maximum of " + maxFeedSize + " entries.");
                }
                List<ActivityFeedEntity> siteFeedsTooMany = feedDAO.selectSiteFeedsToClean(maxFeedSize);
                for (ActivityFeedEntity siteFeedTooMany : siteFeedsTooMany)
                {
                    if (!keepGoing.get())
                    {
                        if (logger.isTraceEnabled())
                        {
                            logger.trace("Stopping cleaning the feeds.");
                        }
                        break;
                    }
                    if (logger.isTraceEnabled())
                    {
                        logger.trace("Found site activity feed entity: " + siteFeedTooMany.toString());
                    }
                    String siteId = siteFeedTooMany.getSiteNetwork();
                    // Get the feeds to keep
                    if (logger.isTraceEnabled())
                    {
                        logger.trace("Get the feeds to keep for site.");
                    }
                    List<ActivityFeedEntity> feedsToKeep = feedDAO.selectSiteFeedEntries(siteId, maxFeedSize);
                    if (logger.isTraceEnabled())
                    {
                        for(ActivityFeedEntity feedToKeep : feedsToKeep)
                        {
                            logger.trace("Found site activity feed entity to keep: " + feedToKeep.toString());
                        }
                    }
                    // If the feeds have been removed, then ignore
                    if (feedsToKeep.size() < maxFeedSize)
                    {
                        continue;
                    }
                    // Get the last one
                    Date oldestFeedEntry = feedsToKeep.get(maxFeedSize-1).getPostDate();
                    if (logger.isTraceEnabled())
                    {
                        logger.trace("Deleting the oldest feed entry: " + oldestFeedEntry.toString());
                    }
                    int deletedCount = feedDAO.deleteSiteFeedEntries(siteId, oldestFeedEntry);
                    if (logger.isTraceEnabled())
                    {
                        logger.trace("Cleaned " + deletedCount + " entries for site '" + siteId + "'.");
                    }
                    maxSizeDeletedCount += deletedCount;
                }
            }
        }
        catch (SQLException e)
        {
            logger.error("Exception during cleanup of feeds", e);
            throw new JobExecutionException(e);
        }
        catch (Throwable e)
        {
            // We were told to stop, which is also what will happen if the VM shuts down
            if (!keepGoing.get())
            {
                // Ignore
            }
            else
            {
                logger.error("Exception during cleanup of feeds", e);
            }
        }
        
        return (maxIdRangeDeletedCount + maxAgeDeletedCount + maxSizeDeletedCount);
    }
    
    // behaviours
    
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        // dummy
    }
    
    public void beforeDeleteNodePerson(NodeRef personNodeRef)
    {
        String userId = (String)nodeService.getProperty(personNodeRef, ContentModel.PROP_USERNAME);
        //MNT-9104 If username contains uppercase letters the action of joining a site will not be displayed in "My activities" 
        if (! userNamesAreCaseSensitive)
        {
            userId = userId.toLowerCase();
        }
        Set<String> deletedUserIds = (Set<String>)AlfrescoTransactionSupport.getResource(KEY_DELETED_USER_IDS);
        if (deletedUserIds == null)
        {
            deletedUserIds = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>()); // Java 6
            AlfrescoTransactionSupport.bindResource(KEY_DELETED_USER_IDS, deletedUserIds);
        }
        
        deletedUserIds.add(userId);
        
        AlfrescoTransactionSupport.bindListener(deletePersonTransactionListener);
    }
    
    public void beforeDeleteNodeSite(NodeRef siteNodeRef)
    {
        String siteId = (String)nodeService.getProperty(siteNodeRef, ContentModel.PROP_NAME);
        
        Set<String> deletedSiteIds = (Set<String>)AlfrescoTransactionSupport.getResource(KEY_DELETED_SITE_IDS);
        if (deletedSiteIds == null)
        {
            deletedSiteIds = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>()); // Java 6
            AlfrescoTransactionSupport.bindResource(KEY_DELETED_SITE_IDS, deletedSiteIds);
        }
        
        deletedSiteIds.add(siteId);
        
        AlfrescoTransactionSupport.bindListener(deleteSiteTransactionListener);
    }
    
    class FeedCleanerDeleteSiteTransactionListener extends TransactionListenerAdapter
    {
        @Override
        public void afterCommit()
        {
            Set<String> deletedSiteIds = TransactionalResourceHelper.getSet(KEY_DELETED_SITE_IDS);
            if (deletedSiteIds != null)
            {
                for (final String siteId : deletedSiteIds)
                {
                    transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                    {
                        public Void execute() throws Throwable
                        {
                            try
                            {
                                // Since we are in post-commit, we do best-effort
                                int deletedCnt = feedDAO.deleteSiteFeedEntries(tenantService.getName(siteId));
                                
                                if (logger.isDebugEnabled())
                                {
                                    logger.debug("afterCommit: deleted "+deletedCnt+" site feed entries for site '"+siteId+"'");
                                }
                            }
                            catch (SQLException e)
                            {
                                logger.error("Activities feed cleanup for site '"+siteId+"' failed: ", e);
                            }
                            return null;
                        }
                    }, false, true);
                }
            }
        }
    }
    
    class FeedCleanerDeletePersonTransactionListener extends TransactionListenerAdapter
    {
        @Override
        public void afterCommit()
        {
            Set<String> deletedUserIds = TransactionalResourceHelper.getSet(KEY_DELETED_USER_IDS);
            if (deletedUserIds != null)
            {
                for (String user : deletedUserIds)
                {
                    //MNT-9104 If username contains uppercase letters the action of joining a site will not be displayed in "My activities" 
                    final String userId;
                    if (! userNamesAreCaseSensitive)
                    {
                        userId = user.toLowerCase();
                    }
                    else
                    {
                        userId = user;
                    }
                    
                    transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                    {
                        public Void execute() throws Throwable
                        {
                            try
                            {
                                // Since we are in post-commit, we do best-effort
                                feedDAO.deleteUserFeedEntries(userId);
                            }
                            catch (SQLException e)
                            {
                                logger.error("Activities feed cleanup for user '"+userId+"' failed: ", e);
                            }
                            
                            return null;
                        }
                    }, false, true);
                }
            }
        }
    }
}
