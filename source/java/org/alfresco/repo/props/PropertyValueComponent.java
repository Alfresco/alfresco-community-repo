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

import org.springframework.dao.DataIntegrityViolationException;

/**
 * A component that gives high-level access to manipulate standalone properties.
 * <p>
 * There are two types of properties: shared and unshared.
 * <p/>
 * <u>Simple, Shared Properties</u>:<br/>
 * These properties are globally unique (apart from values that don't have a meaningful key).
 * If two different applications attempt to create the same value, then the same ID will
 * be returned (after conflict resolution), assuming that the value is not treated as binary
 * data.  It is not possible to modify or delete these values.  You should store types that
 * can be converted to and from a well-known type.  Complex collections should not be stored
 * using this value.<br/>
 * <u>Unshared Properties</u>:<br/>
 * These properties may be duplicated, modifed and deleted.  It is not possible to look
 * values up and therefore new IDs are generated for each creation.  Complex values can
 * be stored in these properties and will be exploded recursively.<br/>
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public interface PropertyValueComponent
{
    /**
     * @param id                the ID (may not be <tt>null</tt>)
     * @throws DataIntegrityViolationException if the ID is invalid
     */
    Serializable getSharedValueById(Long id);
    /**
     * @param value             the value to find the ID for (may be <tt>null</tt>)
     * @return                  Returns the ID of the shared value or <tt>null</tt> if it doesn't exist
     */
    Long getSharedValueId(Serializable value);
    /**
     * @param value             the value to find the ID for (may be <tt>null</tt>)
     * @return                  Returns the ID of the shared value (created or not)
     */
    Long getOrCreateSharedValue(Serializable value);
    
    /**
     * @param id                the ID (may not be <tt>null</tt>)
     * @return                  Returns the value of the property (never <tt>null</tt>)
     * @throws DataIntegrityViolationException if the ID is invalid
     */
    Serializable getUnsharedPropertyById(Long id);
    /**
     * @param value             the value to create (may be <tt>null</tt>)
     * @return                  Returns the new property's ID
     */
    Long createUnsharedProperty(Serializable value);
    /**
     * @param id                the ID of the root property to change (may not be <tt>null</tt>)
     * @param value             the new property value
     * @throws DataIntegrityViolationException if the ID is invalid
     */
    void updateUnsharedProperty(Long id, Serializable value);
    /**
     * @param id                the ID of the root property to delete (may be <tt>null</tt>)
     * @throws DataIntegrityViolationException if the ID is invalid
     */
    void deleteUnsharedProperty(Long id);
    
    /**
     * Create a new combination of three unique properties.
     * 
     * @param value1                the first property, which should denote application name or use-case ID
     *                              (<tt>null</tt> allowed)
     * @param value2                the second property, which should denote the context or container value
     *                              (<tt>null</tt> allowed)
     * @param value3                the third property, which should denote the unique value within the context
     *                              of the previous two properties (<tt>null</tt> allowed)
     * @return                      Returns the ID of the unique property combination for later updates
     * @throws PropertyUniqueConstraintViolation        if the combination is not unique
     */
    Long createPropertyUniqueContext(Serializable value1, Serializable value2, Serializable value3);
    /**
     * Get the ID of a unique property context.
     * 
     * @see #createPropertyUniqueContext(Serializable, Serializable, Serializable)
     */
    Long getPropertyUniqueContext(Serializable value1, Serializable value2, Serializable value3);
    /**
     * Update a unique property context.
     * 
     * @see #createPropertyUniqueContext(Serializable, Serializable, Serializable)
     */
    void updatePropertyUniqueContext(Long id, Serializable value1, Serializable value2, Serializable value3);
    /**
     * Update a combination of three unique properties.  If the <i>before</i> values exist, then they
     * are updated to the new values.  If the <i>before</i> values don't exist, then the new values
     * are created assuming no pre-existence -
     * using {@link #createPropertyUniqueContext(Serializable, Serializable, Serializable) create} is better
     * if there is no pre-existing set of values. 
     * 
     * @param value1Before          the first property before (<tt>null</tt> allowed)
     * @param value2Before          the second property before (<tt>null</tt> allowed)
     * @param value3Before          the third property before (<tt>null</tt> allowed)
     * @param value1                the first property (<tt>null</tt> allowed)
     * @param value2                the second property (<tt>null</tt> allowed)
     * @param value3                the third property (<tt>null</tt> allowed)
     * @return                      Returns the ID of the unique property combination for later updates
     * @throws PropertyUniqueConstraintViolation        if the new combination is not unique
     */
    Long updatePropertyUniqueContext(
            Serializable value1Before, Serializable value2Before, Serializable value3Before,
            Serializable value1, Serializable value2, Serializable value3);
    /**
     * Delete a unique property context.
     * 
     * @see #createPropertyUniqueContext(Serializable, Serializable, Serializable)
     */
    void deletePropertyUniqueContext(Long id);
    /**
     * Delete a combination of three unique properties.  It doesn't matter if the unique combination
     * already existed or not.
     * 
     * @param values                an array of one, two or three values, any of which may be <tt>null</tt>
     * @return                      Returns the number of unique combinations deleted
     */
    int deletePropertyUniqueContexts(Serializable ... values);
}
