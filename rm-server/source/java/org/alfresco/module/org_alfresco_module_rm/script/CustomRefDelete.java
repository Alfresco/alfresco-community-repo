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
package org.alfresco.module.org_alfresco_module_rm.script;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementAdminService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation for Java backed webscript to remove RM custom reference instances
 * from a node.
 * 
 * @author Neil McErlean
 */
public class CustomRefDelete extends AbstractRmWebScript
{
    private static Log logger = LogFactory.getLog(CustomRefDelete.class);
    
    private RecordsManagementAdminService rmAdminService;

    public void setRecordsManagementAdminService(RecordsManagementAdminService rmAdminService)
    {
		this.rmAdminService = rmAdminService;
	}

    /*
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> ftlModel = removeCustomReferenceInstance(req);
        
        return ftlModel;
    }
    
    /**
     * Removes custom reference.
     */
    protected Map<String, Object> removeCustomReferenceInstance(WebScriptRequest req)
    {
        NodeRef fromNodeRef = parseRequestForNodeRef(req);

        // Get the toNode from the URL query string.
        String storeType = req.getParameter("st");
        String storeId = req.getParameter("si");
        String nodeId = req.getParameter("id");
        
        // create the NodeRef and ensure it is valid
        StoreRef storeRef = new StoreRef(storeType, storeId);
        NodeRef toNodeRef = new NodeRef(storeRef, nodeId);
        
        if (!this.nodeService.exists(toNodeRef))
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find to-node: " + 
            		toNodeRef.toString());
        }

        Map<String, Object> result = new HashMap<String, Object>();
        
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String clientsRefId = templateVars.get("refId");
        QName qn = rmAdminService.getQNameForClientId(clientsRefId);
        if (qn == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            		"Unable to find reference type: " + clientsRefId);
        }
        
        if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Removing reference ").append(qn).append(" from ")
                .append(fromNodeRef).append(" to ").append(toNodeRef);
            logger.debug(msg.toString());
        }
        
        rmAdminService.removeCustomReference(fromNodeRef, toNodeRef, qn);
        rmAdminService.removeCustomReference(toNodeRef, fromNodeRef, qn);
        
        result.put("success", true);

        return result;
    }
}