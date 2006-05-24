/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.domain.hibernate;

import java.io.Serializable;

import org.alfresco.repo.domain.DbAccessControlEntry;
import org.alfresco.repo.domain.DbAccessControlList;
import org.alfresco.repo.domain.DbAuthority;
import org.alfresco.repo.domain.DbPermission;
import org.alfresco.util.EqualsHelper;

/**
 * Persisted permission entries
 * 
 * @author andyh
 */
public class DbAccessControlEntryImpl extends LifecycleAdapter
    implements DbAccessControlEntry, Serializable
{
    private static final long serialVersionUID = -418837862334064582L;

    /** The object id */
    private long id;
    
    /** The container of these entries */
    private DbAccessControlList accessControlList;

    /** The permission to which this applies (non null - all is a special string) */
    private DbPermission permission;

    /** The recipient to which this applies (non null - all is a special string) */
    private DbAuthority authority;

    /** Is this permission allowed? */
    private boolean allowed;

    public DbAccessControlEntryImpl()
    {
        super();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("DbAccessControlEntryImpl")
          .append("[ id=").append(id)
          .append(", acl=").append(accessControlList.getId())
          .append(", permission=").append(permission.getKey())
          .append(", authority=").append(authority.getRecipient())
          .append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof DbAccessControlEntry))
        {
            return false;
        }
        DbAccessControlEntry other = (DbAccessControlEntry) o;
        return (EqualsHelper.nullSafeEquals(this.permission, other.getPermission())
                && EqualsHelper.nullSafeEquals(this.authority, other.getAuthority()));
    }

    @Override
    public int hashCode()
    {
        int hashCode = 0;
        if (permission != null)
        {
            hashCode = hashCode * 37 + permission.hashCode();
        }
        if (authority != null)
        {
            hashCode = hashCode * 37 + authority.hashCode();
        }
        return hashCode;
    }
    
    public long getId()
    {
        return id;
    }
    
    /**
     * For Hibernate use
     */
    /* package */ void setId(long id)
    {
        this.id = id;
    }

    public DbAccessControlList getAccessControlList()
    {
        return accessControlList;
    }

    public void setAccessControlList(DbAccessControlList nodePermissionEntry)
    {
        this.accessControlList = nodePermissionEntry;
    }

    public DbPermission getPermission()
    {
        return permission;
    }

    public void setPermission(DbPermission permissionReference)
    {
        this.permission = permissionReference;
    }

    public DbAuthority getAuthority()
    {
        return authority;
    }

    public void setAuthority(DbAuthority recipient)
    {
        this.authority = recipient;
    }

    public boolean isAllowed()
    {
        return allowed;
    }

    public void setAllowed(boolean allowed)
    {
        this.allowed = allowed;
    }

    public void delete()
    {
        // remove the instance from the access control list
        @SuppressWarnings("unused")
        boolean removed = getAccessControlList().getEntries().remove(this);
        // delete the instance
        getSession().delete(this);
    }
}
