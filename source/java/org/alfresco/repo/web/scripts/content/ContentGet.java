/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.web.scripts.content;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.scripts.WebScriptException;
import org.alfresco.web.scripts.WebScriptRequest;
import org.alfresco.web.scripts.WebScriptResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Content Retrieval Service
 * 
 * Stream content from the Repository.
 * 
 * @author davidc
 */
public class ContentGet extends StreamContent
{
    // Logger
    @SuppressWarnings("unused")
    private static final Log logger = LogFactory.getLog(ContentGet.class);
    
    // Component dependencies
    private Repository repository;
    private NamespaceService namespaceService;
    
    /**
     * @param repository
     */
    public void setRepository(Repository repository)
    {
        this.repository = repository; 
    }
    
    /**
     * @param namespaceService
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService; 
    }
        
    /**
     * @see org.alfresco.web.scripts.WebScript#execute(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    public void execute(WebScriptRequest req, WebScriptResponse res)
        throws IOException
    {        
        // convert web script URL to node reference in Repository
        String match = req.getServiceMatch().getPath();
        String[] matchParts = match.split("/");
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String[] id = templateVars.get("id").split("/");
        String[] path = new String[id.length + 2];
        path[0] = templateVars.get("store_type");
        path[1] = templateVars.get("store_id");
        System.arraycopy(id, 0, path, 2, id.length);
        NodeRef nodeRef = repository.findNodeRef(matchParts[2], path);
        if (nodeRef == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find " + matchParts[2] + " reference " + Arrays.toString(path));
        }
        
        // determine content property
        QName propertyQName = ContentModel.PROP_CONTENT;
        String contentPart = templateVars.get("property");
        if (contentPart.length() > 0 && contentPart.charAt(0) == ';')
        {
            if (contentPart.length() < 2)
            {
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Content property malformed");
            }
            String propertyName = contentPart.substring(1);
            if (propertyName.length() > 0)
            {
                propertyQName = QName.createQName(propertyName, namespaceService);
            }
        }

        // determine attachment
        boolean attach = Boolean.valueOf(req.getParameter("a"));
        
        // Stream the content
        streamContent(req, res, nodeRef, propertyQName, attach);
    }
}