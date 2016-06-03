package org.alfresco.repo.security.permissions.impl;

import org.alfresco.service.namespace.QName;

/**
 * Store and read the definition of a required permission.
 * 
 * @author andyh
 */
public final class RequiredPermission extends PermissionReferenceImpl
{
    /**
     * 
     */
    private static final long serialVersionUID = 4840771159714835909L;

    public enum On {
        PARENT, NODE, CHILDREN
    };

    private On on;

    boolean implies;

    public RequiredPermission(QName qName, String name, On on, boolean implies)
    {
        super(qName, name);
        this.on = on;
        this.implies = implies;
    }

    public boolean isImplies()
    {
        return implies;
    }

    public On getOn()
    {
        return on;
    }
}
