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
import java.util.Map;

import org.alfresco.web.api.APIRequest;
import org.alfresco.web.api.APIResponse;
import org.alfresco.web.api.APIRequest.HttpMethod;
import org.alfresco.web.api.APIRequest.RequiredAuthentication;


/**
 * Provide OpenSearch Description for an Alfresco Text (simple) Search
 *
 * @author davidc
 */
public class TextSearchDescription extends APIServiceImpl
{
    
    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIService#getRequiredAuthentication()
     */
    public RequiredAuthentication getRequiredAuthentication()
    {
        return APIRequest.RequiredAuthentication.None;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIService#getHttpMethod()
     */
    public HttpMethod getHttpMethod()
    {
        return APIRequest.HttpMethod.GET;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIService#execute(org.alfresco.web.api.APIRequest, org.alfresco.web.api.APIResponse)
     */
    public void execute(APIRequest req, APIResponse res)
        throws IOException
    {
        Map<String, Object> model = createTemplateModel(req, res);
        res.setContentType(APIResponse.OPEN_SEARCH_DESCRIPTION_TYPE + ";charset=UTF-8");
        renderTemplate(OPEN_SEARCH_DESCRIPTION, model, res);
    }

    // TODO: place into accessible file
    private final static String OPEN_SEARCH_DESCRIPTION = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<OpenSearchDescription xmlns=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns:alf=\"http://www.alfresco.org\">\n" +
        "  <ShortName>Alfresco Text Search</ShortName>\n" +
        "  <LongName>Alfresco ${agent.edition} Text Search ${agent.version}</LongName>\n" +
        "  <Description>Search Alfresco \"company home\" using text keywords</Description>\n" +
        "  <Url type=\"application/atom+xml\" template=\"${request.servicePath}/search/text?q={searchTerms}&amp;p={startPage?}&amp;c={count?}&amp;l={language?}&amp;guest={alf:guest?}&amp;format=atom\"/>\n" +
        "  <Url type=\"text/html\" template=\"${request.servicePath}/search/text?q={searchTerms}&amp;p={startPage?}&amp;c={count?}&amp;l={language?}&amp;guest={alf:guest?}\"/>\n" +
        "  <Image height=\"16\" width=\"16\" type=\"image/x-icon\">${request.path}/images/logo/AlfrescoLogo16.ico</Image>\n" +
        "</OpenSearchDescription>";

}
