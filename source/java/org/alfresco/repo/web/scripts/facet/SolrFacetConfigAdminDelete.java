
package org.alfresco.repo.web.scripts.facet;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the "solr-facet-config-admin.delete" web script.
 * 
 * @author Jamal Kaabi-Mofrad
 */
public class SolrFacetConfigAdminDelete extends AbstractSolrFacetConfigAdminWebScript
{
    private static final Log logger = LogFactory.getLog(SolrFacetConfigAdminDelete.class);

    @Override
    protected Map<String, Object> unprotectedExecuteImpl(WebScriptRequest req, Status status, Cache cache)
    {
        // get the filterID parameter.
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String filterID = templateVars.get("filterID");

        if (filterID == null)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Filter id not provided");
        }
        facetService.deleteFacet(filterID);

        Map<String, Object> model = new HashMap<String, Object>(1);
        model.put("success", true);

        if (logger.isDebugEnabled())
        {
            logger.debug("Facet [" + filterID + "] has been deleted successfully");
        }

        return model;
    }
}
