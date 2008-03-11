/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
