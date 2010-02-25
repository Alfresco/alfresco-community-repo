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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.cmis.CMISObjectReference;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cmis.reference.ReferenceFactory;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
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
    private ReferenceFactory referenceFactory;
    private NamespaceService namespaceService;
    
    /**
     * @param reference factory
     */
    public void setReferenceFactory(ReferenceFactory referenceFactory)
    {
        this.referenceFactory = referenceFactory; 
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
        // create map of args
        String[] names = req.getParameterNames();
        Map<String, String> args = new HashMap<String, String>(names.length, 1.0f);
        for (String name : names)
        {
            args.put(name, req.getParameter(name));
        }
        
        // create map of template vars
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        
        // create object reference from url
        CMISObjectReference reference = referenceFactory.createObjectReferenceFromUrl(args, templateVars);
        NodeRef nodeRef = reference.getNodeRef();
        if (nodeRef == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find " + reference.toString());
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