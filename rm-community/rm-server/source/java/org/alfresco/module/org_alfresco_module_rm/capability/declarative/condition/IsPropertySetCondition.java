/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;

/**
 * Indicates whether a property is set or not.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class IsPropertySetCondition extends AbstractCapabilityCondition
{
    /** property name (eg: rma:location) */
    private String propertyName;
    private QName propertyQName;
    
    /** namespace service */
    private NamespaceService namespaceService;
    
    /**
     * @param propertyName  property name (eg: rma:location)
     */
    public void setPropertyName(String propertyName)
    {
        this.propertyName = propertyName;
    }
    
    /**
     * @param namespaceService  namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * @return QName    property qname
     */
    protected QName getPropertyQName()
    {
    	if (propertyQName == null)
    	{
    		propertyQName = QName.createQName(propertyName, namespaceService);
    	}
    	return propertyQName;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.CapabilityCondition#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean evaluateImpl(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        boolean result = false;
        
        if (nodeService.getProperty(nodeRef, getPropertyQName()) != null)
        {
            result = true;
        }
                
        return result;
    }

}
