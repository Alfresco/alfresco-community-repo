package org.alfresco.repo.ownable.impl;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.OwnableService;

/**
 * A simple implementation that does not support ownership.
 * 
 * @author Andy Hind
 */
public class OwnableServiceNOOPImpl implements OwnableService
{

    public OwnableServiceNOOPImpl()
    {
        super();
    }

    public String getOwner(NodeRef nodeRef)
    {
        // Return null as there is no owner.
        return null;
    }

    public void setOwner(NodeRef nodeRef, String userName)
    {
        // No action.
    }

    public void takeOwnership(NodeRef nodeRef)
    {   
        // No action.
    }

    public boolean hasOwner(NodeRef nodeRef)
    {
        // There is no owner for any node.
        return false;
    }

}
