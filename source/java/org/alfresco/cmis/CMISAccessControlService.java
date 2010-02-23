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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.cmis;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service for CMIS access control support.
 * 
 * @author andyh
 */
public interface CMISAccessControlService
{
    /**
     * CMIS Read (properties and content)               
     */
    public static final String CMIS_READ_PERMISSION = "cmis:read";
    
    /**
     * CMIS Write (properties and content)
     */
    public static final String CMIS_WRITE_PERMISSION = "cmis:write";
    
    /**
     * CMIS ALL permissions (includes all permissions defined within the repository)
     */
    public static final String CMIS_ALL_PERMISSION = "cmis:all";
    
    /**
     * Get the ACL capability enum.
     * @return the ACL capability enum.
     */
    public CMISAclCapabilityEnum getAclCapability();
    
    /**
     * Gets the supported permission types
     * @return the supported permission types
     */
    public CMISAclSupportedPermissionEnum getSupportedPermissions();

    /**
     * Get the ACL propagation enum.
     * @return the ACL propagation enum.
     */
    public CMISAclPropagationEnum getAclPropagation();
    
    /**
     * Get all the permissions defined by the repository.
     * @return a list of permissions
     */
    public List<CMISPermissionDefinition> getRepositoryPermissions();
    
    /**
     * Get the list of permission mappings.
     * @return get the permission mapping as defined by the CMIS specification.
     */
    public List<? extends CMISPermissionMapping> getPermissionMappings();
    
    /**
     * Gets the name of the principal who is used for anonymous access. This principal can then be passed to the ACL
     * services to specify what permissions anonymous users should have.
     * 
     * @return name of the principal who is used for anonymous access
     */
    public String getPrincipalAnonymous();

    /**
     * Gets the name of the principal who is used to indicate any authenticated user. This principal can then be passed
     * to the ACL services to specify what permissions any authenticated user should have.
     * 
     * @return name of the principal who is used to indicate any authenticated user
     */
    public String getPrincipalAnyone();

    /**
     * Get the ACLs set on a node.
     * @param nodeRef
     * @param format 
     * @return an access control report
     */
    public CMISAccessControlReport getAcl(NodeRef nodeRef, CMISAccessControlFormatEnum format);
    
    /**
     * Update the ACEs on a node.
     * Those already existing, are preserved, those new are added and those missing are removed.
     * 
     * @param nodeRef
     * @param acesToApply
     * @return an access control report of the final state
     */
    public CMISAccessControlReport applyAcl(NodeRef nodeRef, List<CMISAccessControlEntry> acesToApply);

    /**
     * Update the ACEs on a node.
     * The deletions are applied before the additions.
     * Existing ACEs not deleted or added remain unchanged.
     * 
     * @param nodeRef
     * @param acesToRemove
     * @param acesToAdd
     * @param propagation
     * @param format 
     * @return an access control report of the final state
     * @exception UnsupportedCMISAclPropagationMode will be thrown for unsupported propagation modes. 
     */
    public CMISAccessControlReport applyAcl(NodeRef nodeRef, List<CMISAccessControlEntry> acesToRemove, List<CMISAccessControlEntry> acesToAdd, CMISAclPropagationEnum propagation, CMISAccessControlFormatEnum format);
}
