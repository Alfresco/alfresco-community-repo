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
 * Collection Post Method Invocation Processor
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
    @SuppressWarnings("rawtypes")
    @Override
    public <T> T process(T object)
    {
        T result = object;

        if (result != null)
        {
            Collection collection = ((Collection) result);
            if (!collection.isEmpty())
            {
                Iterator iterator = collection.iterator();
                while (iterator.hasNext())
                {
                    Object next = iterator.next();
                    // TODO: Can we guarantee that all the elements of a collection can be processed by the same processor?
                    BasePostMethodInvocationProcessor processor = getPostMethodInvocationProcessor().getProcessor(next);
                    if (processor != null)
                    {
                        Object processed = processor.process(next);
                        if (processed == null)
                        {
                            iterator.remove();
                        }
                        else if (!processed.equals(next))
                        {
                            // TODO Support this, as it will be hit by e.g. collections of collections.
                            throw new IllegalStateException("Modifying members of a collection is not yet supported.");
                        }
                    }
                }
            }
        }

        return result;
    }
}
