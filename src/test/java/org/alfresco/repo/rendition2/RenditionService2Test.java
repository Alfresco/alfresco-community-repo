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

import org.alfresco.model.ContentModel;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private RenditionService2Impl renditionService2;
    private RenditionDefinitionRegistry2Impl renditionDefinitionRegistry2;

    @Mock private TransformClient transformClient;
    @Mock private TransactionService transactionService;
    @Mock private NodeService nodeService;
    @Mock private ContentService contentService;
    @Mock private RenditionPreventionRegistry renditionPreventionRegistry;
    @Mock private ContentData contentData;
    @Mock private PolicyComponent policyComponent;
    @Mock private BehaviourFilter behaviourFilter;
    @Mock private RuleService ruleService;

    private NodeRef nodeRef = new NodeRef("workspace://spacesStore/test-id");
    private static final String IMGPREVIEW = "imgpreview";
    private static final String JPEG = "image/jpeg";
    private String contentUrl = "test-content-url";

    @Before
    public void setup() throws Exception
    {
        renditionService2 = new RenditionService2Impl();
        renditionDefinitionRegistry2 = new RenditionDefinitionRegistry2Impl();

        Map<String, String> options = new HashMap<>();
        options.put("width", "960");
        options.put("height", "1024");
        new RenditionDefinition2Impl(IMGPREVIEW, JPEG, options, renditionDefinitionRegistry2);

        when(nodeService.exists(nodeRef)).thenReturn(true);
        when(nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT)).thenReturn(contentData);
        when(nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED)).thenReturn(new Date());
        when(contentData.getContentUrl()).thenReturn(contentUrl);

        renditionService2.setTransactionService(transactionService);
        renditionService2.setNodeService(nodeService);
        renditionService2.setContentService(contentService);
        renditionService2.setRenditionPreventionRegistry(renditionPreventionRegistry);
        renditionService2.setRenditionDefinitionRegistry2(renditionDefinitionRegistry2);
        renditionService2.setTransformClient(transformClient);
        renditionService2.setPolicyComponent(policyComponent);
        renditionService2.setBehaviourFilter(behaviourFilter);
        renditionService2.setRuleService(ruleService);
        renditionService2.setTransactionService(transactionService);
        renditionService2.setEnabled(true);
        renditionService2.setThumbnailsEnabled(true);
        renditionService2.setRenditionRequestSheduler(new RenditionRequestSchedulerMock());
        renditionService2.afterPropertiesSet();
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
                fail("The rendition callback failed: " + throwable);
            }
        }
    }

    @Test(expected = RenditionService2Exception.class)
    public void disabled()
    {
        renditionService2.setEnabled(false);
        renditionService2.render(nodeRef, IMGPREVIEW);
    }

    @Test(expected = RenditionService2Exception.class)
    public void thumbnailsDisabled()
    {
        renditionService2.setThumbnailsEnabled(false);
        renditionService2.render(nodeRef, IMGPREVIEW);
    }

    @Test
    public void useLocalTransform()
    {
        renditionService2.render(nodeRef, IMGPREVIEW);
        verify(transformClient, times(1)).transform(any(), any(), anyString(), anyInt());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void noTransform()
    {
        doThrow(UnsupportedOperationException.class).when(transformClient).checkSupported(any(), any(), any(), anyLong(), any());
        renditionService2.render(nodeRef, IMGPREVIEW);
    }

    @Test(expected = RenditionService2PreventedException.class)
    public void checkSourceNodeForPreventionClass()
    {
        when(renditionPreventionRegistry.isContentClassRegistered((QName)any())).thenReturn(true);
        renditionService2.render(nodeRef, IMGPREVIEW);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noDefinition()
    {
        renditionService2.render(nodeRef, "doesNotExist");
    }
}
