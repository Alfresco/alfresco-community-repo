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

package org.alfresco.repo.attributes.hibernate;

import java.util.List;

import org.alfresco.repo.attributes.MapAttribute;
import org.alfresco.repo.attributes.MapEntry;
import org.alfresco.repo.attributes.MapEntryDAO;
import org.alfresco.repo.attributes.MapEntryImpl;
import org.alfresco.repo.attributes.MapEntryKey;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate implementation of persistence for MapEntries.
 * @author britt
 */
public class MapEntryDAOHibernate extends HibernateDaoSupport implements
        MapEntryDAO
{
    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.MapEntryDAO#delete(org.alfresco.repo.attributes.MapEntry)
     */
    public void delete(MapEntry entry)
    {
        getSession().delete(entry);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.MapEntryDAO#delete(org.alfresco.repo.attributes.MapAttribute)
     */
    public void delete(MapAttribute mapAttr)
    {
        Query query = getSession().createQuery("delete from MapEntryImpl me where me.key.map = :map");
        query.setEntity("map", mapAttr);
        query.executeUpdate();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.MapEntryDAO#get(org.alfresco.repo.attributes.MapAttribute, java.lang.String)
     */
    public MapEntry get(MapEntryKey key)
    {
        return (MapEntry)getSession().get(MapEntryImpl.class, key);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.MapEntryDAO#get(org.alfresco.repo.attributes.MapAttribute)
     */
    @SuppressWarnings("unchecked")
    public List<MapEntry> get(MapAttribute mapAttr)
    {
        Query query = getSession().createQuery("from MapEntryImpl me where me.key.map = :map");
        query.setEntity("map", mapAttr);
        return (List<MapEntry>)query.list();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.MapEntryDAO#save(org.alfresco.repo.attributes.MapEntry)
     */
    public void save(MapEntry entry)
    {
        getSession().save(entry);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.MapEntryDAO#size(org.alfresco.repo.attributes.MapAttribute)
     */
    public int size(MapAttribute mapAttr)
    {
        Query query = getSession().createQuery("select count(*) from MapEntryImpl me where me.key.map = :map");
        query.setEntity("map", mapAttr);
        return ((Long)query.uniqueResult()).intValue();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.MapEntryDAO#evict(org.alfresco.repo.attributes.MapEntry)
     */
    public void evict(MapEntry entry)
    {
    }
}
