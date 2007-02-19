/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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

    
    /* (non-Javadoc)
     * @see org.jbpm.context.exe.Converter#supports(java.lang.Object)
     */
    public boolean supports(Object value)
    {
        if (value == null)
        {
            return true;
        }
        return (value.getClass() == JBPMNodeList.class);
    }

    /* (non-Javadoc)
     * @see org.jbpm.context.exe.Converter#convert(java.lang.Object)
     */
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

    /* (non-Javadoc)
     * @see org.jbpm.context.exe.Converter#revert(java.lang.Object)
     */
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
