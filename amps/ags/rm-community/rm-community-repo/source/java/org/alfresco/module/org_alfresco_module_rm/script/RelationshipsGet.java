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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.jscript.app.JSONConversionComponent;
import org.alfresco.module.org_alfresco_module_rm.relationship.Relationship;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipDefinition;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.WebScriptUtils;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to get the relationships for a node.
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RelationshipsGet extends AbstractRmWebScript
{
    /** Constants */
    private static final String RELATIONSHIPS = "relationships";
    private static final String RELATIONSHIP_LABEL = "relationshipLabel";
    private static final String RELATIONSHIP_UNIQUE_NAME = "relationshipUniqueName";

    /** The relationship end point */
    private enum RelationshipEndPoint
    {
        SOURCE, TARGET
    }

    /** Relationship service */
    private RelationshipService relationshipService;

    /** JSON conversion component */
    private JSONConversionComponent jsonConversionComponent;

    /**
     * Gets the relationship service
     *
     * @return The relationship service
     */
    protected RelationshipService getRelationshipService()
    {
        return this.relationshipService;
    }

    /**
     * Gets the JSON conversion component
     *
     * @return The JSON conversion component
     */
    protected JSONConversionComponent getJsonConversionComponent()
    {
        return this.jsonConversionComponent;
    }

    /**
     * Sets the relationship service
     *
     * @param relationshipService The relationship service
     */
    public void setRelationshipService(RelationshipService relationshipService)
    {
        this.relationshipService = relationshipService;
    }

    /**
     * Sets the JSON conversion component
     *
     * @param jsonConversionComponent The JSON conversion component
     */
    public void setJsonConversionComponent(JSONConversionComponent jsonConversionComponent)
    {
        this.jsonConversionComponent = jsonConversionComponent;
    }

    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest,
     *      org.springframework.extensions.webscripts.Status,
     *      org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<>(1);
        NodeRef nodeRef = parseRequestForNodeRef(req);
        model.put(RELATIONSHIPS, getRelationships(nodeRef));
        return model;
    }

    /**
     * Gets the relationships of a node
     *
     * @param nodeRef The node reference
     *
     * @return The list of relationships of a node
     */
    private List<String> getRelationships(NodeRef nodeRef)
    {
        List<String> relationships = new ArrayList<>();

        Set<Relationship> relationshipsFrom = getRelationshipService().getRelationshipsFrom(nodeRef);
        relationships.addAll(buildRelationshipData(relationshipsFrom, RelationshipEndPoint.TARGET));

        Set<Relationship> relationshipsTo = getRelationshipService().getRelationshipsTo(nodeRef);
        relationships.addAll(buildRelationshipData(relationshipsTo, RelationshipEndPoint.SOURCE));

        return relationships;
    }

    /**
     * Creates the relationship data
     *
     * @param relationships The {@link Set} of relationships
     * @param relationshipEndPoint The end point of the relationship, which is either {@link RelationshipEndpoint#SOURCE} or {@link RelationshipEndpoint#TARGET}
     * @return The relationship data as {@link List}
     */
    private List<String> buildRelationshipData(Set<Relationship> relationships, RelationshipEndPoint relationshipEndPoint)
    {
        List<String> result = new ArrayList<>();

        for (Relationship relationship : relationships)
        {
            String uniqueName = relationship.getUniqueName();
            RelationshipDefinition relationshipDefinition = getRelationshipService().getRelationshipDefinition(uniqueName);
            if (relationshipDefinition != null)
            {
                NodeRef node;
                String label;

                if (RelationshipEndPoint.SOURCE.equals(relationshipEndPoint))
                {
                    node = relationship.getSource();
                    label = relationshipDefinition.getDisplayName().getSourceText();
                }
                else if (RelationshipEndPoint.TARGET.equals(relationshipEndPoint))
                {
                    node = relationship.getTarget();
                    label = relationshipDefinition.getDisplayName().getTargetText();
                }
                else
                {
                    throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Unknown relationship end point type '" + relationshipEndPoint + "'.");
                }

                String nodeDetails = getJsonConversionComponent().toJSON(node, true);
                JSONObject jsonObject = WebScriptUtils.createJSONObject(nodeDetails);
                WebScriptUtils.putValueToJSONObject(jsonObject, RELATIONSHIP_LABEL, label);
                WebScriptUtils.putValueToJSONObject(jsonObject, RELATIONSHIP_UNIQUE_NAME, relationshipDefinition.getUniqueName());

                result.add(jsonObject.toString());
            }
        }

        return result;
    }
}
