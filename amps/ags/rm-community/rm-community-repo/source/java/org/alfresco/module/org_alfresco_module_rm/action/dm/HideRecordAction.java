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

import org.alfresco.module.org_alfresco_module_rm.action.AuditableActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.InplaceRecordService;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Hides a record within a collaboration site.
 *
 * Note: This is a 'normal' dm action, rather than a records management action.
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class HideRecordAction extends AuditableActionExecuterAbstractBase
                              implements RecordsManagementModel
{

    /** Logger */
    private static Log logger = LogFactory.getLog(HideRecordAction.class);

    /** Action name */
    public static final String NAME = "hide-record";

    /** Node service */
    private NodeService nodeService;

    /** Inplace record service */
    private InplaceRecordService inplaceRecordService;

    /**
     * @param nodeService node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param inplaceRecordService inplace record service
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
        if (!nodeService.hasAspect(actionedUponNodeRef, ASPECT_RECORD))
        {
            // we cannot hide a document which is not a record
            if (logger.isDebugEnabled())
            {
                logger.debug("Cannot hide the document, because '" + actionedUponNodeRef.toString() + "' is not a record.");
            }
        }
        else
        {
            // hide the record from the collaboration site
            inplaceRecordService.hideRecord(actionedUponNodeRef);
        }
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
