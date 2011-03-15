/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.workflow.jbpm;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.jbpm.context.exe.converter.SerializableToByteArrayConverter;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springmodules.workflow.jbpm31.JbpmFactoryLocator;


/**
 * jBPM Converter for transforming Alfresco Node to string and back
 * 
 * @author davidc
 */
public class NodeListConverter extends SerializableToByteArrayConverter
{

    private static final long serialVersionUID = 1L;
    private static BeanFactoryLocator jbpmFactoryLocator = new JbpmFactoryLocator();

    
    /**
     * {@inheritDoc}
      */
    @Override
    public boolean supports(Object value)
    {
        if (value == null)
        {
            return true;
        }
        return (value.getClass() == JBPMNodeList.class);
    }

    /**
     * {@inheritDoc}
      */
    @Override
    public Object convert(Object o)
    {
        Object converted = null;
        if (o != null)
        {
            JBPMNodeList nodes = (JBPMNodeList)o;
            List<NodeRef> values = new ArrayList<NodeRef>(nodes.size());
            for (JBPMNode node : nodes)
            {
                values.add(node.getNodeRef());
            }
            converted = super.convert(values);
        }
        return converted;
    }

    /**
     * {@inheritDoc}
      */
    @Override
    @SuppressWarnings("unchecked")
    public Object revert(Object o)
    {
        Object reverted = null;
        if (o != null)
        {
            List<NodeRef> nodeRefs = (List<NodeRef>)super.revert(o);
            BeanFactoryReference factory = jbpmFactoryLocator.useBeanFactory(null);
            ServiceRegistry serviceRegistry = (ServiceRegistry)factory.getFactory().getBean(ServiceRegistry.SERVICE_REGISTRY);
            
            JBPMNodeList nodes = new JBPMNodeList();
            for (NodeRef nodeRef : nodeRefs)
            {
                nodes.add(new JBPMNode(nodeRef, serviceRegistry));
            }
            reverted = nodes;
        }
        return reverted;
    }

}
