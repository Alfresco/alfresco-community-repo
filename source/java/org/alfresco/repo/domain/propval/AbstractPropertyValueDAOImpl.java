/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.domain.propval;

import java.io.Serializable;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.alfresco.repo.cache.NullCache;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import org.alfresco.repo.domain.CrcHelper;
import org.alfresco.repo.domain.control.ControlDAO;
import org.alfresco.repo.domain.propval.PropertyValueEntity.PersistedType;
import org.alfresco.repo.domain.schema.SchemaBootstrap;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Abstract implementation for Property Value DAO.
 * <p>
 * This provides basic services such as caching, but defers to the underlying implementation
 * for CRUD operations. 
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public abstract class AbstractPropertyValueDAOImpl implements PropertyValueDAO
{
    private static final String CACHE_REGION_PROPERTY_CLASS = "PropertyClass";
    private static final String CACHE_REGION_PROPERTY_DATE_VALUE = "PropertyDateValue";
    private static final String CACHE_REGION_PROPERTY_STRING_VALUE = "PropertyStringValue";
    private static final String CACHE_REGION_PROPERTY_DOUBLE_VALUE = "PropertyDoubleValue";
    private static final String CACHE_REGION_PROPERTY_SERIALIZABLE_VALUE = "PropertySerializableValue";
    private static final String CACHE_REGION_PROPERTY_VALUE = "PropertyValue";
    private static final String CACHE_REGION_PROPERTY = "Property";
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    protected PropertyTypeConverter converter;
    protected ControlDAO controlDAO;
    
    private final PropertyClassCallbackDAO propertyClassDaoCallback;
    private final PropertyDateValueCallbackDAO propertyDateValueCallback;
    private final PropertyStringValueCallbackDAO propertyStringValueCallback;
    private final PropertyDoubleValueCallbackDAO propertyDoubleValueCallback;
    private final PropertySerializableValueCallbackDAO propertySerializableValueCallback;
    private final PropertyValueCallbackDAO propertyValueCallback;
    private final PropertyCallbackDAO propertyCallback;
    /**
     * Cache for the property class:<br/>
     * KEY: ID<br/>
     * VALUE: Java class<br/>
     * VALUE KEY: Java class name<br/>
     */
    private EntityLookupCache<Long, Class<?>, String> propertyClassCache;
    /**
     * Cache for the property date value:<br/>
     * KEY: ID<br/>
     * VALUE: The Date instance<br/>
     * VALUE KEY: The date-only date (i.e. everything below day is zeroed)<br/>
     */
    private EntityLookupCache<Long, Date, Date> propertyDateValueCache;
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
     * Cache for the property Serializable value:<br/>
     * KEY: ID<br/>
     * VALUE: The Serializable instance<br/>
     * VALUE KEY: none<br/>.  The cache is not used for value-based lookups.
     */
    private EntityLookupCache<Long, Serializable, Serializable> propertySerializableValueCache;
    /**
     * Cache for the property value:<br/>
     * KEY: ID<br/>
     * VALUE: The Serializable instance<br/>
     * VALUE KEY: A value key based on the persisted type<br/>
     */
    private EntityLookupCache<Long, Serializable, Serializable> propertyValueCache;
    /**
     * Cache for the property:<br/>
     * KEY: ID<br/>
     * VALUE: The Serializable instance<br/>
     * VALUE KEY: A value key based on the persisted type<br/>
     */
    private EntityLookupCache<Long, Serializable, Serializable> propertyCache;
    
    private SimpleCache<CachePucKey, PropertyUniqueContextEntity> propertyUniqueContextCache; // cluster-aware
    
    /**
     * Set the cache to use for <b>avm_version_roots</b> lookups (optional).
     * 
     * @param vrEntityCache
     */
    public void setPropertyUniqueContextCache(SimpleCache<CachePucKey, PropertyUniqueContextEntity> propertyUniqueContextCache)
    {
        this.propertyUniqueContextCache = propertyUniqueContextCache;
    }
    
    
    /**
     * Default constructor.
     * <p>
     * This sets up the DAO accessors to bypass any caching to handle the case where the caches are not
     * supplied in the setters.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public AbstractPropertyValueDAOImpl()
    {
        this.propertyClassDaoCallback = new PropertyClassCallbackDAO();
        this.propertyDateValueCallback = new PropertyDateValueCallbackDAO();
        this.propertyStringValueCallback = new PropertyStringValueCallbackDAO();
        this.propertyDoubleValueCallback = new PropertyDoubleValueCallbackDAO();
        this.propertySerializableValueCallback = new PropertySerializableValueCallbackDAO();
        this.propertyValueCallback = new PropertyValueCallbackDAO();
        this.propertyCallback = new PropertyCallbackDAO();
        
        this.propertyClassCache = new EntityLookupCache<Long, Class<?>, String>(propertyClassDaoCallback);
        this.propertyDateValueCache = new EntityLookupCache<Long, Date, Date>(propertyDateValueCallback);
        this.propertyStringValueCache = new EntityLookupCache<Long, String, Pair<String, Long>>(propertyStringValueCallback);
        this.propertyDoubleValueCache = new EntityLookupCache<Long, Double, Double>(propertyDoubleValueCallback);
        this.propertySerializableValueCache = new EntityLookupCache<Long, Serializable, Serializable>(propertySerializableValueCallback);
        this.propertyValueCache = new EntityLookupCache<Long, Serializable, Serializable>(propertyValueCallback);
        this.propertyCache = new EntityLookupCache<Long, Serializable, Serializable>(propertyCallback);
        
        this.propertyUniqueContextCache = (SimpleCache<CachePucKey, PropertyUniqueContextEntity>)new NullCache();
    }

    /**
     * @param converter                     the converter that translates between external and persisted values
     */
    public void setConverter(PropertyTypeConverter converter)
    {
        this.converter = converter;
    }

    /**
     * @param controlDAO                    the DAO that provides connection control
     */
    public void setControlDAO(ControlDAO controlDAO)
    {
        this.controlDAO = controlDAO;
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
     * Set the cache to use for <b>alf_prop_date_value</b> lookups (optional).
     * 
     * @param propertyDateValueCache        the cache of IDs to property values
     */
    public void setPropertyDateValueCache(SimpleCache<Serializable, Object> propertyDateValueCache)
    {
        this.propertyDateValueCache = new EntityLookupCache<Long, Date, Date>(
                propertyDateValueCache,
                CACHE_REGION_PROPERTY_DATE_VALUE,
                propertyDateValueCallback);
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
     * Set the cache to use for <b>alf_prop_serializable_value</b> lookups (optional).
     * 
     * @param propertySerializableValueCache     the cache of IDs to property values
     */
    public void setPropertySerializableValueCache(SimpleCache<Serializable, Object> propertySerializableValueCache)
    {
        this.propertySerializableValueCache = new EntityLookupCache<Long, Serializable, Serializable>(
                propertySerializableValueCache,
                CACHE_REGION_PROPERTY_SERIALIZABLE_VALUE,
                propertySerializableValueCallback);
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
    
    /**
     * Set the cache to use for <b>alf_prop_root</b> lookups (optional).
     * 
     * @param propertyValueCache     the cache of IDs to property values
     */
    public void setPropertyCache(SimpleCache<Serializable, Object> propertyCache)
    {
        this.propertyCache = new EntityLookupCache<Long, Serializable, Serializable>(
                propertyCache,
                CACHE_REGION_PROPERTY,
                propertyCallback);
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
            throw new DataIntegrityViolationException("No property class exists for ID " + id);
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
    private class PropertyClassCallbackDAO extends EntityLookupCallbackDAOAdaptor<Long, Class<?>, String>
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
    // 'alf_prop_date_value' accessors
    //================================

    public Pair<Long, Date> getPropertyDateValueById(Long id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Cannot look up entity by null ID.");
        }
        Pair<Long, Date> entityPair = propertyDateValueCache.getByKey(id);
        if (entityPair == null)
        {
            throw new DataIntegrityViolationException("No property date value exists for ID " + id);
        }
        return entityPair;
    }

    public Pair<Long, Date> getPropertyDateValue(Date value)
    {
        if (value == null)
        {
            throw new IllegalArgumentException("Persisted date values cannot be null");
        }
        value = PropertyDateValueEntity.truncateDate(value);
        Pair<Long, Date> entityPair = propertyDateValueCache.getByValue(value);
        return entityPair;
    }

    public Pair<Long, Date> getOrCreatePropertyDateValue(Date value)
    {
        if (value == null)
        {
            throw new IllegalArgumentException("Persisted date values cannot be null");
        }
        value = PropertyDateValueEntity.truncateDate(value);
        Pair<Long, Date> entityPair = propertyDateValueCache.getOrCreateByValue(value);
        return (Pair<Long, Date>) entityPair;
    }

    /**
     * Callback for <b>alf_prop_date_value</b> DAO.
     */
    private class PropertyDateValueCallbackDAO extends EntityLookupCallbackDAOAdaptor<Long, Date, Date>
    {
        private final Pair<Long, Date> convertEntityToPair(PropertyDateValueEntity entity)
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
        
        /**
         * {@inheritDoc}
         * <p/>
         * The value will already have been truncated to be accurate to the last day
         */
        public Date getValueKey(Date value)
        {
            return PropertyDateValueEntity.truncateDate(value);
        }

        public Pair<Long, Date> createValue(Date value)
        {
            PropertyDateValueEntity entity = createDateValue(value);
            return convertEntityToPair(entity);
        }

        public Pair<Long, Date> findByKey(Long key)
        {
            PropertyDateValueEntity entity = findDateValueById(key);
            return convertEntityToPair(entity);
        }

        public Pair<Long, Date> findByValue(Date value)
        {
            PropertyDateValueEntity entity = findDateValueByValue(value);
            return convertEntityToPair(entity);
        }
    }
    
    protected abstract PropertyDateValueEntity findDateValueById(Long id);
    /**
     * @param value             a date, accurate to the day
     */
    protected abstract PropertyDateValueEntity findDateValueByValue(Date value);
    /**
     * @param value             a date, accurate to the day
     */
    protected abstract PropertyDateValueEntity createDateValue(Date value);

    //================================
    // 'alf_prop_string_value' accessors
    //================================

    public Pair<String, Long> getPropertyStringCaseSensitiveSearchParameters(String value)
    {
        return CrcHelper.getStringCrcPair(value, 16, false, true);
    }

    public Pair<Long, String> getPropertyStringValueById(Long id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Cannot look up entity by null ID.");
        }
        Pair<Long, String> entityPair = propertyStringValueCache.getByKey(id);
        if (entityPair == null)
        {
            throw new DataIntegrityViolationException("No property string value exists for ID " + id);
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
        int maxStringLen = SchemaBootstrap.getMaxStringLength();
        if (value.length() > maxStringLen)
        {
            throw new IllegalArgumentException(
                    "Persisted string values for 'alf_prop_string_value' cannot be longer than "
                    + maxStringLen + " characters.  Increase the string column sizes and set property " +
                    "'system.maximumStringLength' accordingly.");
        }
        Pair<Long, String> entityPair = propertyStringValueCache.getOrCreateByValue(value);
        return entityPair;
    }

    /**
     * Callback for <b>alf_prop_string_value</b> DAO.
     */
    private class PropertyStringValueCallbackDAO extends EntityLookupCallbackDAOAdaptor<Long, String, Pair<String, Long>>
    {
        public Pair<String, Long> getValueKey(String value)
        {
            return getPropertyStringCaseSensitiveSearchParameters(value);
        }

        public Pair<Long, String> createValue(String value)
        {
            Long key = createStringValue(value);
            return new Pair<Long, String>(key, value);
        }

        public Pair<Long, String> findByKey(Long key)
        {
            String value = findStringValueById(key);
            if (value == null)
            {
                return null;
            }
            else
            {
                return new Pair<Long, String>(key, value);
            }
        }

        public Pair<Long, String> findByValue(String value)
        {
            Long key = findStringValueByValue(value);
            if (key == null)
            {
                return null;
            }
            else
            {
                return new Pair<Long, String>(key, value);
            }
        }
    }
    
    protected abstract String findStringValueById(Long id);
    protected abstract Long findStringValueByValue(String value);
    protected abstract Long createStringValue(String value);

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
            throw new DataIntegrityViolationException("No property double value exists for ID " + id);
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
    private class PropertyDoubleValueCallbackDAO extends EntityLookupCallbackDAOAdaptor<Long, Double, Double>
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
    // 'alf_prop_serializable_value' accessors
    //================================

    public Pair<Long, Serializable> getPropertySerializableValueById(Long id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Cannot look up entity by null ID.");
        }
        Pair<Long, Serializable> entityPair = propertySerializableValueCache.getByKey(id);
        if (entityPair == null)
        {
            throw new DataIntegrityViolationException("No property serializable value exists for ID " + id);
        }
        return entityPair;
    }

    public Pair<Long, Serializable> createPropertySerializableValue(Serializable value)
    {
        if (value == null)
        {
            throw new IllegalArgumentException("Persisted serializable values cannot be null");
        }
        Pair<Long, Serializable> entityPair = propertySerializableValueCache.getOrCreateByValue(value);
        return (Pair<Long, Serializable>) entityPair;
    }

    /**
     * Callback for <b>alf_prop_serializable_value</b> DAO.
     */
    private class PropertySerializableValueCallbackDAO extends EntityLookupCallbackDAOAdaptor<Long, Serializable, Serializable>
    {
        private final Pair<Long, Serializable> convertEntityToPair(PropertySerializableValueEntity entity)
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
        
        public Pair<Long, Serializable> createValue(Serializable value)
        {
            PropertySerializableValueEntity entity = createSerializableValue(value);
            return convertEntityToPair(entity);
        }

        public Pair<Long, Serializable> findByKey(Long key)
        {
            PropertySerializableValueEntity entity = findSerializableValueById(key);
            return convertEntityToPair(entity);
        }
    }
    
    protected abstract PropertySerializableValueEntity findSerializableValueById(Long id);
    protected abstract PropertySerializableValueEntity createSerializableValue(Serializable value);

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
            throw new DataIntegrityViolationException("No property value exists for ID " + id);
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
    private class PropertyValueCallbackDAO extends EntityLookupCallbackDAOAdaptor<Long, Serializable, Serializable>
    {
        @SuppressWarnings("unchecked")
        private final Serializable convertToValue(PropertyValueEntity entity)
        {
            if (entity == null)
            {
                return null;
            }
            Long actualTypeId = entity.getActualTypeId();
            final Class<Serializable> actualType = (Class<Serializable>) getPropertyClassById(actualTypeId).getSecond();
            final Serializable actualValue = entity.getValue(actualType, converter);
            // Done
            return actualValue;
        }
        
        private final Pair<Long, Serializable> convertEntityToPair(PropertyValueEntity entity)
        {
            if (entity == null)
            {
                return null;
            }
            Long entityId = entity.getId();
            Serializable actualValue = convertToValue(entity);
            // Done
            return new Pair<Long, Serializable>(entityId, actualValue);
        }
        
        public Serializable getValueKey(Serializable value)
        {
            PersistedType persistedType = PropertyValueEntity.getPersistedTypeEnum(value, converter);
            // We don't return keys for pure Serializable instances
            if (persistedType == PersistedType.SERIALIZABLE)
            {
                // It will be Serialized, so no search key
                return null;
            }
            else if (value instanceof String)
            {
                return CrcHelper.getStringCrcPair((String)value, 128, true, true);
            }
            else
            {
                // We've dodged Serializable and String; everything else is OK as a key.
                return value;
            }
        }

        public Pair<Long, Serializable> createValue(Serializable value)
        {
            PropertyValueEntity entity = createPropertyValue(value);
            // Done
            return new Pair<Long, Serializable>(entity.getId(), value);
        }

        public Pair<Long, Serializable> findByKey(Long key)
        {
            PropertyValueEntity entity = findPropertyValueById(key);
            return convertEntityToPair(entity);
        }

        public Pair<Long, Serializable> findByValue(Serializable value)
        {
            PropertyValueEntity entity = findPropertyValueByValue(value);
            return convertEntityToPair(entity);
        }

        /**
         * No-op.  This is implemented as we just want to update the cache.
         * @return              Returns 0 always
         */
        @Override
        public int updateValue(Long key, Serializable value)
        {
            return 0;
        }
    }
    
    protected abstract PropertyValueEntity findPropertyValueById(Long id);
    protected abstract PropertyValueEntity findPropertyValueByValue(Serializable value);
    protected abstract PropertyValueEntity createPropertyValue(Serializable value);

    //================================
    // 'alf_prop_root' accessors
    //================================

    public Serializable getPropertyById(Long id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Cannot look up entity by null ID.");
        }
        Pair<Long, Serializable> entityPair = propertyCache.getByKey(id);
        if (entityPair == null)
        {
            // Remove from cache
            propertyCache.removeByKey(id);
            
            throw new DataIntegrityViolationException("No property value exists for ID " + id);
        }
        return entityPair.getSecond();
    }

    public void getPropertiesByIds(List<Long> ids, PropertyFinderCallback callback)
    {
        findPropertiesByIds(ids, callback);
    }

    /**
     * {@inheritDoc}
     * @see #createPropertyImpl(Serializable, int, int)
     */
    public Long createProperty(Serializable value)
    {
        Pair<Long, Serializable> entityPair = propertyCache.getOrCreateByValue(value);
        return entityPair.getFirst();
    }
    
    public void updateProperty(Long rootPropId, Serializable value)
    {
        propertyCache.updateValue(rootPropId, value);
    }

    public void deleteProperty(Long id)
    {
        propertyCache.deleteByKey(id);
    }

    /**
     * Callback for <b>alf_prop_root</b> DAO.
     */
    private class PropertyCallbackDAO extends EntityLookupCallbackDAOAdaptor<Long, Serializable, Serializable>
    {
        public Pair<Long, Serializable> createValue(Serializable value)
        {
            // We will need a new root
            Long rootPropId = createPropertyRoot();
            createPropertyImpl(rootPropId, 0L, 0L, null, value);
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Created property: \n" +
                        "   ID: " + rootPropId + "\n" +
                        "   Value: " + value);
            }
            return new Pair<Long, Serializable>(rootPropId, value);
        }

        public Pair<Long, Serializable> findByKey(Long key)
        {
            List<PropertyIdSearchRow> rows = findPropertyById(key);
            if (rows.size() == 0)
            {
                // No results
                return null;
            }
            Serializable value = convertPropertyIdSearchRows(rows);
            return new Pair<Long, Serializable>(key, value);
        }

        /**
         * Updates a property.  The <b>alf_prop_root</b> entity is updated
         * to ensure concurrent modification is detected.
         * 
         * @return              Returns 1 always
         */
        @Override
        public int updateValue(Long key, Serializable value)
        {
            // Remove all entries for the root
            PropertyRootEntity entity = getPropertyRoot(key);
            if (entity == null)
            {
                throw new DataIntegrityViolationException("No property root exists for ID " + key);
            }
            // Remove all links using the root
            deletePropertyLinks(key);
            // Create the new properties and update the cache
            createPropertyImpl(key, 0L, 0L, null, value);
            // Update the property root to detect concurrent modification
            updatePropertyRoot(entity);
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Updated property: \n" +
                        "   ID: " + key + "\n" +
                        "   Value: " + value);
            }
            return 1;
        }

        @Override
        public int deleteByKey(Long key)
        {
            deletePropertyRoot(key);
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Deleted property: \n" +
                        "   ID: " + key);
            }
            return 1;
        }
    }

    /**
     * @param propIndex         a unique index within the context of the current property root
     */
    @SuppressWarnings("unchecked")
    private long createPropertyImpl(
            Long rootPropId,
            long propIndex,
            long containedIn,
            Long keyPropId,
            Serializable value)
    {
        // Keep track of the index for this property.  It gets used later when making the link entry.
        long thisPropIndex = propIndex;
        
        Long valuePropId = null;
        if (value == null)
        {
            // The key and the value are the same
            valuePropId = getOrCreatePropertyValue(value).getFirst();
        }
        else if (value instanceof Map<?, ?>)
        {
            Map<Serializable, Serializable> map = (Map<Serializable, Serializable>) value;
            // Check if the it has a default constructor
            Serializable emptyInstance = constructEmptyContainer(value.getClass());
            if (emptyInstance == null)
            {
                // No default constructor, so we just throw the whole thing in as a single property
                valuePropId = getOrCreatePropertyValue(value).getFirst();
            }
            else
            {
                // Persist the empty map
                valuePropId = getOrCreatePropertyValue(emptyInstance).getFirst();
                // Persist the individual entries
                for (Map.Entry<Serializable, Serializable> entry : map.entrySet())
                {
                    // Recurse for each value
                    Serializable mapKey = entry.getKey();
                    Serializable mapValue = entry.getValue();
                    // Get the IDs for these
                    Long mapKeyId = getOrCreatePropertyValue(mapKey).getFirst();
                    propIndex = createPropertyImpl(
                            rootPropId,
                            propIndex + 1L,
                            thisPropIndex,
                            mapKeyId,
                            mapValue);
                }
            }
        }
        else if (value instanceof Collection<?>)
        {
            Collection<Serializable> collection = (Collection<Serializable>) value;
            // Check if the it has a default constructor
            Serializable emptyInstance = constructEmptyContainer(value.getClass());
            if (emptyInstance == null)
            {
                // No default constructor, so we just throw the whole thing in as a single property
                valuePropId = getOrCreatePropertyValue(value).getFirst();
            }
            else
            {
                // Persist the empty collection
                valuePropId = getOrCreatePropertyValue(emptyInstance).getFirst();
                // Persist the individual entries
                for (Serializable collectionValue : collection)
                {
                    // Recurse for each value
                    propIndex = createPropertyImpl(
                            rootPropId,
                            propIndex + 1L,
                            thisPropIndex,
                            null,
                            collectionValue);
                }
            }
        }
        else
        {
            // The key and the value are the same
            valuePropId = getOrCreatePropertyValue(value).getFirst();
        }
        
        // Create a link entry
        if (keyPropId == null)
        {
            // If the key matches the value then it is the root
            keyPropId = valuePropId;
        }
        createPropertyLink(rootPropId, thisPropIndex, containedIn, keyPropId, valuePropId);
        
        // Done
        return propIndex;
    }
    
    private static final Serializable EMPTY_HASHMAP = new HashMap<Serializable, Serializable>();
    private static final Serializable EMPTY_LIST = new ArrayList<Serializable>();
    private static final Serializable EMPTY_SET = new HashSet<Serializable>();

    /**
     * Returns a reconstructable instance 
     * 
     * @return          Returns an empty instance of the given container (map or collection), or
     *                  <tt>null</tt> if it is not possible to do 
     */
    protected Serializable constructEmptyContainer(Class<?> clazz)
    {
        try
        {
            return (Serializable) clazz.getConstructor().newInstance();
        }
        catch (Throwable e)
        {
            // Can't be constructed, so we just choose a well-known implementation.
            // There are so many variations on maps and collections (Unmodifiable, Immutable, etc)
            // that to not choose an alternative would leave the database full of BLOBs
        }
        if (Map.class.isAssignableFrom(clazz))
        {
            return EMPTY_HASHMAP;
        }
        else if (List.class.isAssignableFrom(clazz))
        {
            return EMPTY_LIST;
        }
        else if (Set.class.isAssignableFrom(clazz))
        {
            return EMPTY_SET;
        }
        else
        {
            logger.warn("Unable to find suitable container type with default constructor: " + clazz);
            return null;
        }
    }
    
    protected abstract List<PropertyIdSearchRow> findPropertyById(Long id);
    protected abstract void findPropertiesByIds(List<Long> ids, PropertyFinderCallback callback);
    protected abstract Long createPropertyRoot();
    protected abstract PropertyRootEntity getPropertyRoot(Long id);
    protected abstract PropertyRootEntity updatePropertyRoot(PropertyRootEntity entity);
    protected abstract void deletePropertyRoot(Long id);
    
    /**
     * Create an entry for the map or collection link.
     * 
     * @param rootPropId            the root (entry-point) property ID
     * @param propIndex             the property number within the root property
     * @param containedIn           the property that contains the current value
     * @param keyPropId             the map key entity ID or collection position count
     * @param valuePropId           the ID of the entity storing the value (may be another map or collection)
     */
    protected abstract void createPropertyLink(
            Long rootPropId,
            Long propIndex,
            Long containedIn,
            Long keyPropId,
            Long valuePropId);
    
    /**
     * Remove all property links for a given property root.
     * 
     * @param rootPropId            the root (entry-point) property ID
     */
    protected abstract int deletePropertyLinks(Long rootPropId);
    
    //================================
    // 'alf_prop_unique_ctx' accessors
    //================================
    
    private CachePucKey getPucKey(Long id1, Long id2, Long id3)
    {
        return new CachePucKey(id1, id2, id3);
    }
    
    /**
     * Key for PropertyUniqueContext cache
     */
    public static class CachePucKey implements Serializable
    {
        private static final long serialVersionUID = -4294324585692613101L;
        
        private final Long key1;
        private final Long key2;
        private final Long key3;
        
        private final int hashCode;
        
        private CachePucKey(Long key1, Long key2, Long key3)
        {
            this.key1 = key1;
            this.key2 = key2;
            this.key3 = key3;
            this.hashCode = (key1 == null ? 0 : key1.hashCode()) + (key2 == null ? 0 : key2.hashCode()) + (key3 == null ? 0 : key3.hashCode());
        }
        
        @Override
        public String toString()
        {
            return key1 + "." + key2 + "." + key3;
        }
        
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            else if (!(obj instanceof CachePucKey))
            {
                return false;
            }
            CachePucKey that = (CachePucKey) obj;
            return EqualsHelper.nullSafeEquals(this.key1, that.key1) && 
                   EqualsHelper.nullSafeEquals(this.key2, that.key2) &&
                   EqualsHelper.nullSafeEquals(this.key3, that.key3);
        }
        
        @Override
        public int hashCode()
        {
            return hashCode;
        }
    }
    
    public Pair<Long, Long> createPropertyUniqueContext(
            Serializable value1, Serializable value2, Serializable value3,
            Serializable propertyValue1)
    {
        /*
         * Use savepoints so that the PropertyUniqueConstraintViolation can be caught and handled in-transactioin
         */
        
        // Translate the properties.  Null values are acceptable
        Long id1 = getOrCreatePropertyValue(value1).getFirst();
        Long id2 = getOrCreatePropertyValue(value2).getFirst();
        Long id3 = getOrCreatePropertyValue(value3).getFirst();
        Long property1Id = null;
        if (propertyValue1 != null)
        {
            property1Id = createProperty(propertyValue1);
        }
        
        CachePucKey pucKey = getPucKey(id1, id2, id3);
        
        Savepoint savepoint = controlDAO.createSavepoint("createPropertyUniqueContext");
        try
        {
            PropertyUniqueContextEntity entity = createPropertyUniqueContext(id1, id2, id3, property1Id);
            controlDAO.releaseSavepoint(savepoint);
            
            // cache
            propertyUniqueContextCache.put(pucKey, entity);
            
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Created unique property context: \n" +
                        "   Values: " + value1 + "-" + value2 + "-" + value3 + "\n" +
                        "   Result: " + entity);
            }
            
            return new Pair<Long, Long>(entity.getId(), property1Id);
        }
        catch (Throwable e)
        {
            // Remove from cache
            propertyUniqueContextCache.remove(pucKey);
            
            controlDAO.rollbackToSavepoint(savepoint);
            throw new PropertyUniqueConstraintViolation(value1, value2, value3, e);
        }
    }
    
    public Pair<Long, Long> getPropertyUniqueContext(Serializable value1, Serializable value2, Serializable value3)
    {
        // Translate the properties.  Null values are quite acceptable
        Pair<Long, Serializable> pair1 = getPropertyValue(value1);
        Pair<Long, Serializable> pair2 = getPropertyValue(value2);
        Pair<Long, Serializable> pair3 = getPropertyValue(value3);
        if (pair1 == null || pair2 == null || pair3 == null)
        {
            // None of the values exist so no unique context values can exist
            return null;
        }
        Long id1 = pair1.getFirst();
        Long id2 = pair2.getFirst();
        Long id3 = pair3.getFirst();
        
        CachePucKey pucKey = getPucKey(id1, id2, id3);
        
        // check cache
        PropertyUniqueContextEntity entity = propertyUniqueContextCache.get(pucKey);
        if (entity == null)
        {
            // Remove from cache
            propertyUniqueContextCache.remove(pucKey);
            
            // query DB
            entity = getPropertyUniqueContextByValues(id1, id2, id3);
            
            if (entity != null)
            {
                // cache
                propertyUniqueContextCache.put(pucKey, entity);
            }
        }
         
        if ((entity != null) && (entity.getPropertyId() != null))
        {
            try
            {
                // eager fetch - ignore return for now (could change API)
                getPropertyById(entity.getPropertyId());
            }
            catch (DataIntegrityViolationException dive)
            {
            	// Remove from cache
                propertyUniqueContextCache.remove(pucKey);
                throw dive;
            }
        }
        
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Searched for unique property context: \n" +
                    "   Values: " + value1 + "-" + value2 + "-" + value3 + "\n" +
                    "   Result: " + entity);
        }
        return entity == null ? null : new Pair<Long, Long>(entity.getId(), entity.getPropertyId());
    }
    
    public void getPropertyUniqueContext(PropertyUniqueContextCallback callback, Serializable... values)
    {
        if (values.length < 1 || values.length > 3)
        {
            throw new IllegalArgumentException("Get of unique property sets must have 1, 2 or 3 values");
        }
        Long[] valueIds = new Long[values.length];
        for (int i = 0; i < values.length; i++)
        {
            Pair<Long, Serializable> valuePair = getPropertyValue(values[i]);
            if (valuePair == null)
            {
                // No such value, so no need to get
                return;
            }
            valueIds[i] = valuePair.getFirst();
        }
        
        // not cached
        getPropertyUniqueContextByValues(callback, valueIds);
        
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Searched for unique property context: \n" +
                    "   Values: " + Arrays.toString(values));
        }
    }
    
    /*
     * Update PUC keys - retain current property value
     * 
     */
    public void updatePropertyUniqueContextKeys(Long id, Serializable value1, Serializable value2, Serializable value3)
    {
        /*
         * Use savepoints so that the PropertyUniqueConstraintViolation can be caught and handled in-transactioin
         */
        
        // Translate the properties.  Null values are acceptable
        Long id1 = getOrCreatePropertyValue(value1).getFirst();
        Long id2 = getOrCreatePropertyValue(value2).getFirst();
        Long id3 = getOrCreatePropertyValue(value3).getFirst();
        
        CachePucKey pucKey = getPucKey(id1, id2, id3);
        
        Savepoint savepoint = controlDAO.createSavepoint("updatePropertyUniqueContext");
        try
        {
            PropertyUniqueContextEntity entity = getPropertyUniqueContextById(id);
            if (entity == null)
            {
                // Remove from cache
                propertyUniqueContextCache.remove(pucKey);
                
                throw new DataIntegrityViolationException("No unique property context exists for id: " + id);
            }
            entity.setValue1PropId(id1);
            entity.setValue2PropId(id2);
            entity.setValue3PropId(id3);
            
            entity = updatePropertyUniqueContext(entity);
            
            controlDAO.releaseSavepoint(savepoint);
            
            // cache
            propertyUniqueContextCache.put(pucKey, entity);
            
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Updated unique property context: \n" +
                        "   ID: " + id + "\n" +
                        "   Values: " + value1 + "-" + value2 + "-" + value3);
            }
            return;
        }
        catch (Throwable e)
        {
            // Remove from cache
            propertyUniqueContextCache.remove(pucKey);
            
            controlDAO.rollbackToSavepoint(savepoint);
            throw new PropertyUniqueConstraintViolation(value1, value2, value3, e);
        }
    }
    
    /* 
     * Update property value by keys
     */
    public void updatePropertyUniqueContext(Serializable value1, Serializable value2, Serializable value3, Serializable propertyValue)
    {
        // Translate the properties.  Null values are acceptable
        Long id1 = getOrCreatePropertyValue(value1).getFirst();
        Long id2 = getOrCreatePropertyValue(value2).getFirst();
        Long id3 = getOrCreatePropertyValue(value3).getFirst();
        
        CachePucKey pucKey = getPucKey(id1, id2, id3);
        
        try
        {
            Pair<Long, Long> entityPair = getPropertyUniqueContext(value1, value2, value3);
            if (entityPair == null)
            {
                throw new DataIntegrityViolationException("No unique property context exists for values: " + value1 + "-" + value2 + "-" + value3);
            }
            
            long id = entityPair.getFirst();
            PropertyUniqueContextEntity entity = getPropertyUniqueContextById(id);
            if (entity == null)
            {
                throw new DataIntegrityViolationException("No unique property context exists for id: " + id);
            }
            
            Long propertyIdToDelete = entity.getPropertyId();
            
            Long propertyId = null;
            if (propertyValue != null)
            {
                propertyId = createProperty(propertyValue);
            }
            
            // Create a new property
            entity.setPropertyId(propertyId);
            
            entity = updatePropertyUniqueContext(entity);
            
            // cache
            propertyUniqueContextCache.put(pucKey, entity);
            
            // Clean up the previous property, if present
            if (propertyIdToDelete != null)
            {
                deleteProperty(propertyIdToDelete);
            }
            
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Updated unique property context: \n" +
                        "   ID: " + id + "\n" +
                        "   Property: " + propertyId);
            }
        }
        catch (DataIntegrityViolationException e)
        {
            // Remove from cache
            propertyUniqueContextCache.remove(pucKey);
            throw e;
        }
        catch (ConcurrencyFailureException e)
        {
            // Remove from cache
            propertyUniqueContextCache.remove(pucKey);
            throw e;
        }
    }

    public int deletePropertyUniqueContext(Serializable... values)
    {
        if (values.length < 1 || values.length > 3)
        {
            throw new IllegalArgumentException("Deletion of unique property sets must have 1, 2 or 3 values");
        }
        Long[] valueIds = new Long[values.length];
        for (int i = 0; i < values.length; i++)
        {
            Pair<Long, Serializable> valuePair = getPropertyValue(values[i]);
            if (valuePair == null)
            {
                // No such value, so no need to delete
                return 0;
            }
            valueIds[i] = valuePair.getFirst();
        }
        int deleted = deletePropertyUniqueContexts(valueIds);
        
        CachePucKey pucKey = getPucKey(valueIds[0], (values.length > 1 ? valueIds[1] : null), (values.length > 2 ? valueIds[2] : null));
        
        if (values.length == 3)
        {
            propertyUniqueContextCache.remove(pucKey);
        }
        else
        {
            // reasonable to clear for now (eg. only used by AVMLockingService.removeLocks*)
            // note: in future, if we need to support mass removal based on specific key grouping then we need to use more intelligent cache (removal)
            propertyUniqueContextCache.clear();
        }
        
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Deleted " + deleted + " unique property contexts: \n" +
                    "   Values: " + Arrays.toString(values) + "\n" +
                    "   IDs:    " + Arrays.toString(valueIds));
        }
        return deleted;
    }

    protected abstract PropertyUniqueContextEntity createPropertyUniqueContext(Long valueId1, Long valueId2, Long valueId3, Long propertyId);
    protected abstract PropertyUniqueContextEntity getPropertyUniqueContextById(Long id);
    protected abstract PropertyUniqueContextEntity getPropertyUniqueContextByValues(Long valueId1, Long valueId2, Long valueId3);
    protected abstract void getPropertyUniqueContextByValues(PropertyUniqueContextCallback callback, Long... valueIds);
    protected abstract PropertyUniqueContextEntity updatePropertyUniqueContext(PropertyUniqueContextEntity entity);
    protected abstract int deletePropertyUniqueContexts(Long ... valueIds);

    //================================
    // Utility methods
    //================================

    @SuppressWarnings("unchecked")
    public Serializable convertPropertyIdSearchRows(List<PropertyIdSearchRow> rows)
    {
        // Shortcut if there are no results
        if (rows.size() == 0)
        {
            return null;
        }
        /*
         * The results all share the same root property.  Pass through the results and construct all
         * instances, storing them ordered by prop_index.
         */
        Map<Long, Serializable> valuesByPropIndex = new HashMap<Long, Serializable>(7);
        TreeMap<Long, PropertyLinkEntity> linkEntitiesByPropIndex = new TreeMap<Long, PropertyLinkEntity>();
        Long rootPropId = null;                         // Keep this to ensure the root_prop_id is common
        for (PropertyIdSearchRow row : rows)
        {
            // Check that we are handling a single root property
            if (rootPropId == null)
            {
                rootPropId = row.getLinkEntity().getRootPropId();
            }
            else if (!rootPropId.equals(row.getLinkEntity().getRootPropId()))
            {
                throw new IllegalArgumentException(
                        "The root_prop_id for the property search rows must not change: \n" +
                        "   Rows: " + rows);
            }
            
            PropertyLinkEntity linkEntity = row.getLinkEntity();
            Long propIndex = linkEntity.getPropIndex();
            Long valuePropId = linkEntity.getValuePropId();
            PropertyValueEntity valueEntity = row.getValueEntity();
            // Get the value
            Serializable value;
            if (valueEntity != null)
            {
                value = propertyValueCallback.convertToValue(valueEntity);
            }
            else
            {
                // Go N+1 if the value entity was not retrieved
                value = getPropertyValueById(valuePropId);
            }
            // Keep it for later
            valuesByPropIndex.put(propIndex, value);
            linkEntitiesByPropIndex.put(propIndex, linkEntity);
        }
        
        Serializable result = null;
        // Iterate again, adding values to the collections and looking for the root property
        for (Map.Entry<Long, PropertyLinkEntity> entry : linkEntitiesByPropIndex.entrySet())
        {
            PropertyLinkEntity linkEntity = entry.getValue();
            Long propIndex = linkEntity.getPropIndex();
            Long containedIn = linkEntity.getContainedIn();
            Long keyPropId = linkEntity.getKeyPropId();
            Serializable value = valuesByPropIndex.get(propIndex);
            // Check if this is the root property
            if (propIndex.equals(containedIn))
            {
                if (result != null)
                {
                    logger.error("Found inconsistent property root data: " + linkEntity);
                    continue;
                }
                // This property is contained in itself i.e. it's the root
                result = value;
            }
            else
            {
                // Add the value to the container to which it belongs.
                // The ordering is irrelevant for some containers; but where it is important,
                // ordering given by the prop_index will ensure that values are added back
                // in the order in which the container originally iterated over them
                Serializable container = valuesByPropIndex.get(containedIn);
                if (container == null)
                {
                    logger.error("Found container ID that doesn't have a value: " + linkEntity);
                }
                else if (container instanceof Map<?, ?>)
                {
                    Map<Serializable, Serializable> map = (Map<Serializable, Serializable>) container;
                    Serializable mapKey = getPropertyValueById(keyPropId).getSecond();
                    map.put(mapKey, value);
                }
                else if (container instanceof Collection<?>)
                {
                    Collection<Serializable> collection = (Collection<Serializable>) container;
                    collection.add(value);
                }
                else
                {
                    logger.error("Found container ID that is not a map or collection: " + linkEntity);
                }
            }
        }
        // This will have put the values into the correct containers
        return result;
    }
}
