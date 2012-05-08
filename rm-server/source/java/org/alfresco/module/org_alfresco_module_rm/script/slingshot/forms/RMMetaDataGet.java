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
package org.alfresco.module.org_alfresco_module_rm.script.slingshot.forms;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.module.org_alfresco_module_rm.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * RM metadata used by form extension
 * 
 * @author Roy Wetherall
 */
public class RMMetaDataGet extends DeclarativeWebScript
{
    /** Query parameters */
    private static final String PARAM_NODEREF = "noderef";
    private static final String PARAM_TYPE = "type";
    
    /** NodeRef pattern */
    private static final Pattern nodeRefPattern = Pattern.compile(".+://.+/.+");
    
    /** Records management service */
    private RecordsManagementService rmService;
    
    /** Namespace service */
    private NamespaceService namespaceService;
    
    /**
     * @param rmService records management service
     */
    public void setRecordsManagementService(RecordsManagementService rmService)
    {
        this.rmService = rmService;
    }
    
    /**
     * @param namespaceService  namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /*
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        // create model object with the lists model
        Map<String, Object> model = new HashMap<String, Object>(1);

        String result = "NONE";
        
        // Get the nodeRef and confirm it is valid        
        String nodeRef = req.getParameter(PARAM_NODEREF);
        if (nodeRef == null || nodeRef.length() == 0)
        {
            String type = req.getParameter(PARAM_TYPE);
            if (type != null && type.length() != 0)
            {
                QName qname = QName.createQName(type, namespaceService);
                FilePlanComponentKind kind = rmService.getFilePlanComponentKindFromType(qname);
                if (kind != null)
                {
                    result = kind.toString();
                }
            }
        }
        else
        {
            // quick test before running slow match for full NodeRef pattern
            if (nodeRef.indexOf(':') != -1)
            {
                Matcher m = nodeRefPattern.matcher(nodeRef);
                if (m.matches())
                {
                    FilePlanComponentKind kind = rmService.getFilePlanComponentKind(new NodeRef(nodeRef));
                    if (kind != null)
                    {
                        result = kind.toString();
                    }
                }
            }
        }
        
        model.put("kind", result);
        return model;
    }
}