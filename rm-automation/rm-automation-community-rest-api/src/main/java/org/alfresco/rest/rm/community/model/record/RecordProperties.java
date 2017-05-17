/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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

package org.alfresco.rest.rm.community.model.record;

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_AUTHOR;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_BOX;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_DATE_FILED;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_DATE_TIME_ORIGINAL;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_DESCRIPTION;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_EXPOSURE_TIME;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_FILE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_FLASH;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_FOCAL_LENGTH;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_F_NUMBER;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_IDENTIFIER;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_ID_IS_TEMPORARILY_EDITABLE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_ISO_SPEED_RATINGS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_MANUFACTURER;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_MODEL;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_NUMBER_OF_COPIES;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_ORIENTATION;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_ORIGINAL_NAME;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_OWNER;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_PHYSICAL_SIZE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_PIXEL_X_DIMENSION;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_PIXEL_Y_DIMENSION;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_RECORD_SEARCH_HAS_DISPOSITION_SCHEDULE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_RESOLUTION_UNIT;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_ROOT_NODE_REF;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_SHELF;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_SOFTWARE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_STORAGE_LOCATION;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_TITLE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_VERSION_LABEL;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_VERSION_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_X_RESOLUTION;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_Y_RESOLUTION;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_RECORD_ORIGINATING_LOCATION;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_RECORD_ORIGINATING_USER_ID;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_RECORD_ORIGINATING_CREATION_DATE;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.rm.community.model.common.Owner;
import org.alfresco.utility.model.TestModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * POJO for record properties
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecordProperties extends TestModel
{
    /*************************/
    /** Mandatory parameters */
    /*************************/
    @JsonProperty (required = true, value = PROPERTIES_ROOT_NODE_REF)
    private String rootNodeRef;

    @JsonProperty (required = true, value = PROPERTIES_DATE_FILED)
    private String dateField;

    @JsonProperty (required = true, value = PROPERTIES_IDENTIFIER)
    private String identifier;

    @JsonProperty (required = true, value = PROPERTIES_RECORD_SEARCH_HAS_DISPOSITION_SCHEDULE)
    private Boolean recordSearchHasDispositionSchedule;

    @JsonProperty (required = true, value = PROPERTIES_ORIGINAL_NAME)
    private String originalName;

    @JsonProperty (required = true, value = PROPERTIES_ID_IS_TEMPORARILY_EDITABLE)
    private Boolean idIsTemporarilyEditable;

    /*********************************/
    /** Electronic record parameters */
    /*********************************/
    @JsonProperty (PROPERTIES_VERSION_TYPE)
    private String versionType;

    @JsonProperty (PROPERTIES_VERSION_LABEL)
    private String versionLabel;

    @JsonProperty (PROPERTIES_DATE_TIME_ORIGINAL)
    private String dateTimeOriginal;

    @JsonProperty (PROPERTIES_EXPOSURE_TIME)
    private Double exposureTime;

    @JsonProperty (PROPERTIES_FLASH)
    private Boolean flash;

    @JsonProperty (PROPERTIES_F_NUMBER)
    private Double fNumber;

    @JsonProperty (PROPERTIES_FOCAL_LENGTH)
    private Double focalLength;

    @JsonProperty (PROPERTIES_ISO_SPEED_RATINGS)
    private Integer isoSpeedRatings;

    @JsonProperty (PROPERTIES_MANUFACTURER)
    private String manufacturer;

    @JsonProperty (PROPERTIES_MODEL)
    private String model;

    @JsonProperty (PROPERTIES_ORIENTATION)
    private Integer orientation;

    @JsonProperty (PROPERTIES_PIXEL_X_DIMENSION)
    private Integer pixelXDimension;

    @JsonProperty (PROPERTIES_PIXEL_Y_DIMENSION)
    private Integer pixelYDimension;

    @JsonProperty (PROPERTIES_RESOLUTION_UNIT)
    private String resolutionUnit;

    @JsonProperty (PROPERTIES_SOFTWARE)
    private String software;

    @JsonProperty (PROPERTIES_X_RESOLUTION)
    private Double xResolution;

    @JsonProperty (PROPERTIES_Y_RESOLUTION)
    private Double yResolution;

    @JsonProperty (PROPERTIES_RECORD_ORIGINATING_LOCATION)
    private String originatingLocation;

    @JsonProperty (PROPERTIES_RECORD_ORIGINATING_USER_ID)
    private String originatingUserId;

    @JsonProperty (PROPERTIES_RECORD_ORIGINATING_CREATION_DATE)
    private String originatingCreationDate;

    /*************************************/
    /** Non-electronic record parameters */
    /*************************************/
    @JsonProperty (PROPERTIES_TITLE)
    private String title;

    @JsonProperty (PROPERTIES_SHELF)
    private String shelf;

    @JsonProperty (PROPERTIES_STORAGE_LOCATION)
    private String storageLocation;

    @JsonProperty (PROPERTIES_FILE)
    private String file;

    @JsonProperty (PROPERTIES_BOX)
    private String box;

    @JsonProperty (PROPERTIES_DESCRIPTION)
    private String description;

    @JsonProperty (PROPERTIES_NUMBER_OF_COPIES)
    private Integer numberOfCopies;

    @JsonProperty (PROPERTIES_PHYSICAL_SIZE)
    private Integer physicalSize;

    @JsonProperty (PROPERTIES_OWNER)
    private Owner owner;

    @JsonProperty(PROPERTIES_AUTHOR)
    private String author;
}
