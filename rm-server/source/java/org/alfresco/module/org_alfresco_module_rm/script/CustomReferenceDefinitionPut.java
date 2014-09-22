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
package org.alfresco.module.org_alfresco_module_rm.script;

import static org.alfresco.util.WebScriptUtils.getRequestContentAsJsonObject;
import static org.alfresco.util.WebScriptUtils.getRequestParameterValue;
import static org.alfresco.util.WebScriptUtils.getStringValueFromJSONObject;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.namespace.QName;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to update RM custom reference definitions.
 * There is currently only support for updating the label (for bidirectional references) or
 * the source/target (for parent/child references).
 *
 * @author Neil McErlean
 * @author Tuna Aksoy
 */
public class CustomReferenceDefinitionPut extends CustomReferenceDefinitionBase
{
	/**
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        JSONObject requestContent = getRequestContentAsJsonObject(req);
        String referenceId = getRequestParameterValue(req, REF_ID);
        updateCustomReference(requestContent, referenceId);

        Map<String, Object> model = new HashMap<String, Object>();
        String servicePath = req.getServicePath();
        Map<String, Object> customReferenceData = getCustomReferenceData(servicePath, referenceId);
        model.putAll(customReferenceData);

        return model;
    }

    /**
     * Updates the custom reference
     *
     * @param requestContent The request content as json object
     * @param referenceId The reference id
     */
    private void updateCustomReference(JSONObject requestContent, String referenceId)
    {
        QName referenceQName = getCustomReferenceQName(referenceId);
        CustomReferenceType customReferenceType = getCustomReferenceType(requestContent);

        if (CustomReferenceType.PARENT_CHILD.equals(customReferenceType))
        {
            String source = getStringValueFromJSONObject(requestContent, SOURCE);
            String target = getStringValueFromJSONObject(requestContent, TARGET);
            getRmAdminService().updateCustomChildAssocDefinition(referenceQName, source, target);
        }
        else if (CustomReferenceType.BIDIRECTIONAL.equals(customReferenceType))
        {
            String label = getStringValueFromJSONObject(requestContent, LABEL);
            getRmAdminService().updateCustomAssocDefinition(referenceQName, label);
        }
        else
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Unsupported custom reference type.");
        }
    }

    /**
     * Gets the custom reference data
     *
     * @param servicePath The service path
     * @param String The reference id
     * @return The custom reference data
     */
    private Map<String, Object> getCustomReferenceData(String servicePath, String referenceId)
    {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put(URL, servicePath);
        result.put(REF_ID, referenceId);
        result.put(SUCCESS, Boolean.TRUE);
        return result;
    }
}