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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.search.impl.solr.facet.Exceptions.UnrecognisedFacetId;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetProperties.CustomProperties;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetProperties;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.Function;
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
 * This class is the controller for the "solr-facet-config-admin.put" web scripts.
 * 
 * @author Jamal Kaabi-Mofrad
 * @author Neil Mc Erlean
 * @since 5.0
 */
public class SolrFacetConfigAdminPut extends AbstractSolrFacetConfigAdminWebScript
{
    private static final Log logger = LogFactory.getLog(SolrFacetConfigAdminPost.class);

    protected static final String PARAM_RELATIVE_POS  = "relativePos";
    protected static final String URL_PARAM_FILTER_ID = "filterID";

    @Override
    protected Map<String, Object> unprotectedExecuteImpl(WebScriptRequest req, Status status, Cache cache)
    {
        final String relativePosString = req.getParameter(PARAM_RELATIVE_POS);
        try
        {
            if (relativePosString != null)
            {
                // This is a request to 'move' (reposition) the specified facet.
                
                // We need the relative position that the facet will move.
                final int relativePos;
                
                try { relativePos = Integer.parseInt(relativePosString); }
                catch (NumberFormatException nfe)
                {
                    throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                                                 "Cannot move facet as could not parse relative position: '" + relativePosString + "'");
                }
                
                // And we need the filterID for the facet we're moving.
                final Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
                String filterId = templateVars.get(URL_PARAM_FILTER_ID);
                
                if (filterId == null) { throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Illegal null filterId"); }
                
                // So let's move the filter...
                try
                {
                    // Get the current sequence of filter IDs.
                    List<SolrFacetProperties> facets = facetService.getFacets();
                    List<String> facetIDs = CollectionUtils.transform(facets, new Function<SolrFacetProperties, String>()
                            {
                                @Override public String apply(SolrFacetProperties value)
                                {
                                    return value.getFilterID();
                                }
                            });
                    
                    List<String> reorderedIDs = CollectionUtils.moveRight(relativePos, filterId, facetIDs);
                    
                    this.facetService.reorderFacets(reorderedIDs);
                    
                    if (logger.isDebugEnabled()) { logger.debug("Moved facet " + filterId + " to relative position: " + relativePos); }
                }
                catch (UnrecognisedFacetId ufi)
                {
                    throw new WebScriptException(Status.STATUS_NOT_FOUND, "Unrecognised filter ID: " + ufi.getFacetId());
                }
            }
            // TODO Allow for simultaneous move and update of facet.
            else
            {
                SolrFacetProperties fp = parseRequestForFacetProperties(req);
                facetService.updateFacet(fp);
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("Updated facet node: " + fp);
                }
            }
        }
        catch (Throwable t)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not update the facet configuration.", t);
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

            final String filterID = json.getString(PARAM_FILTER_ID); // Must exist

            final String facetQNameStr = getValue(String.class, json.opt(PARAM_FACET_QNAME), null);
            final QName facetQName = (facetQNameStr == null) ? null : QName.resolveToQName(namespaceService, facetQNameStr);
            final String displayName = getValue(String.class, json.opt(PARAM_DISPLAY_NAME), null);
            final String displayControl = getValue(String.class, json.opt(PARAM_DISPLAY_CONTROL), null);
            final int maxFilters = getValue(Integer.class, json.opt(PARAM_MAX_FILTERS), -1);
            final int hitThreshold = getValue(Integer.class, json.opt(PARAM_HIT_THRESHOLD), -1);
            final int minFilterValueLength = getValue(Integer.class, json.opt(PARAM_MIN_FILTER_VALUE_LENGTH), -1);
            final String sortBy = getValue(String.class, json.opt(PARAM_SORT_BY), null);
            final String scope = getValue(String.class, json.opt(PARAM_SCOPE), null);
            final Boolean isEnabled = getValue(Boolean.class, json.opt(PARAM_IS_ENABLED), null);
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
