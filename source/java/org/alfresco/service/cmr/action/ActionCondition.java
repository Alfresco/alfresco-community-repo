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
package org.alfresco.service.cmr.action;


/**
 * Rule condition interface
 * 
 * @author Roy Wetherall
 */
public interface ActionCondition extends ParameterizedItem
{
	/**
	 * Get the action condition definition name
	 * 
	 * @param	the action condition definition name
	 */
    public String getActionConditionDefinitionName();
    
    /**
     * Set whether the condition result should be inverted.
     * <p>
     * This is achieved by applying the NOT logical operator to the
     * result.
     * <p>
     * The default value is false.
     * 
     * @param invertCondition   true indicates that the result of the condition
     *                          is inverted, false otherwise.
     */
    public void setInvertCondition(boolean invertCondition);
    
    /**
     * Indicates whether the condition result should be inverted.
     * <p>
     * This is achieved by applying the NOT logical operator to the result.
     * <p>
     * The default value is false.
     * 
     * @return  true indicates that the result of the condition is inverted, false 
     *          otherwise
     */
    public boolean getInvertCondition();
}
