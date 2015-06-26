/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.search.impl.querymodel.QueryEngineResults;
import org.alfresco.service.cmr.search.ResultSet;

/**
 * A post method invocation processor for {@link QueryEngineResults}.
 *
 * @author Tom Page
 * @since 3.0
 */
public class QueryEngineResultsPostMethodInvocationProcessor extends BasePostMethodInvocationProcessor
{
    /** The post method invocation processor for {@link ResultSet ResultSets}. */
    private BasePostMethodInvocationProcessor resultSetProcessor;

    @Override
    protected Class<QueryEngineResults> getClassName()
    {
        return QueryEngineResults.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T process(T object)
    {
        if (resultSetProcessor == null)
        {
            resultSetProcessor = getPostMethodInvocationProcessor().getProcessorForClass(ResultSet.class);
        }

        QueryEngineResults queryEngineResults = getClassName().cast(object);
        Map<Set<String>, ResultSet> resultsMap = queryEngineResults.getResults();
        Map<Set<String>, ResultSet> returnMap = new HashMap<>();
        for (Set<String> key : resultsMap.keySet())
        {
            ResultSet newResultSet = resultSetProcessor.process(resultsMap.get(key));
            if (newResultSet != null)
            {
                returnMap.put(key, newResultSet);
            }
        }
        return (T) new QueryEngineResults(returnMap);
    }
}
