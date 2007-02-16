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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.action;

import java.util.List;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.CompositeAction;

/**
 * Composite action test
 * 
 * @author Roy Wetherall
 */
public class CompositeActionImplTest extends ActionImplTest
{
	private static final String ACTION1_ID = "action1Id";
	private static final String ACTION2_ID = "action2Id";
	private static final String ACTION3_ID = "action3Id";
	private static final String ACTION1_NAME = "actionName1";
	private static final String ACTION2_NAME = "actionName1";
	private static final String ACTION3_NAME = "actionName3";

	public void testActions()
    {
    	Action action1 = new ActionImpl(null, ACTION1_ID, ACTION1_NAME, null);
    	Action action2 = new ActionImpl(null, ACTION2_ID, ACTION2_NAME, null);
    	Action action3 = new ActionImpl(null, ACTION3_ID, ACTION3_NAME, null);
		
		CompositeAction compositeAction = new CompositeActionImpl(null, ID);
    	
    	// Check has no action
    	assertFalse(compositeAction.hasActions());
    	List<Action> noActions = compositeAction.getActions();
    	assertNotNull(noActions);
    	assertEquals(0, noActions.size());
    
    	// Add actions
    	compositeAction.addAction(action1);
    	compositeAction.addAction(action2);
    	compositeAction.addAction(action3);
    	
    	// Check that the actions that are there and in the correct order
    	assertTrue(compositeAction.hasActions());
    	List<Action> actions = compositeAction.getActions();
    	assertNotNull(actions);
    	assertEquals(3, actions.size());
    	int counter = 0;
    	for (Action action : actions)
		{
			if (counter == 0)
			{
				assertEquals(action1, action);
			}
			else if (counter == 1)
			{
				assertEquals(action2, action);
			}
			else if (counter == 2)
			{
				assertEquals(action3, action);
			}
			counter+=1;
		}    	
    	assertEquals(action1, compositeAction.getAction(0));
    	assertEquals(action2, compositeAction.getAction(1));
    	assertEquals(action3, compositeAction.getAction(2));
    	
    	// Check remove
    	compositeAction.removeAction(action3);
    	assertEquals(2, compositeAction.getActions().size());
    	
    	// Check set
    	compositeAction.setAction(1, action3);
    	assertEquals(action1, compositeAction.getAction(0));
    	assertEquals(action3, compositeAction.getAction(1));
    	
    	// Check index of
    	assertEquals(0, compositeAction.indexOfAction(action1));
    	assertEquals(1, compositeAction.indexOfAction(action3));
    	
    	// Test insert
    	compositeAction.addAction(1, action2);
    	assertEquals(3, compositeAction.getActions().size());
    	assertEquals(action1, compositeAction.getAction(0));
    	assertEquals(action2, compositeAction.getAction(1));
    	assertEquals(action3, compositeAction.getAction(2));
    	
    	// Check remote all
    	compositeAction.removeAllActions();
    	assertFalse(compositeAction.hasActions());
    	assertEquals(0, compositeAction.getActions().size());
    }
}
