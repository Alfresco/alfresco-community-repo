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
 * Hibernate implementation of a CapabilityEntryDAO.
 * @author britt
 */
public class CapabilityEntryDAOHibernate extends HibernateDaoSupport implements CapabilityEntryDAO
{
    /* (non-Javadoc)
     * @see org.alfresco.repo.simple.permission.CapabilityEntryDAO#get(java.lang.String)
     */
    public CapabilityEntry get(String name)
    {
        Query query = getSession().createQuery("from CapabilityEntryImpl ce where ce.name = :name");
        query.setString("name", name);
        return (CapabilityEntry)query.uniqueResult();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.simple.permission.CapabilityEntryDAO#getAll()
     */
    @SuppressWarnings("unchecked")
    public List<CapabilityEntry> getAll()
    {
        Query query = getSession().createQuery("from CapabilityEntryImpl ce");
        return (List<CapabilityEntry>)query.list();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.simple.permission.CapabilityEntryDAO#save(org.alfresco.repo.simple.permission.CapabilityEntry)
     */
    public void save(CapabilityEntry entry)
    {
        getSession().save(entry);
        getSession().flush();
    }
}
