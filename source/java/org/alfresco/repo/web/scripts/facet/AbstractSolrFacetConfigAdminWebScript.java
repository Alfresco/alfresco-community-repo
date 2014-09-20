/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

package org.alfresco.repo.web.scripts.facet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.search.impl.solr.facet.SolrFacetModel;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetProperties.CustomProperties;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is an abstract base class for the various web script controllers
 * in the SolrFacetService.
 * 
 * @author Jamal Kaabi-Mofrad
 */
public abstract class AbstractSolrFacetConfigAdminWebScript extends DeclarativeWebScript
{
    private static final Log logger = LogFactory.getLog(AbstractSolrFacetConfigAdminWebScript.class);
    
    protected static final String PARAM_FILTER_ID = "filterID";
    protected static final String PARAM_FACET_QNAME = "facetQName";
    protected static final String PARAM_DISPLAY_NAME = "displayName";
    protected static final String PARAM_DISPLAY_CONTROL = "displayControl";
    protected static final String PARAM_MAX_FILTERS = "maxFilters";
    protected static final String PARAM_HIT_THRESHOLD = "hitThreshold";
    protected static final String PARAM_MIN_FILTER_VALUE_LENGTH = "minFilterValueLength";
    protected static final String PARAM_SORT_BY = "sortBy";
    protected static final String PARAM_SCOPE = "scope";
    protected static final String PARAM_SCOPED_SITES = "scopedSites";
    protected static final String PARAM_INDEX = "index";
    protected static final String PARAM_IS_ENABLED = "isEnabled";
    protected static final String PARAM_CUSTOM_PROPERTIES = "customProperties";
    protected static final String CUSTOM_PARAM_NAME = "name";
    protected static final String CUSTOM_PARAM_VALUE = "value";

    // The pattern is equivalent to the pattern defined in the forms-runtime.js
    protected static final Pattern FILTER_ID_PATTERN = Pattern.compile("([\"\\*\\\\\\>\\<\\?\\/\\:\\|]+)|([\\.]?[\\.]+$)");

    protected SolrFacetService facetService;
    protected NamespaceService namespaceService;

    /**
     * @param facetService the facetService to set
     */
    public void setFacetService(SolrFacetService facetService)
    {
        this.facetService = facetService;
    }

