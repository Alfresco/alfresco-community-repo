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
import java.util.Iterator;

import org.springframework.stereotype.Component;

/**
 * Collection Post Method Invocation Processor.
 *
 * @author Tuna Aksoy
 * @since 3.0.a
 */
@Component
public class CollectionPostMethodInvocationProcessor extends BasePostMethodInvocationProcessor
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor.BasePostMethodInvocationProcessor#getClassName()
     */
    @SuppressWarnings("rawtypes")
    @Override
    protected Class<Collection> getClassName()
    {
        return Collection.class;
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
            BasePostMethodInvocationProcessor processor = null;
            Collection collection = getClassName().cast(object);
            Iterator<T> iterator = collection.iterator();

            while (iterator.hasNext())
            {
                Object element = iterator.next();
                if (processor == null)
                {
                    processor = getPostMethodInvocationProcessor().getProcessor(element);
                    if (processor == null)
                    {
                        break;
                    }
                }

                Object processedElement = processor.process(element);
                if (processedElement == null)
                {
                    iterator.remove();
                }
            }

            result = (T) collection;
        }

        return result;
    }
}
