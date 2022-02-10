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

package org.alfresco.module.org_alfresco_module_rm.model.behaviour;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Abstract disposable item, containing commonality between record and record folder.
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
public abstract class AbstractDisposableItem extends BaseBehaviourBean
{
    /** unwanted aspects */
    protected QName[] unwantedAspects =
    {
        ASPECT_VITAL_RECORD,
        ASPECT_DISPOSITION_LIFECYCLE,
        RecordsManagementSearchBehaviour.ASPECT_RM_SEARCH
    };
    
    /** disposition service */
    protected DispositionService dispositionService;

    /** record service */
    protected RecordService recordService;

    /** record folder service */
    protected RecordFolderService recordFolderService;

    /**
     * @param dispositionService    disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    /**
     * @param recordService    record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    /**
     * @param recordFolderService    record folder service
     */
    public void setRecordFolderService(RecordFolderService recordFolderService)
    {
        this.recordFolderService = recordFolderService;
    }
    
    /**
     * Removes unwanted aspects
     *
     * @param nodeService
     * @param nodeRef
     */
    protected void cleanDisposableItem(NodeService nodeService, NodeRef nodeRef)
    {
        // Remove unwanted aspects
        for (QName aspect : unwantedAspects)
        {
            if (nodeService.hasAspect(nodeRef, aspect))
            {
                nodeService.removeAspect(nodeRef, aspect);
            }
        }
        
        // remove the current disposition action (if there is one)
        DispositionAction dispositionAction = dispositionService.getNextDispositionAction(nodeRef);
        if (dispositionAction != null)
        {
            nodeService.deleteNode(dispositionAction.getNodeRef());
        }
    }

    /**
     * Cleans and re-initiates the containing records
     *
     * @param childAssociationRef
     */
    protected void reinitializeRecordFolder(ChildAssociationRef childAssociationRef)
    {

        NodeRef newNodeRef = childAssociationRef.getChildRef();

        AuthenticationUtil.runAs(() -> {
            // clean record folder
            cleanDisposableItem(nodeService, newNodeRef);

            // re-initialise the record folder
            recordFolderService.setupRecordFolder(newNodeRef);

            // sort out the child records
            for (NodeRef record : recordService.getRecords(newNodeRef))
            {
                // clean record
                cleanDisposableItem(nodeService, record);

                // Re-initiate the records in the new folder.
                recordService.file(record);
            }

            return null;
        }, AuthenticationUtil.getSystemUserName());
    }

}
