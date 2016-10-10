/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.PROP_DISPOSITION_AS_OF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
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

    /** Inject the mock services into the class under test and link the content and next action nodes. */
    @Before
    public void setUp()
    {
        action.setNodeService(mockNodeService);
        action.setDispositionService(mockDispositionService);

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
        when(mockDispositionService.calculateAsOfDate(CONTENT_NODE_REF, mockDispositionActionDefinition, false))
                        .thenReturn(asOfDate);

        // Call the method under test.
        action.persistPeriodChanges(DISPOSITION_ACTION_DEF_NODE, mockAction);

        // Check that the "disposition as of" date has been set on the next action.
        verify(mockNodeService).setProperty(NEXT_ACTION_NODE_REF, PROP_DISPOSITION_AS_OF, asOfDate);
    }
}
