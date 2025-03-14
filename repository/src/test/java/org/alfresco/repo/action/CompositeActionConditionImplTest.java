/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
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
