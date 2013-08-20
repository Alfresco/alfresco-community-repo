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
package org.alfresco.repo.action;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.extensions.surf.util.I18NUtil;
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
	protected static final String DISPLAY_LABEL = "display-label";
	
	/**
	 * Action service
	 */
	protected RuntimeActionService runtimeActionService;
    
	/**
	 * Indicates whether or not ad-hoc properties can be provided. Default so false. 
	 */
	protected boolean adhocPropertiesAllowed = false;
	
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
	 * Setter for Spring injection of adhocPropertiesAllowed property
	 * 
	 * @param allowed
	 */
	public void setAdhocPropertiesAllowed(boolean allowed)
	{
	    this.adhocPropertiesAllowed = allowed;
	}
	
	/**
	 * Indicates whether adhoc property definitions are allowed or not
	 * 
	 * @return	true if they are, by default false
	 */
	protected boolean getAdhocPropertiesAllowed()
	{
		// By default adhoc properties are not allowed
		return this.adhocPropertiesAllowed;
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
