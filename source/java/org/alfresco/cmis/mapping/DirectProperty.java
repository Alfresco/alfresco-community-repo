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
package org.alfresco.cmis.mapping;

import java.io.Serializable;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;

/**
 * A simple 1-1 property mapping from a CMIS property name to an alfresco property
 * 
 * @author andyh
 */
public class DirectProperty extends AbstractSimpleProperty
{
    private QName alfrescoName;

    /**
     * Construct
     * 
     * @param serviceRegistry
     * @param propertyName
     * @param alfrescoName
     */
    public DirectProperty(ServiceRegistry serviceRegistry, String propertyName, QName alfrescoName)
    {
        super(serviceRegistry, propertyName);
        this.alfrescoName = alfrescoName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.property.AbstractPropertyAccessor#getMappedProperty()
     */
    public QName getMappedProperty()
    {
        return alfrescoName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.property.PropertyAccessor#getValue(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Serializable getValue(NodeRef nodeRef)
    {
        return getServiceRegistry().getNodeService().getProperty(nodeRef, alfrescoName);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.mapping.AbstractProperty#getValue(org.alfresco.service.cmr.repository.AssociationRef)
     */
    public Serializable getValue(AssociationRef assocRef)
    {
        return null;
    }
    
    public String getLuceneFieldName()
    {
        StringBuilder field = new StringBuilder(64);
        field.append("@");
        field.append(alfrescoName);
        return field.toString();
    }

    protected String getValueAsString(Serializable value)
    {
        PropertyDefinition pd = getServiceRegistry().getDictionaryService().getProperty(alfrescoName);
        Object converted = DefaultTypeConverter.INSTANCE.convert(pd.getDataType(), value);
        String asString = DefaultTypeConverter.INSTANCE.convert(String.class, converted);
        return asString;
    }

    protected QName getQNameForExists()
    {
        return alfrescoName;
    }

    protected DataTypeDefinition getInDataType()
    {
        PropertyDefinition pd = getServiceRegistry().getDictionaryService().getProperty(alfrescoName);
        return pd.getDataType();
    }

    

}
