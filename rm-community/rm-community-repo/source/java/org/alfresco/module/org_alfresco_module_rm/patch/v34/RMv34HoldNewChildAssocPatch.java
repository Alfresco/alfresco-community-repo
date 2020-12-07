/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.module.org_alfresco_module_rm.patch.v34;

import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.ASSOC_FROZEN_CONTENT;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.ASSOC_FROZEN_RECORDS;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldService;
import org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * Patch to create new hold child association to link the record to the hold
 *
 * See: https://alfresco.atlassian.net/browse/APPS-659
 *
 *
 * @since 3.4.1
 */
public class RMv34HoldNewChildAssocPatch extends AbstractModulePatch
{
    /**
     * Data abstraction layer for QName and Namespace entities.
     */
    private QNameDAO qnameDAO;

    /**
     * File plan service interface
     */
    private FilePlanService filePlanService;

    /**
     * Hold service interface.
     */
    private HoldService holdService;

    /**
     * Interface for public and internal node and store operations.
     */
    private NodeService nodeService;

    /**
     * Setter for fileplanservice
     * @param filePlanService File plan service interface
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * Setter for hold service
     * @param holdService Hold service interface.
     */
    public void setHoldService(HoldService holdService)
    {
        this.holdService = holdService;
    }

    /**
     * Setter for node service
     * @param nodeService Interface for public and internal node and store operations.
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    @Override
    public void applyInternal()
    {
        for (NodeRef filePlan : filePlanService.getFilePlans())
        {
            for (NodeRef hold : holdService.getHolds(filePlan))
            {
                for (ChildAssociationRef ref : nodeService.getChildAssocs(hold))
                {
                    holdService.removeFromHold(hold, ref.getChildRef());
                    holdService.addToHold(hold, ref.getChildRef());
                }
            }
        }
    }
}
