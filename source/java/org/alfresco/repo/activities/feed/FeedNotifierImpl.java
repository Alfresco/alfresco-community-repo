/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.dictionary.RepositoryLocation;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.JobLockService.JobLockRefreshCallback;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ModelUtil;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.UrlUtil;
import org.alfresco.util.VmShutdownListener;
import org.alfresco.util.VmShutdownListener.VmShutdownException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Implementation of the Activity Feed Notifier component
 * 
 * Note: currently implemented to email activities stored in JSON format
 * 
 * @since 4.0
 */
public class FeedNotifierImpl implements FeedNotifier, ApplicationContextAware
{
    protected static Log logger = LogFactory.getLog(FeedNotifier.class);
    
    private static final QName LOCK_QNAME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ActivityFeedNotifier");
    private static final long LOCK_TTL = 30000L;
    
    private static VmShutdownListener vmShutdownListener = new VmShutdownListener(FeedNotifierImpl.class.getName());
    
    private static final String MSG_EMAIL_SUBJECT = "activities.feed.notifier.email.subject";
    
    private NamespaceService namespaceService;
    private FileFolderService fileFolderService;
    private SearchService searchService;
    private PersonService personService;
    private NodeService nodeService;
    private JobLockService jobLockService;
    private TransactionService transactionService;
    private SysAdminParams sysAdminParams;
    private RepoAdminService repoAdminService;
    private UserNotifier userNotifier;
    
    private ApplicationContext applicationContext;
    
    private RepositoryLocation feedEmailTemplateLocation;
    
    private int numThreads = 4;
    private int batchSize = 200;
    
    public void setNumThreads(int numThreads)
    {
    	this.numThreads = numThreads;
    }
    
    public void setBatchSize(int batchSize)
    {
    	this.batchSize = batchSize;
    }
    
	public void setUserNotifier(UserNotifier userNotifier)
    {
		this.userNotifier = userNotifier;
	}
    
    public void setFileFolderService(FileFolderService fileFolderService)
    {
		this.fileFolderService = fileFolderService;
	}

	public void setSearchService(SearchService searchService)
	{
		this.searchService = searchService;
	}

