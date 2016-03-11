package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
