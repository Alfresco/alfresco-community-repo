
package org.alfresco.repo.transfer;

import java.util.Collections;
import java.util.Set;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class PrimaryParentNodeFinder extends AbstractNodeFinder
{
    private NodeService nodeService;
    
    /**
    * {@inheritDoc}
    */
    @Override
    public void init()
    {
        super.init();
        this.nodeService = serviceRegistry.getNodeService();
    }
    
    /**
    * {@inheritDoc}
    */
    @Override
    public Set<NodeRef> findFrom(NodeRef thisNode)
    {
        ChildAssociationRef assoc = nodeService.getPrimaryParent(thisNode);
        if(assoc != null)
        {
            return Collections.singleton(assoc.getParentRef());
        }
        return Collections.emptySet();
    }
}
