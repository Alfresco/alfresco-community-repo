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
package org.alfresco.repo.security.person;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.LocaleDAO;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.NodePropertyValue;
import org.alfresco.repo.domain.PropertyMapKey;
import org.alfresco.repo.domain.QNameDAO;
import org.alfresco.repo.domain.hibernate.NodeImpl;
import org.alfresco.repo.node.db.hibernate.HibernateNodeDaoServiceImpl;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class PersonDaoImpl extends HibernateDaoSupport implements PersonDao
{
    private static final String PERSON_GET_PERSON = "person.getPerson";

    private static final String PERSON_GET_ALL_PEOPLE = "person.getAllPeople";

    private QNameDAO qnameDAO;

    private Long qNameId;

    private LocaleDAO localeDAO;

    private DictionaryService dictionaryService;

    @SuppressWarnings("unchecked")
    public List<NodeRef> getPersonOrNull(final String searchUserName, boolean userNamesAreCaseSensitive)
    {
        List<NodeRef> answer = new ArrayList<NodeRef>();

        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                SQLQuery query = getSession().createSQLQuery("SELECT {n.*} FROM alf_node n JOIN alf_node_properties p ON n.id = p.node_id JOIN alf_child_assoc c on c.child_node_id = n.id WHERE c.qname_localname = :userName1 AND p.qname_id = :qnameId AND p.string_value = :userName2");
                query.addEntity("n", NodeImpl.class);
                query.setParameter("qnameId", qNameId);
                query.setParameter("userName1", searchUserName);
                query.setParameter("userName2", searchUserName);
                return query.list();
            }
        };

        List<Node> results = (List<Node>) getHibernateTemplate().execute(callback);

        for (Node node : results)
        {
            NodeRef nodeRef = node.getNodeRef();
            Map<PropertyMapKey, NodePropertyValue> nodeProperties = node.getProperties();

            // Convert the QName IDs
            Map<QName, Serializable> converted = HibernateNodeDaoServiceImpl.convertToPublicProperties(nodeProperties, qnameDAO, localeDAO, dictionaryService);

            Serializable value = converted.get(ContentModel.PROP_USERNAME);
            String realUserName = DefaultTypeConverter.INSTANCE.convert(String.class, value);

            if (userNamesAreCaseSensitive)
            {
                if (realUserName.equals(searchUserName))
                {
                    answer.add(nodeRef);
                }
            }
            else
            {
                if (realUserName.equalsIgnoreCase(searchUserName))
                {
                    answer.add(nodeRef);
                }
            }

        }
        return answer;

    }

    public void init()
    {
        qNameId = qnameDAO.getOrCreateQNameEntity(ContentModel.PROP_USERNAME).getId();
    }

    @SuppressWarnings("unchecked")
    public Set<NodeRef> getAllPeople()
    {
        Set<NodeRef> answer = new HashSet<NodeRef>();

        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                SQLQuery query = getSession().createSQLQuery("SELECT {n.*} FROM alf_node n JOIN alf_node_properties p ON n.id = p.node_id WHERE p.qname_id = :qnameId");
                query.addEntity("n", NodeImpl.class);
                query.setParameter("qnameId", qNameId);
                return query.list();
            }
        };

        List<Node> results = (List<Node>) getHibernateTemplate().execute(callback);

        for (Node node : results)
        {
            NodeRef nodeRef = node.getNodeRef();
            answer.add(nodeRef);
        }
        return answer;

    }

    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

    public void setLocaleDAO(LocaleDAO localeDAO)
    {
        this.localeDAO = localeDAO;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

}
