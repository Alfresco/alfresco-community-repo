/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.attributes;

import org.alfresco.repo.domain.DbAccessControlList;

/**
 * The base class of the implementation of Values.
 * @author britt
 */
public abstract class AttributeImpl extends AbstractAttribute implements Attribute 
{
    /**
     * The primary key.
     */
    private long fID;
    
    /**
     * The optimistic locking version.
     */
    private long fVersion;
    
    /**
     * ACL for this Attribute.
     */
    private DbAccessControlList fACL;
    
    /**
     * Base constructor.
     */
    protected AttributeImpl()
    {
    }
    
    /**
     * Helper constructor for copy constructors.
     * @param acl The ACL.
     */
    protected AttributeImpl(DbAccessControlList acl)
    {
        fACL = acl;
    }
    
    public void setId(long id)
    {
        fID = id;
    }
    
    public long getId()
    {
        return fID;
    }
    
    public void setVersion(long version)
    {
        fVersion = version;
    }
    
    public long getVersion()
    {
        return fVersion;
    }

    public DbAccessControlList getAcl()
    {
        return fACL;
    }

    public void setAcl(DbAccessControlList acl)
    {
        fACL = acl;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof AttributeImpl))
        {
            return false;
        }
        return fID == ((AttributeImpl)obj).fID;
    }

    @Override
    public int hashCode()
    {
        return (int)fID;
    }
}
