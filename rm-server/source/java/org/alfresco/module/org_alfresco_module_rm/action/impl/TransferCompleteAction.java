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

import java.util.Date;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Transfer complete action
 * 
 * @author Roy Wetherall
 */
public class TransferCompleteAction extends RMActionExecuterAbstractBase
{
    /** I18N */
    private static final String MSG_NODE_NOT_TRANSFER = "rm.action.node-not-transfer";

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        QName className = this.nodeService.getType(actionedUponNodeRef);
        if (this.dictionaryService.isSubClass(className, TYPE_TRANSFER) == true)
        {
            boolean accessionIndicator = ((Boolean)nodeService.getProperty(actionedUponNodeRef, PROP_TRANSFER_ACCESSION_INDICATOR)).booleanValue();
            String transferLocation = nodeService.getProperty(actionedUponNodeRef, PROP_TRANSFER_LOCATION).toString();
            
            List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(actionedUponNodeRef, ASSOC_TRANSFERRED, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef assoc : assocs)
            {
                markComplete(assoc.getChildRef(), accessionIndicator, transferLocation);
            }

            // Delete the transfer object
            this.nodeService.deleteNode(actionedUponNodeRef);

            NodeRef transferNodeRef = (NodeRef) AlfrescoTransactionSupport.getResource(TransferAction.KEY_TRANSFER_NODEREF);
            if (transferNodeRef != null)
            {
                if (transferNodeRef.equals(actionedUponNodeRef))
                {
                    AlfrescoTransactionSupport.bindResource(TransferAction.KEY_TRANSFER_NODEREF, null);
                }
            }
        }
        else
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_NODE_NOT_TRANSFER));
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
        if (recordsManagementService.isRecordFolder(nodeRef) == true)
        {
            List<NodeRef> records = recordsManagementService.getRecords(nodeRef);
            for (NodeRef record : records)
            {
                nodeService.addAspect(record, markerAspectQName, null);
                nodeService.setProperty(record, PROP_LOCATION, transferLocation);
            }
        }

        // Update to the next disposition action
        updateNextDispositionAction(nodeRef);
    }
}
