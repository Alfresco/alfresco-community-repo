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
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.repo.search.impl.querymodel.QueryEngineResults;
import org.alfresco.service.cmr.search.ResultSet;
import org.springframework.stereotype.Component;

/**
 * A post method invocation processor for {@link QueryEngineResults}.
 *
 * @author Tom Page
 * @since 3.0
 */
@Component
public class QueryEngineResultsPostMethodInvocationProcessor extends BasePostMethodInvocationProcessor
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor.BasePostMethodInvocationProcessor#getClassName()
     */
    @Override
    protected Class<QueryEngineResults> getClassName()
    {
        return QueryEngineResults.class;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor.BasePostMethodInvocationProcessor#process(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected <T> T process(T object)
    {
        T result = object;

        if (result != null)
        {
            QueryEngineResults queryEngineResults = getClassName().cast(result);
            Map<Set<String>, ResultSet> resultsMap = queryEngineResults.getResults();
            Map<Set<String>, ResultSet> returnMap = new HashMap<>();
            BasePostMethodInvocationProcessor processor = null;

            for (Entry<Set<String>, ResultSet> entry : resultsMap.entrySet())
            {
                ResultSet value = entry.getValue();
                if (processor == null)
                {
                    processor = getPostMethodInvocationProcessor().getProcessor(value);
                }

                ResultSet newResultSet = processor.process(value);
                if (newResultSet != null)
                {
                    returnMap.put(entry.getKey(), newResultSet);
                }
            }

            result = (T) new QueryEngineResults(returnMap);
        }

        return result;
    }
}
