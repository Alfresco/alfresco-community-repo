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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.search.impl.solr.facet.Exceptions.UnrecognisedFacetId;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetProperties;
import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.Function;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the "solr-facet-config-admin.put" web scripts.
 * 
 * @author Jamal Kaabi-Mofrad
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

}
