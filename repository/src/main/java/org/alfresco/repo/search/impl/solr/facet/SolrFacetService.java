/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.search.impl.solr.facet;

import java.util.List;

import org.alfresco.repo.dictionary.Facetable;
import org.alfresco.repo.search.impl.solr.facet.Exceptions.DuplicateFacetId;
import org.alfresco.repo.search.impl.solr.facet.Exceptions.MissingFacetId;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Solr Facet service configuration API.
 * 
 * @author Jamal Kaabi-Mofrad
 * @author Neil Mc Erlean
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
     * @param filterID
     *            the filter Id
     * @return {@code SolrFacetProperties} object or <i>null</i> if there is no facet with the specified Id
     */
    public SolrFacetProperties getFacet(String filterID);

    /**
     * Gets the facet's {@code NodeRef} by filter Id.
     * 
     * @param filterID
     *            the filter Id
     * @return facet's {@code NodeRef} or <i>null</i> if there is no facet with the specified Id
     */
    public NodeRef getFacetNodeRef(String filterID);

    /**
     * Indicates whether the specified user is a search-administrator or not.
     * <p>
     * Note: The super/repo admin is considered to be a search-administrator too.
     * 
     * @param userName
     *            The user name
     * @return true if the specified user is a search-administrator, false otherwise
     */
    public boolean isSearchAdmin(String userName);

    /**
     * Creates a new facet.
     * 
     * @param facetProperties
     *            the facet's properties
     * @return the created facet's {@code NodeRef}
     */
    public NodeRef createFacetNode(SolrFacetProperties facetProperties);

    /**
     * Updates the existing facet.
     * 
     * @param facetProperties
     *            the facet's properties
     */
    public void updateFacet(SolrFacetProperties facetProperties);

    /**
     * Deletes the specified facet permanently
     * 
     * @param filterID
     *            the filter Id
     */
    public void deleteFacet(String filterID);

    /**
     * Reorders existing facets to the provided order.
     * 
     * @param filterIds
     *            an ordered sequence of filter IDs.
     * @throws NullPointerException
     *             if filterIds is {@code null}.
     * @throws MissingFacetId
     *             if the list is empty.
     * @throws DuplicateFacetId
     *             if there is a duplicate filter ID in the list.
     */
    public void reorderFacets(List<String> filterIds);

    /**
     * This method offers a convenient access point for getting all Facetable content properties defined in the repository.
     * 
     * @return a collection of facetable {@link PropertyDefinition}s.
     * @see Facetable
     */
    public List<PropertyDefinition> getFacetableProperties();

    /**
     * This method offers a convenient access point for getting all Facetable content properties defined on the specified content class (type or aspect) or any of its inherited properties.
     * 
     * @param contentClass
     *            the QName of an aspect or type, whose facetable properties are sought.
     * @return a collection of facetable {@link PropertyDefinition}s.
     * @see Facetable
     */
    public List<PropertyDefinition> getFacetableProperties(QName contentClass);

    /**
     * This method gets all synthetic, facetable properties across all content models in the repository.
     */
    public List<SyntheticPropertyDefinition> getFacetableSyntheticProperties();

    /**
     * This method gets all synthetic, facetable properties defined on the specified content class (type or aspect) or any of its inherited properties.
     * 
     * @param contentClass
     *            the QName of an aspect or type, whose synthetic, facetable properties are sought.
     */
    public List<SyntheticPropertyDefinition> getFacetableSyntheticProperties(QName contentClass);

    /**
     * This class represents a special case of a property, examples being file size and MIME type, which are not modelled as Alfresco content model properties, but are instead stored as components within properties of type {@code cm:content}.
     */
    public class SyntheticPropertyDefinition
    {
        public final PropertyDefinition containingPropertyDef;
        public final String syntheticPropertyName;
        public final QName dataTypeDefinition;

        public SyntheticPropertyDefinition(PropertyDefinition containingPropertyDef, String syntheticPropertyName,
                QName syntheticDataTypeDefinition)
        {
            this.containingPropertyDef = containingPropertyDef;
            this.syntheticPropertyName = syntheticPropertyName;
            this.dataTypeDefinition = syntheticDataTypeDefinition;
        }

        @Override
        public String toString()
        {
            return SyntheticPropertyDefinition.class.getSimpleName() +
                    "[" + this.syntheticPropertyName + "]";
        }
    }
}
