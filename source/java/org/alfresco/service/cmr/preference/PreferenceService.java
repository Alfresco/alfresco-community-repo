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
package org.alfresco.service.cmr.preference;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.Auditable;
import org.alfresco.util.Pair;

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
    @Auditable(parameters = {"userName"})
    Map<String, Serializable> getPreferences(String userName);

    @Auditable(parameters = {"userName", "preferenceName"})
    Serializable getPreference(String userName, String preferenceName);
    
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
    @Auditable(parameters = {"userName", "preferenceFilter"})
    Map<String, Serializable> getPreferences(String userName, String preferenceFilter);
    
    @Auditable(parameters = {"userName", "preferenceFilter"})
    PagingResults<Pair<String, Serializable>> getPagedPreferences(String userName, String preferenceFilter, PagingRequest pagingRequest);
    
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
    @Auditable(parameters = {"userName", "preferences"})
    void setPreferences(String userName, Map<String, Serializable> preferences);
    
    /**
     * Clears all the preferences for a particular user.
     * 
     * @param userName      the user name
     */
    @Auditable(parameters = {"userName"})
    void clearPreferences(String userName);

    /**
     * Clears the preferences for a particular user that match the filter optionally provided.
     * <p> 
     * If no filter if present then all preferences are cleared.
     * 
     * @param userName          the user name
     * @param preferenceFilter  the preference filter
     */
    @Auditable(parameters = {"userName", "preferenceFilter"})
    void clearPreferences(String userName, String preferenceFilter);
}
