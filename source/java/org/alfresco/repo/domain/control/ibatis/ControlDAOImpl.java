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
package org.alfresco.repo.domain.control.ibatis;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

import org.alfresco.repo.domain.control.AbstractControlDAOImpl;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * iBatis-specific, DB-agnostic implementation for connection controlling DAO.
 * 
 * @author Derek Hulley
 * @since 3.2SP1
 */
public class ControlDAOImpl extends AbstractControlDAOImpl
{
    /**
     * The iBatis-specific template for convenient statement execution.
     */
    protected SqlMapClientTemplate template;
    
    public void setSqlMapClientTemplate(SqlMapClientTemplate sqlMapClientTemplate)
    {
        this.template = sqlMapClientTemplate;
    }

    public void startBatch()
    {
        /*
         * The 'transactions' here are just iBatis internal markers and
         * don't have any effect other than to let iBatis know that a batch
         * is possible.
         */
        SqlMapClient sqlMapClient = template.getSqlMapClient();
        try
        {
            sqlMapClient.startTransaction();
            sqlMapClient.startBatch();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Failed to start DAO batch.", e);
        }
    }

    public void executeBatch()
    {
        /*
         * The 'transactions' here are just iBatis internal markers and
         * don't have any effect other than to let iBatis know that a batch
         * is possible.
         */
        SqlMapClient sqlMapClient = template.getSqlMapClient();
        try
        {
            sqlMapClient.executeBatch();
            sqlMapClient.commitTransaction();
            sqlMapClient.endTransaction();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Failed to start DAO batch.", e);
        }
    }
    
    /**
     * PostgreSQL-specific implementation for control DAO.
     * 
     * @author Derek Hulley
     * @since 3.2SP1
     */
    public static class PostgreSQL extends ControlDAOImpl
    {
        /**
         * Calls through to the {@link Connection#setSavepoint(String) current connection}.
         */
        @Override
        public Savepoint createSavepoint(final String savepoint)
        {
            try
            {
                Connection connection = DataSourceUtils.getConnection(template.getDataSource());
                return connection.setSavepoint(savepoint);
            }
            catch (SQLException e)
            {
                throw new RuntimeException("Failed to create SAVEPOINT: " + savepoint, e);
            }
        }
        /**
         * Calls through to the {@link Connection#setSavepoint(String) current connection}.
         */
        @Override
        public void rollbackToSavepoint(Savepoint savepoint)
        {
            try
            {
                Connection connection = DataSourceUtils.getConnection(template.getDataSource());
                connection.rollback(savepoint);
            }
            catch (SQLException e)
            {
                throw new RuntimeException("Failed to create SAVEPOINT: " + savepoint, e);
            }
        }
        @Override
        public void releaseSavepoint(Savepoint savepoint)
        {
            try
            {
                Connection connection = DataSourceUtils.getConnection(template.getDataSource());
                connection.releaseSavepoint(savepoint);
            }
            catch (SQLException e)
            {
                throw new RuntimeException("Failed to create SAVEPOINT: " + savepoint, e);
            }
        }
    }

    @Override
    public int setTransactionIsolationLevel(int isolationLevel)
    {
        Connection connection = DataSourceUtils.getConnection(template.getDataSource());
        if (connection == null)
        {
            throw new NullPointerException("There is no current connection");
        }
        try
        {
            if (!connection.getMetaData().supportsTransactionIsolationLevel(isolationLevel))
            {
                throw new IllegalStateException("Transaction isolation level not supported: " + isolationLevel);
            }
            int isolationLevelWas = connection.getTransactionIsolation();
            connection.setTransactionIsolation(isolationLevel);
            return isolationLevelWas;
        }
        catch (SQLException e)
        {
            throw new IllegalStateException("Failed to set transaction isolation level: " + isolationLevel, e);
        }
    }
}
