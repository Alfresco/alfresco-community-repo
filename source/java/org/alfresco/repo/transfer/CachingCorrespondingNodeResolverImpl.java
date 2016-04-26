
package org.alfresco.repo.transfer;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author brian
 * 
 */
public class CachingCorrespondingNodeResolverImpl implements CorrespondingNodeResolver
{
    private static final Log log = LogFactory.getLog(CachingCorrespondingNodeResolverImpl.class);
    
    private Map<NodeRef, ResolvedParentChildPair> cache = new HashMap<NodeRef, ResolvedParentChildPair>(359);
    private CorrespondingNodeResolver delegateResolver;

    public CachingCorrespondingNodeResolverImpl()
    {

    }

    /**
     * @param delegateResolver CorrespondingNodeResolver
     */
    public CachingCorrespondingNodeResolverImpl(CorrespondingNodeResolver delegateResolver)
    {
        super();
        this.delegateResolver = delegateResolver;
    }

    public ResolvedParentChildPair resolveCorrespondingNode(NodeRef sourceNodeRef, ChildAssociationRef primaryAssoc,
            Path parentPath)
    {
        
        ResolvedParentChildPair result = cache.get(sourceNodeRef);

        if (result != null)
        {
            if (log.isDebugEnabled()) 
            {
                log.debug("Found fully-resolved entry in cache for node " + sourceNodeRef);
            }
            return result;
        }

        result = delegateResolver.resolveCorrespondingNode(sourceNodeRef, primaryAssoc, parentPath);

        //If we have fully resolved the parent and child nodes then stick it in the cache...
        if (result.resolvedChild != null && result.resolvedParent != null)
        {
            cache.put(sourceNodeRef, result);
        }
        return result;
    }

    /**
     * @param delegateResolver the delegateResolver to set
     */
    public void setDelegateResolver(CorrespondingNodeResolver delegateResolver)
    {
        this.delegateResolver = delegateResolver;
    }
}
