package org.alfresco.repo.web.scripts.solr;

import java.io.IOException;
import java.util.Map;

import org.alfresco.repo.solr.AlfrescoModel;
import org.alfresco.repo.solr.SOLRTrackingComponent;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Support for SOLR: Get Alfresco model
 *
 * @since 4.0
 */
public class AlfrescoModelGet extends AbstractWebScript
{
    protected static final Log logger = LogFactory.getLog(AlfrescoModelGet.class);

    private NamespaceService namespaceService;
    private SOLRTrackingComponent solrTrackingComponent;

    public void setSolrTrackingComponent(SOLRTrackingComponent solrTrackingComponent)
    {
        this.solrTrackingComponent = solrTrackingComponent;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void execute(WebScriptRequest req, WebScriptResponse res)
    {
        try
        {
            handle(req, res);
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
    
    private void handle(WebScriptRequest req, WebScriptResponse res) throws JSONException, IOException
    {
        // create map of template vars
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String modelName = templateVars.get("modelShortQName");
        if(modelName == null)
        {
            throw new WebScriptException(
                    Status.STATUS_BAD_REQUEST,
                    "URL parameter 'modelShortQName' not provided.");
        }

        ModelDefinition.XMLBindingType bindingType = ModelDefinition.XMLBindingType.SOLR;
        AlfrescoModel model = solrTrackingComponent.getModel(QName.createQName(modelName, namespaceService));
        res.setHeader("XAlfresco-modelChecksum", String.valueOf(model.getModelDef().getChecksum(bindingType)));
        model.getModelDef().toXML(bindingType, res.getOutputStream());
    }

}
