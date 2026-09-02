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

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.alfresco.messaging.MessagePublisher;
import org.alfresco.repo.rawevents.types.EventType;
import org.alfresco.repo.rawevents.types.OnContentUpdatePolicyEvent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;

/**
 * Provides a base set of tests for {@link TransactionAwareEventProducer}
 *
 * @author Cristian Turlica
 */
public class TransactionAwareEventProducerTest extends BaseSpringTest
{
    @Autowired
    private RetryingTransactionHelper retryingTransactionHelper;
    @Autowired
    private TransactionAwareEventProducer eventProducer;

    @Test
    public void send() throws Exception
    {
        String endpointUri = "jms:" + this.getClass().getSimpleName() + "_" + GUID.generate();
        MessagePublisher publisher = mock(MessagePublisher.class);
        eventProducer.setPublisher(publisher);

        String stringMessage = "stringMessage";
        OnContentUpdatePolicyEvent objectMessage = new OnContentUpdatePolicyEvent();
        objectMessage.setId(GUID.generate());
        objectMessage.setType(EventType.CONTENT_UPDATED.toString());
        objectMessage.setTimestamp(System.currentTimeMillis());

        retryingTransactionHelper.doInTransaction(() -> {
            eventProducer.send(endpointUri, stringMessage);

            verify(publisher, never()).send(eq(endpointUri), eq(stringMessage), anyMap());

            eventProducer.send(endpointUri, objectMessage);

            verify(publisher, never()).send(eq(endpointUri), eq(stringMessage), anyMap());

            return null;
        });

        verify(publisher).send(eq(endpointUri), eq(stringMessage), anyMap());
        verify(publisher, times(2)).send(eq(endpointUri), org.mockito.ArgumentMatchers.anyString(), anyMap());
    }
}
