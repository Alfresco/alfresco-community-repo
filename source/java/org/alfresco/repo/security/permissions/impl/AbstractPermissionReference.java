package org.alfresco.repo.security.permissions.impl;

import org.alfresco.repo.security.permissions.PermissionReference;

/**
 * This class provides common support for hash code and equality.
 * 
 * @author andyh
 */
@SuppressWarnings("serial")
public abstract class AbstractPermissionReference implements PermissionReference
{
    private int hashcode = 0;
    private String str = null;
    
    protected AbstractPermissionReference()
    {
        super();
    }

    @Override
    public final boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(!(o instanceof AbstractPermissionReference))
        {
            return false;
        }
        AbstractPermissionReference other = (AbstractPermissionReference)o;
        if(other.hashCode() != this.hashCode())
        {
            return false;
        }
        return this.getName().equals(other.getName()) && this.getQName().equals(other.getQName());
    }

    @Override
    public final int hashCode()
    {
        if (hashcode == 0)
        {
           hashcode = (getName().hashCode() * 1000003) + getQName().hashCode();
        }
        return hashcode;
    }

    @Override
    public String toString()
    {
        if (str == null)
        {
            str = getQName() + "." + getName();
        }
        return str;
    }
}
