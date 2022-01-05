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

import static org.alfresco.util.WebScriptUtils.getRequestContentAsJSONObject;
import static org.alfresco.util.WebScriptUtils.getRequestParameterValue;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipDisplayName;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
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
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest,
     *      org.springframework.extensions.webscripts.Status,
     *      org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        String uniqueName = getRequestParameterValue(req, REF_ID);
        JSONObject requestContent = getRequestContentAsJSONObject(req);
        RelationshipDisplayName displayName = createDisplayName(requestContent);
        getRelationshipService().updateRelationshipDefinition(uniqueName, displayName);

        Map<String, Object> model = new HashMap<>();
        String servicePath = req.getServicePath();
        Map<String, Object> customReferenceData = createRelationshipDefinitionData(servicePath, uniqueName);
        model.putAll(customReferenceData);

        return model;
    }

    /**
     * Creates relationship definition data for the ftl template
     *
     * @param servicePath The service path
     * @param String The relationship unique name
     * @return The relationship definition data
     */
    private Map<String, Object> createRelationshipDefinitionData(String servicePath, String uniqueName)
    {
        Map<String, Object> relationshipDefinitionData = new HashMap<>(3);
        relationshipDefinitionData.put(URL, servicePath);
        relationshipDefinitionData.put(REF_ID, uniqueName);
        relationshipDefinitionData.put(SUCCESS, Boolean.TRUE);
        return relationshipDefinitionData;
    }
}
