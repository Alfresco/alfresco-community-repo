package org.alfresco.repo.security.person;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A ref to a home folder 
 * - the node ref
 * - a simple status as to how it was obtained 
 * 
 * @author Andy Hind
 */
public class HomeSpaceNodeRef
{
    public enum Status{VALID, REFERENCED, CREATED};
    
    private NodeRef nodeRef;

    private Status status;
    
    public HomeSpaceNodeRef(NodeRef nodeRef, Status status)
    {
        this.nodeRef = nodeRef;
        this.status = status;
    }

    NodeRef getNodeRef()
    {
        return nodeRef;
    }

    Status getStatus()
    {
        return status;
    }
    
    
}
