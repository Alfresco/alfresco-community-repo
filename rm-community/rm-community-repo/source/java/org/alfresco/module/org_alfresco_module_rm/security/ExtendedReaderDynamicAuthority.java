 
package org.alfresco.module.org_alfresco_module_rm.security;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Extended readers dynamic authority implementation.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class ExtendedReaderDynamicAuthority extends ExtendedSecurityBaseDynamicAuthority
{
    /** Extended reader role */
    public static final String EXTENDED_READER = "ROLE_EXTENDED_READER";
    
    /**
     * @see org.alfresco.repo.security.permissions.DynamicAuthority#getAuthority()
     */
    @Override
    public String getAuthority()
    {
        return EXTENDED_READER;
    }    

    /**
     * @see org.alfresco.repo.security.permissions.DynamicAuthority#requiredFor()
     */
    @Override
    public Set<PermissionReference> requiredFor()
    {
    	if (requiredFor == null)
    	{
    		requiredFor = Collections.singleton(getModelDAO().getPermissionReference(null, RMPermissionModel.READ_RECORDS));
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
        
        Map<String, Integer> readerMap = (Map<String, Integer>)getNodeService().getProperty(nodeRef, PROP_READERS);
        if (readerMap != null)
        {
            result = readerMap.keySet();
        }
        
        return result;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityBaseDynamicAuthority#getTransactionCacheName()
     */
    @Override
    protected String getTransactionCacheName() 
    {
    	return "rm.extendedreaderdynamicauthority";
    }
}
