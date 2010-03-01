/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.attributes.hibernate;

import java.util.List;

import org.alfresco.repo.attributes.Attribute;
import org.alfresco.repo.attributes.AttributeDAO;
import org.alfresco.repo.attributes.GlobalAttributeEntry;
import org.alfresco.repo.attributes.GlobalAttributeEntryDAO;
import org.alfresco.repo.attributes.GlobalAttributeEntryImpl;
import org.alfresco.repo.domain.hibernate.DirtySessionMethodInterceptor;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate implementation of Global Attribute Entries.
 * @author britt
 */
public class GlobalAttributeEntryDAOHibernate extends HibernateDaoSupport
        implements GlobalAttributeEntryDAO
{
    private AttributeDAO fAttributeDAO;
    
    public GlobalAttributeEntryDAOHibernate()
    {
    }
    
    public void setAttributeDao(AttributeDAO dao)
    {
        fAttributeDAO = dao;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.GlobalAttributeEntryDAO#delete(org.alfresco.repo.attributes.GlobalAttributeEntry)
     */
    public void delete(GlobalAttributeEntry entry)
    {
        Attribute attr = entry.getAttribute();
        getSession().delete(entry);
        fAttributeDAO.delete(attr);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.GlobalAttributeEntryDAO#delete(java.lang.String)
     */
    public void delete(String name)
    {
        delete((GlobalAttributeEntry)getSession().get(GlobalAttributeEntryImpl.class, name));
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.GlobalAttributeEntryDAO#get(java.lang.String)
     */
    public GlobalAttributeEntry get(String name)
    {
        return (GlobalAttributeEntry)getSession().get(GlobalAttributeEntryImpl.class, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.GlobalAttributeEntryDAO#save(org.alfresco.repo.attributes.GlobalAttributeEntry)
     */
    public void save(GlobalAttributeEntry entry)
    {
        getSession().save(entry);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.GlobalAttributeEntryDAO#getKeys()
     */
    @SuppressWarnings("unchecked")
    public List<String> getKeys()
    {
        Query query = getSession().createQuery("select gae.name from GlobalAttributeEntryImpl gae");
        DirtySessionMethodInterceptor.setQueryFlushMode(getSession(), query);
        return (List<String>)query.list();
    }
}
