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
package org.alfresco.repo.admin.patch.impl;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.DbPermission;
import org.alfresco.repo.domain.hibernate.DbPermissionImpl;
import org.alfresco.repo.domain.qname.QNameDAO;
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

    public void setQnameDAO(QNameDAO qnameDAO)
    {
        helper.setQnameDAO(qnameDAO);
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
        private Long typeQNameId;
        private String name;
        public GetPermissionCallback(Long typeQNameId, String name)
        {
            this.typeQNameId = typeQNameId;
            this.name = name;
        }
        public Object doInHibernate(Session session)
        {
            // flush any outstanding entities
            session.flush();
            
            Query query = session.getNamedQuery(HibernateHelper.QUERY_GET_PERMISSION);
            query.setLong("permissionTypeQNameId", typeQNameId)
                 .setString("permissionName", name);
            return query.uniqueResult();
        }
    }
    
    private static class HibernateHelper extends HibernateDaoSupport
    {
        private static final String QUERY_GET_PERMISSION = "permission.GetPermission";
        
        private QNameDAO qnameDAO;
        
        public void setQnameDAO(QNameDAO qnameDAO)
        {
            this.qnameDAO = qnameDAO;
        }

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
            
            // Get the QName entities
            Long oldTypeQNameId = qnameDAO.getOrCreateQName(oldTypeQName).getFirst();
            Long newTypeQNameId = qnameDAO.getOrCreateQName(newTypeQName).getFirst();
            
            HibernateCallback getNewPermissionCallback = new GetPermissionCallback(oldTypeQNameId, oldName);
            DbPermission permission = (DbPermission) getHibernateTemplate().execute(getNewPermissionCallback);
            if (permission == null)
            {
                // create the permission
                permission = new DbPermissionImpl();
                permission.setTypeQNameId(newTypeQNameId);
                permission.setName(newName);
                // save
                getHibernateTemplate().save(permission);
            }
            else
            {
                permission.setTypeQNameId(newTypeQNameId);
                permission.setName(newName);
            }
            // done
            return 1;
        }
    }
}
