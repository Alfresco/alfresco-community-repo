/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

/**
 * Frozen capability condition unit test
 * 
 * @author Roy Wetherall
 * @since 2.3
 */
public class FrozenCapabilityConditionUnitTest extends BaseUnitTest
{
    /** evaluator */
    private @InjectMocks FrozenCapabilityCondition condition;
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest#before()
     */
    @Before
    @Override
    public void before() throws Exception
    {
        super.before();
    }
    
    /**
     * Given hold
     * When evaluate
     * Then true
     */
    @Test
    public void evaluateHold()
    {
        // is a hold
        NodeRef nodeRef = generateNodeRef();
        when(mockedHoldService.isHold(nodeRef))
            .thenReturn(true);        
        
        // evaluate
        assertTrue(condition.evaluate(nodeRef));
        
        // verify
        verify(mockedHoldService, times(1)).isHold(nodeRef);
        verify(mockedFreezeService, never()).isFrozen(nodeRef);
        verify(mockedFreezeService, never()).hasFrozenChildren(nodeRef);
    }
    
    /**
     * Given is frozen
     * And no check children
     * When evaluate
     * Then true
     */
    @Test
    public void frozenDontCheckChildren()
    {
        // is not a hold
        NodeRef nodeRef = generateNodeRef();
        when(mockedHoldService.isHold(nodeRef))
            .thenReturn(false);
        
        // dont check children
        condition.setCheckChildren(false);
        
        // is frozen
        when(mockedFreezeService.isFrozen(nodeRef))
            .thenReturn(true);
        
        // evaluate
        assertTrue(condition.evaluate(nodeRef));
        
        // verify
        verify(mockedHoldService, times(1)).isHold(nodeRef);
        verify(mockedFreezeService, times(1)).isFrozen(nodeRef);
        verify(mockedFreezeService, never()).hasFrozenChildren(nodeRef);
    }
    
    /**
     * Given is not frozen
     * And no check children
     * When evaluate
     * Then false
     */
    @Test
    public void notFrozenDontCheckChildren()
    {
        // is not a hold
        NodeRef nodeRef = generateNodeRef();
        when(mockedHoldService.isHold(nodeRef))
            .thenReturn(false);
        
        // dont check children
        condition.setCheckChildren(false);
        
        // is not frozen
        when(mockedFreezeService.isFrozen(nodeRef))
            .thenReturn(false);
        
        // evaluate
        assertFalse(condition.evaluate(nodeRef));
        
        // verify
        verify(mockedHoldService, times(1)).isHold(nodeRef);
        verify(mockedFreezeService, times(1)).isFrozen(nodeRef);
        verify(mockedFreezeService, never()).hasFrozenChildren(nodeRef);
    }
    
    /**
     * Given is frozen
     * And check children
     * When evaluate
     * Then true
     */
    @Test
    public void frozenCheckChildren()
    {
        // is not a hold
        NodeRef nodeRef = generateNodeRef();
        when(mockedHoldService.isHold(nodeRef))
            .thenReturn(false);
        
        // check children
        condition.setCheckChildren(true);
        
        // is frozen
        when(mockedFreezeService.isFrozen(nodeRef))
            .thenReturn(true);
        
        // evaluate
        assertTrue(condition.evaluate(nodeRef));
        
        // verify
        verify(mockedHoldService, times(1)).isHold(nodeRef);
        verify(mockedFreezeService, times(1)).isFrozen(nodeRef);
        verify(mockedFreezeService, never()).hasFrozenChildren(nodeRef);
    }
    
    /**
     * Given is not frozen
     * And check children
     * And children no frozen
     * When evaluate
     * Then false
     */
    @Test
    public void notFrozenCheckChildrenNotFrozen()
    {
        // is not a hold
        NodeRef nodeRef = generateNodeRef();
        when(mockedHoldService.isHold(nodeRef))
            .thenReturn(false);
        
        // check children
        condition.setCheckChildren(true);
        
        // is not frozen
        when(mockedFreezeService.isFrozen(nodeRef))
            .thenReturn(false);
        
        // children not frozen
        when(mockedFreezeService.hasFrozenChildren(nodeRef))
            .thenReturn(false);
        
        // evaluate
        assertFalse(condition.evaluate(nodeRef));
        
        // verify
        verify(mockedHoldService, times(1)).isHold(nodeRef);
        verify(mockedFreezeService, times(1)).isFrozen(nodeRef);
        verify(mockedFreezeService, times(1)).hasFrozenChildren(nodeRef);
    }
    
    /**
     * Given is not frozen
     * And check children
     * And children frozen
     * When evaluate
     * Then true
     */
    @Test
    public void notFrozenCheckChildrenFrozen()
    {
        // is not a hold
        NodeRef nodeRef = generateNodeRef();
        when(mockedHoldService.isHold(nodeRef))
            .thenReturn(false);
     
        // check children
        condition.setCheckChildren(true);
     
        // is not frozen
        when(mockedFreezeService.isFrozen(nodeRef))
            .thenReturn(false);
     
        // children frozen
        when(mockedFreezeService.hasFrozenChildren(nodeRef))
            .thenReturn(true);
     
        // evaluate
        assertTrue(condition.evaluate(nodeRef));
     
        // verify
        verify(mockedHoldService, times(1)).isHold(nodeRef);
        verify(mockedFreezeService, times(1)).isFrozen(nodeRef);
        verify(mockedFreezeService, times(1)).hasFrozenChildren(nodeRef);
    }
}
