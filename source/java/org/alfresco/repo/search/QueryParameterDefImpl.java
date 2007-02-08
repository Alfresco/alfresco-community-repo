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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.search;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.dom4j.Element;
import org.dom4j.Namespace;

public class QueryParameterDefImpl implements QueryParameterDefinition
{

    private static final org.dom4j.QName ELEMENT_QNAME = new org.dom4j.QName("parameter-definition", new Namespace(NamespaceService.ALFRESCO_PREFIX, NamespaceService.ALFRESCO_URI));

    private static final org.dom4j.QName DEF_QNAME = new org.dom4j.QName("qname", new Namespace(NamespaceService.ALFRESCO_PREFIX, NamespaceService.ALFRESCO_URI));

    private static final org.dom4j.QName PROPERTY_QNAME = new org.dom4j.QName("property", new Namespace(NamespaceService.ALFRESCO_PREFIX, NamespaceService.ALFRESCO_URI));

    private static final org.dom4j.QName PROPERTY_TYPE_QNAME = new org.dom4j.QName("type", new Namespace(NamespaceService.ALFRESCO_PREFIX, NamespaceService.ALFRESCO_URI));

    private static final org.dom4j.QName DEFAULT_VALUE = new org.dom4j.QName("default-value", new Namespace(NamespaceService.ALFRESCO_PREFIX, NamespaceService.ALFRESCO_URI));

    
    private QName qName;

    private PropertyDefinition propertyDefintion;

    private DataTypeDefinition dataTypeDefintion;
    
    private boolean hasDefaultValue;
    
    private String defaultValue;

    public QueryParameterDefImpl(QName qName, PropertyDefinition propertyDefinition, boolean hasDefaultValue, String defaultValue)
    {
        this(qName, hasDefaultValue, defaultValue);
        this.propertyDefintion = propertyDefinition;
        this.dataTypeDefintion = propertyDefinition.getDataType();
    }

    private QueryParameterDefImpl(QName qName, boolean hasDefaultValue, String defaultValue)
    {
        super();
        this.qName = qName;
        this.hasDefaultValue = hasDefaultValue;
        this.defaultValue = defaultValue;
    }
    
    public QueryParameterDefImpl(QName qName, DataTypeDefinition dataTypeDefintion, boolean hasDefaultValue, String defaultValue)
    {
        this(qName, hasDefaultValue, defaultValue);
        this.propertyDefintion = null;
        this.dataTypeDefintion = dataTypeDefintion;
    }

    public QName getQName()
    {
        return qName;
    }

    public PropertyDefinition getPropertyDefinition()
    {
        return propertyDefintion;
    }

    public DataTypeDefinition getDataTypeDefinition()
    {
        return dataTypeDefintion;
    }

    public static QueryParameterDefinition createParameterDefinition(Element element, DictionaryService dictionaryService,  NamespacePrefixResolver nspr)
    {

        if (element.getQName().getName().equals(ELEMENT_QNAME.getName()))
        {
            QName qName = null;
            Element qNameElement = element.element(DEF_QNAME.getName());
            if (qNameElement != null)
            {
                qName = QName.createQName(qNameElement.getText(), nspr);
            }

            PropertyDefinition propDef = null;
            Element propDefElement = element.element(PROPERTY_QNAME.getName());
            if (propDefElement != null)
            {
                propDef = dictionaryService.getProperty(QName.createQName(propDefElement.getText(), nspr));
            }

            DataTypeDefinition typeDef = null;
            Element typeDefElement = element.element(PROPERTY_TYPE_QNAME.getName());
            if (typeDefElement != null)
            {
                typeDef = dictionaryService.getDataType(QName.createQName(typeDefElement.getText(), nspr));
            }

            boolean hasDefault = false;
            String defaultValue = null;
            Element defaultValueElement = element.element(DEFAULT_VALUE.getName());
            if(defaultValueElement != null)
            {
                hasDefault = true;
                defaultValue = defaultValueElement.getText();
            }
            
            if (propDef != null)
            {
                return new QueryParameterDefImpl(qName, propDef, hasDefault, defaultValue);
            }
            else
            {
                return new QueryParameterDefImpl(qName, typeDef, hasDefault, defaultValue);
            }
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
        return this;
    }

    /**
     * There may be a default value which is null ie <default-value/> the empty
     * string <default-value></default-value> or no entry at all for no default
     * value
     */
    public String getDefault()
    {
        return defaultValue;
    }

    public boolean hasDefaultValue()
    {
        return hasDefaultValue;
    }

}
