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
package org.alfresco.web.api.services;

import java.util.Collection;
import java.util.Map;

import org.alfresco.web.api.APIRequest;
import org.alfresco.web.api.APIResponse;
import org.alfresco.web.api.APIService;
import org.alfresco.web.api.APIRequest.HttpMethod;
import org.alfresco.web.api.APIRequest.RequiredAuthentication;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * Retrieves the list of available Web APIs
 * 
 * @author davidc
 */
public class Services extends APIServiceTemplateImpl implements ApplicationContextAware
{
    private ApplicationContext applicationContext;

    
    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIService#getDescription()
     */
    public String getDescription()
    {
        return "Retrieve the list of available Alfresco Web APIs";
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIService#getRequiredAuthentication()
     */
    public RequiredAuthentication getRequiredAuthentication()
    {
        return RequiredAuthentication.None;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIService#getHttpMethod()
     */
    public HttpMethod getHttpMethod()
    {
        return HttpMethod.GET;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIService#getDefaultFormat()
     */
    public String getDefaultFormat()
    {
        return APIResponse.HTML_FORMAT;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.services.APIServiceTemplateImpl#createModel(org.alfresco.web.api.APIRequest, org.alfresco.web.api.APIResponse, java.util.Map)
     */
    @Override
    protected Map<String, Object> createModel(APIRequest req, APIResponse res, Map<String, Object> model)
    {
        model.put("services", getServices());
        return model;
    }

    /**
     * Gets the collection of API Services
     * 
     * @return  api services
     */
    private Collection<APIService> getServices()
    {
        Map<String, APIService> services = applicationContext.getBeansOfType(APIService.class, false, false);
        return services.values();
    }
    
    
    /**
     * Simple test that can be executed outside of web context
     */
    public static void main(String[] args)
        throws Exception
    {
        Services service = (Services)APIServiceImpl.getMethod("web.api.Services");
        service.test(APIResponse.HTML_FORMAT);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.services.APIServiceImpl#createTestModel()
     */
    @Override
    protected Map<String, Object> createTestModel()
    {
        Map<String, Object> model = super.createTestModel();
        model.put("services", getServices());
        return model;
    }
    
}
