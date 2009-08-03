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
import org.alfresco.repo.domain.CrcHelper;
import org.alfresco.repo.domain.propval.PropertyValueEntity.PersistedType;
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
    private static final String CACHE_REGION_PROPERTY_STRING_VALUE = "PropertyStringValue";
    private static final String CACHE_REGION_PROPERTY_DOUBLE_VALUE = "PropertyDoubleValue";
    private static final String CACHE_REGION_PROPERTY_VALUE = "PropertyValue";
    
    protected PropertyTypeConverter converter;
    
    private final PropertyClassCallbackDAO propertyClassDaoCallback;
    private final PropertyStringValueCallbackDAO propertyStringValueCallback;
    private final PropertyDoubleValueCallbackDAO propertyDoubleValueCallback;
    private final PropertyValueCallbackDAO propertyValueCallback;
    /**
     * Cache for the property class:<br/>
     * KEY: ID<br/>
     * VALUE: Java class<br/>
     * VALUE KEY: Java class name<br/>
     */
    private EntityLookupCache<Long, Class<?>, String> propertyClassCache;
    /**
     * Cache for the property string value:<br/>
     * KEY: ID<br/>
     * VALUE: The full string<br/>
     * VALUE KEY: Short string-crc pair ({@link CrcHelper#getStringCrcPair(String, int, boolean, boolean)})<br/>
     */
    private EntityLookupCache<Long, String, Pair<String, Long>> propertyStringValueCache;
    /**
     * Cache for the property double value:<br/>
     * KEY: ID<br/>
     * VALUE: The Double instance<br/>
     * VALUE KEY: The value itself<br/>
     */
    private EntityLookupCache<Long, Double, Double> propertyDoubleValueCache;
    /**
     * Cache for the property value:<br/>
     * KEY: ID<br/>
     * VALUE: The Serializable instance<br/>
     * VALUE KEY: A value key based on the persisted type<br/>
     */
    private EntityLookupCache<Long, Serializable, Serializable> propertyValueCache;
    
    /**
     * Default constructor.
     * <p>
     * This sets up the DAO accessors to bypass any caching to handle the case where the caches are not
     * supplied in the setters.
     */
    public AbstractPropertyValueDAOImpl()
    {
        this.propertyClassDaoCallback = new PropertyClassCallbackDAO();
        this.propertyStringValueCallback = new PropertyStringValueCallbackDAO();
        this.propertyDoubleValueCallback = new PropertyDoubleValueCallbackDAO();
        this.propertyValueCallback = new PropertyValueCallbackDAO();
        
        this.propertyClassCache = new EntityLookupCache<Long, Class<?>, String>(propertyClassDaoCallback);
        this.propertyStringValueCache = new EntityLookupCache<Long, String, Pair<String, Long>>(propertyStringValueCallback);
        this.propertyDoubleValueCache = new EntityLookupCache<Long, Double, Double>(propertyDoubleValueCallback);
        this.propertyValueCache = new EntityLookupCache<Long, Serializable, Serializable>(propertyValueCallback);
    }

    /**
     * @param converter                     the converter that translates between external and persisted values
     */
    public void setConverter(PropertyTypeConverter converter)
    {
        this.converter = converter;
    }

    /**
     * Set the cache to use for <b>alf_prop_class</b> lookups (optional).
     * 
     * @param propertyClassCache            the cache of IDs to property classes
     */
    public void setPropertyClassCache(SimpleCache<Serializable, Object> propertyClassCache)
    {
        this.propertyClassCache = new EntityLookupCache<Long, Class<?>, String>(
                propertyClassCache,
                CACHE_REGION_PROPERTY_CLASS,
                propertyClassDaoCallback);
    }
    
    /**
     * Set the cache to use for <b>alf_prop_string_value</b> lookups (optional).
     * 
     * @param propertyStringValueCache      the cache of IDs to property string values
     */
    public void setPropertyStringValueCache(SimpleCache<Serializable, Object> propertyStringValueCache)
    {
        this.propertyStringValueCache = new EntityLookupCache<Long, String, Pair<String, Long>>(
                propertyStringValueCache,
                CACHE_REGION_PROPERTY_STRING_VALUE,
                propertyStringValueCallback);
    }
    
    /**
     * Set the cache to use for <b>alf_prop_double_value</b> lookups (optional).
     * 
     * @param propertyDoubleValueCache     the cache of IDs to property values
     */
    public void setPropertyDoubleValueCache(SimpleCache<Serializable, Object> propertyDoubleValueCache)
    {
        this.propertyDoubleValueCache = new EntityLookupCache<Long, Double, Double>(
                propertyDoubleValueCache,
                CACHE_REGION_PROPERTY_DOUBLE_VALUE,
                propertyDoubleValueCallback);
    }
    
    /**
     * Set the cache to use for <b>alf_prop_value</b> lookups (optional).
     * 
     * @param propertyValueCache     the cache of IDs to property values
     */
    public void setPropertyValueCache(SimpleCache<Serializable, Object> propertyValueCache)
    {
        this.propertyValueCache = new EntityLookupCache<Long, Serializable, Serializable>(
                propertyValueCache,
                CACHE_REGION_PROPERTY_VALUE,
                propertyValueCallback);
    }
    
    //================================
    // 'alf_prop_class' accessors
    //================================

    public Pair<Long, Class<?>> getPropertyClassById(Long id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Cannot look up entity by null ID.");
        }
        Pair<Long, Class<?>> entityPair = propertyClassCache.getByKey(id);
        if (entityPair == null)
        {
            throw new AlfrescoRuntimeException("No property class exists for ID " + id);
        }
        return entityPair;
    }

    public Pair<Long, Class<?>> getPropertyClass(Class<?> value)
    {
        if (value == null)
        {
            throw new IllegalArgumentException("Property class cannot be null");
        }
        Pair<Long, Class<?>> entityPair = propertyClassCache.getByValue(value);
        return entityPair;
    }

    public Pair<Long, Class<?>> getOrCreatePropertyClass(Class<?> value)
    {
        if (value == null)
        {
            throw new IllegalArgumentException("Property class cannot be null");
        }
        Pair<Long, Class<?>> entityPair = propertyClassCache.getOrCreateByValue(value);
        return entityPair;
    }

    /**
     * Callback for <b>alf_prop_class</b> DAO.
     */
    private class PropertyClassCallbackDAO implements EntityLookupCallbackDAO<Long, Class<?>, String>
    {
        private final Pair<Long, Class<?>> convertEntityToPair(PropertyClassEntity entity)
        {
            if (entity == null)
            {
                return null;
            }
            else
            {
                return entity.getEntityPair();
            }
        }
        
        public String getValueKey(Class<?> value)
        {
            return value.getName();
        }

        public Pair<Long, Class<?>> createValue(Class<?> value)
        {
            PropertyClassEntity entity = createClass(value);
            return convertEntityToPair(entity);
        }

        public Pair<Long, Class<?>> findByKey(Long key)
        {
            PropertyClassEntity entity = findClassById(key);
            return convertEntityToPair(entity);
        }

        public Pair<Long, Class<?>> findByValue(Class<?> value)
        {
            PropertyClassEntity entity = findClassByValue(value);
            return convertEntityToPair(entity);
        }
    }
    
    protected abstract PropertyClassEntity findClassById(Long id);
    protected abstract PropertyClassEntity findClassByValue(Class<?> value);
    protected abstract PropertyClassEntity createClass(Class<?> value);
    
    //================================
    // 'alf_prop_string_value' accessors
    //================================

    public Pair<Long, String> getPropertyStringValueById(Long id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Cannot look up entity by null ID.");
        }
        Pair<Long, String> entityPair = propertyStringValueCache.getByKey(id);
        if (entityPair == null)
        {
            throw new AlfrescoRuntimeException("No property string value exists for ID " + id);
        }
        return entityPair;
    }

    public Pair<Long, String> getPropertyStringValue(String value)
    {
        if (value == null)
        {
            throw new IllegalArgumentException("Persisted string values cannot be null");
        }
        Pair<Long, String> entityPair = propertyStringValueCache.getByValue(value);
        return entityPair;
    }

    public Pair<Long, String> getOrCreatePropertyStringValue(String value)
    {
        if (value == null)
        {
            throw new IllegalArgumentException("Persisted string values cannot be null");
        }
        Pair<Long, String> entityPair = propertyStringValueCache.getOrCreateByValue(value);
        return entityPair;
    }

    /**
     * Callback for <b>alf_prop_string_value</b> DAO.
     */
    private class PropertyStringValueCallbackDAO implements EntityLookupCallbackDAO<Long, String, Pair<String, Long>>
    {
        private final Pair<Long, String> convertEntityToPair(PropertyStringValueEntity entity)
        {
            if (entity == null)
            {
                return null;
            }
            else
            {
                return entity.getEntityPair();
            }
        }
        
        public Pair<String, Long> getValueKey(String value)
        {
            return CrcHelper.getStringCrcPair(value, 128, true, true);
        }

        public Pair<Long, String> createValue(String value)
        {
            PropertyStringValueEntity entity = createStringValue(value);
            return convertEntityToPair(entity);
        }

        public Pair<Long, String> findByKey(Long key)
        {
            PropertyStringValueEntity entity = findStringValueById(key);
            return convertEntityToPair(entity);
        }

        public Pair<Long, String> findByValue(String value)
        {
            PropertyStringValueEntity entity = findStringValueByValue(value);
            return convertEntityToPair(entity);
        }
    }
    
    protected abstract PropertyStringValueEntity findStringValueById(Long id);
    protected abstract PropertyStringValueEntity findStringValueByValue(String value);
    protected abstract PropertyStringValueEntity createStringValue(String value);

    //================================
    // 'alf_prop_double_value' accessors
    //================================

    public Pair<Long, Double> getPropertyDoubleValueById(Long id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Cannot look up entity by null ID.");
        }
        Pair<Long, Double> entityPair = propertyDoubleValueCache.getByKey(id);
        if (entityPair == null)
        {
            throw new AlfrescoRuntimeException("No property double value exists for ID " + id);
        }
        return entityPair;
    }

    public Pair<Long, Double> getPropertyDoubleValue(Double value)
    {
        if (value == null)
        {
            throw new IllegalArgumentException("Persisted double values cannot be null");
        }
        Pair<Long, Double> entityPair = propertyDoubleValueCache.getByValue(value);
        return entityPair;
    }

    public Pair<Long, Double> getOrCreatePropertyDoubleValue(Double value)
    {
        if (value == null)
        {
            throw new IllegalArgumentException("Persisted double values cannot be null");
        }
        Pair<Long, Double> entityPair = propertyDoubleValueCache.getOrCreateByValue(value);
        return (Pair<Long, Double>) entityPair;
    }

    /**
     * Callback for <b>alf_prop_double_value</b> DAO.
     */
    private class PropertyDoubleValueCallbackDAO implements EntityLookupCallbackDAO<Long, Double, Double>
    {
        private final Pair<Long, Double> convertEntityToPair(PropertyDoubleValueEntity entity)
        {
            if (entity == null)
            {
                return null;
            }
            else
            {
                return entity.getEntityPair();
            }
        }
        
        public Double getValueKey(Double value)
        {
            return value;
        }

        public Pair<Long, Double> createValue(Double value)
        {
            PropertyDoubleValueEntity entity = createDoubleValue(value);
            return convertEntityToPair(entity);
        }

        public Pair<Long, Double> findByKey(Long key)
        {
            PropertyDoubleValueEntity entity = findDoubleValueById(key);
            return convertEntityToPair(entity);
        }

        public Pair<Long, Double> findByValue(Double value)
        {
            PropertyDoubleValueEntity entity = findDoubleValueByValue(value);
            return convertEntityToPair(entity);
        }
    }
    
    protected abstract PropertyDoubleValueEntity findDoubleValueById(Long id);
    protected abstract PropertyDoubleValueEntity findDoubleValueByValue(Double value);
    protected abstract PropertyDoubleValueEntity createDoubleValue(Double value);

    //================================
    // 'alf_prop_value' accessors
    //================================

    public Pair<Long, Serializable> getPropertyValueById(Long id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Cannot look up entity by null ID.");
        }
        Pair<Long, Serializable> entityPair = propertyValueCache.getByKey(id);
        if (entityPair == null)
        {
            throw new AlfrescoRuntimeException("No property value exists for ID " + id);
        }
        return entityPair;
    }

    public Pair<Long, Serializable> getPropertyValue(Serializable value)
    {
        Pair<Long, Serializable> entityPair = propertyValueCache.getByValue(value);
        return entityPair;
    }

    public Pair<Long, Serializable> getOrCreatePropertyValue(Serializable value)
    {
        Pair<Long, Serializable> entityPair = propertyValueCache.getOrCreateByValue(value);
        return (Pair<Long, Serializable>) entityPair;
    }

    /**
     * Callback for <b>alf_prop_value</b> DAO.
     */
    private class PropertyValueCallbackDAO implements EntityLookupCallbackDAO<Long, Serializable, Serializable>
    {
        private final Pair<Long, Serializable> convertEntityToPair(PropertyValueEntity entity)
        {
            if (entity == null)
            {
                return null;
            }
            else
            {
                return entity.getEntityPair();
            }
        }
        
        public Serializable getValueKey(Serializable value)
        {
            // Find out how it would be persisted
            Pair<Short, Serializable> persistedValuePair = converter.convertToPersistedType(value);
            // We don't return keys for pure Serializable instances
            if (persistedValuePair.getFirst().equals(PersistedType.SERIALIZABLE.getOrdinalNumber()))
            {
                // It will be Serialized, so no key
                return null;
            }
            else if (value instanceof String)
            {
                return CrcHelper.getStringCrcPair((String)value, 128, true, true);
            }
            else
            {
                // We've dodged Serializable and String; everything else is OK as a key.
                return persistedValuePair;
            }
        }

        public Pair<Long, Serializable> createValue(Serializable value)
        {
            PropertyValueEntity entity = createPropertyValue(value);
            return convertEntityToPair(entity);
        }

        public Pair<Long, Serializable> findByKey(Long key)
        {
            PropertyValueEntity entity = findPropertyValueById(key);
            return convertEntityToPair(entity);
        }

        public Pair<Long, Serializable> findByValue(Serializable value)
        {
//            // Find out how it would be persisted
//            Pair<Short, Serializable> persistedValuePair = converter.convertToPersistedType(value);
//            // We don't do lookups for serializable values
//            if (persistedValuePair.getFirst().equals(PersistedType.SERIALIZABLE.getOrdinalNumber()))
//            {
//                // It will be Serialized, so no we don't look it up
//                return null;
//            }
            PropertyValueEntity entity = findPropertyValueByValue(value);
            return convertEntityToPair(entity);
        }
    }
    
    protected abstract PropertyValueEntity findPropertyValueById(Long id);
    protected abstract PropertyValueEntity findPropertyValueByValue(Serializable value);
    protected abstract PropertyValueEntity createPropertyValue(Serializable value);
}
