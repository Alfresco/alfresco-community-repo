
package org.alfresco.repo.web.scripts.facet;

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
