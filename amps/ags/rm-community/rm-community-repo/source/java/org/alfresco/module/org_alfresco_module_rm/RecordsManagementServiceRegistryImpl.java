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

package org.alfresco.module.org_alfresco_module_rm;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService;
import org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierService;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService;
import org.alfresco.module.org_alfresco_module_rm.security.FilePlanPermissionService;
import org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService;
import org.alfresco.module.org_alfresco_module_rm.transfer.TransferService;
import org.alfresco.repo.service.ServiceDescriptorRegistry;

/**
 * Records management service registry implementation
 *
 * @author Roy Wetherall
 */
@SuppressWarnings("deprecation")
public class RecordsManagementServiceRegistryImpl extends ServiceDescriptorRegistry
                                                  implements RecordsManagementServiceRegistry
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry#getRecordsManagementActionService()
     */
    @Override
    public RecordsManagementActionService getRecordsManagementActionService()
    {
        return (RecordsManagementActionService) getService(RECORDS_MANAGEMENT_ACTION_SERVICE);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry#getRecordsManagementAdminService()
     */
    @Override
    public RecordsManagementAdminService getRecordsManagementAdminService()
    {
        return (RecordsManagementAdminService) getService(RECORDS_MANAGEMENT_ADMIN_SERVICE);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry#getRecordsManagementEventService()
     */
    @Override
    public RecordsManagementEventService getRecordsManagementEventService()
    {
        return (RecordsManagementEventService) getService(RECORDS_MANAGEMENT_EVENT_SERVICE);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry#getRecordsManagementService()
     */
    @Override
    public RecordsManagementService getRecordsManagementService()
    {
        return (RecordsManagementService) getService(RECORDS_MANAGEMENT_SERVICE);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry#getRecordService()
     */
    public RecordService getRecordService()
    {
        return (RecordService) getService(RECORD_SERVICE);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry#getRecordsManagementSecurityService()
     */
    @Override
    @Deprecated
    public RecordsManagementSecurityService getRecordsManagementSecurityService()
    {
        return (RecordsManagementSecurityService) getService(RECORDS_MANAGEMENT_SECURITY_SERVICE);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry#getRecordsManagementAuditService()
     */
    @Override
    public RecordsManagementAuditService getRecordsManagementAuditService()
    {
        return (RecordsManagementAuditService) getService(RECORDS_MANAGEMENT_AUDIT_SERVICE);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry#getDictionaryService()
     */
    @Override
    public DispositionService getDispositionService()
    {
        return (DispositionService) getService(DISPOSITION_SERVICE);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry#getCapabilityService()
     */
    @Override
    public CapabilityService getCapabilityService()
    {
        return (CapabilityService) getService(CAPABILITY_SERVICE);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry#getFreezeService()
     */
    @Override
    public FreezeService getFreezeService()
    {
        return (FreezeService) getService(FREEZE_SERVICE);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry#getExtendedSecurityService()
     */
    @Override
    public ExtendedSecurityService getExtendedSecurityService()
    {
        return (ExtendedSecurityService) getService(EXTENDED_SECURITY_SERVICE);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry#getFilePlanService()
     */
    @Override
    public FilePlanService getFilePlanService()
    {
        return (FilePlanService) getService(FILE_PLAN_SERVICE);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry#getFilePlanRoleService()
     */
    @Override
    public FilePlanRoleService getFilePlanRoleService()
    {
        return (FilePlanRoleService) getService(FILE_PLAN_ROLE_SERVICE);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry#getFilePlanPermissionService()
     */
    @Override
    public FilePlanPermissionService getFilePlanPermissionService()
    {
        return (FilePlanPermissionService) getService(FILE_PLAN_PERMISSION_SERVICE);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry#getIdentifierService()
     */
    @Override
    public IdentifierService getIdentifierService()
    {
        return (IdentifierService) getService(IDENTIFIER_SERVICE);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry#getRecordFolderService()
     */
    @Override
    public RecordFolderService getRecordFolderService()
    {
        return (RecordFolderService) getService(RECORD_FOLDER_SERVICE);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry#getTransferService()
     */
    @Override
    public TransferService getTransferService()
    {
        return (TransferService) getService(TRANSFER_SERVICE);
    }
}
