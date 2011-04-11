/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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

package org.alfresco.repo.action.constraint;

import java.util.Map;

import org.alfresco.repo.action.RuntimeActionService;
import org.alfresco.service.cmr.action.ParameterConstraint;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Base implementation of a parameter constraint
 * 
 * @author Roy Wetherall
 */
public abstract class BaseParameterConstraint implements ParameterConstraint,
                                                               BeanNameAware
{   
    /** Constraint name */
    protected String name;

    /** Runtime action service */
    protected RuntimeActionService actionService;
    
    /** Flag to determine whether the allowable values should be cached */
    protected boolean cache = true;
    
    /** Map of allowable values */
    protected Map<String, String> allowableValues;
    
    /**
     * Init method
     */
    public void init()
    {
        actionService.registerParameterConstraint(this);
    }
    
    /**
     * Set the action service
     * 
     * @param actionService     action service
     */
    public void setActionService(RuntimeActionService actionService)
    {
        this.actionService = actionService;
    }
    
    /**
     * Determines whether the allowable values should be cached, default is true.
     * 
     * @param cache
     */
    public void setCacheAllowableValues(boolean cache)
    {
        this.cache = cache;
    }
    
    /**
     * @see org.alfresco.service.cmr.action.ParameterConstraint#getName()
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name)
    {
        this.name = name;
    }    
    
    /**
     * @see org.alfresco.service.cmr.action.ParameterConstraint#getAllowableValues()
     */
    public Map<String, String> getAllowableValues()
    {
        if (this.cache)
        {
            if (this.allowableValues == null)
            {            
                this.allowableValues = getAllowableValuesImpl();
            }
            
            return this.allowableValues;
        }
        else
        {
            return getAllowableValuesImpl();
        }
    }
    
    /**
     * Gets the list of allowable values, calculating them every time it is called.
     * 
     * @return Map<String, String> map of allowable values
     */
    protected abstract Map<String, String> getAllowableValuesImpl();
    
    /**
     * Get the I18N display label for a particular key
     * 
     * @param key 
     * @return String I18N value
     */
    protected String getI18NLabel(String key)
    {
        String result = key.toString();
        StringBuffer longKey = new StringBuffer(name).
                                    append(".").
                                    append(key.toString().toLowerCase());
        String i18n = I18NUtil.getMessage(longKey.toString());
        if (i18n != null)
        {
            result = i18n;
        }
        return result;
    }
    
    /**
     * @see org.alfresco.service.cmr.action.ParameterConstraint#getValueDisplayLabel(java.io.Serializable)
     */
    public String getValueDisplayLabel(String value)
    {
        return getAllowableValues().get(value);        
    }

    /**
     * @see org.alfresco.service.cmr.action.ParameterConstraint#isValidValue(java.io.Serializable)
     */
    public boolean isValidValue(String value)
    {
        return getAllowableValues().containsKey(value);
    }
}
