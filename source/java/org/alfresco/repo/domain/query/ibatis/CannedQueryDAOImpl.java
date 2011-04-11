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
package org.alfresco.repo.domain.query.ibatis;

import java.sql.Savepoint;

import org.alfresco.repo.domain.query.AbstractCannedQueryDAOImpl;
import org.alfresco.repo.domain.query.QueryException;
import org.alfresco.util.PropertyCheck;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

/**
 * DAO implementation providing canned query support.
 * 
 * @author Derek Hulley
 * @since 3.5
 */
public class CannedQueryDAOImpl extends AbstractCannedQueryDAOImpl
{
    private SqlMapClientTemplate template;

    public void setSqlMapClientTemplate(SqlMapClientTemplate sqlMapClientTemplate)
    {
        this.template = sqlMapClientTemplate;
    }
    
    @Override
    public void init()
    {
        super.init();
        PropertyCheck.mandatory(this, "template", template);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Only one return value is allowed and is checked to prevent <tt>null</tt> returns.
     */
    @Override
    public Long executeCountQuery(String sqlNamespace, String queryName, Object parameterObj)
    {
        String query = new StringBuilder(sqlNamespace.length() + queryName.length() + 1)
            .append(sqlNamespace).append(".").append(queryName).toString();
        
        try
        {
            Long result = (Long) template.queryForObject(query, parameterObj);
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
}
