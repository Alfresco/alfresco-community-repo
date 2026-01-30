/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.repo.event2;

import static java.util.concurrent.TimeUnit.SECONDS;

import static org.awaitility.Awaitility.await;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import jakarta.jms.ConnectionFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.DataFormat;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.repo.event.v1.model.ChildAssociationResource;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.EventType;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.PeerAssociationResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.Resource;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.dictionary.CustomModelService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;

/**
 * @author Iulian Aftene
 */
@TestPropertySource(properties = {"repo.event2.queue.skip=false"})
public abstract class AbstractContextAwareRepoEvent extends BaseSpringTest
{
    protected static final boolean DEBUG = false;

    protected static final String TEST_NAMESPACE = "http://www.alfresco.org/test/ContextAwareRepoEvent";
    protected static final RepoEventContainer EVENT_CONTAINER = new RepoEventContainer();

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String TOPIC_NAME = "alfresco.repo.event2";
    private static final String CAMEL_ROUTE = "jms:topic:" + TOPIC_NAME;
    private static final CamelContext CAMEL_CONTEXT = new DefaultCamelContext();

    protected final Locale defaultLocale = new Locale(MLText.getDefaultLocale().getLanguage());
    protected final Locale germanLocale = new Locale(Locale.GERMAN.getLanguage(), "XX");
    protected final Locale frenchLocale = new Locale(Locale.FRENCH.getLanguage());
    protected final Locale japaneseLocale = new Locale(Locale.JAPANESE.getLanguage(), "YY", "ZZ");

    private static boolean isCamelConfigured;
    private static DataFormat dataFormat;

    @Autowired
    protected RetryingTransactionHelper retryingTransactionHelper;

    @Autowired
    protected NodeService nodeService;

    @Autowired
    protected CustomModelService customModelService;

    @Autowired
    @Qualifier("descriptorComponent") protected DescriptorService descriptorService;

    @Autowired
    @Qualifier("event2ObjectMapper") protected ObjectMapper objectMapper;

    @Autowired
    @Qualifier("eventGeneratorV2") protected EventGenerator eventGenerator;

    @Autowired
    private NamespaceDAO namespaceDAO;

    protected NodeRef rootNodeRef;

    @BeforeClass
    public static void beforeAll()
    {
        isCamelConfigured = false;
    }

    @AfterClass
    public static void afterAll()
    {
        CAMEL_CONTEXT.stop();
    }

    @Before
    public void setUp() throws Exception
    {
        if (!isCamelConfigured)
        {
            dataFormat = new JacksonDataFormat(objectMapper, RepoEvent.class);
            configRoute();
            isCamelConfigured = true;
        }

        // authenticate as admin
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        rootNodeRef = retryingTransactionHelper.doInTransaction(() -> {
            // create a store and get the root node
            StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, getClass().getName());
            if (!nodeService.exists(storeRef))
            {
                storeRef = nodeService.createStore(storeRef.getProtocol(),
                        storeRef.getIdentifier());
            }
            return nodeService.getRootNode(storeRef);
        });

        initTestNamespacePrefixMapping();

