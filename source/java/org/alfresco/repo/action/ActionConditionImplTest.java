/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.action;

import org.alfresco.service.cmr.action.ActionCondition;

/**
 * @author Roy Wetherall
 */
public class ActionConditionImplTest extends BaseParameterizedItemImplTest
{
    /**
     * @see org.alfresco.repo.rule.common.RuleItemImplTest#create()
     */
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
