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
package org.alfresco.service.cmr.attributes;

import java.io.Serializable;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;

/**
 * This provides services for reading, writing, and querying global attributes.
 * <p/>
 * Attributes are uniquely identified by up to 3 keys; <tt>null</tt> keys are themselves treated uniquely i.e.
 * <code>['a','b']</code> is equivalent to <code>['a','b',null]</code> in all cases except where multiple search results
 * are possible. Keys can be any simple <code>Serializable</code> type, typically being convertable using
 * {@link DefaultTypeConverter}. The attribute values persisted can be any <code>Serializable</code>
 * (including collections) but the raw values should be convertable by the {@link DefaultTypeConverter} for
 * the most efficient persistence.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
@AlfrescoPublicApi
public interface AttributeService
{
    /**
     * Determine if a particular attribute exists.
     * 
     * @param keys                  List of 1 to 3 keys to uniquely identify the attribute
     * @return                      <tt>true</tt> if the attribute exists (regardless of its value)
     *                              or <tt>false</tt> if it doesn't exist
     */
    public boolean exists(Serializable ... keys);

    /**
     * Get an attribute using a list of unique keys
     *
     * @param keys                  List of 1 to 3 keys to uniquely identify the attribute
     * @return                      The attribute value or <tt>null</tt>
     */
    public Serializable getAttribute(Serializable ... keys);
    
    /**
     * Callback used for querying for lists of attributes.
     * 
     * @author Derek Hulley
     * @since 3.4
     */
    public interface AttributeQueryCallback
    {
        /**
         * Handle an attribute value
         * 
         * @param id                the unique attribute ID
         * @param value             the value associated with the attribute
         * @param keys              the unique attribute keys
         * @return                  <tt>true</tt> to continue sending results if any are available
         */
        boolean handleAttribute(Long id, Serializable value, Serializable[] keys);
    }
    
    /**
     * Get all attributes that share the starter keys provided.  If 3 key values are given,
     * there can be, at most, one result.
     * 
     * @param callback              the callback that handles the results
     * @param keys                  0 to 3 key values to search against
     */
    public void getAttributes(AttributeQueryCallback callback, Serializable ... keys);
    
    /**
     * Set an attribute, overwriting its prior value if it already existed.  <tt>null</tt>
     * values are treated as unique i.e. if the value set is <tt>null</tt> then
     * {@link #exists(String...)} will still return <tt>true</tt>.  If the attribute doesn't
     * exist, it will be created otherwise it will be modified.
     *
     * @param value                 The value to store (can be a collection or <tt>null</tt>)
     * @param keys                  List of 1 to 3 keys to uniquely identify the attribute
     */
    public void setAttribute(Serializable value, Serializable ... keys);

    /**
     * Create an attribute with an optional value, assuming there is no existing attribute
     * using the same keys.
     * 
     * @param value                 The value to store (can be a collection or <tt>null</tt>)
     * @param keys                  List of 1 to 3 keys to uniquely identify the attribute
     * 
     * @throws DuplicateAttributeException if the attribute already exists
     */
    public void createAttribute(Serializable value, Serializable ... keys);
    
    /**
     * Update an attribute key whilst preserving the associated value (if any).  If there is
     * no existing key matching the original value, then nothing will happen.
     * 
     * @param keyBefore1            the first part of the original unique key (never <tt>null</tt>)
     * @param keyBefore2            the second part of the original unique key (<tt>null</tt> allowed)
     * @param keyBefore3            the third part of the original unique key (<tt>null</tt> allowed)
     * @param keyAfter1             the first part of the new unique key (never <tt>null</tt>)
     * @param keyAfter2             the second part of the new unique key (<tt>null</tt> allowed)
     * @param keyAfter3             the third part of the new unique key (<tt>null</tt> allowed)
     */
    public void updateOrCreateAttribute(
            Serializable keyBefore1, Serializable keyBefore2, Serializable keyBefore3,
            Serializable keyAfter1, Serializable keyAfter2, Serializable keyAfter3);
    
    /**
     * Remove a specific attribute.
     * 
     * @param keys                  up to 3 keys to uniquely identify the attribute
     */
    public void removeAttribute(Serializable ... keys);
    
    /**
     * Remove all attributes that share a set of keys (in order)
     * 
     * @param keys                  up to 3 keys to identify attributes to remove
     */
    public void removeAttributes(Serializable ... keys);
}
