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

package org.alfresco.module.org_alfresco_module_rm.action.dm;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.AuditableActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.InplaceRecordService;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Moves a record within a collaboration site.
 * The record can be moved only within the collaboration site where it was declared.
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class MoveDmRecordAction extends AuditableActionExecuterAbstractBase implements RecordsManagementModel
{
    /** Logger */
    private static Log logger = LogFactory.getLog(MoveDmRecordAction.class);

    /** Action name */
    public static final String NAME = "move-dm-record";

    /** Constant for target node reference parameter */
    public static final String PARAM_TARGET_NODE_REF = "targetNodeRef";

    /** Node service */
    private NodeService nodeService;

    /** Inplace record service */
    private InplaceRecordService inplaceRecordService;

    /**
     * Gets the node service
     *
     * @return Node service
     */
    protected NodeService getNodeService()
    {
        return this.nodeService;
    }

    /**
     * Sets the node service
     *
     * @param nodeService Node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Gets the inplace record service
     *
     * @return Inplace record service
     */
    protected InplaceRecordService getInplaceRecordService()
    {
        return this.inplaceRecordService;
    }

    /**
     * Sets the inplace record service
     *
     * @param inplaceRecordService Inplace record service
     */
    public void setInplaceRecordService(InplaceRecordService inplaceRecordService)
    {
        this.inplaceRecordService = inplaceRecordService;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        // Cannot move a document which is not a record
        if (!getNodeService().hasAspect(actionedUponNodeRef, ASPECT_RECORD) && logger.isDebugEnabled())
        {
            logger.debug("Cannot move the document, because '" + actionedUponNodeRef.toString() + "' is not a record.");
        }
        else
        {
            // Move the record within the collaboration site
            getInplaceRecordService().moveRecord(actionedUponNodeRef, getTargetNodeRef(action));
        }
    }

    /**
     * Helper method to get the target node reference from the action parameter
     *
     * @param action The action
     * @return Node reference of the target
     */
    private NodeRef getTargetNodeRef(Action action)
    {
        String targetNodeRef = (String) action.getParameterValue(PARAM_TARGET_NODE_REF);

        if (StringUtils.isBlank(targetNodeRef))
        {
            throw new AlfrescoRuntimeException("Could not find target node reference.");
        }

        return new NodeRef(targetNodeRef);
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        // Intentionally empty
    }
}
