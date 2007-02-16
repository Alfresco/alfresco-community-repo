/*
 * Copyright (C) 2005 Alfresco, Inc.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.RegexpMethodPointcutAdvisor;
import org.springframework.context.ApplicationContext;


/**
 * Registry of Web API Services methods
 * 
 * @author davidc
 */
public class APIServiceRegistry
{
    // TODO: Support different kinds of uri resolution (e.g. regex:/search/.*)
    
    private List<APIRequest.HttpMethod> methods = new ArrayList<APIRequest.HttpMethod>();
    private List<String> uris = new ArrayList<String>();
    private List<APIService> services = new ArrayList<APIService>();
    
    
    /**
     * Construct list of API Services
     * 
     * @param context
     */
    public APIServiceRegistry(ApplicationContext appContext)
    {
        // retrieve service authenticator
        MethodInterceptor authenticator = (MethodInterceptor)appContext.getBean("web.api.Authenticator");
        MethodInterceptor serviceLogger = (MethodInterceptor)appContext.getBean("web.api.ServiceLogger");

        // register all API Services
        // NOTE: An API Service is one registered in Spring which is of type APIServiceImpl
        Map<String, APIService> apiServices = appContext.getBeansOfType(APIService.class, false, false);
        for (Map.Entry<String, APIService> apiService : apiServices.entrySet())
        {
            // retrieve service
            APIService service = apiService.getValue();
            
            // retrieve http method
            APIRequest.HttpMethod method = service.getHttpMethod();
            String httpUri = service.getHttpUri();
            if (httpUri == null || httpUri.length() == 0)
            {
                throw new APIException("Web API Service " + apiService.getKey() + " does not specify a HTTP URI mapping");
            }
            
            // wrap API Service in appropriate interceptors (e.g. authentication)
            if (serviceLogger != null && authenticator != null)
            {
                ProxyFactory authFactory = new ProxyFactory((APIService)service);

                // authentication
                if (service.getRequiredAuthentication() != APIRequest.RequiredAuthentication.None)
                {
                    if (authenticator == null)
                    {
                        throw new APIException("Web API Authenticator not specified");
                    }
                    RegexpMethodPointcutAdvisor advisor = new RegexpMethodPointcutAdvisor(".*execute", authenticator);
                    authFactory.addAdvisor(advisor);
                }

                // logging
                if (serviceLogger != null)
                {
                    RegexpMethodPointcutAdvisor advisor = new RegexpMethodPointcutAdvisor(".*execute", serviceLogger);
                    authFactory.addAdvisor(advisor);
                }
                
                service = (APIService)authFactory.getProxy();
            }

            // register service
            methods.add(method);
            uris.add(httpUri);
            services.add(service);
        }
    }
    
    
    /**
     * Gets an API Service given an HTTP Method and URI
     * 
     * @param method
     * @param uri
     * @return
     */
    public APIService get(APIRequest.HttpMethod method, String uri)
    {
        APIService apiService = null;

        // TODO: Replace with more efficient approach
        for (int i = 0; i < services.size(); i++)
        {
            if (methods.get(i).equals(method) && uri.startsWith(uris.get(i)))
            {
                apiService = services.get(i);
                break;
            }
        }
        
        return apiService;
    }
    
}
