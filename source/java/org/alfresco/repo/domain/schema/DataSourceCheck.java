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
package org.alfresco.repo.domain.schema;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import javax.sql.DataSource;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.hibernate.DialectFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.SQLServerDialect;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

/**
 * Bean to log connection details and attempt to ensure that the connection is OK.
 * 
 * @author Derek Hulley
 * @since 4.1.5
 */
public class DataSourceCheck
{
    private static Log logger = LogFactory.getLog("org.alfresco.repo.admin");
    
    private static final String MSG_DB_CONNECTION = "system.config_check.info.db_connection";
    private static final String MSG_DB_VERSION = "system.config_check.info.db_version";
    private static final String ERR_DB_CONNECTION = "system.config_check.err.db_connection";
    private static final String ERR_WRONG_TRANSACTION_ISOLATION_SQL_SERVER =
            "system.config_check.err.wrong_transaction_isolation_sql_server";
    /** The required transaction isolation */
    private static final int SQL_SERVER_TRANSACTION_ISOLATION = 4096;

    private Configuration cfg;
    private String dbUrl;
    private String dbUsername;
    private int transactionIsolation;
    private DataSource dataSource;

    public void setLocalSessionFactory(LocalSessionFactoryBean localSessionFactory)
    {
        this.cfg = localSessionFactory.getConfiguration();
    }

    public void setDbUrl(String dbUrl)
    {
        this.dbUrl = dbUrl;
    }

    public void setDbUsername(String dbUsername)
    {
        this.dbUsername = dbUsername;
    }

    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public void setTransactionIsolation(int transactionIsolation)
    {
        this.transactionIsolation = transactionIsolation;
    }

    public void init()
    {
        logger.info(I18NUtil.getMessage(MSG_DB_CONNECTION, dbUrl, dbUsername));
        
        Connection con = null;
        try
        {
            con = dataSource.getConnection();
            con.setAutoCommit(true);
            DatabaseMetaData meta = con.getMetaData();
            logger.info(I18NUtil.getMessage(
                    MSG_DB_VERSION,
                    meta.getDatabaseProductName(), meta.getDatabaseProductVersion()));

            Dialect dialect = DialectFactory.buildDialect(
                    cfg.getProperties(),
                    meta.getDatabaseProductName(),
                    meta.getDatabaseMajorVersion());

            // Check MS SQL Server specific settings
            if (dialect instanceof SQLServerDialect)
            {
                if (transactionIsolation != SQL_SERVER_TRANSACTION_ISOLATION)
                {
                    throw new AlfrescoRuntimeException(
                            ERR_WRONG_TRANSACTION_ISOLATION_SQL_SERVER,
                            new Object[] {transactionIsolation, SQL_SERVER_TRANSACTION_ISOLATION});
                }
            }
        }
        catch (RuntimeException re)
        {
            // just rethrow
            throw re;
        }
        catch (Exception e)
        {
            throw new AlfrescoRuntimeException(ERR_DB_CONNECTION, new Object[] {e.getMessage()}, e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
}
