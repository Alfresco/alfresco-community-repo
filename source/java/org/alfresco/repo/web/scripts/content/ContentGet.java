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
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.cmis.CMISFilterNotValidException;
import org.alfresco.cmis.CMISObjectReference;
import org.alfresco.cmis.CMISRendition;
import org.alfresco.cmis.CMISRenditionService;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cmis.reference.ReferenceFactory;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.FileTypeImageSize;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.ServletContextResource;


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
    private ReferenceFactory referenceFactory;
    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private CMISRenditionService renditionService;

    /**
     * @param 
     */
    public void setServletContext(ServletContext servletContext)
    {
        this.servletContext = servletContext;
    }

    /**
     * @param reference factory
     */
    public void setReferenceFactory(ReferenceFactory referenceFactory)
    {
        this.referenceFactory = referenceFactory; 
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
     * @param renditionService
     */
    public void setCMISRenditionService(CMISRenditionService renditionService)
    {
        this.renditionService = renditionService;
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
        
        // determine attachment
        boolean attach = Boolean.valueOf(req.getParameter("a"));
        
        // stream content on node, or rendition of node
        String streamId = req.getParameter("streamId");
        if (streamId != null && streamId.length() > 0)
        {
            // render content rendition
            streamRendition(req, res, reference, streamId, attach);
        }
        else
        {
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
    }

    private void streamContentLocal(WebScriptRequest req, WebScriptResponse res, NodeRef nodeRef, boolean attach, QName propertyQName) throws IOException
    {
        String userAgent = req.getHeader("User-Agent");

        boolean rfc5987Supported = (null != userAgent) && (userAgent.contains("MSIE") || userAgent.contains(" Chrome/") || userAgent.contains(" FireFox/"));

        if (attach && rfc5987Supported)
        {
            String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
            streamContent(req, res, nodeRef, propertyQName, attach, name, null);
        }
        else
        {
            streamContent(req, res, nodeRef, propertyQName, attach, null, null);
        }
    }

    /**
     * Stream content rendition
     * 
     * @param req
     * @param res
     * @param reference
     * @param streamId
     * @param attach
     * @throws IOException
     */
    private void streamRendition(WebScriptRequest req, WebScriptResponse res, CMISObjectReference reference, String streamId, boolean attach)
        throws IOException
    {
        try
        {
            // find rendition
            CMISRendition rendition = null;
            List<CMISRendition> renditions = renditionService.getRenditions(reference.getNodeRef(), "*");
            for (CMISRendition candidateRendition : renditions)
            {
                if (candidateRendition.getStreamId().equals(streamId))
                {
                    rendition = candidateRendition;
                    break;
                }
            }
            if (rendition == null)
            {
                throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find rendition " + streamId + " for " + reference.toString());
            }
            
            // determine if special case for icons
            if (streamId.startsWith("alf:icon"))
            {
                streamIcon(res, reference, streamId, attach);
            }
            else
            {
                streamContentLocal(req, res, rendition.getNodeRef(), attach, ContentModel.PROP_CONTENT);
            }
        }
        catch(CMISFilterNotValidException e)
        {
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid Rendition Filter");
        }
    }
    
    /**
     * Stream Icon
     * 
     * @param res
     * @param reference
     * @param streamId
     * @param attach
     * @throws IOException
     */
    private void streamIcon(WebScriptResponse res, CMISObjectReference reference, String streamId, boolean attach)
        throws IOException
    {
        // convert stream id to icon size
        FileTypeImageSize imageSize = streamId.equals("alf:icon16") ? FileTypeImageSize.Small : FileTypeImageSize.Medium; 
        String iconSize = streamId.equals("alf:icon16") ? "-16" : "";
        
        // calculate icon file name and path
        String iconPath = null;
        if (dictionaryService.isSubClass(nodeService.getType(reference.getNodeRef()), ContentModel.TYPE_CONTENT))
        {
            String name = (String)nodeService.getProperty(reference.getNodeRef(), ContentModel.PROP_NAME);
            iconPath = FileTypeImageUtils.getFileTypeImage(servletContext, name, imageSize);
        }
        else
        {
            String icon = (String)nodeService.getProperty(reference.getNodeRef(), ApplicationModel.PROP_ICON);
            if (icon != null)
            {
                iconPath = "/images/icons/" + icon + iconSize + ".gif";
            }
            else
            {
                iconPath = "/images/icons/space-icon-default" + iconSize + ".gif";
            }
        }
        
        // set mimetype
        String mimetype = MimetypeMap.MIMETYPE_BINARY;
        int extIndex = iconPath.lastIndexOf('.');
        if (extIndex != -1)
        {
            String ext = iconPath.substring(extIndex + 1);
            mimetype = mimetypeService.getMimetype(ext);
        }
        res.setContentType(mimetype);

        // stream icon
        ServletContextResource resource = new ServletContextResource(servletContext, iconPath);
        if (!resource.exists())
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find rendition " + streamId + " for " + reference.toString());
        }
        FileCopyUtils.copy(resource.getInputStream(), res.getOutputStream());
    }

}