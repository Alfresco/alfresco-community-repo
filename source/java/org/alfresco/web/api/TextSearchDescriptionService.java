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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.TemplateService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


/**
 * Provide OpenSearch Description for an Alfresco Text (simple) Search
 *
 * @author davidc
 */
public class TextSearchDescriptionService implements APIService
{
    // dependencies
    private TemplateService templateService;
    
    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIService#init(javax.servlet.ServletContext)
     */
    public void init(ServletContext context)
    {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(context);
        templateService = (TemplateService)appContext.getBean(ServiceRegistry.TEMPLATE_SERVICE.getLocalName());
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIService#execute(org.alfresco.web.api.APIRequest, org.alfresco.web.api.APIResponse)
     */
    public void execute(APIRequest req, APIResponse res)
        throws IOException
    {
        // create model for open search template
        Map<String, Object> model = new HashMap<String, Object>(7, 1.0f);
        model.put("request", req);

        // execute template
        res.setContentType(APIResponse.OPEN_SEARCH_DESCRIPTION_TYPE + ";charset=UTF-8");
        templateService.processTemplateString(null, OPEN_SEARCH_DESCRIPTION, model, res.getWriter());
    }

    // TODO: place into accessible file
    private final static String OPEN_SEARCH_DESCRIPTION = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<OpenSearchDescription xmlns=\"http://a9.com/-/spec/opensearch/1.1/\">\n" +
        "  <ShortName>Alfresco Text Search</ShortName>\n" +
        "  <Description>Search all of Alfresco Company Home via text keywords</Description>\n" +
        "  <Url type=\"application/atom+xml\" template=\"${request.servicePath}/search/text?q={searchTerms}&amp;p={startPage?}&amp;format=atom\"/>\n" +
        "  <Url type=\"text/html\" template=\"${request.servicePath}/search/text?q={searchTerms}&amp;p={startPage?}\"/>\n" +
        "</OpenSearchDescription>";
        
}
