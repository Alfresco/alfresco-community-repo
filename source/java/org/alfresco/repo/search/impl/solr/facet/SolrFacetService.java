/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

package org.alfresco.repo.search.impl.solr.facet;

import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Solr Facet service configuration API.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public interface SolrFacetService
{

    /**
     * Gets all the available facets.
     * 
     * @return List of {@code SolrFacetProperties} or an empty list if none exists
     */
    public List<SolrFacetProperties> getFacets();

    /**
     * Gets the facet by filter Id.
     * 
     * @param filterID the filter Id
     * @return {@code SolrFacetProperties} object or <i>null</i> if there is no facet with the specified Id
     */
    public SolrFacetProperties getFacet(String filterID);

    /**
     * Gets the facet's {@code NodeRef} by filter Id.
     * 
     * @param filterID the filter Id
     * @return facet's {@code NodeRef} or <i>null</i> if there is no facet with the specified Id
     */
    public NodeRef getFacetNodeRef(String filterID);

    /**
     * Indicates whether the specified user is a search-administrator or not.
     * <p>
     * Note: The super/repo admin is considered to be a search-administrator too.
     * 
     * @param userName The user name
     * @return true if the specified user is a search-administrator, false otherwise
     */
    public boolean isSearchAdmin(String userName);

    /**
     * Creates a new facet.
     * 
     * @param facetProperties the facet's properties
     * @return the created facet's {@code NodeRef}
     */
    public NodeRef createFacetNode(SolrFacetProperties facetProperties);

    /**
     * Updates the existing facet.
     * 
     * @param facetProperties the facet's properties
     */
    public void updateFacet(SolrFacetProperties facetProperties);

    /**
     * Deletes the specified facet permanently
     * 
     * @param filterID the filter Id
     */
    public void deleteFacet(String filterID);

    /**
     * Reorders existing facets to the provided order.
     * 
     * @param filterIds an ordered sequence of filter IDs.
     * @throws NullPointerException if filterIds is {@code null}.
     * @throws MissingFacetId if the list is empty.
     * @throws UnrecognisedFacetId if any of the provided filter IDs are not recognised.
     * @throws DuplicateFacetId if there is a duplicate filter ID in the list.
     */
    public void reorderFacets(List<String> filterIds);
}
