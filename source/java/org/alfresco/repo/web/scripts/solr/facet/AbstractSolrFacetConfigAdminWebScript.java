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

package org.alfresco.repo.web.scripts.solr.facet;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.search.impl.solr.facet.SolrFacetProperties;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
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
    protected static final String PARAM_FILTER_ID = "filterID";
    protected static final String PARAM_FACET_QNAME = "facetQName";
    protected static final String PARAM_DISPLAY_NAME = "displayName";
    protected static final String PARAM_MAX_FILTERS = "maxFilters";
    protected static final String PARAM_HIT_THRESHOLD = "hitThreshold";
    protected static final String PARAM_MIN_FILTER_VALUE_LENGTH = "minFilterValueLength";
    protected static final String PARAM_SORT_BY = "sortBy";
    protected static final String PARAM_SCOPE = "scope";
    protected static final String PARAM_SCOPED_SITES = "scopedSites";
    protected static final String PARAM_INDEX = "index";
    protected static final String PARAM_IS_ENABLED = "isEnabled";

    protected SolrFacetService facetService;

    /**
     * @param facetService the facetService to set
     */
    public void setFacetService(SolrFacetService facetService)
    {
        this.facetService = facetService;
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

    protected SolrFacetProperties parseRequestForFacetProperties(WebScriptRequest req)
    {
        JSONObject json = null;
        try
        {
            json = new JSONObject(new JSONTokener(req.getContent().getContent()));

            final String filterID = json.getString(PARAM_FILTER_ID);
            final String facetQNameStr = json.getString(PARAM_FACET_QNAME);
            if (filterID == null || facetQNameStr == null)
            {
                String requiredProp = (filterID == null) ? "filterID" : "facetQName";
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, requiredProp + " not provided.");
            }

            final QName facetQName = QName.createQName(facetQNameStr);
            final String displayName = json.getString(PARAM_DISPLAY_NAME);
            final int maxFilters = json.getInt(PARAM_MAX_FILTERS);
            final int hitThreshold = json.getInt(PARAM_HIT_THRESHOLD);
            final int minFilterValueLength = json.getInt(PARAM_MIN_FILTER_VALUE_LENGTH);
            final String sortBy = json.getString(PARAM_SORT_BY);
            final String scope = json.getString(PARAM_SCOPE);
            final int index = json.getInt(PARAM_INDEX);
            final boolean isEnabled = json.getBoolean(PARAM_IS_ENABLED);
            JSONArray scopedSitesJsonArray = json.getJSONArray(PARAM_SCOPED_SITES);
            Set<String> scopedSites = null;
            if (scopedSitesJsonArray != null)
            {
                scopedSites = new HashSet<String>(scopedSitesJsonArray.length());
                for (int i = 0, length = scopedSitesJsonArray.length(); i < length; i++)
                {
                    String site = scopedSitesJsonArray.getString(i);
                    scopedSites.add(site);
                }
            }

            SolrFacetProperties fp = new SolrFacetProperties.Builder()
                        .filterID(filterID)
                        .facetQName(facetQName)
                        .displayName(displayName)
                        .maxFilters(maxFilters)
                        .hitThreshold(hitThreshold)
                        .minFilterValueLength(minFilterValueLength)
                        .sortBy(sortBy)
                        .scope(scope)
                        .index(index)
                        .isEnabled(isEnabled)
                        .scopedSites(scopedSites).build();
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

    abstract protected Map<String, Object> unprotectedExecuteImpl(WebScriptRequest req, Status status, Cache cache);
}
