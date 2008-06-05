/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
import java.io.Writer;
import java.net.SocketException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.scripts.AbstractWebScript;
import org.alfresco.web.scripts.Cache;
import org.alfresco.web.scripts.Container;
import org.alfresco.web.scripts.Description;
import org.alfresco.web.scripts.ScriptContent;
import org.alfresco.web.scripts.Status;
import org.alfresco.web.scripts.WebScriptException;
import org.alfresco.web.scripts.WebScriptRequest;
import org.alfresco.web.scripts.WebScriptResponse;
import org.alfresco.web.scripts.WebScriptStatus;
import org.alfresco.web.scripts.servlet.WebScriptServletRequest;
import org.alfresco.web.scripts.servlet.WebScriptServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Web script 'type' that can be used when the binary data of a content property needs to be streamed back to the client
 * as the result of executing the web script.
 * 
 * @author Roy Wetherall
 */
public class StreamContent extends AbstractWebScript
{
    // Logger
    private static final Log logger = LogFactory.getLog(StreamContent.class);
    
    protected PermissionService permissionService;
    protected NodeService nodeService;
    protected ContentService contentService;
    protected MimetypeService mimetypeService;    
    
    // Script Context
    private String basePath;
    private ScriptContent executeScript;

    /**
     * @param mimetypeService
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService; 
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

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.AbstractWebScript#init(org.alfresco.web.scripts.WebScriptRegistry)
     */
    @Override
    public void init(Container container, Description description)
    {
        super.init(container, description);

        // Test for "execute" script
        basePath = getDescription().getId();
        String scriptPath = basePath + ".js";
        executeScript = container.getScriptProcessor().findScript(scriptPath);
    }

    /**
     * @see org.alfresco.web.scripts.WebScript#execute(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        // retrieve requested format
        String format = req.getFormat();

        try
        {
            // construct model for script / template
            Status status = new Status();
            Cache cache = new Cache(getDescription().getRequiredCache());
            Map<String, Object> model = executeImpl(req, status, cache);
            if (model == null)
            {
                model = new HashMap<String, Object>(8, 1.0f);
            }
            model.put("status", status);
            model.put("cache", cache);
            
            // execute script if it exists
            if (executeScript != null)
            {
                if (logger.isDebugEnabled())
                    logger.debug("Executing script " + executeScript.getPathDescription());
                
                Map<String, Object> scriptModel = createScriptParameters(req, res, model);
                // add return model allowing script to add items to template model
                Map<String, Object> returnModel = new HashMap<String, Object>(8, 1.0f);
                scriptModel.put("model", returnModel);
                executeScript(executeScript, scriptModel);
                mergeScriptModelIntoTemplateModel(returnModel, model);
            }
            
            // Get the content parameters from the model
            NodeRef nodeRef = (NodeRef)model.get("contentNode");
            if (nodeRef == null)
            {
                throw new WebScriptException("The content node was not specified so the content cannot be streamed to the client");
            }
            QName propertyQName = null;
            String contentProperty = (String)model.get("contentProperty");
            if (contentProperty == null)
            {
                // default to the standard content property
                propertyQName = ContentModel.PROP_CONTENT;
            }
            else
            {
                propertyQName = QName.createQName(contentProperty);
            }
            Boolean attachBoolean = (Boolean)model.get("attach");
            boolean attach = false;
            if (attachBoolean != null)
            {
                attach = attachBoolean.booleanValue();
            }
            
            // is a redirect to a status specific template required?
            if (status.getRedirect())
            {
                // create model for template rendering
                Map<String, Object> templateModel = createTemplateParameters(req, res, model);
                sendStatus(req, res, status, cache, format, templateModel);
            }
            else
            {
                // Stream the content
                streamContent(req, res, nodeRef, propertyQName, attach);
            }
        }
        catch(Throwable e)
        {
            if (logger.isInfoEnabled())
                logger.info("Caught exception & redirecting to status template: " + e.getMessage());
                
            // extract status code, if specified
            int statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            if (e instanceof WebScriptException)
            {
                statusCode = ((WebScriptException)e).getStatus();
            }

            // send status
            Status status = new Status();
            status.setCode(statusCode);
            status.setMessage(e.getMessage());
            status.setException(e);
            Cache cache = new Cache();
            cache.setNeverCache(true);
            Map<String, Object> customModel = new HashMap<String, Object>(8, 1.0f);
            customModel.put("status", status);
            Map<String, Object> templateModel = createTemplateParameters(req, res, customModel);
            sendStatus(req, res, status, cache, format, templateModel);
        }
    }
    
    /**
     * Merge script generated model into template-ready model
     * 
     * @param scriptModel  script model
     * @param templateModel  template model
     */
    final private void mergeScriptModelIntoTemplateModel(Map<String, Object> scriptModel, Map<String, Object> templateModel)
    {
        for (Map.Entry<String, Object> entry : scriptModel.entrySet())
        {
            // retrieve script model value
            Object value = entry.getValue();
            Object templateValue = getContainer().getScriptProcessor().unwrapValue(value);
            templateModel.put(entry.getKey(), templateValue);
        }
    }

    /**
     * Execute custom Java logic
     * 
     * @param req  Web Script request
     * @param status Web Script status
     * @return  custom service model
     * @deprecated
     */
    @SuppressWarnings("deprecation")
    protected Map<String, Object> executeImpl(WebScriptRequest req, WebScriptStatus status)
    {
        return null;
    }

    /**
     * Execute custom Java logic
     * 
     * @param req  Web Script request
     * @param status Web Script status
     * @return  custom service model
     * @deprecated
     */
    @SuppressWarnings("deprecation")
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        return executeImpl(req, new WebScriptStatus(status));
    }

    /**
     * Execute custom Java logic
     * 
     * @param  req  Web Script request
     * @param  status Web Script status
     * @param  cache  Web Script cache
     * @return  custom service model
     */
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        // NOTE: Redirect to those web scripts implemented before cache support and v2.9
        return executeImpl(req, status);
    }
    
    /**
     * Render a template (of given format) to the Web Script Response
     * 
     * @param format  template format (null, default format)  
     * @param model  data model to render
     * @param writer  where to output
     */
    final protected void renderFormatTemplate(String format, Map<String, Object> model, Writer writer)
    {
        format = (format == null) ? "" : format;
        String templatePath = basePath + "." + format + ".ftl";

        if (logger.isDebugEnabled())
            logger.debug("Rendering template '" + templatePath + "'");

        renderTemplate(templatePath, model, writer);
    }
    
    /**
     * Streams the content on a given node's content property to the response of the web script.
     * 
     * @param req           request
     * @param res           response
     * @param nodeRef       the node reference
     * @param propertyQName the content property name
     * @param attach        indicates whether the content should be streamed as an attachment or not
     * @throws IOException 
     */
    protected void streamContent(WebScriptRequest req, WebScriptResponse res, NodeRef nodeRef, QName propertyQName, boolean attach)
        throws IOException
    {
        // NOTE: This web script must be executed in a HTTP Servlet environment
        if (!(req instanceof WebScriptServletRequest))
        {
            throw new WebScriptException("Content retrieval must be executed in HTTP Servlet environment");
        }
        HttpServletRequest httpReq = ((WebScriptServletRequest)req).getHttpServletRequest();
        HttpServletResponse httpRes = ((WebScriptServletResponse)res).getHttpServletResponse();
        
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
        String extensionPath = req.getExtensionPath();
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
