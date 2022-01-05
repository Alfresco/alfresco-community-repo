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

package org.alfresco.module.org_alfresco_module_rm.capability.impl;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.capability.declarative.DeclarativeCapability;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;

import net.sf.acegisecurity.vote.AccessDecisionVoter;

/**
 * Create group capability implementation
 *
 * @author Andy Hind
 */
public class CreateCapability extends DeclarativeCapability
{
    /** record service */
    private RecordService recordService;

    /** record folder service */
    private RecordFolderService recordFolderService;

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
     * @see org.alfresco.module.org_alfresco_module_rm.capability.Capability#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public int evaluate(NodeRef nodeRef)
    {
        return evaluate(nodeRef, null, null);
    }

    /**
     * Evaluate capability.
     *
     * @param destination   destination node reference
     * @param linkee        linkee node reference, can be null
     * @param assocType     association type, can be null
     * @return
     */
    public int evaluate(NodeRef destination, NodeRef linkee, QName assocType)
    {
        if (linkee != null)
        {
            int state = checkRead(linkee, true);
            if (state != AccessDecisionVoter.ACCESS_GRANTED)
            {
                return AccessDecisionVoter.ACCESS_DENIED;
            }
        }
        if (getFilePlanService().isFilePlanComponent(destination))
        {
            if ((assocType == null) || !assocType.equals(ContentModel.ASSOC_CONTAINS))
            {
                if (linkee == null)
                {
                    if (recordService.isRecord(destination) &&
                        !recordService.isDeclared(destination) &&
                        permissionService.hasPermission(destination, RMPermissionModel.FILE_RECORDS) == AccessStatus.ALLOWED)
                    {
                        return AccessDecisionVoter.ACCESS_GRANTED;
                    }
                }
                else
                {
                    if (recordService.isRecord(linkee) &&
                            recordService.isRecord(destination) &&
                            !recordService.isDeclared(destination) &&
                            permissionService.hasPermission(destination, RMPermissionModel.FILE_RECORDS) == AccessStatus.ALLOWED)
                    {
                        return AccessDecisionVoter.ACCESS_GRANTED;
                    }
                }

            }

            // Build the conditions map
            Map<String, Boolean> conditions = new HashMap<>(5);
            conditions.put("capabilityCondition.filling", Boolean.TRUE);
            conditions.put("capabilityCondition.frozen", Boolean.FALSE);
            conditions.put("capabilityCondition.closed", Boolean.FALSE);
            conditions.put("capabilityCondition.cutoff", Boolean.FALSE);

            // if the destination folder is not a record folder and the user has filling capability on it, grant access to create the record
            if (checkConditions(destination, conditions) &&
                   !recordFolderService.isRecordFolder(destination) )
            {
                return AccessDecisionVoter.ACCESS_GRANTED;
            }

            if (checkConditions(destination, conditions) &&
                    recordFolderService.isRecordFolder(destination) &&
                    permissionService.hasPermission(destination, RMPermissionModel.FILE_RECORDS) == AccessStatus.ALLOWED)
            {
                return AccessDecisionVoter.ACCESS_GRANTED;
            }

            conditions.put("capabilityCondition.closed", Boolean.TRUE);
            if (checkConditions(destination, conditions) &&
                    recordFolderService.isRecordFolder(destination) &&
                    permissionService.hasPermission(getFilePlanService().getFilePlan(destination), RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS) == AccessStatus.ALLOWED)
            {
                return AccessDecisionVoter.ACCESS_GRANTED;
            }

            conditions.remove("capabilityCondition.closed");
            conditions.put("capabilityCondition.cutoff", Boolean.TRUE);
            if (checkConditions(destination, conditions) &&
                    recordFolderService.isRecordFolder(destination) &&
                    permissionService.hasPermission(getFilePlanService().getFilePlan(destination), RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS) == AccessStatus.ALLOWED)
            {
                return AccessDecisionVoter.ACCESS_GRANTED;
            }
        }
        if (capabilityService.getCapability(RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS).evaluate(destination) == AccessDecisionVoter.ACCESS_GRANTED)
        {
            return AccessDecisionVoter.ACCESS_GRANTED;
        }
        if (capabilityService.getCapability(RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS).evaluate(destination) == AccessDecisionVoter.ACCESS_GRANTED)
        {
            return AccessDecisionVoter.ACCESS_GRANTED;
        }
        if (capabilityService.getCapability(RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS).evaluate(destination) == AccessDecisionVoter.ACCESS_GRANTED)
        {
            return AccessDecisionVoter.ACCESS_GRANTED;
        }
        if (capabilityService.getCapability(RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA).evaluate(destination) == AccessDecisionVoter.ACCESS_GRANTED)
        {
            return AccessDecisionVoter.ACCESS_GRANTED;
        }
        if (capabilityService.getCapability(RMPermissionModel.CREATE_HOLD).evaluate(destination) == AccessDecisionVoter.ACCESS_GRANTED)
        {
            return AccessDecisionVoter.ACCESS_GRANTED;
        }
        if (((ChangeOrDeleteReferencesCapability)capabilityService.getCapability(RMPermissionModel.CHANGE_OR_DELETE_REFERENCES)).evaluate(destination, linkee) == AccessDecisionVoter.ACCESS_GRANTED)
        {
            return AccessDecisionVoter.ACCESS_GRANTED;
        }
        return AccessDecisionVoter.ACCESS_DENIED;
    }
}
