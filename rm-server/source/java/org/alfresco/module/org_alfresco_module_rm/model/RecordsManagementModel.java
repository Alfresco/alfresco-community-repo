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
	public static final String RM_URI = "http://www.alfresco.org/model/recordsmanagement/1.0";
	public static final String RM_PREFIX = "rma";
    
    // Model
    public static final QName RM_MODEL = QName.createQName(RM_URI, "recordsmanagement");
    
    // RM Site
    public static final QName TYPE_RM_SITE = QName.createQName(RM_URI, "rmsite");
    
    // Caveat config
    public static final QName TYPE_CAVEAT_CONFIG = QName.createQName(RM_URI, "caveatConfig");
    
    public static final QName ASPECT_CAVEAT_CONFIG_ROOT = QName.createQName(RM_URI, "caveatConfigRoot");
    public static final QName ASSOC_CAVEAT_CONFIG = QName.createQName(RM_URI, "caveatConfigAssoc");
    
    // Email config
    public static final QName TYPE_EMAIL_CONFIG = QName.createQName(RM_URI, "emailConfig");    
    public static final QName ASPECT_EMAIL_CONFIG_ROOT = QName.createQName(RM_URI, "emailConfigRoot");
    public static final QName ASSOC_EMAIL_CONFIG = QName.createQName(RM_URI, "emailConfigAssoc");

    // Records management container
    public static final QName TYPE_RECORDS_MANAGEMENT_CONTAINER = QName.createQName(RM_URI, "recordsManagementContainer");
    
    // Record Category
    public static final QName TYPE_RECORD_CATEGORY = QName.createQName(RM_URI, "recordCategory");
    
    // Records management root container
    public static final QName TYPE_FILE_PLAN = QName.createQName(RM_URI, "filePlan");
    
    // Disposition instructions aspect
    public static final QName ASPECT_SCHEDULED = QName.createQName(RM_URI, "scheduled");
    public static final QName ASSOC_DISPOSITION_SCHEDULE = QName.createQName(RM_URI, "dispositionSchedule");
    
    // Disposition definition type
    public static final QName TYPE_DISPOSITION_SCHEDULE = QName.createQName(RM_URI, "dispositionSchedule");
    public static final QName PROP_DISPOSITION_AUTHORITY = QName.createQName(RM_URI, "dispositionAuthority");
    public static final QName PROP_DISPOSITION_INSTRUCTIONS = QName.createQName(RM_URI, "dispositionInstructions");
    public static final QName PROP_RECORD_LEVEL_DISPOSITION = QName.createQName(RM_URI, "recordLevelDisposition");
    public static final QName ASSOC_DISPOSITION_ACTION_DEFINITIONS = QName.createQName(RM_URI, "dispositionActionDefinitions");    
    
    // Disposition action type
    public static final QName TYPE_DISPOSITION_ACTION_DEFINITION = QName.createQName(RM_URI, "dispositionActionDefinition");
    public static final QName PROP_DISPOSITION_ACTION_NAME = QName.createQName(RM_URI, "dispositionActionName");
    public static final QName PROP_DISPOSITION_DESCRIPTION = QName.createQName(RM_URI, "dispositionDescription");
    public static final QName PROP_DISPOSITION_PERIOD = QName.createQName(RM_URI, "dispositionPeriod");
    public static final QName PROP_DISPOSITION_PERIOD_PROPERTY = QName.createQName(RM_URI, "dispositionPeriodProperty");
    public static final QName PROP_DISPOSITION_EVENT = QName.createQName(RM_URI, "dispositionEvent");
    public static final QName PROP_DISPOSITION_EVENT_COMBINATION = QName.createQName(RM_URI, "dispositionEventCombination");
    public static final QName PROP_DISPOSITION_LOCATION = QName.createQName(RM_URI, "dispositionLocation");
    
    // Records folder
    public static final QName TYPE_RECORD_FOLDER = QName.createQName(RM_URI, "recordFolder");
    public static final QName PROP_IS_CLOSED = QName.createQName(RM_URI, "isClosed");
    
    // Declared record aspect
    public static final QName ASPECT_DECLARED_RECORD = QName.createQName(RM_URI, "declaredRecord");
    public static final QName PROP_DECLARED_AT = QName.createQName(RM_URI, "declaredAt");
    public static final QName PROP_DECLARED_BY = QName.createQName(RM_URI, "declaredBy");
    
    // Record aspect
    public static final QName ASPECT_RECORD = QName.createQName(RM_URI, "record");
    public static final QName PROP_DATE_FILED = QName.createQName(RM_URI, "dateFiled");
    public static final QName PROP_ORIGINATOR = QName.createQName(RM_URI, "originator");
    public static final QName PROP_ORIGINATING_ORGANIZATION = QName.createQName(RM_URI, "originatingOrganization");
    public static final QName PROP_PUBLICATION_DATE = QName.createQName(RM_URI, "publicationDate");
    public static final QName PROP_MEDIA_TYPE = QName.createQName(RM_URI, "mediaType");
    public static final QName PROP_FORMAT = QName.createQName(RM_URI, "format");
    public static final QName PROP_DATE_RECEIVED = QName.createQName(RM_URI, "dateReceived");  
    
    // Common record details
    public static final QName PROP_LOCATION = QName.createQName(RM_URI, "location");
    
    // Fileable aspect
    public static final QName ASPECT_FILABLE = QName.createQName(RM_URI, "fileable");
    
    // Record component identifier aspect
    public static final QName ASPECT_RECORD_COMPONENT_ID = QName.createQName(RM_URI, "recordComponentIdentifier");
    public static final QName PROP_IDENTIFIER = QName.createQName(RM_URI, "identifier");
    public static final QName PROP_DB_UNIQUENESS_ID = QName.createQName(RM_URI, "dbUniquenessId");
    
    // Vital record definition aspect
    public static final QName ASPECT_VITAL_RECORD_DEFINITION = QName.createQName(RM_URI, "vitalRecordDefinition");
    public static final QName PROP_VITAL_RECORD_INDICATOR = QName.createQName(RM_URI, "vitalRecordIndicator");
    public static final QName PROP_REVIEW_PERIOD = QName.createQName(RM_URI, "reviewPeriod");
     
    // Vital record aspect
    public static final QName ASPECT_VITAL_RECORD = QName.createQName(RM_URI, "vitalRecord");
    public static final QName PROP_REVIEW_AS_OF = QName.createQName(RM_URI, "reviewAsOf");
    public static final QName PROP_NOTIFICATION_ISSUED = QName.createQName(RM_URI, "notificationIssued");
    
    // Cut off aspect
    public static final QName ASPECT_CUT_OFF = QName.createQName(RM_URI, "cutOff");
    public static final QName PROP_CUT_OFF_DATE = QName.createQName(RM_URI, "cutOffDate");
    
    // Transferred aspect
    public static final QName ASPECT_TRANSFERRED = QName.createQName(RM_URI, "transferred");
    
    // Ascended aspect
    public static final QName ASPECT_ASCENDED = QName.createQName(RM_URI, "ascended");
    
    // Disposition schedule aspect
    public static final QName ASPECT_DISPOSITION_LIFECYCLE = QName.createQName(RM_URI, "dispositionLifecycle");
    public static final QName ASSOC_NEXT_DISPOSITION_ACTION = QName.createQName(RM_URI, "nextDispositionAction");
    public static final QName ASSOC_DISPOSITION_ACTION_HISTORY = QName.createQName(RM_URI, "dispositionActionHistory");
    
    // Disposition action type
    public static final QName TYPE_DISPOSITION_ACTION = QName.createQName(RM_URI, "dispositionAction");
    public static final QName PROP_DISPOSITION_ACTION_ID = QName.createQName(RM_URI, "dispositionActionId");
    public static final QName PROP_DISPOSITION_ACTION = QName.createQName(RM_URI, "dispositionAction");
    public static final QName PROP_DISPOSITION_AS_OF = QName.createQName(RM_URI, "dispositionAsOf");
    public static final QName PROP_DISPOSITION_EVENTS_ELIGIBLE = QName.createQName(RM_URI, "dispositionEventsEligible");
    public static final QName PROP_DISPOSITION_ACTION_STARTED_AT = QName.createQName(RM_URI, "dispositionActionStartedAt");
    public static final QName PROP_DISPOSITION_ACTION_STARTED_BY = QName.createQName(RM_URI, "dispositionActionStartedBy");
    public static final QName PROP_DISPOSITION_ACTION_COMPLETED_AT = QName.createQName(RM_URI, "dispositionActionCompletedAt");
    public static final QName PROP_DISPOSITION_ACTION_COMPLETED_BY = QName.createQName(RM_URI, "dispositionActionCompletedBy");
    public static final QName ASSOC_EVENT_EXECUTIONS = QName.createQName(RM_URI, "eventExecutions");
    
    // Event execution type
    public static final QName TYPE_EVENT_EXECUTION = QName.createQName(RM_URI, "eventExecution");
    public static final QName PROP_EVENT_EXECUTION_NAME = QName.createQName(RM_URI, "eventExecutionName");
    public static final QName PROP_EVENT_EXECUTION_AUTOMATIC = QName.createQName(RM_URI, "eventExecutionAutomatic");
    public static final QName PROP_EVENT_EXECUTION_COMPLETE = QName.createQName(RM_URI, "eventExecutionComplete");
    public static final QName PROP_EVENT_EXECUTION_COMPLETED_BY = QName.createQName(RM_URI, "eventExecutionCompletedBy");
    public static final QName PROP_EVENT_EXECUTION_COMPLETED_AT = QName.createQName(RM_URI, "eventExecutionCompletedAt");
    
    // Custom RM data aspect
    public static final QName ASPECT_CUSTOM_RM_DATA = QName.createQName(RM_URI, "customRMData");
    
    // marker aspect on all RM objercts (except caveat root)
    public static final QName ASPECT_FILE_PLAN_COMPONENT = QName.createQName(RM_URI, "filePlanComponent");
    public static final QName PROP_ROOT_NODEREF = QName.createQName(RM_URI, "rootNodeRef");
	
    // Non-electronic document
	public static final QName TYPE_NON_ELECTRONIC_DOCUMENT = QName.createQName(RM_URI, "nonElectronicDocument");
	
	// Records management root aspect
	public static final QName ASPECT_RECORDS_MANAGEMENT_ROOT = QName.createQName(RM_URI, "recordsManagementRoot");
    public static final QName ASSOC_HOLDS = QName.createQName(RM_URI, "holds");
	public static final QName ASSOC_TRANSFERS = QName.createQName(RM_URI, "transfers");
	
	// Hold type
	public static final QName TYPE_HOLD = QName.createQName(RM_URI, "hold");
	public static final QName PROP_HOLD_REASON = QName.createQName(RM_URI, "holdReason");
	public static final QName ASSOC_FROZEN_RECORDS = QName.createQName(RM_URI, "frozenRecords");
	
	// Record meta data aspect
	public static final QName ASPECT_RECORD_META_DATA = QName.createQName(RM_URI, "recordMetaData");
	
	// Frozen aspect
	public static final QName ASPECT_FROZEN = QName.createQName(RM_URI, "frozen");
	public static final QName PROP_FROZEN_AT = QName.createQName(RM_URI, "frozenAt");
	public static final QName PROP_FROZEN_BY = QName.createQName(RM_URI, "frozenBy");
	
	// Transfer aspect
	public static final QName TYPE_TRANSFER = QName.createQName(RM_URI, "transfer");
	public static final QName PROP_TRANSFER_ACCESSION_INDICATOR = QName.createQName(RM_URI, "transferAccessionIndicator");
	public static final QName PROP_TRANSFER_PDF_INDICATOR = QName.createQName(RM_URI, "transferPDFIndicator");
	public static final QName PROP_TRANSFER_LOCATION = QName.createQName(RM_URI, "transferLocation");
    public static final QName ASSOC_TRANSFERRED = QName.createQName(RM_URI, "transferred");
    
    // Transferring aspect
    public static final QName ASPECT_TRANSFERRING = QName.createQName(RM_URI, "transferring");
    
    // Versioned record aspect
    public static final QName ASPECT_VERSIONED_RECORD = QName.createQName(RM_URI, "versionedRecord");
    
    // Unpublished update aspect
    public static final QName ASPECT_UNPUBLISHED_UPDATE = QName.createQName(RM_URI, "unpublishedUpdate");
    public static final QName PROP_UNPUBLISHED_UPDATE = QName.createQName(RM_URI, "unpublishedUpdate");
    public static final QName PROP_UPDATE_TO = QName.createQName(RM_URI, "updateTo");
    public static final QName PROP_UPDATED_PROPERTIES = QName.createQName(RM_URI, "updatedProperties");
    public static final QName PROP_PUBLISH_IN_PROGRESS = QName.createQName(RM_URI, "publishInProgress");
    public static final String UPDATE_TO_DISPOSITION_ACTION_DEFINITION = "dispositionActionDefinition"; 
        
    // Ghosted aspect
    public static QName ASPECT_GHOSTED = QName.createQName(RM_URI, "ghosted");
    
    // Search rollup aspect
    public static final QName ASPECT_RM_SEARCH = QName.createQName(RM_URI, "recordSearch");
    public static final QName PROP_RS_DISPOSITION_ACTION_NAME = QName.createQName(RM_URI, "recordSearchDispositionActionName");
    public static final QName PROP_RS_DISPOSITION_ACTION_AS_OF = QName.createQName(RM_URI, "recordSearchDispositionActionAsOf");
    public static final QName PROP_RS_DISPOSITION_EVENTS_ELIGIBLE = QName.createQName(RM_URI, "recordSearchDispositionEventsEligible");
    public static final QName PROP_RS_DISPOSITION_EVENTS = QName.createQName(RM_URI, "recordSearchDispositionEvents");
    public static final QName PROP_RS_VITAL_RECORD_REVIEW_PERIOD = QName.createQName(RM_URI, "recordSearchVitalRecordReviewPeriod");
    public static final QName PROP_RS_VITAL_RECORD_REVIEW_PERIOD_EXPRESSION = QName.createQName(RM_URI, "recordSearchVitalRecordReviewPeriodExpression");
    public static final QName PROP_RS_DISPOSITION_PERIOD = QName.createQName(RM_URI, "recordSearchDispositionPeriod");
    public static final QName PROP_RS_DISPOSITION_PERIOD_EXPRESSION = QName.createQName(RM_URI, "recordSearchDispositionPeriodExpression");
    public static final QName PROP_RS_HAS_DISPOITION_SCHEDULE = QName.createQName(RM_URI, "recordSearchHasDispositionSchedule");
    public static final QName PROP_RS_DISPOITION_INSTRUCTIONS = QName.createQName(RM_URI, "recordSearchDispositionInstructions");
    public static final QName PROP_RS_DISPOITION_AUTHORITY = QName.createQName(RM_URI, "recordSearchDispositionAuthority");
    public static final QName PROP_RS_HOLD_REASON = QName.createQName(RM_URI, "recordSearchHoldReason");
}
