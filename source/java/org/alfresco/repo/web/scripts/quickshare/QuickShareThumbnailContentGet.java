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
package org.alfresco.repo.web.scripts.quickshare;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.thumbnail.script.ScriptThumbnailService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Scriptable;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;


/**
 * QuickShare/PublicView 
 * 
 * GET web script to stream "shared" thumbnail content (ie. enabled for public/unauthenticated access) from the repository
 *
 * WARNING: **unauthenticated** web script (equivalent to authenticated version - see "thumbnail.get.js")
 * 
 * @author janv
 * @since Cloud/4.2
 */
public class QuickShareThumbnailContentGet extends QuickShareContentGet
{
    private static final Log logger = LogFactory.getLog(QuickShareContentGet.class);
    
    private ThumbnailService thumbnailService;
    private ScriptThumbnailService scriptThumbnailService;
    private ServiceRegistry serviceRegistry;
    
    public void setThumbnailService(ThumbnailService thumbnailService)
    {
        this.thumbnailService = thumbnailService;
    }
    
    public void setScriptThumbnailService(ScriptThumbnailService scriptThumbnailService)
    {
        this.scriptThumbnailService = scriptThumbnailService;
    }
    
    public void setServiceRegistry(ServiceRegistry services)
    {
        this.serviceRegistry = services;
    }
    
	@Override
	protected void executeImpl(NodeRef nodeRef, Map<String, String> templateVars, WebScriptRequest req, WebScriptResponse res, Map<String, Object> model) throws IOException
    {	
        String thumbnailName = templateVars.get("thumbnailname");
        if (thumbnailName == null)
        {
            logger.error("Thumbnail name was not provided: "+nodeRef);
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find " + nodeRef);
        }
        
        // Indicate whether or not the thumbnail can be cached by the browser. Caching is allowed if the lastModified
        // argument is provided as this is an indication of request uniqueness and therefore the browser will have
        // the latest thumbnail image.
        if (model == null)
        {
            model = new HashMap<String, Object>(1);
        }
        
        if (req.getParameter("lastModified") != null)
        {
            model.put("allowBrowserToCache", "true");  // note: must be String not boolean
        }
        else
        {
            model.put("allowBrowserToCache", "false"); // note: must be String not boolean
        }
        
        
        NodeRef thumbnailNodeRef = thumbnailService.getThumbnailByName(nodeRef, ContentModel.PROP_CONTENT, thumbnailName);
        
        if (thumbnailNodeRef == null)
        {
            // Get the queue/force create setting
            boolean qc = false;
            boolean fc = false;
            String c = req.getParameter("c");
            if (c != null)
            {
               if (c.equals("queue"))
               {
                  qc = true;
               }
               else if (c.equals("force"))
               {
                  fc = true;
               }
            }
            
            // Get the place holder flag
            boolean ph = false;
            String phString = req.getParameter("ph");
            if (phString != null)
            {
               ph = new Boolean(phString);
            }
            
            Scriptable scope = new BaseScopableProcessorExtension().getScope(); // note: required for ValueConverter (collection)
            ScriptNode node = new ScriptNode(nodeRef, serviceRegistry, scope);
            
            // Queue the creation of the thumbnail if appropriate
            if (fc)
            {
                ScriptNode thumbnailNode = node.createThumbnail(thumbnailName, false);
                if (thumbnailNode != null)
                {
                    thumbnailNodeRef = thumbnailNode.getNodeRef();
                }
            }
            else
            {
               if (qc)
               {
                   node.createThumbnail(thumbnailName, true);
               }
            }
            
            if (thumbnailNodeRef == null)
            {
                if (ph == true)
                {
                    // Try and get the place holder resource. We use a method in the thumbnail service
                    // that by default gives us a resource based on the content's mime type.
                    String phPath = null;
                    ContentData contentData = (ContentData)this.serviceRegistry.getNodeService().getProperty(nodeRef, ContentModel.PROP_CONTENT);
                    if (contentData != null)
                    {
                        phPath = scriptThumbnailService.getMimeAwarePlaceHolderResourcePath(thumbnailName, contentData.getMimetype());
                    }
                    
                    if (phPath == null)
                    {
                        // 404 since no thumbnail was found
                        throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Thumbnail was not found and no place holder resource set for '" + thumbnailName + "'");
                    }
                    else
                    {
                        // Set the resouce path in the model ready for the content stream to send back to the client
                        model.put("contentPath", phPath);
                    }
                }
                else
                {
                    // 404 since no thumbnail was found
                    throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Thumbnail was not found");
                }
            }
        }
        
        super.executeImpl(thumbnailNodeRef, templateVars, req, res, model);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("QuickShare - retrieved thumbnail content: "+thumbnailNodeRef+" ["+nodeRef+","+thumbnailName+"]");
        }
    }
}