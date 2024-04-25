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
package org.alfresco.rm.rest.api.model;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Date;

public record HoldBulkStatus(String processId, Date startTime, Date endTime, long itemsProcessed, long errorsCount,
                             long totalItems, String lastError) implements Serializable
{
    public enum Status
    {
        PENDING("PENDING"),
        IN_PROGRESS("IN PROGRESS"),
        DONE("DONE");

        private final String value;

        Status(String value)
        {
            this.value = value;
        }

        public String getValue()
        {
            return value;
        }
    }

    public String getStatus()
    {
        if (startTime == null && endTime == null)
        {
            return Status.PENDING.getValue();
        }
        else if (startTime != null && endTime == null)
        {
            return Status.IN_PROGRESS.getValue();
        }
        else
        {
            return Status.DONE.getValue();
        }
    }

    public String getPercentageProcessed()
    {
        return itemsProcessed <= totalItems ? NumberFormat.getPercentInstance().format(
            totalItems == 0 ? 1.0F : (float) itemsProcessed / totalItems) : "Unknown";
    }
}
