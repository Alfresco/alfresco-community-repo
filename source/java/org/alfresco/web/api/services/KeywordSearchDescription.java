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

import java.util.Map;

import org.alfresco.web.api.APIRequest;
import org.alfresco.web.api.APIResponse;
import org.alfresco.web.api.APIRequest.HttpMethod;
import org.alfresco.web.api.APIRequest.RequiredAuthentication;


/**
 * Provide OpenSearch Description for an Alfresco Keyword (simple) Search
 *
 * @author davidc
 */
public class KeywordSearchDescription extends APIServiceTemplateImpl
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
     * @see org.alfresco.web.api.APIService#getDefaultFormat()
     */
    public String getDefaultFormat()
    {
        return APIResponse.OPENSEARCH_DESCRIPTION_FORMAT;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.services.APIServiceTemplateImpl#createModel(org.alfresco.web.api.APIRequest, org.alfresco.web.api.APIResponse, java.util.Map)
     */
    @Override
    protected Map<String, Object> createModel(APIRequest req, APIResponse res, Map<String, Object> model)
    {
        return model;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIService#getDescription()
     */
    public String getDescription()
    {
        return "Retrieve the OpenSearch Description for the Alfresco Web Client keyword search";
    }

    /**
     * Simple test that can be executed outside of web context
     */
    public static void main(String[] args)
        throws Exception
    {
        KeywordSearchDescription service = (KeywordSearchDescription)APIServiceImpl.getMethod("web.api.KeywordSearchDescription");
        service.test(APIResponse.OPENSEARCH_DESCRIPTION_FORMAT);
    }
    
}
