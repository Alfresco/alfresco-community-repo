package org.alfresco.repo.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

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
