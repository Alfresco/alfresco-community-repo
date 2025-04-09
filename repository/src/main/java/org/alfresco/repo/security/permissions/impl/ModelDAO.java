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
package org.alfresco.repo.security.permissions.impl;

import java.util.Set;

import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * The API for the alfresco permission model.
 * 
 * @author Andy Hind
 */
public interface ModelDAO
{

    /**
     * Get the permissions that can be set for the given type.
     * 
     * @param type
     *            - the type in the data dictionary.
     */
    public Set<PermissionReference> getAllPermissions(QName type);

    /**
     * Get the permissions that can be set for the given type.
     * 
     * @param type
     *            - the type in the data dictionary.
     */
    public Set<PermissionReference> getAllPermissions(QName type, Set<QName> aspects);

    /**
     * Get the permissions that can be set for the given node. This is determined by the node type.
     * 
     * @param nodeRef
     *            NodeRef
     */
    public Set<PermissionReference> getAllPermissions(NodeRef nodeRef);

    /**
     * Get the permissions that are exposed to be set for the given type.
     * 
     * @param type
     *            - the type in the data dictionary.
     */
    public Set<PermissionReference> getExposedPermissions(QName type);

    /**
     * Get the permissions that are exposed to be set for the given node. This is determined by the node type.
     * 
     * @param nodeRef
     *            NodeRef
     */
    public Set<PermissionReference> getExposedPermissions(NodeRef nodeRef);

    /**
     * Get all the permissions that grant this permission.
     * 
     * @param perm
     *            PermissionReference
     */
    public Set<PermissionReference> getGrantingPermissions(PermissionReference perm);

    /**
     * Get the permissions that must also be present on the node for the required permission to apply.
     * 
     * @param required
     *            PermissionReference
     * @param qName
     *            QName
     * @param on
     *            RequiredPermission.On
     */
    public Set<PermissionReference> getRequiredPermissions(PermissionReference required, QName qName, Set<QName> aspectQNames, RequiredPermission.On on);

    public Set<PermissionReference> getUnconditionalRequiredPermissions(PermissionReference required, RequiredPermission.On on);

    /**
     * Get the permissions which are granted by the supplied permission.
     * 
     * @param permissionReference
     *            PermissionReference
     */
    public Set<PermissionReference> getGranteePermissions(PermissionReference permissionReference);

    /**
     * Get the permissions which are granted by the supplied permission.
     * 
     * @param permissionReference
     *            PermissionReference
     */
    public Set<PermissionReference> getImmediateGranteePermissions(PermissionReference permissionReference);

    /**
     * Is this permission refernece to a permission and not a permissoinSet?
     * 
     * @param required
     *            PermissionReference
     * @return boolean
     */
    public boolean checkPermission(PermissionReference required);

    /**
     * Does the permission reference have a unique name?
     * 
     * @param permissionReference
     *            PermissionReference
     * @return boolean
     */
    public boolean isUnique(PermissionReference permissionReference);

    /**
     * Find a permission by name in the type context. If the context is null and the permission name is unique it will be found.
     * 
     * @param qname
     *            QName
     * @param permissionName
     *            String
     * @return PermissionReference
     */
    public PermissionReference getPermissionReference(QName qname, String permissionName);

    /**
     * Get the global permissions for the model. Permissions that apply to all nodes and take precedence over node specific permissions.
     * 
     * @return Set
     */
    public Set<? extends PermissionEntry> getGlobalPermissionEntries();

    /**
     * Get all exposed permissions (regardless of type exposure)
     */
    public Set<PermissionReference> getAllExposedPermissions();

    /**
     * Get all exposed permissions (regardless of type exposure)
     */
    public Set<PermissionReference> getAllPermissions();

    /**
     * Does this permission allow full control?
     * 
     * @param permissionReference
     *            PermissionReference
     * @return boolean
     */
    public boolean hasFull(PermissionReference permissionReference);

}
