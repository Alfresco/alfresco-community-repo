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
package org.alfresco.rest.rm.community.model.recordfolder;

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_CLASSIFICATION;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_DESCRIPTION;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_HELD_CHILDREN_COUNT;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_IDENTIFIER;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_IS_CLOSED;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_LOCATION;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_OWNER;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields
        .PROPERTIES_RECORD_SEARCH_DISPOSITION_AUTHORITY;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields
        .PROPERTIES_RECORD_SEARCH_DISPOSITION_INSTRUCTIONS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_RECORD_SEARCH_HAS_DISPOSITION_SCHEDULE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_RECORD_SEARCH_VITAL_RECORD_REVIEW_PERIOD;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_RECORD_SEARCH_VITAL_RECORD_REVIEW_PERIOD_EXPRESSION;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_REVIEW_PERIOD;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_ROOT_NODE_REF;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_TITLE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_VITAL_RECORD_INDICATOR;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.alfresco.rest.rm.community.model.common.Owner;
import org.alfresco.rest.rm.community.model.common.ReviewPeriod;
import org.alfresco.rest.rm.community.util.ReviewPeriodSerializer;
import org.alfresco.utility.model.TestModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * POJO for record folder properties
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties (ignoreUnknown = true)
public class RecordFolderProperties extends TestModel
{
    /*************************/
    /** Mandatory parameters */
    /*************************/
    @JsonProperty (required = true, value = PROPERTIES_IS_CLOSED)
    private Boolean isClosed;

    @JsonProperty (required = true, value = PROPERTIES_IDENTIFIER)
    private String identifier;

    @JsonProperty (required = true, value = PROPERTIES_HELD_CHILDREN_COUNT)
    private Integer heldChildrenCount;

    /************************/
    /** Optional parameters */
    /************************/
    @JsonProperty (PROPERTIES_TITLE)
    private String title;

    @JsonProperty (PROPERTIES_VITAL_RECORD_INDICATOR)
    private Boolean vitalRecordIndicator;

    @JsonProperty (PROPERTIES_ROOT_NODE_REF)
    private String rootNodeRef;

    @JsonProperty (PROPERTIES_LOCATION)
    private String location;

    @JsonProperty (PROPERTIES_RECORD_SEARCH_HAS_DISPOSITION_SCHEDULE)
    private Boolean recordSearchHasDispositionSchedule;

    @JsonProperty (PROPERTIES_REVIEW_PERIOD)
    @JsonSerialize (using = ReviewPeriodSerializer.class)
    private ReviewPeriod reviewPeriod;

    @JsonProperty (PROPERTIES_CLASSIFICATION)
    private List<String> classification;
    
    @JsonProperty (PROPERTIES_DESCRIPTION)
    private String description;

    @JsonProperty (PROPERTIES_OWNER)
    private Owner owner;
 
    @JsonProperty (PROPERTIES_RECORD_SEARCH_VITAL_RECORD_REVIEW_PERIOD)
    private String recordSearchVitalRecordReviewPeriod;

    @JsonProperty (PROPERTIES_RECORD_SEARCH_VITAL_RECORD_REVIEW_PERIOD_EXPRESSION)
    private String recordSearchVitalRecordReviewPeriodExpression;

    @JsonProperty (PROPERTIES_RECORD_SEARCH_DISPOSITION_AUTHORITY)
    private String recordSearchDispositionAuthority;

    @JsonProperty (PROPERTIES_RECORD_SEARCH_DISPOSITION_INSTRUCTIONS)
    private String recordSearchDispositionInstructions;

}
