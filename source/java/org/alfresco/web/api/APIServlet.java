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
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.web.app.servlet.BaseServlet;


/**
 * Entry point for web based services (REST style)
 * 
 * @author davidc
 */
public class APIServlet extends BaseServlet
{
    private static final long serialVersionUID = 4209892938069597860L;
    
    
    // API Services
    // TODO: Define via configuration
    // TODO: Provide mechanism to construct service specific urls (ideally from template)
    private static Pattern TEXT_SEARCH_DESCRIPTION_URI = Pattern.compile("/search/textsearchdescription.xml");
    private static Pattern SEARCH_URI = Pattern.compile("/search/text");
    private static APIService TEXT_SEARCH_DESCRIPTION_SERVICE;
    private static APIService TEXT_SEARCH_SERVICE;
    
    
    @Override
    public void init() throws ServletException
    {
        super.init();
        
        // TODO: Replace with dispatch mechanism (maybe lazy construct)
        TEXT_SEARCH_DESCRIPTION_SERVICE = new TextSearchDescriptionService();
        TEXT_SEARCH_DESCRIPTION_SERVICE.init(getServletContext());
        TEXT_SEARCH_SERVICE = new TextSearchService();
        TEXT_SEARCH_SERVICE.init(getServletContext());
    }


// TODO:
// - authentication
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
        
        // TODO: Handle authentication - HTTP Auth?
        

        //
        // Execute appropriate service
        //
        // TODO: Replace with configurable dispatch mechanism based on HTTP method & uri.
        // TODO: Handle errors (with appropriate HTTP error responses) 

        APIRequest.HttpMethod method = request.getHttpMethod();
        String uri = request.getPathInfo();

        if (method == APIRequest.HttpMethod.GET && TEXT_SEARCH_DESCRIPTION_URI.matcher(uri).matches())
        {
            TEXT_SEARCH_DESCRIPTION_SERVICE.execute(request, response);
        }
        else if (method == APIRequest.HttpMethod.GET && SEARCH_URI.matcher(uri).matches())
        {
            TEXT_SEARCH_SERVICE.execute(request, response);
        }
        else
        {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
    }

}
