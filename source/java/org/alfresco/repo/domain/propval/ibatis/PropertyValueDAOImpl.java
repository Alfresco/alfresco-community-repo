/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.domain.propval.ibatis;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.alfresco.ibatis.RollupResultHandler;
import org.alfresco.repo.domain.propval.AbstractPropertyValueDAOImpl;
import org.alfresco.repo.domain.propval.PropertyClassEntity;
import org.alfresco.repo.domain.propval.PropertyDateValueEntity;
import org.alfresco.repo.domain.propval.PropertyDoubleValueEntity;
import org.alfresco.repo.domain.propval.PropertyIdQueryParameter;
import org.alfresco.repo.domain.propval.PropertyIdQueryResult;
import org.alfresco.repo.domain.propval.PropertyIdSearchRow;
import org.alfresco.repo.domain.propval.PropertyLinkEntity;
import org.alfresco.repo.domain.propval.PropertyRootEntity;
import org.alfresco.repo.domain.propval.PropertySerializableValueEntity;
import org.alfresco.repo.domain.propval.PropertyStringQueryEntity;
import org.alfresco.repo.domain.propval.PropertyStringValueEntity;
import org.alfresco.repo.domain.propval.PropertyUniqueContextEntity;
import org.alfresco.repo.domain.propval.PropertyValueEntity;
import org.alfresco.repo.domain.propval.PropertyValueEntity.PersistedType;
import org.alfresco.util.Pair;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * iBatis-specific implementation of the PropertyValue DAO.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class PropertyValueDAOImpl extends AbstractPropertyValueDAOImpl
{
    private static final String SELECT_PROPERTY_CLASS_BY_ID = "alfresco.propval.select_PropertyClassByID";
    private static final String SELECT_PROPERTY_CLASS_BY_NAME = "alfresco.propval.select_PropertyClassByName";
    private static final String INSERT_PROPERTY_CLASS = "alfresco.propval.insert.insert_PropertyClass";
    
    private static final String SELECT_PROPERTY_DATE_VALUE_BY_ID = "alfresco.propval.select_PropertyDateValueByID";
    private static final String SELECT_PROPERTY_DATE_VALUE_BY_VALUE = "alfresco.propval.select_PropertyDateValueByValue";
    private static final String INSERT_PROPERTY_DATE_VALUE = "alfresco.propval.insert.insert_PropertyDateValue";
    
    private static final String SELECT_PROPERTY_STRING_VALUE_BY_ID = "alfresco.propval.select_PropertyStringValueByID";
    private static final String SELECT_PROPERTY_STRING_VALUE_BY_VALUE = "alfresco.propval.select_PropertyStringValueByValue";
    private static final String INSERT_PROPERTY_STRING_VALUE = "alfresco.propval.insert.insert_PropertyStringValue";
    
    private static final String SELECT_PROPERTY_DOUBLE_VALUE_BY_ID = "alfresco.propval.select_PropertyDoubleValueByID";
    private static final String SELECT_PROPERTY_DOUBLE_VALUE_BY_VALUE = "alfresco.propval.select_PropertyDoubleValueByValue";
    private static final String INSERT_PROPERTY_DOUBLE_VALUE = "alfresco.propval.insert.insert_PropertyDoubleValue";
    
    private static final String SELECT_PROPERTY_SERIALIZABLE_VALUE_BY_ID = "alfresco.propval.select_PropertySerializableValueByID";
    private static final String INSERT_PROPERTY_SERIALIZABLE_VALUE = "alfresco.propval.insert.insert_PropertySerializableValue";
    
    private static final String SELECT_PROPERTY_VALUE_BY_ID = "alfresco.propval.select_PropertyValueById";
    private static final String SELECT_PROPERTY_VALUE_BY_LOCAL_VALUE = "alfresco.propval.select_PropertyValueByLocalValue";
    private static final String SELECT_PROPERTY_VALUE_BY_DOUBLE_VALUE = "alfresco.propval.select_PropertyValueByDoubleValue";
    private static final String SELECT_PROPERTY_VALUE_BY_STRING_VALUE = "alfresco.propval.select_PropertyValueByStringValue";
    private static final String INSERT_PROPERTY_VALUE = "alfresco.propval.insert.insert_PropertyValue";
    
    private static final String SELECT_PROPERTY_BY_ID = "alfresco.propval.select_PropertyById";
    private static final String SELECT_PROPERTIES_BY_IDS = "alfresco.propval.select_PropertiesByIds";
    private static final String SELECT_PROPERTY_ROOT_BY_ID = "alfresco.propval.select_PropertyRootById";
    private static final String INSERT_PROPERTY_ROOT = "alfresco.propval.insert.insert_PropertyRoot";
    private static final String UPDATE_PROPERTY_ROOT = "alfresco.propval.update_PropertyRoot";
    private static final String DELETE_PROPERTY_ROOT_BY_ID = "alfresco.propval.delete_PropertyRootById";
    
    private static final String SELECT_PROPERTY_UNIQUE_CTX_BY_ID = "alfresco.propval.select_PropertyUniqueContextById";
    private static final String SELECT_PROPERTY_UNIQUE_CTX_BY_VALUES = "alfresco.propval.select_PropertyUniqueContextByValues";
    private static final String INSERT_PROPERTY_UNIQUE_CTX = "alfresco.propval.insert.insert_PropertyUniqueContext";
    private static final String UPDATE_PROPERTY_UNIQUE_CTX = "alfresco.propval.update_PropertyUniqueContext";
    private static final String DELETE_PROPERTY_UNIQUE_CTX_BY_ID = "alfresco.propval.delete_PropertyUniqueContextById";
    private static final String DELETE_PROPERTY_UNIQUE_CTX_BY_VALUES = "alfresco.propval.delete_PropertyUniqueContextByValues";
    
    private static final String INSERT_PROPERTY_LINK = "alfresco.propval.insert.insert_PropertyLink";
    private static final String DELETE_PROPERTY_LINKS_BY_ROOT_ID = "alfresco.propval.delete_PropertyLinksByRootId";
    
    
    private SqlSessionTemplate template;
    
    public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) 
    {
        this.template = sqlSessionTemplate;
    }
    
    
    //================================
    // 'alf_prop_class' accessors
    //================================

    @Override
    protected PropertyClassEntity findClassById(Long id)
    {
        PropertyClassEntity entity = new PropertyClassEntity();
        entity.setId(id);
        entity = (PropertyClassEntity) template.selectOne(
                SELECT_PROPERTY_CLASS_BY_ID,
                entity);
        // Done
        return entity;
    }

    @Override
    protected PropertyClassEntity findClassByValue(Class<?> value)
    {
        PropertyClassEntity entity = new PropertyClassEntity();
        entity.setJavaClass(value);
        entity = (PropertyClassEntity) template.selectOne(
                SELECT_PROPERTY_CLASS_BY_NAME,
                entity);
        // Done
        return entity;
    }

    @Override
    protected PropertyClassEntity createClass(Class<?> value)
    {
        PropertyClassEntity entity = new PropertyClassEntity();
        entity.setJavaClass(value);
        template.insert(INSERT_PROPERTY_CLASS, entity);
        // Done
        return entity;
    }

    //================================
    // 'alf_prop_date_value' accessors
    //================================

    @Override
    protected PropertyDateValueEntity findDateValueById(Long id)
    {
        PropertyDateValueEntity entity = (PropertyDateValueEntity) template.selectOne(
                SELECT_PROPERTY_DATE_VALUE_BY_ID,
                id);
        // Done
        return entity;
    }

    @Override
    protected PropertyDateValueEntity findDateValueByValue(Date value)
    {
        PropertyDateValueEntity result = (PropertyDateValueEntity) template.selectOne(
                SELECT_PROPERTY_DATE_VALUE_BY_VALUE,
                new Long(value.getTime()));
        // The ID is the actual time in ms (GMT)
        return result;
    }

    @Override
    protected PropertyDateValueEntity createDateValue(Date value)
    {
        PropertyDateValueEntity entity = new PropertyDateValueEntity();
        entity.setValue(value);
        template.insert(INSERT_PROPERTY_DATE_VALUE, entity);
        // Done
        return entity;
    }

    //================================
    // 'alf_prop_string_value' accessors
    //================================

    @Override
    protected String findStringValueById(Long id)
    {
        PropertyStringValueEntity entity = new PropertyStringValueEntity();
        entity.setId(id);
        String value = (String) template.selectOne(
                SELECT_PROPERTY_STRING_VALUE_BY_ID,
                entity);
        // Done
        return value;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Long findStringValueByValue(String value)
    {
        PropertyStringValueEntity entity = new PropertyStringValueEntity();
        entity.setValue(value);
        List<Long> rows = (List<Long>) template.selectList(
                SELECT_PROPERTY_STRING_VALUE_BY_VALUE,
                entity,
                new RowBounds(0, 1));
        // The CRC match prevents incorrect results from coming back.  Although there could be
        // several matches, we are sure that the matches are case-sensitive.
        if (rows.size() > 0)
        {
            return rows.get(0);
        }
        else
        {
            return null;
        }
    }

    @Override
    protected Long createStringValue(String value)
    {
        PropertyStringValueEntity entity = new PropertyStringValueEntity();
        entity.setValue(value);
        template.insert(INSERT_PROPERTY_STRING_VALUE, entity);
        // Done
        return entity.getId();
    }

    //================================
    // 'alf_prop_double_value' accessors
    //================================

    @Override
    protected PropertyDoubleValueEntity findDoubleValueById(Long id)
    {
        PropertyDoubleValueEntity entity = new PropertyDoubleValueEntity();
        entity.setId(id);
        entity = (PropertyDoubleValueEntity) template.selectOne(
                SELECT_PROPERTY_DOUBLE_VALUE_BY_ID,
                entity);
        // Done
        return entity;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected PropertyDoubleValueEntity findDoubleValueByValue(Double value)
    {
        PropertyDoubleValueEntity entity = new PropertyDoubleValueEntity();
        entity.setDoubleValue(value);
        List<PropertyDoubleValueEntity> results = (List<PropertyDoubleValueEntity>) template.selectList(
                SELECT_PROPERTY_DOUBLE_VALUE_BY_VALUE,
                entity,
                new RowBounds(0, 1));
        // There could be several matches, so just get one
        if (results.size() > 0)
        {
            return results.get(0);
        }
        else
        {
            // No match
            return null;
        }
    }
    
    @Override
    protected PropertyDoubleValueEntity createDoubleValue(Double value)
    {
        PropertyDoubleValueEntity entity = new PropertyDoubleValueEntity();
        entity.setDoubleValue(value);
        template.insert(INSERT_PROPERTY_DOUBLE_VALUE, entity);
        // Done
        return entity;
    }

    //================================
    // 'alf_prop_serializable_value' accessors
    //================================

    @Override
    protected PropertySerializableValueEntity findSerializableValueById(Long id)
    {
        PropertySerializableValueEntity entity = new PropertySerializableValueEntity();
        entity.setId(id);
        entity = (PropertySerializableValueEntity) template.selectOne(
                SELECT_PROPERTY_SERIALIZABLE_VALUE_BY_ID,
                entity);
        // Done
        return entity;
    }

    @Override
    protected PropertySerializableValueEntity createSerializableValue(Serializable value)
    {
        PropertySerializableValueEntity entity = new PropertySerializableValueEntity();
        entity.setSerializableValue(value);
        template.insert(INSERT_PROPERTY_SERIALIZABLE_VALUE, entity);
        // Done
        return entity;
    }

    //================================
    // 'alf_prop_value' accessors
    //================================

    @SuppressWarnings("unchecked")
    @Override
    protected PropertyValueEntity findPropertyValueById(Long id)
    {
        PropertyValueEntity entity = new PropertyValueEntity();
        entity.setId(id);
        List<PropertyValueEntity> results = (List<PropertyValueEntity>) template.selectList(
                SELECT_PROPERTY_VALUE_BY_ID,
                entity);
        // At most one of the results represents a real value
        int size = results.size();
        if (size == 0)
        {
            return null;
        }
        else if (size == 1)
        {
            return results.get(0);
        }
        else
        {
            logger.error("Found property value linked to multiple raw types: " + results);
            return results.get(0);
        }
    }

    @Override
    protected PropertyValueEntity findPropertyValueByValue(Serializable value)
    {
        // Get the actual type ID
        Class<?> clazz = (value == null ? Object.class : value.getClass());
        Pair<Long, Class<?>> clazzPair = getPropertyClass(clazz);
        if (clazzPair == null)
        {
            // Shortcut: There are no properties of this type
            return null;
        }
        Long actualTypeId = clazzPair.getFirst();
        
        // Construct the search parameters
        PropertyValueEntity queryEntity = new PropertyValueEntity();
        queryEntity.setValue(value, converter);
        queryEntity.setActualTypeId(actualTypeId);
        
        // How would it be persisted?
        PersistedType persistedType = queryEntity.getPersistedTypeEnum();
        Short persistedTypeId = queryEntity.getPersistedType();
        
        // Query based on the the persistable value type
        String query = null;
        Object queryObject = queryEntity;

        // Handle each persisted type individually

        switch (persistedType)
        {
        case NULL:
        case LONG:
            query = SELECT_PROPERTY_VALUE_BY_LOCAL_VALUE;
            break;
        case DOUBLE:
            query = SELECT_PROPERTY_VALUE_BY_DOUBLE_VALUE;
            break;
        case CONSTRUCTABLE:
            // The string value is the name of the class (e.g. 'java.util.HashMap')
        case ENUM:
            // The string-equivalent representation
        case STRING:
            // It's best to query using the CRC and short end-value
            query = SELECT_PROPERTY_VALUE_BY_STRING_VALUE;
            queryObject = new PropertyStringQueryEntity(
                    persistedTypeId,
                    actualTypeId,
                    queryEntity.getStringValue());
            break;
        case SERIALIZABLE:
            // No query
            break;
        default:
            throw new IllegalStateException("Unhandled PersistedType value: " + persistedType);
        }
        
        // Now query
        PropertyValueEntity result = null;
        if (query != null)
        {
            // Uniqueness is guaranteed by the tables, so we get one value only
            result = (PropertyValueEntity) template.selectOne(query, queryObject);
        }
        
        // Done
        return result;
    }
    
    @Override
    protected PropertyValueEntity createPropertyValue(Serializable value)
    {
        // Get the actual type ID
        Class<?> clazz = (value == null ? Object.class : value.getClass());
        Pair<Long, Class<?>> clazzPair = getOrCreatePropertyClass(clazz);
        Long actualTypeId = clazzPair.getFirst();
        
        // Construct the insert entity
        PropertyValueEntity insertEntity = new PropertyValueEntity();
        insertEntity.setValue(value, converter);
        insertEntity.setActualTypeId(actualTypeId);
        
        // Persist the persisted value
        switch (insertEntity.getPersistedTypeEnum())
        {
        case DOUBLE:
            Double doubleValue = insertEntity.getDoubleValue();
            Pair<Long, Double> insertDoublePair = getOrCreatePropertyDoubleValue(doubleValue);
            insertEntity.setLongValue(insertDoublePair.getFirst());
            break;
        case STRING:
        case CONSTRUCTABLE:
        case ENUM:
            String stringValue = insertEntity.getStringValue();
            Pair<Long, String> insertStringPair = getOrCreatePropertyStringValue(stringValue);
            insertEntity.setLongValue(insertStringPair.getFirst());
            break;
        case SERIALIZABLE:
            Pair<Long, Serializable> insertSerializablePair = createPropertySerializableValue(value);
            insertEntity.setLongValue(insertSerializablePair.getFirst());
            break;
        case NULL:
        case LONG:
            // Do nothing for these
            break;
        default:
            throw new IllegalStateException("Unknown PersistedType enum: " + insertEntity.getPersistedTypeEnum());
        }
        
        // Persist the entity
        template.insert(INSERT_PROPERTY_VALUE, insertEntity);
        // Done
        return insertEntity;
    }

    //================================
    // 'alf_prop_root' accessors
    //================================

    @SuppressWarnings("unchecked")
    @Override
    protected List<PropertyIdSearchRow> findPropertyById(Long id)
    {
        PropertyValueEntity entity = new PropertyValueEntity();
        entity.setId(id);
        List<PropertyIdSearchRow> results = (List<PropertyIdSearchRow>) template.selectList(
                SELECT_PROPERTY_BY_ID,
                entity);
        return results;
    }

    private static final String[] KEY_COLUMNS_FINDBYIDS = new String[] {"propId"};
    @Override
    protected void findPropertiesByIds(List<Long> ids, final PropertyFinderCallback callback)
    {
        ResultHandler valueResultHandler = new ResultHandler()
        {
            public void handleResult(ResultContext context)
            {
                PropertyIdQueryResult result = (PropertyIdQueryResult) context.getResultObject();
                Long id = result.getPropId();
                // Make the serializable value
                List<PropertyIdSearchRow> rows = result.getPropValues();
                Serializable value = convertPropertyIdSearchRows(rows);
                callback.handleProperty(id, value);
            }
        };
        // A row handler to roll up individual rows
        RollupResultHandler rollupResultHandler = new RollupResultHandler(
                KEY_COLUMNS_FINDBYIDS,
                "propValues",
                valueResultHandler);
        // Query using the IDs
        PropertyIdQueryParameter params = new PropertyIdQueryParameter();
        params.setRootPropIds(ids);
        template.select(SELECT_PROPERTIES_BY_IDS, params, rollupResultHandler);
        // Process any remaining results
        rollupResultHandler.processLastResults();
        // Done
    }

    @Override
    protected Long createPropertyRoot()
    {
        PropertyRootEntity rootEntity = new PropertyRootEntity();
        rootEntity.setVersion((short)0);
        template.insert(INSERT_PROPERTY_ROOT, rootEntity);
        return rootEntity.getId();
    }

    @Override
    protected PropertyRootEntity getPropertyRoot(Long id)
    {
        PropertyRootEntity entity = new PropertyRootEntity();
        entity.setId(id);
        return (PropertyRootEntity) template.selectOne(SELECT_PROPERTY_ROOT_BY_ID, entity);
    }

    @Override
    protected PropertyRootEntity updatePropertyRoot(PropertyRootEntity entity)
    {
        entity.incrementVersion();
        int updated = template.update(UPDATE_PROPERTY_ROOT, entity);
        if (updated != 1)
        {
            // unexpected number of rows affected
            throw new ConcurrencyFailureException("Incorrect number of rows affected for updatePropertyRoot: " + entity + ": expected 1, actual " + updated);
        }
        return entity;
    }

    @Override
    protected void deletePropertyRoot(Long id)
    {
        PropertyRootEntity entity = new PropertyRootEntity();
        entity.setId(id);
        template.delete(DELETE_PROPERTY_ROOT_BY_ID, entity);
    }

    @Override
    protected PropertyUniqueContextEntity createPropertyUniqueContext(
            Long valueId1, Long valueId2, Long valueId3,
            Long propertyId)
    {
        PropertyUniqueContextEntity entity = new PropertyUniqueContextEntity();
        entity.setValue1PropId(valueId1);
        entity.setValue2PropId(valueId2);
        entity.setValue3PropId(valueId3);
        entity.setPropertyId(propertyId);
        template.insert(INSERT_PROPERTY_UNIQUE_CTX, entity);
        return entity;
    }

    @Override
    protected PropertyUniqueContextEntity getPropertyUniqueContextById(Long id)
    {
        PropertyUniqueContextEntity entity = new PropertyUniqueContextEntity();
        entity.setId(id);
        entity = (PropertyUniqueContextEntity) template.selectOne(SELECT_PROPERTY_UNIQUE_CTX_BY_ID, entity);
        return entity;
    }

    @Override
    protected PropertyUniqueContextEntity getPropertyUniqueContextByValues(Long valueId1, Long valueId2, Long valueId3)
    {
        PropertyUniqueContextEntity entity = new PropertyUniqueContextEntity();
        entity.setValue1PropId(valueId1);
        entity.setValue2PropId(valueId2);
        entity.setValue3PropId(valueId3);
        entity = (PropertyUniqueContextEntity) template.selectOne(SELECT_PROPERTY_UNIQUE_CTX_BY_VALUES, entity);
        return entity;
    }
    
    @Override
    protected void getPropertyUniqueContextByValues(final PropertyUniqueContextCallback callback, Long... valueIds)
    {
        PropertyUniqueContextEntity entity = new PropertyUniqueContextEntity();
        for (int i = 0; i < valueIds.length; i++)
        {
            switch (i)
            {
            case 0:
                entity.setValue1PropId(valueIds[i]);
                break;
            case 1:
                entity.setValue2PropId(valueIds[i]);
                break;
            case 2:
                entity.setValue3PropId(valueIds[i]);
                break;
            default:
                throw new IllegalArgumentException("Only 3 ids allowed");
            }
        }
        
        ResultHandler valueResultHandler = new ResultHandler()
        {
            public void handleResult(ResultContext context)
            {
                PropertyUniqueContextEntity result = (PropertyUniqueContextEntity) context.getResultObject();
                
                Long id = result.getId();
                Long propId = result.getPropertyId();
                Serializable[] keys = new Serializable[3];
                keys[0] = result.getValue1PropId();
                keys[1] = result.getValue2PropId();
                keys[2] = result.getValue3PropId();
                
                callback.handle(id, propId, keys);
            }
        };
        
        template.select(SELECT_PROPERTY_UNIQUE_CTX_BY_VALUES, entity, valueResultHandler);
        // Done
    }

    @Override
    protected PropertyUniqueContextEntity updatePropertyUniqueContext(PropertyUniqueContextEntity entity)
    {
        entity.incrementVersion();
        int updated = template.update(UPDATE_PROPERTY_UNIQUE_CTX, entity);
        if (updated != 1)
        {
            // unexpected number of rows affected
            throw new ConcurrencyFailureException("Incorrect number of rows affected for updatePropertyUniqueContext: " + entity + ": expected 1, actual " + updated);
        }
        return entity;
    }

    public void deletePropertyUniqueContext(Long id)
    {
        PropertyUniqueContextEntity entity = new PropertyUniqueContextEntity();
        entity.setId(id);
        template.delete(DELETE_PROPERTY_UNIQUE_CTX_BY_ID, entity);
    }

    @Override
    protected int deletePropertyUniqueContexts(Long... valueIds)
    {
        PropertyUniqueContextEntity entity = new PropertyUniqueContextEntity();
        for (int i = 0; i < valueIds.length; i++)
        {
            switch (i)
            {
            case 0:
                entity.setValue1PropId(valueIds[i]);
                break;
            case 1:
                entity.setValue2PropId(valueIds[i]);
                break;
            case 2:
                entity.setValue3PropId(valueIds[i]);
                break;
            default:
                throw new IllegalArgumentException("Only 3 ids allowed");
            }
        }
        return template.delete(DELETE_PROPERTY_UNIQUE_CTX_BY_VALUES, entity);
    }

    @Override
    protected void createPropertyLink(
            Long rootPropId,
            Long propIndex,
            Long containedIn,
            Long keyPropId,
            Long valuePropId)
    {
        PropertyLinkEntity insertEntity = new PropertyLinkEntity();
        insertEntity.setRootPropId(rootPropId);
        insertEntity.setPropIndex(propIndex);
        insertEntity.setContainedIn(containedIn);
        insertEntity.setKeyPropId(keyPropId);
        insertEntity.setValuePropId(valuePropId);
        template.insert(INSERT_PROPERTY_LINK, insertEntity);
        // Done
    }

    @Override
    protected int deletePropertyLinks(Long rootPropId)
    {
        PropertyRootEntity entity = new PropertyRootEntity();
        entity.setId(rootPropId);
        return template.delete(DELETE_PROPERTY_LINKS_BY_ROOT_ID, entity);
    }
}
