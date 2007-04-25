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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Value based implementation of a map attribute.
 * @author britt
 */
public class MapAttributeValue extends AttributeValue implements MapAttribute
{
    private static final long serialVersionUID = -5090943744202078113L;

    private Map<String, Attribute> fData;
    
    public MapAttributeValue()
    {
        fData = new HashMap<String, Attribute>();
    }
    
    public MapAttributeValue(MapAttribute attr)
    {
        super(attr.getAcl());
        fData = new HashMap<String, Attribute>();
        for (Map.Entry<String, Attribute> entry : attr.entrySet())
        {
            Attribute value = entry.getValue();
            Attribute newAttr = null;
            switch (value.getType())
            {
                case BOOLEAN :
                {
                    newAttr = new BooleanAttributeValue((BooleanAttribute)value);
                    break;
                }
                case BYTE :
                {
                    newAttr = new ByteAttributeValue((ByteAttribute)value);
                    break;
                }
                case SHORT :
                {
                    newAttr = new ShortAttributeValue((ShortAttribute)value);
                    break;
                }
                case INT :
                {
                    newAttr = new IntAttributeValue((IntAttribute)value);
                    break;
                }
                case LONG :
                {
                    newAttr = new LongAttributeValue((LongAttribute)value);
                    break;
                }
                case FLOAT :
                {
                    newAttr = new FloatAttributeValue((FloatAttribute)value);
                    break;
                }
                case DOUBLE :
                {
                    newAttr = new DoubleAttributeValue((DoubleAttribute)value);
                    break;
                }
                case STRING :
                {
                    newAttr = new StringAttributeValue((StringAttribute)value);
                    break;
                }
                case SERIALIZABLE :
                {
                    newAttr = new SerializableAttributeValue((SerializableAttribute)value);
                    break;
                }
                case MAP :
                {
                    newAttr = new MapAttributeValue((MapAttribute)value);
                    break;
                }
                case LIST :
                {
                    newAttr = new ListAttributeValue((ListAttribute)value);
                    break;
                }
                default :
                {
                    throw new AlfrescoRuntimeException("Unknown Attribute Type: " + value.getType());
                }
            }
            fData.put(entry.getKey(), newAttr);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#getType()
     */
    public Type getType()
    {
        return Type.MAP;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeValue#clear()
     */
    @Override
    public void clear()
    {
        fData.clear();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeValue#entrySet()
     */
    @Override
    public Set<Entry<String, Attribute>> entrySet()
    {
        return fData.entrySet();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeValue#get(java.lang.String)
     */
    @Override
    public Attribute get(String key)
    {
        return fData.get(key);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeValue#keySet()
     */
    @Override
    public Set<String> keySet()
    {
        return fData.keySet();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeValue#put(java.lang.String, org.alfresco.repo.attributes.Attribute)
     */
    @Override
    public void put(String key, Attribute value)
    {
        fData.put(key, value);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeValue#remove(java.lang.String)
     */
    @Override
    public void remove(String key)
    {
        fData.remove(key);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeValue#values()
     */
    @Override
    public Collection<Attribute> values()
    {
        return fData.values();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        for (Map.Entry<String, Attribute> entry : fData.entrySet())
        {
            builder.append(entry.getKey());
            builder.append('=');
            builder.append(entry.getValue().toString());
            builder.append(' ');
        }
        builder.append('}');
        return builder.toString();
    }
}
