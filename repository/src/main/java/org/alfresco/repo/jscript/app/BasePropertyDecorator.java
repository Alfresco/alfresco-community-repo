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
/**
 * 
 */
package org.alfresco.repo.jscript.app;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Roy Wetherall
 */
public abstract class BasePropertyDecorator implements PropertyDecorator
{
    protected Set<QName> propertyNames;
    
    protected NodeService nodeService;
    
    protected NamespaceService namespaceService;
    
    protected PermissionService permissionService;
    
    protected JSONConversionComponent jsonConversionComponent;
    
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    public void setJsonConversionComponent(JSONConversionComponent jsonConversionComponent)
    {
        this.jsonConversionComponent = jsonConversionComponent;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    public void init()
    {
        jsonConversionComponent.registerPropertyDecorator(this);
    }
    
    @Override
    public Set<QName> getPropertyNames()
    {
        return propertyNames;
    }
    
    public void setPropertyName(String propertyName)
    {
        propertyNames = new HashSet<QName>(1);        
        propertyNames.add(QName.createQName(propertyName, namespaceService));
    }
    
    public void setPropertyNames(Set<String> propertyNames)
    {
        this.propertyNames = new HashSet<QName>(propertyNames.size());
        for (String propertyName : propertyNames)
        {
            this.propertyNames.add(QName.createQName(propertyName, namespaceService));
        }
    }

}
