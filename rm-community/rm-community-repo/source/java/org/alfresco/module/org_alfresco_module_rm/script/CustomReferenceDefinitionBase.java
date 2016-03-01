 
package org.alfresco.module.org_alfresco_module_rm.script;

import static org.alfresco.util.WebScriptUtils.getStringValueFromJSONObject;
import static org.apache.commons.lang.StringUtils.isBlank;

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
