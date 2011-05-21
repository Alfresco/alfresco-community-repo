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
package org.alfresco.repo.domain.permissions;

import java.util.List;

import org.alfresco.repo.security.permissions.ACLCopyMode;
import org.alfresco.repo.security.permissions.AccessControlEntry;
import org.alfresco.repo.security.permissions.AccessControlList;
import org.alfresco.repo.security.permissions.AccessControlListProperties;
import org.alfresco.repo.security.permissions.impl.AclChange;

/**
 * Provides data access support for persistence in <b>alf_access_control_list</b>.
 * 
 * @since 3.4
 * @author Andy Hind, janv
 */
public interface AclDAO
{
    /**
     * Get an ACL (including entries)
     */
    public AccessControlList getAccessControlList(Long id);
    
    /**
     * Get an ACL
     */
    public Acl getAcl(Long id);
    
    /**
     * Get the ACL properties
     * 
     * @return - the id of all ACLs affected
     */
    public AccessControlListProperties getAccessControlListProperties(Long id);
    
    /**
     * Delete an ACL
     * 
     * @return - the id of all ACLs affected
     */
    public List<AclChange> deleteAccessControlList(Long id);
    
    /**
     * Delete the ACEs in position 0 (those set directly on the ACL and not inherited) Cleans up existing acls
     * 
     * @return - the id of all ACLs affected
     */
    public List<AclChange> deleteLocalAccessControlEntries(Long id);
    
    /**
     * Delete the ACEs in position > 0 (those not set directly on the ACL but inherited) No affect on any other acl
     * 
     * @return - the id of all ACLs affected
     */
    public List<AclChange> deleteInheritedAccessControlEntries(Long id);
    
    /**
     * Delete all ACEs that reference this authority as no longer valid. THIS DOES NOT CAUSE ANY ACL TO VERSION
     * Used when deleting a user. No ACL is updated - the user has gone the aces and all related info is deleted.
     * 
     * @return - the id of all ACLs affected
     */
    public List<AclChange> deleteAccessControlEntries(String authority);
    
    /**
     * Delete some locally set ACLs according to the pattern
     * 
     * @param pattern -
     *            non null elements are used for the match
     * @return - the id of all ACLs affected
     */
    public List<AclChange> deleteAccessControlEntries(Long id, AccessControlEntry pattern);
    
    /**
     * Add an access control entry
     */
    public List<AclChange> setAccessControlEntry(Long id, AccessControlEntry ace);
    
    /**
     * Enable inheritance
     */
    public List<AclChange> enableInheritance(Long id, Long parent);
    
    /**
     * Disable inheritance
     */
    public List<AclChange> disableInheritance(Long id, boolean setInheritedOnAcl);
    
    /**
     * Create a new ACL with default properties
     * 
     * @see #getDefaultProperties()
     * @see #createAccessControlList(AccessControlListProperties)
     */
    public Long createAccessControlList();
    
    /**
     * Get the default ACL properties
     * 
     * @return the default properties
     */
    public AccessControlListProperties getDefaultProperties();
    
    /**
     * Create a new ACL with the given properties. Unset properties are assigned defaults.
     * 
     * @return Acl
     */
    public Acl createAccessControlList(AccessControlListProperties properties);
    
    /**
     * @see #createAccessControlList(AccessControlListProperties)
     * @return Acl
     */
    public Acl createAccessControlList(AccessControlListProperties properties, List<AccessControlEntry> aces, Long inherited);
    
    /**
     * Get the id of the ACL inherited from the one given
     * May return null if there is nothing to inherit -> OLD world where nodes have their own ACL and we walk the parent chain
     */
    public Long getInheritedAccessControlList(Long id);
    
    /**
     * Merge inherited ACEs in to target - the merged ACEs will go in at their current position +1
     */
    public List<AclChange> mergeInheritedAccessControlList(Long inherited, Long target);
    
    public Acl getAclCopy(Long toCopy, Long toInheritFrom, ACLCopyMode mode);
    
    public List<Long> getAVMNodesByAcl(long aclEntityId, int maxResults);
    
    public List<Long> getADMNodesByAcl(long aclEntityId, int maxResults);
    
    public Acl createLayeredAcl(Long indirectedAcl);
    
    public void renameAuthority(String before, String after);
    
    public void deleteAclForNode(long aclId, boolean isAVMNode);

    /**
     * @param inheritedAclId
     * @param aclId
     */
    public void fixSharedAcl(Long shared, Long defining);
}
