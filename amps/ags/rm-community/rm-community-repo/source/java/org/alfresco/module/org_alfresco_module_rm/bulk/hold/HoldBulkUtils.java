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
package org.alfresco.module.org_alfresco_module_rm.bulk.hold;

import org.alfresco.module.org_alfresco_module_rm.bulk.BulkOperation;
import org.alfresco.rest.api.search.model.Query;
import org.alfresco.rm.rest.api.model.HoldBulkOperation;
import org.alfresco.rm.rest.api.model.HoldBulkOperationType;
import org.alfresco.rm.rest.api.model.HoldBulkStatusEntry;

/**
 * Utility class for hold bulk operations
 */
public final class HoldBulkUtils
{
    public static HoldBulkStatusEntry toHoldBulkStatusEntry(
        HoldBulkStatusAndProcessDetails holdBulkStatusAndProcessDetails)
    {
        HoldBulkStatus bulkStatus = holdBulkStatusAndProcessDetails.holdBulkStatus();
        BulkOperation bulkOperation = holdBulkStatusAndProcessDetails.holdBulkProcessDetails().bulkOperation();
        return new HoldBulkStatusEntry(bulkStatus.bulkStatusId(), bulkStatus.startTime(),
            bulkStatus.endTime(), bulkStatus.processedItems(), bulkStatus.errorsCount(),
            bulkStatus.totalItems(), bulkStatus.lastError(), bulkStatus.getStatus(),
            bulkStatus.cancellationReason(),
            new HoldBulkOperation(new Query(bulkOperation.searchQuery().getLanguage(),
                bulkOperation.searchQuery().getQuery(), bulkOperation.searchQuery().getUserQuery()),
                HoldBulkOperationType.valueOf(bulkOperation.operationType())));
    }
}
