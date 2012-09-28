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
package org.alfresco.repo.web.scripts.download;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript for canceling a download.
 *
 * @author Alex Miller
 */
public class DownloadDelete extends AbstractDownloadWebscript
{

    protected NodeService nodeService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        if (templateVars == null)
        {
           String error = "No parameters supplied";
           throw new WebScriptException(Status.STATUS_BAD_REQUEST, error);
        }

        if (! ( templateVars.containsKey("store_type") 
                && templateVars.containsKey("store_id")
                && templateVars.containsKey("node_id")) )
        {
            String error = "Missing template variables (store_type, store_id or node_id).";
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, error);
        }
        
        StoreRef store = new StoreRef(templateVars.get("store_type"), templateVars.get("store_id"));
        NodeRef nodeRef = new NodeRef(store, templateVars.get("node_id"));
        if (! nodeService.exists(nodeRef))
        {
            String error = "Could not find node: " + nodeRef;
            throw new WebScriptException(Status.STATUS_NOT_FOUND, error);
        }
        
        downloadService.cancelDownload(nodeRef);
        
        status.setCode(HttpServletResponse.SC_OK);
        
        return new HashMap<String, Object>();
        
    }

    public void setNodeService(NodeService nodeSerivce)
    {
        this.nodeService = nodeSerivce;
    }
}
