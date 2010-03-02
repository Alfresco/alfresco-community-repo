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
package org.alfresco.repo.web.scripts.content;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.cmis.CMISObjectReference;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cmis.reference.ReferenceFactory;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;


/**
 * Content Delete Service
 * 
 * Delete content stream from the Repository.
 * 
 * @author davidc
 */
public class ContentDelete extends AbstractWebScript
{
    // Component dependencies
    private ReferenceFactory referenceFactory;
    private NamespaceService namespaceService;
    private NodeService nodeService;
    
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
     * @param nodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService; 
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

        // retrieve content property
        ContentData contentData = (ContentData)nodeService.getProperty(nodeRef, propertyQName);
        if (contentData != null)
        {
            contentData = new ContentData(null, null, 0, null);
            nodeService.setProperty(nodeRef, propertyQName, contentData);
        }
     
        // no content returned
        res.setStatus(Status.STATUS_NO_CONTENT);
    }
}