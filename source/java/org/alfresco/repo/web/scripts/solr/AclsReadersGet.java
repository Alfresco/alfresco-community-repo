package org.alfresco.repo.web.scripts.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.solr.AclReaders;
import org.alfresco.repo.solr.SOLRTrackingComponent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Support for SOLR: Track ACLs
 *
 * @since 4.0
 */
public class AclsReadersGet extends DeclarativeWebScript
{
    protected static final Log logger = LogFactory.getLog(AclsReadersGet.class);

    private SOLRTrackingComponent solrTrackingComponent;
    
    public void setSolrTrackingComponent(SOLRTrackingComponent solrTrackingComponent)
    {
        this.solrTrackingComponent = solrTrackingComponent;
    }

    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        try
        {
            Map<String, Object> model = buildModel(req);
            if (logger.isDebugEnabled())
            {
                logger.debug("Result: \n\tRequest: " + req + "\n\tModel: " + model);
            }
            return model;
        }
        catch(IOException e)
        {
            throw new WebScriptException("IO exception parsing request", e);
        }
        catch(JSONException e)
        {
            throw new WebScriptException("Invalid JSON", e);
        }
    }
    
    private Map<String, Object> buildModel(WebScriptRequest req) throws JSONException, IOException
    {
        List<Long> aclIds = null;
        
        Content content = req.getContent();
        if (content == null)
        {
            throw new WebScriptException("Request content is empty");
        }
        JSONObject o = new JSONObject(content.getContent());
        JSONArray aclIdsJSON = o.has("aclIds") ? o.getJSONArray("aclIds") : null;
        if (aclIdsJSON == null)
        {
            throw new WebScriptException(
                    Status.STATUS_BAD_REQUEST,
                    "Parameter 'aclIds' not provided in request content.");
        }
        else if (aclIdsJSON.length() == 0)
        {
            throw new WebScriptException(
                    Status.STATUS_BAD_REQUEST,
                    "Parameter 'aclIds' must hold from 1 or more IDs.");
        }
        aclIds = new ArrayList<Long>(aclIdsJSON.length());
        for (int i = 0; i < aclIdsJSON.length(); i++)
        {
            aclIds.add(aclIdsJSON.getLong(i));
        }

        // Request according to the paging query style required
        List<AclReaders> aclsReaders = solrTrackingComponent.getAclsReaders(aclIds);
        
        Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
        model.put("aclsReaders", aclsReaders);

        if (logger.isDebugEnabled())
        {
            logger.debug("Result: \n\tRequest: " + req + "\n\tModel: " + model);
        }
        
        return model;
    }
}
