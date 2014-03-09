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
package org.alfresco.module.org_alfresco_module_rm.model;

import org.alfresco.service.namespace.QName;

/**
 * Helper class containing records management qualified names
 *
 * @author Roy Wetherall
 */
public interface RecordsManagementModel extends RecordsManagementCustomModel
{
    // Namespace details
    static final String RM_URI = "http://www.alfresco.org/model/recordsmanagement/1.0";
    static final String RM_PREFIX = "rma";

    // Model
    static final QName RM_MODEL = QName.createQName(RM_URI, "recordsmanagement");

    // RM Site
    static final QName TYPE_RM_SITE = QName.createQName(RM_URI, "rmsite");

    // Caveat config
    static final QName TYPE_CAVEAT_CONFIG = QName.createQName(RM_URI, "caveatConfig");

    static final QName ASPECT_CAVEAT_CONFIG_ROOT = QName.createQName(RM_URI, "caveatConfigRoot");
    static final QName ASSOC_CAVEAT_CONFIG = QName.createQName(RM_URI, "caveatConfigAssoc");

    // Email config
    static final QName TYPE_EMAIL_CONFIG = QName.createQName(RM_URI, "emailConfig");
    static final QName ASPECT_EMAIL_CONFIG_ROOT = QName.createQName(RM_URI, "emailConfigRoot");
    static final QName ASSOC_EMAIL_CONFIG = QName.createQName(RM_URI, "emailConfigAssoc");

    // Records management container
    static final QName TYPE_RECORDS_MANAGEMENT_CONTAINER = QName.createQName(RM_URI, "recordsManagementContainer");

    // Record Category
    static final QName TYPE_RECORD_CATEGORY = QName.createQName(RM_URI, "recordCategory");

    // Records management root container
    static final QName TYPE_FILE_PLAN = QName.createQName(RM_URI, "filePlan");

    // Unfiled record container
    static final QName TYPE_UNFILED_RECORD_CONTAINER = QName.createQName(RM_URI, "unfiledRecordContainer");

    // Unfiled record container child
    static final QName TYPE_UNFILED_RECORD_CONTAINER_CHILD = QName.createQName(RM_URI, "unfiledRecordContainerChild");

    // Hold container
    static final QName TYPE_HOLD_CONTAINER = QName.createQName(RM_URI, "holdContainer");
    static final QName TYPE_HOLD_CONTAINER_CHILD = QName.createQName(RM_URI, "holdContainerChild");

    // Transfer container
    static final QName TYPE_TRANSFER_CONTAINER = QName.createQName(RM_URI, "transferContainer");

    // Disposition instructions aspect
    static final QName ASPECT_SCHEDULED = QName.createQName(RM_URI, "scheduled");
    static final QName ASSOC_DISPOSITION_SCHEDULE = QName.createQName(RM_URI, "dispositionSchedule");

    // Disposition definition type
    static final QName TYPE_DISPOSITION_SCHEDULE = QName.createQName(RM_URI, "dispositionSchedule");
    static final QName PROP_DISPOSITION_AUTHORITY = QName.createQName(RM_URI, "dispositionAuthority");
    static final QName PROP_DISPOSITION_INSTRUCTIONS = QName.createQName(RM_URI, "dispositionInstructions");
    static final QName PROP_RECORD_LEVEL_DISPOSITION = QName.createQName(RM_URI, "recordLevelDisposition");
    static final QName ASSOC_DISPOSITION_ACTION_DEFINITIONS = QName.createQName(RM_URI, "dispositionActionDefinitions");

    // Disposition action type
    static final QName TYPE_DISPOSITION_ACTION_DEFINITION = QName.createQName(RM_URI, "dispositionActionDefinition");
    static final QName PROP_DISPOSITION_ACTION_NAME = QName.createQName(RM_URI, "dispositionActionName");
    static final QName PROP_DISPOSITION_DESCRIPTION = QName.createQName(RM_URI, "dispositionDescription");
    static final QName PROP_DISPOSITION_PERIOD = QName.createQName(RM_URI, "dispositionPeriod");
    static final QName PROP_DISPOSITION_PERIOD_PROPERTY = QName.createQName(RM_URI, "dispositionPeriodProperty");
    static final QName PROP_DISPOSITION_EVENT = QName.createQName(RM_URI, "dispositionEvent");
    static final QName PROP_DISPOSITION_EVENT_COMBINATION = QName.createQName(RM_URI, "dispositionEventCombination");
    static final QName PROP_DISPOSITION_LOCATION = QName.createQName(RM_URI, "dispositionLocation");

