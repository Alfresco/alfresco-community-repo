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
package org.alfresco.config;

import java.util.Properties;

import javax.naming.NamingException;

import org.springframework.jndi.JndiTemplate;

/**
 * An extended {@link SystemPropertiesFactoryBean} that allows properties to be set through JNDI entries in
 * java:comp/env/properties/*. The precedence given to system properties is still as per the superclass.
 * 
 * @author dward
 */
public class JndiPropertiesFactoryBean extends SystemPropertiesFactoryBean
{
    private JndiTemplate jndiTemplate = new JndiTemplate();

    @Override
    protected void resolveMergedProperty(String propertyName, Properties props)
    {
        try
        {
            Object value = this.jndiTemplate.lookup("java:comp/env/properties/" + propertyName);
            if (value != null)
            {
                String stringValue = value.toString();
                if (stringValue.length() > 0)
                {
                    // Unfortunately, JBoss 4 wrongly expects every env-entry declared in web.xml to have an
                    // env-entry-value (even though these are meant to be decided on deployment!). So we treat the empty
                    // string as null.
                    props.setProperty(propertyName, stringValue);
                }
            }
        }
        catch (NamingException e)
        {
            // Fall back to merged value in props
        }
    }
}
