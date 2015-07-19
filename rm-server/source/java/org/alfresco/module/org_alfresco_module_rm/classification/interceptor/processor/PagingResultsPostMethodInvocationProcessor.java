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

import java.util.List;

import org.alfresco.query.PagingResults;
import org.alfresco.util.Pair;
import org.springframework.stereotype.Component;

/**
 * PagingResults Post Method Invocation Processor
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
@Component
public class PagingResultsPostMethodInvocationProcessor extends BasePostMethodInvocationProcessor
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor.BasePostMethodInvocationProcessor#getClassName()
     */
    @SuppressWarnings("rawtypes")
    @Override
    protected Class<PagingResults> getClassName()
    {
        return PagingResults.class;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor.BasePostMethodInvocationProcessor#process(java.lang.Object)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected <T> T process(T object)
    {
        T result = object;

        if (result != null)
        {
            final PagingResults pagingResults = getClassName().cast(result);
            List page = pagingResults.getPage();
            int originalPageSize = page.size();
            final List processedPage = getPostMethodInvocationProcessor().process(page);

            if (processedPage != null && processedPage.size() != originalPageSize)
            {
                result = (T) new PagingResults<T>()
                {
                    @Override
                    public String getQueryExecutionId()
                    {
                        return pagingResults.getQueryExecutionId();
                    }
                    @Override
                    public List<T> getPage()
                    {
                        return processedPage;
                    }
                    @Override
                    public boolean hasMoreItems()
                    {
                        // hasMoreItems might not be correct. Cannot determine the correct value as request details are needed.
                        return pagingResults.hasMoreItems();
                    }
                    @Override
                    public Pair<Integer, Integer> getTotalResultCount()
                    {
                        int size = processedPage.size();
                        return new Pair<Integer, Integer>(size, size);
                    }
                };
            }
        }

        return result;
    }
}