    // Records folder
    static final QName TYPE_RECORD_FOLDER = QName.createQName(RM_URI, "recordFolder");
    static final QName PROP_IS_CLOSED = QName.createQName(RM_URI, "isClosed");

    // Declared record aspect
    static final QName ASPECT_DECLARED_RECORD = QName.createQName(RM_URI, "declaredRecord");
    static final QName PROP_DECLARED_AT = QName.createQName(RM_URI, "declaredAt");
    static final QName PROP_DECLARED_BY = QName.createQName(RM_URI, "declaredBy");

    // Record aspect
    static final QName ASPECT_RECORD = QName.createQName(RM_URI, "record");
    static final QName PROP_DATE_FILED = QName.createQName(RM_URI, "dateFiled");
    static final QName PROP_ORIGIONAL_NAME = QName.createQName(RM_URI, "origionalName");
    //static final QName PROP_ORIGINATOR = QName.createQName(RM_URI, "originator");
    //static final QName PROP_ORIGINATING_ORGANIZATION = QName.createQName(RM_URI, "originatingOrganization");
    //static final QName PROP_PUBLICATION_DATE = QName.createQName(RM_URI, "publicationDate");
    //static final QName PROP_MEDIA_TYPE = QName.createQName(RM_URI, "mediaType");
    //static final QName PROP_FORMAT = QName.createQName(RM_URI, "format");
    //static final QName PROP_DATE_RECEIVED = QName.createQName(RM_URI, "dateReceived");

    // Common record details
    static final QName PROP_LOCATION = QName.createQName(RM_URI, "location");

    // Fileable aspect
    static final QName ASPECT_FILABLE = QName.createQName(RM_URI, "fileable");

    // Record component identifier aspect
    static final QName ASPECT_RECORD_COMPONENT_ID = QName.createQName(RM_URI, "recordComponentIdentifier");
    static final QName PROP_IDENTIFIER = QName.createQName(RM_URI, "identifier");
    static final QName PROP_DB_UNIQUENESS_ID = QName.createQName(RM_URI, "dbUniquenessId");

    // Vital record definition aspect
    static final QName ASPECT_VITAL_RECORD_DEFINITION = QName.createQName(RM_URI, "vitalRecordDefinition");
    static final QName PROP_VITAL_RECORD_INDICATOR = QName.createQName(RM_URI, "vitalRecordIndicator");
    static final QName PROP_REVIEW_PERIOD = QName.createQName(RM_URI, "reviewPeriod");

    // Vital record aspect
    static final QName ASPECT_VITAL_RECORD = QName.createQName(RM_URI, "vitalRecord");
    static final QName PROP_REVIEW_AS_OF = QName.createQName(RM_URI, "reviewAsOf");
    static final QName PROP_NOTIFICATION_ISSUED = QName.createQName(RM_URI, "notificationIssued");

    // Cut off aspect
    static final QName ASPECT_CUT_OFF = QName.createQName(RM_URI, "cutOff");
    static final QName PROP_CUT_OFF_DATE = QName.createQName(RM_URI, "cutOffDate");

    // Transferred aspect
    static final QName ASPECT_TRANSFERRED = QName.createQName(RM_URI, "transferred");

    // Ascended aspect
    static final QName ASPECT_ASCENDED = QName.createQName(RM_URI, "ascended");

    // Disposition schedule aspect
    static final QName ASPECT_DISPOSITION_LIFECYCLE = QName.createQName(RM_URI, "dispositionLifecycle");
    static final QName ASSOC_NEXT_DISPOSITION_ACTION = QName.createQName(RM_URI, "nextDispositionAction");
    static final QName ASSOC_DISPOSITION_ACTION_HISTORY = QName.createQName(RM_URI, "dispositionActionHistory");

