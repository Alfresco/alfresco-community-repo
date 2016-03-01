 
package org.alfresco.module.org_alfresco_module_rm.security;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * File plan permission service.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public interface FilePlanPermissionService
{
    /**
     * Setup permissions for a record category
     * 
     * @param nodeRef   record category node reference
     */
    void setupRecordCategoryPermissions(NodeRef recordCategory);
    
    /**
     * Setup permissions for an object within a given parent.
     * 
     * @param parent    parent node to inherit permissions from
     * @param nodeRef   node ref to setup permissions on 
     */
    void setupPermissions(NodeRef parent, NodeRef nodeRef);
    
    /**
     * Sets a permission on a file plan object.  Assumes allow is true.  Cascades permission down to record folder.  
     * Cascades ReadRecord up to file plan.
     * 
     * @param nodeRef       node reference
     * @param authority     authority 
     * @param permission    permission
     */
    void setPermission(NodeRef nodeRef, String authority, String permission);
    
    /**
     * Deletes a permission from a file plan object.  Cascades removal down to record folder.
     * 
     * @param nodeRef       node reference
     * @param authority     authority 
     * @param permission    permission
     */
    void deletePermission(NodeRef nodeRef, String authority, String permission);

}
