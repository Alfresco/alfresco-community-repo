/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigService;
import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.web.api.framework.APIRequest;
import org.alfresco.web.api.framework.APIResponse;
import org.alfresco.web.api.framework.ScriptedAPIService;
import org.alfresco.web.config.OpenSearchConfigElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * List of (server-side) registered Search Engines
 * 
 * @author davidc
 */
public class SearchEngines extends ScriptedAPIService
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

    @Override
    protected Map<String, Object> executeImpl(APIRequest req, APIResponse res)
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