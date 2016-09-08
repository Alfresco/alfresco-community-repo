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
package org.alfresco.module.org_alfresco_module_rm.jscript;

import java.util.Collections;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.mozilla.javascript.Scriptable;

/**
 * Base records management script node
 * 
 * NOTE: this could be removed, but is being kept as a place holder for future development
 * 
 * @author Roy Wetherall
 */
public class ScriptRecordsManagmentNode extends ScriptNode
{
    private static final long serialVersionUID = 8872385533440938353L;

    private RecordsManagementServiceRegistry rmServices;
    
    public ScriptRecordsManagmentNode(NodeRef nodeRef, RecordsManagementServiceRegistry services, Scriptable scope)
    {       
        super(nodeRef, services, scope);
        rmServices = services;
    }

    public ScriptRecordsManagmentNode(NodeRef nodeRef, RecordsManagementServiceRegistry services)
    {
        super(nodeRef, services);
        rmServices = services;
    }
    
    public boolean hasCapability(String capabilityName)
    {
        boolean result = false;
        
        CapabilityService capabilityService = (CapabilityService)rmServices.getCapabilityService();
        Capability capability = capabilityService.getCapability(capabilityName);
        if (capability != null)
        {
            Map<Capability, AccessStatus> map = capabilityService.getCapabilitiesAccessState(nodeRef, Collections.singletonList(capabilityName));
            if (map.containsKey(capability) == true)
            {
                AccessStatus accessStatus = map.get(capability);
                if (accessStatus.equals(AccessStatus.DENIED) == false)
                {
                    result = true;
                }
            }
        }
        
        return result;
    }
}
