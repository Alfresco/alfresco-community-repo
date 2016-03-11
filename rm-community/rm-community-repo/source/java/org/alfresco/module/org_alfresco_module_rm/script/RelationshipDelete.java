package org.alfresco.module.org_alfresco_module_rm.script;

import static org.alfresco.util.WebScriptUtils.getRequestParameterValue;
import static org.alfresco.util.WebScriptUtils.getTemplateVars;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to delete a relationship from a node.
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RelationshipDelete extends AbstractRmWebScript
{
    /** Constants */
    private static final String STORE_TYPE = "target_store_type";
    private static final String STORE_ID = "target_store_id";
    private static final String ID = "target_id";
    private static final String UNIQUE_NAME = "uniqueName";

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
        String uniqueName = getRequestParameterValue(req, UNIQUE_NAME);
        NodeRef source = parseRequestForNodeRef(req);
        NodeRef target = parseRequestForTargetNodeRef(req);

        getRelationshipService().removeRelationship(uniqueName, source, target);
        getRelationshipService().removeRelationship(uniqueName, target, source);

        Map<String, Object> model = new HashMap<String, Object>(1);
        model.put(SUCCESS, true);
        return model;
    }

    /**
     * Gets the node reference of target
     *
     * @param req The webscript request
     * @return The node reference of the target
     */
    private NodeRef parseRequestForTargetNodeRef(WebScriptRequest req)
    {
        Map<String, String> templateVars = getTemplateVars(req);
        String storeType = templateVars.get(STORE_TYPE);
        String storeId = templateVars.get(STORE_ID);
        String nodeId = templateVars.get(ID);

        NodeRef nodeRef = new NodeRef(storeType, storeId, nodeId);

        if (!getNodeService().exists(nodeRef))
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find node: '" +
                        nodeRef.toString() + "'.");
        }

        return nodeRef;
    }
}
