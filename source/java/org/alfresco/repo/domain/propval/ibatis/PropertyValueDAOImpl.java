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
    private static final String SELECT_PROPERTY_STRING_VALUE_BY_STRING = "select.PropertyStringValueByString";
    private static final String INSERT_PROPERTY_STRING_VALUE = "insert.PropertyStringValue";
    private static final String SELECT_PROPERTY_DOUBLE_VALUE_BY_ID = "select.PropertyDoubleValueByID";
    private static final String SELECT_PROPERTY_DOUBLE_VALUE_BY_VALUE = "select.PropertyDoubleValueByValue";
    private static final String INSERT_PROPERTY_DOUBLE_VALUE = "insert.PropertyDoubleValue";
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
                SELECT_PROPERTY_STRING_VALUE_BY_STRING,
                entity);
        // There double be several matches, so find the first one that matches exactly
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
        // There coult be several matches, so find the first one that matches exactly
        for (PropertyDoubleValueEntity resultEntity : results)
        {
            if (value.equals(resultEntity.getDoubleValue()))
            {
                // Found a match
                return resultEntity;
            }
        }
        // No real match
        return null;
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
        // TODO: Full query pulling back all related values
        return null;
    }

    @Override
    protected PropertyValueEntity findPropertyValueByValue(Serializable value)
    {
        // TODO: Find out persisted type and perform relevant query
        return null;
    }

    @Override
    protected PropertyValueEntity createPropertyValue(Serializable value)
    {
        // Find out how it would be persisted
        Pair<Short, Serializable> persistedValuePair = converter.convertToPersistedType(value);
        Serializable persistedValue = persistedValuePair.getSecond();

        PropertyValueEntity entity = new PropertyValueEntity();

        PersistedType persistedType = PropertyValueEntity.persistedTypesByOrdinal.get(persistedValuePair.getFirst());
        entity.setPersistedType(persistedType.getOrdinalNumber());
        // Handle each persisted type individually
        if (persistedType.equals(PersistedType.NULL.getOrdinalNumber()))
        {
            entity.setLongValue(PropertyValueEntity.LONG_ZERO);
        }
        else if (persistedType.equals(PersistedType.BOOLEAN.getOrdinalNumber()))
        {
            Boolean booleanValue = (Boolean) persistedValue;
            entity.setLongValue(
                    booleanValue.booleanValue() ? PropertyValueEntity.LONG_ONE : PropertyValueEntity.LONG_ZERO);
        }
        else if (persistedType.equals(PersistedType.LONG.getOrdinalNumber()))
        {
            Long longValue = (Long) persistedValue;
            entity.setLongValue(longValue);
        }
        else if (persistedType.equals(PersistedType.DOUBLE.getOrdinalNumber()))
        {
            Double doubleValue = (Double) persistedValue;
            // Look it up
            Pair<Long, Double> entityPair = getOrCreatePropertyDoubleValue(doubleValue);
            entity.setLongValue(entityPair.getFirst());
            entity.setDoubleValue(doubleValue);
        }
        else if (persistedType.equals(PersistedType.STRING.getOrdinalNumber()))
        {
            String stringValue = (String) persistedValue;
            // Look it up
            Pair<Long, String> entityPair = getOrCreatePropertyStringValue(stringValue);
            entity.setLongValue(entityPair.getFirst());
            entity.setStringValue(stringValue);
        }
        else if (persistedType.equals(PersistedType.SERIALIZABLE.getOrdinalNumber()))
        {
            throw new UnsupportedOperationException("Serializable not done, yet.");
        }
        else
        {
            throw new IllegalStateException(
                    "The persisted property is not valid: \n" +
                    "   Raw Value:       " + value + "\n" +
                    "   Persisted Value: " + persistedValuePair + "\n" +
                    "   Converter:" + converter.getClass());
        }
        
        // Persist the entity
        Long id = (Long) template.insert(INSERT_PROPERTY_VALUE, entity);
        entity.setId(id);
        // Done
        return entity;
    }
}
