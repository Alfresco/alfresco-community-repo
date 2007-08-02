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
package org.alfresco.web.scripts;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.jscript.ScriptableHashMap;
import org.alfresco.repo.template.AbsoluteUrlMethod;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.web.scripts.WebScriptDescription.RequiredAuthentication;
import org.alfresco.web.scripts.WebScriptDescription.RequiredTransaction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Skeleton implementation of a Web Script
 *
 * @author davidc
 */
public abstract class AbstractWebScript implements WebScript 
{
    // Logger
    private static final Log logger = LogFactory.getLog(AbstractWebScript.class);

    // dependencies
    private WebScriptContext scriptContext;
    private WebScriptRegistry scriptRegistry;
    private WebScriptDescription description;
    private ServiceRegistry serviceRegistry;
    private DescriptorService descriptorService;
    
    // Status Template cache
    private Map<String, StatusTemplate> statusTemplates = new HashMap<String, StatusTemplate>();    
    private ReentrantReadWriteLock statusTemplateLock = new ReentrantReadWriteLock(); 

    
    //
    // Initialisation
    //
    
    /**
     * @param scriptContext
     */
    final public void setScriptContext(WebScriptContext scriptContext)
    {
        this.scriptContext = scriptContext;
    }
    
    /**
     * @param serviceRegistry
     */
    final public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * @param descriptorService
     */
    final public void setDescriptorService(DescriptorService descriptorService)
    {
        this.descriptorService = descriptorService;
    }

    /**
     * Sets the Service Description
     * 
     * @param description
     */
    final public void setDescription(WebScriptDescription description)
    {
        this.description = description;
    }

