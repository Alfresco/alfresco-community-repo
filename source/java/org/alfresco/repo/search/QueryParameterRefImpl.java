/*
 * Copyright (C) 2005 Alfresco, Inc.
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
