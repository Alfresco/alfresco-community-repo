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
package org.alfresco.web.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.ApplicationContext;


/**
 * Registry of Web API Services methods
 * 
 * @author davidc
 */
public class APIServiceMap
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
    public APIServiceMap(ApplicationContext context)
    {
        // locate authentication interceptor
        MethodInterceptor authInterceptor = (MethodInterceptor)context.getBean("web.api.Authentication");
        
        // register all API Services
        // NOTE: An API Service is one registered in Spring which supports the APIService interface
        Map<String, APIService> apiServices = context.getBeansOfType(APIService.class, false, false);
        for (Map.Entry<String, APIService> apiService : apiServices.entrySet())
        {
            APIService service = apiService.getValue();
            APIRequest.HttpMethod method = service.getHttpMethod();
            String httpUri = service.getHttpUri();
            if (httpUri == null || httpUri.length() == 0)
            {
                throw new APIException("Web API Service " + apiService.getKey() + " does not specify a HTTP URI mapping");
            }
            
            if (authInterceptor != null && service.getRequiredAuthentication() != APIRequest.RequiredAuthentication.None)
            {
                // wrap API Service in appropriate interceptors (e.g. authentication)
                ProxyFactory authFactory = new ProxyFactory(service);
                authFactory.addAdvice(authInterceptor);
                service = (APIService)authFactory.getProxy();
            }
            
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
            if (methods.get(i).equals(method) && uris.get(i).equals(uri))
            {
                apiService = services.get(i);
                break;
            }
        }
        
        return apiService;
    }
    
}
