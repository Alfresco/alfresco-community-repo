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

import org.alfresco.repo.domain.DbPermission;
import org.alfresco.repo.domain.DbPermissionKey;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CallbackException;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * The persisted class for permissions.
 * 
 * @author andyh
 */
public class DbPermissionImpl extends LifecycleAdapter
    implements DbPermission, Serializable
{   
    private static final long serialVersionUID = -6352566900815035461L;
    
    private static Log logger = LogFactory.getLog(DbPermissionImpl.class);

    private long id;
    private QName typeQname;
    private String name;

    public DbPermissionImpl()
    {
        super();
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("PermissionImpl")
          .append("[ id=").append(id)
          .append(", typeQname=").append(typeQname)
          .append(", name=").append(getName())
          .append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(!(o instanceof DbPermission))
        {
            return false;
        }
        DbPermission other = (DbPermission)o;
        return (EqualsHelper.nullSafeEquals(typeQname, other.getTypeQname()))
                && (EqualsHelper.nullSafeEquals(name, other.getName())); 
    }

    @Override
    public int hashCode()
    {
        return typeQname.hashCode() + (37 * name.hashCode());
    }
    
    public int deleteEntries()
    {
        /*
         * This can use a delete direct to the database as well, but then care must be taken
         * to evict the instances from the session.
         */
        
        // bypass L2 cache and get all entries for this list
        Query query = getSession()
                .getNamedQuery(PermissionsDaoComponentImpl.QUERY_GET_AC_ENTRIES_FOR_PERMISSION)
                .setSerializable("permissionId", this.id);
        int count = HibernateHelper.deleteDbAccessControlEntries(getSession(), query);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Deleted " + count + " access entries for permission " + this.id);
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

    public long getId()
    {
        return id;
    }

    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setId(long id)
    {
        this.id = id;
    }

    public QName getTypeQname()
    {
        return typeQname;
    }

    public void setTypeQname(QName typeQname)
    {
        this.typeQname = typeQname;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    public DbPermissionKey getKey()
    {
        return new DbPermissionKey(typeQname, name);
    }

    /**
     * Helper method to find a permission based on its natural key
     * 
     * @param session the Hibernate session to use
     * @param qname the type qualified name
     * @param name the name of the permission
     * @return Returns an existing instance or null if not found
     */
    public static DbPermission find(Session session, QName qname, String name)
    {
        Query query = session
                .getNamedQuery(PermissionsDaoComponentImpl.QUERY_GET_PERMISSION)
                .setString("permissionTypeQName", qname.toString())
                .setString("permissionName", name);
        return (DbPermission) query.uniqueResult();
    }
}
