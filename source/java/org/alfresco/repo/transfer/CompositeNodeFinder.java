
package org.alfresco.repo.transfer;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.NodeFinder;

/**
 * A {@link NodeFinder} that sums the results of multiple {@link NodeFinder}s.
 * @author Nick Smith
 * @since 4.0
 *
 */
public class CompositeNodeFinder extends AbstractNodeFinder
{
    private final Collection<NodeFinder> finders;

    public CompositeNodeFinder(NodeFinder... finders)
    {
        this.finders = Arrays.asList(finders);
    }
    
    public CompositeNodeFinder(Collection<NodeFinder> finders)
    {
        this.finders = finders;
    }
    
    /**
    * {@inheritDoc}
    */
    @Override
    public void init()
    {
        super.init();
        for (NodeFinder finder : finders)
        {
            if(finder instanceof AbstractNodeFinder)
            {
                AbstractNodeFinder nodeFinder = (AbstractNodeFinder) finder;
                nodeFinder.setServiceRegistry(serviceRegistry);
                nodeFinder.init();
            }
        }
    }
    
    /**
    * {@inheritDoc}
    */
    public Set<NodeRef> findFrom(NodeRef thisNode)
    {
        HashSet<NodeRef> results = new HashSet<NodeRef>();
        for (NodeFinder finder : finders)
        {
            Set<NodeRef> result = finder.findFrom(thisNode);
            if(result != null)
            {
                results.addAll(result);
            }
        }
        return results;
    }
}
