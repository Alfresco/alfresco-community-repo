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

import java.util.Collections;
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
        return getCapabilities(true);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService#getCapabilities(boolean)
     */
    @Override
    public Set<Capability> getCapabilities(boolean includePrivate)
    {
        Set<Capability> result = null;
        if (includePrivate == true)
        {
            result = new HashSet<Capability>(capabilities.values());
        }
        else
        {
            result = new HashSet<Capability>(capabilities.size());
            for (Capability capability : capabilities.values())
            {
                if (capability.isPrivate() == false)
                {
                    result.add(capability);
                }
            }
        }
        
        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService#getCapabilitiesAccessState(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Map<Capability, AccessStatus> getCapabilitiesAccessState(NodeRef nodeRef)
    {        
        return getCapabilitiesAccessState(nodeRef, false);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService#getCapabilitiesAccessState(org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    @Override
    public Map<Capability, AccessStatus> getCapabilitiesAccessState(NodeRef nodeRef, boolean includePrivate)
    {
        Set<Capability> listOfCapabilites = getCapabilities(includePrivate);
        HashMap<Capability, AccessStatus> answer = new HashMap<Capability, AccessStatus>();
        for (Capability capability : listOfCapabilites)
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
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService#getCapabilityAccessState(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @Override
    public AccessStatus getCapabilityAccessState(NodeRef nodeRef, String capabilityName)
    {
        AccessStatus result = AccessStatus.UNDETERMINED;
        Capability capability = getCapability(capabilityName);
        if (capability != null)
        {
            List<String> list = Collections.singletonList(capabilityName);
            Map<Capability, AccessStatus> map = getCapabilitiesAccessState(nodeRef, list);
            result = map.get(capability);
        }
        return result;
    }

}
