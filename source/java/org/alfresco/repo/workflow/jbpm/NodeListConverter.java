/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
            
            JBPMNodeList nodes = new JBPMNodeList(serviceRegistry);
            for (NodeRef nodeRef : nodeRefs)
            {
                nodes.add(new JBPMNode(nodeRef, serviceRegistry));
            }
            reverted = nodes;
        }
        return reverted;
    }

}
