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

package org.alfresco.service.cmr.attributes;

import java.io.Serializable;

import org.alfresco.repo.attributes.Attribute;
import org.alfresco.repo.attributes.AttributeUnsupportedQueryType;

/**
 * Abstract base class for Attribute Query nodes.
 * @author britt
 */
public abstract class AttrQuery implements Serializable
{
    protected Attribute fValue;
    
    protected String fAttrName;
    
    protected String fEntityName;
    
    protected AttrQuery(Attribute value)
    {
        fValue = value;
        switch (fValue.getType())
        {
            case BOOLEAN :
            {
                fAttrName = "booleanValue";
                fEntityName = "BooleanAttributeImpl";
                break;
            }
            case BYTE :
            {
                fAttrName = "byteValue";
                fEntityName = "ByteAttributeImpl";
                break;
            }
            case SHORT :
            {
                fAttrName = "shortValue";
                fEntityName = "ShortAttributeImpl";
                break;
            }
            case INT :
            {
                fAttrName = "intValue";
                fEntityName = "IntAttributeImpl";
                break;
            }
            case LONG :
            {
                fAttrName = "longValue";
                fEntityName = "LongAttributeImpl";
                break;
            }
            case FLOAT :
            {
                fAttrName = "floatValue";
                fEntityName = "FloatAttributeImpl";
                break;
            }
            case DOUBLE :
            {
                fAttrName = "doubleValue";
                fEntityName = "DoubleAttributeImpl";
                break;
            }
            case STRING :
            {
                fAttrName = "stringValue";
                fEntityName = "StringAttributeImpl";
                break;
            }
            case BLOB :
            {
                fAttrName = "blobValue";
                fEntityName = "BlobValueImpl";
                break;
            }
            case SERIALIZABLE :
            {
                fAttrName = "serializableValue";
                fEntityName = "SerializableValueImpl";
                break;                    
            }
            case MAP :
            {
                fAttrName = "mapValue";  // This doesn't need to make sense.
                fEntityName = "MapValueImpl"; // Nor does this.
            }
        }
    }
    
    /**
     * Get the predicate that goes into a Hibernate query.
     * @return The predicate.
     */
    public abstract String getPredicate();
    
    /**
     * Get the entity that this predicate applies to.
     * @return The entity name.
     */
    public String getEntity()
    {
        return fEntityName;
    }
    
    protected String getValue()
    {
        switch (fValue.getType())
        {
            case BOOLEAN :
            {
                return fValue.getBooleanValue() ? "1" : "0";
            }
            case BYTE :
            {
                return Byte.toString(fValue.getByteValue());
            }
            case SHORT :
            {
                return Short.toString(fValue.getShortValue());
            }
            case INT :
            {
                return Integer.toString(fValue.getIntValue());
            }
            case LONG :
            {
                return Long.toString(fValue.getLongValue());
            }
            case FLOAT :
            {
                return Float.toString(fValue.getFloatValue());
            }
            case DOUBLE :
            {
                return Double.toString(fValue.getDoubleValue());
            }
            case STRING :
            {
                StringBuilder builder = new StringBuilder();
                char[] chars = fValue.getStringValue().toCharArray();
                builder.append('\'');
                for (char c : chars)
                {
                    if (c == '\'')
                    {
                        builder.append("\\'");
                        continue;
                    }
                    builder.append(c);
                }
                builder.append('\'');
                return builder.toString();
            }
            default :
            {
                throw new AttributeUnsupportedQueryType(fValue.getType().name());
            }
        }
    }
}
