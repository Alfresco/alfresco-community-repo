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

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for post method invocation processors
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
public class PostMethodInvocationProcessorRegistry
{
    /** Post method invocation processors */
    private Map<Class<?>, BasePostMethodInvocationProcessor> processors = new HashMap<Class<?>, BasePostMethodInvocationProcessor>();

    /**
     * Registers a post method invocation processor
     *
     * @param Post method invocation processor object
     */
    public void register(BasePostMethodInvocationProcessor object)
    {
        mandatory("object", object);

        processors.put(object.getClassName(), object);
    }

    /**
     * Gets all the available processors
     *
     * @return the processors Available processors
     */
    public Map<Class<?>, BasePostMethodInvocationProcessor> getProcessors()
    {
        return this.processors;
    }

    /**
     * Gets the processor from the available processors
     *
     * @param clazz The runtime class of the post invocation object
     * @return The suitable processor for the given class
     */
    public BasePostMethodInvocationProcessor getProcessor(Class<? extends Object> clazz)
    {
        mandatory("clazz", clazz);

        BasePostMethodInvocationProcessor result = null;

        for (Map.Entry<Class<?>, BasePostMethodInvocationProcessor> processor : processors.entrySet())
        {
            if (processor.getKey().isAssignableFrom(clazz))
            {
                result = processor.getValue();
                break;
            }
        }

        return result;
    }
}
