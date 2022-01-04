/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
    	Map<String, Object> result = new HashMap<>();

    	rmAdminService.removeCustomPropertyDefinition(propQName);

        result.put("propertyqname", propQName.toPrefixString(getNamespaceService()));

        return result;
    }
}
