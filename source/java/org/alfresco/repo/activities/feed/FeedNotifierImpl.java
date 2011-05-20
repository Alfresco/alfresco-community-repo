/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.activities.feed;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.dictionary.RepositoryLocation;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ModelUtil;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.UrlUtil;
import org.alfresco.util.VmShutdownListener;
import org.alfresco.util.VmShutdownListener.VmShutdownException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Implementation of the Activity Feed Notifier component
 * 
 * Note: currently implemented to email activities stored in JSON format
 * 
 * @since 3.5
 */
public class FeedNotifierImpl implements FeedNotifier
{
    private static Log logger = LogFactory.getLog(FeedNotifierImpl.class);
    
    private static final QName LOCK_QNAME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ActivityFeedNotifier");
    private static final long LOCK_TTL = 30000L;
    private static ThreadLocal<Pair<Long, String>> lockThreadLocal = new ThreadLocal<Pair<Long, String>>();
    
    private static VmShutdownListener vmShutdownListener = new VmShutdownListener(FeedNotifierImpl.class.getName());
    
    private static final String MSG_EMAIL_SUBJECT = "activities.feed.notifier.email.subject";
    
    private ActivityService activityService;
    private PersonService personService;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private ActionService actionService;
    private SearchService searchService;
    private NamespaceService namespaceService;
    private SiteService siteService;
    private JobLockService jobLockService;
    private TransactionService transactionService;
    private AuthenticationContext authenticationContext;
    private SysAdminParams sysAdminParams;
    private RepoAdminService repoAdminService;
    private List<String> excludedEmailSuffixes;
    
