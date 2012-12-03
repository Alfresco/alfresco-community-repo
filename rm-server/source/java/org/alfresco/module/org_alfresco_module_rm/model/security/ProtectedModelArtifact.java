/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.model.security;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Protected model artifact class.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public abstract class ProtectedModelArtifact
{
    /** Model security service */
    private ModelSecurityService modelSecurityService;    
    
    /** Namespace service */
    private NamespaceService namespaceService;
    
    /** Qualified name of the model artifact */
    private QName name;
    
    /** Set of capabilities */
    private Set<Capability> capabilities;
    
    private Set<String> capabilityNames;
    
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    public void setModelSecurityService(ModelSecurityService modelSecurityService)
    {
        this.modelSecurityService = modelSecurityService;
    }
    
    public void init()
    {
        modelSecurityService.register(this);
    }
    
    public void setName(String name)
    {
        QName qname = QName.createQName(name, namespaceService);
        this.name = qname;
    }
    
    public QName getQName()
    {
        return name;
    }
    
    public void setCapabilities(Set<Capability> capabilities)
    {
        this.capabilities = capabilities;
    }
    
    public Set<Capability> getCapabilities()
    {
        return capabilities;
    }
    
    public Set<String> getCapilityNames()
    {
        if (capabilityNames == null && capabilities != null)
        {
            capabilityNames = new HashSet<String>(capabilities.size());
            for (Capability capability : capabilities)
            {
                capabilityNames.add(capability.getName());
            }            
        }
        
        return capabilityNames;
    }
}
