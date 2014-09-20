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
import java.util.Set;

import org.alfresco.repo.dictionary.Facetable;
import org.alfresco.repo.search.impl.solr.facet.Exceptions.DuplicateFacetId;
import org.alfresco.repo.search.impl.solr.facet.Exceptions.MissingFacetId;
import org.alfresco.repo.search.impl.solr.facet.Exceptions.UnrecognisedFacetId;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

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
    
    /**
     * This method offers a convenient access point for getting all Facetable
     * content properties defined in the repository.
     * @return a collection of facetable {@link FacetablePropertyData}s.
     * @see Facetable
     */
    public Set<FacetablePropertyData> getFacetableProperties();
    
    /**
     * This method offers a convenient access point for getting all Facetable
     * content properties defined on the specified content class (type or aspect).
     * @param contentClass the QName of an aspect or type, whose facetable properties are sought.
     * @return a collection of facetable {@link FacetablePropertyData}s.
     * @see Facetable
     */
    public Set<FacetablePropertyData> getFacetableProperties(QName contentClass);
    
    /** A simple POJO/DTO intended primarily for use in an FTL model and rendering in the JSON API. */
    public static class FacetablePropertyData
    {
        private final PropertyDefinition propDef;
        private final String             localisedTitle;
        private final String             displayName;
        
        public FacetablePropertyData(PropertyDefinition propDef, String localisedTitle)
        {
            this.propDef        = propDef;
            this.localisedTitle = localisedTitle;
            this.displayName    = propDef.getName().getPrefixString() +
                                  (localisedTitle == null ? "" : " (" + localisedTitle + ")");
        }
        
        public PropertyDefinition getPropertyDefinition() { return this.propDef; }
        public String             getLocalisedTitle()     { return this.localisedTitle; }
        public String             getDisplayName()        { return this.displayName; }
        
        @Override public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
            result = prime * result + ((localisedTitle == null) ? 0 : localisedTitle.hashCode());
            result = prime * result + ((propDef == null) ? 0 : propDef.hashCode());
            return result;
        }
        
        @Override public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            FacetablePropertyData other = (FacetablePropertyData) obj;
            if (displayName == null)
            {
                if (other.displayName != null)
                    return false;
            } else if (!displayName.equals(other.displayName))
                return false;
            if (localisedTitle == null)
            {
                if (other.localisedTitle != null)
                    return false;
            } else if (!localisedTitle.equals(other.localisedTitle))
                return false;
            if (propDef == null)
            {
                if (other.propDef != null)
                    return false;
            } else if (!propDef.equals(other.propDef))
                return false;
            return true;
        }
    }
    
}
