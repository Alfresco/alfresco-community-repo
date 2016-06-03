package org.alfresco.repo.search.impl.noindex;

import org.alfresco.repo.node.index.IndexRecovery;

/**
 * @author Andy
 *
 */
public class NoIndexIndexRecovery implements IndexRecovery
{

    /* (non-Javadoc)
     * @see org.alfresco.repo.node.index.IndexRecovery#reindex()
     */
    @Override
    public void reindex()
    {
        // Nothing to do at the moment
        // Should send check and recovery commands etc when we support them ....
    }

}
