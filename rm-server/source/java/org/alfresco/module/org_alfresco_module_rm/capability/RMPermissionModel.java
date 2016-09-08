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

    public static final String FILING = "Filing";

    public static final String READ_RECORDS = "ReadRecords";

    public static final String FILE_RECORDS = "FileRecords";
    
    // Roles

    public static final String ROLE_NAME_USER = "User";
    public static final String ROLE_USER = SimplePermissionReference.getPermissionReference(RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT, ROLE_NAME_USER).toString();

    public static final String ROLE_NAME_POWER_USER = "PowerUser";
    public static final String ROLE_POWER_USER = SimplePermissionReference.getPermissionReference(RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT, ROLE_NAME_POWER_USER).toString();

    public static final String ROLE_NAME_SECURITY_OFFICER = "SecurityOfficer";
    public static final String ROLE_SECURITY_OFFICER = SimplePermissionReference.getPermissionReference(RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT, ROLE_NAME_SECURITY_OFFICER)
            .toString();

    public static final String ROLE_NAME_RECORDS_MANAGER = "RecordsManager";
    public static final String ROLE_RECORDS_MANAGER = SimplePermissionReference.getPermissionReference(RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT, ROLE_NAME_RECORDS_MANAGER)
            .toString();

    public static final String ROLE_NAME_ADMINISTRATOR = "Administrator";
    public static final String ROLE_ADMINISTRATOR = SimplePermissionReference.getPermissionReference(RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT, ROLE_NAME_ADMINISTRATOR).toString();

    // Capability permissions

    public static final String DECLARE_RECORDS = "DeclareRecords";

    public static final String VIEW_RECORDS = "ViewRecords";

    public static final String CREATE_MODIFY_DESTROY_FOLDERS = "CreateModifyDestroyFolders";

    public static final String EDIT_RECORD_METADATA = "EditRecordMetadata";

    public static final String EDIT_NON_RECORD_METADATA = "EditNonRecordMetadata";

    public static final String ADD_MODIFY_EVENT_DATES = "AddModifyEventDates";

    public static final String CLOSE_FOLDERS = "CloseFolders";

    public static final String DECLARE_RECORDS_IN_CLOSED_FOLDERS = "DeclareRecordsInClosedFolders";

    public static final String RE_OPEN_FOLDERS = "ReOpenFolders";

    public static final String CYCLE_VITAL_RECORDS = "CycleVitalRecords";

    public static final String PLANNING_REVIEW_CYCLES = "PlanningReviewCycles";

    public static final String UPDATE_TRIGGER_DATES = "UpdateTriggerDates";

    public static final String CREATE_MODIFY_DESTROY_EVENTS = "CreateModifyDestroyEvents";

    public static final String MANAGE_ACCESS_RIGHTS = "ManageAccessRights";

    public static final String MOVE_RECORDS = "MoveRecords";

    public static final String CHANGE_OR_DELETE_REFERENCES = "ChangeOrDeleteReferences";

    public static final String DELETE_LINKS = "DeleteLinks";

    public static final String EDIT_DECLARED_RECORD_METADATA = "EditDeclaredRecordMetadata";

    public static final String MANUALLY_CHANGE_DISPOSITION_DATES = "ManuallyChangeDispositionDates";

    public static final String APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF = "ApproveRecordsScheduledForCutoff";

    public static final String CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS = "CreateModifyRecordsInCutoffFolders";

    public static final String EXTEND_RETENTION_PERIOD_OR_FREEZE = "ExtendRetentionPeriodOrFreeze";

    public static final String UNFREEZE = "Unfreeze";

    public static final String VIEW_UPDATE_REASONS_FOR_FREEZE = "ViewUpdateReasonsForFreeze";

    public static final String DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION = "DestroyRecordsScheduledForDestruction";

    public static final String DESTROY_RECORDS = "DestroyRecords";

    public static final String UPDATE_VITAL_RECORD_CYCLE_INFORMATION = "UpdateVitalRecordCycleInformation";

    public static final String UNDECLARE_RECORDS = "UndeclareRecords";

    public static final String DECLARE_AUDIT_AS_RECORD = "DeclareAuditAsRecord";

    public static final String DELETE_AUDIT = "DeleteAudit";

    public static final String CREATE_MODIFY_DESTROY_TIMEFRAMES = "CreateModifyDestroyTimeframes";

    public static final String AUTHORIZE_NOMINATED_TRANSFERS = "AuthorizeNominatedTransfers";

    public static final String EDIT_SELECTION_LISTS = "EditSelectionLists";

    public static final String AUTHORIZE_ALL_TRANSFERS = "AuthorizeAllTransfers";

    public static final String CREATE_MODIFY_DESTROY_FILEPLAN_METADATA = "CreateModifyDestroyFileplanMetadata";

    public static final String CREATE_AND_ASSOCIATE_SELECTION_LISTS = "CreateAndAssociateSelectionLists";

    public static final String ATTACH_RULES_TO_METADATA_PROPERTIES = "AttachRulesToMetadataProperties";

    public static final String CREATE_MODIFY_DESTROY_FILEPLAN_TYPES = "CreateModifyDestroyFileplanTypes";

    public static final String CREATE_MODIFY_DESTROY_RECORD_TYPES = "CreateModifyDestroyRecordTypes";

    public static final String MAKE_OPTIONAL_PARAMETERS_MANDATORY = "MakeOptionalParametersMandatory";

    public static final String MAP_EMAIL_METADATA = "MapEmailMetadata";

    public static final String DELETE_RECORDS = "DeleteRecords";

    public static final String TRIGGER_AN_EVENT = "TriggerAnEvent";

    public static final String CREATE_MODIFY_DESTROY_ROLES = "CreateModifyDestroyRoles";

    public static final String CREATE_MODIFY_DESTROY_USERS_AND_GROUPS = "CreateModifyDestroyUsersAndGroups";

    public static final String PASSWORD_CONTROL = "PasswordControl";

    public static final String ENABLE_DISABLE_AUDIT_BY_TYPES = "EnableDisableAuditByTypes";

    public static final String SELECT_AUDIT_METADATA = "SelectAuditMetadata";

    public static final String DISPLAY_RIGHTS_REPORT = "DisplayRightsReport";

    public static final String ACCESS_AUDIT = "AccessAudit";

    public static final String EXPORT_AUDIT = "ExportAudit";

    public static final String CREATE_MODIFY_DESTROY_REFERENCE_TYPES = "CreateModifyDestroyReferenceTypes";

    public static final String UPDATE_CLASSIFICATION_DATES = "UpdateClassificationDates";

    public static final String CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES = "CreateModifyDestroyClassificationGuides";

    public static final String UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS = "UpgradeDowngradeAndDeclassifyRecords";

    public static final String UPDATE_EXEMPTION_CATEGORIES = "UpdateExemptionCategories";

    public static final String MAP_CLASSIFICATION_GUIDE_METADATA = "MapClassificationGuideMetadata";

    public static final String MANAGE_ACCESS_CONTROLS = "ManageAccessControls";
}
