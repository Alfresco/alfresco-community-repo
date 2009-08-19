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
package org.alfresco.repo.domain.propval;

import java.io.Serializable;
import java.util.Date;

import org.alfresco.util.Pair;

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
     * <b>alf_prop_class</b> accessor
     * 
     * @param id                the ID (may not be <tt>null</tt>)
     */
    Pair<Long, Class<?>> getPropertyClassById(Long id);
    /**
     * <b>alf_prop_class</b> accessor
     * 
     * @param value             the value to find the ID for (may not be <tt>null</tt>)
     */
    Pair<Long, Class<?>> getPropertyClass(Class<?> value);
    /**
     * <b>alf_prop_class</b> accessor
     * 
     * @param value             the value to find the ID for (may not be <tt>null</tt>)
     */
    Pair<Long, Class<?>> getOrCreatePropertyClass(Class<?> value);

    //================================
    // 'alf_prop_date_value' accessors
    //================================
    /**
     * <b>alf_prop_date_value</b> accessor
     * 
     * @param id                the ID (may not be <tt>null</tt>)
     */
    Pair<Long, Date> getPropertyDateValueById(Long id);
    /**
     * <b>alf_prop_date_value</b> accessor
     * 
     * @param value             the value to find the ID for (may not be <tt>null</tt>)
     */
    Pair<Long, Date> getPropertyDateValue(Date value);
    /**
     * <b>alf_prop_date_value</b> accessor
     * 
     * @param value             the value to find the ID for (may not be <tt>null</tt>)
     */
    Pair<Long, Date> getOrCreatePropertyDateValue(Date value);
    
    //================================
    // 'alf_prop_string_value' accessors
    //================================
    /**
     * <b>alf_prop_string_value</b> accessor
     * 
     * @param id                the ID (may not be <tt>null</tt>)
     */
    Pair<Long, String> getPropertyStringValueById(Long id);
    /**
     * <b>alf_prop_string_value</b> accessor
     * 
     * @param value             the value to find the ID for (may not be <tt>null</tt>)
     */
    Pair<Long, String> getPropertyStringValue(String value);
    /**
     * <b>alf_prop_string_value</b> accessor
     * 
     * @param value             the value to find the ID for (may not be <tt>null</tt>)
     */
    Pair<Long, String> getOrCreatePropertyStringValue(String value);

    //================================
    // 'alf_prop_double_value' accessors
    //================================
    /**
     * <b>alf_prop_double_value</b> accessor
     * 
     * @param id                the ID (may not be <tt>null</tt>)
     */
    Pair<Long, Double> getPropertyDoubleValueById(Long id);
    /**
     * <b>alf_prop_double_value</b> accessor
     * 
     * @param value             the value to find the ID for (may not be <tt>null</tt>)
     */
    Pair<Long, Double> getPropertyDoubleValue(Double value);
    /**
     * <b>alf_prop_double_value</b> accessor
     * 
     * @param value             the value to find the ID for (may not be <tt>null</tt>)
     */
    Pair<Long, Double> getOrCreatePropertyDoubleValue(Double value);
    
    //================================
    // 'alf_prop_value' accessors
    //================================
    /**
     * <b>alf_prop_value</b> accessor: get a property based on the database ID
     * 
     * @param id                the ID (may not be <tt>null</tt>)
     */
    Pair<Long, Serializable> getPropertyValueById(Long id);
    /**
     * <b>alf_prop_value</b> accessor: find a property based on the value
     * 
     * @param value             the value to find the ID for (may be <tt>null</tt>)
     */
    Pair<Long, Serializable> getPropertyValue(Serializable value);
    /**
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
    /**
     * <b>alf_prop_value</b> accessor: find or create a property based on the value.
     * <b>Note:</b> This method will not recurse into maps or collections.  Use the
     * dedicated methods if you want recursion; otherwise maps and collections will
     * be serialized and probably stored as BLOB values.
     * <p>
     * <u>Max depth examples</u> (there is no upper limit):
     * <ul>
     *   <li>0: don't expand the value if it's a map or collection</li>
     *   <li>1: open the value up if it is a map or collection but don't do any more</li>
     *   <li>...</li>
     *   <li>10: open up 10 levels of maps or collections</li>
     * </ul>
     * All collections that are not opened up will be serialized unless there is a
     * custom {@link PropertyTypeConverter converter} which can serialize it in an
     * alternative format.
     * 
     * @param value             the value to find the ID for (may be <tt>null</tt>)
     * @param maxDepth          the maximum depth of collections and maps to iterate into
     */
    Pair<Long, Serializable> getOrCreatePropertyValue(Serializable value, int maxDepth);
}
