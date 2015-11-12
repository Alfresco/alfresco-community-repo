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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The context in which a virtualization process takes place.
 * 
 * @author Bogdan Horje
 */
public class VirtualContext
{

    public static final String CONTEXT_PARAM = "context";

    public static final String PLACEHOLDERS_PARAM = "placeholders";

    private Map<String, Object> parameters;

    private NodeRef actualNodeRef;

    private ActualEnvironment actualEnviroment;

    public VirtualContext(VirtualContext context)
    {
        this(context.actualEnviroment,
             context.actualNodeRef,
             new HashMap<String, Object>(context.parameters));
    }

    public VirtualContext(ActualEnvironment actualEnviroment, NodeRef actualNodeRef)
    {
        this(actualEnviroment,
             actualNodeRef,
             new HashMap<String, Object>());
    }

    public VirtualContext(ActualEnvironment actualEnviroment, NodeRef actualNodeRef, Map<String, Object> parameters)
    {
        this.parameters = parameters;
        this.parameters.put(CONTEXT_PARAM,
                            this);
        this.actualEnviroment = actualEnviroment;
        this.actualNodeRef = actualNodeRef;
    }

    public ActualEnvironment getActualEnviroment()
    {
        return actualEnviroment;
    }

    public NodeRef getActualNodeRef()
    {
        return actualNodeRef;
    }

    public void setParameter(String parameter, Object value)
    {
        parameters.put(parameter,
                       value);
    }

    public Map<String, Object> getParameters()
    {
        return new HashMap<>(parameters);
    }

    @Override
    public int hashCode()
    {
        return actualNodeRef.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof VirtualContext)
        {
            VirtualContext ctxObj = (VirtualContext) obj;
            return actualNodeRef.equals(ctxObj.actualNodeRef);
        }
        else
        {
            return false;
        }
    }

    @Override
    public String toString()
    {
        return actualNodeRef.toString();
    }

    public Object getParameter(String param)
    {
        return parameters.get(param);
    }
}