    /**
     * Initialise Web Script
     *
     * @param scriptRegistry
     */
    public void init(WebScriptRegistry scriptRegistry)
    {
        this.scriptRegistry = scriptRegistry;
        this.statusTemplateLock.writeLock().lock();
        try
        {
            this.statusTemplates.clear();
        }
        finally
        {
            this.statusTemplateLock.writeLock().unlock();
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScript#getDescription()
     */
    final public WebScriptDescription getDescription()
    {
        return this.description;
    }


    //
    // Service Implementation Helpers
    //

    /**
     * Gets the Repository Context
     * 
     * @return  repository context
     */
    final public WebScriptContext getRepositoryContext()
    {
        return this.scriptContext;
    }
    
    /**
     * Gets the Web Script Registry
     *  
     * @return Web Script Registry
     */
    final public WebScriptRegistry getWebScriptRegistry()
    {
        return this.scriptRegistry;
    }

    /**
     * Gets the Alfresco Service Registry
     * 
     * @return service registry
     */
    final public ServiceRegistry getServiceRegistry()
    {
        return this.serviceRegistry;
    }
    
    /**
     * Gets the Alfresco Descriptor
     * 
     * @return descriptor
     */
    final public DescriptorService getDescriptorService()
    {
        return this.descriptorService;
    }
    
    //
    // Scripting Support
    //

    /**
     * Create a map of arguments from Web Script Request
     * 
     * @param req  Web Script Request
     * @return  argument map
     */
    final protected Map<String, String> createArgModel(WebScriptRequest req)
    {
        Map<String, String> args = new ScriptableHashMap<String, String>();
        String[] names = req.getParameterNames();
        for (String name : names)
        {
           args.put(name, req.getParameter(name));
        }
        return args;
    }
    
    /**
     * Create a model for script usage
     *  
     * @param req  web script request
     * @param res  web script response
     * @param customModel  custom model entries
     * 
     * @return  script model
     */
    final protected Map<String, Object> createScriptModel(WebScriptRequest req, WebScriptResponse res,  Map<String, Object> customModel)
    {
        // create script model
        Map<String, Object> model = new HashMap<String, Object>(7, 1.0f);
        
        // add repository context (only if authenticated and transaction enabled)
        if (getDescription().getRequiredAuthentication() != RequiredAuthentication.none &&
            getDescription().getRequiredTransaction() != RequiredTransaction.none)
        {
            NodeRef rootHome = scriptContext.getRootHome();
            if (rootHome != null)
            {
                model.put("roothome", rootHome);
            }
            NodeRef companyHome = scriptContext.getCompanyHome();
            if (companyHome != null)
            {
                model.put("companyhome", companyHome);
            }
            NodeRef person = scriptContext.getPerson();
            if (person != null)
            {
                model.put("person", person);
                model.put("userhome", scriptContext.getUserHome(person));
            }
        }

        // add web script context
        model.put("args", createArgModel(req));
        if (req instanceof WebScriptServletRequest)
        {
            model.put("formdata", new FormData(((WebScriptServletRequest)req).getHttpServletRequest()));
        }
        model.put("guest", req.isGuest());
        model.put("url", new URLModel(req));
        model.put("server", new ServerModel(descriptorService.getServerDescriptor()));

        // add custom model
        if (customModel != null)
        {
            model.putAll(customModel);
        }

        // return model
        return model;
    }
    
    /**
     * Create a model for template usage
     * 
     * @param req  web script request
     * @param res  web script response
     * @param customModel  custom model entries
     *
     * @return  template model
     */
    final protected Map<String, Object> createTemplateModel(WebScriptRequest req, WebScriptResponse res, Map<String, Object> customModel)
    {
        // create template model
        Map<String, Object> model = new HashMap<String, Object>(8, 1.0f);
        
        // add repository context
        if (getDescription().getRequiredAuthentication() != RequiredAuthentication.none &&
            getDescription().getRequiredTransaction() != RequiredTransaction.none)
        {
            NodeRef rootHome = scriptContext.getRootHome();
            if (rootHome != null)
            {
                model.put("roothome", rootHome);
            }            
            NodeRef companyHome = scriptContext.getCompanyHome();
            if (companyHome != null)
            {
                model.put("companyhome", companyHome);
            }
            NodeRef person = scriptContext.getPerson();
            if (person != null)
            {
                model.put("person", person);
                model.put("userhome", scriptContext.getUserHome(person));
            }
        }
        
        // add web script context
        model.put("args", createArgModel(req));
        model.put("guest", req.isGuest());
        model.put("url", new URLModel(req));
        model.put("webscript", getDescription());
        model.put("server", new ServerModel(descriptorService.getServerDescriptor()));
        
        // add template support
        model.put("absurl", new AbsoluteUrlMethod(req.getServerPath()));
        model.put("scripturl", new ScriptUrlMethod(req, res));
        model.put("clienturlfunction", new ClientUrlFunctionMethod(res));
        model.put("date", new Date());
        model.put(TemplateService.KEY_IMAGE_RESOLVER, getWebScriptRegistry().getTemplateImageResolver());
        
        // add custom model
        if (customModel != null)
        {
            model.putAll(customModel);
        }

        return model;
    }

    /**
     * Render a template (identified by path)
     * 
     * @param templatePath  template path
     * @param model  model
     * @param writer  output writer
     */
    final protected void renderTemplate(String templatePath, Map<String, Object> model, Writer writer)
    {
        long start = System.currentTimeMillis();
        getWebScriptRegistry().getTemplateProcessor().process(templatePath, model, writer);
        if (logger.isDebugEnabled())
            logger.debug("Rendered template " + templatePath + " in " + (System.currentTimeMillis() - start) + "ms");
    }
    
    /**
     * Render a template (contents as string)
     * @param template  the template
     * @param model  model
     * @param writer  output writer
     */
    final protected void renderString(String template, Map<String, Object> model, Writer writer)
    {
        getWebScriptRegistry().getTemplateProcessor().processString(template, model, writer);
    }
     
    /**
     * Render an explicit response status template
     * 
     * @param req  web script request
     * @param res  web script response
     * @param status  web script status
     * @param format  format
     * @param model  model
     * @throws IOException
     */
    final protected void sendStatus(WebScriptRequest req, WebScriptResponse res, WebScriptStatus status, String format, Map<String, Object> model)
        throws IOException
    {
        // locate status template
        // NOTE: search order...
        // NOTE: package path is recursed to root package
        //   1) script located <scriptid>.<format>.<status>.ftl
        //   2) script located <scriptid>.<format>.status.ftl
        //   3) package located <scriptpath>/<format>.<status>.ftl
        //   4) package located <scriptpath>/<format>.status.ftl
        //   5) default <status>.ftl
        //   6) default status.ftl

        int statusCode = status.getCode();
        String statusFormat = (format == null) ? "" : format;
        String scriptId = getDescription().getId();
        StatusTemplate template = getStatusTemplate(scriptId, statusCode, statusFormat);

        // render output
        String mimetype = getWebScriptRegistry().getFormatRegistry().getMimeType(req.getAgent(), template.format);
        if (mimetype == null)
        {
            throw new WebScriptException("Web Script format '" + template.format + "' is not registered");
        }
    
        if (logger.isDebugEnabled())
        {
            logger.debug("Force success status header in response: " + req.forceSuccessStatus());
            logger.debug("Sending status " + statusCode + " (Template: " + template.path + ")");
            logger.debug("Rendering response: content type=" + mimetype);
        }
    
        res.reset();
        res.setStatus(req.forceSuccessStatus() ? HttpServletResponse.SC_OK : statusCode);
        res.setContentType(mimetype + ";charset=UTF-8");
        renderTemplate(template.path, model, res.getWriter());
    }

    /**
     * Find status template
     * 
     * Note: This method caches template search results
     * 
     * @param scriptId
     * @param statusCode
     * @param format
     * @return  status template (or null if not found)
     */
    private StatusTemplate getStatusTemplate(String scriptId, int statusCode, String format)
    {
        StatusTemplate statusTemplate = null;
        statusTemplateLock.readLock().lock();

        try
        {
            String key = statusCode + "." + format;
            statusTemplate = statusTemplates.get(key);
            if (statusTemplate == null)
            {
                // Upgrade read lock to write lock
                statusTemplateLock.readLock().unlock();
                statusTemplateLock.writeLock().lock();

                try
                {
                    // Check again
                    statusTemplate = statusTemplates.get(key);
                    if (statusTemplate == null)
                    {
                        // Locate template in web script store
                        statusTemplate = getScriptStatusTemplate(scriptId, statusCode, format);
                        if (statusTemplate == null)
                        {
                            WebScriptPath path = getWebScriptRegistry().getPackage(Path.concatPath("/", getDescription().getScriptPath()));
                            statusTemplate = getPackageStatusTemplate(path, statusCode, format);
                            if (statusTemplate == null)
                            {
                                statusTemplate = getDefaultStatusTemplate(statusCode);
                            }
                        }
                        
                        if (logger.isDebugEnabled())
                            logger.debug("Caching template " + statusTemplate.path + " for web script " + scriptId + " and status " + statusCode + " (format: " + format + ")");
                        
                        statusTemplates.put(key, statusTemplate);
                    }
                }
                finally
                {
                    // Downgrade lock to read
                    statusTemplateLock.readLock().lock();
                    statusTemplateLock.writeLock().unlock();
                }
            }
            return statusTemplate;
        }
        finally
        {
            statusTemplateLock.readLock().unlock();
        }
    }
    
    /**
     * Find a script specific status template
     * 
     * @param scriptId
     * @param statusCode
     * @param format
     * @return  status template (or null, if not found)
     */
    private StatusTemplate getScriptStatusTemplate(String scriptId, int statusCode, String format)
    {
        String path = scriptId + "." + format + "." + statusCode + ".ftl";
        if (getWebScriptRegistry().getTemplateProcessor().hasTemplate(path))
        {
            return new StatusTemplate(path, format);
        }
        path = scriptId + "." + format + ".status.ftl";
        if (getWebScriptRegistry().getTemplateProcessor().hasTemplate(path))
        {
            return new StatusTemplate(path, format);
        }
        return null;
    }

    /**
     * Find a package specific status template
     * 
     * @param scriptPath
     * @param statusCode
     * @param format
     * @return  status template (or null, if not found)
     */
    private StatusTemplate getPackageStatusTemplate(WebScriptPath scriptPath, int statusCode, String format)
    {
        while(scriptPath != null)
        {
            String path = Path.concatPath(scriptPath.getPath(), format + "." + statusCode + ".ftl");
            if (getWebScriptRegistry().getTemplateProcessor().hasTemplate(path))
            {
                return new StatusTemplate(path, format);
            }
            path = Path.concatPath(scriptPath.getPath(), format + ".status.ftl");
            if (getWebScriptRegistry().getTemplateProcessor().hasTemplate(path))
            {
                return new StatusTemplate(path, format);
            }
            scriptPath = scriptPath.getParent();
        }
        return null;
    }
    
    /**
     * Find default status template
     * 
     * @param statusCode
     * @return  status template
     */
    private StatusTemplate getDefaultStatusTemplate(int statusCode)
    {
        String path = statusCode + ".ftl";
        if (getWebScriptRegistry().getTemplateProcessor().hasTemplate(path))
        {
            return new StatusTemplate(path, WebScriptResponse.HTML_FORMAT);
        }
        path = "status.ftl";
        if (getWebScriptRegistry().getTemplateProcessor().hasTemplate(path))
        {
            return new StatusTemplate(path, WebScriptResponse.HTML_FORMAT);
        }
        throw new WebScriptException("Default status template /status.ftl could not be found");
    }
        
    /**
     * Execute a script
     * 
     * @param location  script location
     * @param model  model
     */
    final protected void executeScript(ScriptLocation location, Map<String, Object> model)
    {
        long start = System.currentTimeMillis();
        getWebScriptRegistry().getScriptProcessor().executeScript(location, model);
        if (logger.isDebugEnabled())
            logger.debug("Executed script " + location.toString() + " in " + (System.currentTimeMillis() - start) + "ms");
    }
    
    /**
     * Helper to convert a Web Script Request URL to a Node Ref
     * 
     * 1) Node - {store_type}/{store_id}/{node_id} 
     *
     *    Resolve to node via its Node Reference.
     *     
     * 2) Path - {store_type}/{store_id}/{path}
     * 
     *    Resolve to node via its display path.
     *    
     * 3) QName - {store_type}/{store_id}/{child_qname_path}  TODO: Implement
     * 
     *    Resolve to node via its child qname path.
     * 
     * @param  referenceType  one of node, path or qname
     * @return  reference  array of reference segments (as described above for each reference type)
     */
    protected NodeRef findNodeRef(String referenceType, String[] reference)
    {
        NodeRef nodeRef = null;
        
        // construct store reference
        if (reference.length < 3)
        {
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Reference " + Arrays.toString(reference) + " is not properly formed");
        }
        StoreRef storeRef = new StoreRef(reference[0], reference[1]);
        NodeService nodeService = serviceRegistry.getNodeService();
        if (nodeService.exists(storeRef))
        {
            if (referenceType.equals("node"))
            {
                NodeRef urlRef = new NodeRef(storeRef, reference[2]);
                if (nodeService.exists(urlRef))
                {
                    nodeRef = urlRef;
                }
            }
            
            else if (referenceType.equals("path"))
            {
                // TODO: Allow a root path to be specified - for now, hard-code to Company Home
//                NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
                NodeRef rootNodeRef = getRepositoryContext().getCompanyHome();
                if (reference.length == 3)
                {
                    nodeRef = rootNodeRef;
                }
                else
                {
                    String[] path = new String[reference.length - /*2*/3];
                    System.arraycopy(reference, /*2*/3, path, 0, path.length);
                    
                    try
                    {
                        FileFolderService ffService = serviceRegistry.getFileFolderService();
                        FileInfo fileInfo = ffService.resolveNamePath(rootNodeRef, Arrays.asList(path));
                        nodeRef = fileInfo.getNodeRef();
                    }
                    catch (FileNotFoundException e)
                    {
                        // NOTE: return null node ref
                    }
                }
            }
            
            else
            {
                // TODO: Implement 'qname' style
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Web Script Node URL specified an invalid reference style of '" + referenceType + "'");
            }
        }
        
        return nodeRef;
    }
    
    
    /**
     * Status Template
     */
    private class StatusTemplate
    {
        /**
         * Construct
         * 
         * @param path
         * @param format
         */
        private StatusTemplate(String path, String format)
        {
            this.path = path;
            this.format = format;
        }
        
        private String path;
        private String format;
    }
    
}
