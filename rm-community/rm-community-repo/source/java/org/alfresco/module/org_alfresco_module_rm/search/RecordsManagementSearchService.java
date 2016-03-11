package org.alfresco.module.org_alfresco_module_rm.search;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * Records management search service.
 * 
 * @author Roy Wetherall
 */
public interface RecordsManagementSearchService 
{   
	/**
	 * Execute a records management search
	 * @param siteId           the id of the rm site to query
	 * @param query	           search query string
	 * @param searchParameters search parameters 
	 * @return {@link List}<{@link Pair}<{@link NodeRef}, {@link NodeRef}> search results as pairs for parent and child nodes 
	 */
	List<Pair<NodeRef, NodeRef>> search(String siteId, String query, RecordsManagementSearchParameters searchParameters);	
	
	/**
	 * Get all the searches saved on the given records management site.
	 * @param siteId   site id
	 * @return {@link List<{@link SavedSearchDetails}>}    list of saved search details 
	 */
	List<SavedSearchDetails> getSavedSearches(String siteId);
	
	/**
	 * Get a named saved search for a given records management site. 
	 * @param siteId   site id
	 * @param name     name of search
	 * @return {@link SavedSearchDetails}  saved search details
	 */
	SavedSearchDetails getSavedSearch(String siteId, String name);
	
	/**
     * Save records management search.
     * @param siteId        site id
     * @param name          name  
     * @param description   description 
     * @param search        search string    
     * @param isPublic      indicates whether the saved search is public or not
     * @return {@link SavedSearchDetails}   details of the saved search
     */
    SavedSearchDetails saveSearch(String siteId, String name, String description, String search, RecordsManagementSearchParameters searchParameters, boolean isPublic);
	
	/**
	 * Save records management search.
	 * @param savedSearchDetails   details of search to save
	 * @return {@link SavedSearchDetails}  details of the saved search
	 */
	SavedSearchDetails saveSearch(SavedSearchDetails savedSearchDetails);
		
	/**
	 * Delete saved search
	 * @param siteId   site id
	 * @param name     name of saved search
	 */
	void deleteSavedSearch(String siteId, String name);
	
	/**
	 * Delete saved search
	 * @param savedSearchDetails   saved search details
	 */
	void deleteSavedSearch(SavedSearchDetails savedSearchDetails);
	
    /**
     * Adds the reports as saved searches to a given site.
     * @param siteId    site id
     */
    void addReports(String siteId);
}
