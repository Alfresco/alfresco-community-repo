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
package org.alfresco.repo.usage.hibernate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.domain.Node;
import org.alfresco.repo.transaction.TransactionalDao;
import org.alfresco.repo.usage.UsageDelta;
import org.alfresco.repo.usage.UsageDeltaDAO;
import org.alfresco.util.GUID;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate-specific implementation of the persistence-independent <b>Usage Delta</b> DAO interface
 * 
 */
public class HibernateUsageDeltaDAO extends HibernateDaoSupport implements UsageDeltaDAO, TransactionalDao
{
    private static final String QUERY_GET_DELTAS = "usage.GetDeltas";
    private static final String QUERY_GET_TOTAL_DELTA_SIZE = "usage.GetTotalDeltaSize";
    private static final String QUERY_GET_USAGE_DELTA_NODES = "usage.GetUsageDeltaNodes";
        
    /** a uuid identifying this unique instance */
    private final String uuid;
    

    /**
     * 
     */
    public HibernateUsageDeltaDAO()
    {
        this.uuid = GUID.generate();
    }

    /**
     * Checks equality by type and uuid
     */
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (!(obj instanceof HibernateUsageDeltaDAO))
        {
            return false;
        }
        HibernateUsageDeltaDAO that = (HibernateUsageDeltaDAO) obj;
        return this.uuid.equals(that.uuid);
    }
    
    /**
     * @see #uuid
     */
    public int hashCode()
    {
        return uuid.hashCode();
    }

    /**
     * NO-OP
     */
    public void beforeCommit()
    {
    }   

    /**
     * Does this <tt>Session</tt> contain any changes which must be
     * synchronized with the store?
     * 
     * @return true => changes are pending
     */
    public boolean isDirty()
    {
        // create a callback for the task
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                return session.isDirty();
            }
        };
        // execute the callback
        return ((Boolean)getHibernateTemplate().execute(callback)).booleanValue();
    }

    /**
     * Just flushes the session
     */
    public void flush()
    {
        getSession().flush();
    }

    @SuppressWarnings("unchecked")
    public int deleteDeltas(final Node node)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_DELTAS);
                query.setParameter("node", node);
                return query.list();
            }
        };
        
        // execute
        List<UsageDelta> queryResults = (List<UsageDelta>)getHibernateTemplate().execute(callback);
        
        for (UsageDelta usageDelta : queryResults)
        {
            getHibernateTemplate().delete(usageDelta);
        }
        
        return queryResults.size();
    }
    
    @SuppressWarnings("unchecked")
    public long getTotalDeltaSize(final Node node)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_TOTAL_DELTA_SIZE);
                query.setParameter("node", node);
                query.setReadOnly(true);
                return query.uniqueResult();
            }
        };
        // execute read-only tx
        Long queryResult = (Long)getHibernateTemplate().execute(callback);
       
        return (queryResult == null ? 0 : queryResult);
    }
    
    public void insertDelta(UsageDelta deltaInfo)
    {
        // Save
        getSession().save(deltaInfo);
    }
    
    @SuppressWarnings("unchecked")
    public Set<Node> getUsageDeltaNodes()
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_USAGE_DELTA_NODES);
                query.setReadOnly(true);
                return query.list();
            }
        };
        // execute read-only tx
        List<Node> queryResults = (List<Node>)getHibernateTemplate().execute(callback);
        Set<Node> results = new HashSet<Node>(queryResults.size());
        for (Node node : queryResults)
        {
            results.add(node);
        }
        return results;
    }
}