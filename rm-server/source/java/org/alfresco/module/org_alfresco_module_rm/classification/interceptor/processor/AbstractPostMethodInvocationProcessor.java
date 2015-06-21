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

import java.util.Collection;

import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.Filter;

/**
 * Abstract Post Method Invocation Processor
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
public abstract class AbstractPostMethodInvocationProcessor extends BasePostMethodInvocationProcessor
{
    /**
     * Abstract method to process a single element
     *
     * @param object The element to process
     * @return Processed element
     */
    protected abstract <T> T processSingleElement(T object);

    /**
     * Processes a collection
     *
     * @param collection The collection to process
     * @return Processed collection
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Collection processCollection(Collection collection)
    {
        return CollectionUtils.filter(collection, new Filter()
        {
            @Override
            public Boolean apply(Object element)
            {
                return processSingleElement(element) != null;
            }
        });
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor.BasePostMethodInvocationProcessor#process(java.lang.Object)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public <T> T process(T object)
    {
        T result = object;

        if (result != null)
        {
            if (isCollection(result))
            {
                Collection collection = ((Collection) result);
                if (!collection.isEmpty())
                {
                    result = (T) processCollection(collection);
                }
            }
            else
            {
                result = processSingleElement(result);
            }
        }

        return result;
    }
}
