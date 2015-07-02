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
package org.alfresco.repo.security.sync;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.dictionary.constraint.NameChecker;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.management.subsystems.ChildApplicationContextManager;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.authentication.AuthenticatorDeletedEvent;
import org.alfresco.repo.security.authority.UnknownAuthorityException;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.TraceableThreadFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.extensions.surf.util.I18NUtil;

import javax.management.*;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A <code>ChainingUserRegistrySynchronizer</code> is responsible for synchronizing Alfresco's local user (person) and
 * group (authority) information with the external subsystems in the authentication chain (most typically LDAP
 * directories). When the {@link #synchronize(boolean, boolean)} method is called, it visits each {@link UserRegistry} bean in
 * the 'chain' of application contexts, managed by a {@link ChildApplicationContextManager}, and compares its
 * timestamped user and group information with the local users and groups last retrieved from the same source. Any
 * updates and additions made to those users and groups are applied to the local copies. The ordering of each
 * {@link UserRegistry} in the chain determines its precedence when it comes to user and group name collisions. The
 * {@link JobLockService} is used to ensure that in a cluster, no two nodes actually run a synchronize at the same time.
 * <p>
 * The <code>force</code> argument determines whether a complete or partial set of information is queried from the
 * {@link UserRegistry}. When <code>true</code> then <i>all</i> users and groups are queried. With this complete set of
 * information, the synchronizer is able to identify which users and groups have been deleted, so it will delete users
 * and groups as well as update and create them. Since processing all users and groups may be fairly time consuming, it
 * is recommended this mode is only used by a background scheduled synchronization job. When the argument is
 * <code>false</code> then only those users and groups modified since the most recent modification date of all the
 * objects last queried from the same {@link UserRegistry} are retrieved. In this mode, local users and groups are
 * created and updated, but not deleted (except where a name collision with a lower priority {@link UserRegistry} is
 * detected). This 'differential' mode is much faster, and by default is triggered on subsystem startup and also by
 * {@link #createMissingPerson(String)} when a user is successfully authenticated who doesn't yet have a local person
 * object in Alfresco. This should mean that new users and their group information are pulled over from LDAP servers as
 * and when required.
 * 
 * @author dward
 */
public class ChainingUserRegistrySynchronizer extends AbstractLifecycleBean 
    implements UserRegistrySynchronizer,
        ChainingUserRegistrySynchronizerStatus,
        TestableChainingUserRegistrySynchronizer,
        ApplicationEventPublisherAware
        
{
    /** The logger. */
    private static final Log logger = LogFactory.getLog(ChainingUserRegistrySynchronizer.class);

    /** The name of the lock used to ensure that a synchronize does not run on more than one node at the same time. */
    private static final QName LOCK_QNAME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI,
            "ChainingUserRegistrySynchronizer");

    /** The time this lock will persist for in the database (now only 2 minutes but refreshed at regular intervals). */
    private static final long LOCK_TTL = 1000 * 60 * 2;

    /** The path in the attribute service below which we persist attributes. */
    public static final String ROOT_ATTRIBUTE_PATH = ".ChainingUserRegistrySynchronizer";

    /** The label under which the last group modification timestamp is stored for each zone. */
    private static final String GROUP_LAST_MODIFIED_ATTRIBUTE = "GROUP";

    /** The label under which the last user modification timestamp is stored for each zone. */
    private static final String PERSON_LAST_MODIFIED_ATTRIBUTE = "PERSON";
    
    /** The label under which the status is stored for each zone. */
    private static final String STATUS_ATTRIBUTE = "STATUS";
    
    /** The label under which the status is stored for each zone. */
    private static final String LAST_ERROR_ATTRIBUTE = "LAST_ERROR";

    /** The label under which the status is stored for each zone. */
    private static final String START_TIME_ATTRIBUTE = "START_TIME";
    
    /** The label under which the status is stored for each zone. */
    private static final String END_TIME_ATTRIBUTE = "END_TIME";
    
    /** The label under which the status is stored for each zone. */
    private static final String SERVER_ATTRIBUTE = "LAST_RUN_HOST";
        
    /** The label under which the status is stored for each zone. */
    private static final String SUMMARY_ATTRIBUTE = "SUMMARY";
    
    /** The manager for the autentication chain to be traversed. */
    private ChildApplicationContextManager applicationContextManager;

    /** The name used to look up a {@link UserRegistry} bean in each child application context. */
    private String sourceBeanName;

    /** The authority service. */
    private AuthorityService authorityService;

    /** The person service. */
    private PersonService personService;

    /** The attribute service. */
    private AttributeService attributeService;

    /** The transaction service. */
    private TransactionService transactionService;

    /** The job lock service. */
    private JobLockService jobLockService;

    /** The application event publisher. */
    private ApplicationEventPublisher applicationEventPublisher;

    /** Should we trigger a differential sync when missing people log in?. */
    private boolean syncWhenMissingPeopleLogIn = true;

    /** Should we trigger a differential sync on startup?. */
    private boolean syncOnStartup = true;

    /** Should we auto create a missing person on log in?. */
    private boolean autoCreatePeopleOnLogin = true;

    /** The number of entries to process before reporting progress. */
    private int loggingInterval = 100;

    /** The number of worker threads. */
    private int workerThreads = 2;
    
    private MBeanServerConnection mbeanServer;

    /** Allow a full sync to perform deletions? */
    private boolean allowDeletions = true;
    
    /** Controls whether to query for users and groups that have been deleted in LDAP */
    private boolean syncDelete = true;

    /** Validates person names over cm:filename constraint **/
    private NameChecker nameChecker;

    private SysAdminParams sysAdminParams;
    
    public void init()
    {
        PropertyCheck.mandatory(this, "attributeService", attributeService);
        PropertyCheck.mandatory(this, "authorityService", authorityService);
        PropertyCheck.mandatory(this, "personService", personService);
        PropertyCheck.mandatory(this, "attributeService", attributeService);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "jobLockService", jobLockService);
        PropertyCheck.mandatory(this, "applicationEventPublisher", applicationEventPublisher);
        PropertyCheck.mandatory(this, "sysAdminParams", sysAdminParams);
    }

    /**
     * Sets name checker
     */
    public void setNameChecker(NameChecker nameChecker)
    {
        this.nameChecker = nameChecker;
    }

    /**
     * Sets the application context manager.
     * 
     * @param applicationContextManager
     *            the applicationContextManager to set
     */
    public void setApplicationContextManager(ChildApplicationContextManager applicationContextManager)
    {
        this.applicationContextManager = applicationContextManager;
    }

    /**
     * Sets the name used to look up a {@link UserRegistry} bean in each child application context.
     * 
     * @param sourceBeanName
     *            the bean name
     */
    public void setSourceBeanName(String sourceBeanName)
    {
        this.sourceBeanName = sourceBeanName;
    }

    /**
     * Sets the authority service.
     * 
     * @param authorityService
     *            the new authority service
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /**
     * Sets the person service.
     * 
     * @param personService
     *            the new person service
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    /**
     * Sets the attribute service.
     * 
     * @param attributeService
     *            the new attribute service
     */
    public void setAttributeService(AttributeService attributeService)
    {
        this.attributeService = attributeService;
    }

    /**
     * Sets the transaction service.
     * 
     * @param transactionService
     *            the transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * Sets the job lock service.
     * 
     * @param jobLockService
     *            the job lock service
     */
    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.springframework.context.ApplicationEventPublisherAware#setApplicationEventPublisher(org.springframework.context
     * .ApplicationEventPublisher)
     */
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher)
    {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Controls whether we auto create a missing person on log in.
     * 
     * @param autoCreatePeopleOnLogin
     *            <code>true</code> if we should auto create a missing person on log in
     */
    public void setAutoCreatePeopleOnLogin(boolean autoCreatePeopleOnLogin)
    {
        this.autoCreatePeopleOnLogin = autoCreatePeopleOnLogin;
    }

    /**
     * Controls whether we trigger a differential sync when missing people log in.
     * 
     * @param syncWhenMissingPeopleLogIn
     *            <codetrue</code> if we should trigger a sync when missing people log in
     */
    public void setSyncWhenMissingPeopleLogIn(boolean syncWhenMissingPeopleLogIn)
    {
        this.syncWhenMissingPeopleLogIn = syncWhenMissingPeopleLogIn;
    }

    /**
     * Controls whether we trigger a differential sync when the subsystem starts up.
     * 
     * @param syncOnStartup
     *            <codetrue</code> if we should trigger a sync on startup
     */
    public void setSyncOnStartup(boolean syncOnStartup)
    {
        this.syncOnStartup = syncOnStartup;
    }

    /**
     * Sets the number of entries to process before reporting progress.
     * 
     * @param loggingInterval
     *            the number of entries to process before reporting progress or zero to disable progress reporting.
     */
    public void setLoggingInterval(int loggingInterval)
    {
        this.loggingInterval = loggingInterval;
    }

    /**
     * Sets the number of worker threads.
     * 
     * @param workerThreads
     *            the number of worker threads
     */
    public void setWorkerThreads(int workerThreads)
    {
        this.workerThreads = workerThreads;
    }
    
    /**
     * Controls how deleted users and groups are handled.
     * By default is set to true.
     * 
     * @param allowDeletions
     *            If <b>true</b> the entries are deleted from alfresco.
     *            If <b>false</b> then they are unlinked from their LDAP authentication zone but remain within alfresco.
     */
    public void setAllowDeletions(boolean allowDeletions)
    {
        this.allowDeletions = allowDeletions;
    }

    /**
     * Controls whether to query for users and groups that have been deleted in LDAP.
     * For large LDAP directories the delete query is expensive and time consuming, needing to read the entire LDAP directory.
     * By default is set to true.
     * 
     * @param syncDelete
     *            If <b>false</b> then LDAP sync does not even attempt to search for deleted users. 
     */
    public void setSyncDelete(boolean syncDelete)
    {
        this.syncDelete = syncDelete;
    }
    
    @Override
    public SynchronizeDiagnostic testSynchronize(String authenticatorName)
    {
        SynchronizeDiagnosticImpl ret = new SynchronizeDiagnosticImpl();
        
        Collection<String> instanceIds = this.applicationContextManager.getInstanceIds();
        
        if(instanceIds.contains(authenticatorName))
        {
            UserRegistry plugin;
            
            ApplicationContext context = this.applicationContextManager.getApplicationContext(authenticatorName);
            plugin = (UserRegistry) context.getBean(this.sourceBeanName);
            
            // If the bean is ActivateableBean check whether it is active
            if (plugin instanceof ActivateableBean)
            {
                if(!((ActivateableBean) plugin).isActive())
                {
                    ret.setActive(false);
                }
            }
            
            long groupLastModifiedMillis = getMostRecentUpdateTime(
                    ChainingUserRegistrySynchronizer.GROUP_LAST_MODIFIED_ATTRIBUTE, 
                    authenticatorName, false);
            
            long personLastModifiedMillis = getMostRecentUpdateTime(
                    ChainingUserRegistrySynchronizer.PERSON_LAST_MODIFIED_ATTRIBUTE, 
                    authenticatorName, false);
            
            Date groupLastModified = groupLastModifiedMillis == -1 ? null : new Date(groupLastModifiedMillis);
            Date personLastModified = personLastModifiedMillis == -1 ? null : new Date(personLastModifiedMillis);

            ret.setGroups(plugin.getGroupNames());

            ret.setUsers(plugin.getPersonNames());
            if(groupLastModified != null)
            {
                ret.setGroupLastSynced(groupLastModified);
            }
            else
            {   // fake a date to test the group query
                groupLastModified= new Date();
            }
            plugin.getGroups(groupLastModified);
            if(personLastModified != null)
            {
                ret.setPersonLastSynced(personLastModified);
            }
            else
            {
                // fake a date to test the person query
                personLastModified= new Date();
            }
            plugin.getPersons(personLastModified);

            return ret;
        }
        
        Object params[] = {authenticatorName};
        throw new AuthenticationException("authentication.err.validation.authenticator.notfound", params);
    }

    @Override
    public void synchronize(boolean forceUpdate, boolean isFullSync)
    {
        synchronizeInternal(forceUpdate, isFullSync, true);
    }

    private void synchronizeInternal(boolean forceUpdate, boolean isFullSync, final boolean splitTxns)
    {
        if (ChainingUserRegistrySynchronizer.logger.isDebugEnabled())
        {
            
            if (forceUpdate)
            {
                ChainingUserRegistrySynchronizer.logger.debug("Running a full sync.");
            }
            else
            {
                ChainingUserRegistrySynchronizer.logger.debug("Running a differential sync.");
            }
            if (allowDeletions)
            {
                ChainingUserRegistrySynchronizer.logger.debug("deletions are allowed");
            }
            else
            {
                ChainingUserRegistrySynchronizer.logger.debug("deletions are not allowed");
            }
            // Don't proceed with the sync if the repository is read only
            if (this.transactionService.isReadOnly())
            {
                ChainingUserRegistrySynchronizer.logger
                        .warn("Unable to proceed with user registry synchronization. Repository is read only.");
                return;
            }
        }

        // Don't proceed with the sync if the repository is read only
        if (this.transactionService.isReadOnly())
        {
            ChainingUserRegistrySynchronizer.logger
                    .warn("Unable to proceed with user registry synchronization. Repository is read only.");
            return;
        }

        // Create a background executor that will refresh our lock. This means we can request a lock with a relatively
        // small persistence time and not worry about it lasting after server restarts. Note we use an independent
        // executor because this is a compound operation that spans accross multiple batch processors.
        String lockToken = null;
        TraceableThreadFactory threadFactory = new TraceableThreadFactory();
        threadFactory.setNamePrefix("ChainingUserRegistrySynchronizer lock refresh");
        threadFactory.setThreadDaemon(true);
        ScheduledExecutorService lockRefresher = new ScheduledThreadPoolExecutor(1, threadFactory);

        // Let's ensure all exceptions get logged
        try
        {
            // First, try to obtain a lock to ensure we are the only node trying to run this job
            try
            {
                if (splitTxns)
                {
                    // If this is an automated sync on startup or scheduled sync, don't even wait around for the lock.
                    // Assume the sync will be completed on another node.
                    lockToken = this.transactionService.getRetryingTransactionHelper().doInTransaction(
                            new RetryingTransactionCallback<String>()
                            {
                                public String execute() throws Throwable
                                {
                                    return ChainingUserRegistrySynchronizer.this.jobLockService.getLock(
                                            ChainingUserRegistrySynchronizer.LOCK_QNAME,
                                            ChainingUserRegistrySynchronizer.LOCK_TTL, 0, 1);
                                }
                            }, false, splitTxns);
                }
                else
                {
                    // If this is a login-triggered sync, give it a few retries before giving up
                    lockToken = this.jobLockService.getLock(ChainingUserRegistrySynchronizer.LOCK_QNAME,
                            ChainingUserRegistrySynchronizer.LOCK_TTL, 3000, 10);
                }
            }
            catch (LockAcquisitionException e)
            {
                // Don't proceed with the sync if it is running on another node
                ChainingUserRegistrySynchronizer.logger
                        .warn("User registry synchronization already running in another thread. Synchronize aborted");
                return;
            }

            // Schedule the lock refresh to run at regular intervals
            final String token = lockToken;
            lockRefresher.scheduleAtFixedRate(new Runnable()
            {
                public void run()
                {
                    ChainingUserRegistrySynchronizer.this.transactionService.getRetryingTransactionHelper()
                            .doInTransaction(new RetryingTransactionCallback<Object>()
                            {
                                public Object execute() throws Throwable
                                {
                                    ChainingUserRegistrySynchronizer.this.jobLockService.refreshLock(token,
                                            ChainingUserRegistrySynchronizer.LOCK_QNAME,
                                            ChainingUserRegistrySynchronizer.LOCK_TTL);
                                    return null;
                                }
                            }, false, splitTxns);
                }
            }, ChainingUserRegistrySynchronizer.LOCK_TTL / 2, ChainingUserRegistrySynchronizer.LOCK_TTL / 2,
                    TimeUnit.MILLISECONDS);

            Set<String> visitedZoneIds = new TreeSet<String>();
            Collection<String> instanceIds = this.applicationContextManager.getInstanceIds();
            
            // Work out the set of all zone IDs in the authentication chain so that we can decide which users / groups
            // need 're-zoning'
            Set<String> allZoneIds = new TreeSet<String>();
            for (String id : instanceIds)
            {
                allZoneIds.add(AuthorityService.ZONE_AUTH_EXT_PREFIX + id);
            }
            
            // Collect the plugins that we can sync : zoneId, plugin
            Map<String, UserRegistry> plugins = new HashMap<String, UserRegistry>();
            
            for (String id : instanceIds)
            {   
                UserRegistry plugin;
                try
                {
                    ApplicationContext context = this.applicationContextManager.getApplicationContext(id);
                    plugin = (UserRegistry) context.getBean(this.sourceBeanName);
                }
                catch (RuntimeException e)
                {
                    // The bean doesn't exist or this subsystem won't start. The reason would have been logged. Ignore and continue.
                    continue;
                }
                
                if (!(plugin instanceof ActivateableBean) || ((ActivateableBean) plugin).isActive())
                {
                    // yes this plugin needs to be synced
                    plugins.put(id, plugin);
                }
            }
            
            /**
             *  Sync starts here
             */
            notifySyncStart(plugins.keySet());

            for (String id : instanceIds)
            {    
                UserRegistry plugin = plugins.get(id);
                
                if (plugin != null)
                {
                    // If debug is enabled then dump out the contents of the authentication JMX bean
                    if (ChainingUserRegistrySynchronizer.logger.isDebugEnabled())
                    {
                        mbeanServer = (MBeanServerConnection) getApplicationContext().getBean("alfrescoMBeanServer");
                        try
                        {
                            StringBuilder nameBuff = new StringBuilder(200).append("Alfresco:Type=Configuration,Category=Authentication,id1=managed,id2=").append(
                                    URLDecoder.decode(id, "UTF-8"));
                            ObjectName name = new ObjectName(nameBuff.toString());
                            if (mbeanServer != null && mbeanServer.isRegistered(name))
                            {
                                MBeanInfo info = mbeanServer.getMBeanInfo(name);
                                MBeanAttributeInfo[] attributes = info.getAttributes();
                                ChainingUserRegistrySynchronizer.logger.debug(id + " attributes:");
                                for (MBeanAttributeInfo attribute : attributes)
                                {
                                    Object value = mbeanServer.getAttribute(name, attribute.getName());
                                    ChainingUserRegistrySynchronizer.logger.debug(attribute.getName() + " = " + value);
                                }
                            }
                        }
                        catch(UnsupportedEncodingException e)
                        {
                            if (ChainingUserRegistrySynchronizer.logger.isWarnEnabled())
                            {
                                ChainingUserRegistrySynchronizer.logger
                                .warn("Exception during logging", e);
                            }
                        }
                        catch (MalformedObjectNameException e)
                        {
                            if (ChainingUserRegistrySynchronizer.logger.isWarnEnabled())
                            {
                                ChainingUserRegistrySynchronizer.logger
                                .warn("Exception during logging", e);
                            }
                        }
                        catch (InstanceNotFoundException e)
                        {
                            if (ChainingUserRegistrySynchronizer.logger.isWarnEnabled())
                            {
                                ChainingUserRegistrySynchronizer.logger
                                .warn("Exception during logging", e);
                            }
                        }
                        catch (IntrospectionException e)
                        {
                            if (ChainingUserRegistrySynchronizer.logger.isWarnEnabled())
                            {
                                ChainingUserRegistrySynchronizer.logger
                                .warn("Exception during logging", e);
                            }
                        }
                        catch (AttributeNotFoundException e)
                        {
                            if (ChainingUserRegistrySynchronizer.logger.isWarnEnabled())
                            {
                                ChainingUserRegistrySynchronizer.logger
                                .warn("Exception during logging", e);
                            }
                        }
                        catch (ReflectionException e)
                        {
                            if (ChainingUserRegistrySynchronizer.logger.isWarnEnabled())
                            {
                                ChainingUserRegistrySynchronizer.logger
                                .warn("Exception during logging", e);
                            }
                        }
                        catch (MBeanException e)
                        {
                            if (ChainingUserRegistrySynchronizer.logger.isWarnEnabled())
                            {
                                ChainingUserRegistrySynchronizer.logger
                                .warn("Exception during logging", e);
                            }
                        }
                        catch (IOException e)
                        {
                            if (ChainingUserRegistrySynchronizer.logger.isWarnEnabled())
                            {
                                ChainingUserRegistrySynchronizer.logger
                                .warn("Exception during logging", e);
                            }
                        }
                    } // end of debug dump of active JMX bean
                    if (ChainingUserRegistrySynchronizer.logger.isInfoEnabled())
                    {
                        ChainingUserRegistrySynchronizer.logger
                                .info("Synchronizing users and groups with user registry '" + id + "'");
                    }
                    if (isFullSync && ChainingUserRegistrySynchronizer.logger.isWarnEnabled())
                    {
                        ChainingUserRegistrySynchronizer.logger
                                .warn("Full synchronization with user registry '"
                                        + id + "'");
                        if (allowDeletions)
                        {
                            ChainingUserRegistrySynchronizer.logger
                                    .warn("Some users and groups previously created by synchronization with this user registry may be removed.");
                        }
                        else
                        {
                            ChainingUserRegistrySynchronizer.logger
                                    .warn("Deletions are disabled. Users and groups removed from this registry will be logged only and will remain in the repository. Users previously found in a different registry will be moved in the repository rather than recreated.");
                        }
                    }
                    // Work out whether we should do the work in a separate transaction (it's most performant if we
                    // bunch it into small transactions, but if we are doing a sync on login, it has to be the same
                    // transaction)
                    boolean requiresNew = splitTxns
                            || AlfrescoTransactionSupport.getTransactionReadState() == TxnReadState.TXN_READ_ONLY;

                    try 
                    {    
                       /**
                        * Do the sync with the specified plugin
                        */
                       syncWithPlugin(id, plugin, forceUpdate, isFullSync, requiresNew, visitedZoneIds, allZoneIds);

                       this.applicationEventPublisher.publishEvent(new SynchronizeDirectoryEndEvent(this, id));                       
                    }
                    catch (final RuntimeException e)
                    {
                        notifySyncDirectoryEnd(id, e);
                        throw e;
                    }
                } // if plugin exists
            } // for each instanceId
            
            //End of successful synchronization here
            notifySyncEnd();            
        }
        catch (final RuntimeException e)
        {
            notifySyncEnd(e);
            ChainingUserRegistrySynchronizer.logger.error("Synchronization aborted due to error", e);
            throw e;
        }
        finally
        {            
            // Release the lock if necessary
            if (lockToken != null)
            {
                // Cancel the lock refresher
                // Because we may hit a perfect storm when trying to interrupt workers in their unsynchronized getTask()
                // method we can't wait indefinitely and may have to retry the shutdown
                int trys = 0;
                do
                {
                    lockRefresher.shutdown();
                    try
                    {
                        lockRefresher.awaitTermination(ChainingUserRegistrySynchronizer.LOCK_TTL, TimeUnit.MILLISECONDS);
                    }
                    catch (InterruptedException e)
                    {
                    }
                }
                while (!lockRefresher.isTerminated() && trys++ < 3);
                if (!lockRefresher.isTerminated())
                {
                    lockRefresher.shutdownNow();
                    ChainingUserRegistrySynchronizer.logger.error("Failed to shut down lock refresher");
                }

                final String token = lockToken;
                this.transactionService.getRetryingTransactionHelper().doInTransaction(
                        new RetryingTransactionCallback<Object>()
                        {
                            public Object execute() throws Throwable
                            {
                                ChainingUserRegistrySynchronizer.this.jobLockService.releaseLock(token,
                                        ChainingUserRegistrySynchronizer.LOCK_QNAME);
                                return null;
                            }
                        }, false, splitTxns);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.sync.UserRegistrySynchronizer#getPersonMappedProperties(java.lang.String)
     */
    public Set<QName> getPersonMappedProperties(String username)
    {
        Set<String> authorityZones = this.authorityService.getAuthorityZones(username);
        if (authorityZones == null)
        {
            return Collections.emptySet();
        }
        Collection<String> instanceIds = this.applicationContextManager.getInstanceIds();

        // Visit the user registries in priority order and return the person mapping of the first registry that matches
        // one of the person's zones
        for (String id : instanceIds)
        {
            String zoneId = AuthorityService.ZONE_AUTH_EXT_PREFIX + id;
            if (!authorityZones.contains(zoneId))
            {
                continue;
            }
            try
            {
                ApplicationContext context = this.applicationContextManager.getApplicationContext(id);
                UserRegistry plugin = (UserRegistry) context.getBean(this.sourceBeanName);
                if (!(plugin instanceof ActivateableBean) || ((ActivateableBean) plugin).isActive())
                {
                    return plugin.getPersonMappedProperties();
                }
            }
            catch (RuntimeException e)
            {
                // The bean doesn't exist or this subsystem won't start. The reason would have been logged. Ignore and continue.
            }
        }

        return Collections.emptySet();
    }

    @Override
    public boolean createMissingPerson(String userName)
    {
        // synchronise or auto-create the missing person if we are allowed
        if (userName != null && !userName.equals(AuthenticationUtil.getSystemUserName()))
        {
            if (this.syncWhenMissingPeopleLogIn)
            {
                try
                {
                    synchronizeInternal(false, false, false);
                }
                catch (Exception e)
                {
                    // We don't want to fail the whole login if we can help it
                    ChainingUserRegistrySynchronizer.logger.warn("User authenticated but failed to sync with user registry", e);
                }
                if (this.personService.personExists(userName))
                {
                    return true;
                }
            }
            if (this.autoCreatePeopleOnLogin && this.personService.createMissingPeople())
            {
                AuthorityType authorityType = AuthorityType.getAuthorityType(userName);
                if (authorityType == AuthorityType.USER)
                {
                    this.personService.getPerson(userName);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Lookup table for sync process used by syncWithPlugin
     * 
     */
    private enum SyncProcess
    {
        GROUP_ANALYSIS("1 Group Analysis"),      
        MISSING_AUTHORITY("2 Missing Authority Scanning"),
        GROUP_CREATION_AND_ASSOCIATION_DELETION("3 Group Creation and Association Deletion"),
        GROUP_ASSOCIATION_CREATION("4 Group Association Creation"),
        PERSON_ASSOCIATION("5 User Association"),
        USER_CREATION("6 User Creation and Association"),
        AUTHORITY_DELETION("7 Authority Deletion");

        SyncProcess(String title) 
        {
            this.title = title;
        }
    
        public String getTitle(String zone)
        {
            return "Synchronization,Category=directory,id1=" +zone+ ",id2=" + title;
        }
        
        private String title;
    }


    /**
     * Synchronizes local groups and users with a {@link UserRegistry} for a particular zone, optionally handling
     * deletions.
     * 
     * @param zone
     *            the zone id. This identifier is used to tag all created groups and users, so that in the future we can
     *            tell those that have been deleted from the registry.
     * @param userRegistry
     *            the user registry for the zone.
     * @param forceUpdate
     *            Should the complete set of users and groups be updated / created locally or just those known to have
     *            changed since the last sync? When <code>true</code> then <i>all</i> users and groups are queried from
     *            the user registry and updated locally. When <code>false</code> then each source is only queried for
     *            those users and groups modified since the most recent modification date of all the objects last
     *            queried from that same source.
     * @param isFullSync
     *            Should a complete set of user and group IDs be queried from the user registries in order to determine
     *            deletions? This parameter is independent of <code>force</code> as a separate query is run to process
     *            updates.
     * @param splitTxns
     *            Can the modifications to Alfresco be split across multiple transactions for maximum performance? If
     *            <code>true</code>, users and groups are created/updated in batches for increased performance. If
     *            <code>false</code>, all users and groups are processed in the current transaction. This is required if
     *            calling synchronously (e.g. in response to an authentication event in the same transaction).
     * @param visitedZoneIds
     *            the set of zone ids already processed. These zones have precedence over the current zone when it comes
     *            to group name 'collisions'. If a user or group is queried that already exists locally but is tagged
     *            with one of the zones in this set, then it will be ignored as this zone has lower priority.
     * @param allZoneIds
     *            the set of all zone ids in the authentication chain. Helps us work out whether the zone information
     *            recorded against a user or group is invalid for the current authentication chain and whether the user
     *            or group needs to be 're-zoned'.
     */
    private void syncWithPlugin(final String zone, UserRegistry userRegistry, boolean forceUpdate,
            boolean isFullSync, boolean splitTxns, final Set<String> visitedZoneIds, final Set<String> allZoneIds)
    {
        // Create a prefixed zone ID for use with the authority service
        final String zoneId = AuthorityService.ZONE_AUTH_EXT_PREFIX + zone;
        
        // Batch Process Names
        final String reservedBatchProcessNames[] = {
             SyncProcess.GROUP_ANALYSIS.getTitle(zone),
             SyncProcess.USER_CREATION.getTitle(zone),
             SyncProcess.MISSING_AUTHORITY.getTitle(zone),
             SyncProcess.GROUP_CREATION_AND_ASSOCIATION_DELETION.getTitle(zone),
             SyncProcess.GROUP_ASSOCIATION_CREATION.getTitle(zone),
             SyncProcess.PERSON_ASSOCIATION.getTitle(zone),
             SyncProcess.AUTHORITY_DELETION.getTitle(zone)          
        };
        
        notifySyncDirectoryStart(zone, reservedBatchProcessNames);

    	// Ensure that the zoneId exists before multiple threads start using it
    	this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
		{
			@Override
			public Void execute() throws Throwable
			{
				authorityService.getOrCreateZone(zoneId);
				return null;
			}
		}, false, splitTxns);
        
        // The set of zones we associate with new objects (default plus registry specific)
        final Set<String> zoneSet = getZones(zoneId);

        long lastModifiedMillis = forceUpdate ? -1 : getMostRecentUpdateTime(
                ChainingUserRegistrySynchronizer.GROUP_LAST_MODIFIED_ATTRIBUTE, zoneId, splitTxns);
        Date lastModified = lastModifiedMillis == -1 ? null : new Date(lastModifiedMillis);

        if (ChainingUserRegistrySynchronizer.logger.isInfoEnabled())
        {
            if (lastModified == null)
            {
                ChainingUserRegistrySynchronizer.logger.info("Retrieving all groups from user registry '" + zone + "'");
            }
            else
            {
                ChainingUserRegistrySynchronizer.logger.info("Retrieving groups changed since "
                        + DateFormat.getDateTimeInstance().format(lastModified) + " from user registry '" + zone + "'");
            }
        }

        // First, analyze the group structure. Create maps of authorities to their parents for associations to create
        // and delete. Also deal with 'overlaps' with other zones in the authentication chain.
        final BatchProcessor<NodeDescription> groupProcessor = new BatchProcessor<NodeDescription>(
                SyncProcess.GROUP_ANALYSIS.getTitle(zone),
                this.transactionService.getRetryingTransactionHelper(), 
                userRegistry.getGroups(lastModified), 
                this.workerThreads, 
                20, 
                this.applicationEventPublisher,
                ChainingUserRegistrySynchronizer.logger, 
                this.loggingInterval);
        class Analyzer extends BaseBatchProcessWorker<NodeDescription>
        {
            private final Map<String, String> groupsToCreate = new TreeMap<String, String>();
            private final Map<String, Set<String>> personParentAssocsToCreate = newPersonMap();
            private final Map<String, Set<String>> personParentAssocsToDelete = newPersonMap();
            private Map<String, Set<String>> groupParentAssocsToCreate = new TreeMap<String, Set<String>>();
            private final Map<String, Set<String>> groupParentAssocsToDelete = new TreeMap<String, Set<String>>();
            private final Map<String, Set<String>> finalGroupChildAssocs = new TreeMap<String, Set<String>>();
            private List<String> personsProcessed = new LinkedList<String>();
            private Set<String> allZonePersons = Collections.emptySet();
            private Set<String> deletionCandidates;

            private long latestTime;

            public Analyzer(final long latestTime)
            {
                this.latestTime = latestTime;
            }

            public long getLatestTime()
            {
                return this.latestTime;
            }

            public Set<String> getDeletionCandidates()
            {
                return this.deletionCandidates;
            }

            public String getIdentifier(NodeDescription entry)
            {
                return entry.getSourceId();
            }

            public void process(NodeDescription group) throws Throwable
            {
                PropertyMap groupProperties = group.getProperties();
                String groupName = (String) groupProperties.get(ContentModel.PROP_AUTHORITY_NAME);
                String groupShortName = ChainingUserRegistrySynchronizer.this.authorityService.getShortName(groupName);
                Set<String> groupZones = ChainingUserRegistrySynchronizer.this.authorityService
                        .getAuthorityZones(groupName);

                if (groupZones == null)
                {
                    // The group did not exist at all
                    updateGroup(group, false);
                }
                else
                {
                    // Check whether the group is in any of the authentication chain zones
                    Set<String> intersection = new TreeSet<String>(groupZones);
                    intersection.retainAll(allZoneIds);
                    // Check whether the group is in any of the higher priority authentication chain zones
                    Set<String> visited = new TreeSet<String>(intersection);
                    visited.retainAll(visitedZoneIds);

                    if (groupZones.contains(zoneId))
                    {
                        // The group already existed in this zone: update the group
                        updateGroup(group, true);
                    }
                    else if (!visited.isEmpty())
                    {
                        // A group that exists in a different zone with higher precedence
                        return;
                    }
                    else if (!allowDeletions || intersection.isEmpty())
                    {
                        // Deletions are disallowed or the group exists, but not in a zone that's in the authentication
                        // chain. May be due to upgrade or zone changes. Let's re-zone them
                        if (ChainingUserRegistrySynchronizer.logger.isWarnEnabled())
                        {
                            ChainingUserRegistrySynchronizer.logger.warn("Updating group '" + groupShortName
                                    + "'. This group will in future be assumed to originate from user registry '"
                                    + zone + "'.");
                        }
                        updateAuthorityZones(groupName, groupZones, zoneSet);

                        // The group now exists in this zone: update the group
                        updateGroup(group, true);
                    }
                    else
                    {
                        // The group existed, but in a zone with lower precedence
                        if (ChainingUserRegistrySynchronizer.logger.isWarnEnabled())
                        {
                            ChainingUserRegistrySynchronizer.logger
                                    .warn("Recreating occluded group '"
                                            + groupShortName
                                            + "'. This group was previously created through synchronization with a lower priority user registry.");
                        }
                        ChainingUserRegistrySynchronizer.this.authorityService.deleteAuthority(groupName);

                        // create the group
                        updateGroup(group, false);
                    }
                }

                synchronized (this)
                {
                    // Maintain the last modified date
                    Date groupLastModified = group.getLastModified();
                    if (groupLastModified != null)
                    {
                        this.latestTime = Math.max(this.latestTime, groupLastModified.getTime());
                    }
                }
            }

            // Recursively walks and caches the authorities relating to and from this group so that we can later detect potential cycles
            private Set<String> getContainedAuthorities(String groupName)
            {
                // Return the cached children if it is processed
                Set<String> children = this.finalGroupChildAssocs.get(groupName);
                if (children != null)
                {
                    return children;
                }

                // First, recurse to the parent most authorities
                for (String parent : ChainingUserRegistrySynchronizer.this.authorityService.getContainingAuthorities(
                        null, groupName, true))
                {
                    getContainedAuthorities(parent);
                }

                // Now descend on unprocessed parents.
                return cacheContainedAuthorities(groupName);
            }

            private Set<String> cacheContainedAuthorities(String groupName)
            {
                // Return the cached children if it is processed
                Set<String> children = this.finalGroupChildAssocs.get(groupName);
                if (children != null)
                {
                    return children;
                }

                // Descend on unprocessed parents.
                children = ChainingUserRegistrySynchronizer.this.authorityService.getContainedAuthorities(null,
                        groupName, true);
                this.finalGroupChildAssocs.put(groupName, children);

                for (String child : children)
                {
                    if (AuthorityType.getAuthorityType(child) != AuthorityType.USER)
                    {
                        cacheContainedAuthorities(child);
                    }
                }
                return children;
            }

            private synchronized void updateGroup(NodeDescription group, boolean existed)
            {
                PropertyMap groupProperties = group.getProperties();
                String groupName = (String) groupProperties.get(ContentModel.PROP_AUTHORITY_NAME);
                String groupDisplayName = (String) groupProperties.get(ContentModel.PROP_AUTHORITY_DISPLAY_NAME);
                if (groupDisplayName == null)
                {
                    groupDisplayName = ChainingUserRegistrySynchronizer.this.authorityService.getShortName(groupName);
                }

                // Divide the child associations into person and group associations, dealing with case sensitivity
                Set<String> newChildPersons = newPersonSet();
                Set<String> newChildGroups = new TreeSet<String>();

                for (String child : group.getChildAssociations())
                {
                    if (AuthorityType.getAuthorityType(child) == AuthorityType.USER)
                    {
                        newChildPersons.add(child);
                    }
                    else
                    {
                        newChildGroups.add(child);
                    }
                }

                // Account for differences if already existing
                if (existed)
                {
                    // Update the display name now
                    ChainingUserRegistrySynchronizer.this.authorityService.setAuthorityDisplayName(groupName,
                            groupDisplayName);

                    // Work out the association differences
                    for (String child : new TreeSet<String>(getContainedAuthorities(groupName)))
                    {
                        if (AuthorityType.getAuthorityType(child) == AuthorityType.USER)
                        {
                            if (!newChildPersons.remove(child))
                            {
                                recordParentAssociationDeletion(child, groupName);
                            }
                        }
                        else
                        {
                            if (!newChildGroups.remove(child))
                            {
                                recordParentAssociationDeletion(child, groupName);
                            }
                        }
                    }
                }
                // Mark as created if new
                else
                {
                    // Make sure each group to be created features in the association deletion map (as these are handled in the same phase)
                    recordParentAssociationDeletion(groupName, null);
                    this.groupsToCreate.put(groupName, groupDisplayName);
                }

                // Create new associations
                for (String child : newChildPersons)
                {
                    // Make sure each person with association changes features as a key in the deletion map
                    recordParentAssociationDeletion(child, null);
                    recordParentAssociationCreation(child, groupName);
                }
                for (String child : newChildGroups)
                {
                    // Make sure each group with association changes features as a key in the deletion map
                    recordParentAssociationDeletion(child, null);
                    recordParentAssociationCreation(child, groupName);
                }
            }

            private void recordParentAssociationDeletion(String child, String parent)
            {
                Map<String, Set<String>> parentAssocs;
                if (AuthorityType.getAuthorityType(child) == AuthorityType.USER)
                {
                    parentAssocs = this.personParentAssocsToDelete;                    
                }
                else
                {
                    // Reflect the change in the map of final group associations (for cycle detection later)
                    parentAssocs = this.groupParentAssocsToDelete;
                    if (parent != null)
                    {
                        Set<String> children = this.finalGroupChildAssocs.get(parent);
                        children.remove(child);
                    }
                }
                Set<String> parents = parentAssocs.get(child);
                if (parents == null)
                {
                    parents = new TreeSet<String>();
                    parentAssocs.put(child, parents);
                }
                if (parent != null)
                {
                    parents.add(parent);
                }
            }

            private void recordParentAssociationCreation(String child, String parent)
            {
                Map<String, Set<String>> parentAssocs = AuthorityType.getAuthorityType(child) == AuthorityType.USER ? this.personParentAssocsToCreate : this.groupParentAssocsToCreate;
                Set<String> parents = parentAssocs.get(child);
                if (parents == null)
                {
                    parents = new TreeSet<String>();
                    parentAssocs.put(child, parents);
                }
                if (parent != null)
                {
                    parents.add(parent);
                }
            }
            
            private void validateGroupParentAssocsToCreate()
            {
                Iterator<Map.Entry<String, Set<String>>> i = this.groupParentAssocsToCreate.entrySet().iterator();
                while (i.hasNext())
                {
                    Map.Entry<String, Set<String>> entry = i.next();
                    String group = entry.getKey();
                    Set<String> parents = entry.getValue();
                    Deque<String> visited = new LinkedList<String>();
                    Iterator<String> j = parents.iterator();
                    while (j.hasNext())
                    {
                        String parent = j.next();
                        visited.add(parent);
                        if (validateAuthorityChildren(visited, group))
                        {
                            // The association validated - commit it
                            Set<String> children = finalGroupChildAssocs.get(parent);
                            if (children == null)
                            {
                                children = new TreeSet<String>();
                                finalGroupChildAssocs.put(parent, children);
                            }
                            children.add(group);
                        }
                        else
                        {
                            // The association did not validate - prune it out
                            if (logger.isWarnEnabled())
                            {
                                ChainingUserRegistrySynchronizer.logger.warn("Not adding group '"
                                        + ChainingUserRegistrySynchronizer.this.authorityService.getShortName(group)
                                        + "' to group '"
                                        + ChainingUserRegistrySynchronizer.this.authorityService.getShortName(parent)
                                        + "' as this creates a cyclic relationship");
                            }
                            j.remove();
                        }
                        visited.removeLast();
                    }
                    if (parents.isEmpty())
                    {
                        i.remove();
                    }
                }
                
                // Sort the group associations in parent-first order (root groups first) to minimize reindexing overhead
                Map<String, Set<String>> sortedGroupAssociations = new LinkedHashMap<String, Set<String>>(
                        this.groupParentAssocsToCreate.size() * 2);
                Deque<String> visited = new LinkedList<String>();
                for (String authority : this.groupParentAssocsToCreate.keySet())
                {
                    visitGroupParentAssocs(visited, authority, this.groupParentAssocsToCreate, sortedGroupAssociations);
                }
                
                this.groupParentAssocsToCreate = sortedGroupAssociations;
            }

            private boolean validateAuthorityChildren(Deque<String> visited, String authority)
            {
                if (AuthorityType.getAuthorityType(authority) == AuthorityType.USER)
                {
                    return true;
                }
                if (visited.contains(authority))
                {
                    return false;
                }
                visited.add(authority);
                try
                {
                    Set<String> children = this.finalGroupChildAssocs.get(authority);
                    if (children != null)
                    {
                        for (String child : children)
                        {
                            if (!validateAuthorityChildren(visited, child))
                            {
                                return false;
                            }
                        }
                    }
                    return true;
                }
                finally
                {
                    visited.removeLast();
                }
            }

            /**
             * Visits the given authority by recursively visiting its parents in associationsOld and then adding the
             * authority to associationsNew. Used to sort associationsOld into 'parent-first' order to minimize
             * reindexing overhead.
             * 
             * @param visited
             *            The ancestors that form the path to the authority to visit. Allows detection of cyclic child
             *            associations.
             * @param authority
             *            the authority to visit
             * @param associationsOld
             *            the association map to sort
             * @param associationsNew
             *            the association map to add to in parent-first order
             */
            private boolean visitGroupParentAssocs(Deque<String> visited, String authority,
                    Map<String, Set<String>> associationsOld, Map<String, Set<String>> associationsNew)
            {
                if (visited.contains(authority))
                {
                    // Prevent cyclic paths (Shouldn't happen as we've already validated)
                    return false;
                }
                visited.add(authority);
                try
                {
                    if (!associationsNew.containsKey(authority))
                    {
                        Set<String> oldParents = associationsOld.get(authority);
                        if (oldParents != null)
                        {
                            Set<String> newParents = new TreeSet<String>();
    
                            for (String parent: oldParents)
                            {
                                if (visitGroupParentAssocs(visited, parent, associationsOld, associationsNew))
                                {
                                    newParents.add(parent);
                                }
                            }
                            associationsNew.put(authority, newParents);
                        }
                    }
                    return true;
                }
                finally
                {
                    visited.removeLast();
                }
            }

            private Set<String> newPersonSet()
            {
                return ChainingUserRegistrySynchronizer.this.personService.getUserNamesAreCaseSensitive() ? new TreeSet<String>()
                        : new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
            }

            private Map<String, Set<String>> newPersonMap()
            {
                return ChainingUserRegistrySynchronizer.this.personService.getUserNamesAreCaseSensitive() ? new TreeMap<String, Set<String>>()
                        : new TreeMap<String, Set<String>>(String.CASE_INSENSITIVE_ORDER);
            }

            private void logRetainParentAssociations(Map<String, Set<String>> parentAssocs, Set<String> toRetain)
            {
                Iterator<Map.Entry<String, Set<String>>> i = parentAssocs.entrySet().iterator();
                StringBuilder groupList = null;
                while (i.hasNext())
                {
                    Map.Entry<String, Set<String>> entry = i.next();
                    String child = entry.getKey();
                    if (!toRetain.contains(child))
                    {
                        if (ChainingUserRegistrySynchronizer.logger.isDebugEnabled())
                        {
                            if (groupList == null)
                            {
                                groupList = new StringBuilder(1024);
                            }
                            else
                            {
                                groupList.setLength(0);
                            }
                            for (String parent : entry.getValue())
                            {
                                if (groupList.length() > 0)
                                {
                                    groupList.append(", ");
                                }
                                groupList.append('\'').append(
                                        ChainingUserRegistrySynchronizer.this.authorityService.getShortName(parent))
                                        .append('\'');

                            }
                            ChainingUserRegistrySynchronizer.logger.debug("Ignoring non-existent member '"
                                    + ChainingUserRegistrySynchronizer.this.authorityService.getShortName(child)
                                    + "' in groups {" + groupList.toString() + "}");
                        }
                        i.remove();
                    }
                }
            }

            private void processGroups(UserRegistry userRegistry, boolean isFullSync, boolean splitTxns)
            {
               // MNT-12454 fix. If syncDelete is false, there is no need to pull all users and all groups from LDAP during the full synchronization.
               if ((syncDelete || !groupsToCreate.isEmpty()) && (isFullSync || !this.groupParentAssocsToDelete.isEmpty()))
               {
                    final Set<String> allZonePersons = newPersonSet();
                    final Set<String> allZoneGroups = new TreeSet<String>();

                    // Add in current set of known authorities
                    ChainingUserRegistrySynchronizer.this.transactionService.getRetryingTransactionHelper()
                            .doInTransaction(new RetryingTransactionCallback<Void>()
                            {
                                public Void execute() throws Throwable
                                {
                                    allZonePersons.addAll(ChainingUserRegistrySynchronizer.this.authorityService
                                            .getAllAuthoritiesInZone(zoneId, AuthorityType.USER));
                                    allZoneGroups.addAll(ChainingUserRegistrySynchronizer.this.authorityService
                                            .getAllAuthoritiesInZone(zoneId, AuthorityType.GROUP));
                                    return null;
                                }
                            }, true, splitTxns);

                    allZoneGroups.addAll(this.groupsToCreate.keySet());

                    // Prune our set of authorities according to deletions
                    if (isFullSync)
                    {
                        final Set<String> personDeletionCandidates = newPersonSet();
                        personDeletionCandidates.addAll(allZonePersons);

                        final Set<String> groupDeletionCandidates = new TreeSet<String>();
                        groupDeletionCandidates.addAll(allZoneGroups);

                        this.deletionCandidates = new TreeSet<String>();

                        for (String person : userRegistry.getPersonNames())
                        {
                            personDeletionCandidates.remove(person);
                        }

                        for (String group : userRegistry.getGroupNames())
                        {
                            groupDeletionCandidates.remove(group);
                        }

                        this.deletionCandidates = new TreeSet<String>();
                        this.deletionCandidates.addAll(personDeletionCandidates);
                        this.deletionCandidates.addAll(groupDeletionCandidates);

                        if (allowDeletions)
                        {
                            allZonePersons.removeAll(personDeletionCandidates);
                            allZoneGroups.removeAll(groupDeletionCandidates);
                        }
                        else
                        {
                            // Complete association deletion information by scanning deleted groups
                            BatchProcessor<String> groupScanner = new BatchProcessor<String>(zone
                                    + " Missing Authority Scanning",
                                    ChainingUserRegistrySynchronizer.this.transactionService.getRetryingTransactionHelper(),
                                    this.deletionCandidates,
                                    ChainingUserRegistrySynchronizer.this.workerThreads, 20,
                                    ChainingUserRegistrySynchronizer.this.applicationEventPublisher,
                                    ChainingUserRegistrySynchronizer.logger,
                                    ChainingUserRegistrySynchronizer.this.loggingInterval);
                            groupScanner.process(new BaseBatchProcessWorker<String>()
                            {

                                @Override
                                public String getIdentifier(String entry)
                                {
                                    return entry;
                                }

                                @Override
                                public void process(String authority) throws Throwable
                                {
                                    //MNT-12454 fix. Modifies an authority's zone. Move authority from AUTH.EXT.LDAP1 to AUTH.ALF.
                                    updateAuthorityZones(authority, Collections.singleton(zoneId),
                                          Collections.singleton(AuthorityService.ZONE_AUTH_ALFRESCO));
                                }
                            }, splitTxns);
                        }

                    }

                    // Prune the group associations now that we have complete information
                    this.groupParentAssocsToCreate.keySet().retainAll(allZoneGroups);
                    logRetainParentAssociations(this.groupParentAssocsToCreate, allZoneGroups);
                    this.finalGroupChildAssocs.keySet().retainAll(allZoneGroups);

                    // Pruning person associations will have to wait until we have passed over all persons and built up
                    // this set
                    this.allZonePersons = allZonePersons;

                    if (!this.groupParentAssocsToDelete.isEmpty())
                    {
                        // Create/update the groups and delete parent associations to be deleted
                        // Batch 4 Group Creation and Association Deletion
                        BatchProcessor<Map.Entry<String, Set<String>>> groupCreator = new BatchProcessor<Map.Entry<String, Set<String>>>(
                                SyncProcess.GROUP_CREATION_AND_ASSOCIATION_DELETION.getTitle(zone),
                                ChainingUserRegistrySynchronizer.this.transactionService.getRetryingTransactionHelper(),
                                this.groupParentAssocsToDelete.entrySet(),
                                ChainingUserRegistrySynchronizer.this.workerThreads, 20,
                                ChainingUserRegistrySynchronizer.this.applicationEventPublisher,
                                ChainingUserRegistrySynchronizer.logger,
                                ChainingUserRegistrySynchronizer.this.loggingInterval);
                        groupCreator.process(new BaseBatchProcessWorker<Map.Entry<String, Set<String>>>()
                        {
                            public String getIdentifier(Map.Entry<String, Set<String>> entry)
                            {
                                return entry.getKey() + " " + entry.getValue();
                            }

                            public void process(Map.Entry<String, Set<String>> entry) throws Throwable
                            {
                                String child = entry.getKey();

                                String groupDisplayName = Analyzer.this.groupsToCreate.get(child);
                                if (groupDisplayName != null)
                                {
                                    String groupShortName = ChainingUserRegistrySynchronizer.this.authorityService
                                            .getShortName(child);
                                    if (ChainingUserRegistrySynchronizer.logger.isDebugEnabled())
                                    {
                                        ChainingUserRegistrySynchronizer.logger.debug("Creating group '"
                                                + groupShortName + "'");
                                    }
                                    // create the group
                                    ChainingUserRegistrySynchronizer.this.authorityService.createAuthority(
                                            AuthorityType.getAuthorityType(child), groupShortName, groupDisplayName,
                                            zoneSet);
                                }
                                else
                                {
                                    // Maintain association deletions now. The creations will have to be done later once
                                    // we have performed all the deletions in order to avoid creating cycles
                                    maintainAssociationDeletions(child);
                                }
                            }
                        }, splitTxns);
                    }
                }
            }

            private void finalizeAssociations(UserRegistry userRegistry, boolean splitTxns)
            {
                // First validate the group associations to be created for potential cycles. Remove any offending association
                validateGroupParentAssocsToCreate();
                
                // Now go ahead and create the group associations
                if (!this.groupParentAssocsToCreate.isEmpty())
                {
                    // Batch 5 Group Association Creation
                    BatchProcessor<Map.Entry<String, Set<String>>> groupCreator = new BatchProcessor<Map.Entry<String, Set<String>>>(
                            SyncProcess.GROUP_ASSOCIATION_CREATION.getTitle(zone),
                            ChainingUserRegistrySynchronizer.this.transactionService.getRetryingTransactionHelper(),
                            this.groupParentAssocsToCreate.entrySet(),
                            ChainingUserRegistrySynchronizer.this.workerThreads, 20,
                            ChainingUserRegistrySynchronizer.this.applicationEventPublisher,
                            ChainingUserRegistrySynchronizer.logger,
                            ChainingUserRegistrySynchronizer.this.loggingInterval);
                    groupCreator.process(new BaseBatchProcessWorker<Map.Entry<String, Set<String>>>()
                    {
                        public String getIdentifier(Map.Entry<String, Set<String>> entry)
                        {
                            return entry.getKey() + " " + entry.getValue();
                        }

                        public void process(Map.Entry<String, Set<String>> entry) throws Throwable
                        {
                            maintainAssociationCreations(entry.getKey());
                        }
                    }, splitTxns);
                }

                // Remove all the associations we have already dealt with
                this.personParentAssocsToDelete.keySet().removeAll(this.personsProcessed);

                // Filter out associations to authorities that simply can't exist (and log if debugging is enabled)
                logRetainParentAssociations(this.personParentAssocsToCreate, this.allZonePersons);

                // Update associations to persons not updated themselves
                if (!this.personParentAssocsToDelete.isEmpty())
                {
                    // Batch 6 Person Association
                    BatchProcessor<Map.Entry<String, Set<String>>> groupCreator = new BatchProcessor<Map.Entry<String, Set<String>>>(
                            SyncProcess.PERSON_ASSOCIATION.getTitle(zone),
                            ChainingUserRegistrySynchronizer.this.transactionService.getRetryingTransactionHelper(),
                            this.personParentAssocsToDelete.entrySet(),
                            ChainingUserRegistrySynchronizer.this.workerThreads, 20,
                            ChainingUserRegistrySynchronizer.this.applicationEventPublisher,
                            ChainingUserRegistrySynchronizer.logger,
                            ChainingUserRegistrySynchronizer.this.loggingInterval);
                    groupCreator.process(new BaseBatchProcessWorker<Map.Entry<String, Set<String>>>()
                    {
                        public String getIdentifier(Map.Entry<String, Set<String>> entry)
                        {
                            return entry.getKey() + " " + entry.getValue();
                        }

                        public void process(Map.Entry<String, Set<String>> entry) throws Throwable
                        {
                            maintainAssociationDeletions(entry.getKey());
                            maintainAssociationCreations(entry.getKey());
                        }
                    }, splitTxns);
                }
            }

            private void maintainAssociationDeletions(String authorityName)
            {
                boolean isPerson = AuthorityType.getAuthorityType(authorityName) == AuthorityType.USER;
                Set<String> parentsToDelete = isPerson ? this.personParentAssocsToDelete.get(authorityName)
                        : this.groupParentAssocsToDelete.get(authorityName);
                if (parentsToDelete != null && !parentsToDelete.isEmpty())
                {
                    for (String parent : parentsToDelete)
                    {
                        if (ChainingUserRegistrySynchronizer.logger.isDebugEnabled())
                        {
                            ChainingUserRegistrySynchronizer.logger
                                    .debug("Removing '"
                                            + ChainingUserRegistrySynchronizer.this.authorityService
                                                    .getShortName(authorityName)
                                            + "' from group '"
                                            + ChainingUserRegistrySynchronizer.this.authorityService
                                                    .getShortName(parent) + "'");
                        }
                        ChainingUserRegistrySynchronizer.this.authorityService.removeAuthority(parent, authorityName);
                    }
                }
                
                
            }
        
            private void maintainAssociationCreations(String authorityName)
            {
                boolean isPerson = AuthorityType.getAuthorityType(authorityName) == AuthorityType.USER;
                Set<String> parents = isPerson ? this.personParentAssocsToCreate.get(authorityName)
                        : this.groupParentAssocsToCreate.get(authorityName);
                if (parents != null && !parents.isEmpty())
                {
                    if (ChainingUserRegistrySynchronizer.logger.isDebugEnabled())
                    {
                        for (String groupName : parents)
                        {
                            ChainingUserRegistrySynchronizer.logger.debug("Adding '"
                                    + ChainingUserRegistrySynchronizer.this.authorityService
                                            .getShortName(authorityName) + "' to group '"
                                    + ChainingUserRegistrySynchronizer.this.authorityService.getShortName(groupName)
                                    + "'");
                        }
                    }
                    try
                    {
                        ChainingUserRegistrySynchronizer.this.authorityService.addAuthority(parents, authorityName);
                    }
                    catch (UnknownAuthorityException e)
                    {
                        // Let's force a transaction retry if a parent doesn't exist. It may be because we are
                        // waiting for another worker thread to create it
                        throw new ConcurrencyFailureException("Forcing batch retry for unknown authority", e);
                    }
                    catch (InvalidNodeRefException e)
                    {
                        // Another thread may have written the node, but it is not visible to this transaction
                        // See: ALF-5471: 'authorityMigration' patch can report 'Node does not exist'
                        throw new ConcurrencyFailureException("Forcing batch retry for invalid node", e);
                    }
                }
                // Remember that this person's associations have been maintained
                if (isPerson)
                {
                    synchronized (this)
                    {
                        this.personsProcessed.add(authorityName);
                    }
                }
            }
        } // end of Analyzer class

        // Run the first process the Group Analyzer
        final Analyzer groupAnalyzer = new Analyzer(lastModifiedMillis);
        int groupProcessedCount = groupProcessor.process(groupAnalyzer, splitTxns);

        groupAnalyzer.processGroups(userRegistry, isFullSync, splitTxns);

        // Process persons and their parent associations

        lastModifiedMillis = forceUpdate ? -1 : getMostRecentUpdateTime(
                ChainingUserRegistrySynchronizer.PERSON_LAST_MODIFIED_ATTRIBUTE, zoneId, splitTxns);
        lastModified = lastModifiedMillis == -1 ? null : new Date(lastModifiedMillis);
        if (ChainingUserRegistrySynchronizer.logger.isInfoEnabled())
        {
            if (lastModified == null)
            {
                ChainingUserRegistrySynchronizer.logger.info("Retrieving all users from user registry '" + zone + "'");
            }
            else
            {
                ChainingUserRegistrySynchronizer.logger.info("Retrieving users changed since "
                        + DateFormat.getDateTimeInstance().format(lastModified) + " from user registry '" + zone + "'");
            }
        }
        
        // User Creation and Association
        final BatchProcessor<NodeDescription> personProcessor = new BatchProcessor<NodeDescription>(
                SyncProcess.USER_CREATION.getTitle(zone),
                this.transactionService.getRetryingTransactionHelper(),
                userRegistry.getPersons(lastModified),
                this.workerThreads,
                10,
                this.applicationEventPublisher,
                ChainingUserRegistrySynchronizer.logger,
                this.loggingInterval);
        class PersonWorker extends BaseBatchProcessWorker<NodeDescription>
        {
            private long latestTime;

            public PersonWorker(final long latestTime)
            {
                this.latestTime = latestTime;
            }

            public long getLatestTime()
            {
                return this.latestTime;
            }

            public String getIdentifier(NodeDescription entry)
            {
                return entry.getSourceId();
            }

            public void process(NodeDescription person) throws Throwable
            {
                // Make a mutable copy of the person properties, since they get written back to by person service
                HashMap<QName, Serializable> personProperties = new HashMap<QName, Serializable>(person.getProperties());
                String personName = personProperties.get(ContentModel.PROP_USERNAME).toString().trim();
                personProperties.put(ContentModel.PROP_USERNAME, personName);
                // for invalid names will throw ConstraintException that will be catched by BatchProcessor$TxnCallback
                nameChecker.evaluate(personName);
                Set<String> zones = ChainingUserRegistrySynchronizer.this.authorityService
                        .getAuthorityZones(personName);
                if (zones == null)
                {
                    // The person did not exist at all
                    if (ChainingUserRegistrySynchronizer.logger.isDebugEnabled())
                    {
                        ChainingUserRegistrySynchronizer.logger.debug("Creating user '" + personName + "'");
                    }
                    ChainingUserRegistrySynchronizer.this.personService.createPerson(personProperties, zoneSet);
                }
                else if (zones.contains(zoneId))
                {
                    // The person already existed in this zone: update the person
                    if (ChainingUserRegistrySynchronizer.logger.isDebugEnabled())
                    {
                        ChainingUserRegistrySynchronizer.logger.debug("Updating user '" + personName + "'");
                    }
                    ChainingUserRegistrySynchronizer.this.personService.setPersonProperties(personName,
                            personProperties, false);
                }
                else
                {
                    // Check whether the user is in any of the authentication chain zones
                    Set<String> intersection = new TreeSet<String>(zones);
                    intersection.retainAll(allZoneIds);
                    // Check whether the user is in any of the higher priority authentication chain zones
                    Set<String> visited = new TreeSet<String>(intersection);
                    visited.retainAll(visitedZoneIds);
                    if (visited.size() > 0)
                    {
                        // A person that exists in a different zone with higher precedence - ignore
                        return;
                    }

                    else if (!allowDeletions || intersection.isEmpty())
                    {
                        // The person exists, but in a different zone. Either deletions are disallowed or the zone is
                        // not in the authentication chain. May be due to upgrade or zone changes. Let's re-zone them
                        if (ChainingUserRegistrySynchronizer.logger.isWarnEnabled())
                        {
                            ChainingUserRegistrySynchronizer.logger.warn("Updating user '" + personName
                                    + "'. This user will in future be assumed to originate from user registry '" + zone
                                    + "'.");
                        }
                        updateAuthorityZones(personName, zones, zoneSet);
                        ChainingUserRegistrySynchronizer.this.personService.setPersonProperties(personName,
                                personProperties, false);
                    }
                    else
                    {
                        // The person existed, but in a zone with lower precedence
                        if (ChainingUserRegistrySynchronizer.logger.isWarnEnabled())
                        {
                            ChainingUserRegistrySynchronizer.logger
                                    .warn("Recreating occluded user '"
                                            + personName
                                            + "'. This user was previously created through synchronization with a lower priority user registry.");
                        }
                        ChainingUserRegistrySynchronizer.this.personService.deletePerson(personName);
                        ChainingUserRegistrySynchronizer.this.personService.createPerson(personProperties, zoneSet);
                    }
                }

                // Maintain association deletions and creations in one shot (safe to do this with persons as we can't
                // create cycles)
                groupAnalyzer.maintainAssociationDeletions(personName);
                groupAnalyzer.maintainAssociationCreations(personName);

                synchronized (this)
                {
                    // Maintain the last modified date
                    Date personLastModified = person.getLastModified();
                    if (personLastModified != null)
                    {
                        this.latestTime = Math.max(this.latestTime, personLastModified.getTime());
                    }
                }
            }
        }

        PersonWorker persons = new PersonWorker(lastModifiedMillis);
        int personProcessedCount = personProcessor.process(persons, splitTxns);

        // Process those associations to persons who themselves have not been updated
        groupAnalyzer.finalizeAssociations(userRegistry, splitTxns);

        // Only now that the whole tree has been processed is it safe to persist the last modified dates
        long latestTime = groupAnalyzer.getLatestTime();
        if (latestTime != -1)
        {
            setMostRecentUpdateTime(ChainingUserRegistrySynchronizer.GROUP_LAST_MODIFIED_ATTRIBUTE, zoneId, latestTime,
                    splitTxns);
        }
        latestTime = persons.getLatestTime();
        if (latestTime != -1)
        {
            setMostRecentUpdateTime(ChainingUserRegistrySynchronizer.PERSON_LAST_MODIFIED_ATTRIBUTE, zoneId,
                    latestTime, splitTxns);
        }

        // Delete authorities if we have complete information for the zone
        Set<String> deletionCandidates = groupAnalyzer.getDeletionCandidates();
        if (isFullSync && allowDeletions && !deletionCandidates.isEmpty())
        {
            // Batch 7 Authority Deletion
            BatchProcessor<String> authorityDeletionProcessor = new BatchProcessor<String>(
                    SyncProcess.AUTHORITY_DELETION.getTitle(zone), 
                    this.transactionService.getRetryingTransactionHelper(),
                    deletionCandidates,
                    this.workerThreads,
                    10,
                    this.applicationEventPublisher,
                    ChainingUserRegistrySynchronizer.logger,
                    this.loggingInterval);
            class AuthorityDeleter extends BaseBatchProcessWorker<String>
            {
                private int personProcessedCount;
                private int groupProcessedCount;

                public int getPersonProcessedCount()
                {
                    return this.personProcessedCount;
                }

                public int getGroupProcessedCount()
                {
                    return this.groupProcessedCount;
                }

                public String getIdentifier(String entry)
                {
                    return entry;
                }

                public void process(String authority) throws Throwable
                {
                    if (AuthorityType.getAuthorityType(authority) == AuthorityType.USER)
                    {
                        if (ChainingUserRegistrySynchronizer.logger.isDebugEnabled())
                        {
                            ChainingUserRegistrySynchronizer.logger.debug("Deleting user '" + authority + "'");
                        }
                        ChainingUserRegistrySynchronizer.this.personService.deletePerson(authority);
                        synchronized (this)
                        {
                            this.personProcessedCount++;
                        }
                    }
                    else
                    {
                        if (ChainingUserRegistrySynchronizer.logger.isDebugEnabled())
                        {
                            ChainingUserRegistrySynchronizer.logger.debug("Deleting group '"
                                    + ChainingUserRegistrySynchronizer.this.authorityService.getShortName(authority)
                                    + "'");
                        }
                        ChainingUserRegistrySynchronizer.this.authorityService.deleteAuthority(authority);
                        synchronized (this)
                        {
                            this.groupProcessedCount++;
                        }
                    }
                }
            }
            AuthorityDeleter authorityDeleter = new AuthorityDeleter();
            authorityDeletionProcessor.process(authorityDeleter, splitTxns);
            groupProcessedCount += authorityDeleter.getGroupProcessedCount();
            personProcessedCount += authorityDeleter.getPersonProcessedCount();
        }

        // Remember we have visited this zone
        visitedZoneIds.add(zoneId);
        
        
        Object statusParams[] = {personProcessedCount, groupProcessedCount};
        final String statusMessage = I18NUtil.getMessage("synchronization.summary.status", statusParams);
        
        if (ChainingUserRegistrySynchronizer.logger.isInfoEnabled())
        {
            ChainingUserRegistrySynchronizer.logger.info("Finished synchronizing users and groups with user registry '"
                    + zone + "'");
            ChainingUserRegistrySynchronizer.logger.info(statusMessage);
        }
        
        notifySyncDirectoryEnd(zone, statusMessage);
               
    } // syncWithPlugin

    /**
     * Gets the persisted most recent update time for a label and zone.
     * 
     * @param label
     *            the label
     * @param zoneId
     *            the zone id
     * @param splitTxns
     *            split transactions, if true run this in a separate transaction           
     * @return the most recent update time in milliseconds
     */
    private long getMostRecentUpdateTime(final String label, final String zoneId, boolean splitTxns)
    {
        return this.transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionCallback<Long>()
                {
                    public Long execute() throws Throwable
                    {
                        Long updateTime = (Long) ChainingUserRegistrySynchronizer.this.attributeService.getAttribute(
                                ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, label, zoneId);
                        return updateTime == null ? -1 : updateTime;
                    }
                }, true, splitTxns);
    }

    /**
     * Persists the most recent update time for a label and zone.
     * 
     * @param label
     *            the label
     * @param zoneId
     *            the zone id
     * @param lastModifiedMillis
     *            the update time in milliseconds
     * @param splitTxns
     *            Can the modifications to Alfresco be split across multiple transactions for maximum performance? If
     *            <code>true</code>, the attribute is persisted in a new transaction for increased performance and
     *            reliability.
     */
    private void setMostRecentUpdateTime(final String label, final String zoneId, final long lastModifiedMillis,
            boolean splitTxns)
    {
        this.transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Throwable
                    {
                        ChainingUserRegistrySynchronizer.this.attributeService.setAttribute(Long
                                .valueOf(lastModifiedMillis), ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH,
                                label, zoneId);
                        return null;
                    }
                }, false, splitTxns);
    }

    /**
     * Gets the default set of zones to set on a person or group belonging to the user registry with the given zone ID.
     * We add the default zone as well as the zone corresponding to the user registry so that the users and groups are
     * visible in the UI.
     * 
     * @param zoneId
     *            the zone id
     * @return the zone set
     */
    private Set<String> getZones(final String zoneId)
    {
        Set<String> zones = new HashSet<String>(5);
        zones.add(AuthorityService.ZONE_APP_DEFAULT);
        zones.add(zoneId);
        return zones;
    }

    /**
     * Modifies an authority's zone set from oldZones to newZones in the most efficient manner (avoiding unnecessary
     * reindexing cost).
     */
    private void updateAuthorityZones(String authorityName, Set<String> oldZones, final Set<String> newZones)
    {
        Set<String> zonesToRemove = new HashSet<String>(oldZones);
        zonesToRemove.removeAll(newZones);
        // Let's keep the authority in the alfresco auth zone if it was already there. Otherwise we may have to
        // regenerate all paths to this authority from site groups, which could be very expensive!
        zonesToRemove.remove(AuthorityService.ZONE_AUTH_ALFRESCO);
        if (!zonesToRemove.isEmpty())
        {
            this.authorityService.removeAuthorityFromZones(authorityName, zonesToRemove);
        }
        Set<String> zonesToAdd = new HashSet<String>(newZones);
        zonesToAdd.removeAll(oldZones);
        if (!zonesToAdd.isEmpty())
        {
            this.authorityService.addAuthorityToZones(authorityName, zonesToAdd);
        }
    }
    
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // Do an initial differential sync on startup, using transaction splitting. This ensures that on the very
        // first startup, we don't have to wait for a very long login operation to trigger the first sync!
        if (this.syncOnStartup)
        {
            AuthenticationUtil.runAs(new RunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    try
                    {
                        synchronizeInternal(false, false, true);
                    }
                    catch (Exception e)
                    {
                        ChainingUserRegistrySynchronizer.logger.warn("Failed initial synchronize with user registries",
                                e);
                    }
                    return null;
                }
            }, AuthenticationUtil.getSystemUserName());
        }
    }

    /*
     * (non-Javadoc)
     * @seeorg.springframework.extensions.surf.util.AbstractLifecycleBean#onShutdown(org.springframework.context.
     * ApplicationEvent)
     */
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
    }
    
    protected abstract class BaseBatchProcessWorker <T> implements BatchProcessWorker<T>
    {
        public final void beforeProcess() throws Throwable
        {
            // Authentication
            AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
        }

        public final void afterProcess() throws Throwable
        {
            // Clear authentication
            AuthenticationUtil.clearCurrentSecurityContext();
        }
    }

    private void notifySyncStart(final Set<String>toSync)
    {
        final String serverId = sysAdminParams.getAlfrescoHost() + ":" + sysAdminParams.getAlfrescoPort();
        this.applicationEventPublisher.publishEvent(new SynchronizeStartEvent(this));
        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                        ChainingUserRegistrySynchronizer.this.attributeService.setAttribute(
                            new Date().getTime(),
                            ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, 
                            ChainingUserRegistrySynchronizer.START_TIME_ATTRIBUTE);
                        ChainingUserRegistrySynchronizer.this.attributeService.setAttribute(
                                -1L,
                                ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, 
                                ChainingUserRegistrySynchronizer.END_TIME_ATTRIBUTE);
                        ChainingUserRegistrySynchronizer.this.attributeService.setAttribute(
                                serverId,
                                ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, 
                                ChainingUserRegistrySynchronizer.SERVER_ATTRIBUTE);
                        ChainingUserRegistrySynchronizer.this.attributeService.setAttribute(
                                SyncStatus.IN_PROGRESS.toString(),
                                ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, 
                                ChainingUserRegistrySynchronizer.STATUS_ATTRIBUTE);
                        ChainingUserRegistrySynchronizer.this.attributeService.setAttribute(
                                null,
                                ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, 
                                ChainingUserRegistrySynchronizer.LAST_ERROR_ATTRIBUTE);
                        ChainingUserRegistrySynchronizer.this.attributeService.setAttribute(
                                "",
                                ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, 
                                ChainingUserRegistrySynchronizer.SUMMARY_ATTRIBUTE);
                        
                        for(String zoneId : toSync)
                        {
                            ChainingUserRegistrySynchronizer.this.attributeService.setAttribute(
                                SyncStatus.WAITING.toString(),
                                ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, 
                                ChainingUserRegistrySynchronizer.STATUS_ATTRIBUTE, 
                                zoneId);
                        }
                        
                        return null;
            }
        }, false, true); 
    }
    private void notifySyncEnd()
    {
        this.applicationEventPublisher.publishEvent(new SynchronizeEndEvent(this));
        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                ChainingUserRegistrySynchronizer.this.attributeService.setAttribute(
                        SyncStatus.COMPLETE.toString(),
                        ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, 
                        ChainingUserRegistrySynchronizer.STATUS_ATTRIBUTE);
                ChainingUserRegistrySynchronizer.this.attributeService.setAttribute(
                        new Date().getTime(),
                        ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, 
                        ChainingUserRegistrySynchronizer.END_TIME_ATTRIBUTE);
                return null;
            }
        }, false, true); 


    }
    
    private void notifySyncEnd(final Exception e)
    {
        this.applicationEventPublisher.publishEvent(new SynchronizeEndEvent(this, e));
        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
                    @Override
                    public Void execute() throws Throwable
                    {
                        ChainingUserRegistrySynchronizer.this.attributeService.setAttribute(
                        e.getMessage(),
                        ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, 
                        ChainingUserRegistrySynchronizer.LAST_ERROR_ATTRIBUTE);
                        
                        ChainingUserRegistrySynchronizer.this.attributeService.setAttribute(
                                SyncStatus.COMPLETE_ERROR.toString(),
                                ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, 
                                ChainingUserRegistrySynchronizer.STATUS_ATTRIBUTE);

                        ChainingUserRegistrySynchronizer.this.attributeService.setAttribute(
                                new Date().getTime(),
                                ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, 
                                ChainingUserRegistrySynchronizer.END_TIME_ATTRIBUTE);

                        return null;
                    }
        }, false, true);      
    }
    
    private void notifyZoneDeleted(final String zoneId)
    {
//        this.applicationEventPublisher.publishEvent(new SynchronizeDirectoryDeleteZoneEvent(this, zoneId, batchProcessNames));
        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                ChainingUserRegistrySynchronizer.this.attributeService.setAttribute(
                        "",
                        ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, 
                        ChainingUserRegistrySynchronizer.STATUS_ATTRIBUTE, 
                        zoneId);
                ChainingUserRegistrySynchronizer.this.attributeService.setAttribute(
                        "",
                        ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, 
                        ChainingUserRegistrySynchronizer.SUMMARY_ATTRIBUTE, 
                        zoneId);
                ChainingUserRegistrySynchronizer.this.attributeService.setAttribute(
                        null,
                        ChainingUserRegistrySynchronizer.LAST_ERROR_ATTRIBUTE, 
                        ChainingUserRegistrySynchronizer.SUMMARY_ATTRIBUTE, 
                        zoneId);
          
                return null;
            }
        }, false, true);    
    }
       
    private void notifySyncDirectoryStart(final String zoneId, final String[] batchProcessNames)
    {
        this.applicationEventPublisher.publishEvent(new SynchronizeDirectoryStartEvent(this, zoneId, batchProcessNames));
        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                ChainingUserRegistrySynchronizer.this.attributeService.setAttribute(
                        SyncStatus.IN_PROGRESS.toString(),
                        ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, 
                        ChainingUserRegistrySynchronizer.STATUS_ATTRIBUTE, 
                        zoneId);
                ChainingUserRegistrySynchronizer.this.attributeService.setAttribute(
                        "",
                        ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, 
                        ChainingUserRegistrySynchronizer.SUMMARY_ATTRIBUTE, 
                        zoneId);
                ChainingUserRegistrySynchronizer.this.attributeService.setAttribute(
                        null,
                        ChainingUserRegistrySynchronizer.LAST_ERROR_ATTRIBUTE, 
                        ChainingUserRegistrySynchronizer.SUMMARY_ATTRIBUTE, 
                        zoneId);
                return null;
            }
        }, false, true);        
        

    }
    
    private void notifySyncDirectoryEnd(final String zoneId, final String statusMessage)
    {
        this.applicationEventPublisher.publishEvent(new SynchronizeDirectoryEndEvent(this, zoneId));

        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                ChainingUserRegistrySynchronizer.this.attributeService.setAttribute(
                    SyncStatus.COMPLETE.toString(),
                    ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, 
                    ChainingUserRegistrySynchronizer.STATUS_ATTRIBUTE, 
                    zoneId);
                ChainingUserRegistrySynchronizer.this.attributeService.setAttribute(
                    "",
                    ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, 
                    ChainingUserRegistrySynchronizer.LAST_ERROR_ATTRIBUTE,
                    zoneId);
                ChainingUserRegistrySynchronizer.this.attributeService.setAttribute(
                    statusMessage,
                    ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, 
                    ChainingUserRegistrySynchronizer.SUMMARY_ATTRIBUTE,
                    zoneId);
                return null;
            }
        }, false, true); 

    }
    
    private void notifySyncDirectoryEnd(final String zoneId, final Exception e)
    {
        this.applicationEventPublisher.publishEvent(new SynchronizeDirectoryEndEvent(this, zoneId, e));
        ChainingUserRegistrySynchronizer.logger.error("Synchronization aborted due to error", e);
        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                ChainingUserRegistrySynchronizer.this.attributeService.setAttribute(
                    SyncStatus.COMPLETE_ERROR.toString(),
                    ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, 
                    ChainingUserRegistrySynchronizer.STATUS_ATTRIBUTE, 
                    zoneId);
                ChainingUserRegistrySynchronizer.this.attributeService.setAttribute(
                    e.getMessage(),
                    ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, 
                    ChainingUserRegistrySynchronizer.LAST_ERROR_ATTRIBUTE,
                    zoneId);
                 return null;
           }
        }, false, true); 
    }
    
    @Override
    public Date getSyncStartTime()
    {
        Long start =  (Long)ChainingUserRegistrySynchronizer.this.attributeService.getAttribute(
                ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, ChainingUserRegistrySynchronizer.START_TIME_ATTRIBUTE);
   
        Date lastUserUpdate = start.longValue() == -1 ? null : new Date(start.longValue());
        return lastUserUpdate;
    }

    @Override
    public Date getSyncEndTime()
    {
        Long start =  (Long)ChainingUserRegistrySynchronizer.this.attributeService.getAttribute(
                ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, ChainingUserRegistrySynchronizer.END_TIME_ATTRIBUTE);
   
        Date lastUserUpdate = start.longValue() == -1 ? null : new Date(start.longValue());
        return lastUserUpdate;
    }

    @Override
    public String getLastErrorMessage()
    {
        String status =  (String)ChainingUserRegistrySynchronizer.this.attributeService.getAttribute(
                ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, ChainingUserRegistrySynchronizer.LAST_ERROR_ATTRIBUTE);
        return status;
    }

    @Override
    public String getLastRunOnServer()
    {
        String status =  (String)ChainingUserRegistrySynchronizer.this.attributeService.getAttribute(
                ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, ChainingUserRegistrySynchronizer.SERVER_ATTRIBUTE);
        return status;
    }

    @Override
    public String getSynchronizationStatus()
    {
        String status =  (String)ChainingUserRegistrySynchronizer.this.attributeService.getAttribute(
                ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, ChainingUserRegistrySynchronizer.STATUS_ATTRIBUTE);
        return status;

    }

    @Override
    public String getSynchronizationStatus(String zoneId)
    {
        String status =  (String)ChainingUserRegistrySynchronizer.this.attributeService.getAttribute(
                ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, ChainingUserRegistrySynchronizer.STATUS_ATTRIBUTE, zoneId);
        return status;
    }

    @Override
    public Date getSynchronizationLastUserUpdateTime(String id)
    {
        String zoneId = AuthorityService.ZONE_AUTH_EXT_PREFIX + id;
        long time = getMostRecentUpdateTime(ChainingUserRegistrySynchronizer.PERSON_LAST_MODIFIED_ATTRIBUTE, zoneId, false);
        Date lastUserUpdate = time == -1 ? null : new Date(time);
        return lastUserUpdate;
    }

    @Override
    public Date getSynchronizationLastGroupUpdateTime(String id)
    {
        String zoneId = AuthorityService.ZONE_AUTH_EXT_PREFIX + id;
        long time = getMostRecentUpdateTime(ChainingUserRegistrySynchronizer.GROUP_LAST_MODIFIED_ATTRIBUTE, zoneId, false);
        Date lastGroupUpdate = time == -1 ? null : new Date(time);
        return lastGroupUpdate;
    }

    @Override
    public String getSynchronizationLastError(String zoneId)
    {
        String status =  (String)ChainingUserRegistrySynchronizer.this.attributeService.getAttribute(
                ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, ChainingUserRegistrySynchronizer.LAST_ERROR_ATTRIBUTE, zoneId);
        return status;
    }

    @Override
    public String getSynchronizationSummary(String zoneId)
    {
        String status =  (String)ChainingUserRegistrySynchronizer.this.attributeService.getAttribute(
                ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, ChainingUserRegistrySynchronizer.SUMMARY_ATTRIBUTE, zoneId);
        return status;
    }

    public void setSysAdminParams(SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
    }

    public SysAdminParams getSysAdminParams()
    {
        return sysAdminParams;
    }
    
    @Override
    public void onApplicationEvent(ApplicationEvent event)
    {
    	 if (event instanceof AuthenticatorDeletedEvent)
         {
    		 AuthenticatorDeletedEvent deleteEvent = (AuthenticatorDeletedEvent)event;
    		 notifyZoneDeleted((String)deleteEvent.getSource());
         }
    	 else
    	 {
    		 // pass to the superclass
    		 super.onApplicationEvent(event);
    	 }
    }
}
