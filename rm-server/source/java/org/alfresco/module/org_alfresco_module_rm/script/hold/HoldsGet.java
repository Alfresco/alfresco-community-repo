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
package org.alfresco.module.org_alfresco_module_rm.script.hold;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to return the list of holds in the hold container.
 *
 * @author Tuna Aksoy
 * @since 2.2
 */
public class HoldsGet extends DeclarativeWebScript
{
    /** File Plan Service */
    private FilePlanService filePlanService;

    /** Node Service */
    private NodeService nodeService;

    /** Hold Service */
    private HoldService holdService;
    
    /** permission service */
    private PermissionService permissionService;

    /**
     * Set the file plan service
     *
     * @param filePlanService the file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * Set the node service
     *
     * @param nodeService the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set the hold service
     *
     * @param holdService the hold service
     */
    public void setHoldService(HoldService holdService)
    {
        this.holdService = holdService;
    }
    
    /**
     * Set the permission service
     * 
     * @param permissionService     the permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status, org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        boolean fileOnly = getFileOnly(req);
        NodeRef itemNodeRef = getItemNodeRef(req);
        List<NodeRef> holds = new ArrayList<NodeRef>();

        if (itemNodeRef == null)
        {
            NodeRef filePlan = getFilePlan(req);
            holds.addAll(holdService.getHolds(filePlan));
        }
        else
        {
            boolean includedInHold = getIncludedInHold(req);
            holds.addAll(holdService.heldBy(itemNodeRef, includedInHold));
        }

        List<Hold> holdObjects = new ArrayList<Hold>(holds.size());
        for (NodeRef nodeRef : holds)
        {
            // only add if user has filling permisson on the hold
            if (!fileOnly || permissionService.hasPermission(nodeRef, RMPermissionModel.FILING) == AccessStatus.ALLOWED)
            {
                String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                holdObjects.add(new Hold(name, nodeRef));
            }
        }

        Map<String, Object> model = new HashMap<String, Object>(1);
        sortHoldByName(holdObjects);
        model.put("holds", holdObjects);

        return model;
    }

    /**
     * Helper method to get the file plan from the request
     *
     * @param req The webscript request
     * @return The {@link NodeRef} of the file plan
     */
    private NodeRef getFilePlan(WebScriptRequest req)
    {
        NodeRef filePlan = null;

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String storeType = templateVars.get("store_type");
        String storeId = templateVars.get("store_id");
        String id = templateVars.get("id");

        if (StringUtils.isNotBlank(storeType) && StringUtils.isNotBlank(storeId) && StringUtils.isNotBlank(id))
        {
            filePlan = new NodeRef(new StoreRef(storeType, storeId), id);
            
            // check that this node is actually a file plan
            if (!nodeService.exists(filePlan) || !filePlanService.isFilePlan(filePlan))
            {
                throw new WebScriptException(Status.STATUS_NOT_FOUND, "The file plan provided could not be found.");
            }
        }
        else
        {
            filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
            if (filePlan == null)
            {
                throw new WebScriptException(Status.STATUS_NOT_FOUND, "The default file plan node could not be found.");
            }
        }

        return filePlan;
    }

    /**
     * Helper method to get the item node reference from the request
     *
     * @param req The webscript request
     * @return The {@link NodeRef} of the item (record / record folder) or null if the parameter has not been passed
     */
    private NodeRef getItemNodeRef(WebScriptRequest req)
    {
        String nodeRef = req.getParameter("itemNodeRef");
        NodeRef itemNodeRef = null;
        if (StringUtils.isNotBlank(nodeRef))
        {
            itemNodeRef = new NodeRef(nodeRef);
        }
        return itemNodeRef;
    }

    /**
     * Helper method to get the includeInHold parameter value from the request
     *
     * @param req The webscript request
     * @return The value of the includeInHold parameter
     */
    private boolean getIncludedInHold(WebScriptRequest req)
    {
        boolean result = true;
        String includedInHold = req.getParameter("includedInHold");
        if (StringUtils.isNotBlank(includedInHold))
        {
            result = Boolean.valueOf(includedInHold).booleanValue();
        }
        return result;
    }
    
    private boolean getFileOnly(WebScriptRequest req)
    {
        boolean result = false;
        String fillingOnly = req.getParameter("fileOnly");
        if (StringUtils.isNotBlank(fillingOnly))
        {
            result = Boolean.valueOf(fillingOnly).booleanValue();
        }
        return result;
    }

    /**
     * Helper method to sort the holds by their names
     *
     * @param holds List of holds to sort
     */
    private void sortHoldByName(List<Hold> holds)
    {
        Collections.sort(holds, new Comparator<Hold>()
        {
            @Override
            public int compare(Hold h1, Hold h2)
            {
                return h1.getName().toLowerCase().compareTo(h2.getName().toLowerCase());
            }
        });
    }
}
