/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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

import junit.framework.TestCase;

import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.CompositeActionCondition;
import org.alfresco.util.GUID;

/**
 * @author Gavin Cornwell
 * @since 3.1
 */
public class CompositeActionConditionImplTest extends TestCase
{

    @Override
    protected void setUp() throws Exception
    {

    }

    protected CompositeActionCondition create()
    {
        return new CompositeActionConditionImpl(GUID.generate());
    }

    public void testGetRuleConditionDefintion()
    {
        ActionCondition temp = (ActionCondition) create();
        assertEquals(CompositeActionCondition.COMPOSITE_CONDITION, temp.getActionConditionDefinitionName());
    }

    public void testAddActionCondition()
    {
        CompositeActionCondition temp = (CompositeActionCondition) create();
        assertEquals(temp.getActionConditions().size(), 0);
        temp.addActionCondition(new ActionConditionImpl("id", "condName", null));
        assertEquals(temp.getActionConditions().size(), 1);
    }

    public void testHasActionConditions()
    {
        CompositeActionCondition temp = (CompositeActionCondition) create();
        assertEquals(temp.hasActionConditions(), false);
        temp.addActionCondition(new ActionConditionImpl("id", "condName", null));
        assertEquals(temp.hasActionConditions(), true);
    }

    public void testRemoveAllActionConditions()
    {
        CompositeActionCondition temp = (CompositeActionCondition) create();
        assertEquals(temp.hasActionConditions(), false);
        temp.addActionCondition(new ActionConditionImpl("id", "condName", null));
        assertEquals(temp.hasActionConditions(), true);
        temp.removeAllActionConditions();
        assertEquals(temp.hasActionConditions(), false);
    }

    public void testSetORCondition()
    {
        CompositeActionCondition temp = (CompositeActionCondition) create();
        assertEquals(temp.isORCondition(), false);
        temp.setORCondition(true);
        assertEquals(temp.isORCondition(), true);

    }

    public void testSetGetInvertCondition()
    {
        ActionCondition temp = (ActionCondition) create();
        assertFalse(temp.getInvertCondition());
        temp.setInvertCondition(true);
        assertTrue(temp.getInvertCondition());
    }

}
