/*
 * Copyright (C) 2009-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
        if (this.allowableValues == null)
        {            
            this.allowableValues = getAllowableValuesImpl();
        }
        
        return this.allowableValues;
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
