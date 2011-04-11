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
package org.alfresco.repo.security.sync;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.management.subsystems.ChildApplicationContextManager;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.authority.UnknownAuthorityException;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.TraceableThreadFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * A <code>ChainingUserRegistrySynchronizer</code> is responsible for synchronizing Alfresco's local user (person) and
 * group (authority) information with the external subsystems in the authentication chain (most typically LDAP
 * directories). When the {@link #synchronize(boolean)} method is called, it visits each {@link UserRegistry} bean in
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
public class ChainingUserRegistrySynchronizer extends AbstractLifecycleBean implements UserRegistrySynchronizer,
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

    /** The rule service. */
    private RuleService ruleService;

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
     * Sets the rule service.
     * 
     * @param ruleService
     *            the new rule service
     */
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
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

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.sync.UserRegistrySynchronizer#synchronize(boolean, boolean, boolean)
     */
    public void synchronize(boolean forceUpdate, boolean allowDeletions, final boolean splitTxns)
    {
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
            for (String id : instanceIds)
            {
                ApplicationContext context = this.applicationContextManager.getApplicationContext(id);
                try
                {
                    UserRegistry plugin = (UserRegistry) context.getBean(this.sourceBeanName);
                    if (!(plugin instanceof ActivateableBean) || ((ActivateableBean) plugin).isActive())
                    {
                        if (ChainingUserRegistrySynchronizer.logger.isInfoEnabled())
                        {
                            ChainingUserRegistrySynchronizer.logger
                                    .info("Synchronizing users and groups with user registry '" + id + "'");
                        }
                        if (allowDeletions && ChainingUserRegistrySynchronizer.logger.isWarnEnabled())
                        {
                            ChainingUserRegistrySynchronizer.logger
                                    .warn("Full synchronization with user registry '"
                                            + id
                                            + "'; some users and groups previously created by synchronization with this user registry may be removed.");
                        }
                        // Work out whether we should do the work in a separate transaction (it's most performant if we
                        // bunch it into small transactions, but if we are doing a sync on login, it has to be the same
                        // transaction)
                        boolean requiresNew = splitTxns
                                || AlfrescoTransactionSupport.getTransactionReadState() == TxnReadState.TXN_READ_ONLY;

                        syncWithPlugin(id, plugin, forceUpdate, allowDeletions, requiresNew, visitedZoneIds, allZoneIds);
                    }
                }
                catch (NoSuchBeanDefinitionException e)
                {
                    // Ignore and continue
                }
            }
        }
        catch (RuntimeException e)
        {
            ChainingUserRegistrySynchronizer.logger.error("Synchronization aborted due to error", e);
            throw e;
        }
        // Release the lock if necessary
        finally
        {
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
                        lockRefresher
                                .awaitTermination(ChainingUserRegistrySynchronizer.LOCK_TTL, TimeUnit.MILLISECONDS);
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
            ApplicationContext context = this.applicationContextManager.getApplicationContext(id);
            try
            {
                UserRegistry plugin = (UserRegistry) context.getBean(this.sourceBeanName);
                if (!(plugin instanceof ActivateableBean) || ((ActivateableBean) plugin).isActive())
                {
                    return plugin.getPersonMappedProperties();
                }
            }
            catch (NoSuchBeanDefinitionException e)
            {
                // Ignore and continue
            }
        }

        return Collections.emptySet();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.sync.UserRegistrySynchronizer#createMissingPerson(java.lang.String)
     */
    public boolean createMissingPerson(String userName)
    {
        // synchronize or auto-create the missing person if we are allowed
        if (userName != null && !userName.equals(AuthenticationUtil.getSystemUserName()))
        {
            if (this.syncWhenMissingPeopleLogIn)
            {
                try
                {
                    synchronize(false, false, false);
                }
                catch (Exception e)
                {
                    // We don't want to fail the whole login if we can help it
                    ChainingUserRegistrySynchronizer.logger.warn(
                            "User authenticated but failed to sync with user registry", e);
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
     * @param allowDeletions
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
            boolean allowDeletions, boolean splitTxns, final Set<String> visitedZoneIds, final Set<String> allZoneIds)
    {
        // Create a prefixed zone ID for use with the authority service
        final String zoneId = AuthorityService.ZONE_AUTH_EXT_PREFIX + zone;

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
        final BatchProcessor<NodeDescription> groupProcessor = new BatchProcessor<NodeDescription>(zone
                + " Group Analysis", this.transactionService.getRetryingTransactionHelper(), userRegistry
                .getGroups(lastModified), this.workerThreads, 20, this.applicationEventPublisher,
                ChainingUserRegistrySynchronizer.logger, this.loggingInterval);
        class Analyzer implements BatchProcessWorker<NodeDescription>
        {
            private final Map<String, String> groupsToCreate = new TreeMap<String, String>();
            private final Map<String, Set<String>> personParentAssocsToCreate = newPersonMap();
            private final Map<String, Set<String>> personParentAssocsToDelete = newPersonMap();
            private final Map<String, Set<String>> groupParentAssocsToCreate = new TreeMap<String, Set<String>>();
            private final Map<String, Set<String>> groupParentAssocsToDelete = new TreeMap<String, Set<String>>();
            private List<String> personsProcessed = new LinkedList<String>();
            private Set<String> allZonePersons;
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

            public void beforeProcess() throws Throwable
            {
                // Disable rules
                ChainingUserRegistrySynchronizer.this.ruleService.disableRules();
                // Authentication
                AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
            }

            public void afterProcess() throws Throwable
            {
                // Enable rules
                ChainingUserRegistrySynchronizer.this.ruleService.enableRules();
                // Clear authentication
                AuthenticationUtil.clearCurrentSecurityContext();
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
                    if (intersection.isEmpty())
                    {
                        // The group exists, but not in a zone that's in the authentication chain. May be due to
                        // upgrade or zone changes. Let's re-zone them
                        if (ChainingUserRegistrySynchronizer.logger.isWarnEnabled())
                        {
                            ChainingUserRegistrySynchronizer.logger.warn("Updating group '" + groupShortName
                                    + "'. This group will in future be assumed to originate from user registry '"
                                    + zone + "'.");
                        }
                        ChainingUserRegistrySynchronizer.this.authorityService.removeAuthorityFromZones(groupName,
                                groupZones);
                        ChainingUserRegistrySynchronizer.this.authorityService.addAuthorityToZones(groupName, zoneSet);
                    }
                    if (groupZones.contains(zoneId) || intersection.isEmpty())
                    {
                        // The group already existed in this zone or no valid zone: update the group
                        updateGroup(group, true);
                    }
                    else
                    {
                        // Check whether the group is in any of the higher priority authentication chain zones
                        intersection.retainAll(visitedZoneIds);
                        if (!intersection.isEmpty())
                        {
                            // A group that exists in a different zone with higher precedence
                            return;
                        }
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

            private synchronized void updateGroup(NodeDescription group, boolean existed)
            {
                PropertyMap groupProperties = group.getProperties();
                String groupName = (String) groupProperties.get(ContentModel.PROP_AUTHORITY_NAME);
                String groupDisplayName = (String) groupProperties.get(ContentModel.PROP_AUTHORITY_DISPLAY_NAME);
                if (groupDisplayName == null)
                {
                    groupDisplayName = ChainingUserRegistrySynchronizer.this.authorityService.getShortName(groupName);
                }

                // Add an entry for the parent itself, in case it is a root group
                recordParentAssociation(this.groupParentAssocsToCreate, groupName, null);

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
                    for (String child : ChainingUserRegistrySynchronizer.this.authorityService.getContainedAuthorities(
                            null, groupName, true))
                    {
                        if (AuthorityType.getAuthorityType(child) == AuthorityType.USER)
                        {
                            if (!newChildPersons.remove(child))
                            {
                                // Make sure each child features as a key in the creation map
                                recordParentAssociation(this.personParentAssocsToCreate, child, null);
                                recordParentAssociation(this.personParentAssocsToDelete, child, groupName);
                            }
                        }
                        else
                        {
                            if (!newChildGroups.remove(child))
                            {
                                // Make sure each child features as a key in the creation map
                                recordParentAssociation(this.groupParentAssocsToCreate, child, null);
                                recordParentAssociation(this.groupParentAssocsToDelete, child, groupName);
                            }
                        }
                    }
                }
                // Mark as created if new
                else
                {
                    this.groupsToCreate.put(groupName, groupDisplayName);
                }

                // Create new associations
                for (String child : newChildPersons)
                {
                    recordParentAssociation(this.personParentAssocsToCreate, child, groupName);
                }
                for (String child : newChildGroups)
                {
                    recordParentAssociation(this.groupParentAssocsToCreate, child, groupName);
                }
            }

            private void recordParentAssociation(Map<String, Set<String>> parentAssocs, String child, String parent)
            {
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

            public void processGroups(UserRegistry userRegistry, boolean allowDeletions, boolean splitTxns)
            {
                // If we got back some groups, we have to cross reference them with the set of known authorities
                if (allowDeletions || !this.groupParentAssocsToCreate.isEmpty())
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

                    final Set<String> personDeletionCandidates = newPersonSet();
                    personDeletionCandidates.addAll(allZonePersons);

                    final Set<String> groupDeletionCandidates = new TreeSet<String>();
                    groupDeletionCandidates.addAll(allZoneGroups);

                    allZoneGroups.addAll(this.groupsToCreate.keySet());

                    // Prune our set of authorities according to deletions
                    if (allowDeletions)
                    {
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

                        allZonePersons.removeAll(personDeletionCandidates);
                        allZoneGroups.removeAll(groupDeletionCandidates);
                    }

                    // Prune the group associations now that we have complete information
                    logRetainParentAssociations(this.groupParentAssocsToCreate, allZoneGroups);
                    this.groupParentAssocsToDelete.keySet().retainAll(allZoneGroups);

                    // Pruning person associations will have to wait until we have passed over all persons and built up
                    // this set
                    this.allZonePersons = allZonePersons;

                    if (!this.groupParentAssocsToCreate.isEmpty())
                    {
                        // Sort the group associations in depth-first order (root groups first) and filter out
                        // non-existent children
                        Map<String, Set<String>> sortedGroupAssociations = new LinkedHashMap<String, Set<String>>(
                                this.groupParentAssocsToCreate.size() * 2);
                        List<String> authorityPath = new ArrayList<String>(5);
                        for (String authority : this.groupParentAssocsToCreate.keySet())
                        {
                            authorityPath.add(authority);
                            visitGroupAssociations(authorityPath, this.groupParentAssocsToCreate,
                                    sortedGroupAssociations);
                            authorityPath.clear();
                        }

                        // Add the groups and their parent associations in depth-first order
                        BatchProcessor<Map.Entry<String, Set<String>>> groupCreator = new BatchProcessor<Map.Entry<String, Set<String>>>(
                                zone + " Group Creation and Association",
                                ChainingUserRegistrySynchronizer.this.transactionService.getRetryingTransactionHelper(),
                                sortedGroupAssociations.entrySet(),
                                ChainingUserRegistrySynchronizer.this.workerThreads, 20,
                                ChainingUserRegistrySynchronizer.this.applicationEventPublisher,
                                ChainingUserRegistrySynchronizer.logger,
                                ChainingUserRegistrySynchronizer.this.loggingInterval);
                        groupCreator.process(new BatchProcessWorker<Map.Entry<String, Set<String>>>()
                        {
                            public String getIdentifier(Map.Entry<String, Set<String>> entry)
                            {
                                return entry.getKey() + " " + entry.getValue();
                            }

                            public void beforeProcess() throws Throwable
                            {
                                // Disable rules
                                ChainingUserRegistrySynchronizer.this.ruleService.disableRules();
                                // Authentication
                                AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
                            }

                            public void afterProcess() throws Throwable
                            {
                                // Enable rules
                                ChainingUserRegistrySynchronizer.this.ruleService.enableRules();
                                // Clear authentication
                                AuthenticationUtil.clearCurrentSecurityContext();
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
                                maintainAssociations(child);
                            }
                        }, splitTxns);
                    }
                }
            }

            public void finalizeAssociations(UserRegistry userRegistry, boolean splitTxns)
            {
                // Remove all the associations we have already dealt with
                this.personParentAssocsToCreate.keySet().removeAll(this.personsProcessed);

                // Filter out associations to authorities that simply can't exist (and log if debugging is enabled)
                logRetainParentAssociations(this.personParentAssocsToCreate, this.allZonePersons);

                if (!this.personParentAssocsToCreate.isEmpty())
                {
                    BatchProcessor<Map.Entry<String, Set<String>>> groupCreator = new BatchProcessor<Map.Entry<String, Set<String>>>(
                            zone + " Authority Association", ChainingUserRegistrySynchronizer.this.transactionService
                                    .getRetryingTransactionHelper(), this.personParentAssocsToCreate.entrySet(),
                            ChainingUserRegistrySynchronizer.this.workerThreads, 20,
                            ChainingUserRegistrySynchronizer.this.applicationEventPublisher,
                            ChainingUserRegistrySynchronizer.logger,
                            ChainingUserRegistrySynchronizer.this.loggingInterval);
                    groupCreator.process(new BatchProcessWorker<Map.Entry<String, Set<String>>>()
                    {
                        public String getIdentifier(Map.Entry<String, Set<String>> entry)
                        {
                            return entry.getKey() + " " + entry.getValue();
                        }

                        public void beforeProcess() throws Throwable
                        {
                            // Disable rules
                            ChainingUserRegistrySynchronizer.this.ruleService.disableRules();
                            // Authentication
                            AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
                        }

                        public void afterProcess() throws Throwable
                        {
                            // Enable rules
                            ChainingUserRegistrySynchronizer.this.ruleService.enableRules();
                            // Clear authentication
                            AuthenticationUtil.clearCurrentSecurityContext();
                        }

                        public void process(Map.Entry<String, Set<String>> entry) throws Throwable
                        {
                            maintainAssociations(entry.getKey());
                        }
                    }, splitTxns);
                }
            }

            private void maintainAssociations(String authorityName)
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

                // Remember that this person's associations have been maintained
                if (isPerson)
                {
                    synchronized (this)
                    {
                        this.personsProcessed.add(authorityName);
                    }
                }
            }
        }

        final Analyzer groupAnalyzer = new Analyzer(lastModifiedMillis);
        int groupProcessedCount = groupProcessor.process(groupAnalyzer, splitTxns);

        groupAnalyzer.processGroups(userRegistry, allowDeletions, splitTxns);

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
        final BatchProcessor<NodeDescription> personProcessor = new BatchProcessor<NodeDescription>(zone
                + " User Creation and Association", this.transactionService.getRetryingTransactionHelper(),
                userRegistry.getPersons(lastModified), this.workerThreads, 10, this.applicationEventPublisher,
                ChainingUserRegistrySynchronizer.logger, this.loggingInterval);
        class PersonWorker implements BatchProcessWorker<NodeDescription>
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

            public void beforeProcess() throws Throwable
            {
                // Disable rules
                ChainingUserRegistrySynchronizer.this.ruleService.disableRules();
                // Authentication
                AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
            }

            public void afterProcess() throws Throwable
            {
                // Enable rules
                ChainingUserRegistrySynchronizer.this.ruleService.enableRules();
                // Clear authentication
                AuthenticationUtil.clearCurrentSecurityContext();
            }

            public void process(NodeDescription person) throws Throwable
            {
                // Make a mutable copy of the person properties, since they get written back to by person service
                HashMap<QName, Serializable> personProperties = new HashMap<QName, Serializable>(person.getProperties());
                String personName = (String) personProperties.get(ContentModel.PROP_USERNAME);
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
                    if (intersection.size() == 0)
                    {
                        // The person exists, but not in a zone that's in the authentication chain. May be due
                        // to upgrade or zone changes. Let's re-zone them
                        if (ChainingUserRegistrySynchronizer.logger.isWarnEnabled())
                        {
                            ChainingUserRegistrySynchronizer.logger.warn("Updating user '" + personName
                                    + "'. This user will in future be assumed to originate from user registry '" + zone
                                    + "'.");
                        }
                        ChainingUserRegistrySynchronizer.this.authorityService.removeAuthorityFromZones(personName,
                                zones);
                        ChainingUserRegistrySynchronizer.this.authorityService.addAuthorityToZones(personName, zoneSet);
                        ChainingUserRegistrySynchronizer.this.personService.setPersonProperties(personName,
                                personProperties);
                    }
                    else
                    {
                        // Check whether the user is in any of the higher priority authentication chain zones
                        intersection.retainAll(visitedZoneIds);
                        if (intersection.size() > 0)
                        {
                            // A person that exists in a different zone with higher precedence - ignore
                            return;
                        }

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

                // Maintain associations
                groupAnalyzer.maintainAssociations(personName);

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
        if (allowDeletions)
        {
            BatchProcessor<String> authorityDeletionProcessor = new BatchProcessor<String>(
                    zone + " Authority Deletion", this.transactionService.getRetryingTransactionHelper(), groupAnalyzer
                            .getDeletionCandidates(), this.workerThreads, 10, this.applicationEventPublisher,
                    ChainingUserRegistrySynchronizer.logger, this.loggingInterval);
            class AuthorityDeleter implements BatchProcessWorker<String>
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

                public void beforeProcess() throws Throwable
                {
                    // Disable rules
                    ChainingUserRegistrySynchronizer.this.ruleService.disableRules();
                    // Authentication
                    AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
                }

                public void afterProcess() throws Throwable
                {
                    // Enable rules
                    ChainingUserRegistrySynchronizer.this.ruleService.enableRules();
                    // Clear authentication
                    AuthenticationUtil.clearCurrentSecurityContext();
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

        if (ChainingUserRegistrySynchronizer.logger.isInfoEnabled())
        {
            ChainingUserRegistrySynchronizer.logger.info("Finished synchronizing users and groups with user registry '"
                    + zone + "'");
            ChainingUserRegistrySynchronizer.logger.info(personProcessedCount + " user(s) and " + groupProcessedCount
                    + " group(s) processed");
        }
    }

    /**
     * Visits the last authority in the given list by recursively visiting its parents in associationsOld and then
     * adding the authority to associationsNew. Used to sort associationsOld into 'depth-first' order.
     * 
     * @param authorityPath
     *            The authority to visit, preceeded by all its descendants. Allows detection of cyclic child
     *            associations.
     * @param associationsOld
     *            the association map to sort
     * @param associationsNew
     *            the association map to add to in depth first order
     */
    private void visitGroupAssociations(List<String> authorityPath, Map<String, Set<String>> associationsOld,
            Map<String, Set<String>> associationsNew)
    {
        String authorityName = authorityPath.get(authorityPath.size() - 1);
        if (!associationsNew.containsKey(authorityName))
        {
            Set<String> associations = associationsOld.get(authorityName);

            if (!associations.isEmpty())
            {
                int insertIndex = authorityPath.size();
                Iterator<String> i = associations.iterator();
                while (i.hasNext())
                {
                    String parentAuthority = i.next();

                    // Prevent cyclic paths
                    if (authorityPath.contains(parentAuthority))
                    {
                        if (ChainingUserRegistrySynchronizer.logger.isWarnEnabled())
                        {
                            ChainingUserRegistrySynchronizer.logger.warn("Detected cyclic dependencies in group '"
                                    + ChainingUserRegistrySynchronizer.this.authorityService
                                            .getShortName(parentAuthority) + "'");
                        }
                        i.remove();
                    }
                    else
                    {
                        authorityPath.add(parentAuthority);
                        visitGroupAssociations(authorityPath, associationsOld, associationsNew);
                        authorityPath.remove(insertIndex);
                    }
                }
            }

            // Omit associations from users from this map, as they will be processed separately
            if (AuthorityType.getAuthorityType(authorityName) != AuthorityType.USER)
            {
                associationsNew.put(authorityName, associations);
            }
        }
    }

    /**
     * Gets the persisted most recent update time for a label and zone.
     * 
     * @param label
     *            the label
     * @param zoneId
     *            the zone id
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
    private Set<String> getZones(String zoneId)
    {
        Set<String> zones = new HashSet<String>(5);
        zones.add(AuthorityService.ZONE_APP_DEFAULT);
        zones.add(zoneId);
        return zones;
    }

    /*
     * (non-Javadoc)
     * @seeorg.springframework.extensions.surf.util.AbstractLifecycleBean#onBootstrap(org.springframework.context.
     * ApplicationEvent)
     */
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
                        synchronize(false, false, true);
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
}
