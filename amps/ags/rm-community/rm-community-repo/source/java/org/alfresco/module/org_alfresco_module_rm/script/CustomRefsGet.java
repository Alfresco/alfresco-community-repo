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

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.capability.impl.ViewRecordsCapability;
import org.alfresco.module.org_alfresco_module_rm.relationship.Relationship;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipDefinition;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipDisplayName;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to get RM custom references for a node.
 *
 * @author Neil McErlean
 * @author Tuna Aksoy
 */
public class CustomRefsGet extends AbstractRmWebScript
{
    /** Constants */
    private static final String REFERENCE_TYPE = "referenceType";
    private static final String REF_ID = "refId";
    private static final String LABEL = "label";
    private static final String SOURCE = "source";
    private static final String TARGET = "target";
    private static final String PARENT_REF = "parentRef";
    private static final String CHILD_REF = "childRef";
    private static final String SOURCE_REF = "sourceRef";
    private static final String TARGET_REF = "targetRef";
    private static final String CUSTOM_REFS_FROM = "customRefsFrom";
    private static final String CUSTOM_REFS_TO = "customRefsTo";
    private static final String NODE_NAME = "nodeName";
    private static final String NODE_TITLE = "nodeTitle";

    /** Relationship service */
    private RelationshipService relationshipService;

    /** Capability service */
    private CapabilityService capabilityService;

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
     * Gets the capability service instance
     *
     * @return The capability service instance
     */
    protected CapabilityService getCapabilityService()
    {
        return this.capabilityService;
    }

    /**
     * Sets the capability service instance
     *
     * @param capabilityService Capability service instance
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }

    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest,
     *      org.springframework.extensions.webscripts.Status,
     *      org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<>(4);
        NodeRef nodeRef = parseRequestForNodeRef(req);
        model.put(NODE_NAME, getNodeService().getProperty(nodeRef, ContentModel.PROP_NAME));
        model.put(NODE_TITLE, getNodeService().getProperty(nodeRef, ContentModel.PROP_TITLE));
        model.put(CUSTOM_REFS_FROM, getOutwardReferences(nodeRef));
        model.put(CUSTOM_REFS_TO, getInwardReferenceData(nodeRef));
        return model;
    }

    /**
     * Gets all the references that come 'out' from this node
     *
     * @param nodeRef Node reference
     * @return All the references that come 'out' from this node
     */
    private List<Map<String, String>> getOutwardReferences(NodeRef nodeRef)
    {
        List<Map<String, String>> outwardReferenceData = new ArrayList<>();
        Set<Relationship> relationships = getRelationshipService().getRelationshipsFrom(nodeRef);
        outwardReferenceData.addAll(getRelationshipData(relationships));
        return outwardReferenceData;
    }

    /**
     * Gets all the references that come 'in' to this node
     *
     * @param nodeRef Node reference
     * @return All the references that come 'in' to this node
     */
    private List<Map<String, String>> getInwardReferenceData(NodeRef nodeRef)
    {
        List<Map<String, String>> inwardReferenceData = new ArrayList<>();
        Set<Relationship> relationships = getRelationshipService().getRelationshipsTo(nodeRef);
        inwardReferenceData.addAll(getRelationshipData(relationships));
        return inwardReferenceData;
    }

    /**
     * Creates relationship data for the ftl template
     *
     * @param relationships The relationships
     * @return The relationship data
     */
    private List<Map<String, String>> getRelationshipData(Set<Relationship> relationships)
    {
        List<Map<String, String>> relationshipData = new ArrayList<>();

        for (Relationship relationship : relationships)
        {
            String uniqueName = relationship.getUniqueName();
            RelationshipDefinition relationshipDefinition = getRelationshipService().getRelationshipDefinition(uniqueName);

            NodeRef source = relationship.getSource();
            NodeRef target = relationship.getTarget();

            if (relationshipDefinition != null && hasView(source) && hasView(target))
            {
                Map<String, String> data = new HashMap<>();

                RelationshipType type = relationshipDefinition.getType();
                RelationshipDisplayName displayName = relationshipDefinition.getDisplayName();

                if (RelationshipType.BIDIRECTIONAL.equals(type))
                {
                    data.put(LABEL, displayName.getSourceText());
                    data.put(SOURCE_REF, source.toString());
                    data.put(TARGET_REF, target.toString());
                }
                else if (RelationshipType.PARENTCHILD.equals(type))
                {
                    data.put(SOURCE, displayName.getSourceText());
                    data.put(TARGET, displayName.getTargetText());
                    data.put(PARENT_REF, source.toString());
                    data.put(CHILD_REF, target.toString());
                }
                else
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Unsupported relationship type '")
                        .append(type)
                        .append("'.");

                    throw new WebScriptException(Status.STATUS_BAD_REQUEST, sb.toString());
                }

                data.put(REFERENCE_TYPE, type.toString().toLowerCase());
                data.put(REF_ID, uniqueName);

                relationshipData.add(data);
            }

        }

        return relationshipData;
    }

    /**
     * Determines whether the current user has view capabilities on the given node.
     *
     * @param  nodeRef Node reference
     * @return boolean <code>true</code> if current user has view capability, <code>false</code> otherwise
     */
    private boolean hasView(NodeRef nodeRef)
    {
        boolean result = false;

        Capability viewRecordCapability = getCapabilityService().getCapability(ViewRecordsCapability.NAME);
        if (AccessStatus.ALLOWED.equals(viewRecordCapability.hasPermission(nodeRef)))
        {
            result = true;
        }

        return result;
    }
}
