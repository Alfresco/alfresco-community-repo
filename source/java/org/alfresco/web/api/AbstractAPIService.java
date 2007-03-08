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
package org.alfresco.web.api;

import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.template.AbsoluteUrlMethod;
import org.alfresco.repo.template.ISO8601DateFormatMethod;
import org.alfresco.repo.template.UrlEncodeMethod;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.descriptor.DescriptorService;


/**
 * Skeleton implementation of an API Service
 *
 * @author davidc
 */
public abstract class AbstractAPIService implements APIService 
{
    // dependencies
    private APIRegistry apiRegistry;
    private APIDescription apiDescription;
    private ServiceRegistry serviceRegistry;
    private DescriptorService descriptorService;

    //
    // Initialisation
    //
    
    final public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }
    
    final public void setDescriptorService(DescriptorService descriptorService)
    {
        this.descriptorService = descriptorService;
    }

    final public void setDescription(APIDescription apiDescription)
    {
        this.apiDescription = apiDescription;
    }
    
    public void init(APIRegistry apiRegistry)
    {
        this.apiRegistry = apiRegistry;
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIService#getServiceDescription()
     */
    final public APIDescription getDescription()
    {
        return this.apiDescription;
    }

    //
    // Service Implementation Helpers
    //

    final public APIRegistry getAPIRegistry()
    {
        return this.apiRegistry;
    }
    
    final public ServiceRegistry getServiceRegistry()
    {
        return this.serviceRegistry;
    }
    
    final public DescriptorService getDescriptorService()
    {
        return this.descriptorService;
    }
    
    
    //
    // Basic Templating Support
    //
    
    
    /**
     * Create a basic API model
     * 
     * @param req  api request
     * @param res  api response
     * @return  template model
     */
    final protected Map<String, Object> createAPIModel(APIRequest req, APIResponse res)
    {
        Map<String, Object> model = new HashMap<String, Object>(7, 1.0f);
        model.put("xmldate", new ISO8601DateFormatMethod());
        model.put("absurl", new AbsoluteUrlMethod(req.getPath()));
        model.put("urlencode", new UrlEncodeMethod());
        model.put("date", new Date());
        model.put("agent", descriptorService.getServerDescriptor());
        model.put("request", req);
        model.put("response", res);
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
        getAPIRegistry().getTemplateProcessor().process(templatePath, model, writer);
    }
    
    /**
     * Render a template (contents as string)
     * @param template  the template
     * @param model  model
     * @param writer  output writer
     */
    final protected void renderString(String template, Map<String, Object> model, Writer writer)
    {
        getAPIRegistry().getTemplateProcessor().processString(template, model, writer);
    }
        
}
