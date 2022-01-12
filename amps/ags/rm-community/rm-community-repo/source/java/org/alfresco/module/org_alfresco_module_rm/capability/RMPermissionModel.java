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

package org.alfresco.module.org_alfresco_module_rm.capability;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;

/**
 * Capability constants for the RM Permission Model
 *
 * @author andyh
 */
public interface RMPermissionModel
{
    // Assignment of Filing
    String FILING = "Filing";
    String READ_RECORDS = "ReadRecords";
    String FILE_RECORDS = "FileRecords";

    // Roles
    /**
     * @deprecated as of 2.1.0.3, please use {@link FilePlanRoleService#ROLE_USER} instead
     */
    @Deprecated
    String ROLE_NAME_USER = FilePlanRoleService.ROLE_USER;
    /**
     * @deprecated as of 2.1.0.3, please use {@link FilePlanRoleService#ROLE_POWER_USER} instead
     */
    @Deprecated
    String ROLE_NAME_POWER_USER = FilePlanRoleService.ROLE_POWER_USER;
    /**
     * @deprecated as of 2.1.0.3, please use {@link FilePlanRoleService#ROLE_SECURITY_OFFICER} instead
     */
    @Deprecated
    String ROLE_NAME_SECURITY_OFFICER = FilePlanRoleService.ROLE_SECURITY_OFFICER;
    /**
     * @deprecated as of 2.1.0.3, please use {@link FilePlanRoleService#ROLE_RECORDS_MANAGER} instead
     */
    @Deprecated
    String ROLE_NAME_RECORDS_MANAGER = FilePlanRoleService.ROLE_RECORDS_MANAGER;
    /**
     * @deprecated as of 2.1.0.3, please use {@link FilePlanRoleService#ROLE_ADMIN} instead
     */
    @Deprecated
    String ROLE_NAME_ADMINISTRATOR = FilePlanRoleService.ROLE_ADMIN;
    String ROLE_ADMINISTRATOR = SimplePermissionReference.getPermissionReference(RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT, FilePlanRoleService.ROLE_ADMIN).toString();

    // Capability permissions
    String DECLARE_RECORDS = "DeclareRecords";
    String VIEW_RECORDS = "ViewRecords";
    String CREATE_RECORDS = "CreateRecords";
    String CREATE_MODIFY_DESTROY_FOLDERS = "CreateModifyDestroyFolders";
    String EDIT_RECORD_METADATA = "EditRecordMetadata";
    String EDIT_NON_RECORD_METADATA = "EditNonRecordMetadata";
    String ADD_MODIFY_EVENT_DATES = "AddModifyEventDates";
    String CLOSE_FOLDERS = "CloseFolders";
    String DECLARE_RECORDS_IN_CLOSED_FOLDERS = "DeclareRecordsInClosedFolders";
    String RE_OPEN_FOLDERS = "ReOpenFolders";
    String CYCLE_VITAL_RECORDS = "CycleVitalRecords";
    String PLANNING_REVIEW_CYCLES = "PlanningReviewCycles";
    String UPDATE_TRIGGER_DATES = "UpdateTriggerDates";
    String CREATE_MODIFY_DESTROY_EVENTS = "CreateModifyDestroyEvents";
    String MANAGE_ACCESS_RIGHTS = "ManageAccessRights";
    String MOVE_RECORDS = "MoveRecords";
    String CHANGE_OR_DELETE_REFERENCES = "ChangeOrDeleteReferences";
    String DELETE_LINKS = "DeleteLinks";
    String EDIT_DECLARED_RECORD_METADATA = "EditDeclaredRecordMetadata";
    String MANUALLY_CHANGE_DISPOSITION_DATES = "ManuallyChangeDispositionDates";
    String APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF = "ApproveRecordsScheduledForCutoff";
    String CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS = "CreateModifyRecordsInCutoffFolders";
    String EXTEND_RETENTION_PERIOD_OR_FREEZE = "ExtendRetentionPeriodOrFreeze";
    String UNFREEZE = "Unfreeze";
    String VIEW_UPDATE_REASONS_FOR_FREEZE = "ViewUpdateReasonsForFreeze";
    String DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION = "DestroyRecordsScheduledForDestruction";
    String DESTROY_RECORDS = "DestroyRecords";
    String UPDATE_VITAL_RECORD_CYCLE_INFORMATION = "UpdateVitalRecordCycleInformation";
    String UNDECLARE_RECORDS = "UndeclareRecords";
    String DECLARE_AUDIT_AS_RECORD = "DeclareAuditAsRecord";
    String DELETE_AUDIT = "DeleteAudit";
    String CREATE_MODIFY_DESTROY_TIMEFRAMES = "CreateModifyDestroyTimeframes";
    String AUTHORIZE_NOMINATED_TRANSFERS = "AuthorizeNominatedTransfers";
    String EDIT_SELECTION_LISTS = "EditSelectionLists";
    String AUTHORIZE_ALL_TRANSFERS = "AuthorizeAllTransfers";
    String CREATE_MODIFY_DESTROY_FILEPLAN_METADATA = "CreateModifyDestroyFileplanMetadata";
    String CREATE_AND_ASSOCIATE_SELECTION_LISTS = "CreateAndAssociateSelectionLists";
    String ATTACH_RULES_TO_METADATA_PROPERTIES = "AttachRulesToMetadataProperties";
    String CREATE_MODIFY_DESTROY_FILEPLAN_TYPES = "CreateModifyDestroyFileplanTypes";
    String CREATE_MODIFY_DESTROY_RECORD_TYPES = "CreateModifyDestroyRecordTypes";
    String MAKE_OPTIONAL_PARAMETERS_MANDATORY = "MakeOptionalParametersMandatory";
    String MAP_EMAIL_METADATA = "MapEmailMetadata";
    String DELETE_RECORDS = "DeleteRecords";
    String TRIGGER_AN_EVENT = "TriggerAnEvent";
    String CREATE_MODIFY_DESTROY_ROLES = "CreateModifyDestroyRoles";
    String CREATE_MODIFY_DESTROY_USERS_AND_GROUPS = "CreateModifyDestroyUsersAndGroups";
    String PASSWORD_CONTROL = "PasswordControl";
    String ENABLE_DISABLE_AUDIT_BY_TYPES = "EnableDisableAuditByTypes";
    String SELECT_AUDIT_METADATA = "SelectAuditMetadata";
    String DISPLAY_RIGHTS_REPORT = "DisplayRightsReport";
    String ACCESS_AUDIT = "AccessAudit";
    String EXPORT_AUDIT = "ExportAudit";
    String CREATE_MODIFY_DESTROY_REFERENCE_TYPES = "CreateModifyDestroyReferenceTypes";
    String UPDATE_CLASSIFICATION_DATES = "UpdateClassificationDates";
    String CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES = "CreateModifyDestroyClassificationGuides";
    String UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS = "UpgradeDowngradeAndDeclassifyRecords";
    String UPDATE_EXEMPTION_CATEGORIES = "UpdateExemptionCategories";
    String MAP_CLASSIFICATION_GUIDE_METADATA = "MapClassificationGuideMetadata";
    String MANAGE_ACCESS_CONTROLS = "ManageAccessControls";
    String CREATE_HOLD = "CreateHold";
    String ADD_TO_HOLD = "AddToHold";
    String REMOVE_FROM_HOLD = "RemoveFromHold";
}
