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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Relinquish Hold Action
 * 
 * @author Roy Wetherall
 */
public class RelinquishHoldAction extends RMActionExecuterAbstractBase
{
    /** Logger */
    private static Log logger = LogFactory.getLog(RelinquishHoldAction.class);
    
    /** I18N */
    private static final String MSG_NOT_HOLD_TYPE = "rm.action.not-hold-type";

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        QName nodeType = this.nodeService.getType(actionedUponNodeRef);
        if (this.dictionaryService.isSubClass(nodeType, TYPE_HOLD) == true)
        {
            final NodeRef holdBeingRelinquished = actionedUponNodeRef;
            List<ChildAssociationRef> frozenNodeAssocs = nodeService.getChildAssocs(holdBeingRelinquished, ASSOC_FROZEN_RECORDS, RegexQNamePattern.MATCH_ALL);
            
            if (logger.isDebugEnabled())
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Relinquishing hold ").append(holdBeingRelinquished)
                    .append(" which has ").append(frozenNodeAssocs.size()).append(" frozen node(s).");
                logger.debug(msg.toString());
            }
            
            for (ChildAssociationRef assoc : frozenNodeAssocs)
            {
                final NodeRef nextFrozenNode = assoc.getChildRef();
                
                // Remove the freeze if this is the only hold that references the node
                removeFreeze(nextFrozenNode, holdBeingRelinquished);
            }
            
            if (logger.isDebugEnabled())
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Deleting hold object ").append(holdBeingRelinquished)
                    .append(" with name ").append(nodeService.getProperty(holdBeingRelinquished, ContentModel.PROP_NAME));
                logger.debug(msg.toString());
            }
            
            // Delete the hold node
            this.nodeService.deleteNode(holdBeingRelinquished);
        }
        else
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_NOT_HOLD_TYPE, TYPE_HOLD.toString(), actionedUponNodeRef.toString()));
        }
    }
    
    /**
     * Removes a freeze from a node
     * 
     * @param nodeRef   node reference
     */
    private void removeFreeze(NodeRef nodeRef, NodeRef holdBeingRelinquished)
    {
        // We should only remove the frozen aspect if there are no other 'holds' in effect for this node.
        // One complication to consider is that holds can be placed on records or on folders.
        // Therefore if the nodeRef here is a record, we need to go up the containment hierarchy looking
        // for holds at each level.

        // Get all the holds and remove this node from them.
        List<ChildAssociationRef> parentAssocs = this.nodeService.getParentAssocs(nodeRef, ASSOC_FROZEN_RECORDS, RegexQNamePattern.MATCH_ALL);
        // If the nodeRef is a record, there could also be applicable holds as parents of the folder(s).
        if (recordsManagementService.isRecord(nodeRef))
        {
            List<NodeRef> parentFolders = recordsManagementService.getRecordFolders(nodeRef);
            for (NodeRef folder : parentFolders)
            {
                List<ChildAssociationRef> moreAssocs = nodeService.getParentAssocs(folder, ASSOC_FROZEN_RECORDS, RegexQNamePattern.MATCH_ALL);
                parentAssocs.addAll(moreAssocs);
            }
        }
        
        if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Removing freeze from ").append(nodeRef).append(" which has ")
                .append(parentAssocs.size()).append(" holds");
            logger.debug(msg.toString());
        }

        boolean otherHoldsAreInEffect = false;
        for (ChildAssociationRef chAssRef : parentAssocs)
        {
            if (!chAssRef.getParentRef().equals(holdBeingRelinquished))
            {
                otherHoldsAreInEffect = true;
                break;
            }
        }
        
        if (!otherHoldsAreInEffect)
        {
            if (logger.isDebugEnabled())
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Removing frozen aspect from ").append(nodeRef);
                logger.debug(msg.toString());
            }

            // Remove the aspect
            this.nodeService.removeAspect(nodeRef, ASPECT_FROZEN);
        }
        
        // Remove the freezes on the child records as long as there is no other hold referencing them
        if (this.recordsManagementService.isRecordFolder(nodeRef) == true)
        {
            if (logger.isDebugEnabled())
            {
                StringBuilder msg = new StringBuilder();
                msg.append(nodeRef).append(" is a record folder");
                logger.debug(msg.toString());
            }
            for (NodeRef record : recordsManagementService.getRecords(nodeRef))
            {
                removeFreeze(record, holdBeingRelinquished);
            }
        }

    }
    
    @Override
    public Set<QName> getProtectedAspects()
    {
        HashSet<QName> qnames = new HashSet<QName>();
        qnames.add(ASPECT_FROZEN);
        return qnames;
    }

    @Override
    protected boolean isExecutableImpl(NodeRef filePlanComponent, Map<String, Serializable> parameters, boolean throwException)
    {
        QName nodeType = this.nodeService.getType(filePlanComponent);
        if (this.dictionaryService.isSubClass(nodeType, TYPE_HOLD) == true)
        {
            return true;
        }
        else
        {
            if(throwException)
            {
                throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_NOT_HOLD_TYPE, TYPE_HOLD.toString(), filePlanComponent.toString()));
            }
            else
            {
                return false;
            }
        }
    }

    
}