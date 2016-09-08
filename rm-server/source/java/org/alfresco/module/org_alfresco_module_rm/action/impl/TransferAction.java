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
package org.alfresco.module.org_alfresco_module_rm.action.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.RMDispositionActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.repo.action.executer.ActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Transfer action
 * 
 * @author Roy Wetherall
 */
public class TransferAction extends RMDispositionActionExecuterAbstractBase
{    
    /** Transfer node reference key */
    public static final String KEY_TRANSFER_NODEREF = "transferNodeRef";
    
    /** I18N */
    private static final String MSG_NODE_ALREADY_TRANSFER = "rm.action.node-already-transfer";
    
    /** Indicates whether the transfer is an accession or not */
    private boolean isAccession = false;
    
    /** File plan service */
    private FilePlanService filePlanService;
    
    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }
    
    /**
     * Indicates whether this transfer is an accession or not
     * 
     * @param isAccession
     */
    public void setIsAccession(boolean isAccession)
    {
        this.isAccession = isAccession;
    }
    
    /**
     * Do not set the transfer action to auto-complete
     * 
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMDispositionActionExecuterAbstractBase#getSetDispositionActionComplete()
     */
    @Override
    public boolean getSetDispositionActionComplete()
    {
        return false;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMDispositionActionExecuterAbstractBase#executeRecordFolderLevelDisposition(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeRecordFolderLevelDisposition(Action action, NodeRef recordFolder)
    {
        doTransfer(action, recordFolder);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMDispositionActionExecuterAbstractBase#executeRecordLevelDisposition(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeRecordLevelDisposition(Action action, NodeRef record)
    {
        doTransfer(action, record);
    }
    
    /**
     * Create the transfer node and link the disposition lifecycle node beneath it
     * 
     * @param dispositionLifeCycleNodeRef        disposition lifecycle node
     */
    private void doTransfer(Action action, NodeRef dispositionLifeCycleNodeRef)
    {
        // Get the root rm node
        NodeRef root = filePlanService.getFilePlan(dispositionLifeCycleNodeRef);
        
        // Get the transfer object
        NodeRef transferNodeRef = (NodeRef)AlfrescoTransactionSupport.getResource(KEY_TRANSFER_NODEREF);            
        if (transferNodeRef == null)
        {
            // Calculate a transfer name
            QName nodeDbid = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "node-dbid");
            Long dbId = (Long)this.nodeService.getProperty(dispositionLifeCycleNodeRef, nodeDbid);
            String transferName = StringUtils.leftPad(dbId.toString(), 10, "0");
            
            // Create the transfer object
            Map<QName, Serializable> transferProps = new HashMap<QName, Serializable>(2);
            transferProps.put(ContentModel.PROP_NAME, transferName);
            transferProps.put(PROP_TRANSFER_ACCESSION_INDICATOR, this.isAccession);
            
            // setup location property from disposition schedule
            DispositionAction da = dispositionService.getNextDispositionAction(dispositionLifeCycleNodeRef);
            if (da != null)
            {
                DispositionActionDefinition actionDef = da.getDispositionActionDefinition();
                if (actionDef != null)
                {
                    transferProps.put(PROP_TRANSFER_LOCATION, actionDef.getLocation());
                }
            }
            
            NodeRef transferContainer = filePlanService.getTransferContainer(root);
            transferNodeRef = this.nodeService.createNode(transferContainer, 
                                                      ContentModel.ASSOC_CONTAINS, 
                                                      QName.createQName(RM_URI, transferName), 
                                                      TYPE_TRANSFER,
                                                      transferProps).getChildRef();
            
            // Bind the hold node reference to the transaction
            AlfrescoTransactionSupport.bindResource(KEY_TRANSFER_NODEREF, transferNodeRef);
        }
        else
        {
            // ensure this node has not already in the process of being transferred 
            List<ChildAssociationRef> transferredAlready = nodeService.getChildAssocs(transferNodeRef, ASSOC_TRANSFERRED, ASSOC_TRANSFERRED);
            for(ChildAssociationRef car : transferredAlready)
            {
                if(car.getChildRef().equals(dispositionLifeCycleNodeRef) == true)
                {
                    throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_NODE_ALREADY_TRANSFER, dispositionLifeCycleNodeRef.toString()));
                    
                }
            }
        }
        
        // Link the record to the trasnfer object
        this.nodeService.addChild(transferNodeRef, 
                                  dispositionLifeCycleNodeRef, 
                                  ASSOC_TRANSFERRED, 
                                  ASSOC_TRANSFERRED);
        
        // Set PDF indicator flag
        setPDFIndicationFlag(transferNodeRef, dispositionLifeCycleNodeRef);
        
        // Set the transferring indicator aspect
        nodeService.addAspect(dispositionLifeCycleNodeRef, ASPECT_TRANSFERRING, null);
        
        // Set the return value of the action
        action.setParameterValue(ActionExecuter.PARAM_RESULT, transferNodeRef);
    }
    
    /**
     * 
     * @param transferNodeRef
     * @param dispositionLifeCycleNodeRef
     */
    private void setPDFIndicationFlag(NodeRef transferNodeRef, NodeRef dispositionLifeCycleNodeRef)
    {
       if (recordsManagementService.isRecordFolder(dispositionLifeCycleNodeRef) == true)
       {
           List<NodeRef> records = recordsManagementService.getRecords(dispositionLifeCycleNodeRef);
           for (NodeRef record : records)
           {
               setPDFIndicationFlag(transferNodeRef, record);
           }
       }
       else
       {
           ContentData contentData = (ContentData)nodeService.getProperty(dispositionLifeCycleNodeRef, ContentModel.PROP_CONTENT);
           if (contentData != null &&
               MimetypeMap.MIMETYPE_PDF.equals(contentData.getMimetype()) == true)
           {
               // Set the property indicator
               nodeService.setProperty(transferNodeRef, PROP_TRANSFER_PDF_INDICATOR, true);
           }           
       }
    }
}
