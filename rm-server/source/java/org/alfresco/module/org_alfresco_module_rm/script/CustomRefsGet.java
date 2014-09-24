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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.capability.impl.ViewRecordsCapability;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
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

    /** RM admin service */
    private RecordsManagementAdminService rmAdminService;

    /** Dictionary service */
    private DictionaryService dictionaryService;

    /** Capability service */
    private CapabilityService capabilityService;

    /**
     * Sets the RM admin service
     * @param rmAdminService RM admin service
     */
    public void setRecordsManagementAdminService(RecordsManagementAdminService rmAdminService)
    {
        this.rmAdminService = rmAdminService;
    }

    /**
     * Sets the dictionary service
     *
     * @param dictionaryService Dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Sets the capability service
     *
     * @param capabilityService Capability service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }

    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status, org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>(4);
        NodeRef nodeRef = parseRequestForNodeRef(req);
        model.put(NODE_NAME, nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
        model.put(NODE_TITLE, nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE));
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
        List<Map<String, String>> outwardReferenceData = new ArrayList<Map<String, String>>();

        List<AssociationRef> assocsFromThisNode = rmAdminService.getCustomReferencesFrom(nodeRef);
        addBidirectionalReferenceData(outwardReferenceData, assocsFromThisNode);

        List<ChildAssociationRef> childAssocs = rmAdminService.getCustomChildReferences(nodeRef);
        addParentChildReferenceData(outwardReferenceData, childAssocs);

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
        List<Map<String, String>> inwardReferenceData = new ArrayList<Map<String, String>>();

        List<AssociationRef> toAssocs = rmAdminService.getCustomReferencesTo(nodeRef);
        addBidirectionalReferenceData(inwardReferenceData, toAssocs);

        List<ChildAssociationRef> parentAssocs = rmAdminService.getCustomParentReferences(nodeRef);
        addParentChildReferenceData(inwardReferenceData, parentAssocs);

        return inwardReferenceData;
    }

    /**
     * This method goes through the associationRefs specified and constructs a Map<String, String>
     * for each assRef. FTL-relevant data are added to that map. The associationRefs must all be
     * parent/child references.
     *
     * @param referenceData Reference data
     * @param childAssocs Association references
     */
    private void addParentChildReferenceData(List<Map<String, String>> referenceData, List<ChildAssociationRef> childAssocs)
    {
        for (ChildAssociationRef childAssRef : childAssocs)
        {
            Map<String, String> data = new HashMap<String, String>();

            QName typeQName = childAssRef.getTypeQName();

            data.put(CHILD_REF, childAssRef.getChildRef().toString());
            data.put(PARENT_REF, childAssRef.getParentRef().toString());

            AssociationDefinition assDef = rmAdminService.getCustomReferenceDefinitions().get(typeQName);

            if (assDef != null &&
                hasView(childAssRef.getParentRef()) &&
                hasView(childAssRef.getChildRef()))
            {
                String compoundTitle = assDef.getTitle(dictionaryService);

                data.put(REF_ID, typeQName.getLocalName());

                String[] sourceAndTarget = rmAdminService.splitSourceTargetId(compoundTitle);
                data.put(SOURCE, sourceAndTarget[0]);
                data.put(TARGET, sourceAndTarget[1]);
                data.put(REFERENCE_TYPE, CustomReferenceType.PARENT_CHILD.toString());

                referenceData.add(data);
            }
    	}
    }

    /**
     * This method goes through the associationRefs specified and constructs a Map<String, String>
     * for each assRef. FTL-relevant data are added to that map. The associationRefs must all be
     * bidirectional references.
     *
     * @param referenceData Reference data
     * @param assocs Association references
     */
    private void addBidirectionalReferenceData(List<Map<String, String>> referenceData, List<AssociationRef> assocs)
    {
        for (AssociationRef assRef : assocs)
        {
            Map<String, String> data = new HashMap<String, String>();

            QName typeQName = assRef.getTypeQName();
            AssociationDefinition assDef = rmAdminService.getCustomReferenceDefinitions().get(typeQName);

            if (assDef != null &&
                hasView(assRef.getTargetRef()) &&
                hasView(assRef.getSourceRef()))
            {
                data.put(LABEL, assDef.getTitle(dictionaryService));
                data.put(REF_ID, typeQName.getLocalName());
                data.put(REFERENCE_TYPE, CustomReferenceType.BIDIRECTIONAL.toString());
                data.put(SOURCE_REF, assRef.getSourceRef().toString());
                data.put(TARGET_REF, assRef.getTargetRef().toString());

                referenceData.add(data);
            }
        }
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

        Capability viewRecordCapability = capabilityService.getCapability(ViewRecordsCapability.NAME);
        if (AccessStatus.ALLOWED.equals(viewRecordCapability.hasPermission(nodeRef)))
        {
            result = true;
        }

        return result;
    }
}