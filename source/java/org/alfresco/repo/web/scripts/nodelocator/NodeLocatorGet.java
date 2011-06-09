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

package org.alfresco.repo.web.scripts.nodelocator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.URLDecoder;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class NodeLocatorGet extends DeclarativeWebScript
{
    private static final String NODE_ID = "node_id";
    private static final String STORE_ID = "store_id";
    private static final String STORE_TYPE = "store_type";
    private static final String NODE_LOCATOR_NAME = "node_locator_name";
    private NodeLocatorService locatorService;
    
    /**
    * {@inheritDoc}
    */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        Map<String, String> vars = req.getServiceMatch().getTemplateVars();
        // getting task id from request parameters
        String locatorName = vars.get(NODE_LOCATOR_NAME);

        // No locatorname specified -> return 404
        if (locatorName == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "No NodeLocator strategy was specified!");
        }

        NodeRef source = null;
        String storeType = vars.get(STORE_TYPE);
        String storeId= vars.get(STORE_ID);
        String nodeId= vars.get(NODE_ID);
        if(storeType!=null && storeId != null && nodeId != null)
        {
            source = new NodeRef(storeType, storeId, nodeId);
        }
        
        Map<String, Serializable> params = mapParams(req);
        
        NodeRef node = locatorService.getNode(locatorName, source, params);
        
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("nodeRef", node==null ? null : node.toString());
        return model;
    }

    private Map<String, Serializable> mapParams(WebScriptRequest req)
    {
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        for(String key: req.getParameterNames())
        {
            String value = req.getParameter(key);
            if (value != null)
            {
                String decodedValue = URLDecoder.decode(value);
                // TODO Handle type conversions here.
                params.put(key, decodedValue);
            }
        }
        return params;
    }

    /**
     * @param locatorService the locatorService to set
     */
    public void setNodeLocatorService(NodeLocatorService locatorService)
    {
        this.locatorService = locatorService;
    }

}
