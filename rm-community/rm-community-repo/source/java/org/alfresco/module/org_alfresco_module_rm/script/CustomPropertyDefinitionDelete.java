package org.alfresco.module.org_alfresco_module_rm.script;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to remove RM custom property definitions
 * from the custom model.
 *
 * @author Neil McErlean
 */
public class CustomPropertyDefinitionDelete extends AbstractRmWebScript
{
    private static final String PROP_ID = "propId";

    private static Log logger = LogFactory.getLog(CustomPropertyDefinitionDelete.class);

    private RecordsManagementAdminService rmAdminService;

    public void setRecordsManagementAdminService(RecordsManagementAdminService rmAdminService)
    {
		this.rmAdminService = rmAdminService;
	}

    /*
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
	@Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> ftlModel = null;
        try
        {
        	QName propQName = getPropertyFromReq(req);
        	if (logger.isDebugEnabled())
        	{
        		StringBuilder msg = new StringBuilder();
        		msg.append("Deleting property definition ").append(propQName);
        		logger.debug(msg.toString());
        	}
            ftlModel = removePropertyDefinition(propQName);
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "Could not parse JSON from req.", je);
        }

        return ftlModel;
    }

    private QName getPropertyFromReq(WebScriptRequest req)
    {
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String propIdString = templateVars.get(PROP_ID);

        QName propQName = this.rmAdminService.getQNameForClientId(propIdString);
        Map<QName, PropertyDefinition> existingPropDefs = rmAdminService.getCustomPropertyDefinitions();

        if (!existingPropDefs.containsKey(propQName))
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND,
                        "Requested property definition (id:" + propIdString + ") does not exist");
        }

        return propQName;
    }

    /**
     * Applies custom properties to the specified record node.
     */
    protected Map<String, Object> removePropertyDefinition(QName propQName) throws JSONException
    {
    	Map<String, Object> result = new HashMap<String, Object>();

    	rmAdminService.removeCustomPropertyDefinition(propQName);

        result.put("propertyqname", propQName.toPrefixString(getNamespaceService()));

        return result;
    }
}
