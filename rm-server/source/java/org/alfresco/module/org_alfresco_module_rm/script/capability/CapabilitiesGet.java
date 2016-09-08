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
package org.alfresco.module.org_alfresco_module_rm.script.capability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class CapabilitiesGet extends DeclarativeWebScript
{
    private RecordsManagementService recordsManagementService;
    
    private CapabilityService capabilityService;
    
    public void setRecordsManagementService(RecordsManagementService recordsManagementService)
    {
        this.recordsManagementService = recordsManagementService;
    }
    
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }
    
    /**
     * @see org.alfresco.repo.web.scripts.content.StreamContent#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status, org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String storeType = templateVars.get("store_type");
        String storeId = templateVars.get("store_id");
        String nodeId = templateVars.get("id");
        
        boolean includePrivate = false;
        String includePrivateString = req.getParameter("includeAll");
        if (includePrivateString != null)
        {
            includePrivate = Boolean.parseBoolean(includePrivateString);
        }
        
        NodeRef nodeRef = null;
        if (storeType != null && storeId != null && nodeId != null)
        {
            nodeRef = new NodeRef(new StoreRef(storeType, storeId), nodeId);
        }
        else
        {
            // we are talking about the file plan node 
            // TODO we are making the assumption there is only one file plan here!
            List<NodeRef> filePlans = recordsManagementService.getFilePlans();
            if (filePlans.isEmpty() == true)
            {
                throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No file plan node has been found.");
            }
            else if (filePlans.size() != 1)
            {
                throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "More than one file plan has been found.");
            }
            nodeRef = filePlans.get(0);
        }
        
        Map<Capability, AccessStatus> map = capabilityService.getCapabilitiesAccessState(nodeRef, includePrivate);
        List<String> list = new ArrayList<String>(map.size());
        for (Map.Entry<Capability, AccessStatus> entry : map.entrySet())
        {
            AccessStatus accessStatus = entry.getValue();
            if (AccessStatus.DENIED.equals(accessStatus) == false)
            {
                Capability capability = entry.getKey();
                list.add(capability.getName());
            }            
        }
        
        Map<String, Object> model = new HashMap<String, Object>(1);
        model.put("capabilities", list);
        return model;
    }
}