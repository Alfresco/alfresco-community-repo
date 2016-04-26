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
     * @param type - the type in the data dictionary.
     * @return Set<PermissionReference>
     */
    public Set<PermissionReference> getAllPermissions(QName type);
    
    /**
     * Get the permissions that can be set for the given type.
     * 
     * @param type - the type in the data dictionary.
     * @param aspects Set<QName>
     * @return Set<PermissionReference>
     */
    public Set<PermissionReference> getAllPermissions(QName type, Set<QName> aspects);

    /**
     * Get the permissions that can be set for the given node. 
     * This is determined by the node type.
     * 
     * @param nodeRef NodeRef
     * @return Set<PermissionReference>
     */
    public Set<PermissionReference> getAllPermissions(NodeRef nodeRef);
    
    /**
     *Get the permissions that are exposed to be set for the given type.
     * 
     * @param type - the type in the data dictionary.
     * @return Set<PermissionReference>
     */
    public Set<PermissionReference> getExposedPermissions(QName type);

    /**
     * Get the permissions that are exposed to be set for the given node. 
     * This is determined by the node type.
     * 
     * @param nodeRef NodeRef
     * @return Set<PermissionReference>
     */
    public Set<PermissionReference> getExposedPermissions(NodeRef nodeRef);

    /**
     * Get all the permissions that grant this permission.
     * 
     * @param perm PermissionReference
     * @return Set<PermissionReference>
     */
    public Set<PermissionReference> getGrantingPermissions(PermissionReference perm);

    /**
     * Get the permissions that must also be present on the node for the required permission to apply.
     *  
     * @param required PermissionReference
     * @param qName QName
     * @param aspectQNames Set<QName>
     * @param on RequiredPermission.On
     * @return Set<PermissionReference>
     */
    public Set<PermissionReference> getRequiredPermissions(PermissionReference required, QName qName, Set<QName> aspectQNames, RequiredPermission.On on);

    public Set<PermissionReference> getUnconditionalRequiredPermissions(PermissionReference required, RequiredPermission.On on);
    
    /**
     * Get the permissions which are granted by the supplied permission.
     * 
     * @param permissionReference PermissionReference
     * @return Set<PermissionReference>
     */
    public Set<PermissionReference> getGranteePermissions(PermissionReference permissionReference);
    
    /**
     * Get the permissions which are granted by the supplied permission.
     * 
     * @param permissionReference PermissionReference
     * @return Set<PermissionReference>
     */
    public Set<PermissionReference> getImmediateGranteePermissions(PermissionReference permissionReference);

    /**
     * Is this permission refernece to a permission and not a permissoinSet?
     * 
     * @param required PermissionReference
     * @return boolean
     */
    public boolean checkPermission(PermissionReference required);

    /**
     * Does the permission reference have a unique name?
     * 
     * @param permissionReference PermissionReference
     * @return boolean
     */
    public boolean isUnique(PermissionReference permissionReference);

    /**
     * Find a permission by name in the type context.
     * If the context is null and the permission name is unique it will be found.
     * 
     * @param qname QName
     * @param permissionName String
     * @return PermissionReference
     */
    public PermissionReference getPermissionReference(QName qname, String permissionName);
    
    /**
     * Get the global permissions for the model.
     * Permissions that apply to all nodes and take precedence over node specific permissions.
     * 
     * @return Set
     */
    public Set<? extends PermissionEntry> getGlobalPermissionEntries();


    /**
     * Get all exposed permissions (regardless of type exposure)
     * @return Set<PermissionReference>
     */
    public Set<PermissionReference> getAllExposedPermissions();
    
    /**
     * Get all exposed permissions (regardless of type exposure)
     * @return Set<PermissionReference>
     */
    public Set<PermissionReference> getAllPermissions();
    
    /**
     * Does this permission allow full control?
     * @param permissionReference PermissionReference
     * @return boolean
     */
    public boolean hasFull(PermissionReference permissionReference);

}
