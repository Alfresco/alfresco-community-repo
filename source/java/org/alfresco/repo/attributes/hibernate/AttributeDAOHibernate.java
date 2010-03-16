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

package org.alfresco.repo.attributes.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.attributes.AttrQueryHelperImpl;
import org.alfresco.repo.attributes.Attribute;
import org.alfresco.repo.attributes.AttributeDAO;
import org.alfresco.repo.attributes.ListAttribute;
import org.alfresco.repo.attributes.ListEntry;
import org.alfresco.repo.attributes.ListEntryDAO;
import org.alfresco.repo.attributes.MapAttribute;
import org.alfresco.repo.attributes.MapEntry;
import org.alfresco.repo.attributes.MapEntryDAO;
import org.alfresco.repo.attributes.Attribute.Type;
import org.alfresco.repo.domain.hibernate.DirtySessionMethodInterceptor;
import org.alfresco.service.cmr.attributes.AttrQuery;
import org.alfresco.service.cmr.attributes.AttrQueryHelper;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate implementation of persistence for Attributes.
 * @author britt
 */
public class AttributeDAOHibernate extends HibernateDaoSupport implements
        AttributeDAO
{
    private static Log fgLogger = LogFactory.getLog(AttributeDAOHibernate.class);

    private MapEntryDAO fMapEntryDAO;

    private ListEntryDAO fListEntryDAO;

    public AttributeDAOHibernate()
    {
    }

    public void setMapEntryDao(MapEntryDAO dao)
    {
        fMapEntryDAO = dao;
    }

    public void setListEntryDao(ListEntryDAO dao)
    {
        fListEntryDAO = dao;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeDAO#delete(org.alfresco.repo.attributes.Attribute)
     */
    public void delete(Attribute attr)
    {
        if (attr.getType() == Type.MAP)
        {
            MapAttribute map = (MapAttribute)attr;
            List<MapEntry> mapEntries = fMapEntryDAO.get(map);
            for (MapEntry entry : mapEntries)
            {
                Attribute subAttr = entry.getAttribute();
                fMapEntryDAO.delete(entry);
                delete(subAttr);
            }
        }
        if (attr.getType() == Type.LIST)
        {
            ListAttribute list = (ListAttribute)attr;
            List<ListEntry> listEntries = fListEntryDAO.get(list);
            for (ListEntry entry : listEntries)
            {
                Attribute subAttr = entry.getAttribute();
                fListEntryDAO.delete(entry);
                delete(subAttr);
            }
        }
        if (fgLogger.isDebugEnabled())
        {
            fgLogger.debug("Entities: " + getSession().getStatistics().getEntityCount());
        }
        getSession().delete(attr);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeDAO#find(java.lang.String, org.alfresco.service.cmr.attributes.AttrQuery)
     */
    @SuppressWarnings("unchecked")
    public List<Pair<String, Attribute>> find(MapAttribute map, AttrQuery query)
    {
        AttrQueryHelper helper = new AttrQueryHelperImpl();
        String predicate = query.getPredicate(helper);
        String fullQuery = "from MapEntryImpl me where me.key.map = :map and " + predicate;
        Query hQuery = getSession().createQuery(fullQuery);
        hQuery.setEntity("map", map);
        for (Map.Entry<String, String> param : helper.getParameters().entrySet())
        {
            hQuery.setParameter(param.getKey(), param.getValue());
        }
        DirtySessionMethodInterceptor.setQueryFlushMode(getSession(), hQuery);
        List<MapEntry> hits = (List<MapEntry>)hQuery.list();
        List<Pair<String, Attribute>> result = new ArrayList<Pair<String, Attribute>>();
        for (MapEntry entry : hits)
        {
            result.add(new Pair<String, Attribute>(entry.getKey().getKey(), entry.getAttribute()));
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeDAO#delete(org.alfresco.repo.attributes.MapAttribute, org.alfresco.service.cmr.attributes.AttrQuery)
     */
    public void delete(MapAttribute map, AttrQuery query)
    {
        List<Pair<String, Attribute>> result = find(map, query);
        for (Pair<String, Attribute> entry : result)
        {
            map.remove(entry.getFirst());
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeDAO#save(org.alfresco.repo.attributes.Attribute)
     */
    public void save(Attribute attr)
    {
        getSession().save(attr);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeDAO#evict(org.alfresco.repo.attributes.Attribute)
     */
    public void evict(Attribute attr)
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeDAO#flush()
     */
    public void flush()
    {
        getSession().flush();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeDAO#evictFlat(org.alfresco.repo.attributes.Attribute)
     */
    public void evictFlat(Attribute attr)
    {
    }
}
