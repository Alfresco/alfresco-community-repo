/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class provides the implementation for the customrefs.get webscript.
 *
 * @author Neil McErlean
 */
public class CustomRefsGet extends AbstractRmWebScript
{
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

    /** logger */
    private static Log logger = LogFactory.getLog(CustomRefsGet.class);
    
    /** records management admin service */
    private RecordsManagementAdminService rmAdminService;
    
    /** dictionary service */
    private DictionaryService dictionaryService;
    
    /** capability service */
    private CapabilityService capabilityService;
    
    /**
     * @param rmAdminService    records management admin service
     */
    public void setRecordsManagementAdminService(RecordsManagementAdminService rmAdminService)
    {
        this.rmAdminService = rmAdminService;
    }

    /**
     * @param dictionaryService dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * @param capabilityService capability service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }

    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status, org.springframework.extensions.webscripts.Cache)
     */
    @Override
    public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> ftlModel = new HashMap<String, Object>();

        NodeRef node = parseRequestForNodeRef(req);

        if (logger.isDebugEnabled())
        {
            logger.debug("Getting custom reference instances for " + node);
        }

        // All the references that come 'out' from this node.
        List<Map<String, String>> listOfOutwardReferenceData = new ArrayList<Map<String, String>>();

        List<AssociationRef> assocsFromThisNode = this.rmAdminService.getCustomReferencesFrom(node);
        addBidirectionalReferenceData(listOfOutwardReferenceData, assocsFromThisNode);

        List<ChildAssociationRef> childAssocs = this.rmAdminService.getCustomChildReferences(node);
        addParentChildReferenceData(listOfOutwardReferenceData, childAssocs);

        // All the references that come 'in' to this node.
        List<Map<String, String>> listOfInwardReferenceData = new ArrayList<Map<String, String>>();

        List<AssociationRef> toAssocs = this.rmAdminService.getCustomReferencesTo(node);
        addBidirectionalReferenceData(listOfInwardReferenceData, toAssocs);

        List<ChildAssociationRef> parentAssocs = this.rmAdminService.getCustomParentReferences(node);
        addParentChildReferenceData(listOfInwardReferenceData, parentAssocs);

    	if (logger.isDebugEnabled())
    	{
    		logger.debug("Retrieved custom reference instances: " + assocsFromThisNode);
    	}

        ftlModel.put(NODE_NAME, nodeService.getProperty(node, ContentModel.PROP_NAME));
        ftlModel.put(NODE_TITLE, nodeService.getProperty(node, ContentModel.PROP_TITLE));
        ftlModel.put(CUSTOM_REFS_FROM, listOfOutwardReferenceData);
        ftlModel.put(CUSTOM_REFS_TO, listOfInwardReferenceData);

        return ftlModel;
    }

    /**
     * This method goes through the associationRefs specified and constructs a Map<String, String>
     * for each assRef. FTL-relevant data are added to that map. The associationRefs must all be
     * parent/child references.
     *
     * @param listOfReferenceData
     * @param assocs
     */
    private void addParentChildReferenceData(List<Map<String, String>> listOfReferenceData,List<ChildAssociationRef> childAssocs)
    {
        for (ChildAssociationRef childAssRef : childAssocs)
    	{
    		Map<String, String> data = new HashMap<String, String>();

    		QName typeQName = childAssRef.getTypeQName();

    		data.put(CHILD_REF, childAssRef.getChildRef().toString());
    		data.put(PARENT_REF, childAssRef.getParentRef().toString());

            AssociationDefinition assDef = rmAdminService.getCustomReferenceDefinitions().get(typeQName);

            if (assDef != null &&
                hasView(childAssRef.getParentRef()) == true &&   
                hasView(childAssRef.getChildRef()) == true)
            {
                String compoundTitle = assDef.getTitle(dictionaryService);

                data.put(REF_ID, typeQName.getLocalName());

                String[] sourceAndTarget = rmAdminService.splitSourceTargetId(compoundTitle);
                data.put(SOURCE, sourceAndTarget[0]);
                data.put(TARGET, sourceAndTarget[1]);
                data.put(REFERENCE_TYPE, CustomReferenceType.PARENT_CHILD.toString());

                listOfReferenceData.add(data);
            }
    	}
    }

    /**
     * This method goes through the associationRefs specified and constructs a Map<String, String>
     * for each assRef. FTL-relevant data are added to that map. The associationRefs must all be
     * bidirectional references.
     *
     * @param listOfReferenceData
     * @param assocs
     */
    private void addBidirectionalReferenceData(List<Map<String, String>> listOfReferenceData, List<AssociationRef> assocs)
    {
        for (AssociationRef assRef : assocs)
    	{
    		Map<String, String> data = new HashMap<String, String>();

    		QName typeQName = assRef.getTypeQName();
            AssociationDefinition assDef = rmAdminService.getCustomReferenceDefinitions().get(typeQName);

            if (assDef != null && 
                hasView(assRef.getTargetRef()) == true &&
                hasView(assRef.getSourceRef()) == true)
            {
                data.put(LABEL, assDef.getTitle(dictionaryService));
                data.put(REF_ID, typeQName.getLocalName());
                data.put(REFERENCE_TYPE, CustomReferenceType.BIDIRECTIONAL.toString());
                data.put(SOURCE_REF, assRef.getSourceRef().toString());
                data.put(TARGET_REF, assRef.getTargetRef().toString());

                listOfReferenceData.add(data);
            }
        }
    }
    
    /**
     * Determine whether the current user has view capabilities on the given node.
     * 
     * @param  nodeRef   node reference
     * @return boolean   true if current user has view capability, false otherwise
     */
    private boolean hasView(NodeRef nodeRef)
    {
        boolean result = false;
        
        Capability viewRecordCapability = capabilityService.getCapability(ViewRecordsCapability.NAME);
        if (AccessStatus.ALLOWED.equals(viewRecordCapability.hasPermission(nodeRef)) == true)
        {
            result = true;
        }
        return result;
    }
}