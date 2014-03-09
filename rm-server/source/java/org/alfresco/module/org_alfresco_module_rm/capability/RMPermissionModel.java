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
package org.alfresco.module.org_alfresco_module_rm.capability;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;

/**
 * Capability constants for the RM Permission Model
 *
 * @author andyh
 */
public interface RMPermissionModel
{
    // Assignment of Filing
    final String FILING = "Filing";
    final String READ_RECORDS = "ReadRecords";
    final String FILE_RECORDS = "FileRecords";

    // Roles
    final String ROLE_NAME_USER = "User";
    final String ROLE_NAME_POWER_USER = "PowerUser";
    final String ROLE_NAME_SECURITY_OFFICER = "SecurityOfficer";
    final String ROLE_NAME_RECORDS_MANAGER = "RecordsManager";

    final String ROLE_NAME_ADMINISTRATOR = "Administrator";
    final String ROLE_ADMINISTRATOR = SimplePermissionReference.getPermissionReference(RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT, ROLE_NAME_ADMINISTRATOR).toString();

    // Capability permissions

    final String DECLARE_RECORDS = "DeclareRecords";

    final String VIEW_RECORDS = "ViewRecords";

    final String CREATE_MODIFY_DESTROY_FOLDERS = "CreateModifyDestroyFolders";

    final String EDIT_RECORD_METADATA = "EditRecordMetadata";

    final String EDIT_NON_RECORD_METADATA = "EditNonRecordMetadata";

    final String ADD_MODIFY_EVENT_DATES = "AddModifyEventDates";

    final String CLOSE_FOLDERS = "CloseFolders";

    final String DECLARE_RECORDS_IN_CLOSED_FOLDERS = "DeclareRecordsInClosedFolders";

    final String RE_OPEN_FOLDERS = "ReOpenFolders";

    final String CYCLE_VITAL_RECORDS = "CycleVitalRecords";

    final String PLANNING_REVIEW_CYCLES = "PlanningReviewCycles";

    final String UPDATE_TRIGGER_DATES = "UpdateTriggerDates";

    final String CREATE_MODIFY_DESTROY_EVENTS = "CreateModifyDestroyEvents";

    final String MANAGE_ACCESS_RIGHTS = "ManageAccessRights";

    final String MOVE_RECORDS = "MoveRecords";

    final String CHANGE_OR_DELETE_REFERENCES = "ChangeOrDeleteReferences";

    final String DELETE_LINKS = "DeleteLinks";

    final String EDIT_DECLARED_RECORD_METADATA = "EditDeclaredRecordMetadata";

    final String MANUALLY_CHANGE_DISPOSITION_DATES = "ManuallyChangeDispositionDates";

    final String APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF = "ApproveRecordsScheduledForCutoff";

    final String CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS = "CreateModifyRecordsInCutoffFolders";

    final String EXTEND_RETENTION_PERIOD_OR_FREEZE = "ExtendRetentionPeriodOrFreeze";

    final String UNFREEZE = "Unfreeze";

    final String VIEW_UPDATE_REASONS_FOR_FREEZE = "ViewUpdateReasonsForFreeze";

    final String DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION = "DestroyRecordsScheduledForDestruction";

    final String DESTROY_RECORDS = "DestroyRecords";

    final String UPDATE_VITAL_RECORD_CYCLE_INFORMATION = "UpdateVitalRecordCycleInformation";

    final String UNDECLARE_RECORDS = "UndeclareRecords";

    final String DECLARE_AUDIT_AS_RECORD = "DeclareAuditAsRecord";

    final String DELETE_AUDIT = "DeleteAudit";

    final String CREATE_MODIFY_DESTROY_TIMEFRAMES = "CreateModifyDestroyTimeframes";

    final String AUTHORIZE_NOMINATED_TRANSFERS = "AuthorizeNominatedTransfers";

    final String EDIT_SELECTION_LISTS = "EditSelectionLists";

    final String AUTHORIZE_ALL_TRANSFERS = "AuthorizeAllTransfers";

    final String CREATE_MODIFY_DESTROY_FILEPLAN_METADATA = "CreateModifyDestroyFileplanMetadata";

    final String CREATE_AND_ASSOCIATE_SELECTION_LISTS = "CreateAndAssociateSelectionLists";

    final String ATTACH_RULES_TO_METADATA_PROPERTIES = "AttachRulesToMetadataProperties";

    final String CREATE_MODIFY_DESTROY_FILEPLAN_TYPES = "CreateModifyDestroyFileplanTypes";

    final String CREATE_MODIFY_DESTROY_RECORD_TYPES = "CreateModifyDestroyRecordTypes";

    final String MAKE_OPTIONAL_PARAMETERS_MANDATORY = "MakeOptionalParametersMandatory";

    final String MAP_EMAIL_METADATA = "MapEmailMetadata";

    final String DELETE_RECORDS = "DeleteRecords";

    final String TRIGGER_AN_EVENT = "TriggerAnEvent";

    final String CREATE_MODIFY_DESTROY_ROLES = "CreateModifyDestroyRoles";

    final String CREATE_MODIFY_DESTROY_USERS_AND_GROUPS = "CreateModifyDestroyUsersAndGroups";

    final String PASSWORD_CONTROL = "PasswordControl";

    final String ENABLE_DISABLE_AUDIT_BY_TYPES = "EnableDisableAuditByTypes";

    final String SELECT_AUDIT_METADATA = "SelectAuditMetadata";

    final String DISPLAY_RIGHTS_REPORT = "DisplayRightsReport";

    final String ACCESS_AUDIT = "AccessAudit";

    final String EXPORT_AUDIT = "ExportAudit";

    final String CREATE_MODIFY_DESTROY_REFERENCE_TYPES = "CreateModifyDestroyReferenceTypes";

    final String UPDATE_CLASSIFICATION_DATES = "UpdateClassificationDates";

    final String CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES = "CreateModifyDestroyClassificationGuides";

    final String UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS = "UpgradeDowngradeAndDeclassifyRecords";

    final String UPDATE_EXEMPTION_CATEGORIES = "UpdateExemptionCategories";

    final String MAP_CLASSIFICATION_GUIDE_METADATA = "MapClassificationGuideMetadata";

    final String MANAGE_ACCESS_CONTROLS = "ManageAccessControls";
}
