/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.search;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

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
	 * @return {@link List}<{@link NodeRef}> search results 
	 */
	List<NodeRef> search(String siteId, String query, RecordsManagementSearchParameters searchParameters);	
	
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
