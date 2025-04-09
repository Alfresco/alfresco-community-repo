/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.dom4j.Namespace;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.search.NamedQueryParameterDefinition;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public class CannedQueryDefImpl implements CannedQueryDef
{
    private static final org.dom4j.QName ELEMENT_QNAME = new org.dom4j.QName("query-definition", new Namespace(NamespaceService.ALFRESCO_PREFIX, NamespaceService.ALFRESCO_URI));

    private static final org.dom4j.QName QNAME = new org.dom4j.QName("qname", new Namespace(NamespaceService.ALFRESCO_PREFIX, NamespaceService.ALFRESCO_URI));

    private static final org.dom4j.QName LANGUAGE = new org.dom4j.QName("language", new Namespace(NamespaceService.ALFRESCO_PREFIX, NamespaceService.ALFRESCO_URI));

    private static final org.dom4j.QName QUERY = new org.dom4j.QName("query", new Namespace(NamespaceService.ALFRESCO_PREFIX, NamespaceService.ALFRESCO_URI));

    private QName qName;

    private String language;

    private Map<QName, QueryParameterDefinition> queryParameterDefs = new HashMap<QName, QueryParameterDefinition>();

    String query;

    QueryCollection container;

    public CannedQueryDefImpl(QName qName, String language, String query, List<QueryParameterDefinition> queryParameterDefs, QueryCollection container)
    {
        super();
        this.qName = qName;
        this.language = language;
        this.query = query;
        for (QueryParameterDefinition paramDef : queryParameterDefs)
        {
            this.queryParameterDefs.put(paramDef.getQName(), paramDef);
        }
        this.container = container;
    }

    public QName getQname()
    {
        return qName;
    }

    public String getLanguage()
    {
        return language;
    }

    public Collection<QueryParameterDefinition> getQueryParameterDefs()
    {
        return Collections.unmodifiableCollection(queryParameterDefs.values());
    }

    public String getQuery()
    {
        return query;
    }

    public NamespacePrefixResolver getNamespacePrefixResolver()
    {
        return container.getNamespacePrefixResolver();
    }

    public static CannedQueryDefImpl createCannedQuery(Element element, DictionaryService dictionaryService, QueryCollection container, NamespacePrefixResolver nspr)
    {
        if (element.getQName().getName().equals(ELEMENT_QNAME.getName()))
        {
            QName qName = null;
            Element qNameElement = element.element(QNAME.getName());
            if (qNameElement != null)
            {
                qName = QName.createQName(qNameElement.getText(), container.getNamespacePrefixResolver());
            }

            String language = null;
            Element languageElement = element.element(LANGUAGE.getName());
            if (languageElement != null)
            {
                language = languageElement.getText();
            }

            String query = null;
            Element queryElement = element.element(QUERY.getName());
            if (queryElement != null)
            {
                query = queryElement.getText();
            }

            List<QueryParameterDefinition> queryParameterDefs = new ArrayList<QueryParameterDefinition>();

            List list = element.elements(QueryParameterDefImpl.getElementQName().getName());
            for (Iterator it = list.iterator(); it.hasNext(); /**/)
            {
                Element defElement = (Element) it.next();
                NamedQueryParameterDefinition nqpd = QueryParameterDefImpl.createParameterDefinition(defElement, dictionaryService, nspr);
                queryParameterDefs.add(nqpd.getQueryParameterDefinition());
            }

            list = element.elements(QueryParameterRefImpl.getElementQName().getName());
            for (Iterator it = list.iterator(); it.hasNext(); /**/)
            {
                Element refElement = (Element) it.next();
                NamedQueryParameterDefinition nqpd = QueryParameterRefImpl.createParameterReference(refElement, dictionaryService, container);
                QueryParameterDefinition resolved = nqpd.getQueryParameterDefinition();
                if (resolved == null)
                {
                    throw new AlfrescoRuntimeException("Unable to find refernce parameter : " + nqpd.getQName());
                }
                queryParameterDefs.add(resolved);
            }

            return new CannedQueryDefImpl(qName, language, query, queryParameterDefs, container);

        }
        else
        {
            return null;
        }
    }

    public static org.dom4j.QName getElementQName()
    {
        return ELEMENT_QNAME;
    }

    public Map<QName, QueryParameterDefinition> getQueryParameterMap()
    {
        return Collections.unmodifiableMap(queryParameterDefs);
    }

}
