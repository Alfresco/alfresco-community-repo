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

import static org.alfresco.util.ParameterCheck.mandatory;

import java.util.Collection;

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
     * Abstract method to process a collection
     *
     * @param collection The collection to process
     * @return Processed collection
     */
    @SuppressWarnings("rawtypes")
    protected abstract Collection processCollection(Collection collection);

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor.BasePostMethodInvocationProcessor#process(java.lang.Object)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public <T> T process(T object)
    {
        mandatory("object", object);

        T result = null;

        if (isCollection(object))
        {
            Collection collection = ((Collection) object);
            if (!collection.isEmpty())
            {
                checkObjectClass(collection.iterator().next());
                result = (T) processCollection(collection);
            }
        }
        else
        {
            checkObjectClass(object);
            result = (T) processSingleElement(object);
        }

        return result;
    }
}
