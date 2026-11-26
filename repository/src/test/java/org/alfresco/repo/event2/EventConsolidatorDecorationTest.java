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
package org.alfresco.repo.event2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import org.alfresco.repo.event.v1.model.EventType;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

@RunWith(MockitoJUnitRunner.class)
public class EventConsolidatorDecorationTest
{

    @Mock
    private NodeResourceHelper nodeResourceHelper;
    @Mock
    private NodeRef nodeRef;
    @Mock
    private NodeResource.Builder resourceBuilder;
    @Mock
    private EventInfo eventInfo;
    @Mock
    private ChildAssociationRef childAssocRef;

    private NodeEventConsolidator eventConsolidator;

    @Before
    public void setUp() throws Exception
    {
        eventConsolidator = Mockito.spy(new NodeEventConsolidator(nodeResourceHelper));
        when(nodeResourceHelper.createNodeResourceBuilder(any())).thenReturn(resourceBuilder);
    }

    @Test
    public void shouldDecorateDeletedNodeResource()
    {
        // given
        eventConsolidator.beforeDeleteNode(nodeRef);

        // when
        eventConsolidator.getRepoEvent(eventInfo);

        // then
        verify(eventConsolidator).decorateDeletedNodeResource(resourceBuilder);
        assertThat(eventConsolidator.isEventTypeEqualTo(EventType.NODE_DELETED)).isTrue();
    }

    @Test
    public void shouldNotDecorateCreatedNodeResource()
    {
        // given
        when(childAssocRef.getChildRef()).thenReturn(nodeRef);
        eventConsolidator.onCreateNode(childAssocRef);

        // when
        eventConsolidator.getRepoEvent(eventInfo);

        // then
        verify(eventConsolidator, never()).decorateDeletedNodeResource(any());
        assertThat(eventConsolidator.isEventTypeEqualTo(EventType.NODE_CREATED)).isTrue();
    }

    @Test
    public void shouldNotDecorateUpdatedNodeResource()
    {
        // given
        eventConsolidator.onUpdateProperties(nodeRef, Collections.emptyMap(), Collections.emptyMap());

        // when
        eventConsolidator.getRepoEvent(eventInfo);

        // then
        verify(eventConsolidator, never()).decorateDeletedNodeResource(any());
        assertThat(eventConsolidator.isEventTypeEqualTo(EventType.NODE_UPDATED)).isTrue();
    }

    @Test
    public void shouldNotDecorateMovedNodeResource()
    {
        // given
        when(childAssocRef.getChildRef()).thenReturn(nodeRef);
        when(childAssocRef.isPrimary()).thenReturn(true);
        eventConsolidator.onMoveNode(childAssocRef, childAssocRef);

        // when
        eventConsolidator.getRepoEvent(eventInfo);

        // then
        verify(eventConsolidator, never()).decorateDeletedNodeResource(any());
        assertThat(eventConsolidator.isEventTypeEqualTo(EventType.NODE_UPDATED)).isTrue();
    }
}
