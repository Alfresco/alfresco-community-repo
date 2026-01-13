/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.module.org_alfresco_module_rm.bulk.hold;

import java.io.Serializable;
import java.util.Date;

/**
 * An immutable POJO that contains the status of a hold bulk operation
 */
public record HoldBulkStatus(String bulkStatusId, Date startTime, Date endTime, long processedItems, long errorsCount,
                             long totalItems, String lastError, boolean isCancelled, String cancellationReason)
    implements Serializable
{
    public enum Status
    {
        PENDING("PENDING"),
        IN_PROGRESS("IN PROGRESS"),
        DONE("DONE"),
        CANCELLED("CANCELLED");

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
        if (isCancelled)
        {
            return Status.CANCELLED.getValue();
        }
        else if (startTime == null && endTime == null)
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
}
