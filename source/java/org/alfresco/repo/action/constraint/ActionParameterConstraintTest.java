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
package org.alfresco.repo.action.constraint;

import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterConstraint;
import org.alfresco.util.BaseSpringTest;

/**
 * Action parameter constraint unit test
 * 
 * @author Roy Wetherall
 */
public class ActionParameterConstraintTest extends BaseSpringTest
{
    private static final String COMPARE_OP = "ac-compare-operations";
    
    private ActionService actionService;
    
    /**
     * @see org.springframework.test.AbstractTransactionalSpringContextTests#onSetUpInTransaction()
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
       actionService = (ActionService)applicationContext.getBean("ActionService");
    }
    
    public void testGetConstraints()
    {
        List<ParameterConstraint> constraints =  actionService.getParameterConstraints();
        
        assertNotNull(constraints);
        assertFalse(constraints.isEmpty());
    }
    
    public void testGetConstraint()
    {
        ParameterConstraint constraint = actionService.getParameterConstraint("junk");        
        assertNull(constraint);
        
        constraint = actionService.getParameterConstraint(COMPARE_OP);        
        assertNotNull(constraint);
    }
    
    public void testCompareOperationsConstraint()
    {
        ParameterConstraint constraint = actionService.getParameterConstraint(COMPARE_OP);        
        assertNotNull(constraint);
        assertEquals(COMPARE_OP, constraint.getName());
        
        assertEquals("Ends With", constraint.getValueDisplayLabel(ComparePropertyValueOperation.ENDS.toString()));
        
        Map<String, String> values = constraint.getAllowableValues();
        for (Map.Entry<String, String> entry : values.entrySet())
        {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
    }
    
    public void testConstraints()
    {
        testConstraint("ac-aspects");
        testConstraint("ac-types");
        testConstraint("ac-properties");
        testConstraint("ac-mimetypes");
        testConstraint("ac-email-templates");
        testConstraint("ac-scripts");
        testConstraint("ac-content-properties");
    }
    
    private void testConstraint(String name)
    {
        ParameterConstraint constraint = actionService.getParameterConstraint(name);        
        assertNotNull(constraint);
        assertEquals(name, constraint.getName());
        
        Map<String, String> values = constraint.getAllowableValues();
        assertTrue(values.size()>0);
        System.out.println("== " + name + " ==\n");
        for (Map.Entry<String, String> entry : values.entrySet())
        {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
    }
}
