/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.metadata.AsynchronousExtractor;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.rendition.RenditionPreventionRegistry;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.util.PostTxnCallbackScheduler;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.transform.registry.TransformServiceRegistryImpl;

/**
 * Tests the RenditionService2 in a Community context where we only have local transformers.
 *
 * Also see EnterpriseRenditionService2Test.
 *
 * @author adavis
 */
@RunWith(MockitoJUnitRunner.class)
public class RenditionService2Test
{
    private static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();;

    private RenditionService2Impl renditionService2;
    private RenditionDefinitionRegistry2Impl renditionDefinitionRegistry2;

    @Mock
    private TransformClient transformClient;
    @Mock
    private TransactionService transactionService;
    @Mock
    private NodeService nodeService;
    @Mock
    private NodeDAO nodeDAO;
    @Mock
    private ContentService contentService;
    @Mock
    private RenditionPreventionRegistry renditionPreventionRegistry;
    @Mock
    private ContentData contentData;
    @Mock
    private PolicyComponent policyComponent;
    @Mock
    private BehaviourFilter behaviourFilter;
    @Mock
    private RuleService ruleService;
    @Mock
    private TransformServiceRegistryImpl transformServiceRegistry;
    @Mock
    private TransformReplyProvider transformReplyProvider;
    @Mock
    private AsynchronousExtractor asynchronousExtractor;

    private NodeRef nodeRef = new NodeRef("workspace://spacesStore/test-id");
    private NodeRef nodeRefMissing = new NodeRef("workspace://spacesStore/bad-test-id");
    private static final String TEST_RENDITION = "testRendition";
    private static final String JPEG = "image/jpeg";
    private String contentUrl = "test-content-url";

    private static final String CLIENT_DATA = "some_clientData";
    private static final String REPLY_QUEUE = "some_replyQueue";
    private static final String REQUEST_ID = "some_requestId";
    private static final TransformDefinition TEST_TRANSFORM = new TransformDefinition(JPEG, Collections.singletonMap("a", "A"), CLIENT_DATA, REPLY_QUEUE, REQUEST_ID);
    private boolean failureCalled;

