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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.action.ParameterizedItem;
import org.alfresco.service.cmr.action.ParameterizedItemDefinition;
import org.alfresco.service.cmr.rule.RuleServiceException;

/**
 * Rule item abstract base.
 * <p>
 * Helper base class used by the action exector and condition evaluator implementations.
 * 
 * @author Roy Wetherall
 */
public abstract class ParameterizedItemAbstractBase extends CommonResourceAbstractBase 
{
	/**
	 * Error messages
	 */
	private static final String ERR_MAND_PROP = "A value for the mandatory parameter {0} has not been set on the rule item {1}";
	
	/**
	 * Look-up constants
	 */
	private static final String TITLE = "title";
	private static final String DESCRIPTION = "description";
	private static final String DISPLAY_LABEL = "display-label";
	
	/**
	 * Action service
	 */
	protected RuntimeActionService runtimeActionService;
    
    /**
     * @return Return a short title and description string
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(60);
        sb.append("ParameterizedItem")
          .append("[ title='").append(getTitleKey()).append("'")
          .append(", description='").append(getDescriptionKey()).append("'")
          .append("]");
        return sb.toString();
    }
	
	/**
	 * Gets a list containing the parameter definitions for this rule item.
	 * 
	 * @return  the list of parameter definitions
	 */
	protected List<ParameterDefinition> getParameterDefintions() 
	{
		List<ParameterDefinition> result = new ArrayList<ParameterDefinition>();		
		addParameterDefinitions(result);
		return result;
	}
	
	/**
	 * Adds the parameter definitions to the list
	 * 
	 * @param paramList		the parameter definitions list
	 */
	protected abstract void addParameterDefinitions(List<ParameterDefinition> paramList);

	/**
	 * Sets the action service 
	 * 
	 * @param actionRegistration the action service
	 */
	public void setRuntimeActionService(RuntimeActionService runtimeActionService)
	{
		this.runtimeActionService = runtimeActionService;
	}

	/**
	 * Gets the title I18N key
	 * 
	 * @return	the title key
	 */
	protected String getTitleKey() 
	{
        return this.name + "." + TITLE;
	}

	/**
	 * Gets the description I18N key
	 * 
	 * @return	the description key
	 */
	protected String getDescriptionKey() 
	{
		return this.name + "." + DESCRIPTION;
	}	
	
	/**
	 * Indicates whether adhoc property definitions are allowed or not
	 * 
	 * @return	true if they are, by default false
	 */
	protected boolean getAdhocPropertiesAllowed()
	{
		// By default adhoc properties are not allowed
		return false;
	}

	/**
	 * Gets the parameter definition display label from the properties file.
	 * 
	 * @param paramName  the name of the parameter
	 * @return			 the diaplay label of the parameter
	 */
	protected String getParamDisplayLabel(String paramName) 
	{
		return I18NUtil.getMessage(this.name + "." + paramName + "." + DISPLAY_LABEL);
	}
	
	/**
	 * Checked whether all the mandatory parameters for the rule item have been assigned.
	 * 
	 * @param ruleItem				the rule item
	 * @param ruleItemDefinition	the rule item definition
	 */
	protected void checkMandatoryProperties(ParameterizedItem ruleItem, ParameterizedItemDefinition ruleItemDefinition)
	{
        List<ParameterDefinition> definitions = ruleItemDefinition.getParameterDefinitions();
        for (ParameterDefinition definition : definitions)
        {
            if (definition.isMandatory() == true)
            {
                // Check that a value has been set for the mandatory parameter
                if (ruleItem.getParameterValue(definition.getName()) == null)
                {
                    // Error since a mandatory parameter has a null value
                   throw new RuleServiceException(
                          MessageFormat.format(ERR_MAND_PROP, new Object[]{definition.getName(), ruleItemDefinition.getName()}));
                }
            }
        }
        
	}
}
