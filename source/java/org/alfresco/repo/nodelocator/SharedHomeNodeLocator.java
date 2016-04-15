
package org.alfresco.repo.nodelocator;


import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Locates the Shared Home {@link NodeRef}.
 * 
 * @author Kevin Roast
 * @since 5.0.1
 */
public class SharedHomeNodeLocator extends AbstractNodeLocator
{
    public static final String NAME = "sharedhome";
    
    private Repository repoHelper;

    /**
    * {@inheritDoc}
    */
    public NodeRef getNode(NodeRef source, Map<String, Serializable> params)
    {
        return repoHelper.getSharedHome();
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public String getName()
    {
        return NAME;
    }
    
    /**
     * @param repoHelper the repoHelper to set
     */
    public void setRepositoryHelper(Repository repoHelper)
    {
        this.repoHelper = repoHelper;
    }
}
