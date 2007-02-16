/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.action.ParameterizedItemDefinition;
import org.alfresco.service.cmr.rule.RuleServiceException;

/**
 * Rule item implementation class
 * 
 * @author Roy Wetherall
 */
public abstract class ParameterizedItemDefinitionImpl implements ParameterizedItemDefinition, Serializable
{
    /**
     * The name of the rule item
     */
    private String name;
    
    /**
     * The title I18N key
     */
    private String titleKey;
    
    /**
     * The description I18N key
     */
    private String descriptionKey;
        
    /**
     * Indicates whether adHocProperties are allowed
     */
    private boolean adhocPropertiesAllowed = false;
    
    /**
     * The list of parameters associated with the rule item
     */
    private List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
    
    /**
     * A map of the parameter definitions by name
     */
    private Map<String, ParameterDefinition> paramDefinitionsByName;

    /**
     * Error messages
     */
    private static final String ERR_NAME_DUPLICATION = "The names " +
            "given to parameter definitions must be unique within the " +
            "scope of the rule item definition.";

    /**
     * Constructor
     * 
     * @param name                  the name 
     */
    public ParameterizedItemDefinitionImpl(String name)
    {
        this.name = name;        
    }

    /**
     * @see org.alfresco.service.cmr.action.ParameterizedItemDefinition#getName()
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Set the title of the rule item
     * 
     * @param title  the title
     */
    public void setTitleKey(String title)
    {
        this.titleKey = title;
    }
    
    /**
     * @see org.alfresco.service.cmr.action.ParameterizedItemDefinition#getTitle()
     */
    public String getTitle()
    {
        return I18NUtil.getMessage(this.titleKey);
    }

    /**
     * Set the description I18N key
     * 
     * @param descriptionKey  the description key
     */
    public void setDescriptionKey(String descriptionKey)
    {
        this.descriptionKey = descriptionKey;
    }
    
    /**
     * @see org.alfresco.service.cmr.action.ParameterizedItemDefinition#getDescription()
     */
    public String getDescription()
    {
        return I18NUtil.getMessage(this.descriptionKey);
    }

    /**
     * @see org.alfresco.service.cmr.action.ParameterizedItemDefinition#getAdhocPropertiesAllowed()
     */
    public boolean getAdhocPropertiesAllowed()
    {
    	return this.adhocPropertiesAllowed;
    }
    
    /**
     * Set whether adhoc properties are allowed
     * 
     * @param adhocPropertiesAllowed	true is adhoc properties are allowed, false otherwise
     */
    public void setAdhocPropertiesAllowed(boolean adhocPropertiesAllowed)
	{
		this.adhocPropertiesAllowed = adhocPropertiesAllowed;
	}
    
    /**
     * Set the parameter definitions for the rule item
     * 
     * @param parameterDefinitions  the parameter definitions
     */
    public void setParameterDefinitions(
            List<ParameterDefinition> parameterDefinitions)
    {
        if (hasDuplicateNames(parameterDefinitions) == true)
        {
            throw new RuleServiceException(ERR_NAME_DUPLICATION);
        }
        
        this.parameterDefinitions = parameterDefinitions;
        
        // Create a map of the definitions to use for subsequent calls
        this.paramDefinitionsByName = new HashMap<String, ParameterDefinition>(this.parameterDefinitions.size());
        for (ParameterDefinition definition : this.parameterDefinitions)
        {
            this.paramDefinitionsByName.put(definition.getName(), definition);
        }
    }
    
    /**
     * Determines whether the list of parameter defintions contains duplicate
     * names of not.
     * 
     * @param parameterDefinitions  a list of parmeter definitions
     * @return                      true if there are name duplications, false
     *                              otherwise
     */
    private boolean hasDuplicateNames(List<ParameterDefinition> parameterDefinitions)
    {
        boolean result = false;
        if (parameterDefinitions != null)
        {
            HashSet<String> temp = new HashSet<String>(parameterDefinitions.size());
            for (ParameterDefinition definition : parameterDefinitions)
            {
                temp.add(definition.getName());
            }
            result = (parameterDefinitions.size() != temp.size());
        }
        return result;
    }
    
    /**
     * @see org.alfresco.service.cmr.action.ParameterizedItemDefinition#hasParameterDefinitions()
     */
    public boolean hasParameterDefinitions()
    {
        return (this.parameterDefinitions.isEmpty() == false);
    }
    
    /**
     * @see org.alfresco.service.cmr.action.ParameterizedItemDefinition#getParameterDefinitions()
     */
    public List<ParameterDefinition> getParameterDefinitions()
    {
        return this.parameterDefinitions;
    }
    
    /**
     * @see org.alfresco.service.cmr.action.ParameterizedItemDefinition#getParameterDefintion(java.lang.String)
     */
    public ParameterDefinition getParameterDefintion(String name)
    {
        ParameterDefinition result = null;
        if (paramDefinitionsByName != null)
        {
            result = this.paramDefinitionsByName.get(name);
        }
        return result;
    }
}
