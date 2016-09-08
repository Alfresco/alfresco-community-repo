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

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
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
 */
public class CustomReferenceDefinitionPut extends AbstractRmWebScript
{
    private static final String URL = "url";
    private static final String REF_ID = "refId";
    private static final String TARGET = "target";
    private static final String SOURCE = "source";
    private static final String LABEL = "label";

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
        JSONObject json = null;
        Map<String, Object> ftlModel = null;
        try
        {
            json = new JSONObject(new JSONTokener(req.getContent().getContent()));

            ftlModel = updateCustomReference(req, json);
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
        catch (IllegalArgumentException iae)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                  iae.getMessage(), iae);
        }

        return ftlModel;
    }

    /**
     * Applies custom properties.
     */
    @SuppressWarnings("rawtypes")
    protected Map<String, Object> updateCustomReference(WebScriptRequest req, JSONObject json) throws JSONException
    {
        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, Serializable> params = new HashMap<String, Serializable>();

        for (Iterator iter = json.keys(); iter.hasNext(); )
        {
            String nextKeyString = (String)iter.next();
            Serializable nextValue = (Serializable)json.get(nextKeyString);

            params.put(nextKeyString, nextValue);
        }

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String refId = templateVars.get(REF_ID);
        // refId cannot be null as it is defined within the URL
        params.put(REF_ID, (Serializable)refId);

        // Ensure that the reference actually exists.
        QName refQName = rmAdminService.getQNameForClientId(refId);
        if (refQName == null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND,
                    "Could not find reference definition for: " + refId);
        }

        String newLabel = (String)params.get(LABEL);
        String newSource = (String)params.get(SOURCE);
        String newTarget = (String)params.get(TARGET);

        // Determine whether it's a bidi or a p/c ref
        AssociationDefinition assocDef = rmAdminService.getCustomReferenceDefinitions().get(refQName);
        if (assocDef == null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND,
                    "Could not find reference definition for: " + refId);
        }

        if (assocDef instanceof ChildAssociationDefinition)
        {
            if (newSource != null || newTarget != null)
            {
                rmAdminService.updateCustomChildAssocDefinition(refQName, newSource, newTarget);
            }
        }
        else if (newLabel != null)
        {
            rmAdminService.updateCustomAssocDefinition(refQName, newLabel);
        }

        result.put(URL, req.getServicePath());
        result.put("refId", refQName.getLocalName());
        result.put("success", Boolean.TRUE);

        return result;
    }
}