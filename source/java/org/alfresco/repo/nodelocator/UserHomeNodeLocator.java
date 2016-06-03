
package org.alfresco.repo.nodelocator;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This {@link NodeLocator} identifies and returns the node representing the current users home folder.
 * 
 * @author Nick Smith
 * @since 4.0
 */
public class UserHomeNodeLocator extends AbstractNodeLocator
{
    public static final String NAME = "userhome";
    private Repository repositoryHelper;
    
    /**
    * {@inheritDoc}
    */
    @Override
    public NodeRef getNode(NodeRef source, Map<String, Serializable> params)
    {
        NodeRef person = repositoryHelper.getPerson();
        if (person != null)
        {
            return repositoryHelper.getUserHome(person);
        }
        return null;
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
     * @param repositoryHelper the repositoryHelper to set
     */
    public void setRepositoryHelper(Repository repositoryHelper)
    {
        this.repositoryHelper = repositoryHelper;
    }
}
