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
package org.alfresco.repo.domain.hibernate;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.domain.Node;
import org.alfresco.service.cmr.repository.NodeRef;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.FetchMode;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.EntityKey;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.stat.SessionStatistics;
import org.hibernate.stat.Statistics;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author andyh
 */
public class HibernateL1CacheBulkLoader extends HibernateDaoSupport implements BulkLoader
{
    public void loadIntoCache(Collection<NodeRef> nodeRefs)
    {
        // TODO: only do if dirty.
        //getSession().flush();
        
        String[] guids = new String[nodeRefs.size()];
        int index = 0;
        for (NodeRef nodeRef : nodeRefs)
        {
            guids[index++] = nodeRef.getId();
        }

        Criteria criteria = getSession().createCriteria(NodeStatusImpl.class, "status");
        criteria.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
        criteria.add(Restrictions.in("key.guid", guids));
        criteria.createAlias("status.node", "node");
        criteria.setFetchMode("node.aspects", FetchMode.SELECT);
        criteria.setFetchMode("node.properties", FetchMode.JOIN);
        criteria.setFetchMode("node.store", FetchMode.SELECT);
        criteria.setCacheMode(CacheMode.IGNORE);
        criteria.setFlushMode(FlushMode.MANUAL);

        criteria.list();
        
        criteria = getSession().createCriteria(NodeStatusImpl.class, "status");
        criteria.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
        criteria.add(Restrictions.in("key.guid", guids));
        criteria.createAlias("status.node", "node");
        criteria.setFetchMode("node.aspects", FetchMode.JOIN);
        criteria.setFetchMode("node.properties", FetchMode.SELECT);
        criteria.setFetchMode("node.store", FetchMode.SELECT);
        criteria.setCacheMode(CacheMode.IGNORE);
        criteria.setFlushMode(FlushMode.MANUAL);

        criteria.list();
        
        
    }

    public void clear()
    {
        getSession().flush();
        getSession().clear();
        Map<String, ClassMetadata> classes = getSessionFactory().getAllClassMetadata();
        for (ClassMetadata clazz : classes.values())
        {
            getSessionFactory().evict(clazz.getMappedClass(EntityMode.POJO));
        }
        Map<String, CollectionMetadata> collections = getSessionFactory().getAllCollectionMetadata();
        for (CollectionMetadata clazz : collections.values())
        {
            getSessionFactory().evictCollection(clazz.getRole());
        }

    }
}
