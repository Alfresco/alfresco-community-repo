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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.web.app.servlet.BaseServlet;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


/**
 * Entry point for web based services (REST style)
 * 
 * @author davidc
 */
public class APIServlet extends BaseServlet
{
    private static final long serialVersionUID = 4209892938069597860L;
    
    // API Services
    private APIServiceMap apiServiceMap;
    
    
    @Override
    public void init() throws ServletException
    {
        super.init();

        // Retrieve all web api services and index by http url & http method
        ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        apiServiceMap = new APIServiceMap(context);
    }


// TODO:
// - authentication (as suggested in http://www.xml.com/pub/a/2003/12/17/dive.html)
// - atom
//   - generator
//   - author (authenticated)
//   - icon
// - html
//   - icon
              

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        APIRequest request = new APIRequest(req);
        APIResponse response = new APIResponse(res);
        
        //
        // Execute appropriate service
        //
        // TODO: Handle errors (with appropriate HTTP error responses) 

        APIRequest.HttpMethod method = request.getHttpMethod();
        String uri = request.getPathInfo();

        APIService service = apiServiceMap.get(method, uri);
        if (service != null)
        {
            service.execute(request, response);
        }
        else
        {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            // TODO: add appropriate error detail 
        }
    }

}
