/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.capability;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;

/**
 * @author Roy Wetherall
 * @since 2.0
 */
public class CapabilityServiceImpl implements CapabilityService
{
    /** Capabilities */
    private Map<String, Capability> capabilities = new HashMap<String, Capability>(57);
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService#getCapability(java.lang.String)
     */
    @Override
    public Capability getCapability(String name)
    {
        return capabilities.get(name);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService#registerCapability(org.alfresco.module.org_alfresco_module_rm.capability.Capability)
     */
    @Override
    public void registerCapability(Capability capability)
    {
        capabilities.put(capability.getName(), capability);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService#getCapabilities()
     */
    @Override
    public Set<Capability> getCapabilities()
    {
        return new HashSet<Capability>(capabilities.values());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService#getCapabilitiesAccessState(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Map<Capability, AccessStatus> getCapabilitiesAccessState(NodeRef nodeRef)
    {        
        HashMap<Capability, AccessStatus> answer = new HashMap<Capability, AccessStatus>();
        for (Capability capability : capabilities.values())
        {
            AccessStatus status = capability.hasPermission(nodeRef);
            if (answer.put(capability, status) != null)
            {
                throw new IllegalStateException();
            }
        }
        return answer;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService#getCapabilitiesAccessState(org.alfresco.service.cmr.repository.NodeRef, java.util.List)
     */
    public Map<Capability, AccessStatus> getCapabilitiesAccessState(NodeRef nodeRef, List<String> capabilityNames)
    {
        HashMap<Capability, AccessStatus> answer = new HashMap<Capability, AccessStatus>();
        for (String capabilityName : capabilityNames)
        {
            Capability capability = capabilities.get(capabilityName);
            if (capability != null)
            {
                AccessStatus status = capability.hasPermission(nodeRef);
                if (answer.put(capability, status) != null)
                {
                    throw new IllegalStateException();
                }
            }
        }
        return answer;        
    }

}
