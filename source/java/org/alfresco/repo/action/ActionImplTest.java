/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.action;

import java.util.List;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.util.GUID;

/**
 * @author Roy Wetherall
 */
public class ActionImplTest extends BaseParameterizedItemImplTest
{
	private static final String ID_COND1 = "idCond1";
	private static final String ID_COND2 = "idCond2";
	private static final String ID_COND3 = "idCond3";
	private static final String NAME_COND1 = "nameCond1";
	private static final String NAME_COND2 = "nameCond2";
	private static final String NAME_COND3 = "nameCond3";

	/**
     * @see org.alfresco.repo.rule.common.RuleItemImplTest#create()
     */
    @Override
    protected ParameterizedItemImpl create()
    {
        return new ActionImpl(
                null,
        		ID,
                NAME, 
                this.paramValues);
    }
    
    public void testGetRuleActionDefintion()
    {
        Action temp = (Action)create();
        assertEquals(NAME, temp.getActionDefinitionName());
    }
    
    public void testSimpleProperties()
    {
    	Action action = (Action)create();
    	
    	// Check the default values
    	assertFalse(action.getExecuteAsychronously());
    	assertNull(action.getCompensatingAction());
    	
    	// Set some values
    	action.setTitle("title");
    	action.setDescription("description");
    	action.setExecuteAsynchronously(true);
    	Action compensatingAction = new ActionImpl(null, GUID.generate(), "actionDefintionName", null);
    	action.setCompensatingAction(compensatingAction);
    	
    	// Check the values have been set
    	assertEquals("title", action.getTitle());
    	assertEquals("description", action.getDescription());
    	assertTrue(action.getExecuteAsychronously());
    	assertEquals(compensatingAction, action.getCompensatingAction());
    }
    
    public void testActionConditions()
    {
    	ActionCondition cond1 = new ActionConditionImpl(ID_COND1, NAME_COND1, this.paramValues);
    	ActionCondition cond2 = new ActionConditionImpl(ID_COND2, NAME_COND2, this.paramValues);
    	ActionCondition cond3 = new ActionConditionImpl(ID_COND3, NAME_COND3, this.paramValues);
    	
    	Action action = (Action)create();
    	
    	// Check has no conditions
    	assertFalse(action.hasActionConditions());
    	List<ActionCondition> noConditions = action.getActionConditions();
    	assertNotNull(noConditions);
    	assertEquals(0, noConditions.size());
    
    	// Add the conditions to the action
    	action.addActionCondition(cond1);
    	action.addActionCondition(cond2);
    	action.addActionCondition(cond3);
    	
    	// Check that the conditions are there and in the correct order
    	assertTrue(action.hasActionConditions());
    	List<ActionCondition> actionConditions = action.getActionConditions();
    	assertNotNull(actionConditions);
    	assertEquals(3, actionConditions.size());
    	int counter = 0;
    	for (ActionCondition condition : actionConditions)
		{
			if (counter == 0)
			{
				assertEquals(cond1, condition);
			}
			else if (counter == 1)
			{
				assertEquals(cond2, condition);
			}
			else if (counter == 2)
			{
				assertEquals(cond3, condition);
			}
			counter+=1;
		}    	
    	assertEquals(cond1, action.getActionCondition(0));
    	assertEquals(cond2, action.getActionCondition(1));
    	assertEquals(cond3, action.getActionCondition(2));
    	
    	// Check remove
    	action.removeActionCondition(cond3);
    	assertEquals(2, action.getActionConditions().size());
    	
    	// Check set
    	action.setActionCondition(1, cond3);
    	assertEquals(cond1, action.getActionCondition(0));
    	assertEquals(cond3, action.getActionCondition(1));
    	
    	// Check index of
    	assertEquals(0, action.indexOfActionCondition(cond1));
    	assertEquals(1, action.indexOfActionCondition(cond3));
    	
    	// Test insert
    	action.addActionCondition(1, cond2);
    	assertEquals(3, action.getActionConditions().size());
    	assertEquals(cond1, action.getActionCondition(0));
    	assertEquals(cond2, action.getActionCondition(1));
    	assertEquals(cond3, action.getActionCondition(2));
    	
    	// Check remote all
    	action.removeAllActionConditions();
    	assertFalse(action.hasActionConditions());
    	assertEquals(0, action.getActionConditions().size());
    }
}
