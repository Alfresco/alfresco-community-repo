/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.service.cmr.preference;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.Auditable;

/**
 * @author Roy Wetherall
 */
public interface PreferenceService
{
    /**
     * Get all preferences for a particular user
     * 
     * @param  userName                     the user name
     * @return Map<String, Serializable>    a map containing the preference values, empty if none
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"userName"})
    Map<String, Serializable> getPreferences(String userName);

    /**
     * Get the preferences for a particular user.
     * <p>
     * If no filter if provided all preferences are returned.
     * <p>
     * If a filter is provided it's used to filter the results.  For example the filter
     * "alfresco.myComp" will only return filters that are in the "namespace" alfresco.myComp.
     * 
     * @param userName                      the user name
     * @param preferenceFilter              the preference filter
     * @return Map<String, Serializable>    a map containing the preference values, empty if none
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"userName", "preferenceFilter"})
    Map<String, Serializable> getPreferences(String userName, String preferenceFilter);
    
    /**
     * Sets the preference values for a user.
     * <p>
     * Values provided overlay those already present.
     * <p>
     * Preference value names can be "namespaced" by using package notation.  For example 
     * "alfresc.myComp.myValue".
     *
     * @param userName      the user name
     * @param preferences   the preference values
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"userName", "preferences"})
    void setPreferences(String userName, Map<String, Serializable> preferences);
    
    /**
     * Clears all the preferences for a particular user.
     * 
     * @param userName      the user name
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"userName"})
    void clearPreferences(String userName);

    /**
     * Clears the preferences for a particular user that match the filter optionally provided.
     * <p> 
     * If no filter if present then all preferences are cleared.
     * 
     * @param userName          the user name
     * @param preferenceFilter  the preference filter
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"userName", "preferenceFilter"})
    void clearPreferences(String userName, String preferenceFilter);

}
