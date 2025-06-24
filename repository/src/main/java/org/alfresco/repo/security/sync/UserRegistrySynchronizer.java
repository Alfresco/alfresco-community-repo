/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.security.sync;

import java.util.Set;

import org.alfresco.service.namespace.QName;

/**
 * A <code>UserRegistrySynchronizer</code> is responsible for synchronizing Alfresco's local user (person) and group (authority) information with one or more external sources (most typically LDAP directories).
 * 
 * @author dward
 */
public interface UserRegistrySynchronizer
{
    /**
     * Creates a person object for a successfully authenticated user who does not yet have a person object, if allowed to by configuration. Depending on configuration, may trigger a partial synchronize and/or create a new person with default settings.
     * 
     * @param username
     *            the user name
     * @return true, if a person is created
     */
    public boolean createMissingPerson(String username);

    /**
     * Retrieves timestamped user and group information from configured external sources and compares it with the local users and groups last retrieved from the same sources. Any updates and additions made to those users and groups are applied to the local Alfresco copies. This process is always run in different transactions and threads.
     *
     * @param forceUpdate
     *            Should the complete set of users and groups be updated / created locally or just those known to have changed since the last sync? When <code>true</code> then <i>all</i> users and groups are queried from the user registry and updated locally. When <code>false</code> then each source is only queried for those users and groups modified since the most recent modification date of all the objects last queried from that same source.
     * @param isFullSync
     *            Should a complete set of user and group IDs be queried from the user registries in order to determine deletions? This parameter is independent of <code>force</code> as a separate query is run to process updates.
     */
    public void synchronize(boolean forceUpdate, boolean isFullSync);

    /**
     * Gets the set of property names that are auto-mapped for the user with the given user name. These should remain read-only for the user in the UI.
     * 
     * @return the person mapped properties
     */
    public Set<QName> getPersonMappedProperties(String username);

}
