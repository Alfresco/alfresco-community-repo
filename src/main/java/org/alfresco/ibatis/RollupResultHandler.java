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
package org.alfresco.ibatis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.ibatis.executor.result.DefaultResultContext;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * A {@link ResultHandler} that collapses multiple rows based on a set of properties.
 * <p/>
 * This class is derived from earlier RollupRowHandler used to workaround the <b>groupBy</b> and nested <b>ResultMap</b>
 * behaviour in iBatis (2.3.4.726) <a href=https://issues.apache.org/jira/browse/IBATIS-503>IBATIS-503</a>.
 * <p>
 * The set of properties given act as a unique key.  When the unique key <i>changes</i>, the collection
 * values from the nested <i>ResultMap<i> are coalesced and the given {@link ResultHandler} is called.  It is
 * possible to embed several instances of this handler for deeply-nested <i>ResultMap</i> declarations.
 * <p>
 * Use this instance as a regular {@link ResultHandler}, but with one big exception: call {@link #processLastResults()}
 * after executing the SQL statement.  Remove the <b>groupBy</b> attribute from the iBatis <b>ResultMap</b>
 * declaration.
 * <p>
 * Example iBatis 2.x (TODO migrate example to MyBatis 3.x):
 * <code><pre>
    &lt;resultMap id="result_AuditQueryAllValues"
               extends="alfresco.audit.result_AuditQueryNoValues"
               class="AuditQueryResult"&gt;
        &lt;result property="auditValues" resultMap="alfresco.propval.result_PropertyIdSearchRow"/&gt;
    &lt;/resultMap&gt;
 * </code></pre>
 * Example usage:
 * <code><pre>
        RowHandler rowHandler = new RowHandler()
        {
            public void handleRow(Object valueObject)
            {
                // DO SOMETHING
            }
        };
        RollupRowHandler rollupRowHandler = new RollupRowHandler(
                new String[] {"auditEntryId"},
                "auditValues",
                rowHandler,
                maxResults);
        
        if (maxResults > 0)
        {
            // Calculate the maximum results required
            int sqlMaxResults = (maxResults > 0 ? ((maxResults+1) * 20) : Integer.MAX_VALUE);
            
            List<AuditQueryResult> rows = template.queryForList(SELECT_ENTRIES_WITH_VALUES, params, 0, sqlMaxResults);
            for (AuditQueryResult row : rows)
            {
                rollupRowHandler.handleRow(row);
            }
            // Don't process last result:
            //    rollupRowHandler.processLastResults();
            //    The last result may be incomplete
        }
        else
        {
            template.queryWithRowHandler(SELECT_ENTRIES_WITH_VALUES, params, rollupRowHandler);
            rollupRowHandler.processLastResults();
        }
 * </pre></code>
 * <p>
 * This class is not thread-safe; use a new instance for each use.
 * 
 * @author Derek Hulley, janv
 * @since 4.0
 */
public class RollupResultHandler implements ResultHandler
{
    private final String[] keyProperties;
    private final String collectionProperty;
    private final ResultHandler resultHandler;
    private final int maxResults;
    
    private Object[] lastKeyValues;
    private List<Object> rawResults;
    private int resultCount;
    
    private Configuration configuration;
    
    /**
     * @param keyProperties         the properties that make up the unique key
     * @param collectionProperty    the property mapped using a nested <b>ResultMap</b>
     * @param resultHandler         the result handler that will receive the rolled-up results
     */
    public RollupResultHandler(Configuration configuration, String[] keyProperties, String collectionProperty, ResultHandler resultHandler)
    {
        this(configuration, keyProperties, collectionProperty, resultHandler, Integer.MAX_VALUE);
    }

    /**
     * @param keyProperties         the properties that make up the unique key
     * @param collectionProperty    the property mapped using a nested <b>ResultMap</b>
     * @param resultHandler         the result handler that will receive the rolled-up results
     * @param maxResults            the maximum number of results to retrieve (-1 for no limit).
     *                              Make sure that the query result limit is large enough to produce this
     *                              at least this number of results
     */
    public RollupResultHandler(Configuration configuration, String[] keyProperties, String collectionProperty, ResultHandler resultHandler, int maxResults)
    {
        if (keyProperties == null || keyProperties.length == 0)
        {
            throw new IllegalArgumentException("RollupRowHandler can only be used with at least one key property.");
        }
        if (collectionProperty == null)
        {
            throw new IllegalArgumentException("RollupRowHandler must have a collection property.");
        }
        this.configuration = configuration;
        this.keyProperties = keyProperties;
        this.collectionProperty = collectionProperty;
        this.resultHandler = resultHandler;
        this.maxResults = maxResults;
        this.rawResults = new ArrayList<Object>(100);
    }
    
    public void handleResult(ResultContext context)
    {
        // Shortcut if we have processed enough results
        if (maxResults > 0 && resultCount >= maxResults)
        {
            return;
        }
        
        Object valueObject = context.getResultObject();
        MetaObject probe = configuration.newMetaObject(valueObject);
        
        // Check if the key has changed
        if (lastKeyValues == null)
        {
            lastKeyValues = getKeyValues(probe);
            resultCount = 0;
        }
        // Check if it has changed
        Object[] currentKeyValues = getKeyValues(probe);
        if (!Arrays.deepEquals(lastKeyValues, currentKeyValues))
        {
            // Key has changed, so handle the results
            Object resultObject = coalesceResults(configuration, rawResults, collectionProperty);
            if (resultObject != null)
            {
                DefaultResultContext resultContext = new DefaultResultContext();
                resultContext.nextResultObject(resultObject);
                
                resultHandler.handleResult(resultContext);
                resultCount++;
            }
            rawResults.clear();
            lastKeyValues = currentKeyValues;
        }
        // Add the new value to the results for next time
        rawResults.add(valueObject);
        // Done
    }
    
    /**
     * Client code <b>must</b> call this method once the query returns so that the final results
     * can be passed to the inner RowHandler.  If a query is limited by size, then it is
     * possible that the unprocessed results represent an incomplete final object; in this case
     * it would be best to ignore the last results.  If the query is complete (i.e. all results
     * are returned) then this method should be called.
     * <p>
     * If you want X results and each result is made up of N rows (on average), then set the query
     * limit to: <br/>
     *   L = X * (N+1)<br/>
     * and don't call this method.
     */
    public void processLastResults()
    {
        // Shortcut if we have processed enough results
        if (maxResults > 0 && resultCount >= maxResults)
        {
            return;
        }
        // Handle any outstanding results
        Object resultObject = coalesceResults(configuration, rawResults, collectionProperty);
        if (resultObject != null)
        {
            DefaultResultContext resultContext = new DefaultResultContext();
            resultContext.nextResultObject(resultObject);
            
            resultHandler.handleResult(resultContext);
            resultCount++;
            rawResults.clear();                         // Stop it from being used again
        }
    }
    
    @SuppressWarnings("unchecked")
    private static Object coalesceResults(Configuration configuration, List<Object> valueObjects, String collectionProperty)
    {
        // Take the first result as the base value
        Object resultObject = null;
        MetaObject probe = null;
        Collection<Object> collection = null;
        for (Object object : valueObjects)
        {
            if (collection == null)
            {
                resultObject = object;
                probe = configuration.newMetaObject(resultObject);
                collection = (Collection<Object>) probe.getValue(collectionProperty);
            }
            else
            {
                Collection<?> addedValues = (Collection<Object>) probe.getValue(collectionProperty);
                collection.addAll(addedValues);
            }
        }
        // Done
        return resultObject;
    }
    
    /**
     * @return          Returns the values for the {@link RollupResultHandler#keyProperties}
     */
    private Object[] getKeyValues(MetaObject probe)
    {
        Object[] keyValues = new Object[keyProperties.length];
        for (int i = 0; i < keyProperties.length; i++)
        {
            keyValues[i] = probe.getValue(keyProperties[i]);
        }
        return keyValues;
    }
}