    // Disposition action type
    static final QName TYPE_DISPOSITION_ACTION = QName.createQName(RM_URI, "dispositionAction");
    static final QName PROP_DISPOSITION_ACTION_ID = QName.createQName(RM_URI, "dispositionActionId");
    static final QName PROP_DISPOSITION_ACTION = QName.createQName(RM_URI, "dispositionAction");
    static final QName PROP_DISPOSITION_AS_OF = QName.createQName(RM_URI, "dispositionAsOf");
    static final QName PROP_DISPOSITION_EVENTS_ELIGIBLE = QName.createQName(RM_URI, "dispositionEventsEligible");
    static final QName PROP_DISPOSITION_ACTION_STARTED_AT = QName.createQName(RM_URI, "dispositionActionStartedAt");
    static final QName PROP_DISPOSITION_ACTION_STARTED_BY = QName.createQName(RM_URI, "dispositionActionStartedBy");
    static final QName PROP_DISPOSITION_ACTION_COMPLETED_AT = QName.createQName(RM_URI, "dispositionActionCompletedAt");
    static final QName PROP_DISPOSITION_ACTION_COMPLETED_BY = QName.createQName(RM_URI, "dispositionActionCompletedBy");
    static final QName ASSOC_EVENT_EXECUTIONS = QName.createQName(RM_URI, "eventExecutions");

    // Event execution type
    static final QName TYPE_EVENT_EXECUTION = QName.createQName(RM_URI, "eventExecution");
    static final QName PROP_EVENT_EXECUTION_NAME = QName.createQName(RM_URI, "eventExecutionName");
    static final QName PROP_EVENT_EXECUTION_AUTOMATIC = QName.createQName(RM_URI, "eventExecutionAutomatic");
    static final QName PROP_EVENT_EXECUTION_COMPLETE = QName.createQName(RM_URI, "eventExecutionComplete");
    static final QName PROP_EVENT_EXECUTION_COMPLETED_BY = QName.createQName(RM_URI, "eventExecutionCompletedBy");
    static final QName PROP_EVENT_EXECUTION_COMPLETED_AT = QName.createQName(RM_URI, "eventExecutionCompletedAt");

    // Custom RM data aspect
    static final QName ASPECT_CUSTOM_RM_DATA = QName.createQName(RM_URI, "customRMData");

    // marker aspect on all RM objercts (except caveat root)
    static final QName ASPECT_FILE_PLAN_COMPONENT = QName.createQName(RM_URI, "filePlanComponent");
    static final QName PROP_ROOT_NODEREF = QName.createQName(RM_URI, "rootNodeRef");

    // Non-electronic document
    static final QName TYPE_NON_ELECTRONIC_DOCUMENT = QName.createQName(RM_URI, "nonElectronicDocument");

    // Records management root aspect
    static final QName ASPECT_RECORDS_MANAGEMENT_ROOT = QName.createQName(RM_URI, "recordsManagementRoot");
    @Deprecated // since 2.1
    static final QName ASSOC_HOLDS = QName.createQName(RM_URI, "holds");
    @Deprecated // since 2.1
    static final QName ASSOC_TRANSFERS = QName.createQName(RM_URI, "transfers");

    // Hold type
    static final QName TYPE_HOLD = QName.createQName(RM_URI, "hold");
    static final QName PROP_HOLD_REASON = QName.createQName(RM_URI, "holdReason");
    static final QName ASSOC_FROZEN_RECORDS = QName.createQName(RM_URI, "frozenRecords");

    // Record meta data aspect
    static final QName ASPECT_RECORD_META_DATA = QName.createQName(RM_URI, "recordMetaData");

    // Frozen aspect
    static final QName ASPECT_FROZEN = QName.createQName(RM_URI, "frozen");
    static final QName PROP_FROZEN_AT = QName.createQName(RM_URI, "frozenAt");
    static final QName PROP_FROZEN_BY = QName.createQName(RM_URI, "frozenBy");

    // Transfer aspect
    static final QName TYPE_TRANSFER = QName.createQName(RM_URI, "transfer");
    static final QName PROP_TRANSFER_ACCESSION_INDICATOR = QName.createQName(RM_URI, "transferAccessionIndicator");
    static final QName PROP_TRANSFER_PDF_INDICATOR = QName.createQName(RM_URI, "transferPDFIndicator");
    static final QName PROP_TRANSFER_LOCATION = QName.createQName(RM_URI, "transferLocation");
    static final QName ASSOC_TRANSFERRED = QName.createQName(RM_URI, "transferred");

    // Transferring aspect
    static final QName ASPECT_TRANSFERRING = QName.createQName(RM_URI, "transferring");

    // Versioned record aspect
    static final QName ASPECT_VERSIONED_RECORD = QName.createQName(RM_URI, "versionedRecord");

