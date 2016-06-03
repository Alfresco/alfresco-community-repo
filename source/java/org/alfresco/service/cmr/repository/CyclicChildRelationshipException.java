package org.alfresco.service.cmr.repository;

/**
 * Thrown when a cyclic parent-child relationship is detected.
 * 
 * @author Derek Hulley
 */
public class CyclicChildRelationshipException extends RuntimeException
{
    private static final long serialVersionUID = 3545794381924874036L;

    private ChildAssociationRef assocRef;
    
    public CyclicChildRelationshipException(String msg, ChildAssociationRef assocRef)
    {
        super(msg);
        this.assocRef = assocRef;
    }

    public ChildAssociationRef getAssocRef()
    {
        return assocRef;
    }
}
