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

import java.util.List;

import org.alfresco.repo.domain.propval.AbstractPropertyValueDAOImpl;
import org.alfresco.repo.domain.propval.PropertyClassEntity;
import org.alfresco.repo.domain.propval.PropertyStringValueEntity;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

/**
 * iBatis-specific implementation of the PropertyValue DAO.
 * 
 * @author Derek Hulley
 * @since 3.3
 */
public class PropertyValueDAOImpl extends AbstractPropertyValueDAOImpl
{
    private static final Long VERSION_ONE = new Long(1L);
    private static final String SELECT_PROPERTY_CLASS_BY_ID = "select.PropertyClassByID";
    private static final String SELECT_PROPERTY_CLASS_BY_NAME = "select.PropertyClassByName";
    private static final String INSERT_PROPERTY_CLASS = "insert.PropertyClass";
    private static final String SELECT_PROPERTY_STRING_VALUE_BY_ID = "select.PropertyStringValueByID";
    private static final String SELECT_PROPERTY_STRING_VALUE_BY_STRING = "select.PropertyStringValueByString";
    private static final String INSERT_PROPERTY_STRING_VALUE = "insert.PropertyStringValue";
    
    private SqlMapClientTemplate template;

    public void setSqlMapClientTemplate(SqlMapClientTemplate sqlMapClientTemplate)
    {
        this.template = sqlMapClientTemplate;
    }

    @Override
    protected PropertyClassEntity findClassById(Long id)
    {
        PropertyClassEntity entity = new PropertyClassEntity();
        entity.setId(id);
        entity = (PropertyClassEntity) template.queryForObject(SELECT_PROPERTY_CLASS_BY_ID, entity);
        // Done
        return entity;
    }

    @Override
    protected PropertyClassEntity findClassByValue(Class<?> value)
    {
        PropertyClassEntity entity = new PropertyClassEntity();
        entity.setJavaClass(value);
        entity = (PropertyClassEntity) template.queryForObject(SELECT_PROPERTY_CLASS_BY_NAME, entity);
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

    @Override
    protected PropertyStringValueEntity findStringValueById(Long id)
    {
        PropertyStringValueEntity entity = new PropertyStringValueEntity();
        entity.setId(id);
        entity = (PropertyStringValueEntity) template.queryForObject(SELECT_PROPERTY_STRING_VALUE_BY_ID, entity);
        // Done
        return entity;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected PropertyStringValueEntity findStringValueByValue(String value)
    {
        PropertyStringValueEntity entity = new PropertyStringValueEntity();
        entity.setStringValue(value);
        List<PropertyStringValueEntity> results = (List<PropertyStringValueEntity>) template.queryForList(SELECT_PROPERTY_STRING_VALUE_BY_STRING, entity);
        // There are several matches, so find the first one that matches exactly
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
}
