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

import java.util.Collection;
import java.util.Date;
import java.util.Set;

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
     * Gets descriptions of all the groups in the user registry or all those changed since a certain date. Group
     * associations should be restricted to those in the given set of known authorities. Optionally this set is 'pruned'
     * to contain only those authorities that no longer exist in the user registry, i.e. the deletion candidates.
     * 
     * @param modifiedSince
     *            if non-null, then only descriptions of groups modified since this date should be returned; if
     *            <code>null</code> then descriptions of all groups should be returned.
     * @param knownAuthorities
     *            the current set of known authorities
     * @param prune
     *            should this set be 'pruned' so that it contains only those authorities that do not exist in the
     *            registry, i.e. the deletion candidates?
     * @return a {@link Collection} of {@link NodeDescription}s of all the groups in the user registry or all those
     *         changed since a certain date. The description properties should correspond to those of an Alfresco
     *         authority node.
     */
    public Collection<NodeDescription> getGroups(Date modifiedSince, Set<String> knownAuthorities, boolean prune);

}
