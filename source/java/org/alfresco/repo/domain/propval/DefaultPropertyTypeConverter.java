/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.domain.propval;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.repo.domain.propval.PropertyValueEntity.PersistedType;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.util.ParameterCheck;

/**
 * Default converter for handling data going to and from the persistence layer.
 * <p/>
 * Properties are stored as a set of well-defined types defined by the enumeration
 * {@link PersistedType}.  Ultimately, data can be persisted as BLOB data, but must
 * be the last resort.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class DefaultPropertyTypeConverter implements PropertyTypeConverter
{
    /**
     * An unmodifiable map of types and how they should be persisted
     */
    protected static final Map<Class<?>, PersistedType> defaultPersistedTypesByClass;
    
    static
    {
        // Create the map of class-type
        Map<Class<?>, PersistedType> mapClass = new HashMap<Class<?>, PersistedType>(29);
        mapClass.put(NodeRef.class, PersistedType.STRING);
        mapClass.put(Period.class, PersistedType.STRING);
        mapClass.put(Locale.class, PersistedType.STRING);
        mapClass.put(AssociationRef.class, PersistedType.STRING);
        mapClass.put(ChildAssociationRef.class, PersistedType.STRING);
        defaultPersistedTypesByClass = Collections.unmodifiableMap(mapClass);
    }
    
    private Map<Class<?>, PersistedType> persistenceMapping;
    
    /**
     * Default constructor
     */
    public DefaultPropertyTypeConverter()
    {
        persistenceMapping = new HashMap<Class<?>, PersistedType>(
                DefaultPropertyTypeConverter.defaultPersistedTypesByClass);
    }
    
    /**
     * Allow subclasses to add further type mappings specific to the implementation
     * 
     * @param clazz                 the class to be converted
     * @param targetType            the target persisted type
     */
    protected void addTypeMapping(Class<?> clazz, PersistedType targetType)
    {
        this.persistenceMapping.put(clazz, targetType);
    }
    
    /**
     * {@inheritDoc}
     */
    public PersistedType getPersistentType(Serializable value)
    {
        ParameterCheck.mandatory("value", value);
        
        Class<?> clazz = value.getClass();
        PersistedType type = persistenceMapping.get(clazz);
        if (type == null)
        {
            return PersistedType.SERIALIZABLE;
        }
        else
        {
            return type;
        }
    }
    
    /**
     * Performs the conversion
     */
    public <T> T convert(Class<T> targetClass, Object value)
    {
        return DefaultTypeConverter.INSTANCE.convert(targetClass, value);
    }
}
