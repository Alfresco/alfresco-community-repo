/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.CustomMetadataException;
import org.alfresco.module.org_alfresco_module_rm.PropertyAlreadyExistsMetadataException;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementAdminService;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to update RM custom property definitions
 * in the custom model.
 *
 * @author Neil McErlean
 */
public class CustomPropertyDefinitionPut extends BaseCustomPropertyWebScript
{
    private RecordsManagementAdminService rmAdminService;

    private static final String PARAM_LABEL = "label";
    private static final String PARAM_CONSTRAINT_REF = "constraintRef";
    private static final String PROP_ID = "propId";
    private static final String URL = "url";
    private static final String MESSAGE = "errorMessage";

    public void setRecordsManagementAdminService(RecordsManagementAdminService rmAdminService)
    {
        this.rmAdminService = rmAdminService;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        JSONObject json = null;
        Map<String, Object> ftlModel = null;
        try
        {
            json = new JSONObject(new JSONTokener(req.getContent().getContent()));
            try
            {
                ftlModel = handlePropertyDefinitionUpdate(req, json);
            }
            catch (CustomMetadataException e)
            {
                status.setCode(Status.STATUS_BAD_REQUEST);
                ftlModel = new HashMap<String, Object>();
                ftlModel.put(MESSAGE, e.getMessage());
            }
        }
        catch (IOException iox)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Could not read content from req.", iox);
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "Could not parse JSON from req.", je);
        }

        return ftlModel;
    }

    /**
     * Applies custom properties.
     * @throws CustomMetadataException
     */
    protected Map<String, Object> handlePropertyDefinitionUpdate(WebScriptRequest req, JSONObject json)
            throws JSONException, CustomMetadataException
    {
        Map<String, Object> result = new HashMap<String, Object>();

        Map<String, Serializable> params = getParamsFromUrlAndJson(req, json);

        QName propertyQName;
        propertyQName = updatePropertyDefinition(params);
        String localName = propertyQName.getLocalName();

        result.put(PROP_ID, localName);

        String urlResult = req.getServicePath();
        result.put(URL, urlResult);

        return result;
    }

    /**
     * If label has a non-null value, it is set on the property def.
     * If constraintRef has a non-null value, it is set on this propDef.
     * If constraintRef has a null value, all constraints for that propDef are removed.
     *
     * @param params
     * @return
     * @throws CustomMetadataException
     */
    protected QName updatePropertyDefinition(Map<String, Serializable> params) throws CustomMetadataException
    {
        QName result = null;
        boolean updated = false;

        String propId = (String)params.get(PROP_ID);
        ParameterCheck.mandatoryString("propId", propId);

        QName propQName = rmAdminService.getQNameForClientId(propId);
        if (propQName == null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND,
                    "Could not find property definition for: " + propId);
        }

        if (params.containsKey(PARAM_CONSTRAINT_REF))
        {
            String constraintRef = (String)params.get(PARAM_CONSTRAINT_REF);
            List<ConstraintDefinition> constraints = rmAdminService.getCustomPropertyDefinitions().get(propQName).getConstraints();

            if (constraintRef == null)
            {
                result = rmAdminService.removeCustomPropertyDefinitionConstraints(propQName);
                updated = constraints.isEmpty() ? false : true;
            }
            else
            {
                boolean exists = false;
                for (ConstraintDefinition constraintDefinition : constraints)
                {
                    if (constraintDefinition.getConstraint().getShortName().equalsIgnoreCase(constraintRef))
                    {
                        exists = true;
                        break;
                    }
                }
                if (exists == false)
                {
                    QName constraintRefQName = QName.createQName(constraintRef, namespaceService);
                    result = rmAdminService.setCustomPropertyDefinitionConstraint(propQName, constraintRefQName);
                    updated = true;
                }
            }
        }

        if (params.containsKey(PARAM_LABEL))
        {
            String label = (String)params.get(PARAM_LABEL);
            try
            {
                result = rmAdminService.updateCustomPropertyDefinitionName(propQName, label);
            }
            catch (PropertyAlreadyExistsMetadataException ex)
            {
                if (updated == false)
                {
                    String propIdAsString = rmAdminService.getQNameForClientId(label).toPrefixString(namespaceService);
                    throw new PropertyAlreadyExistsMetadataException(propIdAsString);
                }
            }
        }

        return result;
    }

    @SuppressWarnings("rawtypes")
    protected Map<String, Serializable> getParamsFromUrlAndJson(WebScriptRequest req, JSONObject json)
            throws JSONException
    {
        Map<String, Serializable> params;
        params = new HashMap<String, Serializable>();

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String propId = templateVars.get(PROP_ID);
        if (propId != null)
        {
            params.put(PROP_ID, (Serializable)propId);
        }

        for (Iterator iter = json.keys(); iter.hasNext(); )
        {
            String nextKeyString = (String)iter.next();
            String nextValueString = null;
            if (!json.isNull(nextKeyString))
            {
                nextValueString = json.getString(nextKeyString);
            }

            params.put(nextKeyString, nextValueString);
        }

        return params;
    }
}