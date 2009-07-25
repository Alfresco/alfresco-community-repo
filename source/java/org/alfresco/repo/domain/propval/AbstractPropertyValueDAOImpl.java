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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache.EntityLookupCallbackDAO;
import org.alfresco.util.Pair;

/**
 * Abstract implementation for Property Value DAO.
 * <p>
 * This provides basic services such as caching, but defers to the underlying implementation
 * for CRUD operations. 
 * 
 * @author Derek Hulley
 * @since 3.3
 */
public abstract class AbstractPropertyValueDAOImpl implements PropertyValueDAO
{
    private static final String CACHE_REGION_PROPERTY_CLASS = "PropertyClass";
    private EntityLookupCache<Long, Class<?>, String> propertyClassCache;
    
    /**
     * 
     * @param propertyClassCache            the cache of IDs to property classes
     */
    public void setPropertyClassCache(SimpleCache<Serializable, Object> propertyClassCache)
    {
        PropertyValueCallbackDAO daoCallback = new PropertyValueCallbackDAO();
        this.propertyClassCache = new EntityLookupCache<Long, Class<?>, String>(
                propertyClassCache,
                CACHE_REGION_PROPERTY_CLASS,
                daoCallback);
    }

    public Pair<Long, Class<?>> getPropertyClass(Long id)
    {
        Pair<Long, Class<?>> entityPair = propertyClassCache.getByKey(id);
        if (entityPair == null)
        {
            throw new AlfrescoRuntimeException("No property class exists for ID " + id);
        }
        return entityPair;
    }

    public Pair<Long, Class<?>> getPropertyClass(Class<?> clazz)
    {
        Pair<Long, Class<?>> entityPair = propertyClassCache.getByValue(clazz);
        return entityPair;
    }

    public Pair<Long, Class<?>> getOrCreatePropertyClass(Class<?> clazz)
    {
        Pair<Long, Class<?>> entityPair = propertyClassCache.getOrCreateByValue(clazz);
        return entityPair;
    }

    /**
     * Callback for <b>alf_prop_type</b> DAO.
     */
    private class PropertyValueCallbackDAO implements EntityLookupCallbackDAO<Long, Class<?>, String>
    {
        private final Pair<Long, Class<?>> convertEntityToPair(PropertyClassEntity propertyClassEntity)
        {
            if (propertyClassEntity == null)
            {
                return null;
            }
            else
            {
                return propertyClassEntity.getEntityPair();
            }
        }
        
        public String getValueKey(Class<?> value)
        {
            return value.getName();
        }

        public Pair<Long, Class<?>> createValue(Class<?> value)
        {
            PropertyClassEntity propertyClassEntity = createClass(value);
            return convertEntityToPair(propertyClassEntity);
        }

        public Pair<Long, Class<?>> findByKey(Long key)
        {
            PropertyClassEntity propertyClassEntity = findClassById(key);
            return convertEntityToPair(propertyClassEntity);
        }

        public Pair<Long, Class<?>> findByValue(Class<?> value)
        {
            PropertyClassEntity propertyClassEntity = findClassByValue(value);
            return convertEntityToPair(propertyClassEntity);
        }
    }
    
    protected abstract PropertyClassEntity createClass(Class<?> value);
    protected abstract PropertyClassEntity findClassById(Long id);
    protected abstract PropertyClassEntity findClassByValue(Class<?> value);
}