    @Before
    public void setup() throws Exception
    {
        renditionService2 = new RenditionService2Impl() {
            @Override
            public void failure(NodeRef sourceNodeRef, RenditionDefinition2 renditionDefinition, int transformContentHashCode)
            {
                failureCalled = true;
            }
        };
        renditionDefinitionRegistry2 = new RenditionDefinitionRegistry2Impl();
        renditionDefinitionRegistry2.setTransformServiceRegistry(transformServiceRegistry);
        renditionDefinitionRegistry2.setRenditionConfigDir("");
        renditionDefinitionRegistry2.setTimeoutDefault("120000");
        renditionDefinitionRegistry2.setJsonObjectMapper(JSON_OBJECT_MAPPER);
        renditionDefinitionRegistry2.setCronExpression(null); // just read it once

        when(nodeService.exists(nodeRef)).thenReturn(true);
        when(nodeService.exists(nodeRefMissing)).thenReturn(false);
        when(nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT)).thenReturn(contentData);
        when(contentData.getContentUrl()).thenReturn(contentUrl);
        when(contentData.getSize()).thenReturn(1024L);

        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            NodeRef sourceNodeRef = (NodeRef) args[0];
            RenditionDefinition2 renditionDefinition = (RenditionDefinition2) args[1];
            int sourceContentHashCode = (int) args[3];
            if (!(renditionDefinition instanceof TransformDefinition))
            {
                sourceContentHashCode += 1; // Change the hashcode so that we don't actually try to create a rendition.
            }
            renditionService2.consume(sourceNodeRef, null, renditionDefinition, sourceContentHashCode);
            return null;
        }).when(transformClient).transform(any(), any(), nullable(String.class), anyInt());

        renditionService2.setTransactionService(transactionService);
        renditionService2.setNodeService(nodeService);
        renditionService2.setNodeDAO(nodeDAO);
        renditionService2.setContentService(contentService);
        renditionService2.setRenditionPreventionRegistry(renditionPreventionRegistry);
        renditionService2.setRenditionDefinitionRegistry2(renditionDefinitionRegistry2);
        renditionService2.setTransformClient(transformClient);
        renditionService2.setPolicyComponent(policyComponent);
        renditionService2.setBehaviourFilter(behaviourFilter);
        renditionService2.setRuleService(ruleService);
        renditionService2.setTransactionService(transactionService);
        renditionService2.setRenditionRequestSheduler(new RenditionRequestSchedulerMock());
        renditionService2.setTransformReplyProvider(transformReplyProvider);
        renditionService2.setEnabled(true);
        renditionService2.setThumbnailsEnabled(true);
        renditionService2.setAsynchronousExtractor(asynchronousExtractor);

        renditionDefinitionRegistry2.setRenditionConfigDir("alfresco/renditions/test");
        renditionDefinitionRegistry2.afterPropertiesSet();
        renditionService2.afterPropertiesSet();

        Map<String, String> options = new HashMap<>();
        options.put("width", "960");
        options.put("height", "1024");
        new RenditionDefinition2Impl(TEST_RENDITION, JPEG, options, true, renditionDefinitionRegistry2);
    }

    private class RenditionRequestSchedulerMock extends PostTxnCallbackScheduler
    {
        @Override
        public void scheduleRendition(RetryingTransactionHelper.RetryingTransactionCallback callback, String uniqueId)
        {
            try
            {
                callback.execute();
            }
            catch (Throwable throwable)
            {
                throwable.printStackTrace();
                fail("The rendition callback failed: " + throwable);
            }
        }
    }

    @Test(expected = RenditionService2Exception.class)
    public void disabledForRenditions()
    {
        renditionService2.setEnabled(false);
        renditionService2.render(nodeRef, TEST_RENDITION);
    }

    @Test(expected = RenditionService2Exception.class)
    public void disabledForTransforms()
    {
        renditionService2.setEnabled(false);
        renditionService2.transform(nodeRef, TEST_TRANSFORM);
    }

    @Test(expected = RenditionService2Exception.class)
    public void thumbnailsDisabledForRenditions()
    {
        renditionService2.setThumbnailsEnabled(false);
        renditionService2.render(nodeRef, TEST_RENDITION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nodeRefDoesNotExistForRenditions()
    {
        renditionService2.render(nodeRefMissing, TEST_RENDITION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nodeRefDoesNotExistForTransforms()
    {
        renditionService2.transform(nodeRefMissing, TEST_TRANSFORM);
    }

    @Test
    public void useLocalTransformForRenditions()
    {
        renditionService2.render(nodeRef, TEST_RENDITION);

        verify(transformClient, times(1)).transform(any(), any(), any(), anyInt());
        verify(transformClient, times(1)).transform(any(), any(), nullable(String.class), anyInt());
        verify(transformReplyProvider, times(0)).produceTransformEvent(any(), any(), any(), anyInt());
    }

    @Test
    public void useLocalTransformForTransforms()
    {
        renditionService2.transform(nodeRef, TEST_TRANSFORM);
        verify(transformClient, times(1)).transform(any(), any(), nullable(String.class), anyInt());
        verify(transformReplyProvider, times(1)).produceTransformEvent(any(), any(), any(), anyInt());

    }

    @Test(expected = UnsupportedOperationException.class)
    public void noTransformForRenditions()
    {
        doThrow(UnsupportedOperationException.class).when(transformClient).checkSupported(any(), any(), any(), anyLong(), any());
        renditionService2.render(nodeRef, TEST_RENDITION);
    }

    @Test
    public void noTransformForTransforms()
    {
        doThrow(UnsupportedOperationException.class).when(transformClient).checkSupported(any(), any(), any(), anyLong(), any());
        renditionService2.transform(nodeRef, TEST_TRANSFORM);
        assertTrue("failure() should be called with transform(...) if an unsupported transform rather than " +
                "having UnsupportedOperationException thrown", failureCalled);
    }

    @Test(expected = RenditionService2PreventedException.class)
    public void checkSourceNodeForRenditionPreventionClass()
    {
        when(renditionPreventionRegistry.isContentClassRegistered((QName) any())).thenReturn(true);
        renditionService2.render(nodeRef, TEST_RENDITION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noDefinitionRenditions()
    {
        renditionService2.render(nodeRef, "doesNotExist");
    }

    @Test
    public void renditionDefinitionsExist() throws IOException
    {
        renditionDefinitionRegistry2.readConfig();

        Set<String> renditionNames = renditionDefinitionRegistry2.getRenditionNames();
        for (String name : new String[]{"medium", "doclib", "imgpreview", "avatar", "avatar32", "webpreview", "pdf"})
        {
            assertTrue("Expected rendition " + name, renditionNames.contains(name));
        }
    }

    @Test()
    public void overriddenRendition()
    {
        // Check the standard rendition doclib has been overridden by one in alfresco/renditions/test/overrideRendition.json
        RenditionDefinition2 doclib = renditionDefinitionRegistry2.getRenditionDefinition("doclib");
        String resizeWidth = doclib.getTransformOptions().get("resizeWidth");
        assertEquals("doclib has not been overridden", "180", resizeWidth);
    }

    /**
     * MNT-25665 / ACS-6670: In a cluster with invalidating cache, the rendition callback reads stale ContentData (size=0) because cache invalidation hasn't propagated. The fix detects size=0, calls nodeDAO.setCheckNodeConsistency() to bypass the shared cache, and re-reads from DB. This test simulates the stale cache scenario where the re-read after bypass returns fresh data.
     */
    @Test
    public void staleCacheBypassSucceedsWithFreshDataFromDB()
    {
        ContentData staleContentData = new ContentData("store://stale-url", "application/pdf", 0L, "UTF-8");
        ContentData freshContentData = new ContentData("store://fresh-url", "application/pdf", 4726L, "UTF-8");

        AtomicInteger getPropertyCallCount = new AtomicInteger(0);
        when(nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT)).thenAnswer(invocation -> {
            int call = getPropertyCallCount.incrementAndGet();
            // First 2 calls return stale: initial checkSupported read + getSourceContentHashCode + first re-read in callback
            // After setCheckNodeConsistency() is called, subsequent reads return fresh data from DB
            return call <= 3 ? staleContentData : freshContentData;
        });

        renditionService2.render(nodeRef, TEST_RENDITION);

        verify(nodeDAO).setCheckNodeConsistency();
        verify(transformClient, times(1)).transform(any(), any(), nullable(String.class), anyInt());
        assertFalse("failure() should NOT be called when DB re-read returns valid content", failureCalled);
    }

    /**
     * When content is genuinely 0 bytes (not a stale cache issue), the rendition should be marked as failed even after bypassing the shared cache and re-reading from DB.
     */
    @Test
    public void genuinelyEmptyContentCallsFailure()
    {
        ContentData emptyContentData = new ContentData("store://empty-url", "application/pdf", 0L, "UTF-8");

        // Always return 0 size — this is genuinely empty content, not stale cache
        when(nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT)).thenReturn(emptyContentData);

        renditionService2.render(nodeRef, TEST_RENDITION);

        verify(nodeDAO).setCheckNodeConsistency();
        verify(transformClient, never()).transform(any(), any(), nullable(String.class), anyInt());
        assertTrue("failure() should be called when content is genuinely 0 bytes", failureCalled);
    }
}
