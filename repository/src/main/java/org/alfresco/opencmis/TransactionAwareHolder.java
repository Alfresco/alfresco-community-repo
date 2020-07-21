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
package org.alfresco.opencmis;

import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionInterceptor;
import org.alfresco.util.transaction.TransactionListenerAdapter;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * A Tx aware wrapper around {@link Holder}.
 *
 * <p>
 *     This wrapper is created in {@link CMISTransactionAwareHolderInterceptor}.
 *     It is designed to handle the state of the {@link Holder} in case of tx retries which are handled by
 *     {@link RetryingTransactionInterceptor}.
 * </p>
 * <p>
 *     There are a few things that influenced the implementation of this wrapper and need to be taken into account:
 *     <ul>
 *         <li>
 *             The wrapper is created in {@link CMISTransactionAwareHolderInterceptor} and is replacing the incoming
 *             parameter ({@link Holder}) in the call to {@link AlfrescoCmisServiceImpl}.
 *         </li>
 *         <li>
 *             The calls to {@link AlfrescoCmisServiceImpl} generally return nothing, therefore the state
 *             of {@link Holder} or it's wrapper ({@link TransactionAwareHolder}) is modified inside
 *             the {@link AlfrescoCmisServiceImpl} and then read in CMIS layer.
 *         </li>
 *         <li>
 *             The {@link CMISTransactionAwareHolderInterceptor} is called after {@link RetryingTransactionInterceptor}
 *             but due to internal counter in Spring AOP it is not called again if the tx is retried.
 *             The proxied service ({@link AlfrescoCmisServiceImpl}) is called straight away.
 *             Fortunately the parameter replacing is not required for the second time.
 *             The wrapper ({@link TransactionAwareHolder}) will still be used.
 *         </li>
 *         <li>
 *             The {@link TxAwareHolderListener} is bound to the tx when the internal value is read.
 *             This is done this way because once the tx is rolled backed the listener list is cleared.
 *             The {@link TxAwareHolderListener} is still required to be called once the retry succeeds with a commit.
 *             The {@link CMISTransactionAwareHolderInterceptor} cannot recreate the {@link TransactionAwareHolder}
 *             as the interceptor is called only once.
 *             It is safe to bind the same listener many times as it is always the same object.
 *         </li>
 *     </ul>
 * </p>
 *
 * @author alex.mukha
 */
public class TransactionAwareHolder<T> extends Holder<T>
{
    private Holder<T> internalHolder;
    private T value;
    private TxAwareHolderListener txListener;

    TransactionAwareHolder(Holder<T> internalHolder)
    {
        this.internalHolder = internalHolder;
        this.value = internalHolder.getValue();
        txListener = new TxAwareHolderListener();
    }

    @Override
    public T getValue()
    {
        if (TransactionSynchronizationManager.isSynchronizationActive())
        {
            AlfrescoTransactionSupport.bindListener(txListener);
        }
        return this.value;
    }

    @Override
    public void setValue(T value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "TransactionAwareHolder{" +
                "internalHolder=" + internalHolder +
                ", value=" + value +
                '}';
    }

    private class TxAwareHolderListener extends TransactionListenerAdapter
    {
        @Override
        public void afterCommit()
        {
            internalHolder.setValue(getValue());
        }

        @Override
        public void afterRollback()
        {
            setValue(internalHolder.getValue());
        }
    }
}
