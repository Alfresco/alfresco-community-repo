/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.api.services;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.alfresco.repo.template.ISO8601DateFormatMethod;
import org.alfresco.repo.template.UrlEncodeMethod;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.web.api.APIContextAware;
import org.alfresco.web.api.APIRequest;
import org.alfresco.web.api.APIResponse;
import org.alfresco.web.api.APIService;
import org.springframework.beans.factory.BeanNameAware;


/**
 * Skeleton implementation of an API Service
 *
 * @author davidc
 */
public abstract class APIServiceImpl implements BeanNameAware, APIService, APIContextAware 
{
    private String name;
    private String uri;

    // dependencies
    private ServletContext context;
    private ServiceRegistry serviceRegistry;
    private DescriptorService descriptorService;
    private TemplateService templateService;

    //
    // Initialisation
    //
    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name)
    {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIContextAware#setAPIContext(javax.servlet.ServletContext)
     */
    public void setAPIContext(ServletContext context)
    {
        this.context = context;
    }
    
    /**
     * @param serviceRegistry 
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * @param templateService
     */
    public void setTemplateService(TemplateService templateService)
    {
        this.templateService = templateService;
    }

    /**
     * @param descriptorService
     */
    public void setDescriptorService(DescriptorService descriptorService)
    {
        this.descriptorService = descriptorService;
    }
    
    /**
     * Sets the Http URI
     * 
     * @param uri
     */
    public void setHttpUri(String uri)
    {
        this.uri = uri;
    }
    
    //
    // Service Meta-Data
    //
    
    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIService#getName()
     */
    public String getName()
    {
        return this.name;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIService#getHttpUri()
     */
    public String getHttpUri()
    {
        return this.uri;
    }

    
    //
    // Service Implementation Helpers
    //

    /**
     * @return descriptorService
     */
    protected ServletContext getAPIContext()
    {
        return context;
    }
    
    /**
     * @return descriptorService
     */
    protected DescriptorService getDescriptorService()
    {
        return descriptorService;
    }

    /**
     * @return serviceRegistry
     */
    protected ServiceRegistry getServiceRegistry()
    {
        return serviceRegistry;
    }
    
    /**
     * @return templateService
     */
    protected TemplateService getTemplateService()
    {
        return templateService;
    }
    
    /**
     * Create a basic template model
     * 
     * @param req  api request
     * @param res  api response
     * @return  template model
     */
    protected Map<String, Object> createTemplateModel(APIRequest req, APIResponse res)
    {
        Map<String, Object> model = new HashMap<String, Object>(7, 1.0f);
        model.put("xmldate", new ISO8601DateFormatMethod());
        model.put("urlencode", new UrlEncodeMethod());
        model.put("date", new Date());
        model.put("agent", descriptorService.getServerDescriptor());
        model.put("request", req);
        model.put("response", res);
        return model;
    }

    /**
     * Render a template to the API Response
     * 
     * @param template
     * @param model
     * @param res
     */
    protected void renderTemplate(String template, Map<String, Object> model, APIResponse res)
        throws IOException
    {
        templateService.processTemplateString(null, template, model, res.getWriter());
    }
    
}
