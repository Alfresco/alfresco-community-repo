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
