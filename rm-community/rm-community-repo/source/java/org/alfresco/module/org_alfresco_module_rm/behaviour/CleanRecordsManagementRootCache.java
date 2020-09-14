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

public class CleanRecordsManagementRootCache implements RecordsManagementModel, OnDeleteNodePolicy, OnRemoveAspectPolicy 
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
            QName.createQName(NamespaceService.ALFRESCO_URI, "onRemoveAspect"),
            this,
            new JavaBehaviour(this, "onRemoveAspect"));
        policyComponent.bindClassBehaviour(
            QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteNode"),
            this,
            new JavaBehaviour(this, "onDeleteNode"));
    }

    /**
     * On remove aspect, performs the records management root cache clean operation but only
     * if the removed aspects matches {@link RecordsManagementModel#ASPECT_RECORDS_MANAGEMENT_ROOT}
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
	 * On delete node, performs the records management root cache clean operation
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
	 * Cleans the records managements root cache in case of supplied nodeRef has {@link RecordsManagementModel#ASPECT_RECORDS_MANAGEMENT_ROOT} aspect
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