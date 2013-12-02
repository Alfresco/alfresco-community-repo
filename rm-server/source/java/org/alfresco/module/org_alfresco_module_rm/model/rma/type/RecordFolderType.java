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
package org.alfresco.module.org_alfresco_module_rm.model.rma.type;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.module.org_alfresco_module_rm.model.behaviour.RecordsManagementSearchBehaviour;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.ArrayUtils;

/**
 * rma:recordFolder behaviour bean
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
@BehaviourBean
(
   defaultType = "rma:recordFolder"
)
public class RecordFolderType extends    BaseBehaviourBean
                              implements NodeServicePolicies.OnMoveNodePolicy,
                                         NodeServicePolicies.OnCreateChildAssociationPolicy
{    
    /** unwanted aspects */
    private QName[] unwantedAspects = 
    {
        ASPECT_VITAL_RECORD, 
        ASPECT_DISPOSITION_LIFECYCLE, 
        RecordsManagementSearchBehaviour.ASPECT_RM_SEARCH
    };
    
    /** record service */
    private RecordService recordService;
    
    /** record folder service */
    private RecordFolderService recordFolderService;
    
    /** disposition service */
    private DispositionService dispositionService;
    
    /**
     * @param recordService record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }
    
    /**
     * @param recordFolderService   record folder service
     */
    public void setRecordFolderService(RecordFolderService recordFolderService)
    {
        this.recordFolderService = recordFolderService;
    }
    
    /**
     * @param dispositionService    disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }
    
    /**
     * Record folder move behaviour
     * 
     * @see org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy#onMoveNode(org.alfresco.service.cmr.repository.ChildAssociationRef, org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            notificationFrequency = NotificationFrequency.FIRST_EVENT
    )
    public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        if (!nodeService.getType(newChildAssocRef.getParentRef()).equals(TYPE_RECORD_FOLDER))
        {
            if (!oldChildAssocRef.getParentRef().equals(newChildAssocRef.getParentRef()))
            {
                //final NodeRef oldNodeRef = oldChildAssocRef.getChildRef();
                final NodeRef newNodeRef = newChildAssocRef.getChildRef();

                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        behaviourFilter.disableBehaviour();
                        try
                        {
                            // Remove unwanted aspects
                            removeUnwantedAspects(nodeService, newNodeRef);

                            // reinitialise the record folder
                            recordFolderService.setupRecordFolder(newNodeRef);

                            // reinitialise the record folder disposition action details
                            dispositionService.refreshDispositionAction(newNodeRef);

                            // Sort out the child records
                            for (NodeRef record : recordService.getRecords(newNodeRef))
                            {
                                // Remove unwanted aspects
                                removeUnwantedAspects(nodeService, record);

                                // Re-initiate the records in the new folder.
                                recordService.file(record);
                            }
                        }
                        finally
                        {
                            behaviourFilter.enableBehaviour();
                        }

                        return null;
                    }
                }, AuthenticationUtil.getSystemUserName());
            }
        }
        else
        {
            throw new UnsupportedOperationException("Cannot move record folder into another record folder.");
        }
    }
    
    /**
     * Record folder copy callback
     */
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            policy = "alf:getCopyCallback"
    )
    public CopyBehaviourCallback getCopyCallback(final QName classRef, final CopyDetails copyDetails)
    {
        return new DefaultCopyBehaviourCallback()
        {
            @Override
            public Map<QName, Serializable> getCopyProperties(QName classRef, CopyDetails copyDetails,  Map<QName, Serializable> properties)
            {
                Map<QName, Serializable> sourceProperties = super.getCopyProperties(classRef, copyDetails, properties);

                // ensure that the 'closed' status of the record folder is not copied
                if (sourceProperties.containsKey(PROP_IS_CLOSED))
                {
                    sourceProperties.remove(PROP_IS_CLOSED);
                }

                return sourceProperties;
            }

            /**
             * If the targets parent is a Record Folder -- Do Not Allow Copy
             *
             * @param classQName
             * @param copyDetails
             * @return boolean
             */
            @Override
            public boolean getMustCopy(QName classQName, CopyDetails copyDetails)
            {
                boolean result = true;

                if (nodeService.getType(copyDetails.getTargetParentNodeRef()).equals(TYPE_RECORD_FOLDER) == true)
                {
                    result = false;
                }
                else if (ArrayUtils.contains(unwantedAspects, classQName) == true)
                {                    
                    result = false;
                }

                return result;
            }
        };
    }
    
    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy#onCreateChildAssociation(org.alfresco.service.cmr.repository.ChildAssociationRef, boolean)
     */
    @Override
    @Behaviour
    (
             kind = BehaviourKind.ASSOCIATION,
             notificationFrequency = NotificationFrequency.FIRST_EVENT
    )
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean bNew)
    {
        NodeRef nodeRef = childAssocRef.getChildRef();
        if (nodeService.exists(nodeRef) == true)
        {
            // ensure folders are never added to a record folder
            if (instanceOf(nodeRef, ContentModel.TYPE_FOLDER) == true)
            {
                throw new AlfrescoRuntimeException("You can't create a folder within an exisiting record folder.");
            }

            // ensure nothing is being added to a closed record folder
            NodeRef recordFolder = childAssocRef.getParentRef();
            Boolean isClosed = (Boolean) nodeService.getProperty(recordFolder, PROP_IS_CLOSED);
            if (isClosed != null && Boolean.TRUE.equals(isClosed) == true)
            {
                throw new AlfrescoRuntimeException("You can't add new items to a closed record folder.");
            }
        }
    }
    
    /**
     * Removes unwanted aspects
     *
     * @param nodeService
     * @param nodeRef
     */
    private void removeUnwantedAspects(NodeService nodeService, NodeRef nodeRef)
    {
        // Remove unwanted aspects
        for (QName aspect : unwantedAspects)
        {
            if (nodeService.hasAspect(nodeRef, aspect) == true)
            {
                nodeService.removeAspect(nodeRef, aspect);
            }
        }
    }
}
