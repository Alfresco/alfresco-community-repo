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
package org.alfresco.util;

import java.io.Serializable;
import java.util.HashMap;

import org.alfresco.service.namespace.QName;

/**
 * Property map helper class.  
 * <p>
 * This class can be used as a short hand when a class of type
 * Map<QName, Serializable> is required.
 * 
 * @author Roy Wetherall
 */
public class PropertyMap extends HashMap<QName, Serializable>
{
    private static final long serialVersionUID = 8052326301073209645L;
    
    /**
     * @see HashMap#HashMap(int, float)
     */
    public PropertyMap(int initialCapacity, float loadFactor)
    {
        super(initialCapacity, loadFactor);
    }
    
    /**
     * @see HashMap#HashMap(int)
     */
    public PropertyMap(int initialCapacity)
    {
        super(initialCapacity);
    }
    
    /**
     * @see HashMap#HashMap()
     */
    public PropertyMap()
    {
        super();
    }
}
