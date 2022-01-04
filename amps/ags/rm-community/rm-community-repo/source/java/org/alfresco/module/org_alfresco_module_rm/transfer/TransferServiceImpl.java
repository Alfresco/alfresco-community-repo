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

package org.alfresco.module.org_alfresco_module_rm.transfer;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.model.rma.type.TransferContainerType;
import org.alfresco.module.org_alfresco_module_rm.model.rma.type.TransferType;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Transfer service implementation
 *
 * @author Tuna Aksoy
 * @since 2.2
 */
public class TransferServiceImpl extends ServiceBaseImpl
                                 implements TransferService, RecordsManagementModel
{
    /** Transfer node reference key */
    public static final String KEY_TRANSFER_NODEREF = "transferNodeRef";

    /** I18N */
    private static final String MSG_NODE_ALREADY_TRANSFER = "rm.action.node-already-transfer";

    /** File Plan Service */
    protected FilePlanService filePlanService;

    /** Disposition service */
    protected DispositionService dispositionService;

    /** Record service */
    protected RecordService recordService;

    /** Record folder service */
    protected RecordFolderService recordFolderService;

    /** Freeze Service */
    protected FreezeService freezeService;

    protected TransferContainerType transferContainerType;

    protected TransferType transferType;

    /**
     * @param filePlanService file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @param dispositionService disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    /**
     * @param recordService record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    /**
     * @param recordFolderService record folder service
     */
    public void setRecordFolderService(RecordFolderService recordFolderService)
    {
        this.recordFolderService = recordFolderService;
    }

    /**
     * @param freezeService freeze service
     */
    public void setFreezeService(FreezeService freezeService)
    {
        this.freezeService = freezeService;
    }

    public void setTransferContainerType(TransferContainerType transferContainerType)
    {
        this.transferContainerType = transferContainerType;
    }

    public void setTransferType(TransferType transferType)
    {
        this.transferType = transferType;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.transfer.TransferService#transfer(NodeRef, boolean)
     */
    @Override
    public NodeRef transfer(NodeRef nodeRef, boolean isAccession)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        // Get the root rm node
        NodeRef root = filePlanService.getFilePlan(nodeRef);

        // Get the transfer object
        NodeRef transferNodeRef = (NodeRef)AlfrescoTransactionSupport.getResource(KEY_TRANSFER_NODEREF);
        if (transferNodeRef == null)
        {
            // Calculate a transfer name
            QName nodeDbid = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "node-dbid");
            Long dbId = (Long) nodeService.getProperty(nodeRef, nodeDbid);
            String transferName = StringUtils.leftPad(dbId.toString(), 10, "0");

            // Create the transfer object
            Map<QName, Serializable> transferProps = new HashMap<>(2);
            transferProps.put(ContentModel.PROP_NAME, transferName);
            transferProps.put(PROP_TRANSFER_ACCESSION_INDICATOR, isAccession);

            // setup location property from disposition schedule
            DispositionAction da = dispositionService.getNextDispositionAction(nodeRef);
            if (da != null)
            {
                DispositionActionDefinition actionDef = da.getDispositionActionDefinition();
                if (actionDef != null)
                {
                    transferProps.put(PROP_TRANSFER_LOCATION, actionDef.getLocation());
                }
            }

            NodeRef transferContainer = filePlanService.getTransferContainer(root);

            transferContainerType.disable();
            transferType.disable();
            try
            {
                transferNodeRef = nodeService.createNode(transferContainer,
                                                          ContentModel.ASSOC_CONTAINS,
                                                          QName.createQName(RM_URI, transferName),
                                                          TYPE_TRANSFER,
                                                          transferProps).getChildRef();

            }
            finally
            {
                transferContainerType.enable();
                transferType.enable();
            }
            // Bind the hold node reference to the transaction
            AlfrescoTransactionSupport.bindResource(KEY_TRANSFER_NODEREF, transferNodeRef);
        }
        else
        {
            // ensure this node has not already in the process of being transferred
            List<ChildAssociationRef> transferredAlready = nodeService.getChildAssocs(transferNodeRef, ASSOC_TRANSFERRED, ASSOC_TRANSFERRED);
            for(ChildAssociationRef car : transferredAlready)
            {
                if(car.getChildRef().equals(nodeRef))
                {
                    throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_NODE_ALREADY_TRANSFER, nodeRef.toString()));

                }
            }
        }

        // Link the record to the trasnfer object
        transferType.disable();
        try
        {
            nodeService.addChild(transferNodeRef,
                        nodeRef,
                        ASSOC_TRANSFERRED,
                        ASSOC_TRANSFERRED);
            // Set PDF indicator flag
            setPDFIndicationFlag(transferNodeRef, nodeRef);
        }
        finally
        {
            transferType.enable();
        }

        // Set the transferring indicator aspect
        nodeService.addAspect(nodeRef, ASPECT_TRANSFERRING, null);
        if (isRecordFolder(nodeRef))
        {
            // add the transferring indicator aspect to all the child records
            for (NodeRef record : recordService.getRecords(nodeRef))
            {
                nodeService.addAspect(record, ASPECT_TRANSFERRING, null);
            }
        }

        return transferNodeRef;
    }

    /**
     *
     * @param transferNodeRef
     * @param dispositionLifeCycleNodeRef
     */
   private void setPDFIndicationFlag(NodeRef transferNodeRef, NodeRef dispositionLifeCycleNodeRef)
   {
      if (recordFolderService.isRecordFolder(dispositionLifeCycleNodeRef))
      {
          List<NodeRef> records = recordService.getRecords(dispositionLifeCycleNodeRef);
          for (NodeRef record : records)
          {
              setPDFIndicationFlag(transferNodeRef, record);
          }
      }
      else
      {
          ContentData contentData = (ContentData)nodeService.getProperty(dispositionLifeCycleNodeRef, ContentModel.PROP_CONTENT);
          if (contentData != null &&
              MimetypeMap.MIMETYPE_PDF.equals(contentData.getMimetype()))
          {
              // Set the property indicator
              nodeService.setProperty(transferNodeRef, PROP_TRANSFER_PDF_INDICATOR, true);
          }
      }
   }

   /**
    * @see org.alfresco.module.org_alfresco_module_rm.transfer.TransferService#completeTransfer(NodeRef)
    */
    @Override
    public void completeTransfer(NodeRef nodeRef)
    {
        boolean accessionIndicator = ((Boolean)nodeService.getProperty(nodeRef, PROP_TRANSFER_ACCESSION_INDICATOR)).booleanValue();
        String transferLocation = nodeService.getProperty(nodeRef, PROP_TRANSFER_LOCATION).toString();

        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, ASSOC_TRANSFERRED, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef assoc : assocs)
        {
            if(freezeService.isFrozen(assoc.getChildRef()))
            {
                throw new AlfrescoRuntimeException("Could not complete a transfer that contains held folders");
            }

            if(freezeService.hasFrozenChildren(assoc.getChildRef()))
            {
                throw new AlfrescoRuntimeException("Cound not complete a transfer that contains folders with held children");
            }

            markComplete(assoc.getChildRef(), accessionIndicator, transferLocation);
        }

        // Delete the transfer object
        nodeService.deleteNode(nodeRef);

        NodeRef transferNodeRef = (NodeRef) AlfrescoTransactionSupport.getResource(KEY_TRANSFER_NODEREF);
        if (transferNodeRef != null && transferNodeRef.equals(nodeRef))
        {
            AlfrescoTransactionSupport.bindResource(KEY_TRANSFER_NODEREF, null);
        }
    }

    /**
     * Marks the node complete
     *
     * @param nodeRef
     *            disposition lifecycle node reference
     */
    private void markComplete(NodeRef nodeRef, boolean accessionIndicator, String transferLocation)
    {
        // Set the completed date
        DispositionAction da = dispositionService.getNextDispositionAction(nodeRef);
        if (da != null)
        {
            nodeService.setProperty(da.getNodeRef(), PROP_DISPOSITION_ACTION_COMPLETED_AT, new Date());
            nodeService.setProperty(da.getNodeRef(), PROP_DISPOSITION_ACTION_COMPLETED_BY, AuthenticationUtil.getRunAsUser());
        }

        // Remove the transferring indicator aspect
        nodeService.removeAspect(nodeRef, ASPECT_TRANSFERRING);
        nodeService.setProperty(nodeRef, PROP_LOCATION, transferLocation);

        // Determine which marker aspect to use
        QName markerAspectQName = null;
        if (accessionIndicator)
        {
            markerAspectQName = ASPECT_ASCENDED;
        }
        else
        {
            markerAspectQName = ASPECT_TRANSFERRED;
        }

        // Mark the object and children accordingly
        nodeService.addAspect(nodeRef, markerAspectQName, null);
        if (recordFolderService.isRecordFolder(nodeRef))
        {
            List<NodeRef> records = recordService.getRecords(nodeRef);
            for (NodeRef record : records)
            {
                nodeService.addAspect(record, markerAspectQName, null);
                nodeService.setProperty(record, PROP_LOCATION, transferLocation);
            }
        }

        // Update to the next disposition action
        dispositionService.updateNextDispositionAction(nodeRef);
    }
}
