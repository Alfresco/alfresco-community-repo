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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.attributes.AttrQueryHelperImpl;
import org.alfresco.repo.attributes.Attribute;
import org.alfresco.repo.attributes.AttributeDAO;
import org.alfresco.repo.attributes.ListAttribute;
import org.alfresco.repo.attributes.ListEntryDAO;
import org.alfresco.repo.attributes.MapAttribute;
import org.alfresco.repo.attributes.MapEntry;
import org.alfresco.repo.attributes.MapEntryDAO;
import org.alfresco.repo.attributes.Attribute.Type;
import org.alfresco.service.cmr.attributes.AttrQuery;
import org.alfresco.service.cmr.attributes.AttrQueryHelper;
import org.alfresco.util.Pair;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate implementation of persistence for Attributes.
 * @author britt
 */
public class AttributeDAOHibernate extends HibernateDaoSupport implements
        AttributeDAO
{
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
            Collection<Attribute> attrs = map.values();
            fMapEntryDAO.delete(map);
            for (Attribute subAttr : attrs)
            {
                delete(subAttr);
            }
        }
        if (attr.getType() == Type.LIST)
        {
            List<Attribute> children = new ArrayList<Attribute>();
            for (Attribute child : attr)
            {
                children.add(child);
            }
            fListEntryDAO.delete((ListAttribute)attr);
            for (Attribute child : children)
            {
                delete(child);
            }
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
        String fullQuery = "from MapEntryImpl me where me.map = :map and " + predicate;
        Query hQuery = getSession().createQuery(fullQuery);
        hQuery.setEntity("map", map);
        for (Map.Entry<String, String> param : helper.getParameters().entrySet())
        {
            hQuery.setParameter(param.getKey(), param.getValue());
        }
        List<MapEntry> hits = (List<MapEntry>)hQuery.list();
        List<Pair<String, Attribute>> result = new ArrayList<Pair<String, Attribute>>();
        for (MapEntry entry : hits)
        {
            result.add(new Pair<String, Attribute>(entry.getKey(), entry.getAttribute()));
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeDAO#save(org.alfresco.repo.attributes.Attribute)
     */
    public void save(Attribute attr)
    {
        getSession().save(attr);
    }
}
