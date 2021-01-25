/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.patch.v35;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldService;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * RM V3.5  Create new hold child association to link the record to the hold
 * 
 * @since 3.5
 */
public class RMv35HoldNewChildAssocPatchUnitTest
{
    @Mock
    private FilePlanService mockFilePlanService;

    @Mock
    private HoldService mockHoldService;

    @Mock
    private NodeService mockNodeService;

    @Mock
    private BehaviourFilter mockBehaviourFilter;

    @InjectMocks
    private RMv35HoldNewChildAssocPatch patch;

    private NodeRef filePlanRef, holdRef, heldItemRef;
    private Set<NodeRef> fileplans;
    private List<NodeRef> holds;

    @Mock
    private ChildAssociationRef childAssociationRef;

    private List<ChildAssociationRef> childAssocs;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        filePlanRef = new NodeRef("workspace://SpacesStore/filePlan");
        holdRef = new NodeRef("workspace://SpacesStore/hold");
        heldItemRef = new NodeRef("workspace://SpacesStore/heldItem");
        fileplans = new HashSet<>();
        fileplans.add(filePlanRef);
        holds = new ArrayList<>();
        holds.add(holdRef);
        childAssocs = new ArrayList<>();
        childAssocs.add(childAssociationRef);
    }

    /**
     * Test held items are removed from a hold and re-add to make sure the association is correct
     */
    @Test
    public void testAHoldIsRemovedAndReplacedDuringUpgrade()
    {
        when(mockFilePlanService.getFilePlans()).thenReturn(fileplans);
        when(mockHoldService.getHolds(filePlanRef)).thenReturn(holds);
        when(childAssociationRef.getChildRef()).thenReturn(heldItemRef);
        when(mockNodeService.getChildAssocs(holdRef)).thenReturn(childAssocs);
        patch.applyInternal();
        verify(mockHoldService, times(1)).removeFromHold(holdRef, heldItemRef);
        verify(mockHoldService, times(1)).addToHold(holdRef, heldItemRef);
    }

    @Test
    public void patchRunWithSuccessWhenNoHoldChilds()
    {
        List<NodeRef> holdList = new ArrayList<>();
        holdList.add(holdRef);
        when(childAssociationRef.getChildRef()).thenReturn(heldItemRef);
        when(mockNodeService.getChildAssocs(holdRef)).thenReturn(new ArrayList<>());
        patch.applyInternal();

        verify(mockHoldService, times(0)).removeFromHold(holdRef, heldItemRef);
        verify(mockHoldService, times(0)).addToHold(holdRef, heldItemRef);

    }

    @Test
    public void patchRunWithSuccessWhenNoHolds()
    {
        //no holds
        List<NodeRef> holdList = new ArrayList<>();
        when(mockFilePlanService.getFilePlans()).thenReturn(fileplans);
        when(mockHoldService.getHolds(filePlanRef)).thenReturn(holdList);
        patch.applyInternal();

        verify(mockHoldService, times(0)).removeFromHold(holdRef, heldItemRef);
        verify(mockHoldService, times(0)).addToHold(holdRef, heldItemRef);
    }

    @Test
    public void patchRunWithSuccessWhenNoFilePlan()
    {
        // given
        doReturn(Collections.EMPTY_SET).when(mockFilePlanService).getFilePlans();

        // when
        patch.applyInternal();

        // then
        verifyZeroInteractions(mockHoldService);
        verify(mockNodeService, times(0)).addAspect(any(NodeRef.class), any(QName.class), anyMap());
    }
}
