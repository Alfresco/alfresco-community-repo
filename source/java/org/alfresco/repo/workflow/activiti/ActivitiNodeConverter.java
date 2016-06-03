
package org.alfresco.repo.workflow.activiti;

import java.util.Collection;
import java.util.List;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.workflow.AbstractWorkflowNodeConverter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Nick Smith
 * @since 3.4.e
 */
public class ActivitiNodeConverter extends AbstractWorkflowNodeConverter
{
    private final ServiceRegistry serviceRegistry;
    
    public ActivitiNodeConverter(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public Object convertNode(NodeRef node)
    {
        return new ActivitiScriptNode(node, serviceRegistry);
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public List<? extends Object> convertNodes(Collection<NodeRef> values)
    {
        ActivitiScriptNodeList results = new ActivitiScriptNodeList();
        for (NodeRef node : values)
        {
            results.add(new ActivitiScriptNode(node, serviceRegistry));
        }
        return results;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public NodeRef convertToNode(Object toConvert)
    {
        return ((ScriptNode)toConvert).getNodeRef();
    }
    
    /**
    * {@inheritDoc}
    */
    @Override
    public boolean isSupported(Object object)
    {
        if (object == null)
        {
            return false;
        }
        if (object instanceof ActivitiScriptNode)
        {
            return true;
        }
        if (object instanceof ActivitiScriptNodeList)
        {
            return true;
        }
        return false;
    }
}
