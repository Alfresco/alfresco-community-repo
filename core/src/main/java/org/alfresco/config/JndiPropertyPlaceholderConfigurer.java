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

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.jndi.JndiTemplate;

/**
 * An extended {@link PropertyPlaceholderConfigurer} that allows properties to be set through JNDI entries in
 * java:comp/env/properties/*. The precedence given to system properties is still as per the superclass.
 * 
 * @author dward
 */
public class JndiPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer
{
    private JndiTemplate jndiTemplate = new JndiTemplate();

    @Override
    protected String resolvePlaceholder(String placeholder, Properties props)
    {
        String result = null;
        try
        {
            Object value = this.jndiTemplate.lookup("java:comp/env/properties/" + placeholder);
            if (value != null)
            {
                result = value.toString();
            }
        }
        catch (NamingException e)
        {
        }
        // Unfortunately, JBoss 4 wrongly expects every env-entry declared in web.xml to have an env-entry-value (even
        // though these are meant to be decided on deployment!). So we treat the empty string as null.
        return result == null || result.length() == 0 ? super.resolvePlaceholder(placeholder, props) : result;
    }
}
