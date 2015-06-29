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
 * @since 3.0
 */
@Component
public class CollectionPostMethodInvocationProcessor extends BasePostMethodInvocationProcessor
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor.BasePostMethodInvocationProcessor#getClassName()
     */
    @Override
    protected Class<?> getClassName()
    {
        return Collection.class;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor.BasePostMethodInvocationProcessor#process(java.lang.Object)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public <T> T process(T object)
    {
        Collection collection = ((Collection) object);

        if (collection != null)
        {
            BasePostMethodInvocationProcessor processor = pickProcessor(collection);
            if (processor != null)
            {
                object = (T) processCollection(collection, processor);
            }
        }

        return object;
    }

    /**
     * Process a collection using the supplied processor.
     *
     * @param collection The collection to be processed.
     * @param processor A collection suitable for access by someone with the current security clearance.
     */
    protected <T> Collection<T> processCollection(Collection<T> collection, BasePostMethodInvocationProcessor processor)
    {
        Iterator<T> iterator = collection.iterator();
        while (iterator.hasNext())
        {
            Object next = iterator.next();
            Object processed = processor.process(next);
            try
            {
                if (processed == null)
                {
                    iterator.remove();
                }
                else if (!processed.equals(next))
                {
                    // Modifying members of this type of collection is not supported, so filter the whole collection.
                    return null;
                }
            }
            catch (UnsupportedOperationException e)
            {
                // If the collection cannot be modified and it contains classified data then the whole thing must be filtered.
                return null;
            }
        }
        return collection;
    }

    /**
     * Pick a suitable processor for the members of the collection. We assume that all the elements of a collection can
     * be processed by the same processor.
     *
     * @param collection The collection to be processed.
     * @return The chosen processor, or {@code null} if no suitable processor could be found.
     */
    @SuppressWarnings("rawtypes")
    private BasePostMethodInvocationProcessor pickProcessor(Collection collection)
    {
        Iterator iterator = collection.iterator();
        while (iterator.hasNext())
        {
            Object next = iterator.next();
            if (next != null)
            {
                BasePostMethodInvocationProcessor processor = getPostMethodInvocationProcessor().getProcessor(next);
                if (processor != null)
                {
                    return processor;
                }
            }
        }
        return null;
    }
}
