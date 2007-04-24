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

import java.io.Writer;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.jscript.Node;
import org.alfresco.repo.jscript.ScriptableHashMap;
import org.alfresco.repo.template.AbsoluteUrlMethod;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.web.scripts.WebScriptDescription.RequiredAuthentication;
import org.alfresco.web.scripts.WebScriptDescription.RequiredTransaction;


/**
 * Skeleton implementation of a Web Script
 *
 * @author davidc
 */
public abstract class AbstractWebScript implements WebScript 
{
    // dependencies
    private WebScriptContext scriptContext;
    private WebScriptRegistry scriptRegistry;
    private WebScriptDescription description;
    private ServiceRegistry serviceRegistry;
    private DescriptorService descriptorService;

    
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
        Enumeration names = req.getParameterNames();
        while (names.hasMoreElements())
        {
           String name = (String)names.nextElement();
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
    final protected Map<String, Object> createScriptModel(WebScriptRequest req, WebScriptResponse res, Map<String, Object> customModel)
    {
        // create script model
        Map<String, Object> model = new HashMap<String, Object>(7, 1.0f);
        
        // add repository context (only if authenticated and transaction enabled)
        if (getDescription().getRequiredAuthentication() != RequiredAuthentication.none &&
            getDescription().getRequiredTransaction() != RequiredTransaction.none)
        {
            NodeRef companyHome = scriptContext.getCompanyHome();
            if (companyHome != null)
            {
                model.put("companyhome", new Node(scriptContext.getCompanyHome(), serviceRegistry));
            }
            NodeRef person = scriptContext.getPerson();
            if (person != null)
            {
                model.put("person", new Node(person, serviceRegistry));
                model.put("userhome", new Node(scriptContext.getUserHome(person), serviceRegistry));
            }
        }

        // add web script context
        model.put("args", createArgModel(req));
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
            NodeRef companyHome = scriptContext.getCompanyHome();
            if (companyHome != null)
            {
                model.put("companyhome", scriptContext.getCompanyHome());
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
        model.put("server", new ServerModel(descriptorService.getServerDescriptor()));
        
        // add template support
        model.put("absurl", new AbsoluteUrlMethod(req.getServerPath()));
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
        getWebScriptRegistry().getTemplateProcessor().process(templatePath, model, writer);
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
     * Execute a script
     * 
     * @param location  script location
     * @param model  model
     */
    final protected void executeScript(ScriptLocation location, Map<String, Object> model)
    {
        getWebScriptRegistry().getScriptProcessor().executeScript(location, model);
    }
}
