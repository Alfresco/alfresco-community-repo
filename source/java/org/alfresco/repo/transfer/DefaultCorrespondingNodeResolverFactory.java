
package org.alfresco.repo.transfer;

import org.alfresco.service.cmr.repository.NodeService;

/**
 * @author brian
 *
 */
public class DefaultCorrespondingNodeResolverFactory implements CorrespondingNodeResolverFactory
{
    private NodeService nodeService;
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.transfer.CorrespondingNodeResolverFactory#getResolver()
     */
    public CorrespondingNodeResolver getResolver()
    {
        BasicCorrespondingNodeResolverImpl basicResolver = new BasicCorrespondingNodeResolverImpl();
        basicResolver.setNodeService(nodeService);
        CachingCorrespondingNodeResolverImpl cachingResolver = new CachingCorrespondingNodeResolverImpl(basicResolver);
        return cachingResolver;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    
}
