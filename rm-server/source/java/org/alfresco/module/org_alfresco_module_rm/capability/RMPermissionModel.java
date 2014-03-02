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
    static final String FILING = "Filing";
    static final String READ_RECORDS = "ReadRecords";
    static final String FILE_RECORDS = "FileRecords";

    // Roles
    static final String ROLE_NAME_USER = "User";
    static final String ROLE_NAME_POWER_USER = "PowerUser";
    static final String ROLE_NAME_SECURITY_OFFICER = "SecurityOfficer";
    static final String ROLE_NAME_RECORDS_MANAGER = "RecordsManager";

    static final String ROLE_NAME_ADMINISTRATOR = "Administrator";
    static final String ROLE_ADMINISTRATOR = SimplePermissionReference.getPermissionReference(RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT, ROLE_NAME_ADMINISTRATOR).toString();

    // Capability permissions

    static final String DECLARE_RECORDS = "DeclareRecords";

    static final String VIEW_RECORDS = "ViewRecords";

    static final String CREATE_MODIFY_DESTROY_FOLDERS = "CreateModifyDestroyFolders";

    static final String EDIT_RECORD_METADATA = "EditRecordMetadata";

    static final String EDIT_NON_RECORD_METADATA = "EditNonRecordMetadata";

    static final String ADD_MODIFY_EVENT_DATES = "AddModifyEventDates";

    static final String CLOSE_FOLDERS = "CloseFolders";

    static final String DECLARE_RECORDS_IN_CLOSED_FOLDERS = "DeclareRecordsInClosedFolders";

    static final String RE_OPEN_FOLDERS = "ReOpenFolders";

    static final String CYCLE_VITAL_RECORDS = "CycleVitalRecords";

    static final String PLANNING_REVIEW_CYCLES = "PlanningReviewCycles";

    static final String UPDATE_TRIGGER_DATES = "UpdateTriggerDates";

    static final String CREATE_MODIFY_DESTROY_EVENTS = "CreateModifyDestroyEvents";

    static final String MANAGE_ACCESS_RIGHTS = "ManageAccessRights";

    static final String MOVE_RECORDS = "MoveRecords";

    static final String CHANGE_OR_DELETE_REFERENCES = "ChangeOrDeleteReferences";

    static final String DELETE_LINKS = "DeleteLinks";

    static final String EDIT_DECLARED_RECORD_METADATA = "EditDeclaredRecordMetadata";

    static final String MANUALLY_CHANGE_DISPOSITION_DATES = "ManuallyChangeDispositionDates";

    static final String APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF = "ApproveRecordsScheduledForCutoff";

    static final String CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS = "CreateModifyRecordsInCutoffFolders";

    static final String EXTEND_RETENTION_PERIOD_OR_FREEZE = "ExtendRetentionPeriodOrFreeze";

    static final String UNFREEZE = "Unfreeze";

    static final String VIEW_UPDATE_REASONS_FOR_FREEZE = "ViewUpdateReasonsForFreeze";

    static final String DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION = "DestroyRecordsScheduledForDestruction";

    static final String DESTROY_RECORDS = "DestroyRecords";

    static final String UPDATE_VITAL_RECORD_CYCLE_INFORMATION = "UpdateVitalRecordCycleInformation";

    static final String UNDECLARE_RECORDS = "UndeclareRecords";

    static final String DECLARE_AUDIT_AS_RECORD = "DeclareAuditAsRecord";

    static final String DELETE_AUDIT = "DeleteAudit";

    static final String CREATE_MODIFY_DESTROY_TIMEFRAMES = "CreateModifyDestroyTimeframes";

    static final String AUTHORIZE_NOMINATED_TRANSFERS = "AuthorizeNominatedTransfers";

    static final String EDIT_SELECTION_LISTS = "EditSelectionLists";

    static final String AUTHORIZE_ALL_TRANSFERS = "AuthorizeAllTransfers";

    static final String CREATE_MODIFY_DESTROY_FILEPLAN_METADATA = "CreateModifyDestroyFileplanMetadata";

    static final String CREATE_AND_ASSOCIATE_SELECTION_LISTS = "CreateAndAssociateSelectionLists";

    static final String ATTACH_RULES_TO_METADATA_PROPERTIES = "AttachRulesToMetadataProperties";

    static final String CREATE_MODIFY_DESTROY_FILEPLAN_TYPES = "CreateModifyDestroyFileplanTypes";

    static final String CREATE_MODIFY_DESTROY_RECORD_TYPES = "CreateModifyDestroyRecordTypes";

    static final String MAKE_OPTIONAL_PARAMETERS_MANDATORY = "MakeOptionalParametersMandatory";

    static final String MAP_EMAIL_METADATA = "MapEmailMetadata";

    static final String DELETE_RECORDS = "DeleteRecords";

    static final String TRIGGER_AN_EVENT = "TriggerAnEvent";

    static final String CREATE_MODIFY_DESTROY_ROLES = "CreateModifyDestroyRoles";

    static final String CREATE_MODIFY_DESTROY_USERS_AND_GROUPS = "CreateModifyDestroyUsersAndGroups";

    static final String PASSWORD_CONTROL = "PasswordControl";

    static final String ENABLE_DISABLE_AUDIT_BY_TYPES = "EnableDisableAuditByTypes";

    static final String SELECT_AUDIT_METADATA = "SelectAuditMetadata";

    static final String DISPLAY_RIGHTS_REPORT = "DisplayRightsReport";

    static final String ACCESS_AUDIT = "AccessAudit";

    static final String EXPORT_AUDIT = "ExportAudit";

    static final String CREATE_MODIFY_DESTROY_REFERENCE_TYPES = "CreateModifyDestroyReferenceTypes";

    static final String UPDATE_CLASSIFICATION_DATES = "UpdateClassificationDates";

    static final String CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES = "CreateModifyDestroyClassificationGuides";

    static final String UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS = "UpgradeDowngradeAndDeclassifyRecords";

    static final String UPDATE_EXEMPTION_CATEGORIES = "UpdateExemptionCategories";

    static final String MAP_CLASSIFICATION_GUIDE_METADATA = "MapClassificationGuideMetadata";

    static final String MANAGE_ACCESS_CONTROLS = "ManageAccessControls";
}
