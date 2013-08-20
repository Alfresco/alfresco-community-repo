/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.attributes;

import java.io.Serializable;
import java.util.Arrays;

import org.alfresco.repo.domain.propval.PropertyUniqueConstraintViolation;
import org.alfresco.repo.domain.propval.PropertyValueDAO;
import org.alfresco.repo.domain.propval.PropertyValueDAO.PropertyUniqueContextCallback;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.attributes.DuplicateAttributeException;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Layers on the storage of property values to provide generic attribute storage
 * 
 * @see PropertyValueDAO
 * @author Derek Hulley
 */
public class AttributeServiceImpl implements AttributeService
{
    private static final Log logger = LogFactory.getLog(AttributeServiceImpl.class);
    
    private PropertyValueDAO propertyValueDAO;

    public AttributeServiceImpl()
    {
    }

    /**
     * Set the DAO that handles the unique property persistence
     */
    public void setPropertyValueDAO(PropertyValueDAO propertyValueDAO)
    {
        this.propertyValueDAO = propertyValueDAO;
    }
    
    /**
     * Formalize the shape of the variable-size array.
     * 
     * @param keys              the variable-size array of 1 to 3 keys
     * @return                  an array of exactly 3 keys (incl. <tt>null</tt> values)
     */
    private Serializable[] normalizeKeys(Serializable ... keys)
    {
        if (keys.length < 1 || keys.length > 3)
        {
            ParameterCheck.mandatory("keys", null);
        }
        return new Serializable[]
        {
                keys[0],
                keys.length > 1 ? keys[1] : null,
                keys.length > 2 ? keys[2] : null
        };
    }

    /**
     * {@inheritDoc}
     */
    public boolean exists(Serializable ... keys)
    {
        keys = normalizeKeys(keys);
        Pair<Long, Long> pair = propertyValueDAO.getPropertyUniqueContext(keys[0], keys[1], keys[2]);
        boolean exists = (pair != null);
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Check attribute exists: \n" +
                    "   Keys:   " + Arrays.asList(keys) + "\n" +
                    "   exists: " + exists);
        }
        return exists;
    }

    /**
     * {@inheritDoc}
     */
    public Serializable getAttribute(Serializable ... keys)
    {
        keys = normalizeKeys(keys);
        Pair<Long, Long> pair = propertyValueDAO.getPropertyUniqueContext(keys[0], keys[1], keys[2]);
        Serializable value = null;
        if (pair != null && pair.getSecond() != null)
        {
            Long valueId = pair.getSecond();
            value = propertyValueDAO.getPropertyById(valueId);
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Got attribute: \n" +
                    "   Keys:   " + Arrays.asList(keys) + "\n" +
                    "   Value: " + value);
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    public void getAttributes(final AttributeQueryCallback callback, Serializable ... keys)
    {
        PropertyUniqueContextCallback propertyUniqueContextCallback = new PropertyUniqueContextCallback()
        {
            private boolean more = true;
            public void handle(Long id, Long valueId, Serializable[] resultKeyIds)
            {
                if (!more)
                {
                    // The callback has terminated fetching
                    return;
                }
                
                Serializable value = null;
                if (valueId != null)
                {
                    value = propertyValueDAO.getPropertyById(valueId);
                }
                
                Serializable[] resultsKeyValues = new Serializable[resultKeyIds.length];
                for (int i = 0; i < resultKeyIds.length; i++)
                {
                    if (resultKeyIds[i] != null)
                    {
                        Pair<Long, Serializable> keyValuePair = propertyValueDAO.getPropertyValueById((Long)resultKeyIds[i]);
                        resultsKeyValues[i] = (keyValuePair != null ? keyValuePair.getSecond() : null);
                    }
                }
                
                more = callback.handleAttribute(id, value, resultsKeyValues);
                
                // Done
                if (logger.isTraceEnabled())
                {
                    logger.trace(
                            "Got attribute: \n" +
                            "   Keys:   " + Arrays.asList(resultsKeyValues) + "\n" +
                            "   Value: " + value);
                }
            }
        };
        propertyValueDAO.getPropertyUniqueContext(propertyUniqueContextCallback, keys);
        // Done
    }
    
    /**
     * {@inheritDoc}
     */
    public void setAttribute(Serializable value, Serializable ... keys)
    {
        keys = normalizeKeys(keys);
        Pair<Long, Long> pair = propertyValueDAO.getPropertyUniqueContext(keys[0], keys[1], keys[2]);
        if (pair == null)
        {
            // We can create it.  Any concurrency issue will be handled by the transaction.
            propertyValueDAO.createPropertyUniqueContext(keys[0], keys[1], keys[2], value);
        }
        else
        {
            propertyValueDAO.updatePropertyUniqueContext(keys[0], keys[1], keys[2], value);
        }
        
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Set attribute Value: \n" +
                    "   Keys:   " + Arrays.asList(keys) + "\n" +
                    "   Value: " + value);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void createAttribute(Serializable value, Serializable... keys)
    {
        keys = normalizeKeys(keys);
        try
        {
            propertyValueDAO.createPropertyUniqueContext(keys[0], keys[1], keys[2], value);
        }
        catch (PropertyUniqueConstraintViolation e)
        {
            throw new DuplicateAttributeException(keys[0], keys[1], keys[2], e); 
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Created attribute: \n" +
                    "   Keys:   " + Arrays.asList(keys) + "\n" +
                    "   Value: " + value);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void updateOrCreateAttribute(
            Serializable keyBefore1,
            Serializable keyBefore2,
            Serializable keyBefore3,
            Serializable keyAfter1,
            Serializable keyAfter2,
            Serializable keyAfter3)
    {
        Pair<Long, Long> pair = propertyValueDAO.getPropertyUniqueContext(keyBefore1, keyBefore2, keyBefore3);
        try
        {
            if (pair == null)
            {
                pair = propertyValueDAO.createPropertyUniqueContext(keyAfter1, keyAfter2, keyAfter3, null);
            }
            else
            {
                Long id = pair.getFirst();
                propertyValueDAO.updatePropertyUniqueContextKeys(id, keyAfter1, keyAfter2, keyAfter3);
            }
        }
        catch (PropertyUniqueConstraintViolation e)
        {
            throw new DuplicateAttributeException(keyAfter1, keyAfter2, keyAfter3, e); 
        }
        // Done
        if (logger.isDebugEnabled())
        {
            Serializable[] keysBefore = normalizeKeys(keyBefore1, keyBefore2, keyBefore3);
            Serializable[] keysAfter = normalizeKeys(keyAfter1, keyAfter2, keyAfter3);
            logger.debug(
                    "Updated attribute: \n" +
                    "   Before:   " + Arrays.asList(keysBefore) + "\n" +
                    "   After:    " + Arrays.asList(keysAfter));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeAttribute(Serializable ... keys)
    {
        keys = normalizeKeys(keys);
        int deleted = propertyValueDAO.deletePropertyUniqueContext(keys[0], keys[1], keys[2]);
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Deleted attribute: \n" +
                    "   Keys:  " + Arrays.asList(keys) + "\n" +
                    "   Count: " + deleted);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeAttributes(Serializable ... keys)
    {
        int deleted = propertyValueDAO.deletePropertyUniqueContext(keys);
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Deleted attributes: \n" +
                    "   Keys:  " + Arrays.asList(keys) + "\n" +
                    "   Count: " + deleted);
        }
    }
}
