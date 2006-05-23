/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.service.cmr.security;

import java.util.Set;

/**
 * The service that encapsulates authorities granted to users.
 * 
 * This service will refuse to create any user authorities. These should be
 * managed using the AuthenticationService and PersonServce. Methods that try to
 * change alter users will throw an exception.
 * 
 * A string key is used to identify the authority. These follow the contract
 * defined in AuthorityType. If there are entities linked to these authorities
 * this key should be used to find them, as userName is used link user and
 * person.
 * 
 * @author Andy Hind
 */
public interface AuthorityService
{
    /**
     * Check of the current user has admin authority.
     * 
     * There is no contract for who should have this authority, only that it can
     * be tested here. It could be determined by group membership, role,
     * authentication mechanism, ...
     * 
     * @return true if the currently authenticated user has the admin authority
     */
    public boolean hasAdminAuthority();

    /**
     * Get the authorities for the current user
     * 
     * @return
     */
    public Set<String> getAuthorities();

    /**
     * Get all authorities by type.
     * 
     * @param type -
     *            the type of authorities.
     * @return
     */
    public Set<String> getAllAuthorities(AuthorityType type);

    /**
     * Get all root authorities by type. Root authorities are ones that were
     * created without an authority as the parent authority;
     * 
     * @param type -
     *            the type of the authority
     * @return
     */

    public Set<String> getAllRootAuthorities(AuthorityType type);

    /**
     * Create an authority. If the parent is null thisw method creates a root
     * authority.
     * 
     * @param type -
     *            the type of the authority
     * @param parentName -
     *            the name of the parent authority. If this is null then a root
     *            authority is created.
     * @param shortName -
     *            the short name of the authority to create
     * 
     * @return the name of the authority (this will be the prefix, if any
     *         associated with the type appended with the short name)
     */
    public String createAuthority(AuthorityType type, String parentName, String shortName);

    /**
     * Set an authority to include another authority. For example, adding a
     * group to a group or adding a user to a group.
     * 
     * @param parentName -
     *            the string identifier for the parent.
     * @param childName -
     *            the string identifier for the child.
     */
    public void addAuthority(String parentName, String childName);

    /**
     * Remove an authority as a member of another authority. The child authority
     * will still exist. If the child authority was not created as a root
     * authority and you remove its creation link, it will be moved to a root
     * authority. If you want rid of it, use delete.
     * 
     * @param parentName -
     *            the string identifier for the parent.
     * @param childName -
     *            the string identifier for the child.
     */
    public void removeAuthority(String parentName, String childName);

    /**
     * Delete an authority and all its relationships.
     * 
     * @param name
     */
    public void deleteAuthority(String name);

    /**
     * Get all the authorities that are contained by the given authority.
     * 
     * For a group you could get all the authorities it contains, just the users
     * it contains or just the other groups it includes.
     * 
     * @param type -
     *            if not null, limit to the type of authority specified
     * @param name -
     *            the name of the containing authority
     * @param immediate -
     *            if true, limit the depth to just immediate child, if false
     *            find authorities at any depth
     * @return
     */
    public Set<String> getContainedAuthorities(AuthorityType type, String name, boolean immediate);

    /**
     * Get the authorities that contain the given authority
     * 
     * For example, this can be used find out all the authorities that contain a
     * user.
     * 
     * @param type -
     *            if not null, limit to the type of authority specified
     * @param name -
     *            the name of the authority for which the containing authorities
     *            are required.
     * @param immediate -
     *            limit to immediate parents or any ancestor.
     * @return
     */
    public Set<String> getContainingAuthorities(AuthorityType type, String name, boolean immediate);

    /**
     * Extract the short name of an authority from its full identifier.
     * 
     * @param name
     * @return
     */
    public String getShortName(String name);

    /**
     * Create the full identifier for an authority given its short name and
     * type.
     * 
     * @param type
     * @param shortName
     * @return
     */
    public String getName(AuthorityType type, String shortName);
    
    /**
     * Check if an authority exists.
     * 
     * @param name (the long name). 
     * @return
     */
    public boolean authorityExists(String name);

}
