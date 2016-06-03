package org.alfresco.repo.security.permissions.impl.model;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Exceptions related to the permissions model
 * 
 * @author andyh
 */
public class PermissionModelException extends AlfrescoRuntimeException
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -5156253607792153538L;

    public PermissionModelException(String msg)
    {
        super(msg);
    }

    public PermissionModelException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

}
