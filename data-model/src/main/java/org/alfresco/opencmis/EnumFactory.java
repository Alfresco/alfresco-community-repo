/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.opencmis;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


/**
 * Enum Factory for managing mapping between Enum name and Enum label
 * 
 * @author dcaruana
 *
 */
public class EnumFactory<E extends Enum<E>>
{
    private E defaultEnum;
    private Map<String, E> labelMap = new HashMap<String, E>(10);
    
    public EnumFactory(Class<E> enumClass)
    {
        this(enumClass, null, false);
    }

    public EnumFactory(Class<E> enumClass, E defaultEnum)
    {
        this(enumClass, defaultEnum, false);
    }
    
    /**
     * @param caseSensitive  case-sensitive lookup for Enum label
     */
    public EnumFactory(Class<E> enumClass, E defaultEnum, boolean caseSensitive)
    {
        this.defaultEnum = defaultEnum;

        // setup label map
        labelMap = caseSensitive ? new HashMap<String, E>(10) : new TreeMap<String, E>(String.CASE_INSENSITIVE_ORDER);
        EnumSet<E> enumSet = EnumSet.allOf(enumClass);
        Iterator<E> iter = enumSet.iterator();
        while(iter.hasNext())
        {
            E e = iter.next();
            if (e instanceof EnumLabel)
            {
                labelMap.put(((EnumLabel)e).getLabel(), e);
            }
        }
    }

    /**
     * Gets the default enum
     * 
     * @return  default enum (or null, if no default specified)
     */
    public Enum<E> getDefaultEnum()
    {
        return defaultEnum;
    }

    /**
     * Gets the default label
     * 
     * @return  label of default enum (or null, if no default specified)
     */
    public String getDefaultLabel()
    {
        return label(defaultEnum);
    }
    
    /**
     * Gets the label for the specified enum
     * 
     * @param e  enum
     * @return  label (or null, if no label specified)
     */
    public String label(E e)
    {
        if (e instanceof EnumLabel)
        {
            return ((EnumLabel)e).getLabel();
        }
        return null;
    }

    /**
     * Is valid label?
     * 
     * @param label String
     * @return  true => valid, false => does not exist for this enum
     */
    public boolean validLabel(String label)
    {
        return fromLabel(label) == null ? false : true;
    }

    /**
     * Gets enum from label
     * 
     * @param label String
     * @return  enum (or null, if no enum has specified label)
     */
    public E fromLabel(String label)
    {
        return labelMap.get(label);
    }
    
    /**
     * Gets enum from label
     * 
     * NOTE: If specified label is invalid, the default enum is returned
     *       
     * @param label String
     * @return  enum (or default enum, if label is invalid)
     */
    public E toEnum(String label)
    {
        E e = (label == null) ? null : fromLabel(label);
        if (e == null)
        {
            e = defaultEnum;
        }
        return e;
    }
}
