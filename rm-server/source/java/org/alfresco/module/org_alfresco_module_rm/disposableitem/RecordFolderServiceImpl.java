/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.disposableitem;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * Record Folder Service Implementation
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class RecordFolderServiceImpl implements RecordFolderService, 
                                                RecordsManagementModel,
                                                NodeServicePolicies.OnCreateChildAssociationPolicy
{
    /** Policy component */
    private PolicyComponent policyComponent;
    
    /** Node service */
    private NodeService nodeService;
    
    /** Disposition service */
    private DispositionService dispositionService;
    
    /** Behaviours */
    private JavaBehaviour onCreateChildAssociation = new JavaBehaviour(
                                                            this, 
                                                            "onCreateChildAssociation", 
                                                            NotificationFrequency.FIRST_EVENT);
    
    /**
     * @param policyComponent   policy component 
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @param dispositionService    disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }
    
   /**
    * Init method
    */
   public void init()
   {
       policyComponent.bindAssociationBehaviour(
               NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME, 
               TYPE_RECORD_CATEGORY, 
               ContentModel.ASSOC_CONTAINS,
               onCreateChildAssociation);
   }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy#onCreateChildAssociation(org.alfresco.service.cmr.repository.ChildAssociationRef, boolean)
     */
    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean bNew)
    {
        NodeRef nodeRef = childAssocRef.getChildRef();
        if (nodeService.exists(nodeRef) == true)
        {
            initialiseRecordFolder(nodeRef);       
        }
    }
    
    /**
     * 
     * @param nodeRef
     */
    public void initialiseRecordFolder(NodeRef nodeRef)
    {
        // initialise disposition details
        if (nodeService.hasAspect(nodeRef, ASPECT_DISPOSITION_LIFECYCLE) == false)
        {
            DispositionSchedule di = dispositionService.getDispositionSchedule(nodeRef);
            if (di != null && di.isRecordLevelDisposition() == false)
            {
                nodeService.addAspect(nodeRef, ASPECT_DISPOSITION_LIFECYCLE, null);
            }
        }
    }

}
