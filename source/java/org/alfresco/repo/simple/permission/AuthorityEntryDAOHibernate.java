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
 * http://www.alfresco.com/legal/licensing
 */

package org.alfresco.repo.simple.permission;

import java.util.List;

import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate DAO for Authority Entries.
 * @author britt
 */
public class AuthorityEntryDAOHibernate extends HibernateDaoSupport implements
        AuthorityEntryDAO
{
    /* (non-Javadoc)
     * @see org.alfresco.repo.simple.permission.AuthorityEntryDAO#get(java.lang.String)
     */
    public AuthorityEntry get(String name)
    {
        Query query = getSession().createQuery("from AuthorityEntryImpl ae where ae.name = :name");
        query.setString("name", name);
        return (AuthorityEntry)query.uniqueResult();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.simple.permission.AuthorityEntryDAO#get(int)
     */
    public AuthorityEntry get(int id)
    {
        return (AuthorityEntry)getSession().get(AuthorityEntryImpl.class, id);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.simple.permission.AuthorityEntryDAO#getRoots()
     */
    @SuppressWarnings("unchecked")
    public List<AuthorityEntry> get()
    {
        Query query = getSession().createQuery("from AuthorityEntryImpl ae");
        return (List<AuthorityEntry>)query.list();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.simple.permission.AuthorityEntryDAO#save(org.alfresco.repo.simple.permission.AuthorityEntry)
     */
    public void save(AuthorityEntry entry)
    {
        getSession().save(entry);
        getSession().flush();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.simple.permission.AuthorityEntryDAO#getParents(org.alfresco.repo.simple.permission.AuthorityEntry)
     */
    @SuppressWarnings("unchecked")
    public List<AuthorityEntry> getParents(AuthorityEntry entry)
    {
        Query query = getSession().createQuery("from AuthorityEntryImpl ae where :child in elements(ae.children)");
        query.setEntity("child", entry);
        return (List<AuthorityEntry>)query.list();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.simple.permission.AuthorityEntryDAO#delete(org.alfresco.repo.simple.permission.AuthorityEntry)
     */
    public void delete(AuthorityEntry entry)
    {
        getSession().delete(entry);
    }
}
