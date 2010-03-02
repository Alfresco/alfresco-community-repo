/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.web.scripts.bean;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.web.scripts.config.OpenSearchConfigElement;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * List of (server-side) registered Search Engines
 * 
 * @author davidc
 */
public class SearchEngines extends DeclarativeWebScript
{
    // url argument values
    public static final String URL_ARG_DESCRIPTION = "description";
    public static final String URL_ARG_TEMPLATE = "template";
    public static final String URL_ARG_ALL = "all";
    
    // Logger
    private static final Log logger = LogFactory.getLog(SearchEngines.class);

    // dependencies
    protected ConfigService configService;
    protected SearchProxy searchProxy;

    /**
     * @param configService
     */
    public void setConfigService(ConfigService configService)
    {
        this.configService = configService;
    }

    /**
     * @param searchProxy
     */
    public void setSearchProxy(SearchProxy searchProxy)
    {
        this.searchProxy = searchProxy;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    @SuppressWarnings("deprecation")
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
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
        Map<String, Object> model = new HashMap<String, Object>(7, 1.0f);
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
                String url = searchProxy.createUrl(engineConfig, type);
                
                if ((urlType.equals(URL_ARG_ALL)) ||
                    (urlType.equals(URL_ARG_DESCRIPTION) && type.equals(MimetypeMap.MIMETYPE_OPENSEARCH_DESCRIPTION)) ||
                    (urlType.equals(URL_ARG_TEMPLATE) && !type.equals(MimetypeMap.MIMETYPE_OPENSEARCH_DESCRIPTION)))
                {
                    String label = engineConfig.getLabel();
                    String labelId = engineConfig.getLabelId();
                    if (labelId != null && labelId.length() > 0)
                    {
                        String i18nLabel = I18NUtil.getMessage(labelId);
                        if (i18nLabel == null && label == null)
                        {
                            label = (i18nLabel == null) ? "$$" + labelId + "$$" : i18nLabel;
                        }
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
            
}