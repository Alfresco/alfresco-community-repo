/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * Wrapper around the SqlSession object that allows us to report on the DB executed queries
 */
public class SqlSessionMetricsWrapper implements SqlSession
{
    private Log logger = LogFactory.getLog(getClass());

    private static final String SELECT_LABEL = "select";
    private static final String INSERT_LABEL = "insert";
    private static final String UPDATE_LABEL = "update";
    private static final String DELETE_LABEL = "delete";

    private final DBMetricsReporter dbMetricsReporter;
    private final SqlSession sqlSession;

    public SqlSessionMetricsWrapper(SqlSession sqlSession, DBMetricsReporter dbMetricsReporter)
    {
        this.sqlSession = sqlSession;
        this.dbMetricsReporter = dbMetricsReporter;
        if (logger.isDebugEnabled())
        {
            logger.debug("Created a new SqlSessionMetricsWrapper with the DBMetricsReporter instance: " + dbMetricsReporter);
        }
    }

    private void reportQueryExecuted(final long startTime, final String queryTypeTag, final String statementID)
    {
        try
        {
            if (dbMetricsReporter != null && dbMetricsReporter.isQueryMetricsEnabled())
            {
                final long delta = System.currentTimeMillis() - startTime;
                dbMetricsReporter.reportQueryExecutionTime(delta, queryTypeTag, statementID);
            }
        }
        catch (Exception e)
        {
            logCouldNotReportDBQueryExecution(e);
        }
    }

    @Override
    public <T> T selectOne(String statement)
    {
        long startTime = System.currentTimeMillis();
        try
        {
            return this.sqlSession.selectOne(statement);
        }
        finally
        {
            reportQueryExecuted(startTime, SELECT_LABEL, statement);
        }
    }

    @Override
    public <T> T selectOne(String statement, Object parameter)
    {
        long startTime = System.currentTimeMillis();
        try
        {
            return this.sqlSession.selectOne(statement, parameter);
        }
        finally
        {
            reportQueryExecuted(startTime, SELECT_LABEL, statement);
        }
    }

    @Override
    public <E> List<E> selectList(String statement)
    {
        long startTime = System.currentTimeMillis();
        try
        {
            return this.sqlSession.selectList(statement);
        }
        finally
        {
            reportQueryExecuted(startTime, SELECT_LABEL, statement);
        }
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter)
    {
        long startTime = System.currentTimeMillis();
        try
        {
            return this.sqlSession.selectList(statement, parameter);
        }
        finally
        {
            reportQueryExecuted(startTime, SELECT_LABEL, statement);
        }
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds)
    {
        long startTime = System.currentTimeMillis();
        try
        {
            return this.sqlSession.selectList(statement, parameter, rowBounds);
        }
        finally
        {
            reportQueryExecuted(startTime, SELECT_LABEL, statement);
        }
    }

    @Override
    public <K, V> Map<K, V> selectMap(String statement, String mapKey)
    {
        long startTime = System.currentTimeMillis();
        try
        {
            return this.sqlSession.selectMap(statement, mapKey);
        }
        finally
        {
            reportQueryExecuted(startTime, SELECT_LABEL, statement);
        }
    }

