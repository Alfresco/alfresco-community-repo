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

package org.alfresco.repo.avm.hibernate;

import java.util.List;

import org.alfresco.repo.avm.VersionLayeredNodeEntry;
import org.alfresco.repo.avm.VersionLayeredNodeEntryDAO;
import org.alfresco.repo.avm.VersionRoot;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate implementation of DAO for VersionLayeredNodeEntries.
 * @author britt
 */
public class VersionLayeredNodeEntryDAOHibernate extends HibernateDaoSupport
        implements VersionLayeredNodeEntryDAO
{
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.VersionLayeredNodeEntryDAO#delete(org.alfresco.repo.avm.VersionRoot)
     */
    public void delete(VersionRoot version)
    {
        Query query = getSession().createQuery("delete from VersionLayeredNodeEntryImpl vln where vln.version = :version");
        query.setEntity("version", version);
        query.executeUpdate();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.VersionLayeredNodeEntryDAO#get(org.alfresco.repo.avm.VersionRoot)
     */
    @SuppressWarnings("unchecked")
    public List<VersionLayeredNodeEntry> get(VersionRoot version)
    {
        Query query = getSession().createQuery("from VersionLayeredNodeEntryImpl vln " +
                                               "where vln.version = :version");
        query.setEntity("version", version);
        return (List<VersionLayeredNodeEntry>)query.list();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.VersionLayeredNodeEntryDAO#save(org.alfresco.repo.avm.VersionLayeredNodeEntry)
     */
    public void save(VersionLayeredNodeEntry entry)
    {
        getSession().save(entry);
    }
}
