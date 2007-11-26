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
package org.alfresco.repo.web.scripts.bean;

import java.io.IOException;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.web.scripts.Repository;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.scripts.AbstractWebScript;
import org.alfresco.web.scripts.Cache;
import org.alfresco.web.scripts.WebScriptException;
import org.alfresco.web.scripts.WebScriptRequest;
import org.alfresco.web.scripts.WebScriptResponse;
import org.alfresco.web.scripts.servlet.WebScriptServletRequest;
import org.alfresco.web.scripts.servlet.WebScriptServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Content Retrieval Service
 * 
 * Stream content from the Repository.
 * 
 * @author davidc
 */
public class ContentGet extends AbstractWebScript
{
    // Logger
    private static final Log logger = LogFactory.getLog(ContentGet.class);
    
    // Component dependencies
    private Repository repository;
    private NamespaceService namespaceService;
    private PermissionService permissionService;
    private NodeService nodeService;
    private ContentService contentService;
    private MimetypeService mimetypeService;
    
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
     * @param permissionService
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService; 
    }

    /**
     * @param nodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService; 
    }

    /**
     * @param contentService
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService; 
    }

    /**
     * @param mimetypeService
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService; 
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScript#execute(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    public void execute(WebScriptRequest req, WebScriptResponse res)
        throws IOException
    {
        // NOTE: This web script must be executed in a HTTP Servlet environment
        if (!(req instanceof WebScriptServletRequest))
        {
            throw new WebScriptException("Content retrieval must be executed in HTTP Servlet environment");
        }
        HttpServletRequest httpReq = ((WebScriptServletRequest)req).getHttpServletRequest();
        HttpServletResponse httpRes = ((WebScriptServletResponse)res).getHttpServletResponse();
        
        // convert web script URL to node reference in Repository
        String match = req.getServiceMatch().getPath();
        String[] matchParts = match.split("/");
        String extensionPath = req.getExtensionPath();
        String[] extParts = extensionPath == null ? new String[1] : extensionPath.split("/");
        String[] path = new String[extParts.length -1];
        System.arraycopy(extParts, 1, path, 0, extParts.length -1);
        NodeRef nodeRef = repository.findNodeRef(matchParts[2], path);
        if (nodeRef == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find " + matchParts[2] + " reference " + Arrays.toString(path));
        }
        
        // determine content property
        QName propertyQName = ContentModel.PROP_CONTENT;
        String contentPart = extParts[0];
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
    
        if (logger.isDebugEnabled())
            logger.debug("Retrieving content from node ref " + nodeRef.toString() + " (property: " + propertyQName.toString() + ") (attach: " + attach + ")");

        // check that the user has at least READ_CONTENT access - else redirect to the login page
        if (permissionService.hasPermission(nodeRef, PermissionService.READ_CONTENT) == AccessStatus.DENIED)
        {
            throw new WebScriptException(HttpServletResponse.SC_FORBIDDEN, "Permission denied");
        }
       
        // check If-Modified-Since header and set Last-Modified header as appropriate
        Date modified = (Date)nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
        long modifiedSince = httpReq.getDateHeader("If-Modified-Since");
        if (modifiedSince > 0L)
        {
            // round the date to the ignore millisecond value which is not supplied by header
            long modDate = (modified.getTime() / 1000L) * 1000L;
            if (modDate <= modifiedSince)
            {
                httpRes.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
        }

        // handle attachment
        if (attach == true)
        {
            // set header based on filename - will force a Save As from the browse if it doesn't recognize it
            // this is better than the default response of the browser trying to display the contents
            httpRes.setHeader("Content-Disposition", "attachment");
        }

        // get the content reader
        ContentReader reader = contentService.getReader(nodeRef, propertyQName);
        if (reader == null || !reader.exists())
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to locate content for node ref " + nodeRef + " (property: " + propertyQName.toString() + ")");
        }

        // establish mimetype
        String mimetype = reader.getMimetype();
        if (mimetype == null || mimetype.length() == 0)
        {
            mimetype = MimetypeMap.MIMETYPE_BINARY;
            int extIndex = extensionPath.lastIndexOf('.');
            if (extIndex != -1)
            {
                String ext = extensionPath.substring(extIndex + 1);
                String mt = mimetypeService.getMimetypesByExtension().get(ext);
                if (mt != null)
                {
                    mimetype = mt;
                }
            }
        }

        // set mimetype for the content and the character encoding + length for the stream
        httpRes.setContentType(mimetype);
        httpRes.setCharacterEncoding(reader.getEncoding());
        httpRes.setHeader("Content-Length", Long.toString(reader.getSize()));
        
        // set caching
        Cache cache = new Cache();
        cache.setNeverCache(false);
        cache.setMustRevalidate(true);
        cache.setLastModified(modified);
        res.setCache(cache);
        
        // get the content and stream directly to the response output stream
        // assuming the repository is capable of streaming in chunks, this should allow large files
        // to be streamed directly to the browser response stream.
        try
        {
            reader.getContent(res.getOutputStream());
        }
        catch (SocketException e1)
        {
            // the client cut the connection - our mission was accomplished apart from a little error message
            if (logger.isInfoEnabled())
                logger.info("Client aborted stream read:\n\tnode: " + nodeRef + "\n\tcontent: " + reader);
        }
        catch (ContentIOException e2)
        {
            if (logger.isInfoEnabled())
                logger.info("Client aborted stream read:\n\tnode: " + nodeRef + "\n\tcontent: " + reader);
        }
    }
    
}