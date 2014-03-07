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
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.hold.HoldService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
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
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status, org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        NodeRef filePlan = getFilePlan(req);
        List<NodeRef> holds = holdService.getHolds(filePlan);
        List<String> holdNames = new ArrayList<String>(holds.size());
        for (NodeRef hold : holds)
        {
            String holdName = (String) nodeService.getProperty(hold, ContentModel.PROP_NAME);
            holdNames.add(holdName);
        }

        Map<String, Object> model = new HashMap<String, Object>(1);
        sortByName(holdNames);
        model.put("holds", holdNames);

        return model;
    }

    /**
     * Helper method to get the file plan
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
     * Helper method to sort the holds by their names
     *
     * @param holdNames List of hold names to sort
     */
    private void sortByName(List<String> holdNames)
    {
        Collections.sort(holdNames, new Comparator<String>()
        {
            @Override
            public int compare(String o1, String o2)
            {
                return o1.toLowerCase().compareTo(o2.toLowerCase());
            }
        });
    }
}
