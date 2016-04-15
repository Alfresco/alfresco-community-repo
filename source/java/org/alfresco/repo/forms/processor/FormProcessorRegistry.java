/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.forms.processor;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.forms.Item;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Holds a FormProcessor implementation for each of the types of form that
 * can be processed. By default a node, type, task, and workflow form processor
 * are available.
 * <p>
 * Given an item the registry selects the relevant form processor, the match
 * is performed via pattern matching on the supplied string.
 *
 * @author Gavin Cornwell
 */
public class FormProcessorRegistry
{
    /** Logger */
    private static Log logger = LogFactory.getLog(FormProcessorRegistry.class);
    
    protected List<FormProcessor> processors;
    
    /**
     * Constructs the registry
     */
    public FormProcessorRegistry()
    {
        this.processors = new ArrayList<FormProcessor>(4);
    }
    
    /**
     * Registers a form processor
     * 
     * @param processor The FormProcessor to regsiter
     */
    public void addProcessor(FormProcessor processor)
    {
        if (processor.isActive())
        {
            this.processors.add(processor);
            
            if (logger.isDebugEnabled())
                logger.debug("Registered processor: " + processor);
        }
        else if (logger.isWarnEnabled())
        {
            logger.warn("Ignored registration of processor " + processor + "as it was marked as inactive");
        }
    }
    
    /**
     * Returns a FormProcessor for the provided item.
     * <p>
     * Each registered processors is asked if it is applicable for 
     * the given item, the first processor to positively respond 
     * that is also active is selected and returned.
     * 
     * @param item The item to find a form processor for
     * @return An applicable FormProcessor
     */
    public FormProcessor getApplicableFormProcessor(Item item)
    {
        FormProcessor selectedProcessor = null;
        
        // iterate round the processors and fall out once the first
        // active applicable processor is found
        for (FormProcessor processor : this.processors)
        {
            if (processor.isActive() && processor.isApplicable(item))
            {
                selectedProcessor = processor;
                break;
            }
        }
        
        if (logger.isDebugEnabled())
            logger.debug("Returning applicable processor: " + selectedProcessor);
        
        return selectedProcessor;
    }
}
