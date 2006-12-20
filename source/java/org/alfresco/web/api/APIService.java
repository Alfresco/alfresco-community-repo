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

import java.io.IOException;

import javax.servlet.ServletContext;

/**
 * API Service
 * 
 * @author davidc
 */
public interface APIService
{

    /**
     * Initialise the Service
     * 
     * @param context
     */
    public void init(ServletContext context);

    /**
     * Gets the name of this service
     * 
     * @return  service name
     */
    public String getName();
    
    /**
     * Gets the required authentication level for execution of this service
     * 
     * @return  the required authentication level 
     */
    public APIRequest.RequiredAuthentication getRequiredAuthentication();

    /**
     * Gets the HTTP method this service is bound to
     * 
     * @return  HTTP method
     */
    public APIRequest.HttpMethod getHttpMethod();

    /**
     * Gets the HTTP uri this service is bound to
     * 
     * @return  HTTP uri
     */
    public String getHttpUri();

    /**
     * Execute service
     * 
     * @param req
     * @param res
     * @throws IOException
     */
    public void execute(APIRequest req, APIResponse res)
        throws IOException;
    
}
