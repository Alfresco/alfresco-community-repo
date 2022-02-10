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

import static org.alfresco.util.WebScriptUtils.getStringValueFromJSONObject;
import static org.apache.commons.lang3.StringUtils.isBlank;

import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipDisplayName;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService;
import org.json.JSONObject;

/**
 * Base class for custom reference definition classes
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class CustomReferenceDefinitionBase extends AbstractRmWebScript
{
    /** Constants for the custom reference definition classes */
    protected static final String REFERENCE_TYPE = "referenceType";
    protected static final String REF_ID = "refId";
    protected static final String LABEL = "label";
    protected static final String SOURCE = "source";
    protected static final String TARGET = "target";
    protected static final String CUSTOM_REFS = "customRefs";
    protected static final String URL = "url";

    /** Relationship service */
    private RelationshipService relationshipService;

    /**
     * Gets the relationship service instance
     *
     * @return The relationship service instance
     */
    protected RelationshipService getRelationshipService()
    {
        return this.relationshipService;
    }

    /**
     * Sets the relationship service instance
     *
     * @param relationshipService The relationship service instance
     */
    public void setRelationshipService(RelationshipService relationshipService)
    {
        this.relationshipService = relationshipService;
    }

    /**
     * Creates the relationship display name from request content
     *
     * @param requestContent The request content as json object
     * @return The relationship display name
     */
    protected RelationshipDisplayName createDisplayName(JSONObject requestContent)
    {
        String sourceText;
        String targetText;

        String labelText = getStringValueFromJSONObject(requestContent, LABEL, false, false);

        if (isBlank(labelText))
        {
            sourceText = getStringValueFromJSONObject(requestContent, SOURCE);
            targetText = getStringValueFromJSONObject(requestContent, TARGET);
        }
        else
        {
            sourceText = labelText;
            targetText = labelText;
        }

        return new RelationshipDisplayName(sourceText, targetText);
    }
}
