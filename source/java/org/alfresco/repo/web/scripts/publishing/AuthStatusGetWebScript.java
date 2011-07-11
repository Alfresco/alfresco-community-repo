/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.repo.web.scripts.publishing;

import java.util.Map;
import java.util.TreeMap;

import org.alfresco.repo.publishing.PublishingModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class AuthStatusGetWebScript extends DeclarativeWebScript
{
    private NodeService nodeService;

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String channelNodeUuid = templateVars.get("node_id");
        String channelNodeStoreProtocol = templateVars.get("store_protocol");
        String channelNodeStoreId = templateVars.get("store_id");

        String authStatus = "REJECTED";
        NodeRef channelNodeRef = new NodeRef(channelNodeStoreProtocol, channelNodeStoreId, channelNodeUuid);
        if (nodeService.exists(channelNodeRef))
        {
            Boolean authComplete = (Boolean)nodeService.getProperty(channelNodeRef, PublishingModel.PROP_AUTHORISATION_COMPLETE);
            if (authComplete)
            {
                authStatus = "AUTHORISED";
            }
            else
            {
                authStatus = "PENDING";
            }
        }
        Map<String,Object> model = new TreeMap<String, Object>();
        model.put("channelId", channelNodeRef.toString());
        model.put("authStatus", authStatus);
        
        return model;
    }
}
