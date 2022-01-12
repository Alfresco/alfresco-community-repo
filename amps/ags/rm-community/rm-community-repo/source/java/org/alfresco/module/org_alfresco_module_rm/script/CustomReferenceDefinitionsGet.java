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

import static org.alfresco.util.WebScriptUtils.getRequestParameterValue;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipDefinition;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipDisplayName;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipType;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to get RM custom reference definitions.
 *
 * @author Neil McErlean
 * @author Tuna Aksoy
 */
public class CustomReferenceDefinitionsGet extends CustomReferenceDefinitionBase
{
    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest,
     *      org.springframework.extensions.webscripts.Status,
     *      org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        String uniqueName = getRequestParameterValue(req, REF_ID, false);
        Set<RelationshipDefinition> relationshipDefinitions = getRelationshipDefinitons(uniqueName);
        List<Map<String, String>> relationshipDefinitionData = createRelationshipDefinitionData(relationshipDefinitions);

        Map<String, Object> model = new HashMap<>();
        model.put(CUSTOM_REFS, relationshipDefinitionData);
        return model;
    }

    /**
     * Gets the relationship definition for the unique name. If the unique
     * name is blank all relationship definitions will be retrieved
     *
     * @param uniqueName The unique name of the relationship definition
     * @return Relationship definition for the given unique name or all
     * relationship definitions if unique name is blank
     */
    private Set<RelationshipDefinition> getRelationshipDefinitons(String uniqueName)
    {
        Set<RelationshipDefinition> relationshipDefinitions = new HashSet<>();

        if (isBlank(uniqueName))
        {
            relationshipDefinitions.addAll(getRelationshipService().getRelationshipDefinitions());
        }
        else
        {
            RelationshipDefinition relationshipDefinition = getRelationshipService().getRelationshipDefinition(uniqueName);
            if (relationshipDefinition != null)
            {
                relationshipDefinitions.add(relationshipDefinition);
            }
        }

        return relationshipDefinitions;
    }

    /**
     * Creates relationship definition data for the ftl template
     *
     * @param relationshipDefinitions The relationship definitions
     * @return The relationship definition data
     */
    private List<Map<String, String>> createRelationshipDefinitionData(Set<RelationshipDefinition> relationshipDefinitions)
    {
        List<Map<String, String>> relationshipDefinitionData = new ArrayList<>();

        for (RelationshipDefinition relationshipDefinition : relationshipDefinitions)
        {
            Map<String, String> data = new HashMap<>();

            RelationshipType type = relationshipDefinition.getType();
            RelationshipDisplayName displayName = relationshipDefinition.getDisplayName();

            if (RelationshipType.BIDIRECTIONAL.equals(type))
            {
                data.put(LABEL, displayName.getSourceText());
            }
            else if (RelationshipType.PARENTCHILD.equals(type))
            {
                data.put(SOURCE, displayName.getSourceText());
                data.put(TARGET, displayName.getTargetText());
            }
            else
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Unsupported relationship type '")
                    .append(type)
                    .append("'.");

                throw new WebScriptException(Status.STATUS_BAD_REQUEST, sb.toString());
            }

            data.put(REF_ID, relationshipDefinition.getUniqueName());
            data.put(REFERENCE_TYPE, type.toString().toLowerCase());

            relationshipDefinitionData.add(data);
        }

        return relationshipDefinitionData;
    }
}
