/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.domain.schema;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean to log connection details and attempt to ensure that the connection is OK.
 * 
 * @author Derek Hulley
 * @since 4.1.5
 */
public class DataSourceCheck
{
    private static Log logger = LogFactory.getLog("org.alfresco.repo.admin");
    
    private static final String MSG_DB_CONNECTION = "Using database URL '%s' with user '%s'.";
    private static final String MSG_DB_VERSION = "Connected to database %s version %s";
    private static final String ERR_DB_CONNECTION = "Database connection failed: %s";
    
    /**
     * Constructor-based check of DB connection
     */
    public DataSourceCheck(String dbUrl, String dbUsername, DataSource dataSource)
    {
        logger.info(String.format(MSG_DB_CONNECTION, dbUrl, dbUsername));
        
        Connection con = null;
        try
        {
            con = dataSource.getConnection();
            DatabaseMetaData meta = con.getMetaData();
            logger.info(String.format(MSG_DB_VERSION, meta.getDatabaseProductName(), meta.getDatabaseProductVersion()));
        }
        catch (Exception e)
        {
            throw new RuntimeException(String.format(ERR_DB_CONNECTION, e.getMessage()), e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
}
