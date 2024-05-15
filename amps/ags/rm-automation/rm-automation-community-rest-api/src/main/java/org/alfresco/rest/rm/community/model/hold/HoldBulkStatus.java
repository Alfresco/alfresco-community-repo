/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
package org.alfresco.rest.rm.community.model.hold;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.alfresco.utility.model.TestModel;

/**
 * POJO for hold bulk request
 *
 * @author Damian Ujma
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoldBulkStatus extends TestModel
{
    @JsonProperty
    private String bulkStatusId;

    @JsonProperty
    private String startTime;

    @JsonProperty
    private String endTime;

    @JsonProperty
    private long processedItems;

    @JsonProperty
    private long errorsCount;

    @JsonProperty
    private long totalItems;

    @JsonProperty
    private String lastError;

    @JsonProperty
    private Status status;

    public enum Status
    {
        PENDING,
        IN_PROGRESS,
        DONE
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        HoldBulkStatus that = (HoldBulkStatus) o;
        return processedItems == that.processedItems && errorsCount == that.errorsCount && totalItems == that.totalItems
            && Objects.equals(bulkStatusId, that.bulkStatusId) && Objects.equals(startTime,
            that.startTime) && Objects.equals(endTime, that.endTime) && Objects.equals(lastError,
            that.lastError) && status == that.status;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(bulkStatusId, startTime, endTime, processedItems, errorsCount, totalItems, lastError,
            status);
    }
}
