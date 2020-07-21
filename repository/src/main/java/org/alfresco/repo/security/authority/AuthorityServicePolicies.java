/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.repo.security.authority;

import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Policies for AuthorityService
 * 
 * @author cpopa
 *
 */
public interface AuthorityServicePolicies
{ 
    /**
     * Policy invoked when an authority is added to a group
     */
    public interface OnAuthorityAddedToGroup extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onAuthorityAddedToGroup");
        
        /**
         * An authority is added in a group
         * 
         * @param parentGroup the group into which the authority is added
         * @param childAuthority the authority being added to the groups
         */
        public void onAuthorityAddedToGroup(String parentGroup, String childAuthority);
    }
    
    /**
     * Policy invoked when an authority is removed from a group
     */
    public interface OnAuthorityRemovedFromGroup extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onAuthorityRemovedFromGroup");
        
        /**
         * An authority was removed from a group
         * 
         * @param parentGroup the group from which the authority is removed
         * @param childAuthority the authority being removed from the group
         */
        public void onAuthorityRemovedFromGroup(String parentGroup, String childAuthority);
    }
    
    /**
     * Policy invoked when a group is deleted
     */
    public interface OnGroupDeleted extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onGroupDeleted");
        
        /**
         * A group has been deleted
         * 
         * @param groupName the group being deleted
         * @param cascade whether the deletion is cascaded to child authorities
         */
        public void onGroupDeleted(String groupName, boolean cascade);
    }
}
