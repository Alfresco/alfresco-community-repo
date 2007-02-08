/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.domain.hibernate;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.repo.domain.DbAuthority;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CallbackException;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * The persisted class for authorities.
 * 
 * @author andyh
 */
public class DbAuthorityImpl extends LifecycleAdapter
    implements DbAuthority, Serializable
{
    private static final long serialVersionUID = -5582068692208928127L;
    
    private static Log logger = LogFactory.getLog(DbAuthorityImpl.class);

    private String recipient;
    private Set<String> externalKeys;

    public DbAuthorityImpl()
    {
        externalKeys = new HashSet<String>();
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(!(o instanceof DbAuthority))
        {
            return false;
        }
        DbAuthority other = (DbAuthority)o;
        return this.getRecipient().equals(other.getRecipient());
    }

    @Override
    public int hashCode()
    {
        return getRecipient().hashCode();
    }

    public int deleteEntries()
    {
        /*
         * This can use a delete direct to the database as well, but then care must be taken
         * to evict the instances from the session.
         */
        
        // bypass L2 cache and get all entries for this list
        Query query = getSession()
                .getNamedQuery(PermissionsDaoComponentImpl.QUERY_GET_AC_ENTRIES_FOR_AUTHORITY)
                .setString("authorityRecipient", this.recipient);
        int count = HibernateHelper.deleteDbAccessControlEntries(getSession(), query);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Deleted " + count + " access entries for access control list " + this.recipient);
        }
        return count;
    }

    /**
     * Ensures that all this access control list's entries have been deleted.
     */
    public boolean onDelete(Session session) throws CallbackException
    {
        deleteEntries();
        return super.onDelete(session);
    }

    public String getRecipient()
    {
        return recipient;
    }

    public void setRecipient(String recipient)
    {
       this.recipient = recipient;
    }

    public Set<String> getExternalKeys()
    {
        return externalKeys;
    }

    // Hibernate
    /* package */ void setExternalKeys(Set<String> externalKeys)
    {
        this.externalKeys = externalKeys;
    }
    
    /**
     * Helper method to find an authority based on its natural key
     * 
     * @param session the Hibernate session to use
     * @param authority the authority name
     * @return Returns an existing instance or null if not found
     */
    public static DbAuthority find(Session session, String authority)
    {
        return (DbAuthority) session.get(DbAuthorityImpl.class, authority);
    }
}
