/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
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
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Transfer service implementation
 *
 * @author Tuna Aksoy
 * @since 2.2
 */
public class TransferServiceImpl extends ServiceBaseImpl implements TransferService, RecordsManagementModel
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
     * @see org.alfresco.module.org_alfresco_module_rm.transfer.TransferService#isTransfer(NodeRef)
     */
    @Override
    public boolean isTransfer(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        return instanceOf(nodeRef, TYPE_TRANSFER);
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
            Map<QName, Serializable> transferProps = new HashMap<QName, Serializable>(2);
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
            transferNodeRef = nodeService.createNode(transferContainer,
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
                if(car.getChildRef().equals(nodeRef) == true)
                {
                    throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_NODE_ALREADY_TRANSFER, nodeRef.toString()));

                }
            }
        }

        // Link the record to the trasnfer object
        nodeService.addChild(transferNodeRef,
                                  nodeRef,
                                  ASSOC_TRANSFERRED,
                                  ASSOC_TRANSFERRED);

        // Set PDF indicator flag
        setPDFIndicationFlag(transferNodeRef, nodeRef);

        // Set the transferring indicator aspect
        nodeService.addAspect(nodeRef, ASPECT_TRANSFERRING, null);

        return transferNodeRef;
    }

    /**
     *
     * @param transferNodeRef
     * @param dispositionLifeCycleNodeRef
     */
   private void setPDFIndicationFlag(NodeRef transferNodeRef, NodeRef dispositionLifeCycleNodeRef)
   {
      if (recordFolderService.isRecordFolder(dispositionLifeCycleNodeRef) == true)
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
              MimetypeMap.MIMETYPE_PDF.equals(contentData.getMimetype()) == true)
          {
              // Set the property indicator
              nodeService.setProperty(transferNodeRef, PROP_TRANSFER_PDF_INDICATOR, true);
          }
      }
   }

   /**
    * @see org.alfresco.module.org_alfresco_module_rm.transfer.TransferService#complete(NodeRef)
    */
    @Override
    public void complete(NodeRef nodeRef)
    {
        boolean accessionIndicator = ((Boolean)nodeService.getProperty(nodeRef, PROP_TRANSFER_ACCESSION_INDICATOR)).booleanValue();
        String transferLocation = nodeService.getProperty(nodeRef, PROP_TRANSFER_LOCATION).toString();

        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, ASSOC_TRANSFERRED, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef assoc : assocs)
        {
            markComplete(assoc.getChildRef(), accessionIndicator, transferLocation);
        }

        // Delete the transfer object
        nodeService.deleteNode(nodeRef);

        NodeRef transferNodeRef = (NodeRef) AlfrescoTransactionSupport.getResource(KEY_TRANSFER_NODEREF);
        if (transferNodeRef != null)
        {
            if (transferNodeRef.equals(nodeRef))
            {
                AlfrescoTransactionSupport.bindResource(KEY_TRANSFER_NODEREF, null);
            }
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
        if (accessionIndicator == true)
        {
            markerAspectQName = ASPECT_ASCENDED;
        }
        else
        {
            markerAspectQName = ASPECT_TRANSFERRED;
        }

        // Mark the object and children accordingly
        nodeService.addAspect(nodeRef, markerAspectQName, null);
        if (recordFolderService.isRecordFolder(nodeRef) == true)
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