        flushSpuriousEvents();
    }

    protected void initTestNamespacePrefixMapping()
    {
        if (namespaceDAO.getNamespaceURI("ce") == null)
        {
            namespaceDAO.addURI(TEST_NAMESPACE);
            namespaceDAO.addPrefix("ce", TEST_NAMESPACE);
        }
    }

    /* When running with an empty database some events related to the creation may creep up here making the test fails. After attempting several other strategies, a smart sleep seems to do the work. */
    protected void flushSpuriousEvents() throws InterruptedException
    {
        int maxloops = 5;

        int count = maxloops;
        do
        {
            Thread.sleep(165l);
            if (EVENT_CONTAINER.isEmpty())
            {
                count--;
            }
            else
            {
                EVENT_CONTAINER.reset();
                count = maxloops;
            }

        } while (count > 0);
    }

    @After
    public void tearDown()
    {
        EVENT_CONTAINER.reset();
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    protected NodeRef createNode(QName contentType)
    {
        return retryingTransactionHelper.doInTransaction(() -> nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(TEST_NAMESPACE, GUID.generate()),
                contentType).getChildRef());
    }

    protected NodeRef createNode(QName contentType, NodeRef parentRef)
    {
        return retryingTransactionHelper.doInTransaction(() -> nodeService.createNode(
                parentRef,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(TEST_NAMESPACE, GUID.generate()),
                contentType).getChildRef());
    }

    protected NodeRef createNode(QName contentType, PropertyMap propertyMap)
    {
        return createNode(contentType, GUID.generate(), propertyMap);
    }

    protected NodeRef createNode(QName contentType, String localName, PropertyMap propertyMap)
    {
        return retryingTransactionHelper.doInTransaction(() -> nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(TEST_NAMESPACE, localName),
                contentType,
                propertyMap).getChildRef());
    }

    protected NodeRef updateNodeName(NodeRef nodeRef, String newName)
    {
        PropertyMap propertyMap = new PropertyMap();
        propertyMap.put(ContentModel.PROP_NAME, newName);
        return retryingTransactionHelper.doInTransaction(() -> {
            nodeService.addProperties(nodeRef, propertyMap);
            return null;
        });
    }

    protected void deleteNode(NodeRef nodeRef)
    {
        retryingTransactionHelper.doInTransaction(() -> {
            nodeService.deleteNode(nodeRef);
            return null;
        });
    }

    protected RepoEventContainer getRepoEventsContainer()
    {
        return EVENT_CONTAINER;
    }

    protected <D extends DataAttributes<? extends Resource>> RepoEvent<D> getRepoEvent(int eventSequenceNumber)
    {
        waitUntilNumOfEvents(eventSequenceNumber);

        RepoEventContainer eventContainer = getRepoEventsContainer();
        @SuppressWarnings("unchecked")
        RepoEvent<D> event = (RepoEvent<D>) eventContainer.getEvent(eventSequenceNumber);
        assertNotNull(event);

        return event;
    }

    protected <D extends DataAttributes<? extends Resource>> RepoEvent<D> getRepoEventWithoutWait(int eventSequenceNumber)
    {
        RepoEventContainer eventContainer = getRepoEventsContainer();
        @SuppressWarnings("unchecked")
        RepoEvent<D> event = (RepoEvent<D>) eventContainer.getEvent(eventSequenceNumber);
        assertNotNull(event);

        return event;
    }

    protected <D extends DataAttributes<? extends Resource>> D getEventData(int eventSequenceNumber)
    {
        RepoEvent<D> event = getRepoEvent(eventSequenceNumber);
        D eventData = event.getData();
        assertNotNull(eventData);

        return eventData;
    }

    protected <D extends DataAttributes<? extends Resource>> D getEventDataWithoutWait(int eventSequenceNumber)
    {
        RepoEvent<D> event = getRepoEventWithoutWait(eventSequenceNumber);
        D eventData = event.getData();
        assertNotNull(eventData);

        return eventData;
    }

    protected <D extends DataAttributes<? extends Resource>> D getEventData(RepoEvent<D> repoEvent)
    {
        assertNotNull(repoEvent);
        D eventData = repoEvent.getData();
        assertNotNull(eventData);

        return eventData;
    }

    protected NodeResource getNodeResource(int eventSequenceNumber)
    {
        DataAttributes<NodeResource> eventData = getEventData(eventSequenceNumber);
        NodeResource resource = eventData.getResource();
        assertNotNull(resource);

        return resource;
    }

    protected NodeResource getNodeResourceWithoutWait(int eventSequenceNumber)
    {
        DataAttributes<NodeResource> eventData = getEventDataWithoutWait(eventSequenceNumber);
        NodeResource resource = eventData.getResource();
        assertNotNull(resource);

        return resource;
    }

    protected <D extends DataAttributes<NodeResource>> NodeResource getNodeResource(RepoEvent<D> repoEvent)
    {
        assertNotNull(repoEvent);
        D eventData = getEventData(repoEvent);
        assertNotNull(eventData);
        NodeResource resource = eventData.getResource();
        assertNotNull(resource);

        return resource;
    }

    protected <D extends DataAttributes<ChildAssociationResource>> ChildAssociationResource getChildAssocResource(
            RepoEvent<D> repoEvent)
    {
        assertNotNull(repoEvent);
        D eventData = repoEvent.getData();
        assertNotNull(eventData);
        ChildAssociationResource resource = eventData.getResource();
        assertNotNull(resource);

        return resource;
    }

    protected <D extends DataAttributes<PeerAssociationResource>> PeerAssociationResource getPeerAssocResource(
            RepoEvent<D> repoEvent)
    {
        assertNotNull(repoEvent);
        D eventData = repoEvent.getData();
        assertNotNull(eventData);
        PeerAssociationResource resource = eventData.getResource();
        assertNotNull(resource);

        return resource;
    }

    protected NodeResource getNodeResourceBefore(int eventSequenceNumber)
    {
        DataAttributes<NodeResource> eventData = getEventData(eventSequenceNumber);
        NodeResource resourceBefore = eventData.getResourceBefore();
        assertNotNull(resourceBefore);

        return resourceBefore;
    }

    protected <D extends DataAttributes<NodeResource>> NodeResource getNodeResourceBefore(RepoEvent<D> repoEvent)
    {
        assertNotNull(repoEvent);
        D eventData = repoEvent.getData();
        assertNotNull(eventData);
        NodeResource resourceBefore = eventData.getResourceBefore();
        assertNotNull(resourceBefore);

        return resourceBefore;
    }

    @SuppressWarnings("unchecked")
    protected <T> T getProperty(NodeResource resource, String propertyName)
    {
        assertNotNull(resource);
        assertNotNull(resource.getProperties());
        return (T) resource.getProperties().get(propertyName);
    }

    protected String getLocalizedProperty(NodeResource resource, String propertyName, Locale locale)
    {
        assertTrue(containsLocalizedProperty(resource, propertyName, locale));
        return resource.getLocalizedProperties().get(propertyName).get(locale.toString());
    }

    protected boolean containsLocalizedProperty(NodeResource resource, String propertyName, Locale locale)
    {
        assertNotNull(resource);
        assertNotNull(resource.getLocalizedProperties());
        assertNotNull(resource.getLocalizedProperties().get(propertyName));
        return resource.getLocalizedProperties().get(propertyName).containsKey(locale.toString());
    }

    /**
     * Await at most 5 seconds for the condition (ie. {@code numOfEvents}) to be met before throwing a timeout exception.
     *
     * @param eventSequenceNumber
     *            the await condition
     */
    protected void waitUntilNumOfEvents(int eventSequenceNumber)
    {
        try
        {
            await().atMost(10, SECONDS).until(() -> EVENT_CONTAINER.getEvents().size() >= eventSequenceNumber);
        }
        catch (Exception ex)
        {
            fail("Requested event #" + eventSequenceNumber
                    + " but, after waiting 10s, total events in the container are: "
                    + EVENT_CONTAINER.getEvents().size());
        }

    }

    protected void checkNumOfEvents(int expected)
    {
        try
        {
            await().atMost(10, SECONDS).until(() -> EVENT_CONTAINER.getEvents().size() == expected);
        }
        catch (Exception ex)
        {
            assertEquals("Wrong number of events ", expected, EVENT_CONTAINER.getEvents().size());
        }
    }

    private void configRoute() throws Exception
    {
        final ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("admin", "admin", BROKER_URL);
        CAMEL_CONTEXT.addComponent("jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));

        CAMEL_CONTEXT.addRoutes(new RouteBuilder() {
            @Override
            public void configure()
            {
                from(CAMEL_ROUTE).id("RepoEvent2Test")
                        .unmarshal(dataFormat)
                        .process(EVENT_CONTAINER);
            }
        });

        CAMEL_CONTEXT.start();
    }

    public static class RepoEventContainer implements Processor
    {
        private final List<RepoEvent<?>> events = Collections.synchronizedList(new ArrayList<>());

        @Override
        public void process(Exchange exchange)
        {
            Object object = exchange.getIn().getBody();
            events.add((RepoEvent<?>) object);

            if (DEBUG)
            {
                System.err.println("XX: " + object);
            }
        }

        public List<RepoEvent<?>> getEvents()
        {
            return events;
        }

        public RepoEvent<?> getEvent(int eventSequenceNumber)
        {
            int index = eventSequenceNumber - 1;
            if (index < events.size())
            {
                return events.get(index);
            }
            return null;
        }

        public void reset()
        {
            events.clear();
        }

        public boolean isEmpty()
        {
            return events.isEmpty();
        }

    }

    @SuppressWarnings("unchecked")
    public <D extends DataAttributes<? extends Resource>> List<RepoEvent<D>> getFilteredEvents(EventType eventType)
    {
        List<RepoEvent<D>> assocChildCreatedEvents = new ArrayList<>();
        for (RepoEvent<?> event : EVENT_CONTAINER.getEvents())
        {
            if (event.getType().equals(eventType.getType()))
            {
                assocChildCreatedEvents.add((RepoEvent<D>) event);
            }
        }
        return assocChildCreatedEvents;
    }

    @SuppressWarnings("unchecked")
    public <D extends DataAttributes<? extends Resource>> RepoEvent<D> getFilteredEvent(EventType eventType, int index)
    {
        List<RepoEvent<D>> events = new ArrayList<>();
        for (RepoEvent<?> event : EVENT_CONTAINER.getEvents())
        {
            if (event.getType().equals(eventType.getType()))
            {
                events.add((RepoEvent<D>) event);
            }
        }
        assertTrue(events.size() > index);

        return events.get(index);
    }
}
