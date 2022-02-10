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

package org.alfresco.module.org_alfresco_module_rm.patch.v32;

import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.ASSOC_FROZEN_CONTENT;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.ASSOC_FROZEN_RECORDS;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldService;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * RM V3.2 Hold child assoc patch unit test
 * 
 * @author Ross Gale
 * @since 3.2
 */
public class RMv32HoldChildAssocPatchUnitTest
{
    @Mock
    private QNameDAO qNameDAO;

    @Mock
    private NodeService nodeService;

    @Mock
    private FilePlanService filePlanService;

    @Mock
    private HoldService holdService;

    @InjectMocks
    private RMv32HoldChildAssocPatch patch;

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
        when(qNameDAO.getQName(ASSOC_FROZEN_RECORDS)).thenReturn(new Pair(ASSOC_FROZEN_CONTENT,null));
        when(filePlanService.getFilePlans()).thenReturn(fileplans);
        when(holdService.getHolds(filePlanRef)).thenReturn(holds);
        when(nodeService.getChildAssocs(holdRef, ASSOC_FROZEN_CONTENT, ASSOC_FROZEN_RECORDS)).thenReturn(childAssocs);
        when(childAssociationRef.getChildRef()).thenReturn(heldItemRef);
        patch.applyInternal();
        verify(holdService, times(1)).removeFromHold(holdRef, heldItemRef);
        verify(holdService, times(1)).addToHold(holdRef, heldItemRef);
    }

    /**
     * Test patch doesnt run without an association added during rm site creation
     */
    @Test
    public void testAHoldIsntRemovedAndReplacedDuringUpgradeWithNoRmSite()
    {
        when(qNameDAO.getQName(ASSOC_FROZEN_RECORDS)).thenReturn(null);
        patch.applyInternal();
        verify(qNameDAO, never()).updateQName(ASSOC_FROZEN_RECORDS, ASSOC_FROZEN_CONTENT);
    }
}
