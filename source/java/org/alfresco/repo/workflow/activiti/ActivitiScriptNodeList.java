
package org.alfresco.repo.workflow.activiti;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * List of {@link ActivitiScriptNode}s.
 *
 * @author Frederik Heremans
 * @since 3.4.e
 */
public class ActivitiScriptNodeList extends ArrayList<ActivitiScriptNode>
{
    private static final long serialVersionUID = 5177463364573735290L;

    public List<NodeRef> getNodeReferences() 
    {
        // Extract all node references
        List<NodeRef> nodeRefs = new ArrayList<NodeRef>();
        for (ActivitiScriptNode scriptNode : this)
        {
            nodeRefs.add(scriptNode.getNodeRef());
        }
        return nodeRefs;
    }
    
    @Override
    public int size() 
    {
        return super.size();
    }
}
