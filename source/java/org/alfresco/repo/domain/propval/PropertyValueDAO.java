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
package org.alfresco.repo.domain.propval;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.alfresco.repo.domain.CrcHelper;
import org.alfresco.util.Pair;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * DAO services for <b>alf_prop_XXX</b> tables.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public interface PropertyValueDAO
{
    //================================
    // 'alf_prop_class' accessors
    //================================
    /**
     * <b>FOR INTERNAL USE ONLY</b>: Do not use directly; see interface comments.
     * <p/>
     * <b>alf_prop_class</b> accessor
     * 
     * @param id                the ID (may not be <tt>null</tt>)
     */
    Pair<Long, Class<?>> getPropertyClassById(Long id);
    /**
     * <b>FOR INTERNAL USE ONLY</b>: Do not use directly; see interface comments.
     * <p/>
     * <b>alf_prop_class</b> accessor
     * 
     * @param value             the value to find the ID for (may not be <tt>null</tt>)
     */
    Pair<Long, Class<?>> getPropertyClass(Class<?> value);
    /**
     * <b>FOR INTERNAL USE ONLY</b>: Do not use directly; see interface comments.
     * <p/>
     * <b>alf_prop_class</b> accessor
     * 
     * @param value             the value to find the ID for (may not be <tt>null</tt>)
     */
    Pair<Long, Class<?>> getOrCreatePropertyClass(Class<?> value);

    //================================
    // 'alf_prop_date_value' accessors
    //================================
    /**
     * <b>FOR INTERNAL USE ONLY</b>: Do not use directly; see interface comments.
     * <p/>
     * <b>alf_prop_date_value</b> accessor
     * 
     * @param id                the ID (may not be <tt>null</tt>)
     */
    Pair<Long, Date> getPropertyDateValueById(Long id);
    /**
     * <b>FOR INTERNAL USE ONLY</b>: Do not use directly; see interface comments.
     * <p/>
     * <b>alf_prop_date_value</b> accessor
     * 
     * @param value             the value to find the ID for (may not be <tt>null</tt>)
     */
    Pair<Long, Date> getPropertyDateValue(Date value);
    /**
     * <b>FOR INTERNAL USE ONLY</b>: Do not use directly; see interface comments.
     * <p/>
     * <b>alf_prop_date_value</b> accessor
     * 
     * @param value             the value to find the ID for (may not be <tt>null</tt>)
     */
    Pair<Long, Date> getOrCreatePropertyDateValue(Date value);
    
    //================================
    // 'alf_prop_string_value' accessors
    //================================
    /**
     * Utility method to get query parameters for case-sensitive string searching
     * @see CrcHelper
     */
    Pair<String, Long> getPropertyStringCaseSensitiveSearchParameters(String value);
    /**
     * <b>FOR INTERNAL USE ONLY</b>: Do not use directly; see interface comments.
     * <p/>
     * <b>alf_prop_string_value</b> accessor
     * 
     * @param id                the ID (may not be <tt>null</tt>)
     */
    Pair<Long, String> getPropertyStringValueById(Long id);
    /**
     * <b>FOR INTERNAL USE ONLY</b>: Do not use directly; see interface comments.
     * <p/>
     * <b>alf_prop_string_value</b> accessor
     * 
     * @param value             the value to find the ID for (may not be <tt>null</tt>)
     */
    Pair<Long, String> getPropertyStringValue(String value);
    /**
     * <b>FOR INTERNAL USE ONLY</b>: Do not use directly; see interface comments.
     * <p/>
     * <b>alf_prop_string_value</b> accessor
     * 
     * @param value             the value to find the ID for (may not be <tt>null</tt>)
     */
    Pair<Long, String> getOrCreatePropertyStringValue(String value);

    //================================
    // 'alf_prop_double_value' accessors
    //================================
    /**
     * <b>FOR INTERNAL USE ONLY</b>: Do not use directly; see interface comments.
     * <p/>
     * <b>alf_prop_double_value</b> accessor
     * 
     * @param id                the ID (may not be <tt>null</tt>)
     */
    Pair<Long, Double> getPropertyDoubleValueById(Long id);
    /**
     * <b>FOR INTERNAL USE ONLY</b>: Do not use directly; see interface comments.
     * <p/>
     * <b>alf_prop_double_value</b> accessor
     * 
     * @param value             the value to find the ID for (may not be <tt>null</tt>)
     */
    Pair<Long, Double> getPropertyDoubleValue(Double value);
    /**
     * <b>FOR INTERNAL USE ONLY</b>: Do not use directly; see interface comments.
     * <p/>
     * <b>alf_prop_double_value</b> accessor
     * 
     * @param value             the value to find the ID for (may not be <tt>null</tt>)
     */
    Pair<Long, Double> getOrCreatePropertyDoubleValue(Double value);
    
    //================================
    // 'alf_prop_serializable_value' accessors
    //================================
    /**
     * <b>FOR INTERNAL USE ONLY</b>: Do not use directly; see interface comments.
     * <p/>
     * <b>alf_prop_serializable_value</b> accessor
     * 
     * @param id                the ID (may not be <tt>null</tt>)
     */
    Pair<Long, Serializable> getPropertySerializableValueById(Long id);
    /**
     * <b>FOR INTERNAL USE ONLY</b>: Do not use directly; see interface comments.
     * <p/>
     * <b>alf_prop_serializable_value</b> accessor
     * 
     * @param value             the value to find the ID for (may not be <tt>null</tt>)
     */
    Pair<Long, Serializable> createPropertySerializableValue(Serializable value);
    
    //================================
    // 'alf_prop_value' accessors
    //================================
    /**
     * Use for accessing unique properties; see interface comments.
     * <p/>
     * <b>alf_prop_value</b> accessor: get a property based on the database ID
     * 
     * @param id                the ID (may not be <tt>null</tt>)
     */
    Pair<Long, Serializable> getPropertyValueById(Long id);
    /**
     * Use for accessing unique properties; see interface comments.
     * <p/>
     * <b>alf_prop_value</b> accessor: find a property based on the value
     * 
     * @param value             the value to find the ID for (may be <tt>null</tt>)
     */
    Pair<Long, Serializable> getPropertyValue(Serializable value);
    /**
     * Use for accessing unique properties; see interface comments.
     * <p/>
     * <b>alf_prop_value</b> accessor: find or create a property based on the value.
     * <b>Note:</b> This method will not recurse into maps or collections.  Use the
     * dedicated methods if you want recursion; otherwise maps and collections will
     * be serialized and probably stored as BLOB values.
     * <p/>
     * All collections and maps will be opened up to any depth.  To limit this behaviour,
     * use {@link #getOrCreatePropertyValue(Serializable, int)}.
     * 
     * @param value             the value to find the ID for (may be <tt>null</tt>)
     */
    Pair<Long, Serializable> getOrCreatePropertyValue(Serializable value);
    
    //================================
    // 'alf_prop_root' accessors
    //================================
    
    /**
     * A callback for handling return properties
     */
    public interface PropertyFinderCallback
    {
        public void handleProperty(Long id, Serializable value);
    }

    /**
     * Use for accessing non-unique, exploded properties; see interface comments.
     * <p/>
     * <b>alf_prop_root</b> accessor: get a property based on the database ID
     * 
     * @param id                the ID (may not be <tt>null</tt>)
     * @return                  Returns the value of the property (never <tt>null</tt>)
     * @throws DataIntegrityViolationException if the ID is invalid
     */
    Serializable getPropertyById(Long id);
    /**
     * Use for accessing non-unique, exploded properties; see interface comments.
     * <p/>
     * <b>alf_prop_root</b> accessor: get all properties based on the database IDs
     * 
     * @param ids               the IDs (may not be <tt>null</tt>; may be empty)
     * @param callback          the callback to handle the results
     * @throws DataIntegrityViolationException if any of the the IDs are invalid
     */
    void getPropertiesByIds(List<Long> ids, PropertyFinderCallback callback);
    /**
     * Use for accessing non-unique, exploded properties; see interface comments.
     * <p/>
     * <b>alf_prop_root</b> accessor: find or create a property based on the value.
     * <p/>
     * All collections and maps will be opened up to any depth.
     * 
     * @param value             the value to create (may be <tt>null</tt>)
     * @return                  Returns the new property's ID
     */
    Long createProperty(Serializable value);
    
    /**
     * Use for accessing non-unique, exploded properties; see interface comments.
     * <p/>
     * <b>alf_prop_root</b> accessor: update the property root to contain a new value.
     * 
     * @param id                the ID of the root property to change
     * @param value             the new property value
     */
    void updateProperty(Long id, Serializable value);
    
    /**
     * Use for accessing non-unique, exploded properties; see interface comments.
     * <p/>
     * <b>alf_prop_root</b> accessor: delete a property root completely
     * 
     * @param id                the ID of the root property to delete
     */
    void deleteProperty(Long id);
    
    //================================
    // 'alf_prop_unique_ctx' accessors
    //================================
    /**
     * <b>alf_prop_unique_ctx</b> accessor: create a unique context with an optional
     * associated value.
     * <p/>
     * The DAO ensures that the region-context-value combination will be globally unique.
     * 
     * @param value1            a simple key value (not a collection) (may be <tt>null</tt>)
     * @param value2            a simple key value (not a collection) (may be <tt>null</tt>)
     * @param value3            a simple key value (not a collection) (may be <tt>null</tt>)
     * @param propertyValue1    a value to store against the key (may be <tt>null</tt>)
     * @return                  Returns the ID-valueId pair of the context
     * 
     * @throws PropertyUniqueConstraintViolation        if the combination is not unique
     */
    Pair<Long, Long> createPropertyUniqueContext(
            Serializable value1, Serializable value2, Serializable value3,
            Serializable propertyValue1);
    /**
     * Get the unique context ID and associated shared property ID, or <tt>null</tt> if no
     * such context exists.  The associated property may be <tt>null</tt> even if the unique
     * context exists.
     *
     * @param values            a combination of one to three values in order
     * @return                  Returns the ID-valueId pair or <tt>null</tt> if the context
     *                          doesn't exist.
     * 
     * @see #createPropertyUniqueContext(Serializable, Serializable, Serializable, Serializable)
     */
    Pair<Long, Long> getPropertyUniqueContext(Serializable value1, Serializable value2, Serializable value3);
    
    /**
     * A callback for handling return property unique contexts
     */
    public interface PropertyUniqueContextCallback
    {
        public void handle(Long id, Long propId, Serializable[] keys);
    }
    
    /**
     * Get unique contexts (unique context ID and associated shared property ID), if any, based on one, two or three context values.
     * The associated property may be <tt>null</tt> even if the unique context exists.
     *
     * @param values            a combination of one to three values in order
     * 
     * @see #createPropertyUniqueContext(Serializable, Serializable, Serializable, Serializable)
     */
    void getPropertyUniqueContext(PropertyUniqueContextCallback callback, Serializable ... values);
    /**
     * Update the unique context, preserving any associated property.
     * 
     * @throws PropertyUniqueConstraintViolation        if the combination is not unique
     * 
     * @see #createPropertyUniqueContext(Serializable, Serializable, Serializable, Serializable)
     */
    void updatePropertyUniqueContextKeys(
            Long id,
            Serializable value1, Serializable value2, Serializable value3);
    /**
     * Update the property associated with a unique context (based on one, two or three context values).
     * 
     * @see #createPropertyUniqueContext(Serializable, Serializable, Serializable, Serializable)
     */
    void updatePropertyUniqueContext(Serializable value1, Serializable value2, Serializable value3, Serializable propertyValue);
    /**
     * @see #createPropertyUniqueContext(Serializable, Serializable, Serializable, Serializable)
     */
    void deletePropertyUniqueContext(Long id);
    /**
     * Delete sets of unique contexts based on one, two or three context values.
     * 
     * @param values            a combination of one to three values in order
     * @return                  Returns the number of unique contexts deleted
     */
    int deletePropertyUniqueContext(Serializable ... values);
    
    //================================
    // Utility methods
    //================================
    /**
     * Utility method to convert property query results into the original value.  Note
     * that the rows must all share the same root property ID.
     * <p/>
     * If the rows passed in don't constitute a valid, full property - they don't contain all
     * the link entities for the property - then the result may be <tt>null</tt>.
     * 
     * @param rows              the search results for a single root property
     * @return                  Returns the root property as originally persisted, or <tt>null</tt>
     *                          if the rows don't represent a complete property
     * @throws IllegalArgumentException     if rows don't all share the same root property ID
     */
    Serializable convertPropertyIdSearchRows(List<PropertyIdSearchRow> rows);
}
