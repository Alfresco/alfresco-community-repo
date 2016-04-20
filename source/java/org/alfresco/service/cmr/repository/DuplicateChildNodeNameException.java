package org.alfresco.service.cmr.repository;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.transaction.DoNotRetryException;
import org.alfresco.service.namespace.QName;


/**
 * Thrown when a child node <b>cm:name</b> property  violates the data dictionary
 * <b>duplicate</b> child association constraint.
 * <p/>
 * Note that this exception may be triggered by database constraints but must
 * still NOT trigger transaction retries.
 * 
 * @author Derek Hulley
 */
public class DuplicateChildNodeNameException extends RuntimeException implements DoNotRetryException
{
    private static final long serialVersionUID = 5143099335847200453L;

    private static final String ERR_DUPLICATE_NAME = "system.err.duplicate_name";
    
    private NodeRef parentNodeRef;
    private QName assocTypeQName;
    private String name;
    
    public DuplicateChildNodeNameException(NodeRef parentNodeRef, QName assocTypeQName, String name, Throwable e)
    {
        super(I18NUtil.getMessage(ERR_DUPLICATE_NAME, name), e);
        this.parentNodeRef = parentNodeRef;
        this.assocTypeQName = assocTypeQName;
        this.name = name;
    }

    public NodeRef getParentNodeRef()
    {
        return parentNodeRef;
    }

    public QName getAssocTypeQName()
    {
        return assocTypeQName;
    }

    public String getName()
    {
        return name;
    }
}
