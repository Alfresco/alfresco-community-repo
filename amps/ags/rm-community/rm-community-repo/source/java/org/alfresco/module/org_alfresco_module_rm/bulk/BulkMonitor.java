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
package org.alfresco.module.org_alfresco_module_rm.bulk;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * An interface for monitoring the progress of a bulk operation
 */
public interface BulkMonitor<T>
{
    /**
     * Update the bulk status
     *
     * @param bulkStatus
     *            the bulk status
     */
    void updateBulkStatus(T bulkStatus);

    /**
     * Register a process
     *
     * @param nodeRef
     *            the node reference
     * @param processId
     *            the process id
     * @param bulkOperation
     *            the bulk operation
     */
    void registerProcess(NodeRef nodeRef, String processId, BulkOperation bulkOperation);

    /**
     * Get the bulk status
     *
     * @param bulkStatusId
     *            the bulk status id
     * @return the bulk status
     */
    T getBulkStatus(String bulkStatusId);

    /**
     * Cancel a bulk operation
     *
     * @param bulkStatusId
     * @param bulkCancellationRequest
     */
    void cancelBulkOperation(String bulkStatusId, BulkCancellationRequest bulkCancellationRequest);

    /**
     * Check if a bulk operation is cancelled
     *
     * @param bulkStatusId
     * @return true if the bulk operation is cancelled
     */
    boolean isCancelled(String bulkStatusId);

    /**
     * Get the bulk cancellation request
     *
     * @param bulkStatusId
     * @return cancellation reason
     */
    BulkCancellationRequest getBulkCancellationRequest(String bulkStatusId);
}
