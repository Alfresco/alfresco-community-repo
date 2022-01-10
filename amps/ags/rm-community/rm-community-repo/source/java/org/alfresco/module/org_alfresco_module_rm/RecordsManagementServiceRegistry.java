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
import org.alfresco.module.org_alfresco_module_rm.model.security.ModelSecurityService;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService;
import org.alfresco.module.org_alfresco_module_rm.security.FilePlanPermissionService;
import org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService;
import org.alfresco.module.org_alfresco_module_rm.transfer.TransferService;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Records management service registry
 *
 * @author Roy Wetherall
 */
@SuppressWarnings("deprecation")
public interface RecordsManagementServiceRegistry extends ServiceRegistry
{
    /** Service QName constants */
    QName RECORDS_MANAGEMENT_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "RecordsManagementService");
    QName DISPOSITION_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "DispositionService");
    QName RECORDS_MANAGEMENT_ADMIN_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "RecordsManagementAdminService");
    QName RECORDS_MANAGEMENT_ACTION_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "RecordsManagementActionService");
    QName RECORDS_MANAGEMENT_EVENT_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "RecordsManagementEventService");
    QName RECORDS_MANAGEMENT_AUDIT_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "RecordsManagementAuditService");
    QName CAPABILITY_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "CapabilityService");
    QName RECORD_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "RecordService");
    QName FREEZE_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "FreezeService");
    QName EXTENDED_SECURITY_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "ExtendedSecurityService");
    QName FILE_PLAN_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "FilePlanService");
    QName FILE_PLAN_ROLE_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "FilePlanRoleService");
    QName FILE_PLAN_PERMISSION_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "FilePlanPermissionService");
    QName FILE_PLAN_AUTHENTICATION_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "FilePlanAuthenticationService");
    QName IDENTIFIER_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "recordsManagementIdentifierService");
    QName RECORD_FOLDER_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "RecordFolderService");
    QName TRANSFER_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "TransferService");
    @Deprecated
    QName RECORDS_MANAGEMENT_SECURITY_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "RecordsManagementSecurityService");


    /**
     * @return  records management service
     */
    @NotAuditable
    RecordsManagementService getRecordsManagementService();

    /**
     * @return  record service
     */
    @NotAuditable
    RecordService getRecordService();

    /**
     * @return disposition service
     */
    @NotAuditable
    DispositionService getDispositionService();

    /**
     * @return  records management admin service
     */
    @NotAuditable
    RecordsManagementAdminService getRecordsManagementAdminService();

    /**
     * @return  records management action service
     */
    @NotAuditable
    RecordsManagementActionService getRecordsManagementActionService();

    /**
     * @return  records management event service
     */
    @NotAuditable
    RecordsManagementEventService getRecordsManagementEventService();

    /**
     * @return  records management security service
     *
     * @deprecated As of release 2.1, replaced with {@link FilePlanRoleService}, {@link FilePlanPermissionService} and {@link ModelSecurityService}
     */
    @Deprecated
    @NotAuditable
    RecordsManagementSecurityService getRecordsManagementSecurityService();

    /**
     * @return  records management audit service
     */
    @NotAuditable
    RecordsManagementAuditService getRecordsManagementAuditService();

    /**
     * @return  capability service
     * @since 2.0
     */
    @NotAuditable
    CapabilityService getCapabilityService();

    /**
     * @return  freeze service
     * @since 2.1
     */
    @NotAuditable
    FreezeService getFreezeService();

    /**
     * @return extended security service
     * @since 2.1
     */
    @NotAuditable
    ExtendedSecurityService getExtendedSecurityService();

    /**
     * @return  file plan service
     * @since 2.1
     */
    @NotAuditable
    FilePlanService getFilePlanService();

    /**
     * @return  file plan role service
     * @since 2.1
     */
    @NotAuditable
    FilePlanRoleService getFilePlanRoleService();

    /**
     * @return  file plan permission service
     * @since 2.1
     */
    @NotAuditable
    FilePlanPermissionService getFilePlanPermissionService();

    /**
     * @return identifier service
     * @since 2.1
     */
    IdentifierService getIdentifierService();

    /**
     * @return  record folder service
     * @since 2.2
     */
    @NotAuditable
    RecordFolderService getRecordFolderService();

    /**
     * @return  transfer service
     * @since 2.2
     */
    @NotAuditable
    TransferService getTransferService();
}
