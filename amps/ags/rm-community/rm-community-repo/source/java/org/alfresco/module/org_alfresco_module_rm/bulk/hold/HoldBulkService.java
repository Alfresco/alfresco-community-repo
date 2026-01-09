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

import org.alfresco.module.org_alfresco_module_rm.bulk.BulkCancellationRequest;
import org.alfresco.module.org_alfresco_module_rm.bulk.BulkOperation;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Interface defining a hold bulk service.
 */
public interface HoldBulkService
{
    /**
     * Initiates a bulk operation on a hold.
     *
     * @param holdRef
     *            The hold reference
     * @param bulkOperation
     *            The bulk operation
     * @return The initial status of the bulk operation
     */
    HoldBulkStatus execute(NodeRef holdRef, BulkOperation bulkOperation);

    /**
     * Cancels a bulk operation.
     *
     * @param holdRef
     *            The hold reference
     * @param bulkStatusId
     *            The bulk status id
     * @param bulkCancellationRequest
     *            The bulk cancellation request
     */
    void cancelBulkOperation(NodeRef holdRef, String bulkStatusId, BulkCancellationRequest bulkCancellationRequest);
}
