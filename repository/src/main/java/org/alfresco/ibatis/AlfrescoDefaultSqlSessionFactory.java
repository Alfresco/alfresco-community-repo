/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.ibatis;

import org.alfresco.metrics.db.DBMetricsReporter;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;

import java.sql.Connection;

public class AlfrescoDefaultSqlSessionFactory extends DefaultSqlSessionFactory
{
    private final DBMetricsReporter dbMetricsReporter;

    public AlfrescoDefaultSqlSessionFactory(Configuration configuration, DBMetricsReporter dbMetricsReporter)
    {
        super(configuration);
        this.dbMetricsReporter = dbMetricsReporter;
    }

    @Override
    public SqlSession openSession()
    {
        SqlSession sqlSession = super.openSession();
        return buildSqlSessionMetricsWrapper(sqlSession);
    }

    @Override
    public SqlSession openSession(boolean autoCommit)
    {
        SqlSession sqlSession = super.openSession(autoCommit);
        return buildSqlSessionMetricsWrapper(sqlSession);
    }

    @Override
    public SqlSession openSession(ExecutorType execType)
    {
        SqlSession sqlSession = super.openSession(execType);
        return buildSqlSessionMetricsWrapper(sqlSession);
    }

    @Override
    public SqlSession openSession(TransactionIsolationLevel level)
    {
        SqlSession sqlSession = super.openSession(level);
        return buildSqlSessionMetricsWrapper(sqlSession);
    }

    @Override
    public SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level)
    {
        SqlSession sqlSession = super.openSession(execType, level);
        return buildSqlSessionMetricsWrapper(sqlSession);
    }

    @Override
    public SqlSession openSession(ExecutorType execType, boolean autoCommit)
    {
        SqlSession sqlSession = super.openSession(execType, autoCommit);
        return buildSqlSessionMetricsWrapper(sqlSession);
    }

    @Override
    public SqlSession openSession(Connection connection)
    {
        SqlSession sqlSession = super.openSession(connection);
        return buildSqlSessionMetricsWrapper(sqlSession);
    }

    @Override
    public SqlSession openSession(ExecutorType execType, Connection connection)
    {
        SqlSession sqlSession = super.openSession(execType, connection);
        return buildSqlSessionMetricsWrapper(sqlSession);
    }

    private SqlSession buildSqlSessionMetricsWrapper(SqlSession sqlSession)
    {
        return new SqlSessionMetricsWrapper(sqlSession, dbMetricsReporter);
    }

}
