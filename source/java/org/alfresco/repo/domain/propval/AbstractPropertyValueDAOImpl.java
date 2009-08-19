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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import org.alfresco.repo.domain.CrcHelper;
import org.alfresco.repo.domain.propval.PropertyValueEntity.PersistedType;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private static final String CACHE_REGION_PROPERTY_VALUE = "PropertyValue";
    
    private static final Log logger = LogFactory.getLog(AbstractPropertyValueDAOImpl.class);
    
    protected PropertyTypeConverter converter;
    
    private final PropertyClassCallbackDAO propertyClassDaoCallback;
    private final PropertyDateValueCallbackDAO propertyDateValueCallback;
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
        this.propertyDateValueCallback = new PropertyDateValueCallbackDAO();
        this.propertyStringValueCallback = new PropertyStringValueCallbackDAO();
        this.propertyDoubleValueCallback = new PropertyDoubleValueCallbackDAO();
        this.propertyValueCallback = new PropertyValueCallbackDAO();
        
        this.propertyClassCache = new EntityLookupCache<Long, Class<?>, String>(propertyClassDaoCallback);
        this.propertyDateValueCache = new EntityLookupCache<Long, Date, Date>(propertyDateValueCallback);
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
            throw new AlfrescoRuntimeException("No property date value exists for ID " + id);
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
    private class PropertyStringValueCallbackDAO extends EntityLookupCallbackDAOAdaptor<Long, String, Pair<String, Long>>
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

    /**
     * {@inheritDoc}
     * @see #getOrCreatePropertyValueImpl(Serializable, int, int)
     */
    public Pair<Long, Serializable> getOrCreatePropertyValue(Serializable value)
    {
        return getOrCreatePropertyValueImpl(value, null, Integer.MAX_VALUE, 0);
    }

    /**
     * {@inheritDoc}
     * @see #getOrCreatePropertyValueImpl(Serializable, int, int)
     */
    public Pair<Long, Serializable> getOrCreatePropertyValue(Serializable value, int maxDepth)
    {
        return getOrCreatePropertyValueImpl(value, null, maxDepth, 0);
    }

    @SuppressWarnings("unchecked")
    private Pair<Long, Serializable> getOrCreatePropertyValueImpl(
            Serializable value,
            Long rootId,
            int maxDepth,
            int currentDepth)
    {
        if (value != null && maxDepth > currentDepth && value instanceof Map<?, ?>)
        {
            // The default is to do a deep expansion
            Long mapId = createPropertyMapImpl(
                    (Map<? extends Serializable, ? extends Serializable>)value,
                    rootId,
                    maxDepth,
                    currentDepth);
            Pair<Long, Serializable> entityPair = new Pair<Long, Serializable>(mapId, value);
            // TODO: Go through cache?
            return entityPair;
        }
        else if (value != null && maxDepth > currentDepth && value instanceof Collection<?>)
        {
            // The default is to do a deep expansion
            Long collectionId = createPropertyCollectionImpl(
                    (Collection<? extends Serializable>)value,
                    rootId,
                    maxDepth,
                    currentDepth);
            Pair<Long, Serializable> entityPair = new Pair<Long, Serializable>(collectionId, value);
            // TODO: Go through cache?
            return entityPair;
        }
        else
        {
            Pair<Long, Serializable> entityPair = propertyValueCache.getOrCreateByValue(value);
            return (Pair<Long, Serializable>) entityPair;
        }
    }

    /**
     * Callback for <b>alf_prop_value</b> DAO.
     */
    private class PropertyValueCallbackDAO extends EntityLookupCallbackDAOAdaptor<Long, Serializable, Serializable>
    {
        private final Serializable convertToValue(PropertyValueEntity entity)
        {
            if (entity == null)
            {
                return null;
            }
            final Serializable actualValue;
            if (entity.getPersistedTypeEnum() == PersistedType.CONSTRUCTABLE)
            {
                actualValue = entity.getPersistedValue();
            }
            else
            {
                Long actualTypeId = entity.getActualTypeId();
                Class<?> actualType = getPropertyClassById(actualTypeId).getSecond();
                
                Serializable entityValue = entity.getPersistedValue();
                actualValue = (Serializable) converter.convert(actualType, entityValue);
            }
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
            PersistedType persistedType = PropertyValueEntity.getPersistedTypeEnum(value);
            // We don't return keys for pure Serializable instances
            if (persistedType == PersistedType.SERIALIZABLE)
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
                return value;
            }
        }

        public Pair<Long, Serializable> createValue(Serializable value)
        {
            PropertyValueEntity entity = createPropertyValue(value);
            Long entityId = entity.getId();
            // Create the link entry for the property
            createPropertyLink(entityId, entityId, entityId, 0L);
            // Done
            return new Pair<Long, Serializable>(entity.getId(), value);
        }

        public Pair<Long, Serializable> findByKey(Long key)
        {
            List<PropertyIdSearchRow> rows = findPropertyValueById(key);
            if (rows.size() == 0)
            {
                // No results
                return null;
            }
            Serializable value = convertPropertyIdSearchRows(rows);
            return new Pair<Long, Serializable>(key, value);
        }

        public Pair<Long, Serializable> findByValue(Serializable value)
        {
            if (value != null)
            {
                if (value instanceof Map<?, ?> || value instanceof Collection<?>)
                {
                    throw new IllegalArgumentException("Should not be searching for Maps or Collections");
                }
            }
            PropertyValueEntity entity = findPropertyValueByValue(value);
            return convertEntityToPair(entity);
        }
    }
    
    protected abstract List<PropertyIdSearchRow> findPropertyValueById(Long id);
    protected abstract PropertyValueEntity findPropertyValueByValue(Serializable value);
    protected abstract PropertyValueEntity createPropertyValue(Serializable value);

    @SuppressWarnings("unchecked")
    protected Serializable convertPropertyIdSearchRows(List<PropertyIdSearchRow> rows)
    {
        /*
         * The results are ordered by the root_prop_id, current_prop_id and value_prop_id.
         * However, for safety (data patching, etc) we take a first pass to create the
         * basic properties before hooking them all together in a second pass.
         */
        final Map<Long, Serializable> values = new HashMap<Long, Serializable>(rows.size());
        List<PropertyLinkEntity> keyRows = new ArrayList<PropertyLinkEntity>(5);
        Serializable result = null;
        for (PropertyIdSearchRow row : rows)
        {
            PropertyLinkEntity linkEntity = row.getLinkEntity();
            PropertyValueEntity valueEntity = row.getValueEntity();
            // Construct the value (Maps and Collections should be CONSTRUCTABLE)
            Serializable value = propertyValueCallback.convertToValue(valueEntity);
            // Keep it
            values.put(new Long(linkEntity.getValuePropId()), value);
            
            // If this row is a mapping row (the property value ID does not match the current property ID)
            // then store it for quicker use later
            if (linkEntity.getCurrentPropId() != linkEntity.getValuePropId())
            {
                keyRows.add(linkEntity);
            }
            if (linkEntity.getRootPropId() == linkEntity.getValuePropId())
            {
                // We found the root
                result = value;
            }
        }
        
        // We expect a value to be found unless the results are empty
        if (result == null)
        {
            return null;
        }
        
        // Now we have all our constructed values and the mapping rows: build the collections
        for (PropertyLinkEntity propertyLinkEntity : keyRows)
        {
            Serializable value = values.get(propertyLinkEntity.getValuePropId());
            Serializable currentProp = values.get(propertyLinkEntity.getCurrentPropId());
            if (value == null)
            {
                logger.error("No value found for link property: " + propertyLinkEntity);
                continue;
            }
            if (currentProp == null)
            {
                logger.error("No current property found for link property: " + propertyLinkEntity);
                continue;
            }
            Long keyId = propertyLinkEntity.getKeyPropId();
            // put the value into the container
            if (currentProp instanceof Map<?, ?>)
            {
                Pair<Long, Serializable> keyPair = getPropertyValueById(keyId);
                if (keyPair == null)
                {
                    logger.error("Current property (map) has key without a value: " + propertyLinkEntity);
                    continue;
                }
                Serializable key = keyPair.getSecond();
                Map<Serializable, Serializable> map = (Map<Serializable, Serializable>) currentProp;
                map.put(key, value);
            }
            else if (currentProp instanceof Set<?>)
            {
                // We can ignore the key - it won't make a difference
                Set<Serializable> set = (Set<Serializable>) currentProp;
                set.add(value);
            }
// Results 'should' be ordered by key
//            else if (currentProp instanceof List<?>)
//            {
//                // The order is important
//                List<Serializable> collection = (List<Serializable>) currentProp;
//                collection.add(keyId.intValue(), value);
//            }
            else if (currentProp instanceof Collection<?>)
            {
                // The order is important
                Collection<Serializable> collection = (Collection<Serializable>) currentProp;
                collection.add(value);
            }
        }
        // This will have put the values into the correct containers
        return result;
    }
    
    //================================
    // Special handling of maps and collections
    //================================

    /**
     * Recursive method to write a map out.  If the root entity ID is <tt>null</tt> then the current
     * map ID is used as the root.
     * 
     * @param value                 the map to write
     * @param rootId                the root entity ID, which may be a map or a collection
     * @return                      Returns the ID of the newly-written map
     */
    private <K extends Serializable, V extends Serializable> Long createPropertyMapImpl(
            Map<K, V> map,
            Long rootId,
            int maxDepth,
            int currentDepth)
    {
        // Create the root of the map
        Class<?> clazz = null;
        if (map instanceof MLText)
        {
            clazz = MLText.class;
        }
        else
        {
            clazz = HashMap.class;
        }
        
        // Can't use a cached instance as each map is unique.  Go direct to entity creation.
        Long entityId = createPropertyValue(clazz).getId();
        
        // Use this as the root if this is the first entry into this method
        if (rootId == null)
        {
            rootId = entityId;
        }

        // Create the link entry for the root
        createPropertyLink(rootId, entityId, entityId, 0L);
        
        // Now iterate over the entries and create properties for the keys and values
        for (Map.Entry<K, V> entry : map.entrySet())
        {
            K key = entry.getKey();
            Long keyId = getOrCreatePropertyValue(key).getFirst();

            V value = entry.getValue();
            // Callback with this level, incrementing the current depth
            Pair<Long, Serializable> valuePair = getOrCreatePropertyValueImpl(
                    (Serializable) value,
                    rootId,
                    maxDepth,
                    currentDepth + 1);
            Long valueId = valuePair.getFirst();
            
            // Now write the mapping entry
            createPropertyLink(rootId, entityId, valueId, keyId);
        }
        
        // Done
        return entityId;
    }

    /**
     * Recursive method to write a collection out.  If the root entity ID is <tt>null</tt> then the current
     * collection ID is used as the root.
     * 
     * @param value                 the collection to write
     * @param rootId                the root property ID
     * @return                      Returns the ID of the newly-written collection
     */
    private <V extends Serializable> Long createPropertyCollectionImpl(
            Collection<V> collection,
            Long rootId,
            int maxDepth,
            int currentDepth)
    {
        // Create the root of the collection
        Class<?> clazz = null;
        if (collection instanceof Set<?>)
        {
            clazz = HashSet.class;
        }
        else
        {
            clazz = ArrayList.class;
        }
        
        // Can't use a cached instance as each collection is unique.  Go direct to entity creation.
        Long entityId = createPropertyValue(clazz).getId();
        
        // Use this as the root if this is the first entry into this method
        if (rootId == null)
        {
            rootId = entityId;
        }
        
        // Create the link entry for the root
        createPropertyLink(rootId, entityId, entityId, 0L);
        
        // Now iterate over the entries and create properties for the keys and values
        long index = 0L;
        for (V value : collection)
        {
            // Callback with this level, incrementing the current depth
            Pair<Long, Serializable> valuePair = getOrCreatePropertyValueImpl(
                    (Serializable) value,
                    rootId,
                    maxDepth,
                    currentDepth + 1);
            Long valueId = valuePair.getFirst();
            // Now write the mapping entry
            Long keyId = new Long(index);
            createPropertyLink(rootId, entityId, valueId, keyId);
            // Keep iterating
            index++;
        }
        return entityId;
    }

    /**
     * Create an entry for the map or collection link
     * 
     * @param rootPropId            the root (entry-point) map or collection ID
     * @param currentPropId         the current map or collection ID
     * @param valueId               the ID of the entity storing the value (may be another map or collection)
     * @param keyId                 the map key entity ID or collection position count
     */
    protected abstract void createPropertyLink(
            Long rootPropId,
            Long currentPropId,
            Long valueId,
            Long keyId);
}
