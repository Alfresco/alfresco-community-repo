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
package org.alfresco.repo.admin.patch.impl;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.NodePropertyValue;
import org.alfresco.repo.domain.PropertyMapKey;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Certain content models make extensive use of the d:any datatype, which has led
 * to storage of simple types as serialized instances.
 * This patch ensures that all previously serializable values are stored in their
 * more native form in the database.<br>
 * e.g. If a property was d:any and a string was written ("ABC"),
 * then the value was stored in serializable_value. Instead, the newer code stores
 * the value in string_value. None of the retrieval code is affected, but the values
 * are made visible to queries, in addition to reducing the size of the node_properties
 * table.  This patch ensures that previously-stored values are changed to conform
 * to the new storage mechanism.
 * <p>
 * JIRA: {@link http://www.alfresco.org/jira/browse/AR-359 AR-359}
 * 
 * @see org.alfresco.repo.domain.PropertyValue
 * 
 * @author Derek Hulley
 */
public class NodePropertySerializablePatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.fixNodeSerializableValues.result";
    
    private HibernateHelper helper;
    
    public NodePropertySerializablePatch()
    {
        helper = new HibernateHelper();
    }
    
    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.helper.setSessionFactory(sessionFactory);
    }

    @Override
    protected String applyInternal() throws Exception
    {
        int updatedEntries = helper.fixSerializableProperties();
        
        // build the result message
        String msg = I18NUtil.getMessage(MSG_SUCCESS, updatedEntries);
        // done
        return msg;
    }

    private static class HibernateHelper extends HibernateDaoSupport
    {
        private static final String QUERY_GET_NODES = "node.patch.GetNodesWithPersistedSerializableProperties";
        
        public int fixSerializableProperties()
        {
            HibernateCallback callback = new HibernateCallback()
            {
                @SuppressWarnings("unchecked")
                public Object doInHibernate(Session session)
                {
                    Query query = session.getNamedQuery(HibernateHelper.QUERY_GET_NODES);
                    Iterator<Node> iterator = query.iterate();
                    // iterate over the nodes
                    int count = 0;
                    while (iterator.hasNext())
                    {
                        Node node = iterator.next();
                        // retrieve the node properties
                        Map<PropertyMapKey, NodePropertyValue> properties = node.getProperties();
                        // check each property
                        for (Map.Entry<PropertyMapKey, NodePropertyValue> entry : properties.entrySet())
                        {
                            NodePropertyValue propertyValue = entry.getValue();
                            if (propertyValue.getSerializableValue() == null)
                            {
                                // the property was not persisted as a serializable - nothing to do
                                continue;
                            }
                            else if (!"SERIALIZABLE".equals(propertyValue.getActualTypeString()))
                            {
                                // only handle actual types that were pushed in as any old type
                                continue;
                            }
                            // make sure that this value is persisted correctly
                            Serializable value = propertyValue.getSerializableValue();
                            // put it back
                            NodePropertyValue newPropertyValue = new NodePropertyValue(DataTypeDefinition.ANY, value);
                            entry.setValue(newPropertyValue);
                            count++;
                        }
                    }
                    return new Integer(count);
                }
            };
            Integer updateCount = (Integer) getHibernateTemplate().execute(callback);
            // done
            return updateCount.intValue();
        }
    }
}
