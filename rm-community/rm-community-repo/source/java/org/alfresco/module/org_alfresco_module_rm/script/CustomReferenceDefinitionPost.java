 
package org.alfresco.module.org_alfresco_module_rm.script;

import static org.alfresco.util.WebScriptUtils.getRequestContentAsJsonObject;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipDefinition;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipDisplayName;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
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
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest,
     *      org.springframework.extensions.webscripts.Status,
     *      org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        JSONObject requestContent = getRequestContentAsJsonObject(req);
        RelationshipDisplayName displayName = createDisplayName(requestContent);
        RelationshipDefinition relationshipDefinition =  getRelationshipService().createRelationshipDefinition(displayName);

        Map<String, Object> model = new HashMap<String, Object>();
        String servicePath = req.getServicePath();
        Map<String, Object> customRelationshipData = createRelationshipDefinitionData(relationshipDefinition, servicePath);
        model.putAll(customRelationshipData);

        return model;
    }

    /**
     * Creates relationship definition data for the ftl template
     *
     * @param relationshipDefinition The relationship definition
     * @param servicePath The service path
     * @return The relationship definition data
     */
    private Map<String, Object> createRelationshipDefinitionData(RelationshipDefinition relationshipDefinition, String servicePath)
    {
        Map<String, Object> relationshipDefinitionData = new HashMap<String, Object>(4);
        String uniqueName = relationshipDefinition.getUniqueName();
        relationshipDefinitionData.put(REFERENCE_TYPE, relationshipDefinition.getType().toString());
        relationshipDefinitionData.put(REF_ID, uniqueName);
        relationshipDefinitionData.put(URL, servicePath + PATH_SEPARATOR + uniqueName);
        relationshipDefinitionData.put(SUCCESS, Boolean.TRUE);
        return relationshipDefinitionData;
    }
}