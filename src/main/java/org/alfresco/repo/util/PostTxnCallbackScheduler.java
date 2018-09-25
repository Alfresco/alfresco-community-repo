/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.util;

import java.util.Objects;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.transaction.TransactionListenerAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Schedules a callback to a post-commit phase.
 *
 * @author alex.mukha
 */
public class PostTxnCallbackScheduler
{
    private static Log logger = LogFactory.getLog(PostTxnCallbackScheduler.class);

    private TransactionService transactionService;

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * @param callback The callback to be scheduled in a post-commit phase
     * @param uniqueId The unique id of the callback. Consecutive requests to schedule the callback with the same id
     *                will overwrite the previously scheduled one.
     */
    public void scheduleRendition(RetryingTransactionHelper.RetryingTransactionCallback callback, String uniqueId)
    {
        AlfrescoTransactionSupport.bindListener(new PostTxTransactionListener(callback, uniqueId));
    }

    private class PostTxTransactionListener extends TransactionListenerAdapter
    {
        private final RetryingTransactionHelper.RetryingTransactionCallback callback;
        private final String id;

        PostTxTransactionListener(RetryingTransactionHelper.RetryingTransactionCallback callback, String uniqueId)
        {
            this.callback = callback;
            this.id = uniqueId;
            logger.debug("Created lister with id = " + id);
        }

        @Override
        public void afterCommit()
        {
            try
            {
                transactionService.getRetryingTransactionHelper().doInTransaction(callback);
            }
            catch (Exception e)
            {
                logger.debug("The after commit callback " + id + " failed to execute: " + e.getMessage());
                // consume exception in afterCommit
            }
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            PostTxTransactionListener that = (PostTxTransactionListener) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(id);
        }
    }
}
