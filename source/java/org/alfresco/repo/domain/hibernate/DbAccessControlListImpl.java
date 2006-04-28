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

import org.alfresco.repo.domain.DbAccessControlList;
import org.alfresco.repo.domain.Node;
import org.alfresco.util.EqualsHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CallbackException;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * The hibernate persisted class for node permission entries.
 * 
 * @author andyh
 */
public class DbAccessControlListImpl extends LifecycleAdapter implements DbAccessControlList
{
    private static Log logger = LogFactory.getLog(DbAccessControlListImpl.class);

    private long id;
    private Node node;
    private boolean inherits;
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("DbAccessControlListImpl")
          .append("[ id=").append(id)
          .append(", node=").append(node)
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

        return (this.inherits == other.getInherits())
                && (EqualsHelper.nullSafeEquals(this.node, other.getNode()));
    }

    @Override
    public int hashCode()
    {
        return (node == null ? 0 : node.hashCode());
    }

    public int deleteEntries()
    {
        /*
         * This can use a delete direct to the database as well, but then care must be taken
         * to evict the instances from the session.
         */
        
        // bypass L2 cache and get all entries for this list
        Query query = getSession()
                .getNamedQuery(PermissionsDaoComponentImpl.QUERY_GET_AC_ENTRIES_FOR_AC_LIST)
                .setLong("accessControlListId", this.id);
        int count = HibernateHelper.deleteQueryResults(getSession(), query);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Deleted " + count + " access entries for access control list " + this.id);
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
     * Hibernate use
     */
    @SuppressWarnings("unused")
    private void setId(long id)
    {
        this.id = id;
    }

    public Node getNode()
    {
        return node;
    }
    
    public void setNode(Node node)
    {
        this.node = node;
    }
    
    public DbAccessControlListImpl()
    {
        super();
    }

    public boolean getInherits()
    {
        return inherits;
    }

    public void setInherits(boolean inherits)
    {
        this.inherits = inherits;
    }
}
