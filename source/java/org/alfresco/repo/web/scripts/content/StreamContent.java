/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
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

/**
 * Web script 'type' that can be used when the binary data of a content property needs to be streamed back to the client
 * as the result of executing the web script.
 * 
 * Many of these methods have been moved into the ContentStreamer class so they can be reused by other webscripts.
 * 
 * @author Roy Wetherall
 */
public class StreamContent extends AbstractWebScript
{
    // Logger
    private static final Log logger = LogFactory.getLog(StreamContent.class);
    
    /** Services */
    protected PermissionService permissionService;
    protected NodeService nodeService;
    protected MimetypeService mimetypeService;
    protected ContentStreamer delegate;
    protected Repository repository;
    
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
     * @param delegate
     */
    public void setDelegate(ContentStreamer delegate)
    {
        this.delegate = delegate;
    }
    
    /**
     * @param repository
     */
    public void setRepository(Repository repository)
    {
        this.repository = repository;
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
                        throw new WebScriptException(
                                "The content node was not specified so the content cannot be streamed to the client: " +
                                executeScript.getContent().getPathDescription());
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
                    delegate.streamContent(req, res, nodeRef, propertyQName, attach, null, model);
                }
                else
                {
                    // Stream the content
                    delegate.streamContent(req, res, contentPath, attach, model);
                }
            }
        }
        catch(Throwable e)
        {
            throw createStatusException(e, req, res);
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
        delegate.setAttachment(null, res, attach, attachFileName);
    }
    
    /**
     * Streams content back to client from a given File. The Last-Modified header will reflect the
     * given file's modification timestamp.
     * 
     * @param req               The request
     * @param res               The response
     * @param file              The file whose content is to be streamed.
     * @throws IOException
     */
    protected void streamContent(WebScriptRequest req, WebScriptResponse res, File file) throws IOException {
        streamContent(req, res, file, false, null, null);
    }
    
    /**
     * Streams content back to client from a given File. The Last-Modified header will reflect the
     * given file's modification timestamp.
     * 
     * @param req               The request
     * @param res               The response
     * @param file              The file whose content is to be streamed.
     * @param attach            Indicates whether the content should be streamed as an attachment or not
     * @param attachFileName    Optional file name to use when attach is <code>true</code>
     * @throws IOException
     */
    protected void streamContent(WebScriptRequest req,
                                 WebScriptResponse res, 
                                 File file, 
                                 boolean attach,
                                 String attachFileName,
                                 Map<String, Object> model) throws IOException
    {
        delegate.streamContent(req, res, file, null, attach, attachFileName, model);
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
    protected void streamContent(WebScriptRequest req, 
                                 WebScriptResponse res, 
                                 NodeRef nodeRef, 
                                 QName propertyQName,
                                 boolean attach, 
                                 String attachFileName,
                                 Map<String, Object> model) throws IOException
     {
        delegate.streamContent(req, res, nodeRef, propertyQName, attach, attachFileName, model);
     }
    
    /**
     * Stream content implementation
     * 
     * @param req               The request
     * @param res               The response
     * @param reader            The reader
     * @param nodeRef           The content nodeRef if applicable
     * @param propertyQName     The content property if applicable
     * @param attach            Indicates whether the content should be streamed as an attachment or not
     * @param modified          Modified date of content
     * @param eTag              ETag to use
     * @param attachFileName    Optional file name to use when attach is <code>true</code>
     * @throws IOException
     */
    protected void streamContentImpl(WebScriptRequest req, 
                                    WebScriptResponse res, 
                                    ContentReader reader, 
                                    NodeRef nodeRef,
                                    QName propertyQName,
                                    boolean attach,
                                    Date modified, 
                                    String eTag, 
                                    String attachFileName, 
                                    Map<String, Object> model) throws IOException
    {
        delegate.streamContentImpl(req, res, reader, nodeRef, propertyQName, attach, modified, eTag, attachFileName, model);
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

    protected ObjectReference createObjectReferenceFromUrl(Map<String, String> args, Map<String, String> templateArgs)
    {
        String objectId = args.get("noderef");
        if (objectId != null)
        {
            return new ObjectReference(objectId);
        }
        
        StoreRef storeRef = null;
        String store_type = templateArgs.get("store_type");
        String store_id = templateArgs.get("store_id");
        if (store_type != null && store_id != null)
        {
            storeRef = new StoreRef(store_type, store_id);
        }
        
        String id = templateArgs.get("id");
        if (storeRef != null && id != null)
        {
            return new ObjectReference(storeRef, id);
        }
        
        String nodepath = templateArgs.get("nodepath");
        if (nodepath == null)
        {
            nodepath = args.get("nodepath");
        }
        if (storeRef != null && nodepath != null)
        {
            return new ObjectReference(storeRef, nodepath.split("/"));
        }
        
        return null;
    }
    
    
    class ObjectReference
    {
        private NodeRef ref;
        
        ObjectReference(String nodeRef)
        {
            this.ref = new NodeRef(nodeRef);
        }
        
        ObjectReference(StoreRef ref, String id)
        {
            if (id.indexOf('/') != -1)
            {
                id = id.substring(0, id.indexOf('/'));
            }
            this.ref = new NodeRef(ref, id);
        }
        
        ObjectReference(StoreRef ref, String[] path)
        {
            String[] reference = new String[path.length + 2];
            reference[0] = ref.getProtocol();
            reference[1] = ref.getIdentifier();
            System.arraycopy(path, 0, reference, 2, path.length);
            this.ref = repository.findNodeRef("path", reference);
        }
        
        public NodeRef getNodeRef()
        {
            return this.ref;
        }
        
        @Override
        public String toString()
        {
            return ref != null ? ref.toString() : super.toString();
        }
    }
}
