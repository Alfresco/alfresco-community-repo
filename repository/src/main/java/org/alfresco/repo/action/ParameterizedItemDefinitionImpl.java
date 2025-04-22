/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.action;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.extensions.surf.util.I18NUtil;

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
    private static final long serialVersionUID = -114215660281683946L;

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
    private Map<Locale, List<ParameterDefinition>> parameterDefinitions = new HashMap<Locale, List<ParameterDefinition>>();

    /**
     * A map of the parameter definitions by name
     */
    private Map<Locale, Map<String, ParameterDefinition>> paramDefinitionsByName;

    /**
     * Error messages
     */
    private static final String ERR_NAME_DUPLICATION = "The names " +
            "given to parameter definitions must be unique within the " +
            "scope of the rule item definition.";

    /**
     * Constructor
     * 
     * @param name
     *            the name
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
     * @param title
     *            the title
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
     * @param descriptionKey
     *            the description key
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
     * @param adhocPropertiesAllowed
     *            true is adhoc properties are allowed, false otherwise
     */
    public void setAdhocPropertiesAllowed(boolean adhocPropertiesAllowed)
    {
        this.adhocPropertiesAllowed = adhocPropertiesAllowed;
    }

    /**
     * Set the parameter definitions for the rule item with the locale specified
     *
     * @param parameterDefinitions
     *            the parameter definitions
     */
    public void setLocalizedParameterDefinitions(Map<Locale, List<ParameterDefinition>> parameterDefinitions)
    {
        if (hasDuplicateNames(parameterDefinitions) == true)
        {
            throw new RuleServiceException(ERR_NAME_DUPLICATION);
        }
        this.parameterDefinitions = parameterDefinitions;
        createParamDefinitionsByName();
    }

    /**
     * Set the parameter definitions for the rule item
     * 
     * @param parameterDefinitions
     *            the parameter definitions
     */
    public void setParameterDefinitions(List<ParameterDefinition> parameterDefinitions)
    {
        Locale currentLocale = I18NUtil.getLocale();
        new HashMap<Locale, Map<String, ParameterDefinition>>();
        if (hasDuplicateNames(parameterDefinitions) == true)
        {
            throw new RuleServiceException(ERR_NAME_DUPLICATION);
        }
        this.parameterDefinitions.put(currentLocale, parameterDefinitions);
        createParamDefinitionsByName();
    }

    /**
     * Create a map of the definitions to use for subsequent calls
     */
    private void createParamDefinitionsByName()
    {
        this.paramDefinitionsByName = new HashMap<Locale, Map<String, ParameterDefinition>>();
        for (Locale locale : this.parameterDefinitions.keySet())
        {
            Map<String, ParameterDefinition> namedDefinitions = new HashMap<String, ParameterDefinition>();
            this.paramDefinitionsByName.put(locale, namedDefinitions);

            List<ParameterDefinition> localizedDefinitions = this.parameterDefinitions.get(locale);

            if (localizedDefinitions != null && localizedDefinitions.size() > 0)
            {
                for (ParameterDefinition definition : localizedDefinitions)
                {
                    namedDefinitions.put(definition.getName(), definition);
                }
            }
        }
    }

    /**
     * Determines whether the list of parameter definitions contains duplicate names of not.
     *
     * @param parameterDefinitions
     *            a list of parameter definitions
     * @return true if there are name duplications, false otherwise
     */
    private boolean hasDuplicateNames(Map<Locale, List<ParameterDefinition>> parameterDefinitions)
    {
        boolean result = false;
        if (parameterDefinitions != null)
        {
            for (List<ParameterDefinition> localizedDefinitions : parameterDefinitions.values())
            {
                result = hasDuplicateNames(localizedDefinitions);
                if (result)
                {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Determines whether the list of parameter defintions contains duplicate names of not.
     * 
     * @param parameterDefinitions
     *            a list of parmeter definitions
     * @return true if there are name duplications, false otherwise
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
        List<ParameterDefinition> localizedDefinitions = getParameterDefinitions();

        return (null != localizedDefinitions) && !localizedDefinitions.isEmpty();
    }

    /**
     * @see org.alfresco.service.cmr.action.ParameterizedItemDefinition#getParameterDefinitions()
     */
    public List<ParameterDefinition> getParameterDefinitions()
    {
        Set<Locale> locales = parameterDefinitions.keySet();
        Locale match = I18NUtil.getNearestLocale(I18NUtil.getLocale(), locales);
        List<ParameterDefinition> result = parameterDefinitions.get(match);

        if (null == result)
        {
            result = parameterDefinitions.get(Locale.ROOT);
        }

        return result;
    }

    /**
     * @see org.alfresco.service.cmr.action.ParameterizedItemDefinition#getParameterDefintion(java.lang.String)
     */
    public ParameterDefinition getParameterDefintion(String name)
    {
        ParameterDefinition result = null;

        if (null != paramDefinitionsByName)
        {
            Set<Locale> locales = paramDefinitionsByName.keySet();
            Locale match = I18NUtil.getNearestLocale(I18NUtil.getLocale(), locales);
            Map<String, ParameterDefinition> localizedDefinitionsByName = paramDefinitionsByName.get(match);

            if (null == localizedDefinitionsByName)
            {
                localizedDefinitionsByName = paramDefinitionsByName.get(Locale.ROOT);
            }

            result = (null == localizedDefinitionsByName) ? (null) : (localizedDefinitionsByName.get(name));
        }

        return result;
    }
}
