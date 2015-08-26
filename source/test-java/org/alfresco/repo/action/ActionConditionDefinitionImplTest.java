/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
