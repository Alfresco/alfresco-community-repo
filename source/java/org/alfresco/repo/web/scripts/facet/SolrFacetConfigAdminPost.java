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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.search.impl.solr.facet.SolrFacetProperties;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetProperties.CustomProperties;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the "solr-facet-config-admin.post" web scripts.
 * 
 * @author Jamal Kaabi-Mofrad
 */
public class SolrFacetConfigAdminPost extends AbstractSolrFacetConfigAdminWebScript
{
    private static final Log logger = LogFactory.getLog(SolrFacetConfigAdminPost.class);

    @Override
    protected Map<String, Object> unprotectedExecuteImpl(WebScriptRequest req, Status status, Cache cache)
    {
        try
        {
            SolrFacetProperties fp = parseRequestForFacetProperties(req);
            facetService.createFacetNode(fp);

            if (logger.isDebugEnabled())
            {
                logger.debug("Created facet node: " + fp);
            }
        }
        catch (Throwable t)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not save the facet configuration.", t);
        }

        Map<String, Object> model = new HashMap<String, Object>(1);
        return model;
    }

    private SolrFacetProperties parseRequestForFacetProperties(WebScriptRequest req)
    {
        JSONObject json = null;
        try
        {
            json = new JSONObject(new JSONTokener(req.getContent().getContent()));

            final String filterID = json.getString(PARAM_FILTER_ID);
            validateFilterID(filterID);

            final String facetQNameStr = json.getString(PARAM_FACET_QNAME);
            final QName facetQName = QName.createQName(facetQNameStr, namespaceService);
            final String displayName = json.getString(PARAM_DISPLAY_NAME);
            final String displayControl = json.getString(PARAM_DISPLAY_CONTROL);
            final int maxFilters = json.getInt(PARAM_MAX_FILTERS);
            final int hitThreshold = json.getInt(PARAM_HIT_THRESHOLD);
            final int minFilterValueLength = json.getInt(PARAM_MIN_FILTER_VALUE_LENGTH);
            final String sortBy = json.getString(PARAM_SORT_BY);
            // Optional params
            final String scope = getValue(String.class, json.opt(PARAM_SCOPE), "ALL");
            final boolean isEnabled = getValue(Boolean.class, json.opt(PARAM_IS_ENABLED), false);
            JSONArray scopedSitesJsonArray = getValue(JSONArray.class, json.opt(PARAM_SCOPED_SITES), null);
            final Set<String> scopedSites = getScopedSites(scopedSitesJsonArray);
            final JSONObject customPropJsonObj = getValue(JSONObject.class, json.opt(PARAM_CUSTOM_PROPERTIES), null);
            final Set<CustomProperties> customProps = getCustomProperties(customPropJsonObj);

            SolrFacetProperties fp = new SolrFacetProperties.Builder()
                        .filterID(filterID)
                        .facetQName(facetQName)
                        .displayName(displayName)
                        .displayControl(displayControl)
                        .maxFilters(maxFilters)
                        .hitThreshold(hitThreshold)
                        .minFilterValueLength(minFilterValueLength)
                        .sortBy(sortBy)
                        .scope(scope)
                        .isEnabled(isEnabled)
                        .scopedSites(scopedSites)
                        .customProperties(customProps).build();
            return fp;
        }
        catch (IOException e)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not read content from req.", e);
        }
        catch (JSONException e)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not parse JSON from req.", e);
        }
    }
}
