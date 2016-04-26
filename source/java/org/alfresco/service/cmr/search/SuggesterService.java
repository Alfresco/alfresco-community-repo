
package org.alfresco.service.cmr.search;

/**
 * A service that returns term suggestions
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public interface SuggesterService
{

    /**
     * Whether the Suggester is enabled (refer to 'solr.suggester.enabled' repository property) or not
     * 
     * @return true if the Suggester is enabled, false otherwise
     */
    public boolean isEnabled();

    /**
     * Get suggestions for the specified term {@link SuggesterParameters#term}
     * 
     * @param suggesterParameters the parameters to use
     * @return term suggestions result. Never <i>null</i>
     */
    public SuggesterResult getSuggestions(SuggesterParameters suggesterParameters);
}
