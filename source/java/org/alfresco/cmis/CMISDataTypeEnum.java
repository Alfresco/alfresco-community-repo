/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.cmis;

import org.alfresco.opencmis.EnumFactory;
import org.alfresco.opencmis.EnumLabel;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * CMIS Property Types Enum
 * 
 * @author andyh
 */
public enum CMISDataTypeEnum implements EnumLabel
{
    STRING("string")
    {
        public QName getDefaultDataType()
        {
            return DataTypeDefinition.TEXT;
        }
    },
    DECIMAL("decimal")
    {
        public QName getDefaultDataType()
        {
            return DataTypeDefinition.DOUBLE;
        }
    },
    INTEGER("integer")
    {
        public QName getDefaultDataType()
        {
            return DataTypeDefinition.LONG;
        }
    },
    BOOLEAN("boolean")
    {
        public QName getDefaultDataType()
        {
            return DataTypeDefinition.BOOLEAN;
        }
    },
    DATETIME("datetime")
    {
        public QName getDefaultDataType()
        {
            return DataTypeDefinition.DATETIME;
        }
    },
    URI("uri")
    {
        public QName getDefaultDataType()
        {
            return DataTypeDefinition.TEXT;
        }
    },
    ID("id")
    {
        public QName getDefaultDataType()
        {
            return DataTypeDefinition.TEXT;
        }
    },
    HTML("html")
    {
        public QName getDefaultDataType()
        {
            return DataTypeDefinition.TEXT;
        }
    };

    public abstract QName getDefaultDataType();

    private String label;

    /**
     * Construct
     * 
     * @param label
     */
    CMISDataTypeEnum(String label)
    {
        this.label = label;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.EnumLabel#label()
     */
    public String getLabel()
    {
        return label;
    }

    public static EnumFactory<CMISDataTypeEnum> FACTORY = new EnumFactory<CMISDataTypeEnum>(CMISDataTypeEnum.class, null, true);
}