    /**
     * @param namespaceService the namespaceService to set
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    @Override
    protected Map<String, Object> executeImpl(final WebScriptRequest req, final Status status, final Cache cache)
    {
        validateCurrentUser();

        return unprotectedExecuteImpl(req, status, cache);
    }

    protected void validateCurrentUser()
    {
        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        // check the current user access rights
        if (!facetService.isSearchAdmin(currentUser))
        {
            throw new WebScriptException(HttpServletResponse.SC_FORBIDDEN, "Access denied.");
        }
    }

    protected <T> T getValue(Class<T> clazz, Object value, T defaultValue) throws JSONException
    {
        if (JSONObject.NULL.equals(value))
        {
            return defaultValue;
        }

        try
        {
            return clazz.cast(value);
        }
        catch (Exception ex)
        {
            throw new JSONException("JSONObject[" + value +"] is not an instance of [" + clazz.getName() +"]");
        }
    }

    protected Set<CustomProperties> getCustomProperties(JSONObject customPropsJsonObj) throws JSONException
    {
        if (customPropsJsonObj == null)
        {
            return null;
        }
        JSONArray keys = customPropsJsonObj.names();
        if (keys == null)
        {
            return Collections.emptySet();
        }

        Set<CustomProperties> customProps = new HashSet<>(keys.length());
        for (int i = 0, length = keys.length(); i < length; i++)
        {
            JSONObject jsonObj = customPropsJsonObj.getJSONObject((String) keys.get(i));

            QName name = resolveToQName(getValue(String.class, jsonObj.opt(CUSTOM_PARAM_NAME), null));
            validateMandatoryCustomProps(name, CUSTOM_PARAM_NAME);
            
            Serializable value = null;
            Object customPropValue = jsonObj.opt(CUSTOM_PARAM_VALUE);
            validateMandatoryCustomProps(customPropValue, CUSTOM_PARAM_VALUE);
            
            if(customPropValue instanceof JSONArray)
            {
                JSONArray array = (JSONArray) customPropValue;
                ArrayList<Serializable> list = new ArrayList<>(array.length());
                for(int j = 0; j < array.length(); j++)
                {
                    list.add(getSerializableValue(array.get(j)));
                }
                value = list;
            }
            else
            {
                value = getSerializableValue(customPropValue);
            }
            
           customProps.add(new CustomProperties(name, value));
        }

        if (logger.isDebugEnabled() && customProps.size() > 0)
        {
            logger.debug("Processed custom properties:" + customProps);
        }

        return customProps;
    }

    protected Set<String> getScopedSites(JSONArray scopedSitesJsonArray) throws JSONException
    {
        if (scopedSitesJsonArray == null)
        {
            return null;
        }

        Set<String> scopedSites = new HashSet<String>(scopedSitesJsonArray.length());
        for (int i = 0, length = scopedSitesJsonArray.length(); i < length; i++)
        {
            String site = scopedSitesJsonArray.getString(i);
            scopedSites.add(site);
        }
        return scopedSites;
    }

    private void validateMandatoryCustomProps(Object obj, String paramName) throws JSONException
    {
        if (obj == null)
        {
            throw new JSONException("Invalid JSONObject in the Custom Properties JSON. [" + paramName + "] cannot be null.");
        }

    }

    protected void validateFilterID(String filterID)
    {
        Matcher matcher = FILTER_ID_PATTERN.matcher(filterID);
        if (matcher.find())
        {
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid Filter Id. The characters \" * \\ < > ? / : | are not allowed. The Filter Id cannot end with a dot.");
        }
    }

    private Serializable getSerializableValue(Object object) throws JSONException
    {
        if (!(object instanceof Serializable))
        {
            throw new JSONException("Invalid value in the Custom Properties JSON. [" + object + "] must be an instance of Serializable.");
        }
        return (Serializable) object;
    }

    private QName resolveToQName(String qnameStr) throws JSONException
    {
        QName typeQName = null;
        if (qnameStr == null)
        {
            return typeQName;
        }
        if(qnameStr.charAt(0) == QName.NAMESPACE_BEGIN && qnameStr.indexOf("solrfacetcustomproperty") < 0)
        {
            throw new JSONException("Invalid name in the Custom Properties JSON. Namespace URL must be [" + SolrFacetModel.SOLR_FACET_CUSTOM_PROPERTY_URL + "]");
        }
        else if(qnameStr.charAt(0) == QName.NAMESPACE_BEGIN)
        {
            typeQName = QName.createQName(qnameStr);
        }
        else
        {
            typeQName = QName.createQName(SolrFacetModel.SOLR_FACET_CUSTOM_PROPERTY_URL, qnameStr);
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("Resolved facet's custom property name [" + qnameStr + "] into [" + typeQName + "]");System.out.println("QQQQQQQQQQQQQQQQQQQQQQQQQQQQQ:Resolved facet's custom property name [" + qnameStr + "] into [" + typeQName + "]");
        }
        return typeQName;
    }
    
    /**
     * Retrieves the named parameter as an integer, if the parameter is not present the default value is returned.
     * 
     * @param req The WebScript request
     * @param paramName The name of parameter to look for.
     * @param defaultValue The default value that should be returned if parameter is not present in request or is negative.
     * @return The request parameter or default value
     * @throws WebScriptException if the named parameter cannot be converted to int (HTTP rsp 400).
     */
    protected int getNonNegativeIntParameter(WebScriptRequest req, String paramName, int defaultValue)
    {
        final String paramString = req.getParameter(paramName);
        
        final int result;
        
        if (paramString != null)
        {
            try
            {
                final int paramInt = Integer.valueOf(paramString);
                
                if   (paramInt < 0) { result = defaultValue; }
                else                { result = paramInt; }
            }
            catch (NumberFormatException e) 
            {
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            }
        }
        else { result = defaultValue; }
        
        return result;
    }
    
    abstract protected Map<String, Object> unprotectedExecuteImpl(WebScriptRequest req, Status status, Cache cache);
}
