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

import static java.lang.reflect.Array.newInstance;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Array Post Method Invocation Processor
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
@Component
public class ArrayPostMethodInvocationProcessor extends BasePostMethodInvocationProcessor
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor.BasePostMethodInvocationProcessor#getClassName()
     */
    @Override
    protected Class<Array> getClassName()
    {
        return Array.class;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor.BasePostMethodInvocationProcessor#process(java.lang.Object)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected <T> T process(T object)
    {
        T result = object;

        if (result != null)
        {
            T[] objects = (T[]) result;
            T obj = objects[0];

            BasePostMethodInvocationProcessor processor = getPostMethodInvocationProcessor().getProcessor(obj);
            if (processor != null)
            {
                int length = objects.length;
                List processedObjects = new ArrayList();

                for (int i = 0; i < length; i++)
                {
                    Object processedObject = processor.process(objects[i]);
                    if (processedObject != null)
                    {
                        processedObjects.add(processedObject);
                    }
                }

                int size = processedObjects.size();
                T[] objs = (T[]) newInstance(obj.getClass(), size);

                for (int i = 0; i < size; i++)
                {
                    objs[i] = (T) processedObjects.get(i);
                }

                result = (T) objs;
            }
        }

        return result;
    }
}
