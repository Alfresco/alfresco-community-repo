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
package org.alfresco.repo.rawevents;

import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.util.transaction.TransactionListenerAdapter;
import org.apache.camel.ExchangePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A transaction aware {@link AbstractEventProducer}. Events are scheduled to be sent in
 * post-commit phase.
 * 
 * @author Cristian Turlica
 */
public class TransactionAwareEventProducer extends AbstractEventProducer
{
    private static Log logger = LogFactory.getLog(TransactionAwareEventProducer.class);

    private static final String POST_TRANSACTION_PENDING_REQUESTS = "postTransactionPendingEventRequests";

    public void send(String endpointUri, Object event)
    {
        send(endpointUri, null, event, null);
    }

    public void send(String endpointUri, Object event, Map<String, Object> headers)
    {
        send(endpointUri, null, event, headers);
    }

    public void send(String endpointUri, ExchangePattern exchangePattern, Object event, Map<String, Object> headers)
    {
        String currentTxn = AlfrescoTransactionSupport.getTransactionId();
        TransactionListener transactionListener = new TransactionListener("TxEvPr" + currentTxn);

        AlfrescoTransactionSupport.bindListener(transactionListener);
        List<PendingRequest> pendingRequests = AlfrescoTransactionSupport.getResource(POST_TRANSACTION_PENDING_REQUESTS);

        if (pendingRequests == null)
        {
            pendingRequests = new LinkedList<>();
            AlfrescoTransactionSupport.bindResource(POST_TRANSACTION_PENDING_REQUESTS, pendingRequests);
        }

        PendingRequest pendingRequest = new PendingRequest(endpointUri, exchangePattern, event, headers);
        pendingRequests.add(pendingRequest);
    }

    private class PendingRequest
    {
        private String endpointUri;
        private Object event;
        private Map<String, Object> headers;
        private ExchangePattern exchangePattern;

        private PendingRequest(String endpointUri, ExchangePattern exchangePattern, Object event, Map<String, Object> headers)
        {
            this.endpointUri = endpointUri;
            this.event = event;
            this.headers = headers;
            this.exchangePattern = exchangePattern;
        }

        void send()
        {
            TransactionAwareEventProducer.super.send(endpointUri, exchangePattern, event, headers);
        }

        @Override
        public boolean equals(Object object)
        {
            if (this == object)
            {
                return true;
            }

            if (!(object instanceof TransactionAwareEventProducer.PendingRequest))
            {
                return false;
            }

            TransactionAwareEventProducer.PendingRequest that = (TransactionAwareEventProducer.PendingRequest) object;
            return Objects.equals(endpointUri, that.endpointUri) && Objects.equals(event, that.event) && Objects.equals(headers, that.headers);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(endpointUri, event, headers);
        }
    }

    private class TransactionListener extends TransactionListenerAdapter implements org.alfresco.repo.transaction.TransactionListener
    {
        private final String id;

        TransactionListener(String uniqueId)
        {
            this.id = uniqueId;

            if (logger.isDebugEnabled())
            {
                logger.debug("Created lister with id = " + id);
            }
        }

        @Override
        public void afterCommit()
        {
            for (TransactionAwareEventProducer.PendingRequest pendingRequest : (List<PendingRequest>) AlfrescoTransactionSupport.getResource(POST_TRANSACTION_PENDING_REQUESTS))
            {
                try
                {
                    pendingRequest.send();
                }
                catch (Exception e)
                {
                    logger.error("The after commit callback " + id + " failed to execute: " + e.getMessage(), e);
                    // consume exception
                }
            }
        }

        @Override
        public void flush()
        {
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof TransactionAwareEventProducer.TransactionListener))
            {
                return false;
            }
            TransactionAwareEventProducer.TransactionListener that = (TransactionAwareEventProducer.TransactionListener) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(id);
        }
    }
}
