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
 * http://www.alfresco.com/legal/licensing
 */

package org.alfresco.repo.attributes;

import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.avm.AVMDAOs;

/**
 * Handles conversions between persistent and value based Attributes.
 * @author britt
 */
public class AttributeConverter
{
    /**
     * Convert an Attribute (recursively) to a persistent attribute. This persists
     * the newly created Attribute immediately.
     * @param from The Attribute to clone.
     * @return The cloned persistent Attribute.
     */
    public Attribute toPersistent(Attribute from)
    {
        switch (from.getType())
        {
            case BOOLEAN :
            {
                return new BooleanAttributeImpl((BooleanAttribute)from);
            }
            case BYTE :
            {
                return new ByteAttributeImpl((ByteAttribute)from);
            }
            case SHORT :
            {
                return new ShortAttributeImpl((ShortAttribute)from);
            }
            case INT :
            {
                return new IntAttributeImpl((IntAttribute)from);
            }
            case LONG :
            {
                return new LongAttributeImpl((LongAttribute)from);
            }
            case FLOAT :
            {
                return new FloatAttributeImpl((FloatAttribute)from);
            }
            case DOUBLE :
            {
                return new DoubleAttributeImpl((DoubleAttribute)from);
            }
            case STRING :
            {
                return new StringAttributeImpl((StringAttribute)from);
            }
            case SERIALIZABLE :
            {
                return new SerializableAttributeImpl((SerializableAttribute)from);
            }
            case MAP :
            {
                return new MapAttributeImpl((MapAttribute)from);
            }
            case LIST :
            {
                return new ListAttributeImpl((ListAttribute)from);
            }
            default :
            {
                throw new AlfrescoRuntimeException("Invalid Attribute Type: " + from.getType());
            }
        }
    }

    public Attribute toValue(Attribute from)
    {
        Attribute ret = null;
        switch (from.getType())
        {
            case BOOLEAN :
            {
                ret = new BooleanAttributeValue((BooleanAttribute)from);
                break;
            }
            case BYTE :
            {
                ret = new ByteAttributeValue((ByteAttribute)from);
                break;
            }
            case SHORT :
            {
                ret = new ShortAttributeValue((ShortAttribute)from);
                break;
            }
            case INT :
            {
                ret = new IntAttributeValue((IntAttribute)from);
                break;
            }
            case LONG :
            {
                ret = new LongAttributeValue((LongAttribute)from);
                break;
            }
            case FLOAT :
            {
                ret = new FloatAttributeValue((FloatAttribute)from);
                break;
            }
            case DOUBLE :
            {
                ret = new DoubleAttributeValue((DoubleAttribute)from);
                break;
            }
            case STRING :
            {
                ret = new StringAttributeValue((StringAttribute)from);
                break;
            }
            case SERIALIZABLE :
            {
                ret = new SerializableAttributeValue((SerializableAttribute)from);
                break;
            }
            case MAP :
            {
                ret = new MapAttributeValue();
                for (Map.Entry<String, Attribute> entry : from.entrySet())
                {
                    ret.put(entry.getKey(), toValue(entry.getValue()));
                }
                break;
            }
            case LIST :
            {
                ret = new ListAttributeValue();
                for (Attribute child : from)
                {
                    ret.add(toValue(child));
                }
                break;
            }
            default :
            {
                throw new AlfrescoRuntimeException("Invalid Attribute Type: " + from.getType());
            }
        }
        AVMDAOs.Instance().fAttributeDAO.evictFlat(from);
        return ret;
    }
}
