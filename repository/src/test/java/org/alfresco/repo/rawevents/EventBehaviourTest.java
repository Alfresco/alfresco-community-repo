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

import java.util.Locale;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.BehaviourDefinition;
import org.alfresco.repo.policy.ClassBehaviourBinding;
import org.alfresco.repo.policy.EventBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.rawevents.types.Event;
import org.alfresco.repo.rawevents.types.EventType;
import org.alfresco.repo.rawevents.types.OnContentUpdatePolicyEvent;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.apache.camel.CamelContext;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Provides a base set of tests for {@link EventBehaviour}
 *
 * @author Cristian Turlica
 */
public class EventBehaviourTest extends BaseSpringTest
{
    private static final String TEST_NAMESPACE = "http://www.alfresco.org/test/EventBehaviourTest";

    @Autowired
    private RetryingTransactionHelper retryingTransactionHelper;
    @Autowired
    private ContentService contentService;
    @Autowired
    private PolicyComponent policyComponent;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private AuthenticationComponent authenticationComponent;
    private NodeRef rootNodeRef;
    private NodeRef contentNodeRef;

    @Autowired
    private CamelContext camelContext;
    @Autowired
    private TransactionAwareEventProducer eventProducer;

    private boolean policyFired = false;
    private boolean newContent = true;

    @Before
    public void setUp() throws Exception
    {
        // authenticate
        authenticationComponent.setSystemUserAsCurrentUser();
    }

    @After
    public void tearDown() throws Exception
    {
        try
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
        catch (Throwable e)
        {
            // ignore
        }
    }

    private void validateSetUp()
    {
        assertNotNull(contentService);
        assertNotNull(nodeService);
        assertNotNull(rootNodeRef);
        assertNotNull(contentNodeRef);

        assertNotNull(camelContext);
        assertNotNull(eventProducer);
    }

    private void setupTestData()
    {
        // create a store and get the root node
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, this.getClass().getName());
        if (!nodeService.exists(storeRef))
        {
            storeRef = nodeService.createStore(storeRef.getProtocol(), storeRef.getIdentifier());
        }
        this.rootNodeRef = nodeService.getRootNode(storeRef);
        // create a content node
        ContentData contentData = new ContentData(null, "text/plain", 0L, "UTF-16", Locale.CHINESE);

        PropertyMap properties = new PropertyMap();
        properties.put(ContentModel.PROP_CONTENT, contentData);