    @Override
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey)
    {
        long startTime = System.currentTimeMillis();
        try
        {
            return this.sqlSession.selectMap(statement, parameter, mapKey);
        }
        finally
        {
            reportQueryExecuted(startTime, SELECT_LABEL, statement);
        }
    }

    @Override
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds)
    {
        long startTime = System.currentTimeMillis();
        try
        {
            return this.sqlSession.selectMap(statement, parameter, mapKey, rowBounds);
        }
        finally
        {
            reportQueryExecuted(startTime, SELECT_LABEL, statement);
        }
    }

    @Override public <T> Cursor<T> selectCursor(String statement)
    {
        long startTime = System.currentTimeMillis();
        try
        {
            return this.sqlSession.selectCursor(statement);
        }
        finally
        {
            reportQueryExecuted(startTime, SELECT_LABEL, statement);
        }
    }

    @Override public <T> Cursor<T> selectCursor(String statement, Object paremeter)
    {
        long startTime = System.currentTimeMillis();
        try
        {
            return this.sqlSession.selectCursor(statement, paremeter);
        }
        finally
        {
            reportQueryExecuted(startTime, SELECT_LABEL, statement);
        }
    }

    @Override public <T> Cursor<T> selectCursor(String statement, Object paramter, RowBounds rowBounds)
    {
        long startTime = System.currentTimeMillis();
        try
        {
            return this.sqlSession.selectCursor(statement, paramter, rowBounds);
        }
        finally
        {
            reportQueryExecuted(startTime, SELECT_LABEL, statement);
        }
    }

    @Override
    public void select(String statement, Object parameter, ResultHandler handler)
    {
        final long startTime = System.currentTimeMillis();
        PassThroughMetricsResultsHandler passThroughHandler = new PassThroughMetricsResultsHandler(handler, startTime, statement);
        try
        {
            this.sqlSession.select(statement, parameter, passThroughHandler);
        }
        finally
        {
            if (!passThroughHandler.hasQueryExecutionTimeBeenReported())
            {
                reportQueryExecuted(startTime, SELECT_LABEL, statement);
            }
        }
    }

    @Override
    public void select(String statement, ResultHandler handler)
    {
        long startTime = System.currentTimeMillis();
        PassThroughMetricsResultsHandler passThroughHandler = new PassThroughMetricsResultsHandler(handler, startTime, statement);
        try
        {
            this.sqlSession.select(statement, passThroughHandler);
        }
        finally
        {
            if (!passThroughHandler.hasQueryExecutionTimeBeenReported())
            {
                reportQueryExecuted(startTime, SELECT_LABEL, statement);
            }
        }
    }

    @Override
    public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler)
    {
        long startTime = System.currentTimeMillis();
        PassThroughMetricsResultsHandler passThroughHandler = new PassThroughMetricsResultsHandler(handler, startTime, statement);
        try
        {
            this.sqlSession.select(statement, parameter, rowBounds, passThroughHandler);
        }
        finally
        {
            if (!passThroughHandler.hasQueryExecutionTimeBeenReported())
            {
                reportQueryExecuted(startTime, SELECT_LABEL, statement);
            }
        }
    }

    @Override
    public <T> Cursor<T> selectCursor(String statement)
    {
        long startTime = System.currentTimeMillis();
        try
        {
            return this.sqlSession.selectCursor(statement);
        }
        finally
        {
            reportQueryExecuted(startTime, SELECT_LABEL, statement);
        }
    }

    @Override
    public <T> Cursor<T> selectCursor(String statement, Object parameter)
    {
        long startTime = System.currentTimeMillis();
        try
        {
            return this.sqlSession.selectCursor(statement, parameter);
        }
        finally
        {
            reportQueryExecuted(startTime, SELECT_LABEL, statement);
        }
    }

    @Override
    public <T> Cursor<T> selectCursor(String statement, Object parameter, RowBounds rowBounds)
    {
        long startTime = System.currentTimeMillis();
        try
        {
            return this.sqlSession.selectCursor(statement, parameter, rowBounds);
        }
        finally
        {
            reportQueryExecuted(startTime, SELECT_LABEL, statement);
        }
    }

    @Override
    public int insert(String statement)
    {
        long startTime = System.currentTimeMillis();
        try
        {
            return this.sqlSession.insert(statement);
        }
        finally
        {
            reportQueryExecuted(startTime, INSERT_LABEL, statement);
        }
    }

    @Override
    public int insert(String statement, Object parameter)
    {
        long startTime = System.currentTimeMillis();
        try
        {
            return this.sqlSession.insert(statement, parameter);
        }
        finally
        {
            reportQueryExecuted(startTime, INSERT_LABEL, statement);
        }
    }

    @Override
    public int update(String statement)
    {
        long startTime = System.currentTimeMillis();
        try
        {
            return this.sqlSession.update(statement);
        }
        finally
        {
            reportQueryExecuted(startTime, UPDATE_LABEL, statement);
        }
    }

    @Override
    public int update(String statement, Object parameter)
    {
        long startTime = System.currentTimeMillis();
        try
        {
            return this.sqlSession.update(statement, parameter);
        }
        finally
        {
            reportQueryExecuted(startTime, UPDATE_LABEL, statement);
        }
    }

    @Override
    public int delete(String statement)
    {
        long startTime = System.currentTimeMillis();
        try
        {
            return this.sqlSession.delete(statement);
        }
        finally
        {
            reportQueryExecuted(startTime, DELETE_LABEL, statement);
        }
    }

    @Override
    public int delete(String statement, Object parameter)
    {
        long startTime = System.currentTimeMillis();
        try
        {
            return this.sqlSession.delete(statement, parameter);
        }
        finally
        {
            reportQueryExecuted(startTime, DELETE_LABEL, statement);
        }
    }

    @Override
    public void commit()
    {
        this.sqlSession.commit();
    }

    @Override
    public void commit(boolean force)
    {
        this.sqlSession.commit(force);
    }

    @Override
    public void rollback()
    {
        this.rollback();
    }

    @Override
    public void rollback(boolean force)
    {
        this.rollback(force);
    }

    @Override
    public List<BatchResult> flushStatements()
    {
        return this.sqlSession.flushStatements();
    }

    @Override
    public void close()
    {
        this.sqlSession.close();
    }

    @Override
    public void clearCache()
    {
        this.sqlSession.clearCache();
    }

    @Override
    public Configuration getConfiguration()
    {
        return this.sqlSession.getConfiguration();
    }

    @Override
    public <T> T getMapper(Class<T> type)
    {
        return this.sqlSession.getMapper(type);
    }

    @Override
    public Connection getConnection()
    {
        return this.sqlSession.getConnection();
    }

    private void logCouldNotReportDBQueryExecution(Exception e)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Could not report DB query execution. Message:" + e.getMessage(), e);
        }
    }

    /**
     * Reports on the time it actually took to execute the query.
     * The execution time is the interval until the first call to "handleResult" method
     * If there are no results returned by the query, then "handleResult" method is not called
     * so we need to mark this and report the time outside this utility pass through class
     */
    class PassThroughMetricsResultsHandler implements ResultHandler
    {
        private final long startTime;
        private boolean firstTime = true;

        private final String statementID;
        private final ResultHandler handler;

        PassThroughMetricsResultsHandler(final ResultHandler handler, final String statementID)
        {
            this.handler = handler;
            this.statementID = statementID;
            this.startTime = System.currentTimeMillis();
        }

        PassThroughMetricsResultsHandler(final ResultHandler handler, long startTime, final String statementID)
        {
            this.handler = handler;
            this.statementID = statementID;
            this.startTime = startTime;
        }

        // this is called for each row returned by the query
        @Override
        public void handleResult(ResultContext resultContext)
        {
            // we may never get here if the query does not return results
            if (firstTime)
            {
                // report the time only when the first row is returned form the DB
                // this report method should never throw exceptions
                reportQueryExecuted(startTime, SELECT_LABEL, statementID);
                firstTime = false;
            }
            // In the future we may be interested in summing up all the time the handler took, and report it as a metric
            handler.handleResult(resultContext);
        }

        public boolean hasQueryExecutionTimeBeenReported()
        {
            return !firstTime;
        }
    }
}
