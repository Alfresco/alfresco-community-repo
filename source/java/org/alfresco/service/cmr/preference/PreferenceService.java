package org.alfresco.service.cmr.preference;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;     
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.Auditable;
import org.alfresco.util.Pair;

/**
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public interface PreferenceService
{
    /**
     * Get all preferences for a particular user
     * 
     * @param  userName                     the user name
     * @return a map containing the preference values, empty if none
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
     * @return a map containing the preference values, empty if none
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
