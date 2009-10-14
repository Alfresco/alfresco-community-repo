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
import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.repo.node.db.hibernate.HibernateNodeDaoServiceImpl;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class PersonDaoImpl extends HibernateDaoSupport implements PersonDao
{
    private static final String QUERY_PERSON_GET_PERSON_IGNORE_CASE = "person.getPersonIgnoreCase";

    private static final String QUERY_PERSON_GET_ALL_PEOPLE = "person.getAllPeople";

    private QNameDAO qnameDAO;
    private Long assocTypeQNameID;
    private Long qNamePropId;
    private Long qNameTypeId;
    private LocaleDAO localeDAO;
    private ContentDataDAO contentDataDAO;
    private DictionaryService dictionaryService;

    private StoreRef storeRef;

    private TenantService tenantService;

    public void setStoreUrl(String storeUrl)
    {
        this.storeRef = new StoreRef(storeUrl);
    }

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    public void init()
    {
        assocTypeQNameID = qnameDAO.getOrCreateQName(ContentModel.ASSOC_CHILDREN).getFirst();
        qNamePropId = qnameDAO.getOrCreateQName(ContentModel.PROP_USERNAME).getFirst();
        qNameTypeId = qnameDAO.getOrCreateQName(ContentModel.TYPE_PERSON).getFirst();
    }

    @SuppressWarnings("unchecked")
    public List<NodeRef> getPersonOrNull(final String searchUserName, UserNameMatcher matcher)
    {
        /*
         * Related JIRA:
         *    https://issues.alfresco.com/jira/browse/MOB-387
         *    https://issues.alfresco.com/jira/browse/ETHREEOH-1431
         *    https://issues.alfresco.com/jira/browse/ETWOTWO-1012
         * 
         * When usernames are case-insensitive, it could happen that the DB is NOT.
         * DB queries should therefore return values regardless of the DB collation.
         * The original, case-preserving username is stored with the node properties.
         * To solve the query issue, a LOWERCASE version of the username is stored on
         * the path (alf_child_assoc.qname_localname).  This is queried for using the
         * lowercase version of the searched-for username.  The case results pruning
         * is done (as it always was) as a post-search task.
         * 
         * Note that the upgrade scripts had to change to force lowercase names as well.
         */
        
        final StoreRef personStoreRef = tenantService.getName(storeRef);

        List<NodeRef> answer = new ArrayList<NodeRef>();

        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                SQLQuery query = (SQLQuery) session.getNamedQuery(QUERY_PERSON_GET_PERSON_IGNORE_CASE);
                query.setParameter("assocTypeQNameID", assocTypeQNameID);
                query.setParameter("qnamePropId", qNamePropId);
                query.setParameter("qnameTypeId", qNameTypeId);
                query.setParameter("userNameLowerCase", searchUserName.toLowerCase());      // Lowercase: ETHREEOH-1431
                query.setParameter("False", Boolean.FALSE);
                query.setParameter("storeProtocol", personStoreRef.getProtocol());
                query.setParameter("storeIdentifier", personStoreRef.getIdentifier());
                return query.list();
            }
        };

        List<Node> results = (List<Node>) getHibernateTemplate().execute(callback);

        for (Node node : results)
        {
            NodeRef nodeRef = node.getNodeRef();
            Map<PropertyMapKey, NodePropertyValue> nodeProperties = node.getProperties();

            // Convert the QName IDs
            Map<QName, Serializable> converted = HibernateNodeDaoServiceImpl.convertToPublicProperties(
                    nodeProperties, qnameDAO, localeDAO, contentDataDAO, dictionaryService);

            Serializable value = converted.get(ContentModel.PROP_USERNAME);
            String realUserName = DefaultTypeConverter.INSTANCE.convert(String.class, value);

            if (matcher.matches(searchUserName, realUserName))
            {
                answer.add(nodeRef);
            }

        }
        return answer;

    }

    @SuppressWarnings("unchecked")
    public Set<NodeRef> getAllPeople()
    {
        final StoreRef personStoreRef = tenantService.getName(storeRef);

        Set<NodeRef> answer = new HashSet<NodeRef>();

        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                SQLQuery query = (SQLQuery) session.getNamedQuery(QUERY_PERSON_GET_ALL_PEOPLE);
                query.setParameter("qnamePropId", qNamePropId);
                query.setParameter("qnameTypeId", qNameTypeId);
                query.setParameter("False", Boolean.FALSE);
                query.setParameter("storeProtocol", personStoreRef.getProtocol());
                query.setParameter("storeIdentifier", personStoreRef.getIdentifier());
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

    public void setContentDataDAO(ContentDataDAO contentDataDAO)
    {
        this.contentDataDAO = contentDataDAO;
    }

}
