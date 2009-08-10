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
package org.alfresco.repo.domain.propval.ibatis;

import java.io.Serializable;
import java.util.List;

import org.alfresco.repo.domain.propval.AbstractPropertyValueDAOImpl;
import org.alfresco.repo.domain.propval.PropertyClassEntity;
import org.alfresco.repo.domain.propval.PropertyDoubleValueEntity;
import org.alfresco.repo.domain.propval.PropertyStringValueEntity;
import org.alfresco.repo.domain.propval.PropertyValueEntity;
import org.alfresco.repo.domain.propval.PropertyValueEntity.PersistedType;
import org.alfresco.util.Pair;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

/**
 * iBatis-specific implementation of the PropertyValue DAO.
 * 
 * @author Derek Hulley
 * @since 3.3
 */
public class PropertyValueDAOImpl extends AbstractPropertyValueDAOImpl
{
    private static final String SELECT_PROPERTY_CLASS_BY_ID = "select.PropertyClassByID";
    private static final String SELECT_PROPERTY_CLASS_BY_NAME = "select.PropertyClassByName";
    private static final String INSERT_PROPERTY_CLASS = "insert.PropertyClass";
    
    private static final String SELECT_PROPERTY_STRING_VALUE_BY_ID = "select.PropertyStringValueByID";
    private static final String SELECT_PROPERTY_STRING_VALUE_BY_VALUE = "select.PropertyStringValueByValue";
    private static final String INSERT_PROPERTY_STRING_VALUE = "insert.PropertyStringValue";
    
    private static final String SELECT_PROPERTY_DOUBLE_VALUE_BY_ID = "select.PropertyDoubleValueByID";
    private static final String SELECT_PROPERTY_DOUBLE_VALUE_BY_VALUE = "select.PropertyDoubleValueByValue";
    private static final String INSERT_PROPERTY_DOUBLE_VALUE = "insert.PropertyDoubleValue";
    
    private static final String SELECT_PROPERTY_VALUE_BY_ID = "select.PropertyValueById";
    private static final String SELECT_PROPERTY_VALUE_BY_LOCAL_VALUE = "select.PropertyValueByLocalValue";
    private static final String SELECT_PROPERTY_VALUE_BY_DOUBLE_VALUE = "select.PropertyValueByDoubleValue";
    private static final String SELECT_PROPERTY_VALUE_BY_STRING_VALUE = "select.PropertyValueByStringValue";
    private static final String INSERT_PROPERTY_VALUE = "insert.PropertyValue";
    
    private SqlMapClientTemplate template;

    public void setSqlMapClientTemplate(SqlMapClientTemplate sqlMapClientTemplate)
    {
        this.template = sqlMapClientTemplate;
    }

    //================================
    // 'alf_prop_class' accessors
    //================================

    @Override
    protected PropertyClassEntity findClassById(Long id)
    {
        PropertyClassEntity entity = new PropertyClassEntity();
        entity.setId(id);
        entity = (PropertyClassEntity) template.queryForObject(
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
        entity = (PropertyClassEntity) template.queryForObject(
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
        Long id = (Long) template.insert(INSERT_PROPERTY_CLASS, entity);
        entity.setId(id);
        // Done
        return entity;
    }

    //================================
    // 'alf_prop_string_value' accessors
    //================================

    @Override
    protected PropertyStringValueEntity findStringValueById(Long id)
    {
        PropertyStringValueEntity entity = new PropertyStringValueEntity();
        entity.setId(id);
        entity = (PropertyStringValueEntity) template.queryForObject(
                SELECT_PROPERTY_STRING_VALUE_BY_ID,
                entity);
        // Done
        return entity;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected PropertyStringValueEntity findStringValueByValue(String value)
    {
        PropertyStringValueEntity entity = new PropertyStringValueEntity();
        entity.setStringValue(value);
        List<PropertyStringValueEntity> results = (List<PropertyStringValueEntity>) template.queryForList(
                SELECT_PROPERTY_STRING_VALUE_BY_VALUE,
                entity);
        // There double be several matches (if the database is case-insensitive), so find the first
        // value that matches exactly.
        for (PropertyStringValueEntity resultEntity : results)
        {
            if (value.equals(resultEntity.getStringValue()))
            {
                // Found a match
                return resultEntity;
            }
        }
        // No real match
        return null;
    }

    @Override
    protected PropertyStringValueEntity createStringValue(String value)
    {
        PropertyStringValueEntity entity = new PropertyStringValueEntity();
        entity.setStringValue(value);
        Long id = (Long) template.insert(INSERT_PROPERTY_STRING_VALUE, entity);
        entity.setId(id);
        // Done
        return entity;
    }

    //================================
    // 'alf_prop_double_value' accessors
    //================================

    @Override
    protected PropertyDoubleValueEntity findDoubleValueById(Long id)
    {
        PropertyDoubleValueEntity entity = new PropertyDoubleValueEntity();
        entity.setId(id);
        entity = (PropertyDoubleValueEntity) template.queryForObject(
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
        List<PropertyDoubleValueEntity> results = (List<PropertyDoubleValueEntity>) template.queryForList(
                SELECT_PROPERTY_DOUBLE_VALUE_BY_VALUE,
                entity);
        // There coult be several matches, so take the first one
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
        Long id = (Long) template.insert(INSERT_PROPERTY_DOUBLE_VALUE, entity);
        entity.setId(id);
        // Done
        return entity;
    }

    //================================
    // 'alf_prop_value' accessors
    //================================

    @Override
    protected PropertyValueEntity findPropertyValueById(Long id)
    {
        PropertyValueEntity entity = new PropertyValueEntity();
        entity.setId(id);
        entity = (PropertyValueEntity) template.queryForObject(
                SELECT_PROPERTY_VALUE_BY_ID,
                entity);
        // Done
        return entity;
    }

    @SuppressWarnings("unchecked")
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
        
        // Query based on the the persistable value type
        String query = null;
        boolean singleResult = true;                // false if multiple query results are possible

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
        case STRING:
            query = SELECT_PROPERTY_VALUE_BY_STRING_VALUE;
            singleResult = false;
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
            if (singleResult)
            {
                result = (PropertyValueEntity) template.queryForObject(query, queryEntity);
            }
            else
            {
                Serializable queryValue = queryEntity.getPersistedValue();
                List<PropertyValueEntity> results = (List<PropertyValueEntity>) template.queryForList(
                        query,
                        queryEntity);
                for (PropertyValueEntity row : results)
                {
                    if (queryValue.equals(row.getPersistedValue()))
                    {
                        // We have a match
                        result = row;
                        break;
                    }
                }
            }
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
            String stringValue = insertEntity.getStringValue();
            Pair<Long, String> insertStringPair = getOrCreatePropertyStringValue(stringValue);
            insertEntity.setLongValue(insertStringPair.getFirst());
            break;
        case SERIALIZABLE:
            throw new UnsupportedOperationException("Serializable not supported, yet.");
//            Pair<Long, Serializable> insertSerializablePair = getOrCreatePropertySerializableValue(value);
//            insertEntity.setLongValue(insertSerializablePair.getFirst());
//            break;
        case NULL:
        case LONG:
            // Do nothing for these
            break;
        default:
            throw new IllegalStateException("Unknown PersistedType enum: " + insertEntity.getPersistedTypeEnum());
        }
        
        // Persist the entity
        Long id = (Long) template.insert(INSERT_PROPERTY_VALUE, insertEntity);
        insertEntity.setId(id);
        // Done
        return insertEntity;
    }
}
