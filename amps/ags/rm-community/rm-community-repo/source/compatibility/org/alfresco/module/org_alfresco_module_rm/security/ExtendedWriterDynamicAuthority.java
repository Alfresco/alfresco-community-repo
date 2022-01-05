/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
@Deprecated
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
    		requiredFor = new HashSet<>(3);
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
