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
import java.text.MessageFormat;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.URLEncoder;
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
    
    private static final String NODE_URL   = "/api/node/content/{0}/{1}/{2}/{3}";
    
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
        
        // Stream the content
        streamContent(req, res, nodeRef, propertyQName, attach);
    }
    
    /**
     * Helper to generate a URL to a content node for downloading content from the server.
     * The content is supplied directly in the reponse. This generally means a browser will
     * attempt to open the content directly if possible, else it will prompt to save the file.
     *
     * @param ref     NodeRef of the content node to generate URL for (cannot be null)
     * @param name    File name end element to return on the url (used by the browser on Save)
     *
     * @return URL to download the content from the specified node
     */
    public final static String generateNodeURL(NodeRef ref, String name)
    {
        return MessageFormat.format(NODE_URL, new Object[] {
                ref.getStoreRef().getProtocol(),
                ref.getStoreRef().getIdentifier(),
                ref.getId(),
                URLEncoder.encode(name) } );
    }
}