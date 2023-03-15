/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.service.cmr.search;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.query.EmptyPagingResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.Auditable;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * Category Service
 *
 * The service for querying and creating categories.
 * All other management can be carried out using the node service.
 * 
 * Classification - the groupings of categories. There is a one-to-one mapping with aspects. For example, Region. 
 * Root Category - the top level categories in a classification. For example, Northern Europe
 * Category - any other category below a root category
 * 
 * @author Andy Hind
 *
 */
@AlfrescoPublicApi
public interface CategoryService
{
    /**
     * Enumeration for navigation control.
     * 
     * MEMBERS - get only category members (the things that have been classified in a category, not the sub categories)
     * SUB_CATEGORIES - get sub categories only, not the things that hyave been classified.
     * ALL - get both of the above
     */
    public enum Mode {MEMBERS, SUB_CATEGORIES, ALL};
    
    /**
     * Depth from which to get nodes.
     * 
     * IMMEDIATE - only immediate sub categories or members
     * ANY - find subcategories or members at any level 
     */
    public enum Depth {IMMEDIATE, ANY};

    /**
     * Get the children of a given category node
     * 
     * @param categoryRef - the category node
     * @param mode - the enumeration mode for what to recover
     * @param depth - the enumeration depth for what level to recover
     * @return a collection of all the nodes found identified by their ChildAssocRef's
     */
    @Auditable(parameters = {"categoryRef", "mode", "depth"})
    public Collection<ChildAssociationRef> getChildren(NodeRef categoryRef, Mode mode, Depth depth );

    /**
     * Get a list of all the categories appropriate for a given property.
     * The full list of categories that may be assigned for this aspect.
     * 
     * @param storeRef StoreRef
     * @param aspectQName QName
     * @param depth - the enumeration depth for what level to recover
     * @return a collection of all the nodes found identified by their ChildAssocRef's
     */
    @Auditable(parameters = {"storeRef", "aspectQName", "depth"})
    public Collection<ChildAssociationRef> getCategories(StoreRef storeRef, QName aspectQName, Depth depth );

    /**
     * Get all the classification entries
     * 
     */
    @Auditable(parameters = {"storeRef"})
    public Collection<ChildAssociationRef> getClassifications(StoreRef storeRef);

    /**
     * Get the root categories for an aspect/classification
     * 
     * @param storeRef StoreRef
     * @param aspectName QName
     */
    @Auditable(parameters = {"storeRef", "aspectName"})
    public Collection<ChildAssociationRef> getRootCategories(StoreRef storeRef, QName aspectName);

    /**
     * Get a paged list of the root categories for an aspect/classification
     * 
     * @param storeRef StoreRef
     * @param aspectName QName
     * @param pagingRequest PagingRequest
     * @param sortByName boolean
     */
    @Auditable(parameters = {"storeRef", "aspectName", "pagingRequest", "sortByName"})
    PagingResults<ChildAssociationRef> getRootCategories(StoreRef storeRef, QName aspectName, PagingRequest pagingRequest, boolean sortByName);
    
    /**
     * Get a paged list of the root categories for an aspect/classification
     * 
     * @param storeRef
     * @param aspectName
     * @param pagingRequest
     * @param sortByName
     * @param filter
     * @return
     */
    @Auditable(parameters = {"storeRef", "aspectName", "pagingRequest", "sortByName", "filter"})
    PagingResults<ChildAssociationRef> getRootCategories(StoreRef storeRef, QName aspectName, PagingRequest pagingRequest, boolean sortByName, String filter);

    /**
     * Get a paged list of the root categories for an aspect/classification supporting multiple name filters.
     *
     * @param storeRef
     * @param aspectName
     * @param pagingRequest
     * @param sortByName
     * @param exactNamesFilter
     * @param alikeNamesFilter
     * @return
     */
    @Auditable(parameters = {"storeRef", "aspectName", "pagingRequest", "sortByName", "exactNamesFilter", "alikeNamesFilter"})
    default PagingResults<ChildAssociationRef> getRootCategories(StoreRef storeRef, QName aspectName, PagingRequest pagingRequest, boolean sortByName,
        Collection<String> exactNamesFilter, Collection<String> alikeNamesFilter)
    {
        return new EmptyPagingResults<>();
    }

