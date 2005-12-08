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

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.search.NamedQueryParameterDefinition;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.dom4j.Element;
import org.dom4j.Namespace;

public class QueryParameterRefImpl implements NamedQueryParameterDefinition
{

    private static final org.dom4j.QName ELEMENT_QNAME = new org.dom4j.QName("parameter-ref", new Namespace(NamespaceService.ALFRESCO_PREFIX, NamespaceService.ALFRESCO_URI));

    private static final org.dom4j.QName DEF_QNAME = new org.dom4j.QName("qname", new Namespace(NamespaceService.ALFRESCO_PREFIX, NamespaceService.ALFRESCO_URI));
    
    private QName qName;
    
    private QueryCollection container;
    
    public QueryParameterRefImpl(QName qName, QueryCollection container)
    {
        super();
        this.qName = qName;
        this.container = container;
    }
 
    public QName getQName()
    {
        return qName;
    }
    
    public static NamedQueryParameterDefinition createParameterReference(Element element, DictionaryService dictionaryService, QueryCollection container)
    {
       
        if (element.getQName().getName().equals(ELEMENT_QNAME.getName()))
        {
            QName qName = null;
            Element qNameElement = element.element(DEF_QNAME.getName());
            if(qNameElement != null)
            {
               qName = QName.createQName(qNameElement.getText(), container.getNamespacePrefixResolver());
            }
            
            return new QueryParameterRefImpl(qName, container);
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

    public QueryParameterDefinition getQueryParameterDefinition()
    {
        return container.getParameterDefinition(getQName());
    }

}
