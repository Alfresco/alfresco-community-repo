package org.alfresco.repo.action;

import org.alfresco.service.cmr.action.ActionCondition;

/**
 * @author Roy Wetherall
 */
public class ActionConditionImplTest extends BaseParameterizedItemImplTest
{
    @Override
    protected ParameterizedItemImpl create()
    {
        return new ActionConditionImpl(
        		ID,
                NAME, 
                this.paramValues);
    }
    
    public void testGetRuleConditionDefintion()
    {
        ActionCondition temp = (ActionCondition)create();
        assertEquals(NAME, temp.getActionConditionDefinitionName());        
    }
    
    public void testSetGetInvertCondition()
    {
        ActionCondition temp = (ActionCondition)create();
        assertFalse(temp.getInvertCondition());
        temp.setInvertCondition(true);
        assertTrue(temp.getInvertCondition());
    }
}
