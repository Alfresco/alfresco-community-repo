/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.repo.action.parameter;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Abstract parameter processor implementation.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public abstract class ParameterProcessor
{
    /** Processor name */
    private String name;
    
    /** Parameter processor component */
    private ParameterProcessorComponent parameterProcessorComponent;
    
    /**
     * @return  parameter processor name
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * @param name  parameter processor name
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * @param parameterProcessorComponent   parameter processor component 
     */
    public void setParameterProcessorComponent(ParameterProcessorComponent parameterProcessorComponent)
    {
        this.parameterProcessorComponent = parameterProcessorComponent;
    }
    
    /**
     * Init method
     */
    public void init()
    {
        parameterProcessorComponent.register(this);
    }
    
    /**
     * Process the parameter value.
     * 
     * @param value                     substitution value
     * @param actionedUponNodeRef       actioned upon node reference
     * @return String                   processed string, original string if subs string invalid
     */
    public abstract String process(String value, NodeRef actionedUponNodeRef);
    
    /**
     * Strips the name of the processor from the subs value.
     * 
     * @param value     subs value
     * @return String   subs value with the name and '.' delimiter removed
     */
    protected String stripName(String value)
    {
        String result = "";
        String[] values = value.split("\\.", 2);
        if (values.length == 2)
        {
            result = values[1];
        }
        return result;
    }
}
