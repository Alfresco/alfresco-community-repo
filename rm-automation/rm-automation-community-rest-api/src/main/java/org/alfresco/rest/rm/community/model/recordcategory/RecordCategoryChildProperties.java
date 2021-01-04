/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.rest.rm.community.model.recordcategory;

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_DESCRIPTION;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_HELD_CHILDREN_COUNT;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_IDENTIFIER;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_IS_CLOSED;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_LOCATION;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_OWNER;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_RECORD_SEARCH_HAS_DISPOSITION_SCHEDULE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_RECORD_SEARCH_DISPOSITION_PERIOD_EXPRESSION;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_RECORD_SEARCH_DISPOSITION_AUTHORITY;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_RECORD_SEARCH_DISPOSITION_ACTION_AS_OF;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_RECORD_SEARCH_DISPOSITION_PERIOD;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_RECORD_SEARCH_DISPOSITION_ACTION_NAME;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_RECORD_SEARCH_DISPOSITION_EVENTS_ELIGIBLE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_RECORD_SEARCH_DISPOSITION_INSTRUCTIONS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_REVIEW_PERIOD;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_ROOT_NODE_REF;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_TITLE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_VITAL_RECORD_INDICATOR;

import java.util.Date;

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
 * POJO for record category child properties
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RecordCategoryChildProperties extends TestModel
{
    /**************************************************************************/
    /** Mandatory parameters - Shared by record categories and record folders */
    /**************************************************************************/
    @JsonProperty (required = true, value = PROPERTIES_TITLE)
    private String title;

    @JsonProperty (required = true, value = PROPERTIES_VITAL_RECORD_INDICATOR)
    private Boolean vitalRecordIndicator;

    @JsonProperty (required = true, value = PROPERTIES_ROOT_NODE_REF)
    private String rootNodeRef;

    @JsonProperty (required = true, value = PROPERTIES_IDENTIFIER)
    private String identifier;

    @JsonProperty (required = true, value = PROPERTIES_REVIEW_PERIOD)
    @JsonSerialize (using = ReviewPeriodSerializer.class)
    private ReviewPeriod reviewPeriod;

    @JsonProperty (required = true, value = PROPERTIES_DESCRIPTION)
    private String description;

    /*********************************************************/
    /** Optional parameters - Applies only to record folders */
    /*********************************************************/
    @JsonProperty (PROPERTIES_HELD_CHILDREN_COUNT)
    private Integer heldChildrenCount;

    @JsonProperty (PROPERTIES_LOCATION)
    private String location;

    @JsonProperty (PROPERTIES_IS_CLOSED)
    private Boolean isClosed;

    @JsonProperty (PROPERTIES_RECORD_SEARCH_HAS_DISPOSITION_SCHEDULE)
    private Boolean recordSearchHasDispositionSchedule;
    
    @JsonProperty (PROPERTIES_RECORD_SEARCH_DISPOSITION_PERIOD_EXPRESSION)
    private String recordSearchDispositionPeriodExpression;
    
    @JsonProperty (PROPERTIES_RECORD_SEARCH_DISPOSITION_AUTHORITY)
    private String recordSearchDispositionAuthority;

    @JsonProperty (PROPERTIES_RECORD_SEARCH_DISPOSITION_ACTION_AS_OF)
    private Date recordSearchDispositionActionAsOf;

    @JsonProperty (PROPERTIES_RECORD_SEARCH_DISPOSITION_PERIOD)
    private String recordSearchDispositionPeriod;

    @JsonProperty (PROPERTIES_RECORD_SEARCH_DISPOSITION_ACTION_NAME)
    private String recordSearchDispositionActionName;

    @JsonProperty (PROPERTIES_RECORD_SEARCH_DISPOSITION_EVENTS_ELIGIBLE)
    private Boolean recordSearchDispositionEventsEligible;

    @JsonProperty (PROPERTIES_RECORD_SEARCH_DISPOSITION_INSTRUCTIONS)
    private String recordSearchDispositionInstructions;

    @JsonProperty (PROPERTIES_OWNER)
    private Owner owner;
}
