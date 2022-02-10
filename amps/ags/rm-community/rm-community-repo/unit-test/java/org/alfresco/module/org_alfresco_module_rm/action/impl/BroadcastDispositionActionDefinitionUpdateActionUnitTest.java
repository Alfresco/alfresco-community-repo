/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.action.impl;

import static java.util.Arrays.asList;
import static org.alfresco.module.org_alfresco_module_rm.action.impl.BroadcastDispositionActionDefinitionUpdateAction.CHANGED_PROPERTIES;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.ASPECT_DISPOSITION_LIFECYCLE;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.PROP_DISPOSITION_AS_OF;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.PROP_DISPOSITION_PERIOD_PROPERTY;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.TYPE_DISPOSITION_ACTION_DEFINITION;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Date;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link BroadcastDispositionActionDefinitionUpdateAction}.
 *
 * @author Tom Page
 * @since 2.3.1
 */
public class BroadcastDispositionActionDefinitionUpdateActionUnitTest
{
    /** The node under the category containing information about the definition of the action. */
    private static final NodeRef DISPOSITION_ACTION_DEF_NODE = new NodeRef("disposition://Action/Def");
    /** The node containing the details of the next disposition step for the content. */
    private static final NodeRef NEXT_ACTION_NODE_REF = new NodeRef("next://Step/");
    /** The node being subject to the disposition step. */
    private static final NodeRef CONTENT_NODE_REF = new NodeRef("content://Node/Ref");

    /** The class under test. */
    private BroadcastDispositionActionDefinitionUpdateAction action = new BroadcastDispositionActionDefinitionUpdateAction();

    private NodeService mockNodeService = mock(NodeService.class);
    private DispositionService mockDispositionService = mock(DispositionService.class);
    private BehaviourFilter mockBehaviourFilter = mock(BehaviourFilter.class);

    /** Inject the mock services into the class under test and link the content and next action nodes. */
    @Before
    public void setUp()
    {
        action.setNodeService(mockNodeService);
        action.setDispositionService(mockDispositionService);
        action.setBehaviourFilter(mockBehaviourFilter);

        ChildAssociationRef mockAssocRef = mock(ChildAssociationRef.class);
        when(mockNodeService.getPrimaryParent(NEXT_ACTION_NODE_REF)).thenReturn(mockAssocRef);
        when(mockAssocRef.getParentRef()).thenReturn(CONTENT_NODE_REF);
    }

    /**
     * Check that the disposition service is used to determine the "disposition as of" date when changes are made to the
     * disposition period.
     */
    @Test
    public void testPersistPeriodChanges()
    {
        // Set up the data associated with the next disposition action.
        DispositionAction mockAction = mock(DispositionAction.class);
        when(mockAction.getNodeRef()).thenReturn(NEXT_ACTION_NODE_REF);
        DispositionActionDefinition mockDispositionActionDefinition = mock(DispositionActionDefinition.class);
        when(mockAction.getDispositionActionDefinition()).thenReturn(mockDispositionActionDefinition);
        when(mockAction.getName()).thenReturn("mockAction");
        // Set up the disposition service to return a known "disposition as of" date.
        Date asOfDate = new Date();
        when(mockDispositionService.calculateAsOfDate(CONTENT_NODE_REF, mockDispositionActionDefinition))
                        .thenReturn(asOfDate);

        // Call the method under test.
        action.persistPeriodChanges(DISPOSITION_ACTION_DEF_NODE, mockAction);

        // Check that the "disposition as of" date has been set on the next action.
        verify(mockNodeService).setProperty(NEXT_ACTION_NODE_REF, PROP_DISPOSITION_AS_OF, asOfDate);
    }

    /**
     * Check that changing the period property triggers a recalculation of the "disposition as of" date.
     * <p>
     * Set up a disposition action definition node under a schedule defintion node, under a category node. Create a
     * record whose next action is an instance of the action definition. Check that if the "period property" of the
     * action definition changes then the "disposition as of" date is recalculated and persisted against the node of the
     * next action.
     */
    @Test
    public void testChangePeriodProperty()
    {
        // Set up the action definition node.
        String definitionNodeId = "definitionNodeId";
        NodeRef definitionNode = new NodeRef("definition://node/" + definitionNodeId);
        DispositionSchedule mockDispositionSchedule = mock(DispositionSchedule.class);
        when(mockDispositionSchedule.getNodeRef()).thenReturn(definitionNode);
        when(mockNodeService.getType(definitionNode)).thenReturn(TYPE_DISPOSITION_ACTION_DEFINITION);
        // Set up the schedule definition node hierarchy.
        NodeRef categoryNode = new NodeRef("category://node/");
        NodeRef scheduleNode = new NodeRef("schedule://node/");
        ChildAssociationRef scheduleDefinitionRelationship = new ChildAssociationRef(null, scheduleNode, null, definitionNode);
        when(mockNodeService.getPrimaryParent(definitionNode)).thenReturn(scheduleDefinitionRelationship);
        ChildAssociationRef categoryScheduleRelationship = new ChildAssociationRef(null, categoryNode, null, scheduleNode);
        when(mockNodeService.getPrimaryParent(scheduleNode)).thenReturn(categoryScheduleRelationship);
        // Set up the record/step relationship.
        NodeRef recordNode = new NodeRef("record://node/");
        NodeRef stepNode = new NodeRef("step://node/");
        ChildAssociationRef recordStepRelationship = new ChildAssociationRef(null, recordNode, null, stepNode);
        when(mockNodeService.getPrimaryParent(stepNode)).thenReturn(recordStepRelationship);
        // Set up the disposition schedule.
        when(mockDispositionService.getAssociatedDispositionSchedule(categoryNode)).thenReturn(mockDispositionSchedule);
        when(mockDispositionService.getDisposableItems(mockDispositionSchedule)).thenReturn(asList(recordNode));
        when(mockDispositionService.getDispositionSchedule(recordNode)).thenReturn(mockDispositionSchedule);
        // Set up the record.
        when(mockNodeService.hasAspect(recordNode, ASPECT_DISPOSITION_LIFECYCLE)).thenReturn(true);
        // Set up the next disposition action.
        DispositionAction nextAction = mock(DispositionAction.class);
        when(nextAction.getId()).thenReturn(definitionNodeId);
        when(nextAction.getNodeRef()).thenReturn(stepNode);
        when(mockDispositionService.getNextDispositionAction(recordNode)).thenReturn(nextAction);
        DispositionActionDefinition mockActionDefinition = mock(DispositionActionDefinition.class);
        when(nextAction.getDispositionActionDefinition()).thenReturn(mockActionDefinition);

        // Set up the action so that it looks like the period property has been changed.
        Action mockAction = mock(Action.class);
        when(mockAction.getParameterValue(CHANGED_PROPERTIES)).thenReturn((Serializable) asList(PROP_DISPOSITION_PERIOD_PROPERTY));
        // Set up the expected "as of" date.
        Date newAsOfDate = new Date(123456789000L);
        when(mockDispositionService.calculateAsOfDate(recordNode, mockActionDefinition)).thenReturn(newAsOfDate);

        // Call the method under test.
        action.executeImpl(mockAction, definitionNode);

        // Check that the "as of" date is updated.
        verify(mockNodeService).setProperty(stepNode, PROP_DISPOSITION_AS_OF, newAsOfDate);
    }
}
