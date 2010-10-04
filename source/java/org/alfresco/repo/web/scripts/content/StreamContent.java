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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.ScriptProcessor;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.WebScriptStatus;
import org.springframework.util.FileCopyUtils;

import de.schlichtherle.io.FileOutputStream;

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

 	/**
 	 * format definied by RFC 822, see http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3 
 	 */
 	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);
    
    /** Services */
    protected PermissionService permissionService;
    protected NodeService nodeService;
    protected ContentService contentService;
    protected MimetypeService mimetypeService;    


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
            ScriptDetails executeScript = getExecuteScript(req.getContentType());
            if (executeScript != null)
            {
                if (logger.isDebugEnabled())
                    logger.debug("Executing script " + executeScript.getContent().getPathDescription());
                
                Map<String, Object> scriptModel = createScriptParameters(req, res, executeScript, model);
                // add return model allowing script to add items to template model
                Map<String, Object> returnModel = new HashMap<String, Object>(8, 1.0f);
                scriptModel.put("model", returnModel);
                executeScript(executeScript.getContent(), scriptModel);
                mergeScriptModelIntoTemplateModel(executeScript.getContent().getPath(), returnModel, model);
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
                // Get the attachement property value    
                Boolean attachBoolean = (Boolean)model.get("attach");
                boolean attach = false;
                if (attachBoolean != null)
                {
                    attach = attachBoolean.booleanValue();
                }
                
                String contentPath = (String)model.get("contentPath");
                if (contentPath == null)
                {
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
                
                    // Stream the content
                    streamContent(req, res, nodeRef, propertyQName, attach);
                }
                else
                {
                    // Stream the content
                    streamContent(req, res, contentPath, attach);
                }
            }
        }
        catch(Throwable e)
        {
            throw createStatusException(e, req, res);
        }
    }
    
    /**
     * Merge script generated model into template-ready model
     *
     * @param scriptPath   path to script
     * @param scriptModel  script model
     * @param templateModel  template model
     */
    final private void mergeScriptModelIntoTemplateModel(String scriptPath, Map<String, Object> scriptModel, Map<String, Object> templateModel)
    {
        int i = scriptPath.lastIndexOf(".");
        if (i != -1)
        {
            String extension = scriptPath.substring(i+1);
            ScriptProcessor processor = getContainer().getScriptProcessorRegistry().getScriptProcessorByExtension(extension);
            if (processor != null)
            {
                for (Map.Entry<String, Object> entry : scriptModel.entrySet())
                {
                    // retrieve script model value
                    Object value = entry.getValue();
                    Object templateValue = processor.unwrapValue(value);
                    templateModel.put(entry.getKey(), templateValue);
                }
            }
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
        String templatePath = getDescription().getId() + "." + format + ".ftl";

        if (logger.isDebugEnabled())
            logger.debug("Rendering template '" + templatePath + "'");

        renderTemplate(templatePath, model, writer);
    }
    
    /**
     * Streams the content on a given node's content property to the response of the web script.
     * 
     * @param req               Request
     * @param res               Response
     * @param nodeRef           The node reference
     * @param propertyQName     The content property name
     * @param attach            Indicates whether the content should be streamed as an attachment or not
     * @throws IOException 
     */
    protected void streamContent(WebScriptRequest req, WebScriptResponse res, NodeRef nodeRef, QName propertyQName, 
                boolean attach) throws IOException
    {
        streamContent(req, res, nodeRef, propertyQName, attach, null);
    }
    
    /**
     * Streams the content on a given node's content property to the response of the web script.
     * 
     * @param req               Request
     * @param res               Response
     * @param nodeRef           The node reference
     * @param propertyQName     The content property name
     * @param attach            Indicates whether the content should be streamed as an attachment or not
     * @param attachFileName    Optional file name to use when attach is <code>true</code>
     * @throws IOException 
     */
    protected void streamContent(WebScriptRequest req, WebScriptResponse res, NodeRef nodeRef, QName propertyQName, 
                boolean attach, String attachFileName) throws IOException
    {
        if (logger.isDebugEnabled())
            logger.debug("Retrieving content from node ref " + nodeRef.toString() + " (property: " + propertyQName.toString() + ") (attach: " + attach + ")");

        // TODO 
        // This was commented out to accomadate records management permissions.  We need to review how we cope with this
        // hard coded permission checked.
        
        // check that the user has at least READ_CONTENT access - else redirect to the login page
//        if (permissionService.hasPermission(nodeRef, PermissionService.READ_CONTENT) == AccessStatus.DENIED)
//        {
//            throw new WebScriptException(HttpServletResponse.SC_FORBIDDEN, "Permission denied");
//        }
       
        // check If-Modified-Since header and set Last-Modified header as appropriate
        Date modified = (Date)nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
        long modifiedSince = -1;
        String modifiedSinceStr = req.getHeader("If-Modified-Since");
        if (modifiedSinceStr != null)
        {
            try
            {
                modifiedSince = dateFormat.parse(modifiedSinceStr).getTime();
            }
            catch (Throwable e)
            {
                if (logger.isInfoEnabled())
                    logger.info("Browser sent badly-formatted If-Modified-Since header: " + modifiedSinceStr);
            }
            
            if (modifiedSince > 0L)
            {
                // round the date to the ignore millisecond value which is not supplied by header
                long modDate = (modified.getTime() / 1000L) * 1000L;
                if (modDate <= modifiedSince)
                {
                    res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    return;
                }
            }
        }

        // get the content reader
        ContentReader reader = contentService.getReader(nodeRef, propertyQName);
        if (reader == null || !reader.exists())
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to locate content for node ref " + nodeRef + " (property: " + propertyQName.toString() + ")");
        }
        
        // Stream the content
        streamContentImpl(req, res, reader, attach, modified, String.valueOf(modified.getTime()), attachFileName);
    }

    /**
     * Streams content back to client from a given resource path.
     * 
     * @param req               The request
     * @param res               The response
     * @param resourcePath      The resource path the content is required for
     * @param attach            Indicates whether the content should be streamed as an attachment or not
     * @throws IOException
     */
    protected void streamContent(WebScriptRequest req, WebScriptResponse res, String resourcePath, 
                boolean attach) throws IOException
    {
        streamContent(req, res, resourcePath, attach, null);
    }
    
    /**
     * Streams content back to client from a given resource path.
     * 
     * @param req               The request
     * @param res               The response
     * @param resourcePath      The resource path the content is required for
     * @param attach            Indicates whether the content should be streamed as an attachment or not
     * @param attachFileName    Optional file name to use when attach is <code>true</code>
     * @throws IOException
     */
    protected void streamContent(WebScriptRequest req, WebScriptResponse res, String resourcePath, 
                boolean attach, String attachFileName) throws IOException
    {
        if (logger.isDebugEnabled())
            logger.debug("Retrieving content from resource path " + resourcePath + " (attach: " + attach + ")");
        
        // get extension of resource
        String ext = "";
        int extIndex = resourcePath.lastIndexOf('.');
        if (extIndex != -1)
        {
            ext = resourcePath.substring(extIndex);
        }
        
        // create temporary file 
        File file = TempFileProvider.createTempFile("streamContent-", ext);
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(resourcePath);
        OutputStream os = new FileOutputStream(file);        
        FileCopyUtils.copy(is, os);        
        
        // stream the contents of the file
        streamContent(req, res, file, attach, attachFileName);
    }
    
    /**
     * Streams content back to client from a given resource path.
     * 
     * @param req               The request
     * @param res               The response
     * @param resourcePath      The resource path the content is required for
     * @param attach            Indicates whether the content should be streamed as an attachment or not
     * @throws IOException
     */
    protected void streamContent(WebScriptRequest req, WebScriptResponse res, File file, boolean attach) 
                throws IOException
    {
        streamContent(req, res, file, attach, null);
    }
    
    /**
     * Streams content back to client from a given resource path.
     * 
     * @param req               The request
     * @param res               The response
     * @param resourcePath      The resource path the content is required for
     * @param attach            Indicates whether the content should be streamed as an attachment or not
     * @param attachFileName    Optional file name to use when attach is <code>true</code>
     * @throws IOException
     */
    protected void streamContent(WebScriptRequest req, WebScriptResponse res, File file, boolean attach, 
                String attachFileName) throws IOException
    {
        if (logger.isDebugEnabled())
            logger.debug("Retrieving content from file " + file.getAbsolutePath() + " (attach: " + attach + ")");
        
        // determine mimetype from file extension
        String filePath = file.getAbsolutePath();
        String mimetype = MimetypeMap.MIMETYPE_BINARY;
        int extIndex = filePath.lastIndexOf('.');
        if (extIndex != -1)
        {
            mimetype = mimetypeService.getMimetype(filePath.substring(extIndex + 1));
        }
        
        // setup file reader and stream
        FileContentReader reader = new FileContentReader(file);
        reader.setMimetype(mimetype);
        reader.setEncoding("UTF-8");
        
        Date lastModified = new Date(file.lastModified());
        
        streamContentImpl(req, res, reader, attach, lastModified, 
                    String.valueOf(lastModified.getTime()), attachFileName);
    }
    
    /**
     * Stream content implementation
     * 
     * @param req               The request
     * @param res               The response
     * @param reader            The reader
     * @param attach            Indicates whether the content should be streamed as an attachment or not
     * @param modified          Modified date of content
     * @param eTag              ETag to use
     * @param attachFileName    Optional file name to use when attach is <code>true</code>
     * @throws IOException
     */
    protected void streamContentImpl(WebScriptRequest req, WebScriptResponse res, ContentReader reader, boolean attach, 
                Date modified, String eTag, String attachFileName) throws IOException
    {
        setAttachment(res, attach, attachFileName);

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
                mimetype = mimetypeService.getMimetype(ext);
            }
        }

        // set mimetype for the content and the character encoding + length for the stream
        res.setContentType(mimetype);
        res.setContentEncoding(reader.getEncoding());
        res.setHeader("Content-Length", Long.toString(reader.getSize()));
        
        // set caching
        Cache cache = new Cache();
        cache.setNeverCache(false);
        cache.setMustRevalidate(true);
        cache.setMaxAge(0L);
        cache.setLastModified(modified);
        cache.setETag(eTag);
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
                logger.info("Client aborted stream read:\n\tcontent: " + reader);
        }
        catch (ContentIOException e2)
        {
            if (logger.isInfoEnabled())
                logger.info("Client aborted stream read:\n\tcontent: " + reader);
        }
    }

    /**
     * Set attachment header
     * 
     * @param res
     * @param attach
     * @param attachFileName
     */
    protected void setAttachment(WebScriptResponse res, boolean attach, String attachFileName)
    {
        if (attach == true)
        {
            String headerValue = "attachment";
            if (attachFileName != null && attachFileName.length() > 0)
            {
                if (logger.isDebugEnabled())
                    logger.debug("Attaching content using filename: " + attachFileName);
                
                headerValue += "; filename=" + attachFileName;
            }
            
            // set header based on filename - will force a Save As from the browse if it doesn't recognize it
            // this is better than the default response of the browser trying to display the contents
            res.setHeader("Content-Disposition", headerValue);
        }
    }

}
