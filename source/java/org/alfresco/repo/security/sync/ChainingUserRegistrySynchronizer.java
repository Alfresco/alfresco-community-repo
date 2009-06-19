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
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.management.subsystems.ChildApplicationContextManager;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * A <code>ChainingUserRegistrySynchronizer</code> is responsible for synchronizing Alfresco's local user (person) and
 * group (authority) information with the external subsystems in the authentication chain (most typically LDAP
 * directories). When the {@link #synchronize(boolean)} method is called, it visits each {@link UserRegistry} bean in
 * the 'chain' of application contexts, managed by a {@link ChildApplicationContextManager}, and compares its
 * timestamped user and group information with the local users and groups last retrieved from the same source. Any
 * updates and additions made to those users and groups are applied to the local copies. The ordering of each
 * {@link UserRegistry} in the chain determines its precedence when it comes to user and group name collisions.
 * <p>
 * The <code>force</code> argument determines whether a complete or partial set of information is queried from the
 * {@link UserRegistry}. When <code>true</code> then <i>all</i> users and groups are queried. With this complete set of
 * information, the synchronizer is able to identify which users and groups have been deleted, so it will delete users
 * and groups as well as update and create them. Since processing all users and groups may be fairly time consuming, it
 * is recommended this mode is only used by a background scheduled synchronization job. When the argument is
 * <code>false</code> then only those users and groups modified since the most recent modification date of all the
 * objects last queried from the same {@link UserRegistry} are retrieved. In this mode, local users and groups are
 * created and updated, but not deleted (except where a name collision with a lower priority {@link UserRegistry} is
 * detected). This 'differential' mode is much faster, and by default is triggered by
 * {@link #createMissingPerson(String)} when a user is successfully authenticated who doesn't yet have a local person
 * object in Alfresco. This should mean that new users and their group information are pulled over from LDAP servers as
 * and when required.
 * 
 * @author dward
 */
public class ChainingUserRegistrySynchronizer implements UserRegistrySynchronizer
{

    /** The logger. */
    private static final Log logger = LogFactory.getLog(ChainingUserRegistrySynchronizer.class);

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

    /** Should we trigger a sync when missing people log in? */
    private boolean syncWhenMissingPeopleLogIn = true;

    /** Should we auto create a missing person on log in? */
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
     * Controls whether we auto create a missing person on log in
     * 
     * @param autoCreatePeopleOnLogin
     *            <code>true</code> if we should auto create a missing person on log in
     */
    public void setAutoCreatePeopleOnLogin(boolean autoCreatePeopleOnLogin)
    {
        this.autoCreatePeopleOnLogin = autoCreatePeopleOnLogin;
    }

    /**
     * Controls whether we trigger a sync when missing people log in
     * 
     * @param syncWhenMissingPeopleLogIn
     *            <codetrue</code> if we should trigger a sync when missing people log in
     */
    public void setSyncWhenMissingPeopleLogIn(boolean syncWhenMissingPeopleLogIn)
    {
        this.syncWhenMissingPeopleLogIn = syncWhenMissingPeopleLogIn;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.sync.UserRegistrySynchronizer#synchronize(boolean)
     */
    public void synchronize(boolean force)
    {
        Set<String> visitedZoneIds = new TreeSet<String>();
        for (String id : this.applicationContextManager.getInstanceIds())
        {
            StringBuilder builder = new StringBuilder(32);
            builder.append(AuthorityService.ZONE_AUTH_EXT_PREFIX);
            builder.append(id);
            String zoneId = builder.toString();
            ApplicationContext context = this.applicationContextManager.getApplicationContext(id);
            try
            {
                UserRegistry plugin = (UserRegistry) context.getBean(this.sourceBeanName);
                if (!(plugin instanceof ActivateableBean) || ((ActivateableBean) plugin).isActive())
                {
                    ChainingUserRegistrySynchronizer.logger.info("Synchronizing users and groups with user registry '"
                            + id + "'");
                    if (force)
                    {
                        ChainingUserRegistrySynchronizer.logger
                                .warn("Forced synchronization with user registry '"
                                        + id
                                        + "'; some users and groups previously created by synchronization with this user registry may be removed.");
                    }
                    int personsProcessed = syncPersonsWithPlugin(zoneId, plugin, force, visitedZoneIds);
                    int groupsProcessed = syncGroupsWithPlugin(zoneId, plugin, force, visitedZoneIds);
                    ChainingUserRegistrySynchronizer.logger
                            .info("Finished synchronizing users and groups with user registry '" + zoneId + "'");
                    ChainingUserRegistrySynchronizer.logger.info(personsProcessed + " user(s) and " + groupsProcessed
                            + " group(s) processed");
                }
            }
            catch (NoSuchBeanDefinitionException e)
            {
                // Ignore and continue
            }
            visitedZoneIds.add(zoneId);
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
                synchronize(false);
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
     * @param zoneId
     *            the zone id. This identifier is used to tag all created users, so that in the future we can tell those
     *            that have been deleted from the registry.
     * @param userRegistry
     *            the user registry for the zone.
     * @param force
     *            <code>true</code> if all persons are to be queried. <code>false</code> if only those changed since the
     *            most recent queried user should be queried.
     * @param visitedZoneIds
     *            the set of zone ids already processed. These zones have precedence over the current zone when it comes
     *            to user name 'collisions'. If a user is queried that already exists locally but is tagged with one of
     *            the zones in this set, then it will be ignored as this zone has lower priority.
     * @return the number of users processed
     */
    private int syncPersonsWithPlugin(String zoneId, UserRegistry userRegistry, boolean force,
            Set<String> visitedZoneIds)
    {
        int processedCount = 0;
        long lastModifiedMillis = force ? -1L : getMostRecentUpdateTime(
                ChainingUserRegistrySynchronizer.PERSON_LAST_MODIFIED_ATTRIBUTE, zoneId);
        Date lastModified = lastModifiedMillis == -1 ? null : new Date(lastModifiedMillis);
        if (lastModified == null)
        {
            ChainingUserRegistrySynchronizer.logger.info("Retrieving all users from user registry '" + zoneId + "'");
        }
        else
        {
            ChainingUserRegistrySynchronizer.logger.info("Retrieving users changed since "
                    + DateFormat.getDateTimeInstance().format(lastModified) + " from user registry '" + zoneId + "'");
        }
        Iterator<NodeDescription> persons = userRegistry.getPersons(lastModified);
        Set<String> personsToDelete = this.authorityService.getAllAuthoritiesInZone(zoneId, AuthorityType.USER);
        while (persons.hasNext())
        {
            NodeDescription person = persons.next();
            PropertyMap personProperties = person.getProperties();
            String personName = (String) personProperties.get(ContentModel.PROP_USERNAME);
            if (personsToDelete.remove(personName))
            {
                // The person already existed in this zone: update the person
                ChainingUserRegistrySynchronizer.logger.info("Updating user '" + personName + "'");
                this.personService.setPersonProperties(personName, personProperties);
            }
            else
            {
                // The person does not exist in this zone, but may exist in another zone
                Set<String> zones = this.authorityService.getAuthorityZones(personName);
                if (zones != null)
                {
                    zones.retainAll(visitedZoneIds);
                    if (zones.size() > 0)
                    {
                        // A person that exists in a different zone with higher precedence
                        continue;
                    }
                    // The person existed, but in a zone with lower precedence
                    ChainingUserRegistrySynchronizer.logger
                            .warn("Recreating occluded user '"
                                    + personName
                                    + "'. This user was previously created manually or through synchronization with a lower priority user registry.");
                    this.personService.deletePerson(personName);
                }
                else
                {
                    // The person did not exist at all
                    ChainingUserRegistrySynchronizer.logger.info("Creating user '" + personName + "'");
                }
                this.personService.createPerson(personProperties, getZones(zoneId));
            }
            // Increment the count of processed people
            processedCount++;

            // Maintain the last modified date
            Date personLastModified = person.getLastModified();
            if (personLastModified != null)
            {
                lastModifiedMillis = Math.max(lastModifiedMillis, personLastModified.getTime());
            }
        }

        if (force && !personsToDelete.isEmpty())
        {
            for (String personName : personsToDelete)
            {
                ChainingUserRegistrySynchronizer.logger.warn("Deleting user '" + personName + "'");
                this.personService.deletePerson(personName);
                processedCount++;
            }
        }

        if (lastModifiedMillis != -1)
        {
            setMostRecentUpdateTime(ChainingUserRegistrySynchronizer.PERSON_LAST_MODIFIED_ATTRIBUTE, zoneId,
                    lastModifiedMillis);
        }

        return processedCount;
    }

    /**
     * Synchronizes local groups (authorities) with a {@link UserRegistry} for a particular zone.
     * 
     * @param zoneId
     *            the zone id. This identifier is used to tag all created groups, so that in the future we can tell
     *            those that have been deleted from the registry.
     * @param userRegistry
     *            the user registry for the zone.
     * @param force
     *            <code>true</code> if all groups are to be queried. <code>false</code> if only those changed since the
     *            most recent queried group should be queried.
     * @param visitedZoneIds
     *            the set of zone ids already processed. These zones have precedence over the current zone when it comes
     *            to group name 'collisions'. If a group is queried that already exists locally but is tagged with one
     *            of the zones in this set, then it will be ignored as this zone has lower priority.
     * @return the number of groups processed
     */
    private int syncGroupsWithPlugin(String zoneId, UserRegistry userRegistry, boolean force, Set<String> visitedZoneIds)
    {
        int processedCount = 0;
        long lastModifiedMillis = force ? -1L : getMostRecentUpdateTime(
                ChainingUserRegistrySynchronizer.GROUP_LAST_MODIFIED_ATTRIBUTE, zoneId);
        Date lastModified = lastModifiedMillis == -1 ? null : new Date(lastModifiedMillis);
        if (lastModified == null)
        {
            ChainingUserRegistrySynchronizer.logger.info("Retrieving all groups from user registry '" + zoneId + "'");
        }
        else
        {
            ChainingUserRegistrySynchronizer.logger.info("Retrieving groups changed since "
                    + DateFormat.getDateTimeInstance().format(lastModified) + " from user registry '" + zoneId + "'");
        }

        Iterator<NodeDescription> groups = userRegistry.getGroups(lastModified);
        Map<String, Set<String>> groupAssocsToCreate = new TreeMap<String, Set<String>>();
        Set<String> groupsToDelete = this.authorityService.getAllAuthoritiesInZone(zoneId, AuthorityType.GROUP);
        while (groups.hasNext())
        {
            NodeDescription group = groups.next();
            PropertyMap groupProperties = group.getProperties();
            String groupName = (String) groupProperties.get(ContentModel.PROP_AUTHORITY_NAME);
            if (groupsToDelete.remove(groupName))
            {
                // update an existing group in the same zone
                Set<String> oldChildren = this.authorityService.getContainedAuthorities(null, groupName, true);
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
                    ChainingUserRegistrySynchronizer.logger.info("Removing '"
                            + this.authorityService.getShortName(child) + "' from group '"
                            + this.authorityService.getShortName(groupName) + "'");
                    this.authorityService.removeAuthority(groupName, child);
                }
            }
            else
            {
                String groupShortName = this.authorityService.getShortName(groupName);
                Set<String> groupZones = this.authorityService.getAuthorityZones(groupName);
                if (groupZones != null)
                {
                    groupZones.retainAll(visitedZoneIds);
                    if (groupZones.size() > 0)
                    {
                        // A group that exists in a different zone with higher precedence
                        continue;
                    }
                    // The group existed, but in a zone with lower precedence
                    ChainingUserRegistrySynchronizer.logger
                            .warn("Recreating occluded group '"
                                    + groupShortName
                                    + "'. This group was previously created manually or through synchronization with a lower priority user registry.");
                    this.authorityService.deleteAuthority(groupName);
                }
                else
                {
                    ChainingUserRegistrySynchronizer.logger.info("Creating group '" + groupShortName + "'");
                }

                // create the group
                this.authorityService.createAuthority(AuthorityType.getAuthorityType(groupName), groupShortName,
                        (String) groupProperties.get(ContentModel.PROP_AUTHORITY_DISPLAY_NAME), getZones(zoneId));
                Set<String> children = group.getChildAssociations();
                if (!children.isEmpty())
                {
                    groupAssocsToCreate.put(groupName, children);
                }
            }

            // Increment the count of processed groups
            processedCount++;

            // Maintain the last modified date
            Date groupLastModified = group.getLastModified();
            if (groupLastModified != null)
            {
                lastModifiedMillis = Math.max(lastModifiedMillis, groupLastModified.getTime());
            }
        }

        // Add the new associations, now that we have created everything
        for (Map.Entry<String, Set<String>> entry : groupAssocsToCreate.entrySet())
        {
            for (String child : entry.getValue())
            {
                String groupName = entry.getKey();
                ChainingUserRegistrySynchronizer.logger.info("Adding '" + this.authorityService.getShortName(child)
                        + "' to group '" + this.authorityService.getShortName(groupName) + "'");
                this.authorityService.addAuthority(groupName, child);
            }

        }

        // Delete groups if we have complete information for the zone
        if (force && !groupsToDelete.isEmpty())
        {
            for (String group : groupsToDelete)
            {
                ChainingUserRegistrySynchronizer.logger.warn("Deleting group '"
                        + this.authorityService.getShortName(group) + "'");
                this.authorityService.deleteAuthority(group);
                processedCount++;
            }
        }

        if (lastModifiedMillis != -1)
        {
            setMostRecentUpdateTime(ChainingUserRegistrySynchronizer.GROUP_LAST_MODIFIED_ATTRIBUTE, zoneId,
                    lastModifiedMillis);
        }

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
        HashSet<String> zones = new HashSet<String>(2, 1.0f);
        zones.add(AuthorityService.ZONE_APP_DEFAULT);
        zones.add(zoneId);
        return zones;
    }
}
