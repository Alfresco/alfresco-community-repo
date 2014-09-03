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
import java.util.Map;

import org.alfresco.repo.search.impl.solr.facet.SolrFacetProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
}
