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
package org.alfresco.repo.admin.patch.impl;

import java.util.List;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.DbAccessControlEntry;
import org.alfresco.repo.domain.DbPermission;
import org.alfresco.repo.domain.hibernate.DbPermissionImpl;
import org.alfresco.service.namespace.QName;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Provides common functionality to change a permission type and/or name.
 *
 * @author Derek Hulley
 */
public abstract class AbstractPermissionChangePatch extends AbstractPatch
{
    private HibernateHelper helper;
    
    public AbstractPermissionChangePatch()
    {
        helper = new HibernateHelper();
    }
    
    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.helper.setSessionFactory(sessionFactory);
    }

    /**
     * Helper method to rename (move) a permission.  This involves checking for the existence of the
     * new permission and then moving all the entries to point to the new permission.
     * 
     * @param oldTypeQName the old permission type
     * @param oldName the old permission name
     * @param newTypeQName the new permission type
     * @param newName the new permission name
     * @return Returns the number of permission entries modified
     */
    protected int renamePermission(QName oldTypeQName, String oldName, QName newTypeQName, String newName)
    {
        return helper.createAndUpdatePermission(oldTypeQName, oldName, newTypeQName, newName);
    }

    /** Helper to get a permission entity */
    private static class GetPermissionCallback implements HibernateCallback
    {
        private QName typeQName;
        private String name;
        public GetPermissionCallback(QName typeQName, String name)
        {
            this.typeQName = typeQName;
            this.name = name;
        }
        public Object doInHibernate(Session session)
        {
            // flush any outstanding entities
            session.flush();
            
            Query query = session.getNamedQuery(HibernateHelper.QUERY_GET_PERMISSION);
            query.setParameter("permissionTypeQName", typeQName)
                 .setString("permissionName", name);
            return query.uniqueResult();
        }
    }
    
    private static class HibernateHelper extends HibernateDaoSupport
    {
        private static final String QUERY_GET_PERMISSION = "permission.GetPermission";
        private static final String QUERY_GET_ENTRIES_TO_CHANGE = "permission.patch.GetAccessControlEntriesToChangePermissionOn";
        
        public int createAndUpdatePermission(
                final QName oldTypeQName,
                final String oldName,
                final QName newTypeQName,
                final String newName)
        {
            if (oldTypeQName.equals(newTypeQName) && oldName.equals(newName))
            {
                throw new IllegalArgumentException("Cannot move permission to itself: " + oldTypeQName + "-" + oldName);
            }
            
            HibernateCallback getNewPermissionCallback = new GetPermissionCallback(newTypeQName, newName);
            DbPermission permission = (DbPermission) getHibernateTemplate().execute(getNewPermissionCallback);
            if (permission == null)
            {
                // create the permission
                permission = new DbPermissionImpl();
                permission.setTypeQname(newTypeQName);
                permission.setName(newName);
                // save
                getHibernateTemplate().save(permission);
            }
            final DbPermission newPermission = permission;
            // now update all entries that refer to the old permission
            HibernateCallback updateEntriesCallback = new HibernateCallback()
            {
                private static final int MAX_RESULTS = 1000;
                @SuppressWarnings("unchecked")
                public Object doInHibernate(Session session)
                {
                    int count = 0;
                    while (true)
                    {
                        // flush any outstanding entities
                        session.flush();
                        
                        Query query = session.getNamedQuery(HibernateHelper.QUERY_GET_ENTRIES_TO_CHANGE);
                        query.setParameter("oldTypeQName", oldTypeQName)
                             .setParameter("oldName", oldName)
                             .setMaxResults(MAX_RESULTS);
                        List<DbAccessControlEntry> entries = (List<DbAccessControlEntry>) query.list();
                        // if there are no results, then we're done
                        if (entries.size() == 0)
                        {
                            break;
                        }
                        for (DbAccessControlEntry entry : entries)
                        {
                            entry.setPermission(newPermission);
                            count++;
                            session.evict(entry);
                        }
                        // flush and evict all the entries
                        session.flush();
                        for (DbAccessControlEntry entry : entries)
                        {
                            session.evict(entry);
                        }
                        // next set of results
                    }
                    // done
                    return count;
                }
            };
            int updateCount = (Integer) getHibernateTemplate().execute(updateEntriesCallback);
            // now delete the old permission
            HibernateCallback getOldPermissionCallback = new GetPermissionCallback(oldTypeQName, oldName);
            DbPermission oldPermission = (DbPermission) getHibernateTemplate().execute(getOldPermissionCallback);
            if (oldPermission != null)
            {
                getHibernateTemplate().delete(oldPermission);
            }
            // done
            return updateCount;
        }
    }
}
