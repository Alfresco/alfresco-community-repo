/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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
package org.alfresco.repo.transaction;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.lang.reflect.UndeclaredThrowableException;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import org.alfresco.service.transaction.TransactionService;

/**
 * Runs callbacks in transactions that have real Spring transaction synchronization, so Alfresco transaction listeners and resources behave as in the repository, but with no database. For unit tests.
 */
public class InMemoryTransactionHelper extends RetryingTransactionHelper
{
    private final SynchronizationOnlyTransactionManager transactionManager = new SynchronizationOnlyTransactionManager();

    @Override
    @SuppressWarnings("PMD.AvoidCatchingThrowable") // RetryingTransactionCallback.execute() declares Throwable
    public <R> R doInTransaction(RetryingTransactionCallback<R> cb, boolean readOnly, boolean requiresNew)
    {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setReadOnly(readOnly);
        template.setPropagationBehavior(requiresNew ? TransactionDefinition.PROPAGATION_REQUIRES_NEW : TransactionDefinition.PROPAGATION_REQUIRED);
        return template.execute(status -> {
            try
            {
                return cb.execute();
            }
            catch (RuntimeException | Error e)
            {
                throw e;
            }
            catch (Throwable e)
            {
                throw new UndeclaredThrowableException(e);
            }
        });
    }

    /**
     * @return a {@link TransactionService} whose {@link TransactionService#getRetryingTransactionHelper()} returns this helper.
     */
    public TransactionService transactionService()
    {
        TransactionService transactionService = mock(TransactionService.class);
        lenient().when(transactionService.getRetryingTransactionHelper()).thenReturn(this);
        return transactionService;
    }

    private static class SynchronizationOnlyTransactionManager extends AbstractPlatformTransactionManager
    {
        @Override
        protected Object doGetTransaction()
        {
            return new Object();
        }

        @Override
        protected boolean isExistingTransaction(Object transaction)
        {
            return TransactionSynchronizationManager.isActualTransactionActive();
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition)
        {
            // Nothing to begin: there is no resource, only synchronization.
        }

        @Override
        protected Object doSuspend(Object transaction)
        {
            return transaction;
        }

        @Override
        protected void doResume(Object transaction, Object suspendedResources)
        {
            // Nothing to resume: doSuspend holds no resource.
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status)
        {
            // Nothing to commit: synchronization callbacks are run by the superclass.
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status)
        {
            // Nothing to roll back: synchronization callbacks are run by the superclass.
        }
    }
}
