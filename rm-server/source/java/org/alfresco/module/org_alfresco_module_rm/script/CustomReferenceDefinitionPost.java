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
 * Implementation for Java backed webscript to add RM custom reference definitions
 * to the custom model.
 *
 * @author Neil McErlean
 * @author Tuna Aksoy
 */
public class CustomReferenceDefinitionPost extends CustomReferenceDefinitionBase
{
    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status, org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        JSONObject requestContent = getRequestContentAsJsonObject(req);
        CustomReferenceType customReferenceType = getCustomReferenceType(requestContent);
        QName customReference = addCustomReference(requestContent, customReferenceType);

        Map<String, Object> model = new HashMap<String, Object>();
        String servicePath = req.getServicePath();
        Map<String, Object> customReferenceData = getCustomReferenceData(customReferenceType, customReference, servicePath);
        model.putAll(customReferenceData);

        return model;
    }

    /**
     * Adds custom reference to the model
     *
     * @param requestContent The request content as json object
     * @param customReferenceType The custom reference type
     * @return Returns the {@link QName} of the new custom reference
     */
    private QName addCustomReference(JSONObject requestContent, CustomReferenceType customReferenceType)
    {
        QName referenceQName;

        if (CustomReferenceType.PARENT_CHILD.equals(customReferenceType))
        {
            String source = getStringValueFromJSONObject(requestContent, SOURCE);
            String target = getStringValueFromJSONObject(requestContent, TARGET);
            referenceQName = getRmAdminService().addCustomChildAssocDefinition(source, target);
        }
        else if (CustomReferenceType.BIDIRECTIONAL.equals(customReferenceType))
        {
            String label = getStringValueFromJSONObject(requestContent, LABEL);
            referenceQName = getRmAdminService().addCustomAssocDefinition(label);
        }
        else
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Unsupported custom reference type.");
        }

        return referenceQName;
    }

    /**
     * Gets the custom reference data
     *
     * @param customReferenceType The custom reference type
     * @param customReference The qualified name of the custom reference
     * @param servicePath The service path
     * @return The custom reference data
     */
    private Map<String, Object> getCustomReferenceData(CustomReferenceType customReferenceType, QName customReference, String servicePath)
    {
        Map<String, Object> result = new HashMap<String, Object>();
        String qnameLocalName = customReference.getLocalName();
        result.put(REFERENCE_TYPE, customReferenceType.toString());
        result.put(REF_ID, qnameLocalName);
        result.put(URL, servicePath + PATH_SEPARATOR + qnameLocalName);
        result.put(SUCCESS, Boolean.TRUE);
        return result;
    }
}