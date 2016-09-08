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

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Unfreeze Action
 * 
 * @author Roy Wetherall
 */
public class UnfreezeAction extends RMActionExecuterAbstractBase
{
    /** Logger */
    private static Log logger = LogFactory.getLog(UnfreezeAction.class);

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (this.nodeService.hasAspect(actionedUponNodeRef, ASPECT_FROZEN) == true)
        {
            final boolean isFolder = this.recordsManagementService.isRecordFolder(actionedUponNodeRef);

            if (logger.isDebugEnabled())
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Unfreezing node ").append(actionedUponNodeRef);
                if (isFolder)
                {
                    msg.append(" (folder)");
                }
                logger.debug(msg.toString());
            }

            // Remove freeze from node
            removeFreeze(actionedUponNodeRef);

            // Remove freeze from records if a record folder
            if (isFolder)
            {
                List<NodeRef> records = this.recordsManagementService.getRecords(actionedUponNodeRef);
                for (NodeRef record : records)
                {
                    removeFreeze(record);
                }
            }
        }
    }

    /**
     * Removes a freeze from a node
     * 
     * @param nodeRef
     *            node reference
     */
    private void removeFreeze(NodeRef nodeRef)
    {
        // Get all the holds and remove this node from them
        List<ChildAssociationRef> assocs = this.nodeService.getParentAssocs(nodeRef, ASSOC_FROZEN_RECORDS, RegexQNamePattern.MATCH_ALL);

        if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Removing freeze from node ").append(nodeRef)
                .append("which has ").append(assocs.size()).append(" holds");
            logger.debug(msg.toString());
        }

        for (ChildAssociationRef assoc : assocs)
        {
            // Remove the frozen node as a child
            NodeRef holdNodeRef = assoc.getParentRef();
            this.nodeService.removeChild(holdNodeRef, nodeRef);

            if (logger.isDebugEnabled())
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Removed frozen node from hold ").append(holdNodeRef);
                logger.debug(msg.toString());
            }

            // Check to see if we should delete the hold
            List<ChildAssociationRef> holdAssocs = this.nodeService.getChildAssocs(holdNodeRef, ASSOC_FROZEN_RECORDS, RegexQNamePattern.MATCH_ALL);
            if (holdAssocs.size() == 0)
            {
                if (logger.isDebugEnabled())
                {
                    StringBuilder msg = new StringBuilder();
                    msg.append("Hold node ").append(holdNodeRef)
                        .append(" with name ").append(nodeService.getProperty(holdNodeRef, ContentModel.PROP_NAME))
                        .append(" has no frozen nodes. Hence deleting it.");
                    logger.debug(msg.toString());
                }
                
                // Delete the hold object
                this.nodeService.deleteNode(holdNodeRef);
            }
        }

        // Remove the aspect
        this.nodeService.removeAspect(nodeRef, ASPECT_FROZEN);

        if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Removed frozen aspect from ").append(nodeRef);
            logger.debug(msg.toString());
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
        return this.nodeService.hasAspect(filePlanComponent, ASPECT_FROZEN);
    }

}