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
