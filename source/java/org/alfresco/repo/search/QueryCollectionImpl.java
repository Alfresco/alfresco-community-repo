/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.search;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.dom4j.Element;
import org.dom4j.Namespace;

public class QueryCollectionImpl implements QueryCollection
{
    private static final org.dom4j.QName ELEMENT_QNAME = new org.dom4j.QName("query-register", new Namespace(NamespaceService.ALFRESCO_PREFIX, NamespaceService.ALFRESCO_URI));

    private static final org.dom4j.QName NAME = new org.dom4j.QName("name", new Namespace(NamespaceService.ALFRESCO_PREFIX, NamespaceService.ALFRESCO_URI));

    private static final org.dom4j.QName NAMESPACES = new org.dom4j.QName("namespaces", new Namespace(NamespaceService.ALFRESCO_PREFIX, NamespaceService.ALFRESCO_URI));

    private static final org.dom4j.QName NAMESPACE = new org.dom4j.QName("namespace", new Namespace(NamespaceService.ALFRESCO_PREFIX, NamespaceService.ALFRESCO_URI));

    private static final org.dom4j.QName PREFIX = new org.dom4j.QName("prefix", new Namespace(NamespaceService.ALFRESCO_PREFIX, NamespaceService.ALFRESCO_URI));

    private static final org.dom4j.QName URI = new org.dom4j.QName("uri", new Namespace(NamespaceService.ALFRESCO_PREFIX, NamespaceService.ALFRESCO_URI));
    
    private String name;
    
    private Map<QName, QueryParameterDefinition> parameters = new HashMap<QName, QueryParameterDefinition>();
    
    private Map<QName, CannedQueryDef> queries = new HashMap<QName, CannedQueryDef>();
    
    NamespacePrefixResolver namespacePrefixResolver;

    public QueryCollectionImpl(String name, Map<QName, QueryParameterDefinition> parameters, NamespacePrefixResolver namespacePrefixResolver)
    {
        super();
        this.name = name;
        this.parameters = parameters;
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    public String getName()
    {
       return name;
    }

    public boolean containsQueryDefinition(QName qName)
    {
        return queries.containsKey(qName);
    }

    private void addQueryDefinition(CannedQueryDef queryDefinition)
    {
        queries.put(queryDefinition.getQname(), queryDefinition);
    }
    
    public CannedQueryDef getQueryDefinition(QName qName)
    {
        return queries.get(qName);
    }

    public boolean containsParameterDefinition(QName qName)
    {
        return parameters.containsKey(qName);
    }

    public QueryParameterDefinition getParameterDefinition(QName qName)
    {
        return parameters.get(qName);
    }

    public NamespacePrefixResolver getNamespacePrefixResolver()
    {
        return namespacePrefixResolver;
    }

    
    public static QueryCollection createQueryCollection(Element element, DictionaryService dictionaryService, NamespacePrefixResolver nspr)
    {
        DynamicNamespacePrefixResolver dnpr = new DynamicNamespacePrefixResolver(nspr);
        if (element.getName().equals(ELEMENT_QNAME.getName()))
        {
            String name = null;
            Element nameElement = element.element(NAME.getName());
            if(nameElement != null)
            {
               name = nameElement.getText();
            } 
            
            Element nameSpaces = element.element(NAMESPACES.getName());
            if(nameSpaces != null)
            {
                List ns = nameSpaces.elements(NAMESPACE.getName());
                for(Iterator it = ns.iterator(); it.hasNext(); /**/)
                {
                    Element nsElement = (Element)it.next();
                    Element prefixElement = nsElement.element(PREFIX.getName());
                    Element uriElement = nsElement.element(URI.getName());
                    if((prefixElement != null) && (nsElement != null))
                    {
                        dnpr.registerNamespace(prefixElement.getText(), uriElement.getText());
                    }
                }
            }
            
            // Do property definitions so they are available to query defintions
            
            Map<QName, QueryParameterDefinition> parameters = new HashMap<QName, QueryParameterDefinition>();
            List list = element.elements(QueryParameterDefImpl.getElementQName().getName());
            for(Iterator it = list.iterator(); it.hasNext(); /**/)
            {
                Element defElement = (Element) it.next();
                QueryParameterDefinition paramDef = QueryParameterDefImpl.createParameterDefinition(defElement, dictionaryService, nspr);
                parameters.put(paramDef.getQName(), paramDef);
            }
            
            QueryCollectionImpl collection = new QueryCollectionImpl(name, parameters, dnpr);
            
            list = element.elements(CannedQueryDefImpl.getElementQName().getName());
            for(Iterator it = list.iterator(); it.hasNext(); /**/)
            {
                Element defElement = (Element) it.next();
                CannedQueryDefImpl queryDef = CannedQueryDefImpl.createCannedQuery(defElement, dictionaryService, collection, nspr);
                collection.addQueryDefinition(queryDef);
            }
            
            return collection;
        }
        else
        {
            return null;
        }
    }
}
