package org.alfresco.repo.action;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.rule.RuleServiceException;


/**
 * @author Roy Wetherall
 */
public class ActionConditionDefinitionImplTest extends BaseParameterizedItemDefinitionImplTest
{
    /**
     * Constants used during tests
     */
    private static final String CONDITION_EVALUATOR = "conditionEvaluator";

    protected ParameterizedItemDefinitionImpl create()
    {    
        // Test duplicate param name
        Map<Locale, List<ParameterDefinition>> localizedParams = new HashMap<Locale, List<ParameterDefinition>>();
        try
        {
            ActionConditionDefinitionImpl temp = new ActionConditionDefinitionImpl(NAME);
            localizedParams.put(Locale.ROOT, duplicateParamDefs);
            temp.setLocalizedParameterDefinitions((localizedParams));
            fail("Duplicate param names are not allowed.");
        }
        catch (RuleServiceException exception)
        {
            // Indicates that there are duplicate param names
        }
        
        // Create a good one
        ActionConditionDefinitionImpl temp = new ActionConditionDefinitionImpl(NAME);
        assertNotNull(temp);
        //temp.setTitle(TITLE);
        //temp.setDescription(DESCRIPTION);
        localizedParams.put(Locale.ROOT, paramDefs);
        temp.setLocalizedParameterDefinitions(localizedParams);
        temp.setConditionEvaluator(CONDITION_EVALUATOR);
        return temp;
    }
    
    /**
     * Test getConditionEvaluator
     */
    public void testGetConditionEvaluator()
    {
        ActionConditionDefinitionImpl cond = (ActionConditionDefinitionImpl)create();
        assertEquals(CONDITION_EVALUATOR, cond.getConditionEvaluator());
    }
}