        ChildAssociationRef assocRef = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName(TEST_NAMESPACE, GUID.generate()),
                ContentModel.TYPE_CONTENT, properties);
        this.contentNodeRef = assocRef.getChildRef();

        validateSetUp();
    }

    /**
     * Tests that the content update policy is triggered correctly for the first
     * event.
     */
    @Test
    public void onContentUpdatePolicyFirstEventNF() throws Exception
    {
        this.policyFired = false;
        String endpointUri = getMockEndpointUri();

        MockEndpoint mockEndpoint = camelContext.getEndpoint(endpointUri, MockEndpoint.class);
        mockEndpoint.setAssertPeriod(500);

        retryingTransactionHelper.doInTransaction(() -> {
            BehaviourDefinition<ClassBehaviourBinding> classBehaviour = null;

            try
            {
                setupTestData();

                EventBehaviour eventBehaviour = new EventBehaviour(eventProducer, endpointUri, this, "createOnContentUpdateEvent", Behaviour.NotificationFrequency.FIRST_EVENT);

                // Register interest in the content update event for a versionable node
                classBehaviour = this.policyComponent.bindClassBehaviour(ContentServicePolicies.OnContentUpdatePolicy.QNAME, ContentModel.ASPECT_VERSIONABLE, eventBehaviour);

                // First check that the policy is not fired when the versionable aspect is not
                // present
                ContentWriter contentWriter = this.contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
                contentWriter.putContent("content update one");
                assertFalse(this.policyFired);

                this.newContent = false;

                // Now check that the policy is fired when the versionable aspect is present
                this.nodeService.addAspect(this.contentNodeRef, ContentModel.ASPECT_VERSIONABLE, null);
                ContentWriter contentWriter2 = this.contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
                contentWriter2.putContent("content update two");
                assertTrue(this.policyFired);
                this.policyFired = false;

                // Check that the policy is not fired when using a non updating content writer
                ContentWriter contentWriter3 = this.contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, false);
                contentWriter3.putContent("content update three");
                assertFalse(this.policyFired);

                // Now check that the policy isn't fired when the versionable aspect is present
                // (because it's triggered only for the first event regardless how many times it
                // is updated in that transaction)
                this.nodeService.addAspect(this.contentNodeRef, ContentModel.ASPECT_VERSIONABLE, null);
                ContentWriter contentWriter4 = this.contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
                contentWriter4.putContent("content update four");
                assertFalse(this.policyFired);

                // Assert that the endpoint didn't receive any message
                // Event is sent only on transaction commit.
                mockEndpoint.setExpectedCount(0);
                mockEndpoint.assertIsSatisfied();
            }
            finally
            {
                if (classBehaviour != null)
                {
                    this.policyComponent.removeClassDefinition(classBehaviour);
                }
            }

            return null;
        });

        // Assert that the endpoint received 1 messages
        mockEndpoint.setExpectedCount(1);
        mockEndpoint.assertIsSatisfied();
    }

    /**
     * Tests that the content update policy is triggered correctly for every event.
     */
    @Test
    public void onContentUpdatePolicyEveryEventNF() throws Exception
    {
        this.policyFired = false;
        String endpointUri = getMockEndpointUri();

        MockEndpoint mockEndpoint = camelContext.getEndpoint(endpointUri, MockEndpoint.class);
        mockEndpoint.setAssertPeriod(500);

        retryingTransactionHelper.doInTransaction(() -> {
            BehaviourDefinition<ClassBehaviourBinding> classBehaviour = null;

            try
            {
                setupTestData();

                EventBehaviour eventBehaviour = new EventBehaviour(eventProducer, endpointUri, this, "createOnContentUpdateEvent", Behaviour.NotificationFrequency.EVERY_EVENT);

                // Register interest in the content update event for a versionable node
                classBehaviour = this.policyComponent.bindClassBehaviour(ContentServicePolicies.OnContentUpdatePolicy.QNAME, ContentModel.ASPECT_VERSIONABLE, eventBehaviour);

                // First check that the policy is not fired when the versionable aspect is not
                // present
                ContentWriter contentWriter = this.contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
                contentWriter.putContent("content update one");
                assertFalse(this.policyFired);

                this.newContent = false;

                // Now check that the policy is fired when the versionable aspect is present
                this.nodeService.addAspect(this.contentNodeRef, ContentModel.ASPECT_VERSIONABLE, null);
                ContentWriter contentWriter2 = this.contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
                contentWriter2.putContent("content update two");
                assertTrue(this.policyFired);
                this.policyFired = false;

                // Check that the policy is not fired when using a non updating content writer
                ContentWriter contentWriter3 = this.contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, false);
                contentWriter3.putContent("content update three");
                assertFalse(this.policyFired);

                // Now check that the policy is fired when the versionable aspect is present
                this.nodeService.addAspect(this.contentNodeRef, ContentModel.ASPECT_VERSIONABLE, null);
                ContentWriter contentWriter4 = this.contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
                contentWriter4.putContent("content update four");
                assertTrue(this.policyFired);
                this.policyFired = false;

                try
                {
                    eventBehaviour.disable();

                    ContentWriter contentWriter5 = this.contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
                    contentWriter5.putContent("content update five");
                    assertFalse(this.policyFired);
                }
                finally
                {
                    eventBehaviour.enable();
                }

                // Assert that the endpoint didn't receive any message
                // Event is sent only on transaction commit.
                mockEndpoint.setExpectedCount(0);
                mockEndpoint.assertIsSatisfied();
            }
            finally
            {
                if (classBehaviour != null)
                {
                    this.policyComponent.removeClassDefinition(classBehaviour);
                }
            }

            return null;
        });

        // Assert that the endpoint received 2 messages.
        mockEndpoint.setExpectedCount(2);
        mockEndpoint.assertIsSatisfied();
    }

    /**
     * Tests that the content update policy is triggered correctly for transaction
     * commit (the default notification frequency).
     */
    @Test
    public void onContentUpdatePolicyTxnCommitNF() throws Exception
    {
        this.policyFired = false;
        String endpointUri = getMockEndpointUri();

        MockEndpoint mockEndpoint = camelContext.getEndpoint(endpointUri, MockEndpoint.class);
        mockEndpoint.setAssertPeriod(500);

        retryingTransactionHelper.doInTransaction(() -> {
            BehaviourDefinition<ClassBehaviourBinding> classBehaviour = null;

            try
            {
                setupTestData();

                EventBehaviour eventBehaviour = new EventBehaviour(eventProducer, endpointUri, this, "createOnContentUpdateEvent",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT);

                // Register interest in the content update event for a versionable node
                classBehaviour = this.policyComponent.bindClassBehaviour(ContentServicePolicies.OnContentUpdatePolicy.QNAME, ContentModel.ASPECT_VERSIONABLE, eventBehaviour);

                // First check that the policy is not fired when the versionable aspect is not
                // present
                ContentWriter contentWriter = this.contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
                contentWriter.putContent("content update one");
                assertFalse(this.policyFired);

                this.newContent = false;

                // The policy is fired when the versionable aspect is present (on transaction
                // commit)
                this.nodeService.addAspect(this.contentNodeRef, ContentModel.ASPECT_VERSIONABLE, null);
                ContentWriter contentWriter2 = this.contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
                contentWriter2.putContent("content update two");
                assertFalse(this.policyFired);

                // Check that the policy is not fired when using a non updating content writer
                ContentWriter contentWriter3 = this.contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, false);
                contentWriter3.putContent("content update three");
                assertFalse(this.policyFired);

                // The policy is fired when the versionable aspect is present (on transaction
                // commit)
                this.nodeService.addAspect(this.contentNodeRef, ContentModel.ASPECT_VERSIONABLE, null);
                ContentWriter contentWriter4 = this.contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
                contentWriter4.putContent("content update four");
                assertFalse(this.policyFired);

                // Assert that the endpoint didn't receive any message
                // Event is sent only on transaction commit.
                mockEndpoint.setExpectedCount(0);
                mockEndpoint.assertIsSatisfied();
            }
            finally
            {
                if (classBehaviour != null)
                {
                    this.policyComponent.removeClassDefinition(classBehaviour);
                }
            }

            return null;
        });

        // Assert that the endpoint received 1 messages
        // Event is created once per transaction regardless how many times it is updated
        mockEndpoint.setExpectedCount(1);
        mockEndpoint.assertIsSatisfied();
    }

    private String getMockEndpointUri()
    {
        return "mock:" + this.getClass().getSimpleName() + "_" + GUID.generate();
    }

    @SuppressWarnings("unused")
    public Event createOnContentUpdateEvent(NodeRef nodeRef, boolean newContent)
    {
        assertEquals(this.contentNodeRef, nodeRef);
        assertEquals(this.newContent, newContent);
        assertTrue(this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE));
        this.policyFired = true;

        long timestamp = System.currentTimeMillis();

        OnContentUpdatePolicyEvent event = new OnContentUpdatePolicyEvent();
        event.setType(EventType.CONTENT_UPDATED.toString());
        event.setTimestamp(timestamp);
        event.setNodeRef(nodeRef.toString());
        event.setNewContent(newContent);

        return event;
    }
}