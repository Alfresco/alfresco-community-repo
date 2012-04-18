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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;

/**
 * Capability service implementation
 * 
 * @author Roy Wetherall
 * @since 2.0
 */
public interface CapabilityService
{
    /**
     * Register a capability
     * 
     * @param capability    capability
     */
    void registerCapability(Capability capability);
    
    /**
     * Get a named capability.
     * 
     * @param name  capability name
     * @return {@link Capability}   capability or null if not found
     */
    Capability getCapability(String name);
    
    /**
     * 
     * @return
     */
    Set<Capability> getCapabilities();
    
    /**
     * 
     * @param nodeRef
     * @return
     */
    Map<Capability, AccessStatus> getCapabilitiesAccessState(NodeRef nodeRef);
    
    /**
     * 
     * @param nodeRef
     * @param capabilityNames
     * @return
     */
    Map<Capability, AccessStatus> getCapabilitiesAccessState(NodeRef nodeRef, List<String> capabilityNames);
}
