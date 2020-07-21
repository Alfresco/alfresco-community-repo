/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.repo.dictionary;

import java.util.List;
import java.util.Properties;


/**
 * Property override definition
 * 
 * @author David Caruana
 *
 */
public class M2PropertyOverride
{
    private String name;
    private Boolean isMandatory;
    private Boolean isMandatoryEnforced;
    private String defaultValue;
    private List<M2Constraint> constraints;
    private Properties configProperties = new Properties();
    
    /*package*/ M2PropertyOverride()
    {
    }

    
    public String getName()
    {
        return name;
    }
    
    
    public void setName(String name)
    {
        this.name = name;
    }

    
    public Boolean isMandatory()
    {
        return isMandatory;
    }

    
    public void setMandatory(Boolean isMandatory)
    {
        this.isMandatory = isMandatory;
    }
    
    public Boolean isMandatoryEnforced()
    {
        return isMandatoryEnforced;
    }
    
    public String getDefaultValue()
    {
        if (defaultValue != null && M2Class.PROPERTY_PLACEHOLDER.matcher(defaultValue).matches())
        {
            String key = defaultValue.substring(defaultValue.indexOf("${") + 2, defaultValue.indexOf("}"));
            String value = defaultValue.substring(defaultValue.indexOf("|") + 1);
            
            return configProperties.getProperty(key, value);
        }
        else
        {
            return defaultValue;
        }
    }
    
    
    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public List<M2Constraint> getConstraints()
    {
        return constraints;
    }    
    
    public void setConfigProperties(Properties configProperties)
    {
        this.configProperties = configProperties;
    }
}
