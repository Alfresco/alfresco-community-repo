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
package org.alfresco.rest.rm.community.model.fileplancomponents;

/**
 * File plan component field names constants
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
public class FilePlanComponentFields
{
    /** Common properties for file plans, record categories, record folders and records */
    public static final String PROPERTIES_ROOT_NODE_REF = "rma:rootNodeRef";
    public static final String PROPERTIES_IDENTIFIER = "rma:identifier";

    /** Common properties for record categories, record folders and records */
    // Non-electronic record properties
    public static final String PROPERTIES_TITLE = "cm:title";
    public static final String PROPERTIES_DESCRIPTION = "cm:description";

    /** Common properties for record categories and record folders **/
    public static final String PROPERTIES_VITAL_RECORD_INDICATOR = "rma:vitalRecordIndicator";
    public static final String PROPERTIES_REVIEW_PERIOD = "rma:reviewPeriod";
    public static final String PROPERTIES_OWNER = "cm:owner";
    public static final String PROPERTIES_AUTHOR="cm:author";

    /** Common properties for record folders and records */
    public static final String PROPERTIES_RECORD_SEARCH_HAS_DISPOSITION_SCHEDULE = "rma:recordSearchHasDispositionSchedule";
    public static final String PROPERTIES_RECORD_SEARCH_DISPOSITION_PERIOD_EXPRESSION = "rma:recordSearchDispositionPeriodExpression";
    public static final String PROPERTIES_RECORD_SEARCH_DISPOSITION_AUTHORITY = "rma:recordSearchDispositionAuthority";
    public static final String PROPERTIES_RECORD_SEARCH_DISPOSITION_ACTION_AS_OF = "rma:recordSearchDispositionActionAsOf";
    public static final String PROPERTIES_RECORD_SEARCH_DISPOSITION_PERIOD = "rma:recordSearchDispositionPeriod";
    public static final String PROPERTIES_RECORD_SEARCH_DISPOSITION_ACTION_NAME = "rma:recordSearchDispositionActionName";
    public static final String PROPERTIES_RECORD_SEARCH_DISPOSITION_EVENTS_ELIGIBLE = "rma:recordSearchDispositionEventsEligible";
    public static final String PROPERTIES_RECORD_SEARCH_DISPOSITION_INSTRUCTIONS = "rma:recordSearchDispositionInstructions";
    public static final String PROPERTIES_RECORD_SEARCH_DISPOSITION_EVENTS = "rma:recordSearchDispositionEvents";
    public static final String PROPERTIES_DECLASSIFICATION_REVIEW_COMPLETED_BY = "rma:declassificationReviewCompletedBy";
    public static final String PROPERTIES_DECLASSIFICATION_REVIEW_COMPLETED_AT = "rma:declassificationReviewCompletedAt";
    

    /** File plan properties */
    public static final String PROPERTIES_COMPONENT_ID = "st:componentId";
    public static final String PROPERTIES_COUNT = "rma:count";

    /** Record category properties */
    // All fields are shared with record folders

    /** Record folder properties */
    public static final String PROPERTIES_IS_CLOSED = "rma:isClosed"; // not to be confused with IS_CLOSED!
    public static final String PROPERTIES_HELD_CHILDREN_COUNT = "rma:heldChildrenCount";
    public static final String PROPERTIES_LOCATION = "rma:location";
    public static final String PROPERTIES_RECORD_SEARCH_VITAL_RECORD_REVIEW_PERIOD = "rma:recordSearchVitalRecordReviewPeriod";
    public static final String PROPERTIES_RECORD_SEARCH_VITAL_RECORD_REVIEW_PERIOD_EXPRESSION = "rma:recordSearchVitalRecordReviewPeriodExpression";

    /**
     * Record properties
     */
    public static final String PROPERTIES_CLASSIFICATION = "sc:classification";
    public static final String PROPERTIES_DATE_FILED = "rma:dateFiled";
    public static final String PROPERTIES_ORIGINAL_NAME = "rma:origionalName";
    public static final String PROPERTIES_REVIEW_AS_OF = "rma:reviewAsOf";

    /** Electronic record properties */
    public static final String PROPERTIES_VERSION_TYPE = "cm:versionType";
    public static final String PROPERTIES_VERSION_LABEL = "cm:versionLabel";
    public static final String PROPERTIES_VERSIONED_NODEREF = "rmv:versionedNodeRef";
    public static final String PROPERTIES_RMV_VERSIONED = "rmv:versionLabel";
    public static final String PROPERTIES_DATE_TIME_ORIGINAL = "exif:dateTimeOriginal";
    public static final String PROPERTIES_EXPOSURE_TIME = "exif:exposureTime";
    public static final String PROPERTIES_FLASH = "exif:flash";
    public static final String PROPERTIES_F_NUMBER = "exif:fNumber";
    public static final String PROPERTIES_FOCAL_LENGTH = "exif:focalLength";
    public static final String PROPERTIES_ISO_SPEED_RATINGS = "exif:isoSpeedRatings";
    public static final String PROPERTIES_MANUFACTURER = "exif:manufacturer";
    public static final String PROPERTIES_MODEL = "exif:model";
    public static final String PROPERTIES_ORIENTATION = "exif:orientation";
    public static final String PROPERTIES_PIXEL_X_DIMENSION = "exif:pixelXDimension";
    public static final String PROPERTIES_PIXEL_Y_DIMENSION = "exif:pixelYDimension";
    public static final String PROPERTIES_RESOLUTION_UNIT = "exif:resolutionUnit";
    public static final String PROPERTIES_SOFTWARE = "exif:software";
    public static final String PROPERTIES_X_RESOLUTION = "exif:xResolution";
    public static final String PROPERTIES_Y_RESOLUTION = "exif:yResolution";
    public static final String PROPERTIES_RECORD_ORIGINATING_LOCATION = "rma:recordOriginatingLocation";
    public static final String PROPERTIES_RECORD_ORIGINATING_USER_ID = "rma:recordOriginatingUserId";
    public static final String PROPERTIES_RECORD_ORIGINATING_CREATION_DATE = "rma:recordOriginatingCreationDate";

    /** Non-electronic record properties */
    public static final String PROPERTIES_SHELF = "rma:shelf";
    public static final String PROPERTIES_STORAGE_LOCATION = "rma:storageLocation";
    public static final String PROPERTIES_FILE = "rma:file";
    public static final String PROPERTIES_BOX = "rma:box";
    public static final String PROPERTIES_NUMBER_OF_COPIES = "rma:numberOfCopies";
    public static final String PROPERTIES_PHYSICAL_SIZE = "rma:physicalSize";

    /** Transfer properties */
    public static final String PROPERTIES_PDF_INDICATOR = "rma:transferPDFIndicator";
    public static final String PROPERTIES_TRANSFER_LOCATION = "rma:transferLocation";
    public static final String PROPERTIES_ACCESSION_INDICATOR = "rma:transferAccessionIndicator";

    /** Parameters */
    public static final String RELATIVE_PATH = "relativePath";
    public static final String INCLUDE = "include";

    /** Include options */
    public static final String ALLOWABLE_OPERATIONS = "allowableOperations";
    public static final String IS_CLOSED = "isClosed";
    public static final String IS_COMPLETED = "isCompleted";
    public static final String CONTENT = "content";
    public static final String PATH = "path";
    /** CONTENT STORE property */
    public static final String PROPERTIES_STORE = "cm:storeName";
    /** WORM Unlock Date */
    public static final String PROPERTIES_WORM_UNLOCK_DATE = "rme:wormUnlockDate";
}
