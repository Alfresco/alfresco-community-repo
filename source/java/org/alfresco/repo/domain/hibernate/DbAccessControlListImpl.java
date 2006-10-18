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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.domain.DbAccessControlEntry;
import org.alfresco.repo.domain.DbAccessControlList;
import org.alfresco.repo.domain.DbAuthority;
import org.alfresco.repo.domain.DbPermission;
import org.alfresco.repo.domain.DbPermissionKey;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

/**
 * The hibernate persisted class for node permission entries.
 * 
 * @author andyh
 */
public class DbAccessControlListImpl extends LifecycleAdapter
    implements DbAccessControlList, Serializable
{
    private static final long serialVersionUID = 3123277428227075648L;

    private static Log logger = LogFactory.getLog(DbAccessControlListImpl.class);

    private long id;
    private Set<DbAccessControlEntry> entries;
    private boolean inherits;
    
    public DbAccessControlListImpl()
    {
        entries = new HashSet<DbAccessControlEntry>(5);
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("DbAccessControlListImpl")
          .append("[ id=").append(id)
          .append(", entries=").append(entries.size())
          .append(", inherits=").append(inherits)
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
        if (!(o instanceof DbAccessControlList))
        {
            return false;
        }
        DbAccessControlList other = (DbAccessControlList) o;

        return (this.inherits == other.getInherits());
    }

    @Override
    public int hashCode()
    {
        return (inherits == false ? 0 : 17);
    }

    public long getId()
    {
        return id;
    }
    
    /**
     * Hibernate use
     */
    @SuppressWarnings("unused")
    private void setId(long id)
    {
        this.id = id;
    }

    public Set<DbAccessControlEntry> getEntries()
    {
        return entries;
    }

    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setEntries(Set<DbAccessControlEntry> entries)
    {
        this.entries = entries;
    }

    public boolean getInherits()
    {
        return inherits;
    }

    public void setInherits(boolean inherits)
    {
        this.inherits = inherits;
    }

    /**
     * @see #deleteEntry(String, DbPermissionKey)
     */
    public int deleteEntriesForAuthority(String authority)
    {
        return deleteEntry(authority, null);
    }

    /**
     * @see #deleteEntry(String, DbPermissionKey)
     */
    public int deleteEntriesForPermission(DbPermissionKey permissionKey)
    {
        return deleteEntry(null, permissionKey);
    }

    public int deleteEntry(String authority, DbPermissionKey permissionKey)
    {
        List<DbAccessControlEntry> toDelete = new ArrayList<DbAccessControlEntry>(2);
        for (DbAccessControlEntry entry : entries)
        {
            if (authority != null && !authority.equals(entry.getAuthority().getRecipient()))
            {
                // authority is not a match
                continue;
            }
            else if (permissionKey != null && !permissionKey.equals(entry.getPermission().getKey()))
            {
                // permission is not a match
                continue;
            }
            toDelete.add(entry);
        }
        // delete them
        for (DbAccessControlEntry entry : toDelete)
        {
            // remove from the entry list
            entry.delete();
        }
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Deleted " + toDelete.size() + " access entries: \n" +
                    "   access control list: " + id + "\n" +
                    "   authority: " + authority + "\n" +
                    "   permission: " + permissionKey);
        }
        return toDelete.size();
    }

    public int deleteEntries()
    {
        /*
         * We don't do the full delete-remove-from-set thing here.  Just delete each child entity
         * and then clear the entry set.
         */
        
        Session session = getSession();
        List<DbAccessControlEntry> toDelete = new ArrayList<DbAccessControlEntry>(entries);
        // delete each entry
        for (DbAccessControlEntry entry : toDelete)
        {
            session.delete(entry);
        }
        // clear the list
        int count = entries.size();
        entries.clear();
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Deleted " + count + " access entries for access control list " + this.id);
        }
        return count;
    }

    public DbAccessControlEntry getEntry(String authority, DbPermissionKey permissionKey)
    {
        for (DbAccessControlEntry entry : entries)
        {
            DbAuthority authorityEntity = entry.getAuthority();
            DbPermission permissionEntity = entry.getPermission();
            // check for a match
            if (authorityEntity.getRecipient().equals(authority)
                    && permissionEntity.getKey().equals(permissionKey))
            {
                // found it
                return entry;
            }
        }
        return null;
    }

    public DbAccessControlEntryImpl newEntry(DbPermission permission, DbAuthority authority, boolean allowed)
    {
        DbAccessControlEntryImpl accessControlEntry = new DbAccessControlEntryImpl();
        // fill
        accessControlEntry.setAccessControlList(this);
        accessControlEntry.setPermission(permission);
        accessControlEntry.setAuthority(authority);
        accessControlEntry.setAllowed(allowed);
        // save it
        getSession().save(accessControlEntry);
        // maintain inverse set on the acl
        getEntries().add(accessControlEntry);
        // done
        return accessControlEntry;
    }
    
    /**
     * Make a copy of this ACL.
     * @return The copy.
     */
    public DbAccessControlList getCopy()
    {
        DbAccessControlList newAcl =
            new DbAccessControlListImpl();
        getSession().save(newAcl);
        for (DbAccessControlEntry entry : entries)
        {
            newAcl.newEntry(entry.getPermission(), entry.getAuthority(), entry.isAllowed());
        }
        return newAcl;
    }
}
