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
package org.alfresco.repo.props;

import java.io.Serializable;

import org.alfresco.repo.domain.propval.PropertyValueDAO;
import org.springframework.extensions.surf.util.Pair;
import org.springframework.extensions.surf.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This component provides a clearer distinction between shared and unshared properties and
 * avoids the need to access the DAO, which is much more obscure and potentially harmful.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class PropertyValueComponentImpl implements PropertyValueComponent
{
    private static final Log logger = LogFactory.getLog(PropertyValueComponentImpl.class);
    
    private PropertyValueDAO propertyValueDAO;
    
    /**
     * Set the underlying DAO that manipulates the database data
     */
    public void setPropertyValueDAO(PropertyValueDAO propertyValueDAO)
    {
        this.propertyValueDAO = propertyValueDAO;
    }

    /**
     * Ensures that all necessary properties have been set
     */
    public void init()
    {
        PropertyCheck.mandatory(this, "propertyValueDAO", propertyValueDAO);
    }
    
    /**
     * @return                  If the pair value is not null, returns the first value (ID)
     */
    private Long getEntityPairId(Pair<Long, Serializable> pair)
    {
        if (pair == null)
        {
            return null;
        }
        else
        {
            return pair.getFirst();
        }
    }
    
    /**
     * @return                  If the pair value is not null, returns the second value (value)
     */
    private Serializable getEntityPairValue(Pair<Long, Serializable> pair)
    {
        if (pair == null)
        {
            return null;
        }
        else
        {
            return pair.getSecond();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Serializable getSharedValueById(Long id)
    {
        Pair<Long, Serializable> pair = propertyValueDAO.getPropertyValueById(id);
        return getEntityPairValue(pair);
    }
    /**
     * {@inheritDoc}
     */
    public Long getSharedValueId(Serializable value)
    {
        Pair<Long, Serializable> pair = propertyValueDAO.getPropertyValue(value);
        return getEntityPairId(pair);
    }
    /**
     * {@inheritDoc}
     */
    public Long getOrCreateSharedValue(Serializable value)
    {
        Pair<Long, Serializable> pair = propertyValueDAO.getOrCreatePropertyValue(value);
        return getEntityPairId(pair);
    }
    
    /**
     * {@inheritDoc}
     */
    public Serializable getUnsharedPropertyById(Long id)
    {
        return propertyValueDAO.getPropertyById(id);
    }
    /**
     * {@inheritDoc}
     */
    public Long createUnsharedProperty(Serializable value)
    {
        return propertyValueDAO.createProperty(value);
    }
    /**
     * {@inheritDoc}
     */
    public void updateUnsharedProperty(Long id, Serializable value)
    {
        propertyValueDAO.updateProperty(id, value);
    }
    /**
     * {@inheritDoc}
     */
    public void deleteUnsharedProperty(Long id)
    {
        propertyValueDAO.deleteProperty(id);
    }
    
    /**
     * {@inheritDoc}
     */
    public Long createPropertyUniqueContext(Serializable value1, Serializable value2, Serializable value3)
    {
        Long id = propertyValueDAO.createPropertyUniqueContext(value1, value2, value3);
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Created unique property context: \n" +
                    "   ID:     " + id + "\n" +
                    "   Value1: " + value1 + "\n" +
                    "   Value2: " + value2 + "\n" +
                    "   Value3: " + value3);
        }
        return id;
    }
    /**
     * {@inheritDoc}
     */
    public Long getPropertyUniqueContext(Serializable value1, Serializable value2, Serializable value3)
    {
        return propertyValueDAO.getPropertyUniqueContext(value1, value2, value3);
    }
    /**
     * {@inheritDoc}
     */
    public void updatePropertyUniqueContext(Long id, Serializable value1, Serializable value2, Serializable value3)
    {
        propertyValueDAO.updatePropertyUniqueContext(id, value1, value2, value3);
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Updated unique property context: \n" +
                    "   ID:     " + id + "\n" +
                    "   Value1: " + value1 + "\n" +
                    "   Value2: " + value2 + "\n" +
                    "   Value3: " + value3);
        }
    }
    /**
     * {@inheritDoc}
     */
    public Long updatePropertyUniqueContext(
            Serializable value1Before, Serializable value2Before, Serializable value3Before,
            Serializable value1, Serializable value2, Serializable value3)
    {
        Long id = getPropertyUniqueContext(value1Before, value2Before, value3Before);
        if (id == null)
        {
            // It didn't exist before, so just create it
            id = createPropertyUniqueContext(value1, value2, value3);
        }
        else
        {
            // Update itUpdated
            updatePropertyUniqueContext(id, value1, value2, value3);
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Updated unique property context: \n" +
                    "   Value1 Before: " + value1Before + "\n" +
                    "   Value2 Before: " + value2Before + "\n" +
                    "   Value3 Before: " + value3Before + "\n" +
                    "   Value1:        " + value1 + "\n" +
                    "   Value2:        " + value2 + "\n" +
                    "   Value3:        " + value3);
        }
        return id;
    }
    /**
     * {@inheritDoc}
     */
    public void deletePropertyUniqueContext(Long id)
    {
        propertyValueDAO.deletePropertyUniqueContext(id);
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Deleted unique property context: \n" +
                    "   ID: " + id);
        }
    }
    /**
     * {@inheritDoc}
     */
    public int deletePropertyUniqueContexts(Serializable ... values)
    {
        int deleted = propertyValueDAO.deletePropertyUniqueContext(values);
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Deleted " + deleted + " unique property contexts: \n" +
                    "   Values: " + values);
        }
        return deleted;
    }
}
