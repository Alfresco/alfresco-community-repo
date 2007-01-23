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
