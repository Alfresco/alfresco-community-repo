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

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.Filter;

/**
 * Collection Post Method Invocation Processor
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
public abstract class CollectionPostMethodInvocationProcessor extends BasePostMethodInvocationProcessor
{
    /** Node ref post method invocation processor */
    private NodeRefPostMethodInvocationProcessor nodeRefPostMethodInvocationProcessor;

    /** Association post method invocation processor */
    private AssociationRefPostMethodInvocationProcessor associationRefPostMethodInvocationProcessor;

    /** Child association post method invocation processor */
    private ChildAssociationRefPostMethodInvocationProcessor childAssociationRefPostMethodInvocationProcessor;

    /**
     * @return the nodeRefPostMethodInvocationProcessor
     */
    protected NodeRefPostMethodInvocationProcessor getNodeRefPostMethodInvocationProcessor()
    {
        return this.nodeRefPostMethodInvocationProcessor;
    }

    /**
     * @return the associationRefPostMethodInvocationProcessor
     */
    protected AssociationRefPostMethodInvocationProcessor getAssociationRefPostMethodInvocationProcessor()
    {
        return this.associationRefPostMethodInvocationProcessor;
    }

    /**
     * @return the childAssociationRefPostMethodInvocationProcessor
     */
    protected ChildAssociationRefPostMethodInvocationProcessor getChildAssociationRefPostMethodInvocationProcessor()
    {
        return this.childAssociationRefPostMethodInvocationProcessor;
    }

    /**
     * @param nodeRefPostMethodInvocationProcessor the nodeRefPostMethodInvocationProcessor to set
     */
    public void setNodeRefPostMethodInvocationProcessor(NodeRefPostMethodInvocationProcessor nodeRefPostMethodInvocationProcessor)
    {
        this.nodeRefPostMethodInvocationProcessor = nodeRefPostMethodInvocationProcessor;
    }

    /**
     * @param associationRefPostMethodInvocationProcessor the associationRefPostMethodInvocationProcessor to set
     */
    public void setAssociationRefPostMethodInvocationProcessor(AssociationRefPostMethodInvocationProcessor associationRefPostMethodInvocationProcessor)
    {
        this.associationRefPostMethodInvocationProcessor = associationRefPostMethodInvocationProcessor;
    }

    /**
     * @param childAssociationRefPostMethodInvocationProcessor the childAssociationRefPostMethodInvocationProcessor to set
     */
    public void setChildAssociationRefPostMethodInvocationProcessor(ChildAssociationRefPostMethodInvocationProcessor childAssociationRefPostMethodInvocationProcessor)
    {
        this.childAssociationRefPostMethodInvocationProcessor = childAssociationRefPostMethodInvocationProcessor;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor.BasePostMethodInvocationProcessor#process(java.lang.Object)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public <T> T process(T object)
    {
        Object result = object;

        Collection collection = ((Collection) object);
        if (!collection.isEmpty())
        {
            Iterator iterator = collection.iterator();
            if (iterator.hasNext())
            {
                Class<? extends Object> clazz = iterator.next().getClass();
                if (NodeRef.class.isAssignableFrom(clazz))
                {
                    result = processNodeRef(collection);
                }

                if (AssociationRef.class.isAssignableFrom(clazz))
                {
                    result = processAssociationRef(collection);
                }

                if (ChildAssociationRef.class.isAssignableFrom(clazz))
                {
                    result = processChildAssociationRef(collection);
                }
            }
        }

        return (T) result;
    }

    /**
     * Processes a {@link NodeRef} collection
     *
     * @param collection The collection to process
     * @return The processed collection
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Collection processNodeRef(Collection collection)
    {
        return CollectionUtils.filter(collection, new Filter<NodeRef>()
        {
            @Override
            public Boolean apply(NodeRef nodeRef)
            {
                return getNodeRefPostMethodInvocationProcessor().process(nodeRef) != null;
            }
        });
    }

    /**
     * Processes a {@link AssociationRef} collection
     *
     * @param collection The collection to process
     * @return The processed collection
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Collection processAssociationRef(Collection collection)
    {
        return CollectionUtils.filter(collection, new Filter<AssociationRef>()
        {
            @Override
            public Boolean apply(AssociationRef associationRef)
            {
                return getAssociationRefPostMethodInvocationProcessor().process(associationRef) != null;
            }
        });
    }

    /**
     * Processes a {@link ChildAssociationRef} collection
     *
     * @param collection The collection to process
     * @return The processed collection
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Collection processChildAssociationRef(Collection collection)
    {
        return CollectionUtils.filter(collection, new Filter<ChildAssociationRef>()
        {
            @Override
            public Boolean apply(ChildAssociationRef childAssociationRef)
            {
                return getChildAssociationRefPostMethodInvocationProcessor().process(childAssociationRef) != null;
            }
        });
    }
}
