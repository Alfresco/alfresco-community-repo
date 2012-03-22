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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.mozilla.javascript.Scriptable;

/**
 * Base records managment script node
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

    public ScriptCapability[] getCapabilities()
    {
        return capabilitiesSet(null);
    }
    
    public ScriptCapability[] capabilitiesSet(String capabilitiesSet)
    {
        RecordsManagementSecurityService rmSecurity = rmServices.getRecordsManagementSecurityService();
        Map<Capability, AccessStatus> cMap = null;
        if (capabilitiesSet == null)
        {
            // Get all capabilities            
            cMap = rmSecurity.getCapabilities(this.nodeRef);
        }
        else
        {
            cMap = rmSecurity.getCapabilities(this.nodeRef, capabilitiesSet);
        }
            
        List<ScriptCapability> list = new ArrayList<ScriptCapability>(cMap.size());
        for (Map.Entry<Capability, AccessStatus> entry : cMap.entrySet())
        {
            if (AccessStatus.ALLOWED.equals(entry.getValue()) == true ||
                AccessStatus.UNDETERMINED.equals(entry.getValue()) == true)
            {
                Capability cap = entry.getKey();
                String[] actions = (String[])cap.getActionNames().toArray(new String[cap.getActionNames().size()]);
                ScriptCapability scriptCap = new ScriptCapability(cap.getName(), cap.getName(), actions);
                list.add(scriptCap);
            }
        }
                                
        return (ScriptCapability[])list.toArray(new ScriptCapability[list.size()]);
    }
}
