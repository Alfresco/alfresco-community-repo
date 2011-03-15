/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @since 3.4.e
 * @author Nick Smith
 *
 */
public abstract class AbstractWorkflowNodeConverter implements WorkflowNodeConverter
{
    /**
    * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public Object convertNodes(Object value, boolean isMany)
    {
        if(value instanceof NodeRef)
        {
            return convertNode((NodeRef) value, isMany);
        }
        else if(value instanceof Collection<?>)
        {
            return convertNodes((Collection<NodeRef>) value, isMany);
        }
        return value; //If null or not a supported type.
    }

    /**
    * {@inheritDoc}
     */
    public Object convertNode(NodeRef value, boolean isMany)
    {
        if (isMany)
        {
            return convertNodes(Collections.singleton(value));
        }
        return convertNode(value);
    }
    
    /**
    * {@inheritDoc}
     */
    public Object convertNodes(Collection<NodeRef> values, boolean isMany)
    {
        if (isMany)
        {
            return convertNodes(values);
        }
        if (values.isEmpty())
        {
            return null;
        }
        return convertNode(values.iterator().next());
    }
    
    /**
     * {@inheritDoc}
     */
    public List<NodeRef> convertToNodes(Object value)
    {
        if (value instanceof Collection<?>)
        {
            return convertToNodes((Collection<?>) value);
        }
        return Collections.singletonList(convertToNode(value));
    }
    
    public List<NodeRef> convertToNodes(Collection<?> toConvert)
    {
        List<NodeRef> results = new ArrayList<NodeRef>(toConvert.size());
        for (Object obj : toConvert)
        {
            results.add(convertToNode(obj));
        }
        return results;
    }
    
    public Serializable convert(Object object)
    {
        if(object instanceof Collection<?>)
        {
            return (Serializable) convertToNodes((Collection<?>)object);
        }
        return convertToNode(object);
    }

    public abstract Object convertNode(NodeRef node);
    
    public abstract List<? extends Object> convertNodes(Collection<NodeRef> values);
    
    public abstract NodeRef convertToNode(Object toConvert);
    
}
