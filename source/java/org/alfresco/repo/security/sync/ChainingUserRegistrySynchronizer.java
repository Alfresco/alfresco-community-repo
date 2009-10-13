/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.security.sync;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.attributes.Attribute;
import org.alfresco.repo.attributes.LongAttributeValue;
import org.alfresco.repo.attributes.MapAttributeValue;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.management.subsystems.ChildApplicationContextManager;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.AbstractLifecycleBean;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;

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
public class ChainingUserRegistrySynchronizer extends AbstractLifecycleBean implements UserRegistrySynchronizer
{

    /** The number of users / groups we add at a time in a transaction *. */
    private static final int BATCH_SIZE = 10;

    /** The logger. */
    private static final Log logger = LogFactory.getLog(ChainingUserRegistrySynchronizer.class);

    /** The name of the lock used to ensure that a synchronize does not run on more than one node at the same time. */
    private static final QName LOCK_QNAME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI,
            "ChainingUserRegistrySynchronizer");

    /** The maximum time this lock will be held for (1 day). */
    private static final long LOCK_TTL = 1000 * 60 * 60 * 24;

    /** The path in the attribute service below which we persist attributes. */
    private static final String ROOT_ATTRIBUTE_PATH = ".ChainingUserRegistrySynchronizer";

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

    /** The retrying transaction helper. */
    private RetryingTransactionHelper retryingTransactionHelper;

    /** The job lock service. */
    private JobLockService jobLockService;

    /** Should we trigger a differential sync when missing people log in?. */
    private boolean syncWhenMissingPeopleLogIn = true;

    /** Should we trigger a differential sync on startup?. */
    private boolean syncOnStartup = true;

    /** Should we auto create a missing person on log in?. */
    private boolean autoCreatePeopleOnLogin = true;

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
     * Sets the retrying transaction helper.
     * 
     * @param retryingTransactionHelper
     *            the new retrying transaction helper
     */
    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
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

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.sync.UserRegistrySynchronizer#synchronize(boolean, boolean)
     */
    public void synchronize(boolean force, boolean splitTxns)
    {
        // First, try to obtain a lock to ensure we are the only node trying to run this job
        try
        {
            if (splitTxns)
            {
                // If this is an automated sync on startup or scheduled sync, don't even wait around for the lock.
                // Assume the sync will be completed on another node.
                this.jobLockService.getTransactionalLock(ChainingUserRegistrySynchronizer.LOCK_QNAME,
                        ChainingUserRegistrySynchronizer.LOCK_TTL, 0, 1);
            }
            else
            {
                // If this is a login-triggered sync, give it a few retries before giving up
                this.jobLockService.getTransactionalLock(ChainingUserRegistrySynchronizer.LOCK_QNAME,
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

        Set<String> visitedZoneIds = new TreeSet<String>();
        Collection<String> instanceIds = this.applicationContextManager.getInstanceIds();

        // Work out the set of all zone IDs in the authentication chain so that we can decide which users / groups
        // need
        // 're-zoning'
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
                    if (force && ChainingUserRegistrySynchronizer.logger.isWarnEnabled())
                    {
                        ChainingUserRegistrySynchronizer.logger
                                .warn("Forced synchronization with user registry '"
                                        + id
                                        + "'; some users and groups previously created by synchronization with this user registry may be removed.");
                    }
                    // Work out whether we should do the work in a separate transaction (it's most performant if we
                    // bunch it into small transactions, but if we are doing a sync on login, it has to be the same
                    // transaction)
                    boolean requiresNew = splitTxns
                            || AlfrescoTransactionSupport.getTransactionReadState() == TxnReadState.TXN_READ_ONLY;

                    int personsProcessed = syncPersonsWithPlugin(id, plugin, force, requiresNew, visitedZoneIds,
                            allZoneIds);
                    int groupsProcessed = syncGroupsWithPlugin(id, plugin, force, requiresNew, visitedZoneIds,
                            allZoneIds);
                    if (ChainingUserRegistrySynchronizer.logger.isInfoEnabled())
                    {
                        ChainingUserRegistrySynchronizer.logger
                                .info("Finished synchronizing users and groups with user registry '" + id + "'");
                        ChainingUserRegistrySynchronizer.logger.info(personsProcessed + " user(s) and "
                                + groupsProcessed + " group(s) processed");
                    }
                }
            }
            catch (NoSuchBeanDefinitionException e)
            {
                // Ignore and continue
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.sync.UserRegistrySynchronizer#ensureExists(java.lang.String)
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
                    synchronize(false, false);
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
     * Synchronizes local users (persons) with a {@link UserRegistry} for a particular zone.
     * 
     * @param zone
     *            the zone id. This identifier is used to tag all created users, so that in the future we can tell those
     *            that have been deleted from the registry.
     * @param userRegistry
     *            the user registry for the zone.
     * @param force
     *            <code>true</code> if all persons are to be queried. <code>false</code> if only those changed since the
     *            most recent queried user should be queried.
     * @param splitTxns
     *            Can the modifications to Alfresco be split across multiple transactions for maximum performance? If
     *            <code>true</code>, users and groups are created/updated in batches of 10 for increased performance. If
     *            <code>false</code>, all users and groups are processed in the current transaction. This is required if
     *            calling synchronously (e.g. in response to an authentication event in the same transaction).
     * @param visitedZoneIds
     *            the set of zone ids already processed. These zones have precedence over the current zone when it comes
     *            to user name 'collisions'. If a user is queried that already exists locally but is tagged with one of
     *            the zones in this set, then it will be ignored as this zone has lower priority.
     * @param allZoneIds
     *            the set of all zone ids in the authentication chain. Helps us work out whether the zone information
     *            recorded against a user is invalid for the current authentication chain and whether the user needs to
     *            be 're-zoned'.
     * @return the number of users processed
     */
    private int syncPersonsWithPlugin(final String zone, UserRegistry userRegistry, boolean force, boolean splitTxns,
            final Set<String> visitedZoneIds, final Set<String> allZoneIds)
    {
        // Create a prefixed zone ID for use with the authority service
        final String zoneId = AuthorityService.ZONE_AUTH_EXT_PREFIX + zone;

        // The set of zones we associate with new objects (default plus registry specific)
        final Set<String> zoneSet = getZones(zoneId);

        final long lastModifiedMillis = force ? -1L : getMostRecentUpdateTime(
                ChainingUserRegistrySynchronizer.PERSON_LAST_MODIFIED_ATTRIBUTE, zoneId);
        final Date lastModified = lastModifiedMillis == -1 ? null : new Date(lastModifiedMillis);
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
        final Iterator<NodeDescription> persons = userRegistry.getPersons(lastModified);
        final Set<String> personsCreated = new TreeSet<String>();

        class CreationWorker implements RetryingTransactionCallback<Integer>
        {
            private long latestTime = lastModifiedMillis;

            public long getLatestTime()
            {
                return this.latestTime;
            }

            /*
             * (non-Javadoc)
             * @see org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback#execute()
             */
            public Integer execute() throws Throwable
            {
                int processedCount = 0;
                do
                {
                    NodeDescription person = persons.next();
                    PropertyMap personProperties = person.getProperties();
                    String personName = (String) personProperties.get(ContentModel.PROP_USERNAME);

                    Set<String> zones = ChainingUserRegistrySynchronizer.this.authorityService
                            .getAuthorityZones(personName);
                    if (zones == null)
                    {
                        // The person did not exist at all
                        if (ChainingUserRegistrySynchronizer.logger.isInfoEnabled())
                        {
                            ChainingUserRegistrySynchronizer.logger.info("Creating user '" + personName + "'");
                        }
                        ChainingUserRegistrySynchronizer.this.personService.createPerson(personProperties, zoneSet);
                    }
                    else if (zones.contains(zoneId))
                    {
                        // The person already existed in this zone: update the person
                        if (ChainingUserRegistrySynchronizer.logger.isInfoEnabled())
                        {
                            ChainingUserRegistrySynchronizer.logger.info("Updating user '" + personName + "'");
                        }
                        ChainingUserRegistrySynchronizer.this.personService.setPersonProperties(personName,
                                personProperties);
                    }
                    else
                    {
                        // Check whether the user is in any of the authentication chain zones
                        Set<String> intersection = new TreeSet<String>(zones);
                        intersection.retainAll(allZoneIds);
                        if (intersection.size() == 0)
                        {
                            // The person exists, but not in a zone that's in the authentication chain. May be due to
                            // upgrade or zone changes. Let's re-zone them
                            if (ChainingUserRegistrySynchronizer.logger.isWarnEnabled())
                            {
                                ChainingUserRegistrySynchronizer.logger.warn("Updating user '" + personName
                                        + "'. This user will in future be assumed to originate from user registry '"
                                        + zone + "'.");
                            }
                            ChainingUserRegistrySynchronizer.this.authorityService.removeAuthorityFromZones(personName,
                                    zones);
                            ChainingUserRegistrySynchronizer.this.authorityService.addAuthorityToZones(personName,
                                    zoneSet);
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
                                continue;
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
                    // Increment the count of processed people
                    personsCreated.add(personName);
                    processedCount++;

                    // Maintain the last modified date
                    Date personLastModified = person.getLastModified();
                    if (personLastModified != null)
                    {
                        this.latestTime = Math.max(this.latestTime, personLastModified.getTime());
                    }
                }
                while (persons.hasNext() && processedCount < ChainingUserRegistrySynchronizer.BATCH_SIZE);
                return processedCount;
            }
        }

        CreationWorker creations = new CreationWorker();
        int processedCount = 0;
        while (persons.hasNext())
        {
            processedCount += this.retryingTransactionHelper.doInTransaction(creations, false, splitTxns);
        }
        long latestTime = creations.getLatestTime();
        if (latestTime != -1)
        {
            setMostRecentUpdateTime(ChainingUserRegistrySynchronizer.PERSON_LAST_MODIFIED_ATTRIBUTE, zoneId, latestTime);
        }

        // Handle deletions if we are doing a full sync
        if (force)
        {
            class DeletionWorker implements RetryingTransactionCallback<Integer>
            {
                /*
                 * (non-Javadoc)
                 * @see org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback#execute()
                 */
                public Integer execute() throws Throwable
                {
                    int processedCount = 0;
                    Set<String> personsToDelete = ChainingUserRegistrySynchronizer.this.authorityService
                            .getAllAuthoritiesInZone(zoneId, AuthorityType.USER);
                    personsToDelete.removeAll(personsCreated);
                    for (String personName : personsToDelete)
                    {
                        if (ChainingUserRegistrySynchronizer.logger.isWarnEnabled())
                        {
                            ChainingUserRegistrySynchronizer.logger.warn("Deleting user '" + personName + "'");
                        }
                        ChainingUserRegistrySynchronizer.this.personService.deletePerson(personName);
                        processedCount++;
                    }
                    return processedCount;
                }
            }

            // Just use a single transaction
            DeletionWorker deletions = new DeletionWorker();
            processedCount += this.retryingTransactionHelper.doInTransaction(deletions, false, splitTxns);
        }

        // Remember we have visited this zone
        visitedZoneIds.add(zoneId);

        return processedCount;
    }

    /**
     * Synchronizes local groups (authorities) with a {@link UserRegistry} for a particular zone.
     * 
     * @param zone
     *            the zone id. This identifier is used to tag all created groups, so that in the future we can tell
     *            those that have been deleted from the registry.
     * @param userRegistry
     *            the user registry for the zone.
     * @param force
     *            <code>true</code> if all groups are to be queried. <code>false</code> if only those changed since the
     *            most recent queried group should be queried.
     * @param splitTxns
     *            Can the modifications to Alfresco be split across multiple transactions for maximum performance? If
     *            <code>true</code>, users and groups are created/updated in batches of 10 for increased performance. If
     *            <code>false</code>, all users and groups are processed in the current transaction. This is required if
     *            calling synchronously (e.g. in response to an authentication event in the same transaction).
     * @param visitedZoneIds
     *            the set of zone ids already processed. These zones have precedence over the current zone when it comes
     *            to group name 'collisions'. If a group is queried that already exists locally but is tagged with one
     *            of the zones in this set, then it will be ignored as this zone has lower priority.
     * @param allZoneIds
     *            the set of all zone ids in the authentication chain. Helps us work out whether the zone information
     *            recorded against a group is invalid for the current authentication chain and whether the group needs
     *            to be 're-zoned'.
     * @return the number of groups processed
     */
    private int syncGroupsWithPlugin(final String zone, UserRegistry userRegistry, boolean force, boolean splitTxns,
            final Set<String> visitedZoneIds, final Set<String> allZoneIds)
    {
        // Create a prefixed zone ID for use with the authority service
        final String zoneId = AuthorityService.ZONE_AUTH_EXT_PREFIX + zone;

        // The set of zones we associate with new objects (default plus registry specific)
        final Set<String> zoneSet = getZones(zoneId);

        final long lastModifiedMillis = force ? -1L : getMostRecentUpdateTime(
                ChainingUserRegistrySynchronizer.GROUP_LAST_MODIFIED_ATTRIBUTE, zoneId);
        final Date lastModified = lastModifiedMillis == -1 ? null : new Date(lastModifiedMillis);

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

        final Iterator<NodeDescription> groups = userRegistry.getGroups(lastModified);
        final Map<String, Set<String>> groupAssocsToCreate = new TreeMap<String, Set<String>>();
        final Set<String> groupsCreated = new TreeSet<String>();

        class CreationWorker implements RetryingTransactionCallback<Integer>
        {
            private long latestTime = lastModifiedMillis;

            public long getLatestTime()
            {
                return this.latestTime;
            }

            /*
             * (non-Javadoc)
             * @see org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback#execute()
             */
            public Integer execute() throws Throwable
            {
                int processedCount = 0;
                do
                {
                    NodeDescription group = groups.next();
                    PropertyMap groupProperties = group.getProperties();
                    String groupName = (String) groupProperties.get(ContentModel.PROP_AUTHORITY_NAME);
                    String groupShortName = ChainingUserRegistrySynchronizer.this.authorityService
                            .getShortName(groupName);
                    Set<String> groupZones = ChainingUserRegistrySynchronizer.this.authorityService
                            .getAuthorityZones(groupName);

                    if (groupZones == null)
                    {
                        // The group did not exist at all
                        if (ChainingUserRegistrySynchronizer.logger.isInfoEnabled())
                        {
                            ChainingUserRegistrySynchronizer.logger.info("Creating group '" + groupShortName + "'");
                        }
                        // create the group
                        ChainingUserRegistrySynchronizer.this.authorityService.createAuthority(AuthorityType
                                .getAuthorityType(groupName), groupShortName, (String) groupProperties
                                .get(ContentModel.PROP_AUTHORITY_DISPLAY_NAME), zoneSet);
                        Set<String> children = group.getChildAssociations();
                        if (!children.isEmpty())
                        {
                            groupAssocsToCreate.put(groupName, children);
                        }
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
                            ChainingUserRegistrySynchronizer.this.authorityService.addAuthorityToZones(groupName,
                                    zoneSet);
                        }
                        if (groupZones.contains(zoneId) || intersection.isEmpty())
                        {
                            // The group already existed in this zone or no valid zone: update the group
                            Set<String> oldChildren = ChainingUserRegistrySynchronizer.this.authorityService
                                    .getContainedAuthorities(null, groupName, true);
                            Set<String> newChildren = group.getChildAssociations();
                            Set<String> toDelete = new TreeSet<String>(oldChildren);
                            Set<String> toAdd = new TreeSet<String>(newChildren);
                            toDelete.removeAll(newChildren);
                            toAdd.removeAll(oldChildren);
                            if (!toAdd.isEmpty())
                            {
                                groupAssocsToCreate.put(groupName, toAdd);
                            }
                            for (String child : toDelete)
                            {
                                if (ChainingUserRegistrySynchronizer.logger.isInfoEnabled())
                                {
                                    ChainingUserRegistrySynchronizer.logger.info("Removing '"
                                            + ChainingUserRegistrySynchronizer.this.authorityService
                                                    .getShortName(child) + "' from group '" + groupShortName + "'");
                                }
                                ChainingUserRegistrySynchronizer.this.authorityService
                                        .removeAuthority(groupName, child);
                            }
                        }
                        else
                        {
                            // Check whether the group is in any of the higher priority authentication chain zones
                            intersection.retainAll(visitedZoneIds);
                            if (!intersection.isEmpty())
                            {
                                // A group that exists in a different zone with higher precedence
                                continue;
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
                            ChainingUserRegistrySynchronizer.this.authorityService.createAuthority(AuthorityType
                                    .getAuthorityType(groupName), groupShortName, (String) groupProperties
                                    .get(ContentModel.PROP_AUTHORITY_DISPLAY_NAME), zoneSet);
                            Set<String> children = group.getChildAssociations();
                            if (!children.isEmpty())
                            {
                                groupAssocsToCreate.put(groupName, children);
                            }
                        }
                    }
                    // Increment the count of processed groups
                    processedCount++;
                    groupsCreated.add(groupName);

                    // Maintain the last modified date
                    Date groupLastModified = group.getLastModified();
                    if (groupLastModified != null)
                    {
                        this.latestTime = Math.max(this.latestTime, groupLastModified.getTime());
                    }

                }
                while (groups.hasNext() && processedCount < ChainingUserRegistrySynchronizer.BATCH_SIZE);
                return processedCount;
            }
        }

        CreationWorker creations = new CreationWorker();
        int processedCount = 0;
        while (groups.hasNext())
        {
            processedCount += this.retryingTransactionHelper.doInTransaction(creations, false, splitTxns);
        }
        long latestTime = creations.getLatestTime();
        if (latestTime != -1)
        {
            setMostRecentUpdateTime(ChainingUserRegistrySynchronizer.GROUP_LAST_MODIFIED_ATTRIBUTE, zoneId, latestTime);
        }

        // Add the new associations, now that we have created everything

        final Iterator<Map.Entry<String, Set<String>>> groupAssocs = groupAssocsToCreate.entrySet().iterator();
        class AssocWorker implements RetryingTransactionCallback<Integer>
        {
            /*
             * (non-Javadoc)
             * @see org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback#execute()
             */
            public Integer execute() throws Throwable
            {
                int processedCount = 0;
                do
                {

                    Map.Entry<String, Set<String>> entry = groupAssocs.next();
                    for (String child : entry.getValue())
                    {
                        String groupName = entry.getKey();
                        if (ChainingUserRegistrySynchronizer.logger.isInfoEnabled())
                        {
                            ChainingUserRegistrySynchronizer.logger.info("Adding '"
                                    + ChainingUserRegistrySynchronizer.this.authorityService.getShortName(child)
                                    + "' to group '"
                                    + ChainingUserRegistrySynchronizer.this.authorityService.getShortName(groupName)
                                    + "'");
                        }
                        try
                        {
                            ChainingUserRegistrySynchronizer.this.authorityService.addAuthority(groupName, child);
                        }
                        catch (Exception e)
                        {
                            // Let's not allow referential integrity problems (dangling references) kill the whole
                            // process
                            if (ChainingUserRegistrySynchronizer.logger.isWarnEnabled())
                            {
                                ChainingUserRegistrySynchronizer.logger.warn("Failed to add '"
                                        + ChainingUserRegistrySynchronizer.this.authorityService.getShortName(child)
                                        + "' to group '"
                                        + ChainingUserRegistrySynchronizer.this.authorityService
                                                .getShortName(groupName) + "'", e);
                            }
                        }

                    }
                }
                while (groupAssocs.hasNext() && processedCount < ChainingUserRegistrySynchronizer.BATCH_SIZE);
                return processedCount;

            }
        }

        AssocWorker assocs = new AssocWorker();
        while (groupAssocs.hasNext())
        {
            this.retryingTransactionHelper.doInTransaction(assocs, false, splitTxns);
        }

        // Delete groups if we have complete information for the zone
        if (force)
        {
            class DeletionWorker implements RetryingTransactionCallback<Integer>
            {
                /*
                 * (non-Javadoc)
                 * @see org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback#execute()
                 */
                public Integer execute() throws Throwable
                {
                    int processedCount = 0;
                    Set<String> groupsToDelete = ChainingUserRegistrySynchronizer.this.authorityService
                            .getAllAuthoritiesInZone(zoneId, AuthorityType.GROUP);
                    groupsToDelete.removeAll(groupsCreated);
                    for (String group : groupsToDelete)
                    {
                        if (ChainingUserRegistrySynchronizer.logger.isWarnEnabled())
                        {
                            ChainingUserRegistrySynchronizer.logger.warn("Deleting group '"
                                    + ChainingUserRegistrySynchronizer.this.authorityService.getShortName(group) + "'");
                        }
                        ChainingUserRegistrySynchronizer.this.authorityService.deleteAuthority(group);
                        processedCount++;
                    }
                    return processedCount;
                }
            }

            // Just use a single transaction
            DeletionWorker deletions = new DeletionWorker();
            processedCount += this.retryingTransactionHelper.doInTransaction(deletions, false, splitTxns);
        }

        // Remember we have visited this zone
        visitedZoneIds.add(zoneId);

        return processedCount;
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
    private long getMostRecentUpdateTime(String label, String zoneId)
    {
        Attribute attribute = this.attributeService.getAttribute(ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH
                + '/' + label + '/' + zoneId);
        return attribute == null ? -1 : attribute.getLongValue();
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
     */
    private void setMostRecentUpdateTime(String label, String zoneId, long lastModifiedMillis)
    {
        String path = ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH + '/' + label;
        if (!this.attributeService.exists(path))
        {
            if (!this.attributeService.exists(ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH))
            {
                this.attributeService.setAttribute("", ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH,
                        new MapAttributeValue());
            }
            this.attributeService.setAttribute(ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, label,
                    new MapAttributeValue());
        }
        this.attributeService.setAttribute(path, zoneId, new LongAttributeValue(lastModifiedMillis));
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
        Set<String> zones = new HashSet<String>(2, 1.0f);
        zones.add(AuthorityService.ZONE_APP_DEFAULT);
        zones.add(zoneId);
        return zones;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.util.AbstractLifecycleBean#onBootstrap(org.springframework.context.ApplicationEvent)
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
                    return ChainingUserRegistrySynchronizer.this.retryingTransactionHelper
                            .doInTransaction(new RetryingTransactionCallback<Object>()
                            {

                                public Object execute() throws Throwable
                                {
                                    try
                                    {
                                        synchronize(false, true);
                                    }
                                    catch (Exception e)
                                    {
                                        ChainingUserRegistrySynchronizer.logger.warn(
                                                "Failed initial synchronize with user registries", e);
                                    }
                                    return null;
                                }
                            });
                }
            }, AuthenticationUtil.getSystemUserName());
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.util.AbstractLifecycleBean#onShutdown(org.springframework.context.ApplicationEvent)
     */
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
    }

}
