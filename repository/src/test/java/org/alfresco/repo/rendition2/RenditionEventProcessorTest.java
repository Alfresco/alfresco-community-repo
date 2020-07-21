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
package org.alfresco.repo.rendition2;

import static org.mockito.Mockito.doThrow;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.rawevents.types.OnContentUpdatePolicyEvent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides a base set of tests for {@link RenditionEventProcessor}
 *
 * @author Cristian Turlica
 */
public class RenditionEventProcessorTest
{
    private RenditionEventProcessor renditionEventProcessor;
    private CamelContext camelContext;
    private ObjectMapper messagingObjectMapper;

    @Mock
    private RenditionService2Impl renditionService2;

    @Rule
    public TestName name = new TestName();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setUp() throws Exception
    {
        ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

        camelContext = (CamelContext) ctx.getBean("alfrescoCamelContext");
        messagingObjectMapper = (ObjectMapper) ctx.getBean("alfrescoEventObjectMapper");
        TransactionService transactionService = (TransactionService) ctx.getBean("transactionService");

        renditionEventProcessor = new RenditionEventProcessor();
        renditionEventProcessor.setMessagingObjectMapper(messagingObjectMapper);
        renditionEventProcessor.setTransactionService(transactionService);
        renditionEventProcessor.setRenditionService2(renditionService2);
    }

    @Test
    public void processEmptyExchange() throws Exception
    {
        renditionEventProcessor.process(createExchange());
    }

    @Test(expected = IllegalArgumentException.class)
    public void processMissingNodeRef() throws Exception
    {
        Exchange exchange = createExchange(new OnContentUpdatePolicyEvent());
        renditionEventProcessor.process(exchange);
    }

    @Test(expected = AlfrescoRuntimeException.class)
    public void processInvalidExchange() throws Exception
    {
        Exchange exchange = createExchange("invalidContent");
        renditionEventProcessor.process(exchange);
    }

    @Test
    public void process() throws Exception
    {
        NodeRef nodeRef = new NodeRef("workspace://spacesStore/test-id");

        OnContentUpdatePolicyEvent policyEvent = new OnContentUpdatePolicyEvent();
        policyEvent.setNodeRef(nodeRef.toString());
        policyEvent.setNewContent(true);

        Exchange exchange = createExchange(policyEvent);
        renditionEventProcessor.process(exchange);
    }

    @Test(expected = AlfrescoRuntimeException.class)
    public void processException() throws Exception
    {
        NodeRef nodeRef = new NodeRef("workspace://spacesStore/test-id");

        OnContentUpdatePolicyEvent policyEvent = new OnContentUpdatePolicyEvent();
        policyEvent.setNodeRef(nodeRef.toString());
        policyEvent.setNewContent(true);

        Exchange exchange = createExchange(policyEvent);

        doThrow(new AlfrescoRuntimeException("any")).when(renditionService2).onContentUpdate(nodeRef, true);
        renditionEventProcessor.process(exchange);
    }

    private Exchange createExchange()
    {
        return new DefaultExchange(camelContext);
    }

    private Exchange createExchange(Object event) throws JsonProcessingException
    {
        Exchange exchange = createExchange();

        Message in = exchange.getIn();
        if (!(event instanceof String))
        {
            event = messagingObjectMapper.writeValueAsString(event);
        }

        in.setBody(event);

        return exchange;
    }
}
