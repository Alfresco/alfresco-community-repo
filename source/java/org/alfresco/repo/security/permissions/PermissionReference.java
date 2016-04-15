package org.alfresco.repo.security.permissions;

import java.io.Serializable;

import org.alfresco.service.namespace.QName;

/**
 * A Permission is a named permission against a type or aspect which is defined
 * by QName. So a permission string is scoped by type.
 * 
 * @author Andy Hind
 */
public interface PermissionReference extends Serializable
{

    /**
     * Get the QName of the type or aspect against which the permission is
     * defined.
     * 
     * @return the qname
     */
    public QName getQName();

    /**
     * Get the name of the permission
     * 
     * @return the name
     */
    public String getName();
    
    
}
