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
public class DbAuthorityImpl extends LifecycleAdapter implements DbAuthority
{
    private static final long serialVersionUID = -5582068692208928127L;
    
    private static Log logger = LogFactory.getLog(DbAuthorityImpl.class);

    private String recipient;
    private Set<String> externalKeys = new HashSet<String>();

    public DbAuthorityImpl()
    {
        super();
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
                .setString("recipient", this.recipient);
        int count = HibernateHelper.deleteQueryResults(getSession(), query);
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
}