    private RepositoryLocation feedEmailTemplateLocation;
    
    
    public void setActivityService(ActivityService activityService)
    {
        this.activityService = activityService;
    }
    
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }
    
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }
    
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }
    
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    public void setAuthenticationContext(AuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }
    
    public void setFeedEmailTemplateLocation(RepositoryLocation feedEmailTemplateLocation)
    {
        this.feedEmailTemplateLocation = feedEmailTemplateLocation;
    }
    
    public void setSysAdminParams(SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
    }
    
    public void setRepoAdminService(RepoAdminService repoAdminService)
    {
        this.repoAdminService = repoAdminService;
    }
    
    public void setExcludedEmailSuffixes(List<String> excludedEmailSuffixes)
    {
        this.excludedEmailSuffixes = excludedEmailSuffixes;
    }
    
    /**
     * Perform basic checks to ensure that the necessary dependencies were injected.
     */
    private void checkProperties()
    {
        PropertyCheck.mandatory(this, "activityService", activityService);
        PropertyCheck.mandatory(this, "personService", personService);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "fileFolderService", fileFolderService);
        PropertyCheck.mandatory(this, "actionService", actionService);
        PropertyCheck.mandatory(this, "searchService", searchService);
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);
        PropertyCheck.mandatory(this, "siteService", siteService);
        PropertyCheck.mandatory(this, "jobLockService", jobLockService);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "authenticationContext", authenticationContext);
        PropertyCheck.mandatory(this, "sysAdminParams", sysAdminParams);
        PropertyCheck.mandatory(this, "feedEmailTemplateLocation", feedEmailTemplateLocation);
    }
    
    public void execute(int repeatIntervalMins)
    {
        checkProperties();
        
        // Bypass if the system is in read-only mode
        if (transactionService.isReadOnly())
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Activities email notification bypassed; the system is read-only");
            }
            return;
        }
        
        try
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("Activities email notification started");
            }
            
            refreshLock();
            executeInternal(repeatIntervalMins);
            
            // Done
            if (logger.isTraceEnabled())
            {
                logger.trace("Activities email notification completed");
            }
        }
        catch (LockAcquisitionException e)
        {
            // Job being done by another process
            if (logger.isDebugEnabled())
            {
                logger.debug("Activities email notification already underway");
            }
        }
        catch (VmShutdownException e)
        {
            // Aborted
            if (logger.isDebugEnabled())
            {
                logger.debug("Activities email notification aborted");
            }
        }
        finally
        {
            releaseLock();
        }
    }
    
    private void executeInternal(final int repeatIntervalMins)
    {
        final NodeRef emailTemplateRef = getEmailTemplateRef();
        
        if (emailTemplateRef == null)
        {
            return;
        }
        
        final String shareUrl = UrlUtil.getShareUrl(sysAdminParams); 
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Share URL configured as: "+shareUrl);
        }
        
        int userCnt = 0;
        int feedEntryCnt = 0;
        
        long startTime = System.currentTimeMillis();
        
        try
        {
            final String subjectText = buildSubjectText(startTime);
            
            Set<NodeRef> people = personService.getAllPeople();
            
            // local cache for this execution
            final Map<String, String> siteNames = new HashMap<String, String>(10);
            
            for (final NodeRef personNodeRef : people)
            {
                refreshLock();
                
                try
                {
                    final RetryingTransactionHelper txHelper = transactionService.getRetryingTransactionHelper();
                    txHelper.setMaxRetries(0);
                    
                    Pair<Integer, Long> result = txHelper.doInTransaction(new RetryingTransactionCallback<Pair<Integer, Long>>()
                    {
                        public Pair<Integer, Long> execute() throws Throwable
                        {
                            return prepareAndSendEmail(personNodeRef, emailTemplateRef, subjectText, siteNames, shareUrl, repeatIntervalMins);
                        }
                    }, true, true);
                    
                    if (result != null)
                    {
                        int entryCnt = result.getFirst();
                        final long maxFeedId = result.getSecond();
                        
                        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
                        {
                            public Void execute() throws Throwable
                            {
                                
                                Long currentMaxFeedId = (Long)nodeService.getProperty(personNodeRef, ContentModel.PROP_EMAIL_FEED_ID);
                                if ((currentMaxFeedId == null) || (currentMaxFeedId < maxFeedId))
                                {
                                    nodeService.setProperty(personNodeRef, ContentModel.PROP_EMAIL_FEED_ID, maxFeedId);
                                }
                                
                                return null;
                            }
                        }, false, true);
                        
                        
                        userCnt++;
                        feedEntryCnt += entryCnt;
                    }
                }
                catch (InvalidNodeRefException inre)
                {
                    // skip this person - eg. no longer exists ?
                    logger.warn("Skip feed notification for user ("+personNodeRef+"): " + inre.getMessage());
                }
            }
        }
        catch (Throwable e)
        {
            // If the VM is shutting down, then ignore
            if (vmShutdownListener.isVmShuttingDown())
            {
                // Ignore
            }
            else
            {
                logger.error("Exception during notification of feeds", e);
            }
        }
        finally
        {
            // assume sends are synchronous - hence bump up to last max feed id
            if (userCnt > 0)
            {
                if (logger.isInfoEnabled())
                {
                    // TODO i18n of info message
                    StringBuilder sb = new StringBuilder();
                    sb.append("Notified ").append(userCnt).append(" user").append(userCnt != 1 ? "s" : "");
                    sb.append(" of ").append(feedEntryCnt).append(" activity feed entr").append(feedEntryCnt != 1 ? "ies" : "y");
                    sb.append(" (in ").append(System.currentTimeMillis()-startTime).append(" msecs)");
                    logger.info(sb.toString());
                }
            }
            else
            {
                if (logger.isTraceEnabled())
                {
                    logger.trace("Nothing to send since no new user activities found");
                }
            }
        }
    }
    
    protected Pair<Integer, Long> prepareAndSendEmail(final NodeRef personNodeRef, NodeRef emailTemplateRef,
                                      String subjectText, Map<String, String> siteNames,
                                      String shareUrl, int repeatIntervalMins)
    {
        Map<QName, Serializable> personProps = nodeService.getProperties(personNodeRef);
        
        String feedUserId = (String)personProps.get(ContentModel.PROP_USERNAME);
        String emailAddress = (String)personProps.get(ContentModel.PROP_EMAIL);
        Boolean emailFeedDisabled = (Boolean)personProps.get(ContentModel.PROP_EMAIL_FEED_DISABLED);
        
        if (skipUser(emailFeedDisabled, feedUserId, emailAddress, excludedEmailSuffixes))
        {
            // skip
            return null;
        }
        
        // where did we get up to ?
        Long emailFeedDBID = (Long)personProps.get(ContentModel.PROP_EMAIL_FEED_ID);
        if (emailFeedDBID != null)
        {
            // increment min feed id
            emailFeedDBID++;
        }
        else
        {
            emailFeedDBID = -1L;
        }
        
        
        // own + others (note: template can be changed to filter out user's own activities if needed)
        List<ActivityFeedEntity> feedEntries = activityService.getUserFeedEntries(feedUserId, FeedTaskProcessor.FEED_FORMAT_JSON, null, false, false, emailFeedDBID);
        
        if (feedEntries.size() > 0)
        {
            long userMaxFeedId = -1L;
            
            Map<String, Object> model = new HashMap<String, Object>();
            List<Map<String, Object>> activityFeedModels = new ArrayList<Map<String, Object>>();
            
            for (ActivityFeedEntity feedEntry : feedEntries)
            {
                Map<String, Object> map = null;
                try
                {
                    map = feedEntry.getModel();
                    activityFeedModels.add(map);
                    
                    String siteId = feedEntry.getSiteNetwork();
                    addSiteName(siteId, siteNames);
                    
                    long feedId = feedEntry.getId();
                    if (feedId > userMaxFeedId)
                    {
                        userMaxFeedId = feedId;
                    }
                }
                catch (JSONException je)
                {
                    // skip this feed entry
                    logger.warn("Skip feed entry for user ("+feedUserId+"): " + je.getMessage());
                    continue;
                }
            }
            
            if (activityFeedModels.size() > 0)
            {
                model.put("activities", activityFeedModels);
                model.put("siteTitles", siteNames);
                model.put("repeatIntervalMins", repeatIntervalMins);
                model.put("feedItemsMax", activityService.getMaxFeedItems());
                model.put("feedItemsCount", activityFeedModels.size());
                
                // add Share info to model
                model.put(TemplateService.KEY_PRODUCT_NAME, ModelUtil.getProductName(repoAdminService));
                
                Map<String, Serializable> personPrefixProps = new HashMap<String, Serializable>(personProps.size());
                for (QName propQName : personProps.keySet())
                {
                    try
                    {
                        String propPrefix = propQName.toPrefixString(namespaceService);
                        personPrefixProps.put(propPrefix, personProps.get(propQName));
                    }
                    catch (NamespaceException ne)
                    {
                        // ignore properties that do not have a registered namespace
                        logger.warn("Ignoring property '" + propQName + "' as it's namespace is not registered");
                    }
                }
                
                model.put("personProps", personPrefixProps);
                
                // send
                sendMail(emailTemplateRef, emailAddress, subjectText, model);
                
                return new Pair<Integer, Long>(activityFeedModels.size(), userMaxFeedId);
            }
        }
        
        return null;
    }
    
    protected void sendMail(NodeRef emailTemplateRef, String emailAddress, String subjectText, Map<String, Object> model)
    {
        ParameterCheck.mandatoryString("emailAddress", emailAddress);
        
        Action mail = actionService.createAction(MailActionExecuter.NAME);
        
        mail.setParameterValue(MailActionExecuter.PARAM_TO, emailAddress);
        mail.setParameterValue(MailActionExecuter.PARAM_SUBJECT, subjectText);
        
        //mail.setParameterValue(MailActionExecuter.PARAM_TEXT, buildMailText(emailTemplateRef, model));
        mail.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, emailTemplateRef);
        mail.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable)model);
        
        actionService.executeAction(mail, null);
    }
    
    protected String buildSubjectText(long currentTime)
    {
        return I18NUtil.getMessage(MSG_EMAIL_SUBJECT, ModelUtil.getProductName(repoAdminService));
    }
    
    protected NodeRef getEmailTemplateRef()
    {
        StoreRef store = feedEmailTemplateLocation.getStoreRef();
        String xpath = feedEmailTemplateLocation.getPath();
        
        if (! feedEmailTemplateLocation.getQueryLanguage().equals(SearchService.LANGUAGE_XPATH))
        {
            logger.warn("Cannot find the activities email template - repository location query language is not 'xpath': "+feedEmailTemplateLocation.getQueryLanguage());
            return null;
        }
        
        List<NodeRef> nodeRefs = searchService.selectNodes(nodeService.getRootNode(store), xpath, null, namespaceService, false);
        if (nodeRefs.size() != 1)
        {
            logger.warn("Cannot find the activities email template: "+xpath);
            return null;
        }
        
        return fileFolderService.getLocalizedSibling(nodeRefs.get(0));
    }
    
    protected void addSiteName(String siteId, Map<String, String> siteNames)
    {
        if (siteId == null)
        {
            return;
        }
        
        String siteName = siteNames.get(siteId);
        if (siteName == null)
        {
            SiteInfo site = siteService.getSite(siteId);
            if (site == null)
            {
                return;
            }
            
            String siteTitle = site.getTitle();
            if (siteTitle != null && siteTitle.length() > 0)
            {
                siteName = siteTitle;
            }
            else
            {
                siteName = siteId;
            }
            
            siteNames.put(siteId, siteName);
        }
    }
    
    protected boolean skipUser(Boolean emailFeedDisabled, String feedUserId, String emailAddress, List<String> excludedEmailSuffixes)
    {
        if ((emailFeedDisabled != null) && (emailFeedDisabled == true))
        {
            return true;
        }
        
        if (authenticationContext.isSystemUserName(feedUserId) || authenticationContext.isGuestUserName(feedUserId))
        {
            // skip "guest" or "System" user
            return true;
        }
        
        if ((emailAddress == null) || (emailAddress.length() <= 0))
        {
            // skip user that does not have an email address
            if (logger.isDebugEnabled())
            {
                logger.debug("Skip for '"+feedUserId+"' since they have no email address set");
            }
            return true;
        }
        
        String lowerEmailAddress = emailAddress.toLowerCase();
        for (String excludedEmailSuffix : excludedEmailSuffixes)
        {
            if (lowerEmailAddress.endsWith(excludedEmailSuffix.toLowerCase()))
            {
                // skip user whose email matches exclude suffix
                if (logger.isDebugEnabled())
                {
                    logger.debug("Skip for '"+feedUserId+"' since email address is excluded ("+emailAddress+")");
                }
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Lazily update the job lock
     */
    private void refreshLock()
    {
        Pair<Long, String> lockPair = lockThreadLocal.get();
        if (lockPair == null)
        {
            String lockToken = jobLockService.getLock(LOCK_QNAME, LOCK_TTL);
            Long lastLock = new Long(System.currentTimeMillis());
            // We have not locked before
            lockPair = new Pair<Long, String>(lastLock, lockToken);
            lockThreadLocal.set(lockPair);
        }
        else
        {
            long now = System.currentTimeMillis();
            long lastLock = lockPair.getFirst().longValue();
            String lockToken = lockPair.getSecond();
            // Only refresh the lock if we are past a threshold
            if (now - lastLock > (long)(LOCK_TTL/2L))
            {
                jobLockService.refreshLock(lockToken, LOCK_QNAME, LOCK_TTL);
                lastLock = System.currentTimeMillis();
                lockPair = new Pair<Long, String>(lastLock, lockToken);
            }
        }
    }
    
    /**
     * Release the lock after the job completes
     */
    private void releaseLock()
    {
        Pair<Long, String> lockPair = lockThreadLocal.get();
        if (lockPair != null)
        {
            // We can't release without a token
            try
            {
                jobLockService.releaseLock(lockPair.getSecond(), LOCK_QNAME);
            }
            finally
            {
                // Reset
                lockThreadLocal.set(null);
            }
        }
        // else: We can't release without a token
    }
}
