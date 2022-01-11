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

package org.alfresco.module.org_alfresco_module_rm.model;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.namespace.QName;

/**
 * Helper class containing records management qualified names
 *
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public interface RecordsManagementModel extends RecordsManagementCustomModel
{
    // Namespace details
    String RM_URI = "http://www.alfresco.org/model/recordsmanagement/1.0";
    String RM_PREFIX = "rma";

    // Model
    QName RM_MODEL = QName.createQName(RM_URI, "recordsmanagement");

    // RM Site
    QName TYPE_RM_SITE = QName.createQName(RM_URI, "rmsite");

    // Caveat config
    QName TYPE_CAVEAT_CONFIG = QName.createQName(RM_URI, "caveatConfig");

    QName ASPECT_CAVEAT_CONFIG_ROOT = QName.createQName(RM_URI, "caveatConfigRoot");
    QName ASSOC_CAVEAT_CONFIG = QName.createQName(RM_URI, "caveatConfigAssoc");

    // Email config
    QName TYPE_EMAIL_CONFIG = QName.createQName(RM_URI, "emailConfig");
    QName ASPECT_EMAIL_CONFIG_ROOT = QName.createQName(RM_URI, "emailConfigRoot");
    QName ASSOC_EMAIL_CONFIG = QName.createQName(RM_URI, "emailConfigAssoc");

    // Records management container
    QName TYPE_RECORDS_MANAGEMENT_CONTAINER = QName.createQName(RM_URI, "recordsManagementContainer");

    // Record Category
    QName TYPE_RECORD_CATEGORY = QName.createQName(RM_URI, "recordCategory");

    // Records management root container
    QName TYPE_FILE_PLAN = QName.createQName(RM_URI, "filePlan");

    // Unfiled record container
    QName TYPE_UNFILED_RECORD_CONTAINER = QName.createQName(RM_URI, "unfiledRecordContainer");

    // Unfiled record folder
    QName TYPE_UNFILED_RECORD_FOLDER = QName.createQName(RM_URI, "unfiledRecordFolder");

    // Hold container
    QName TYPE_HOLD_CONTAINER = QName.createQName(RM_URI, "holdContainer");

    // Transfer container
    QName TYPE_TRANSFER_CONTAINER = QName.createQName(RM_URI, "transferContainer");

    // Disposition instructions aspect
    QName ASPECT_SCHEDULED = QName.createQName(RM_URI, "scheduled");
    QName ASSOC_DISPOSITION_SCHEDULE = QName.createQName(RM_URI, "dispositionSchedule");

    // Disposition definition type
    QName TYPE_DISPOSITION_SCHEDULE = QName.createQName(RM_URI, "dispositionSchedule");
    QName PROP_DISPOSITION_AUTHORITY = QName.createQName(RM_URI, "dispositionAuthority");
    QName PROP_DISPOSITION_INSTRUCTIONS = QName.createQName(RM_URI, "dispositionInstructions");
    QName PROP_RECORD_LEVEL_DISPOSITION = QName.createQName(RM_URI, "recordLevelDisposition");
    QName ASSOC_DISPOSITION_ACTION_DEFINITIONS = QName.createQName(RM_URI, "dispositionActionDefinitions");

    // Disposition action type
    QName TYPE_DISPOSITION_ACTION_DEFINITION = QName.createQName(RM_URI, "dispositionActionDefinition");
    QName PROP_DISPOSITION_ACTION_NAME = QName.createQName(RM_URI, "dispositionActionName");
    QName PROP_DISPOSITION_DESCRIPTION = QName.createQName(RM_URI, "dispositionDescription");
    QName PROP_DISPOSITION_PERIOD = QName.createQName(RM_URI, "dispositionPeriod");
    QName PROP_DISPOSITION_PERIOD_PROPERTY = QName.createQName(RM_URI, "dispositionPeriodProperty");
    QName PROP_DISPOSITION_EVENT = QName.createQName(RM_URI, "dispositionEvent");
    QName PROP_DISPOSITION_EVENT_COMBINATION = QName.createQName(RM_URI, "dispositionEventCombination");
    QName PROP_COMBINE_DISPOSITION_STEP_CONDITIONS = QName.createQName(RM_URI, "combineDispositionStepConditions");
    QName PROP_DISPOSITION_LOCATION = QName.createQName(RM_URI, "dispositionLocation");
    QName PROP_DISPOSITION_ACTION_GHOST_ON_DESTROY = QName.createQName(RM_URI, "dispositionActionGhostOnDestroy");

    // Records folder
    QName TYPE_RECORD_FOLDER = QName.createQName(RM_URI, "recordFolder");
    QName PROP_IS_CLOSED = QName.createQName(RM_URI, "isClosed");

    // Declared record aspect
    QName ASPECT_DECLARED_RECORD = QName.createQName(RM_URI, "declaredRecord");
    QName PROP_DECLARED_AT = QName.createQName(RM_URI, "declaredAt");
    QName PROP_DECLARED_BY = QName.createQName(RM_URI, "declaredBy");

    // Record aspect
    QName ASPECT_RECORD = QName.createQName(RM_URI, "record");
    QName PROP_DATE_FILED = QName.createQName(RM_URI, "dateFiled");
    QName PROP_ORIGIONAL_NAME = QName.createQName(RM_URI, "origionalName");

    // Common record details
    QName ASPECT_COMMON_RECORD_DETAILS = QName.createQName(RM_URI, "commonRecordDetails");
    QName PROP_LOCATION = QName.createQName(RM_URI, "location");

    // Fileable aspect
    QName ASPECT_FILABLE = QName.createQName(RM_URI, "fileable");

    // Record component identifier aspect
    QName ASPECT_RECORD_COMPONENT_ID = QName.createQName(RM_URI, "recordComponentIdentifier");
    QName PROP_IDENTIFIER = QName.createQName(RM_URI, "identifier");
    QName PROP_DB_UNIQUENESS_ID = QName.createQName(RM_URI, "dbUniquenessId");

    // Vital record definition aspect
    QName ASPECT_VITAL_RECORD_DEFINITION = QName.createQName(RM_URI, "vitalRecordDefinition");
    QName PROP_VITAL_RECORD_INDICATOR = QName.createQName(RM_URI, "vitalRecordIndicator");
    QName PROP_REVIEW_PERIOD = QName.createQName(RM_URI, "reviewPeriod");

    // Vital record aspect
    QName ASPECT_VITAL_RECORD = QName.createQName(RM_URI, "vitalRecord");
    QName PROP_REVIEW_AS_OF = QName.createQName(RM_URI, "reviewAsOf");

    // Cut off aspect
    QName ASPECT_CUT_OFF = QName.createQName(RM_URI, "cutOff");
    QName PROP_CUT_OFF_DATE = QName.createQName(RM_URI, "cutOffDate");

    // Uncut off aspect
    QName ASPECT_UNCUT_OFF = QName.createQName(RM_URI, "uncutOff");

    // Transferred aspect
    QName ASPECT_TRANSFERRED = QName.createQName(RM_URI, "transferred");

    // Ascended aspect
    QName ASPECT_ASCENDED = QName.createQName(RM_URI, "ascended");

    // Disposition schedule aspect
    QName ASPECT_DISPOSITION_LIFECYCLE = QName.createQName(RM_URI, "dispositionLifecycle");
    QName ASSOC_NEXT_DISPOSITION_ACTION = QName.createQName(RM_URI, "nextDispositionAction");
    QName ASSOC_DISPOSITION_ACTION_HISTORY = QName.createQName(RM_URI, "dispositionActionHistory");

    // Disposition action type
    QName TYPE_DISPOSITION_ACTION = QName.createQName(RM_URI, "dispositionAction");
    QName PROP_DISPOSITION_ACTION_ID = QName.createQName(RM_URI, "dispositionActionId");
    QName PROP_DISPOSITION_ACTION = QName.createQName(RM_URI, "dispositionAction");
    QName PROP_DISPOSITION_AS_OF = QName.createQName(RM_URI, "dispositionAsOf");
    /** A flag indicating that the "disposition as of" date has been manually set and shouldn't be changed. */
    QName PROP_MANUALLY_SET_AS_OF = QName.createQName(RM_URI, "manuallySetAsOf");
    QName PROP_DISPOSITION_EVENTS_ELIGIBLE = QName.createQName(RM_URI, "dispositionEventsEligible");
    QName PROP_DISPOSITION_ACTION_STARTED_AT = QName.createQName(RM_URI, "dispositionActionStartedAt");
    QName PROP_DISPOSITION_ACTION_STARTED_BY = QName.createQName(RM_URI, "dispositionActionStartedBy");
    QName PROP_DISPOSITION_ACTION_COMPLETED_AT = QName.createQName(RM_URI, "dispositionActionCompletedAt");
    QName PROP_DISPOSITION_ACTION_COMPLETED_BY = QName.createQName(RM_URI, "dispositionActionCompletedBy");
    QName ASSOC_EVENT_EXECUTIONS = QName.createQName(RM_URI, "eventExecutions");

    // Event execution type
    QName TYPE_EVENT_EXECUTION = QName.createQName(RM_URI, "eventExecution");
    QName PROP_EVENT_EXECUTION_NAME = QName.createQName(RM_URI, "eventExecutionName");
    QName PROP_EVENT_EXECUTION_AUTOMATIC = QName.createQName(RM_URI, "eventExecutionAutomatic");
    QName PROP_EVENT_EXECUTION_COMPLETE = QName.createQName(RM_URI, "eventExecutionComplete");
    QName PROP_EVENT_EXECUTION_COMPLETED_BY = QName.createQName(RM_URI, "eventExecutionCompletedBy");
    QName PROP_EVENT_EXECUTION_COMPLETED_AT = QName.createQName(RM_URI, "eventExecutionCompletedAt");

    // Custom RM data aspect
    QName ASPECT_CUSTOM_RM_DATA = QName.createQName(RM_URI, "customRMData");

    // marker aspect on all RM objercts (except caveat root)
    QName ASPECT_FILE_PLAN_COMPONENT = QName.createQName(RM_URI, "filePlanComponent");
    QName PROP_ROOT_NODEREF = QName.createQName(RM_URI, "rootNodeRef");

    // Non-electronic document
    QName TYPE_NON_ELECTRONIC_DOCUMENT = QName.createQName(RM_URI, "nonElectronicDocument");

    // Records management root aspect
    QName ASPECT_RECORDS_MANAGEMENT_ROOT = QName.createQName(RM_URI, "recordsManagementRoot");
    // since 2.1
    @Deprecated
    QName ASSOC_HOLDS = QName.createQName(RM_URI, "holds");
    // since 2.1
    @Deprecated
    QName ASSOC_TRANSFERS = QName.createQName(RM_URI, "transfers");

    // Hold type
    QName TYPE_HOLD = QName.createQName(RM_URI, "hold");
    QName PROP_HOLD_REASON = QName.createQName(RM_URI, "holdReason");
    //since 3.2
    @Deprecated
    QName ASSOC_FROZEN_RECORDS = QName.createQName(RM_URI, "frozenRecords");
    QName ASSOC_FROZEN_CONTENT = QName.createQName(RM_URI, "frozenContent");

    // Record meta data aspect
    QName ASPECT_RECORD_META_DATA = QName.createQName(RM_URI, "recordMetaData");

    // Frozen aspect
    QName ASPECT_FROZEN = QName.createQName(RM_URI, "frozen");
    QName PROP_FROZEN_AT = QName.createQName(RM_URI, "frozenAt");
    QName PROP_FROZEN_BY = QName.createQName(RM_URI, "frozenBy");

    // Transfer aspect
    QName TYPE_TRANSFER = QName.createQName(RM_URI, "transfer");
    QName PROP_TRANSFER_ACCESSION_INDICATOR = QName.createQName(RM_URI, "transferAccessionIndicator");
    QName PROP_TRANSFER_PDF_INDICATOR = QName.createQName(RM_URI, "transferPDFIndicator");
    QName PROP_TRANSFER_LOCATION = QName.createQName(RM_URI, "transferLocation");
    QName ASSOC_TRANSFERRED = QName.createQName(RM_URI, "transferred");

    // Transferring aspect
    QName ASPECT_TRANSFERRING = QName.createQName(RM_URI, "transferring");

    // Versioned record aspect
    QName ASPECT_VERSIONED_RECORD = QName.createQName(RM_URI, "versionedRecord");

    // Unpublished update aspect
    QName ASPECT_UNPUBLISHED_UPDATE = QName.createQName(RM_URI, "unpublishedUpdate");
    QName PROP_UNPUBLISHED_UPDATE = QName.createQName(RM_URI, "unpublishedUpdate");
    QName PROP_UPDATE_TO = QName.createQName(RM_URI, "updateTo");
    QName PROP_UPDATED_PROPERTIES = QName.createQName(RM_URI, "updatedProperties");
    QName PROP_PUBLISH_IN_PROGRESS = QName.createQName(RM_URI, "publishInProgress");
    String UPDATE_TO_DISPOSITION_ACTION_DEFINITION = "dispositionActionDefinition";

    // Ghosted aspect
    QName ASPECT_GHOSTED = QName.createQName(RM_URI, "ghosted");

    // Search rollup aspect
    QName ASPECT_RM_SEARCH = QName.createQName(RM_URI, "recordSearch");
    QName PROP_RS_DISPOSITION_ACTION_NAME = QName.createQName(RM_URI, "recordSearchDispositionActionName");
    QName PROP_RS_DISPOSITION_ACTION_AS_OF = QName.createQName(RM_URI, "recordSearchDispositionActionAsOf");
    QName PROP_RS_DISPOSITION_EVENTS_ELIGIBLE = QName.createQName(RM_URI, "recordSearchDispositionEventsEligible");
    QName PROP_RS_DISPOSITION_EVENTS = QName.createQName(RM_URI, "recordSearchDispositionEvents");
    QName PROP_RS_VITAL_RECORD_REVIEW_PERIOD = QName.createQName(RM_URI, "recordSearchVitalRecordReviewPeriod");
    QName PROP_RS_VITAL_RECORD_REVIEW_PERIOD_EXPRESSION = QName.createQName(RM_URI, "recordSearchVitalRecordReviewPeriodExpression");
    QName PROP_RS_DISPOSITION_PERIOD = QName.createQName(RM_URI, "recordSearchDispositionPeriod");
    QName PROP_RS_DISPOSITION_PERIOD_EXPRESSION = QName.createQName(RM_URI, "recordSearchDispositionPeriodExpression");
    QName PROP_RS_HAS_DISPOITION_SCHEDULE = QName.createQName(RM_URI, "recordSearchHasDispositionSchedule");
    QName PROP_RS_DISPOITION_INSTRUCTIONS = QName.createQName(RM_URI, "recordSearchDispositionInstructions");
    QName PROP_RS_DISPOITION_AUTHORITY = QName.createQName(RM_URI, "recordSearchDispositionAuthority");
    QName PROP_RS_DECLASSIFICATION_REVIEW_COMPLETED_AT = QName.createQName(RM_URI, "declassificationReviewCompletedAt");
    QName PROP_RS_DECLASSIFICATION_REVIEW_COMPLETED_BY = QName.createQName(RM_URI, "declassificationReviewCompletedBy");
    /** @depreacted as of 2.2, because disposable items can now be in multiple holds */
    @Deprecated
    QName PROP_RS_HOLD_REASON = QName.createQName(RM_URI, "recordSearchHoldReason");

    // Loaded Data Set Ids
    QName ASPECT_LOADED_DATA_SET_ID = QName.createQName(RM_URI, "loadedDataSetId");
    QName PROP_LOADED_DATA_SET_IDS = QName.createQName(RM_URI, "loadedDataSetIds");

    // Extended security aspect
    // @deprecated as of 2.5, because of performance issues
    @Deprecated QName ASPECT_EXTENDED_SECURITY = QName.createQName(RM_URI, "extendedSecurity");
    @Deprecated QName PROP_READERS = QName.createQName(RM_URI, "readers");
    @Deprecated QName PROP_WRITERS = QName.createQName(RM_URI, "writers");

    // Originating details of a record
    QName ASPECT_RECORD_ORIGINATING_DETAILS = QName.createQName(RM_URI, "recordOriginatingDetails");
    QName PROP_RECORD_ORIGINATING_USER_ID = QName.createQName(RM_URI, "recordOriginatingUserId");
    QName PROP_RECORD_ORIGINATING_CREATION_DATE = QName.createQName(RM_URI, "recordOriginatingCreationDate");
    QName PROP_RECORD_ORIGINATING_LOCATION = QName.createQName(RM_URI, "recordOriginatingLocation");

    // Rejection details of a record
    QName ASPECT_RECORD_REJECTION_DETAILS = QName.createQName(RM_URI, "recordRejectionDetails");
    QName PROP_RECORD_REJECTION_USER_ID = QName.createQName(RM_URI, "recordRejectionUserId");
    QName PROP_RECORD_REJECTION_DATE = QName.createQName(RM_URI, "recordRejectionDate");
    QName PROP_RECORD_REJECTION_REASON = QName.createQName(RM_URI, "recordRejectionReason");

    // Held children aspect
    // @since 2.2
    QName ASPECT_HELD_CHILDREN = QName.createQName(RM_URI, "heldChildren");
    QName PROP_HELD_CHILDREN_COUNT = QName.createQName(RM_URI, "heldChildrenCount");

    // Countable aspect
    QName ASPECT_COUNTABLE = QName.createQName(RM_URI, "countable");
    QName PROP_COUNT = QName.createQName(RM_URI, "count");

    QName ASPECT_SAVED_SEARCH = QName.createQName(RM_URI, "savedSearch");
    //Workaround for RM-6788
    String GL_URI = "http://www.alfresco.org/model/glacier/1.0";
    QName ASPECT_ARCHIVED = QName.createQName(GL_URI, "archived");

    QName ASPECT_DISPOSITION_PROCESSED = QName.createQName(RM_URI, "dispositionProcessed");
}
