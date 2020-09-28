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
package org.alfresco.repo.security.permissions;

import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Policies for PermissionService 
 * 
 * @author cpopa
 *
 */
public interface PermissionServicePolicies
{    
    /**
     * Policy invoked when a permission is granted to an authority for a specific node
     */
    public interface OnGrantLocalPermission extends ClassPolicy
    {        
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onGrantLocalPermission");

        /**
         * A permission was granted to an authority for a specific node
         * 
         * @param nodeRef the node on which the permission is granted
         * @param authority the authority being granted the permission
         * @param permission the permission at question
         */
        public void onGrantLocalPermission(NodeRef nodeRef, String authority, String permission);
    }
    
    /**
     * Policy invoked when a permission is revoked from an authority for a specific node
     */
    public interface OnRevokeLocalPermission extends ClassPolicy
    {        
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onRevokeLocalPermission");

        /**
         * A permission was revoked from an authority for a specific node
         * 
         * @param nodeRef the node from which the permission is revoked
         * @param authority the authority being revoked the permission
         * @param permission the permission at question
         */
        public void onRevokeLocalPermission(NodeRef nodeRef, String authority, String permission);
    }

    /**
     * Policy invoked when permission inheritance is enabled for a specific node
     */
    public interface OnInheritPermissionsEnabled extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onInheritPermissionsEnabled");

        /**
         * Permission inheritance was enabled
         * 
         * @param nodeRef the node for which the inheritance is enabled
         */
        public void onInheritPermissionsEnabled(NodeRef nodeRef);

    }
    
    /**
     * Policy invoked when permission inheritance is disabled for a specific node (sync or async mode)
     */
    public interface OnInheritPermissionsDisabled extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onInheritPermissionsDisabled");

        /**
         * Permission inheritance was disabled
         * 
         * @param nodeRef the node for which the inheritance is disabled
         * @param async whether the operation has been done in asynchronous mode, thus it may not be finished yet
         */
        public void onInheritPermissionsDisabled(NodeRef nodeRef, boolean async);
    }
}
