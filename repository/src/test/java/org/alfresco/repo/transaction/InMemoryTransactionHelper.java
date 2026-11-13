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

import java.lang.reflect.Proxy;
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
     * @return a {@link TransactionService} whose only supported method returns this helper.
     */
    public TransactionService transactionService()
    {
        return (TransactionService) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{TransactionService.class},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class)
                    {
                        return switch (method.getName())
                        {
                        case "equals" -> proxy == args[0];
                        case "hashCode" -> System.identityHashCode(proxy);
                        default -> "InMemoryTransactionService";
                        };
                    }
                    if (method.getName().equals("getRetryingTransactionHelper"))
                    {
                        return this;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
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
        {}

        @Override
        protected Object doSuspend(Object transaction)
        {
            return transaction;
        }

        @Override
        protected void doResume(Object transaction, Object suspendedResources)
        {}

        @Override
        protected void doCommit(DefaultTransactionStatus status)
        {}

        @Override
        protected void doRollback(DefaultTransactionStatus status)
        {}
    }
}
