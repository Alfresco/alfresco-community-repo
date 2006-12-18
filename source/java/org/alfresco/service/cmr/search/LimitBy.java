package org.alfresco.service.cmr.search;

/**
 * Enum to describe how the maximum size of the returned result set should be determined.
 * 
 * @author Andy Hind
 */
public enum LimitBy
{
    /**
     * The final number of search results is not important.
     */
    UNLIMITED,
    /**
     * Limit the total number of search results returned after pruning by permissions.
     */
    FINAL_SIZE,
    /**
     * Limit the number of results that will be passed through for permission checks.<br/> 
     * Used internally to prevent excessive permission checking
     * (see property <b>lucene.query.maxInitialSearchResults</b>).
     */
    NUMBER_OF_PERMISSION_EVALUATIONS;
}