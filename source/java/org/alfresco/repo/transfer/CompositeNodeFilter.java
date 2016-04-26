
package org.alfresco.repo.transfer;

import java.util.Arrays;
import java.util.Collection;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.NodeFilter;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class CompositeNodeFilter extends AbstractNodeFilter
{
    private final Collection<NodeFilter> filters;

    public CompositeNodeFilter(NodeFilter... filters)
    {
        this.filters = Arrays.asList(filters);
    }
    
    public CompositeNodeFilter(Collection<NodeFilter> filters)
    {
        this.filters = filters;
    }
    
    /**
    * {@inheritDoc}
    */
    @Override
    public void init()
    {
        super.init();
        for (NodeFilter filter : filters)
        {
            if(filter instanceof AbstractNodeFilter)
            {
                AbstractNodeFilter nodeFilter = (AbstractNodeFilter) filter;
                nodeFilter.setServiceRegistry(serviceRegistry);
                nodeFilter.init();
            }
        }
    }
    
    /**
    * {@inheritDoc}
    */
    @Override
    public boolean accept(NodeRef thisNode)
    {
        for (NodeFilter filter : filters)
        {
            if(filter.accept(thisNode)==false)
            {
                return false;
            }
        }
        return true;
    }
}