    /**
     * Get the root categories for an aspect/classification with names that start with filter
     * 
     * @param storeRef StoreRef
     * @param aspectName QName
     * @param filter String
     */
    @Auditable(parameters = {"storeRef", "aspectName"})
    public Collection<ChildAssociationRef> getRootCategories(StoreRef storeRef, QName aspectName, String filter);

    /**
     * Looks up a category by name under its immediate parent. Index-independent so can be used for cluster-safe
     * existence checks.
     * 
     * @param parent
     *            the parent
     * @param aspectName
     *            the aspect name
     * @param name
     *            the category name
     * @return the category child association reference
     */
    @Auditable(parameters = {"storeRef", "aspectName", "name"})
    public ChildAssociationRef getCategory(NodeRef parent, QName aspectName, String name);
    
    /**
     * Gets root categories by name, optionally creating one if one does not exist. Index-independent so can be used for
     * cluster-safe existence checks.
     * 
     * @param storeRef
     *            the store ref
     * @param aspectName
     *            the aspect name
     * @param name
     *            the aspect name
     * @param create
     *            should a category node be created if one does not exist?
     * @return the root categories
     */
    @Auditable(parameters = {"storeRef", "aspectName", "name", "create"})
    public Collection<ChildAssociationRef> getRootCategories(StoreRef storeRef, QName aspectName, String name,
            boolean create);

    /**
     * Get all the types that represent categories
     * 
     */
    @Auditable
    public Collection<QName> getClassificationAspects();

    /**
     * Create a new category.
     * 
     * This will extend the category types in the data dictionary
     * All it needs is the type name and the attribute in which to store noderefs to categories.
     * 
     * @param storeRef StoreRef
     * @param aspectName QName
     * @param attributeName String
     */
    @Auditable(parameters = {"storeRef", "aspectName", "attributeName"})
    public NodeRef createClassification(StoreRef storeRef, QName aspectName, String attributeName);
    
    /**
     * Create a new root category in the given classification
     * 
     * @param storeRef StoreRef
     * @param aspectName QName
     * @param name String
     * @return NodeRef
     */
    @Auditable(parameters = {"storeRef", "aspectName", "name"})
    public NodeRef createRootCategory(StoreRef storeRef, QName aspectName, String name);
    
    /**
     *  Create a new category.
     * 
     * @param parent NodeRef
     * @param name String
     * @return NodeRef
     */
    @Auditable(parameters = {"parent", "name"})
    public NodeRef createCategory(NodeRef parent, String name);
    
    /**
     * Delete a classification
     * 
     * @param storeRef StoreRef
     * @param aspectName QName
     */
    @Auditable(parameters = {"storeRef", "aspectName"})
    public void deleteClassification(StoreRef storeRef, QName aspectName);
    
    /**
     * Delete a category
     * 
     * @param nodeRef NodeRef
     */
    @Auditable(parameters = {"nodeRef"})
    public void deleteCategory(NodeRef nodeRef);

   /**
    * Get the most polular categories
    *
    * @param storeRef StoreRef
    * @param aspectName QName
    * @param count int
    * @return List
    */
    @Auditable(parameters = {"storeRef", "aspectName", "count"})
    public List<Pair<NodeRef, Integer>> getTopCategories(StoreRef storeRef, QName aspectName, int count);

    /**
     * Get a root category NodeRef
     *
     * @return NodeRef for category root node
     */
    @Experimental
    @Auditable(parameters = {"storeRef"})
    default Optional<NodeRef> getRootCategoryNodeRef(final StoreRef storeRef)
    {
        return Optional.empty();
    }
}
