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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigService;
import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.web.api.APIRequest;
import org.alfresco.web.api.APIResponse;
import org.alfresco.web.api.APIRequest.HttpMethod;
import org.alfresco.web.api.APIRequest.RequiredAuthentication;
import org.alfresco.web.config.OpenSearchConfigElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * List of (server-side) registered Search Engines
 * 
 * @author davidc
 */
public class SearchEngines extends APIServiceTemplateImpl
{
    // url argument values
    public static final String URL_ARG_DESCRIPTION = "description";
    public static final String URL_ARG_TEMPLATE = "template";
    public static final String URL_ARG_ALL = "all";
    
    // Logger
    private static final Log logger = LogFactory.getLog(SearchEngines.class);

    // dependencies
    protected ConfigService configService;

    /**
     * @param configService
     */
    public void setConfigService(ConfigService configService)
    {
        this.configService = configService;
    }

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
        return APIResponse.HTML_FORMAT;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIService#getDescription()
     */
    public String getDescription()
    {
        return "Retrieve a list of (server-side) registered search engines";
    }
    
    @Override
    protected Map<String, Object> createModel(APIRequest req, APIResponse res, Map<String, Object> model)
    {
        String urlType = req.getParameter("type");
        if (urlType == null || urlType.length() == 0)
        {
            urlType = URL_ARG_DESCRIPTION;
        }
        else if (!urlType.equals(URL_ARG_DESCRIPTION) && !urlType.equals(URL_ARG_TEMPLATE) && !urlType.equals(URL_ARG_ALL))
        {
            urlType = URL_ARG_DESCRIPTION;
        }
        
        //
        // retrieve open search engines configuration
        //

        Set<UrlTemplate> urls = getUrls(urlType);
        model.put("urltype", urlType);
        model.put("engines", urls);
        return model;
    }
    
    /**
     * Retrieve registered search engines
     * 
     * @return  set of search engines
     */
    private Set<UrlTemplate> getUrls(String urlType)
    {
        if (logger.isDebugEnabled())
            logger.debug("Search Engine parameters: urltype=" + urlType);

        Set<UrlTemplate> urls = new HashSet<UrlTemplate>();
        Config config = configService.getConfig("OpenSearch");

        OpenSearchConfigElement searchConfig = (OpenSearchConfigElement)config.getConfigElement(OpenSearchConfigElement.CONFIG_ELEMENT_ID);
        for (OpenSearchConfigElement.EngineConfig engineConfig : searchConfig.getEngines())
        {
            Map<String, String> engineUrls = engineConfig.getUrls();
            for (Map.Entry<String, String> engineUrl : engineUrls.entrySet())
            {
                String type = engineUrl.getKey();
                String url = engineUrl.getValue();
                
                if ((urlType.equals(URL_ARG_ALL)) ||
                    (urlType.equals(URL_ARG_DESCRIPTION) && type.equals(MimetypeMap.MIMETYPE_OPENSEARCH_DESCRIPTION)) ||
                    (urlType.equals(URL_ARG_TEMPLATE) && !type.equals(MimetypeMap.MIMETYPE_OPENSEARCH_DESCRIPTION)))
                {
                    String label = engineConfig.getLabel();
                    String labelId = engineConfig.getLabelId();
                    if (labelId != null && labelId.length() > 0)
                    {
                        String i18nLabel = I18NUtil.getMessage(labelId); 
                        label = (i18nLabel == null) ? "$$" + labelId + "$$" : i18nLabel;
                    }
                    urls.add(new UrlTemplate(label, type, url));
                }

                // TODO: Extract URL templates from OpenSearch description
                else if (urlType.equals(URL_ARG_TEMPLATE) && 
                         type.equals(MimetypeMap.MIMETYPE_OPENSEARCH_DESCRIPTION))
                {            
                }
            }
        }
        
        if (logger.isDebugEnabled())
            logger.debug("Retrieved " + urls.size() + " engine registrations");
        
        return urls;
    }

    /**
     * Model object for representing a registered search engine
     */
    public static class UrlTemplate
    {
        private String type;
        private String label;
        private String url;
        private UrlTemplate engine;
        
        public UrlTemplate(String label, String type, String url)
        {
            this.label = label;
            this.type = type;
            this.url = url;
            this.engine = null;
        }

        public UrlTemplate(String label, String type, String url, UrlTemplate engine)
        {
            this(label, type, url);
            this.engine = engine;
        }

        public String getLabel()
        {
            return label;
        }
        
        public String getType()
        {
            return type;
        }
        
        public String getUrl()
        {
            return url;
        }
        
        public String getUrlType()
        {
            return (type.equals(MimetypeMap.MIMETYPE_OPENSEARCH_DESCRIPTION) ? "description" : "template");
        }

        public UrlTemplate getEngine()
        {
            return engine;
        }
    }
    
        
    /**
     * Simple test that can be executed outside of web context
     */
    public static void main(String[] args)
        throws Exception
    {
        SearchEngines service = (SearchEngines)APIServiceImpl.getMethod("web.api.SearchEngines");
        service.test(APIResponse.ATOM_FORMAT);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.api.services.APIServiceImpl#createTestModel()
     */
    @Override
    protected Map<String, Object> createTestModel()
    {
        Map<String, Object> model = super.createTestModel();
        Set<UrlTemplate> urls = getUrls(URL_ARG_ALL);
        model.put("urltype", "template");
        model.put("engines", urls);
        return model;
    }

}