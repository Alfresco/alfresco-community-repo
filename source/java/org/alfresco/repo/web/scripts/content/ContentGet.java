/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.web.context.ServletContextAware;


/**
 * Content Retrieval Service
 * 
 * Stream content from the Repository.
 * 
 * @author davidc
 */
public class ContentGet extends StreamContent implements ServletContextAware
{
    // Logger
    @SuppressWarnings("unused")
    private static final Log logger = LogFactory.getLog(ContentGet.class);
    
    // Component dependencies
    private ServletContext servletContext;
    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private ContentService contentService;

    /**
     * @param 
     */
    public void setServletContext(ServletContext servletContext)
    {
        this.servletContext = servletContext;
    }

    /**
     * @param dictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService; 
    }

    /**
     * @param namespaceService
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService; 
    }
    
    /**
     * @param contentService
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
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
        ObjectReference reference = createObjectReferenceFromUrl(args, templateVars);
        NodeRef nodeRef = reference.getNodeRef();
        if (nodeRef == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find " + reference.toString());
        }
        
        // determine attachment
        boolean attach = Boolean.valueOf(req.getParameter("a"));
        
        // render content
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

        // Stream the content
        streamContentLocal(req, res, nodeRef, attach, propertyQName);
    }

    private void streamContentLocal(WebScriptRequest req, WebScriptResponse res, NodeRef nodeRef, boolean attach, QName propertyQName) throws IOException
    {
        String userAgent = req.getHeader("User-Agent");

        boolean rfc5987Supported = (null != userAgent) && (userAgent.contains("MSIE") || userAgent.contains(" Chrome/") || userAgent.contains(" FireFox/"));

        if (attach && rfc5987Supported)
        {
            String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
            
            //IE use file extension to get mimetype
            //So we set correct extension. see MNT-11246
            if(userAgent.contains("MSIE"))
            {
                String mimeType = contentService.getReader(nodeRef, propertyQName).getMimetype();
                if (!mimetypeService.getMimetypes(FilenameUtils.getExtension(name)).contains(mimeType))
                {
                    name = FilenameUtils.removeExtension(name) + FilenameUtils.EXTENSION_SEPARATOR_STR + mimetypeService.getExtension(mimeType); 
                }
            }
            
            streamContent(req, res, nodeRef, propertyQName, attach, name, null);
        }
        else
        {
            streamContent(req, res, nodeRef, propertyQName, attach, null, null);
        }
    }
}