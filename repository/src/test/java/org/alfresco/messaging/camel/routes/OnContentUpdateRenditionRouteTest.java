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
package org.alfresco.messaging.camel.routes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.rendition2.RenditionService2Impl;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.util.PostTxnCallbackScheduler;
import org.alfresco.service.cmr.repository.NodeRef;

@RunWith(MockitoJUnitRunner.class)
public class OnContentUpdateRenditionRouteTest
{
    private static final NodeRef NODE = new NodeRef("workspace://SpacesStore/test-id");

    @Mock
    private PolicyComponent policyComponent;
    @Mock
    private PostTxnCallbackScheduler scheduler;
    @Mock
    private RenditionService2Impl renditionService2;

    private OnContentUpdateRenditionRoute route;

    @Before
    public void setUp()
    {
        route = new OnContentUpdateRenditionRoute();
        ReflectionTestUtils.setField(route, "policyComponent", policyComponent);
        ReflectionTestUtils.setField(route, "renditionRequestSheduler", scheduler);
        ReflectionTestUtils.setField(route, "renditionService2", renditionService2);
        route.sourceQueue = "direct:content-update-events-of-earlier-versions";
    }

    @Test
    public void contentUpdateSchedulesAReRenderThatRunsAfterCommit() throws Throwable
    {
        route.onContentUpdate(NODE, true);

        RetryingTransactionCallback<?> callback = scheduledCallback(1);
        verifyNoInteractions(renditionService2);

        callback.execute();
        verify(renditionService2).onContentUpdate(NODE, true);
    }

    @Test
    public void repeatedUpdatesOfADocumentShareOneQueueEntry()
    {
        route.onContentUpdate(NODE, true);
        route.onContentUpdate(NODE, false);

        verify(scheduler, times(2)).scheduleRendition(any(), eq("onContentUpdate:" + NODE));
    }

    private RetryingTransactionCallback<?> scheduledCallback(int times)
    {
        ArgumentCaptor<RetryingTransactionCallback> callback = ArgumentCaptor.forClass(RetryingTransactionCallback.class);
        verify(scheduler, times(times)).scheduleRendition(callback.capture(), eq("onContentUpdate:" + NODE));
        return callback.getValue();
    }
}
