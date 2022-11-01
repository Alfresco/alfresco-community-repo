/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.web.scripts.content;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.web.scripts.MimeTypeUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
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

    private List<String> nonAttachContentTypes = Collections.emptyList();

    /**
     * @param nonAttachContentTypes List<String>
     */
    public void setNonAttachContentTypes(List<String> nonAttachContentTypes)
    {
        if (nonAttachContentTypes != null && !nonAttachContentTypes.isEmpty())
        {
            this.nonAttachContentTypes = nonAttachContentTypes;
        }
    }

    /**
     * @param servletContext ServletContext
     */
    public void setServletContext(ServletContext servletContext)
    {
        this.servletContext = servletContext;
    }

    /**
     * @param dictionaryService DictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService; 
    }

    /**
     * @param namespaceService NamespaceService
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService; 
    }
    
    /**
     * @param contentService ContentService
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * @see org.springframework.extensions.webscripts.WebScript#execute(WebScriptRequest, WebScriptResponse)
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
        // determine attachment and force download for specific mimetypes - see PRODSEC-5862
        boolean attach = Boolean.valueOf(req.getParameter("a"));
        ContentReader reader = contentService.getReader(nodeRef, propertyQName);
        String mimetype = MimeTypeUtil.determineMimetype(reader, req, mimetypeService);

        if (!attach)
        {
            if (nonAttachContentTypes == null || !nonAttachContentTypes.contains(mimetype))
            {
                attach = true;
                logger.warn("Ignored a=false for " + nodeRef.getId() + " since " + mimetype + " is not in the whitelist for non-attach content types");
            }
        }

        // Stream the content
        streamContentLocal(req, res, nodeRef, attach, propertyQName, null);
    }

    protected void streamContentLocal(WebScriptRequest req, WebScriptResponse res, NodeRef nodeRef, boolean attach, QName propertyQName, Map<String, Object> model) throws IOException
    {
        String userAgent = req.getHeader("User-Agent");
        userAgent = userAgent != null ? userAgent.toLowerCase() : "";
        boolean rfc5987Supported = (userAgent.contains("msie") ||
                userAgent.contains(" trident/") ||
                userAgent.contains(" chrome/") ||
                userAgent.contains(" firefox/") ||
                userAgent.contains(" safari/"));

        if (attach && rfc5987Supported)
        {
            String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
            
            // maintain the original name of the node during the download - do not modify it - see MNT-16510
            streamContent(req, res, nodeRef, propertyQName, attach, name, model);
        }
        else
        {
            streamContent(req, res, nodeRef, propertyQName, attach, null, model);
        }
    }
}