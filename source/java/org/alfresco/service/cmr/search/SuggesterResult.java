
package org.alfresco.service.cmr.search;

import java.util.List;

import org.alfresco.util.Pair;

/**
 * Term suggestions response object
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public interface SuggesterResult
{

    /**
     * Get the number of suggestions
     * 
     * @return long
     */
    long getNumberFound();

    /**
     * Get the list of suggestions as ("term", "weight") pairs. Never <i>null</i>.
     * 
     * @return list of suggestions
     */
    List<Pair<String, Integer>> getSuggestions();
}
