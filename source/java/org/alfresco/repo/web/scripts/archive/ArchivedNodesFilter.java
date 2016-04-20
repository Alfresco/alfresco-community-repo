package org.alfresco.repo.web.scripts.archive;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This interface defines a filter for ArchivedNodes.
 * 
 * @author Neil Mc Erlean
 * @since 3.5
 */
public interface ArchivedNodesFilter
{
    /**
     * This method checks whether or not the specified {@link NodeRef} should be included,
     * as defined by the concrete filter implementation.
     * @param nodeRef the NodeRef to be checked for filtering.
     * @return <code>true</code> if the {@link NodeRef} is acceptable, else <code>false</code>.
     */
    boolean accept(NodeRef nodeRef);

}