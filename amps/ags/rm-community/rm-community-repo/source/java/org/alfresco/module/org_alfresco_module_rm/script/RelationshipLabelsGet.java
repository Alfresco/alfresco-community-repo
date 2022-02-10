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

import static org.alfresco.util.ParameterCheck.mandatoryString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipDefinition;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipDisplayName;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipType;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to get the relationship labels.
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RelationshipLabelsGet extends AbstractRmWebScript
{
    /** Constants */
    private static final String RELATIONSHIP_LABELS = "relationshipLabels";

    /** Relationship service */
    private RelationshipService relationshipService;

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
     * Sets the relationship service
     *
     * @param relationshipService The relationship service
     */
    public void setRelationshipService(RelationshipService relationshipService)
    {
        this.relationshipService = relationshipService;
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
        model.put(RELATIONSHIP_LABELS, getRelationshipsLabels());
        return model;
    }

    /**
     * Gets the list of available relationship labels
     *
     * @return The list of available relationship labels
     */
    private List<RelationshipLabel> getRelationshipsLabels()
    {
        List<RelationshipLabel> relationshipLabels = new ArrayList<>();

        Set<RelationshipDefinition> relationshipDefinitions = getRelationshipService().getRelationshipDefinitions();
        for (RelationshipDefinition relationshipDefinition : relationshipDefinitions)
        {
            RelationshipType type = relationshipDefinition.getType();
            String uniqueName = relationshipDefinition.getUniqueName();
            RelationshipDisplayName displayName = relationshipDefinition.getDisplayName();
            String sourceText = displayName.getSourceText();
            String targetText = displayName.getTargetText();

            if (RelationshipType.PARENTCHILD.equals(type))
            {
                relationshipLabels.add(new RelationshipLabel(sourceText, uniqueName + INVERT));
                relationshipLabels.add(new RelationshipLabel(targetText, uniqueName));
            }
            else if (RelationshipType.BIDIRECTIONAL.equals(type))
            {
                if (!sourceText.equals(targetText))
                {
                    throw new WebScriptException(
                            Status.STATUS_BAD_REQUEST,
                            "The source '"
                                    + sourceText
                                    + "' and target text '"
                                    + targetText
                                    + "' must be the same for a bidirectional relationship.");
                }
                relationshipLabels.add(new RelationshipLabel(sourceText, uniqueName));
            }
            else
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Unknown relationship type '" + type + "'.");
            }
        }

        return sortRelationshipLabelsByName(relationshipLabels);
    }

    /**
     * Helper method to sort the relationship labels by their names
     *
     * @param relationshipLabels Relationship labels to sort
     * @return Sorted list of relationship labels
     */
    private List<RelationshipLabel> sortRelationshipLabelsByName(List<RelationshipLabel> relationshipLabels)
    {
        Collections.sort(relationshipLabels, new Comparator<RelationshipLabel>()
        {
            @Override
            public int compare(RelationshipLabel r1, RelationshipLabel r2)
            {
                return r1.getLabel().toLowerCase().compareTo(r2.getLabel().toLowerCase());
            }
        });
        return relationshipLabels;
    }

    /**
     * Relationship label helper class
     */
    public class RelationshipLabel
    {
        /** Label of the relationship */
        private String label;

        /** Unique name of the relationship */
        private String uniqueName;

        /**
         * Constructor
         *
         * @param label Label of the relationship
         * @param uniqueName Unique name of the relationship
         */
        public RelationshipLabel(String label, String uniqueName)
        {
            mandatoryString("label", label);
            mandatoryString("uniqueName", uniqueName);

            setLabel(label);
            setUniqueName(uniqueName);
        }

        /**
         * Gets the label of the relationship
         *
         * @return The label of the relationship
         */
        public String getLabel()
        {
            return this.label;
        }

        /**
         * Sets the label of the relationship
         *
         * @param label The label of the relationship
         */
        private void setLabel(String label)
        {
            this.label = label;
        }

        /**
         * Gets the unique name of the relationship
         *
         * @return The unique name of the relationship
         */
        public String getUniqueName()
        {
            return this.uniqueName;
        }

        /**
         * Sets the unique name of the relationship
         *
         * @param uniqueName The unique name of the relationship
         */
        private void setUniqueName(String uniqueName)
        {
            this.uniqueName = uniqueName;
        }
    }
}
