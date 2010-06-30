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

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.alfresco.service.namespace.QName;

/**
 * A <code>UserRegistry</code> is an encapsulation of an external registry from which user and group information can be
 * queried (typically an LDAP directory). Implementations may optional support the ability to query only those users and
 * groups modified since a certain time.
 * 
 * @author dward
 */
public interface UserRegistry
{
    /**
     * Gets descriptions of all the persons (users) in the user registry or all those changed since a certain date.
     * 
     * @param modifiedSince
     *            if non-null, then only descriptions of users modified since this date should be returned; if
     *            <code>null</code> then descriptions of all users should be returned.
     * @return a {@link Collection} of {@link NodeDescription}s of all the persons (users) in the user registry or all
     *         those changed since a certain date. The description properties should correspond to those of an Alfresco
     *         person node.
     */
    public Collection<NodeDescription> getPersons(Date modifiedSince);

    /**
     * Gets descriptions of all the groups in the user registry or all those changed since a certain date.
     * 
     * @param modifiedSince
     *            if non-null, then only descriptions of groups modified since this date should be returned; if
     *            <code>null</code> then descriptions of all groups should be returned.
     * @return a {@link Collection} of {@link NodeDescription}s of all the groups in the user registry or all those
     *         changed since a certain date. The description properties should correspond to those of an Alfresco
     *         authority node.
     */
    public Collection<NodeDescription> getGroups(Date modifiedSince);

    /**
     * Gets the names of all persons in the registry. Used to detect local persons to be deleted. Note that the
     * treatment of these names will depend on Alfresco's username case-sensitivity setting.
     * 
     * @return the person names
     */
    public Collection<String> getPersonNames();

    /**
     * Gets the names of all groups in the registry. Used to detect local groups to be deleted.
     * 
     * @return the person names
     */
    public Collection<String> getGroupNames();

    /**
     * Gets the set of property names that are auto-mapped by this user registry. These should remain read-only for this
     * registry's users in the UI.
     * 
     * @return the person mapped properties
     */
    public Set<QName> getPersonMappedProperties();
}
