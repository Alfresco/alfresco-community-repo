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
