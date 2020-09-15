/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.module.org_alfresco_module_rm.behaviour;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnRemoveAspectPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;

/**
 * Updates the records management root nodes cache from {@link FilePlanService}
 *
 * @author Tiago Salvado
 *
 * @see RecordsManagementModel
 * @see OnCreateNodePolicy
 * @see OnDeleteNodePolicy
 * @see OnAddAspectPolicy
 * @see OnRemoveAspectPolicy
 */
public class UpdateRecordsManagementRootCache implements RecordsManagementModel, OnCreateNodePolicy, OnDeleteNodePolicy, OnAddAspectPolicy, OnRemoveAspectPolicy
{

	private PolicyComponent policyComponent;

	private NodeService nodeService;

    private FilePlanService filePlanService;

    /**
     * 
     * @param policyComponent
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
		this.policyComponent = policyComponent;
	}

   /**
    * @param nodeService
    */
   public void setNodeService(NodeService nodeService)
   {
		this.nodeService = nodeService;
	}

    /**
     * 
     * @param filePlanService
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
		this.filePlanService = filePlanService;
	}

    /**
     * Performs the initialization operations for this behavior
     */
    public void init()
    {
        // check that required properties have been set
        PropertyCheck.mandatory("CleanRecordsManagementRootCache", "policyComponent", policyComponent);
        PropertyCheck.mandatory("CleanRecordsManagementRootCache", "nodeService", nodeService);
        PropertyCheck.mandatory("CleanRecordsManagementRootCache", "filePlanService", filePlanService);

        // register behaviour
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"),
                this,
                new JavaBehaviour(this, "onAddAspect"));
        policyComponent.bindClassBehaviour(
            QName.createQName(NamespaceService.ALFRESCO_URI, "onRemoveAspect"),
            this,
            new JavaBehaviour(this, "onRemoveAspect"));
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"),
                this,
                new JavaBehaviour(this, "onCreateNode"));
        policyComponent.bindClassBehaviour(
            QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteNode"),
            this,
            new JavaBehaviour(this, "onDeleteNode"));
    }

    /**
     * Updates the root records management cache when adding an aspect
     */
	@Override
	public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName)
	{
        if (nodeRef != null && ASPECT_RECORDS_MANAGEMENT_ROOT.isMatch(aspectTypeQName))
        {
            addToRootRecordsManagementCache(nodeRef);
        }
	}

    /**
     * Updates the root records management cache on aspect removal
     */
	@Override
	public void onRemoveAspect(NodeRef nodeRef, QName aspectTypeQName)
	{
        if (nodeRef != null && ASPECT_RECORDS_MANAGEMENT_ROOT.isMatch(aspectTypeQName))
        {
            clearRootRecordsManagementCache(nodeRef);
        }
	}

    /**
     * Updates the root records management cache on node creation
     */
	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef)
	{
        if (childAssocRef != null)
		{
            addToRootRecordsManagementCache(childAssocRef.getParentRef());
            addToRootRecordsManagementCache(childAssocRef.getChildRef());
        }
    }

	/**
	 * Updates the root records management cache on node removal
	 */
	@Override
	public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived)
	{
		if (childAssocRef != null)
		{
            clearRootRecordsManagementCache(childAssocRef.getParentRef());
            clearRootRecordsManagementCache(childAssocRef.getChildRef());
        }
	}
	
	/**
	 * Adds a node to the records managements root node cache
	 *
	 * @param nodeRef
	 */
	private void addToRootRecordsManagementCache(NodeRef nodeRef) {
        if (nodeRef != null && nodeService.hasAspect(nodeRef, ASPECT_RECORDS_MANAGEMENT_ROOT))
        {
            filePlanService.addToRootRecordsManagementCache(nodeRef);
        }
	}
	
	/**
	 * Cleans the records managements root node cache
	 * 
	 * @param nodeRef
	 */
	private void clearRootRecordsManagementCache(NodeRef nodeRef) {
        if (nodeRef != null && nodeService.hasAspect(nodeRef, ASPECT_RECORDS_MANAGEMENT_ROOT))
        {
            filePlanService.clearRootRecordsManagementCache(nodeRef.getStoreRef());
		}
	}
}