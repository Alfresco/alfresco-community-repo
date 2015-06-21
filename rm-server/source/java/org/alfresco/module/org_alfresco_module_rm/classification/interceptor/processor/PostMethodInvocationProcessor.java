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

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Registry for post method invocation processors
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
public class PostMethodInvocationProcessor
{
    /** Logger */
    private static Logger LOG = Logger.getLogger(PostMethodInvocationProcessor.class);

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
    private Map<Class<?>, BasePostMethodInvocationProcessor> getProcessors()
    {
        return this.processors;
    }

    /**
     * Gets the processor from the available processors
     *
     * @param object The post invocation object
     * @return The suitable processor for the given class
     */
    protected BasePostMethodInvocationProcessor getProcessor(Object object)
    {
        mandatory("object", object);

        BasePostMethodInvocationProcessor result = null;
        Class<? extends Object> clazz = object.getClass();

        if (clazz.isArray())
        {
            result = getProcessors().get(Array.class);
        }

        if (result == null)
        {
            Set<Entry<Class<?>, BasePostMethodInvocationProcessor>> processorsEntrySet = getProcessors().entrySet();
            for (Map.Entry<Class<?>, BasePostMethodInvocationProcessor> processorEntry : processorsEntrySet)
            {
                if (processorEntry.getKey().isAssignableFrom(clazz))
                {
                    result = processorEntry.getValue();
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Processes the given object
     *
     * @param object The object to process
     * @return The processed object
     */
    public <T> T process(T object)
    {
        T result = object;

        if (result != null)
        {
            BasePostMethodInvocationProcessor processor = getProcessor(result);
            if (processor != null)
            {
                result = processor.process(result);
            }
            else
            {
                LOG.debug("No processor found for '" + result + "'.");
            }
        }

        return result;
    }
}
