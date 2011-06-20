package org.alfresco.repo.web.scripts.solr;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.solr.AlfrescoModelDiff;
import org.alfresco.repo.solr.SOLRTrackingComponent;
import org.alfresco.service.namespace.QName;
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
 * Support for SOLR: Track Alfresco model changes
 *
 * @since 4.0
 */
public class AlfrescoModelsDiff extends DeclarativeWebScript
{
    protected static final Log logger = LogFactory.getLog(AlfrescoModelsDiff.class);

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
        Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);

        Content content = req.getContent();
        if(content == null)
        {
            throw new WebScriptException("Failed to convert request to String");
        }
        JSONObject o = new JSONObject(content.getContent());
        JSONArray jsonModels = o.getJSONArray("models");
        Map<QName, Long> models = new HashMap<QName, Long>(jsonModels.length());
        for(int i = 0; i < jsonModels.length(); i++)
        {
            JSONObject jsonModel = jsonModels.getJSONObject(i);
            models.put(QName.createQName(jsonModel.getString("name")), jsonModel.getLong("checksum"));
        }

        List<AlfrescoModelDiff> diffs = solrTrackingComponent.getModelDiffs(models);
        model.put("diffs", diffs);

        if (logger.isDebugEnabled())
        {
            logger.debug("Result: \n\tRequest: " + req + "\n\tModel: " + model);
        }
        
        return model;
    }
}
