 
package org.alfresco.module.org_alfresco_module_rm.security;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Extended writers dynamic authority implementation.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class ExtendedWriterDynamicAuthority extends ExtendedSecurityBaseDynamicAuthority
{
    /** Extended writer role */
    public static final String EXTENDED_WRITER = "ROLE_EXTENDED_WRITER";
    
    /**
     * @see org.alfresco.repo.security.permissions.DynamicAuthority#getAuthority()
     */
    @Override
    public String getAuthority()
    {
        return EXTENDED_WRITER;
    }
    
    /**
     * @see org.alfresco.repo.security.permissions.DynamicAuthority#requiredFor()
     */
    @Override
    public Set<PermissionReference> requiredFor()
    {
    	if (requiredFor == null)
    	{
    		requiredFor = new HashSet<PermissionReference>(3);
    		Collections.addAll(requiredFor, 
    						   getModelDAO().getPermissionReference(null, RMPermissionModel.READ_RECORDS),
    				           getModelDAO().getPermissionReference(null, RMPermissionModel.FILING), 
    				           getModelDAO().getPermissionReference(null, RMPermissionModel.FILE_RECORDS));
    	}
    	
    	return requiredFor;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityBaseDynamicAuthority#getAuthorites(org.alfresco.service.cmr.repository.NodeRef)
     */
    @SuppressWarnings("unchecked")
	protected Set<String> getAuthorites(NodeRef nodeRef) 
    {
    	Set<String> result = null;
        
        Map<String, Integer> map = (Map<String, Integer>)getNodeService().getProperty(nodeRef, PROP_WRITERS);
        if (map != null)
        {
            result = map.keySet();
        }
        
        return result;
    }  
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityBaseDynamicAuthority#getTransactionCacheName()
     */
    @Override
    protected String getTransactionCacheName() 
    {
    	return "rm.extendedwriterdynamicauthority";
    }  
}