	public void setNamespaceService(NamespaceService namespaceService)
	{
		this.namespaceService = namespaceService;
	}

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }
    
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    public void setSysAdminParams(SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
    }
    
    public void setRepoAdminService(RepoAdminService repoAdminService)
    {
        this.repoAdminService = repoAdminService;
    }
    
    /**
     * Perform basic checks to ensure that the necessary dependencies were injected.
     */
    protected void checkProperties()
    {
        PropertyCheck.mandatory(this, "personService", personService);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "jobLockService", jobLockService);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "sysAdminParams", sysAdminParams);
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

        String lockToken = null;
        // Use a flag to keep track of the running job
        final AtomicBoolean running = new AtomicBoolean(true);

        try
        {
            lockToken = jobLockService.getLock(LOCK_QNAME, LOCK_TTL);
            if (lockToken == null)
            {
                logger.info("Can't get lock. Assume multiple feed notifiers...");
                return;
            }
            
            if (logger.isTraceEnabled())
            {
                logger.trace("Activities email notification started");
            }

            jobLockService.refreshLock(lockToken, LOCK_QNAME, LOCK_TTL, new JobLockRefreshCallback()
            {
                @Override
                public boolean isActive()
                {
                    return running.get();
                }

                @Override
                public void lockReleased()
                {
                    running.set(false);
                }
            });

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
            // The lock will self-release if answer isActive in the negative
            running.set(false);
            if (lockToken != null)
            {
                jobLockService.releaseLock(lockToken, LOCK_QNAME);
            }
        }
    }

    public void setFeedEmailTemplateLocation(RepositoryLocation feedEmailTemplateLocation)
    {
        this.feedEmailTemplateLocation = feedEmailTemplateLocation;
    }
    
    protected String getEmailTemplateRef()
    {
        String locationType = feedEmailTemplateLocation.getQueryLanguage();
        
        if (locationType.equals(SearchService.LANGUAGE_XPATH))
        {
            StoreRef store = feedEmailTemplateLocation.getStoreRef();
            String xpath = feedEmailTemplateLocation.getPath();
            
            try
            {
                if (! feedEmailTemplateLocation.getQueryLanguage().equals(SearchService.LANGUAGE_XPATH))
                {
                    logger.error("Cannot find the activities email template - repository location query language is not 'xpath': "+feedEmailTemplateLocation.getQueryLanguage());
                    return null;
                }
                
                List<NodeRef> nodeRefs = searchService.selectNodes(nodeService.getRootNode(store), xpath, null, namespaceService, false);
                if (nodeRefs.size() != 1)
                {
                    logger.error("Cannot find the activities email template: "+xpath);
                    return null;
                }
                
                return fileFolderService.getLocalizedSibling(nodeRefs.get(0)).toString();
            } 
            catch (SearcherException e)
            {
                logger.error("Cannot find the email template!", e);
            }
            
            return null;
        }
        else if (locationType.equals(RepositoryLocation.LANGUAGE_CLASSPATH))
        {
            return feedEmailTemplateLocation.getPath();
        }
        else
        {
            logger.error("Unsupported location type: "+locationType);
            return null;
        }
    }
    
    private void executeInternal(final int repeatIntervalMins)
    {
        final String emailTemplateRef = getEmailTemplateRef();
        
        if (emailTemplateRef == null)
        {
            return;
        }

        final String shareUrl = UrlUtil.getShareUrl(sysAdminParams); 

        if (logger.isDebugEnabled())
        {
            logger.debug("Share URL configured as: "+shareUrl);
        }

        final AtomicInteger userCnt = new AtomicInteger(0);
        final AtomicInteger feedEntryCnt = new AtomicInteger(0);

        final long startTime = System.currentTimeMillis();

        // local cache for this execution
        final Map<String, String> siteNames = new ConcurrentHashMap<String, String>(10);

        try
        {
            final String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
            final String tenantDomain = TenantUtil.getCurrentDomain();

            // process the feeds using the batch processor {@link BatchProcessor}
	        BatchProcessor.BatchProcessWorker<PersonInfo> worker = new BatchProcessor.BatchProcessWorker<PersonInfo>()
	        {
	            public String getIdentifier(final PersonInfo person)
	            {
	            	StringBuilder sb = new StringBuilder("Person ");
	                sb.append(person.getUserName());
	                return sb.toString();
	            }

	            public void beforeProcess() throws Throwable
	            {
	                AuthenticationUtil.setRunAsUser(currentUser);
	            }

	            public void afterProcess() throws Throwable
	            {
	            }

	            public void process(final PersonInfo person) throws Throwable
	            {
	            	final RetryingTransactionHelper txHelper = transactionService.getRetryingTransactionHelper();
                    txHelper.setMaxRetries(0);

                    TenantUtil.runAsTenant(new TenantRunAsWork<Void>()
                    {
                        @Override
                        public Void doWork() throws Exception
                        {
                            txHelper.doInTransaction(new RetryingTransactionCallback<Void>()
                            {
                                public Void execute() throws Throwable
                                {
                                    processInternal(person);
                                    return null;
                                }
                            }, false, true);
                            return null;
                        }
                    }, tenantDomain);
	            }
	            
                private void processInternal(final PersonInfo person) throws Exception
                {
                    final NodeRef personNodeRef = person.getNodeRef();
                    try
                    {
                        Pair<Integer, Long> result = userNotifier.notifyUser(personNodeRef, MSG_EMAIL_SUBJECT, new Object[] {ModelUtil.getProductName(repoAdminService)}, siteNames, shareUrl, repeatIntervalMins, emailTemplateRef);
                        if (result != null)
                        {
                            int entryCnt = result.getFirst();
                            final long maxFeedId = result.getSecond();

	                        Long currentMaxFeedId = (Long)nodeService.getProperty(personNodeRef, ContentModel.PROP_EMAIL_FEED_ID);
	                        if ((currentMaxFeedId == null) || (currentMaxFeedId < maxFeedId))
	                        {
	                        	nodeService.setProperty(personNodeRef, ContentModel.PROP_EMAIL_FEED_ID, maxFeedId);
	                        }

	                        userCnt.incrementAndGet();
	                        feedEntryCnt.addAndGet(entryCnt);
	                    }
	                }
	                catch (InvalidNodeRefException inre)
	                {
	                    // skip this person - eg. no longer exists ?
	                    logger.warn("Skip feed notification for user ("+personNodeRef+"): " + inre.getMessage());
	                }
		        }
	        };

	        // grab people for the batch processor in chunks of size batchSize
	        BatchProcessWorkProvider<PersonInfo> provider = new BatchProcessWorkProvider<PersonInfo>()
			{
	        	private int skip = 0;
	        	private int maxItems = batchSize;
                private boolean hasMore = true;

				@Override
				public int getTotalEstimatedWorkSize()
				{
					return personService.countPeople();
				}

				@Override
                public Collection<PersonInfo> getNextWork()
                {
                    if (!hasMore)
                    {
                        return Collections.emptyList();
                    }
                    PagingResults<PersonInfo> people = personService.getPeople(null, null, null, new PagingRequest(skip, maxItems));
                    List<PersonInfo> page = people.getPage();
                    skip += page.size();
                    hasMore = people.hasMoreItems();
                    return page;
                }
	    };

            final RetryingTransactionHelper txHelper = transactionService.getRetryingTransactionHelper();
            txHelper.setMaxRetries(0);

	        new BatchProcessor<PersonInfo>(
	                "FeedNotifier",
	                txHelper,
	                provider,
	                numThreads, batchSize,
	                applicationContext,
	                logger, 100).process(worker, true);
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
        	int count = userCnt.get();
        	int entryCount = feedEntryCnt.get();

            // assume sends are synchronous - hence bump up to last max feed id
            if (count > 0)
            {
                if (logger.isInfoEnabled())
                {
                    // TODO i18n of info message
                    StringBuilder sb = new StringBuilder();
                    sb.append("Notified ").append(userCnt).append(" user").append(count != 1 ? "s" : "");
                    sb.append(" of ").append(feedEntryCnt).append(" activity feed entr").append(entryCount != 1 ? "ies" : "y");
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
    
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
	{
		this.applicationContext = applicationContext;
	}
}
