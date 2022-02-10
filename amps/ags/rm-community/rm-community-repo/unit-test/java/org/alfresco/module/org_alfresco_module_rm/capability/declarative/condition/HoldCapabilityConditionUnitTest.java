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

package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

/**
 * Freeze evaluator unit test.
 * 
 * @author Roy Wetherall
 */
public class HoldCapabilityConditionUnitTest extends BaseUnitTest
{
    /** test data */
    private NodeRef hold1;
    private NodeRef hold2;
    private List<NodeRef> holds;
    
    /** mocked objects */
    private @Mock(name="kinds") Set<FilePlanComponentKind> mockedKinds;
    
    /** evaluator */
    private @Spy @InjectMocks HoldCapabilityCondition evaluator;
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest#before()
     */
    @Before
    @Override
    public void before() throws Exception
    {
        super.before();
        
        // setup test data
        hold1 = generateNodeRef(TYPE_HOLD);
        hold2 = generateNodeRef(TYPE_HOLD);
        holds = new ArrayList<>(2);
        holds.add(hold1);
        holds.add(hold2);
        
        // setup interactions
        doReturn(false).when(mockedKinds).contains(FilePlanComponentKind.RECORD_CATEGORY);
        doReturn(true).when(mockedKinds).contains(FilePlanComponentKind.RECORD_FOLDER);
        doReturn(true).when(mockedKinds).contains(FilePlanComponentKind.HOLD);
    }

    /**
     * Test given there are no holds 
     */
    @Test
    public void noHolds()
    {
        // given
        doReturn(Collections.EMPTY_LIST).when(mockedHoldService).heldBy(eq(recordFolder), anyBoolean());

        // when
        boolean result = evaluator.evaluateImpl(recordFolder);

        // then
        assertFalse(result);
        verify(mockedPermissionService, never()).hasPermission(any(NodeRef.class), eq(RMPermissionModel.FILING));
        
    }
    
    /**
     * Test given the user has no filling permissions on any of the available holds
     */
    @Test
    public void noFillingOnHolds()
    {
        // given
        doReturn(holds).when(mockedHoldService).heldBy(eq(recordFolder), anyBoolean());
        doReturn(AccessStatus.DENIED).when(mockedPermissionService).hasPermission(hold1, RMPermissionModel.FILING);
        doReturn(AccessStatus.DENIED).when(mockedPermissionService).hasPermission(hold2, RMPermissionModel.FILING);  

        // when
        boolean result = evaluator.evaluateImpl(recordFolder);

        // then
        assertFalse(result);
        verify(mockedPermissionService, times(2)).hasPermission(any(NodeRef.class), eq(RMPermissionModel.FILING));  
        
    }
    
    /**
     * Test given the user has filling on one of the available holds
     */
    @Test
    public void fillingOnHolds()
    {
        // given
        doReturn(holds).when(mockedHoldService).heldBy(eq(recordFolder), anyBoolean());
        doReturn(AccessStatus.DENIED).when(mockedPermissionService).hasPermission(hold1, RMPermissionModel.FILING);
        doReturn(AccessStatus.ALLOWED).when(mockedPermissionService).hasPermission(hold2, RMPermissionModel.FILING);  

        // when
        boolean result = evaluator.evaluateImpl(recordFolder);

        // then
        assertTrue(result);
        verify(mockedPermissionService, times(2)).hasPermission(any(NodeRef.class), eq(RMPermissionModel.FILING));          
    }
}
