package org.alfresco.repo.workflow.jbpm;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.jbpm.context.exe.Converter;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springmodules.workflow.jbpm31.JbpmFactoryLocator;


/**
 * jBPM Converter for transforming Alfresco Node to string and back
 * 
 * @author davidc
 */
public class NodeConverter implements Converter
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
        return (value.getClass() == JBPMNode.class);
    }

    /* (non-Javadoc)
     * @see org.jbpm.context.exe.Converter#convert(java.lang.Object)
     */
    public Object convert(Object o)
    {
        Object converted = null;
        if (o != null)
        {
            converted = ((JBPMNode)o).getNodeRef().toString();
        }
        return converted;
    }

    /* (non-Javadoc)
     * @see org.jbpm.context.exe.Converter#revert(java.lang.Object)
     */
    public Object revert(Object o)
    {
        Object reverted = null;
        if (o != null)
        {
            BeanFactoryReference factory = jbpmFactoryLocator.useBeanFactory(null);
            ServiceRegistry serviceRegistry = (ServiceRegistry)factory.getFactory().getBean(ServiceRegistry.SERVICE_REGISTRY);
            reverted = new JBPMNode(new NodeRef((String)o), serviceRegistry);
        }
        return reverted;
    }

}
