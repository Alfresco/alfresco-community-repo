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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;


/**
 * API Service Request
 * 
 * @author davidc
 */
public class APIRequest extends HttpServletRequestWrapper
{

    /**
     * Enumerartion of HTTP Methods 
     */
    public enum HttpMethod
    {
        GET;
        // TODO: Complete list...
    }

    
    /**
     * Construct
     * 
     * @param req
     */
    public APIRequest(HttpServletRequest req)
    {
        super(req);
    }

    /**
     * Gets the HTTP Method
     * 
     * @return  Http Method
     */
    public HttpMethod getHttpMethod()
    {
        String method = getMethod().trim().toUpperCase();
        return HttpMethod.valueOf(method);
    }

    /**
     * Gets the Alfresco Context URL
     *  
     * @return  context url  e.g. http://localhost:port/alfresco
     */
    public String getPath()
    {
        return getScheme() + "://" + getServerName() + ":" + getServerPort() + getContextPath();
    }

    /**
     * Gets the Alfresco Service URL
     * 
     * @return  service url  e.g. http://localhost:port/alfresco/service
     */
    public String getServicePath()
    {
        return getPath() + getServletPath();
    }
    
}
