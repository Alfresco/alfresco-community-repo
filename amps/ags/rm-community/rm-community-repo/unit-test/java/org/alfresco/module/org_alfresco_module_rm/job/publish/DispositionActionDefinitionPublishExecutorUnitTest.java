/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.job.publish;

import static java.util.Collections.singletonList;
import static org.alfresco.module.org_alfresco_module_rm.action.impl.BroadcastDispositionActionDefinitionUpdateAction.BATCHING_ENABLED;
import static org.alfresco.module.org_alfresco_module_rm.action.impl.BroadcastDispositionActionDefinitionUpdateAction.BATCHING_SIZE;
import static org.alfresco.module.org_alfresco_module_rm.action.impl.BroadcastDispositionActionDefinitionUpdateAction.BATCHING_THREADS;
import static org.alfresco.module.org_alfresco_module_rm.action.impl.BroadcastDispositionActionDefinitionUpdateAction.CHANGED_PROPERTIES;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.action.impl.BroadcastDispositionActionDefinitionUpdateAction;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Unit tests for {@link DispositionActionDefinitionPublishExecutor}.
 */
public class DispositionActionDefinitionPublishExecutorUnitTest
{
    private final NodeRef actionDefinitionNode = new NodeRef("workspace://SpacesStore/action-def");

    private final DispositionActionDefinitionPublishExecutor executor = new DispositionActionDefinitionPublishExecutor();
    private final NodeService nodeService = mock(NodeService.class);
    private final RecordsManagementActionService rmActionService = mock(RecordsManagementActionService.class);

    @Before
    public void setUp()
    {
        executor.setNodeService(nodeService);
        executor.setRmActionService(rmActionService);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPublishPassesBatchingParameters()
    {
        List<QName> updatedProps = singletonList(RecordsManagementModel.PROP_DISPOSITION_PERIOD);
        when(nodeService.getProperty(actionDefinitionNode, RecordsManagementModel.PROP_UPDATED_PROPERTIES))
                .thenReturn((Serializable) updatedProps);

        executor.setBatchingEnabled(true);
        executor.setBatchSize(500);

        executor.publish(actionDefinitionNode);

        ArgumentCaptor<Map<String, Serializable>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(rmActionService).executeRecordsManagementAction(
                eq(actionDefinitionNode),
                eq(BroadcastDispositionActionDefinitionUpdateAction.NAME),
                paramsCaptor.capture());

        Map<String, Serializable> params = paramsCaptor.getValue();
        assertEquals(updatedProps, params.get(CHANGED_PROPERTIES));
        assertEquals(Boolean.TRUE, params.get(BATCHING_ENABLED));
        assertEquals(500, params.get(BATCHING_SIZE));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPublishUsesDefaultBatchSizeWhenConfiguredValueIsInvalid()
    {
        List<QName> updatedProps = singletonList(RecordsManagementModel.PROP_DISPOSITION_PERIOD);
        when(nodeService.getProperty(actionDefinitionNode, RecordsManagementModel.PROP_UPDATED_PROPERTIES))
                .thenReturn((Serializable) updatedProps);

        executor.setBatchingEnabled(true);
        executor.setBatchSize(0);

        executor.publish(actionDefinitionNode);

        ArgumentCaptor<Map<String, Serializable>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(rmActionService).executeRecordsManagementAction(
                eq(actionDefinitionNode),
                eq(BroadcastDispositionActionDefinitionUpdateAction.NAME),
                paramsCaptor.capture());

        assertEquals(100, paramsCaptor.getValue().get(BATCHING_SIZE));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPublishPassesWorkerThreadsParameter()
    {
        List<QName> updatedProps = singletonList(RecordsManagementModel.PROP_DISPOSITION_PERIOD);
        when(nodeService.getProperty(actionDefinitionNode, RecordsManagementModel.PROP_UPDATED_PROPERTIES))
                .thenReturn((Serializable) updatedProps);

        executor.setBatchingEnabled(true);
        executor.setBatchSize(100);
        executor.setWorkerThreads(8);

        executor.publish(actionDefinitionNode);

        ArgumentCaptor<Map<String, Serializable>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(rmActionService).executeRecordsManagementAction(
                eq(actionDefinitionNode),
                eq(BroadcastDispositionActionDefinitionUpdateAction.NAME),
                paramsCaptor.capture());

        assertEquals(8, paramsCaptor.getValue().get(BATCHING_THREADS));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPublishUsesDefaultWorkerThreadsWhenConfiguredValueIsInvalid()
    {
        List<QName> updatedProps = singletonList(RecordsManagementModel.PROP_DISPOSITION_PERIOD);
        when(nodeService.getProperty(actionDefinitionNode, RecordsManagementModel.PROP_UPDATED_PROPERTIES))
                .thenReturn((Serializable) updatedProps);

        executor.setBatchingEnabled(true);
        executor.setBatchSize(100);
        executor.setWorkerThreads(0);

        executor.publish(actionDefinitionNode);

        ArgumentCaptor<Map<String, Serializable>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(rmActionService).executeRecordsManagementAction(
                eq(actionDefinitionNode),
                eq(BroadcastDispositionActionDefinitionUpdateAction.NAME),
                paramsCaptor.capture());

        assertEquals(4, paramsCaptor.getValue().get(BATCHING_THREADS));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPublishPassesBatchingDisabledParameter()
    {
        List<QName> updatedProps = singletonList(RecordsManagementModel.PROP_DISPOSITION_PERIOD);
        when(nodeService.getProperty(actionDefinitionNode, RecordsManagementModel.PROP_UPDATED_PROPERTIES))
                .thenReturn((Serializable) updatedProps);

        executor.setBatchingEnabled(false);
        executor.setBatchSize(100);

        executor.publish(actionDefinitionNode);

        ArgumentCaptor<Map<String, Serializable>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(rmActionService).executeRecordsManagementAction(
                eq(actionDefinitionNode),
                eq(BroadcastDispositionActionDefinitionUpdateAction.NAME),
                paramsCaptor.capture());

        assertEquals(Boolean.FALSE, paramsCaptor.getValue().get(BATCHING_ENABLED));
    }

    @Test
    public void testPublishSkipsActionWhenUpdatedPropsIsNull()
    {
        when(nodeService.getProperty(actionDefinitionNode, RecordsManagementModel.PROP_UPDATED_PROPERTIES))
                .thenReturn(null);

        executor.publish(actionDefinitionNode);

        verifyNoInteractions(rmActionService);
    }
}
