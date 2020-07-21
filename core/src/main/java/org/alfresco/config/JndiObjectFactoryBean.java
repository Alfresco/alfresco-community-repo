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

import java.sql.Connection;

import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * An extended version of JndiObjectFactoryBean that actually tests a JNDI data source before falling back to its
 * default object. Allows continued backward compatibility with old-style datasource configuration.
 * 
 * @author dward
 */
public class JndiObjectFactoryBean extends org.springframework.jndi.JndiObjectFactoryBean
{

    @Override
    protected Object lookup() throws NamingException
    {
        Object candidate = super.lookup();
        if (candidate instanceof DataSource)
        {
            Connection con = null;
            try
            {
                con = ((DataSource) candidate).getConnection();
            }
            catch (Exception e)
            {
                NamingException e1 = new NamingException("Unable to get connection from " + getJndiName());
                e1.setRootCause(e);
                throw e1;
            }
            finally
            {
                try
                {
                    if (con != null)
                    {
                        con.close();
                    }
                }
                catch (Exception e)
                {
                }
            }
        }
        return candidate;
    }
}