    // Unpublished update aspect
    static final QName ASPECT_UNPUBLISHED_UPDATE = QName.createQName(RM_URI, "unpublishedUpdate");
    static final QName PROP_UNPUBLISHED_UPDATE = QName.createQName(RM_URI, "unpublishedUpdate");
    static final QName PROP_UPDATE_TO = QName.createQName(RM_URI, "updateTo");
    static final QName PROP_UPDATED_PROPERTIES = QName.createQName(RM_URI, "updatedProperties");
    static final QName PROP_PUBLISH_IN_PROGRESS = QName.createQName(RM_URI, "publishInProgress");
    static final String UPDATE_TO_DISPOSITION_ACTION_DEFINITION = "dispositionActionDefinition";

    // Ghosted aspect
    static QName ASPECT_GHOSTED = QName.createQName(RM_URI, "ghosted");

    // Search rollup aspect
    static final QName ASPECT_RM_SEARCH = QName.createQName(RM_URI, "recordSearch");
    static final QName PROP_RS_DISPOSITION_ACTION_NAME = QName.createQName(RM_URI, "recordSearchDispositionActionName");
    static final QName PROP_RS_DISPOSITION_ACTION_AS_OF = QName.createQName(RM_URI, "recordSearchDispositionActionAsOf");
    static final QName PROP_RS_DISPOSITION_EVENTS_ELIGIBLE = QName.createQName(RM_URI, "recordSearchDispositionEventsEligible");
    static final QName PROP_RS_DISPOSITION_EVENTS = QName.createQName(RM_URI, "recordSearchDispositionEvents");
    static final QName PROP_RS_VITAL_RECORD_REVIEW_PERIOD = QName.createQName(RM_URI, "recordSearchVitalRecordReviewPeriod");
    static final QName PROP_RS_VITAL_RECORD_REVIEW_PERIOD_EXPRESSION = QName.createQName(RM_URI, "recordSearchVitalRecordReviewPeriodExpression");
    static final QName PROP_RS_DISPOSITION_PERIOD = QName.createQName(RM_URI, "recordSearchDispositionPeriod");
    static final QName PROP_RS_DISPOSITION_PERIOD_EXPRESSION = QName.createQName(RM_URI, "recordSearchDispositionPeriodExpression");
    static final QName PROP_RS_HAS_DISPOITION_SCHEDULE = QName.createQName(RM_URI, "recordSearchHasDispositionSchedule");
    static final QName PROP_RS_DISPOITION_INSTRUCTIONS = QName.createQName(RM_URI, "recordSearchDispositionInstructions");
    static final QName PROP_RS_DISPOITION_AUTHORITY = QName.createQName(RM_URI, "recordSearchDispositionAuthority");
    static final QName PROP_RS_HOLD_REASON = QName.createQName(RM_URI, "recordSearchHoldReason");

    // Loaded Data Set Ids
    static final QName ASPECT_LOADED_DATA_SET_ID = QName.createQName(RM_URI, "loadedDataSetId");
    static final QName PROP_LOADED_DATA_SET_IDS = QName.createQName(RM_URI, "loadedDataSetIds");

    // Extended security aspect
    static final QName ASPECT_EXTENDED_SECURITY = QName.createQName(RM_URI, "extendedSecurity");
    static final QName PROP_READERS = QName.createQName(RM_URI, "readers");
    static final QName PROP_WRITERS = QName.createQName(RM_URI, "writers");

    // Originating details of a record
    static final QName ASPECT_RECORD_ORIGINATING_DETAILS = QName.createQName(RM_URI, "recordOriginatingDetails");
    static final QName PROP_RECORD_ORIGINATING_USER_ID = QName.createQName(RM_URI, "recordOriginatingUserId");
    static final QName PROP_RECORD_ORIGINATING_CREATION_DATE = QName.createQName(RM_URI, "recordOriginatingCreationDate");
    static final QName PROP_RECORD_ORIGINATING_LOCATION = QName.createQName(RM_URI, "recordOriginatingLocation");

    // Rejection details of a record
    static final QName ASPECT_RECORD_REJECTION_DETAILS = QName.createQName(RM_URI, "recordRejectionDetails");
    static final QName PROP_RECORD_REJECTION_USER_ID = QName.createQName(RM_URI, "recordRejectionUserId");
    static final QName PROP_RECORD_REJECTION_DATE = QName.createQName(RM_URI, "recordRejectionDate");
    static final QName PROP_RECORD_REJECTION_REASON = QName.createQName(RM_URI, "recordRejectionReason");

    // Countable aspect
    static final QName ASPECT_COUNTABLE = QName.createQName(RM_URI, "countable");
    static final QName PROP_COUNT = QName.createQName(RM_URI, "count");
}
