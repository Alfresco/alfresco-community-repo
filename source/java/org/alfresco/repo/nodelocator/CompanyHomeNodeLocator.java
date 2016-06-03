
package org.alfresco.repo.nodelocator;


import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Locates the Company Home {@link NodeRef}.
 * 
 * @author Nick Smith
 * @since 4.0
 */
public class CompanyHomeNodeLocator extends AbstractNodeLocator
{
    public static final String NAME = "companyhome";
    
    private Repository repoHelper;

    /**
    * {@inheritDoc}
    */
    public NodeRef getNode(NodeRef source, Map<String, Serializable> params)
    {
        return repoHelper.getCompanyHome();
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
