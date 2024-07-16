/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.capability.policy;

import lombok.Getter;
import lombok.Setter;
import net.sf.acegisecurity.vote.AccessDecisionVoter;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.capability.declarative.DeclarativeCapability;
import org.alfresco.module.org_alfresco_module_rm.capability.impl.ChangeOrDeleteReferencesCapability;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Delete group capability implementation
 */
@Getter
@Setter
public class DeleteCapability extends DeclarativeCapability
{
    /**
     * record service
     */
    private RecordService recordService;

    /**
     * record folder service
     */
    private RecordFolderService recordFolderService;

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
     * @see org.alfresco.module.org_alfresco_module_rm.capability.Capability#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public int evaluate(NodeRef nodeRef)
    {
        return evaluate(nodeRef, null);
    }

    /**
     * Evaluate capability.
     *
     * @param destination destination node reference
     * @param linkee      linkee node reference, can be null
     * @return int
     */
    public int evaluate(NodeRef destination, NodeRef linkee)
    {
        if (getFilePlanService().isFilePlanComponent(destination))
        {
            // Build the conditions map
            Map<String, Boolean> conditions = new HashMap<>(2);

            if (permissionService.hasPermission(getFilePlanService().getFilePlan(destination),
                    RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION) == AccessStatus.ALLOWED)
            {
                conditions.put("capabilityCondition.cutoff", Boolean.TRUE);
                return checkConditionAndRecord(destination, conditions);
            }
            if (permissionService.hasPermission(getFilePlanService().getFilePlan(destination),
                            RMPermissionModel.DELETE_RECORDS) == AccessStatus.ALLOWED)
            {
                return checkConditionAndRecord(destination, conditions);
            }

        }

        if (((ChangeOrDeleteReferencesCapability) capabilityService.getCapability(CHANGE_OR_DELETE_REFERENCES)).evaluate(destination, linkee) == AccessDecisionVoter.ACCESS_GRANTED)
        {
            return AccessDecisionVoter.ACCESS_GRANTED;
        }
        if (capabilityService.getCapability(DELETE_LINKS).evaluate(destination) == AccessDecisionVoter.ACCESS_GRANTED)
        {
            return AccessDecisionVoter.ACCESS_GRANTED;
        }
        if (capabilityService.getCapability(DELETE_AUDIT).evaluate(destination) == AccessDecisionVoter.ACCESS_GRANTED)
        {
            return AccessDecisionVoter.ACCESS_GRANTED;
        }
        return AccessDecisionVoter.ACCESS_DENIED;
    }

    private int checkConditionAndRecord(NodeRef destination, Map<String, Boolean> conditions) {

        if (checkConditions(destination, conditions) && recordFolderService.isRecordFolder(destination))
        {
            return AccessDecisionVoter.ACCESS_GRANTED;
        }
        return 0;
    }
}
