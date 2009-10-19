/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.security.permissions.impl;

import java.util.List;
import java.util.Set;

import org.alfresco.repo.domain.DbAccessControlList;
import org.alfresco.repo.domain.hibernate.AclDaoComponentImpl.Indirection;
import org.alfresco.repo.security.permissions.ACLCopyMode;
import org.alfresco.repo.security.permissions.AccessControlEntry;
import org.alfresco.repo.security.permissions.AccessControlList;
import org.alfresco.repo.security.permissions.AccessControlListProperties;
import org.alfresco.repo.transaction.TransactionalDao;

/**
 * DAO component for creating, deleting, manipulating and finding ACLs and associated ACEs and anc ACE context.
 * 
 * @author andyh
 */
public interface AclDaoComponent extends TransactionalDao
{
    /**
     * Temp support to get a DBAccessControlList to wire up ...
     * 
     * @param id
     * @return
     */
    DbAccessControlList getDbAccessControlList(Long id);
    
    
    /**
     * Get an ACL id.
     * 
     * @param id
     * @return
     */
    public AccessControlList getAccessControlList(Long id);

    /**
     * Delete an ACL
     * 
     * @param id
     * @return - the id of all ACLs affected
     */
    public List<AclChange> deleteAccessControlList(Long id);

    /**
     * Delete the ACEs in position 0 (those set directly on the ACL and not inherited) Cleans up existing acls
     * 
     * @param id
     * @return - the id of all ACLs affected
     */
    public List<AclChange> deleteLocalAccessControlEntries(Long id);

    /**
     * Delete the ACEs in position > 0 (those not set directly on the ACL but inherited) No affect on any other acl
     * 
     * @param id
     * @return - the id of all ACLs affected
     */
    public List<AclChange> deleteInheritedAccessControlEntries(Long id);

    /**
     * Mark all ACEs that reference this authority as no longer valid - the authority has been deleted
     * 
     * @param authority
     * @return - the id of all ACLs affected
     */
    public List<AclChange> invalidateAccessControlEntries(String authority);

    /**
     * Delete all ACEs that reference this authority as no longer valid. THIS DOES NOT CAUSE ANY ACL TO VERSION
     * 
     * @param authority
     * @return - the id of all ACLs affected
     */
    public List<AclChange> deleteAccessControlEntries(String authority);

    /**
     * Delete some locally set ACLs according to the pattern
     * 
     * @param id
     * @param pattern -
     *            non null elements are used for the match
     * @return - the id of all ACLs affected
     */
    public List<AclChange> deleteAccessControlEntries(Long id, AccessControlEntry pattern);

    /**
     * Add an access control entry
     * 
     * @param id
     * @param ace
     * @return - the id of all ACLs affected
     */
    public List<AclChange> setAccessControlEntry(Long id, AccessControlEntry ace);

    /**
     * Enable inheritance
     * 
     * @param id
     * @param parent
     * @return
     */
    public List<AclChange> enableInheritance(Long id, Long parent);

    /**
     * Disable inheritance
     * 
     * @param id
     * @param setInheritedOnAcl
     * @return
     */
    public List<AclChange> disableInheritance(Long id, boolean setInheritedOnAcl);

    /**
     * Get the ACL properties
     * 
     * @param id
     * @return - the id of all ACLs affected
     */
    public AccessControlListProperties getAccessControlListProperties(Long id);

    /**
     * Create a bew ACL with teh given properties. Unset ones are assigned defaults.
     * 
     * @param properties
     * @return
     */
    public Long createAccessControlList(AccessControlListProperties properties);

    /**
     * Get the id of the ACL inherited from the one given
     * May return null if there is nothing to inherit -> OLD world where nodes have thier own ACL and we wlak the parent chain
     * 
     * @param id
     * @return
     */
    public Long getInheritedAccessControlList(Long id);

    /**
     * Merge inherited ACEs in to target - the merged ACEs will go in at thier current position +1
     * 
     * @param inherited
     * @param target
     * @return
     */
    public List<AclChange> mergeInheritedAccessControlList(Long inherited, Long target);
    
    public DbAccessControlList getDbAccessControlListCopy(Long toCopy, Long toInheritFrom, ACLCopyMode mode);
    
    public Long getCopy(Long toCopy, Long toInheritFrom, ACLCopyMode mode);
    
    public List<Long> getAvmNodesByACL(Long id);
    
    public List<Indirection> getAvmIndirections();
    
    /**
     * hibernate lifecycle support
     * @param id
     */
    public void onDeleteAccessControlList(final long id);
    
    public void updateAuthority(String before, String after);
    
    public void createAuthority(String authority);
}
