/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.action;

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

    /**
     * @see org.alfresco.repo.rule.common.RuleItemDefinitionImplTest#create()
     */
    protected ParameterizedItemDefinitionImpl create()
    {    
        // Test duplicate param name
        try
        {
            ActionConditionDefinitionImpl temp = new ActionConditionDefinitionImpl(NAME);
            temp.setParameterDefinitions(this.duplicateParamDefs);
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
        temp.setParameterDefinitions(this.paramDefs);
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
