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
package org.alfresco.rest.rm.community.model.fileplancomponents;

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_BOX;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_DESCRIPTION;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_FILE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_HOLD_REASON;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_IS_CLOSED;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_LOCATION;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_NUMBER_OF_COPIES;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_PHYSICAL_SIZE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_REVIEW_PERIOD;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_SHELF;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_SUPPLEMENTAL_MARKING_LIST;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_TITLE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_VITAL_RECORD_INDICATOR;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.alfresco.rest.rm.community.util.ReviewPeriodSerializer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for file plan component properties
 *
 * @author Kristijan Conkas
 * @since 2.6
 */
//FIXME: Once the fields have been added the JsonIgnoreProperties annotation should be removed
@JsonIgnoreProperties (ignoreUnknown = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilePlanComponentProperties
{
    @JsonProperty(PROPERTIES_VITAL_RECORD_INDICATOR)
    private Boolean vitalRecord;

    @JsonProperty(PROPERTIES_TITLE)
    private String title;

    @JsonProperty(PROPERTIES_HOLD_REASON)
    private String holdReason;

    @JsonProperty(PROPERTIES_DESCRIPTION)
    private String description;

    @JsonProperty(PROPERTIES_SUPPLEMENTAL_MARKING_LIST)
    private List<String> supplementalMarkingList;

    @JsonProperty(PROPERTIES_REVIEW_PERIOD)
    @JsonSerialize (using = ReviewPeriodSerializer.class)
    private FilePlanComponentReviewPeriod reviewPeriod;

    @JsonProperty(PROPERTIES_LOCATION)
    private String location;

    @JsonProperty(value = PROPERTIES_IS_CLOSED, required = false)
    private Boolean isClosed;

    @JsonProperty(value = PROPERTIES_BOX, required = false)
    private String box;

    @JsonProperty(value = PROPERTIES_FILE, required = false)
    private String file;

    @JsonProperty(value = PROPERTIES_SHELF, required = false)
    private String shelf;

    @JsonProperty(value = PROPERTIES_NUMBER_OF_COPIES, required = false)
    private Integer numberOfCopies;

    @JsonProperty(value = PROPERTIES_PHYSICAL_SIZE, required = false)
    private Integer physicalSize;
}
