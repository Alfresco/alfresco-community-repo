/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.domain.query.ibatis;

import java.util.List;

import org.alfresco.repo.domain.query.AbstractCannedQueryDAOImpl;
import org.alfresco.repo.domain.query.QueryException;
import org.alfresco.util.PropertyCheck;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * DAO implementation providing canned query support.
 * 
 * @author Derek Hulley
 * @since 3.5
 */
public class CannedQueryDAOImpl extends AbstractCannedQueryDAOImpl
{
    private SqlSessionTemplate template;
    
    public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) 
    {
        this.template = sqlSessionTemplate;
    }
    
    @Override
    public void init()
    {
        super.init();
        PropertyCheck.mandatory(this, "template", template);
    }
    
    /**
     * @return                      the compound query name
     */
    private final String makeQueryName(final String sqlNamespace, final String queryName)
    {
        return new StringBuilder(sqlNamespace.length() + queryName.length() + 1)
            .append(sqlNamespace).append(".").append(queryName).toString();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Only one return value is allowed and is checked to prevent <tt>null</tt> returns.
     */
    @Override
    public Long executeCountQuery(String sqlNamespace, String queryName, Object parameterObj)
    {
        String query = makeQueryName(sqlNamespace, queryName);
        try
        {
            Long result = (Long) template.selectOne(query, parameterObj);
            if (result == null)
            {
                result = 0L;
            }

            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Executed query: \n" +
                        "   Query:  " + query + "\n" +
                        "   Params: " + parameterObj + "\n" +
                        "   Result: " + result);
            }
            return result;
        }
        catch (ClassCastException e)
        {
            throw new QueryException(
                    "Count query results must return exactly one Long value: \n" +
                    "   Query:  " + query + "\n" +
                    "   Params: " + parameterObj,
                    e);
        }
        catch (Throwable e)
        {
            throw new QueryException(
                    "Failed to execute query: \n" +
                    "   Query:  " + query + "\n" +
                    "   Params: " + parameterObj,
                    e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R executeQueryUnique(String sqlNamespace, String queryName, Object parameterObj)
    {
        String query = makeQueryName(sqlNamespace, queryName);
        Object obj = template.selectOne(query, parameterObj);
        try
        {
            return (R) obj;
        }
        catch (ClassCastException e)
        {
            throw new IllegalArgumentException("Return type of query does not match expected type.", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> List<R> executeQuery(
            String sqlNamespace, String queryName, Object parameterObj,
            int offset, int limit)
    {
        if (offset < 0 || offset == Integer.MAX_VALUE)
        {
            throw new IllegalArgumentException("Query result offset must be zero or greater.");
        }
        
        if (limit <= 0)
        {
            throw new IllegalArgumentException("Query results limit must be greater than zero.");
        }
        
        String query = makeQueryName(sqlNamespace, queryName);
        try
        {
            List<R> result;
            if ((offset == 0) && (limit == Integer.MAX_VALUE))
            {
                result = (List<R>) template.selectList(query, parameterObj);
            }
            else
            {
                RowBounds bounds = new RowBounds(offset, limit);
                result = (List<R>) template.selectList(query, parameterObj, bounds);
            }
            

            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Executed query: \n" +
                        "   Query:  " + query + "\n" +
                        "   Params: " + parameterObj + "\n" +
                        "   Result: " + result);
            }
            return result;
        }
        catch (ClassCastException e)
        {
            throw new IllegalArgumentException("Return type of query does not match expected type.", e);
        }
        catch (Throwable e)
        {
            throw new QueryException(
                    "Failed to execute query: \n" +
                    "   Namespace: " + sqlNamespace + "\n" +
                    "   queryName: " + queryName + "\n" +
                    "   Parameter: " + parameterObj + "\n" +
                    "   Offset:    " + offset + "\n" +
                    "   Limit:     " + limit,
                    e);
        }
    }

    @Override
    public <R> void executeQuery(
            String sqlNamespace, String queryName, Object parameterObj,
            int offset, int limit,
            ResultHandler<R> handler)
    {
        if (offset < 0 || offset == Integer.MAX_VALUE)
        {
            throw new IllegalArgumentException("Query result offset must be zero or greater.");
        }
        
        if (limit <= 0)
        {
            throw new IllegalArgumentException("Query results limit must be greater than zero.");
        }
        
        String query = makeQueryName(sqlNamespace, queryName);
        ResultHandlerTranslator<R> resultHandler = new ResultHandlerTranslator<R>(handler);
        try
        {
        	if ((offset == 0) && (limit == Integer.MAX_VALUE))
            {
                template.select(query, parameterObj, resultHandler);
            }
            else
            {
                RowBounds bounds = new RowBounds(offset, limit);
                template.select(query, parameterObj, bounds, resultHandler);
            }
        }
        catch (ClassCastException e)
        {
            throw new IllegalArgumentException("Return type of query does not match expected type.", e);
        }
        catch (Throwable e)
        {
            throw new QueryException(
                    "Failed to execute query: \n" +
                    "   Namespace: " + sqlNamespace + "\n" +
                    "   queryName: " + queryName + "\n" +
                    "   Parameter: " + parameterObj + "\n" +
                    "   Offset:    " + offset + "\n" +
                    "   Limit:     " + limit,
                    e);
        }
    }
    
    /**
     * Helper class to translate MyBatis <tt>ResultHandler</tt> to Alfresco <tt>ResultHandler</tt>.
     * 
     * @author Derek Hulley
     *
     * @param <R>
     */
    private static class ResultHandlerTranslator<R> implements org.apache.ibatis.session.ResultHandler
    {
        private final ResultHandler<R> target;
        boolean stopped = false;
        private ResultHandlerTranslator(ResultHandler<R> target)
        {
            this.target = target;
        }
        @SuppressWarnings("unchecked")
        @Override
        public void handleResult(ResultContext ctx)
        {
            if (stopped || ctx.isStopped())
            {
                return;             // Fly through results without further callbacks
            }
            boolean more = this.target.handleResult((R)ctx.getResultObject());
            if (!more)
            {
                ctx.stop();
                stopped = true;
            }
        }
    }
}
