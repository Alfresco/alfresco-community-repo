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

import org.alfresco.repo.domain.propval.AbstractPropertyValueDAOImpl;
import org.alfresco.repo.domain.propval.PropertyClassEntity;
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
    
    private SqlMapClientTemplate template;

    public void setSqlMapClientTemplate(SqlMapClientTemplate sqlMapClientTemplate)
    {
        this.template = sqlMapClientTemplate;
    }

    @Override
    protected PropertyClassEntity findClassById(Long id)
    {
        PropertyClassEntity propertyClassEntity = new PropertyClassEntity();
        propertyClassEntity.setId(id);
        propertyClassEntity = (PropertyClassEntity) template.queryForObject(SELECT_PROPERTY_CLASS_BY_ID, propertyClassEntity);
        // Done
        return propertyClassEntity;
    }

    @Override
    protected PropertyClassEntity findClassByValue(Class<?> value)
    {
        PropertyClassEntity propertyClassEntity = new PropertyClassEntity();
        propertyClassEntity.setJavaClass(value);
        propertyClassEntity = (PropertyClassEntity) template.queryForObject(SELECT_PROPERTY_CLASS_BY_NAME, propertyClassEntity);
        // Done
        return propertyClassEntity;
    }

    @Override
    protected PropertyClassEntity createClass(Class<?> value)
    {
        PropertyClassEntity propertyClassEntity = new PropertyClassEntity();
        propertyClassEntity.setJavaClass(value);
        propertyClassEntity.setVersion(VERSION_ONE);
        Long id = (Long) template.insert(INSERT_PROPERTY_CLASS, propertyClassEntity);
        propertyClassEntity.setId(id);
        // Done
        return propertyClassEntity;
    }
}
