/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.web.scripts.facet;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.repo.search.impl.solr.facet.SolrFacetProperties;

/**
 * This class is the controller for the "solr-facet-config-admin.get" web script.
 * 
 * @author Jamal Kaabi-Mofrad
 */
public class SolrFacetConfigAdminGet extends AbstractSolrFacetConfigAdminWebScript
{
    private static final Log logger = LogFactory.getLog(SolrFacetConfigAdminGet.class);

    @Override
    protected Map<String, Object> executeImpl(final WebScriptRequest req, final Status status, final Cache cache)
    {
        // Allow all authenticated users view the filters
        return unprotectedExecuteImpl(req, status, cache);
    }

    @Override
    protected Map<String, Object> unprotectedExecuteImpl(WebScriptRequest req, Status status, Cache cache)
    {
        // get the filterID parameter.
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String filterID = templateVars.get("filterID");

        Map<String, Object> model = new HashMap<String, Object>(1);

        if (filterID == null)
        {
            model.put("filters", facetService.getFacets());
        }
        else
        {
            SolrFacetProperties fp = facetService.getFacet(filterID);
            if (fp == null)
            {
                throw new WebScriptException(Status.STATUS_NOT_FOUND, "Filter not found");
            }
            model.put("filter", fp);
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Retrieved all available facets: " + model.values());
        }

        return model;
    }
}
