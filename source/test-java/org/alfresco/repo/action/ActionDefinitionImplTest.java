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
public class ActionDefinitionImplTest extends BaseParameterizedItemDefinitionImplTest
{
    private static final String RULE_ACTION_EXECUTOR = "ruleActionExector";
    
    protected ParameterizedItemDefinitionImpl create()
    {    
        // Test duplicate param name
        Map<Locale, List<ParameterDefinition>> localizedParams = new HashMap<Locale, List<ParameterDefinition>>();
        try
        {
            ActionDefinitionImpl temp = new ActionDefinitionImpl(NAME);
            localizedParams.put(Locale.ROOT, duplicateParamDefs);
            temp.setLocalizedParameterDefinitions(localizedParams);
            fail("Duplicate param names are not allowed.");
        }
        catch (RuleServiceException exception)
        {
            // Indicates that there are duplicate param names
        }
        
        // Create a good one
        ActionDefinitionImpl temp = new ActionDefinitionImpl(NAME);
        assertNotNull(temp);
        //temp.setTitle(TITLE);
       // temp.setDescription(DESCRIPTION);
        localizedParams.put(Locale.ROOT, paramDefs);
        temp.setLocalizedParameterDefinitions(localizedParams);
        temp.setRuleActionExecutor(RULE_ACTION_EXECUTOR);
        return temp;
    }
    
    /**
     * Test getRuleActionExecutor
     */
    public void testGetRuleActionExecutor()
    {
        ActionDefinitionImpl temp = (ActionDefinitionImpl)create();
        assertEquals(RULE_ACTION_EXECUTOR, temp.getRuleActionExecutor());
    }
}
