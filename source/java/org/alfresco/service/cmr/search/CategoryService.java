/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.service.cmr.search;

import java.util.Collection;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

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
@PublicService
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
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"categoryRef", "mode", "depth"})
    public Collection<ChildAssociationRef> getChildren(NodeRef categoryRef, Mode mode, Depth depth );

    /**
     * Get a list of all the categories appropriate for a given property.
     * The full list of categories that may be assigned for this aspect.
     * 
     * @param aspectQName
     * @param depth - the enumeration depth for what level to recover
     * @return a collection of all the nodes found identified by their ChildAssocRef's
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"storeRef", "aspectQName", "depth"})
    public Collection<ChildAssociationRef> getCategories(StoreRef storeRef, QName aspectQName, Depth depth );

    /**
     * Get all the classification entries
     * 
     * @return
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"storeRef"})
    public Collection<ChildAssociationRef> getClassifications(StoreRef storeRef);

    /**
     * Get the root categories for an aspect/classification
     * 
     * @param storeRef
     * @param aspectName
     * @return
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"storeRef", "aspectName"})
    public Collection<ChildAssociationRef> getRootCategories(StoreRef storeRef, QName aspectName);
    
    /**
     * Get all the types that represent categories
     * 
     * @return
     */
    @Auditable
    public Collection<QName> getClassificationAspects();

    /**
     * Create a new category.
     * 
     * This will extend the category types in the data dictionary
     * All it needs is the type name and the attribute in which to store noderefs to categories.
     * 
     * @param aspectName
     * @param attributeName
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"storeRef", "aspectName", "attributeName"})
    public NodeRef createClassifiction(StoreRef storeRef, QName aspectName, String attributeName);
    
    /**
     * Create a new root category in the given classification
     * 
     * @param storeRef
     * @param aspectName
     * @param name
     * @return
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"storeRef", "aspectName", "name"})
    public NodeRef createRootCategory(StoreRef storeRef, QName aspectName, String name);
    
    /**
     *  Create a new category.
     * 
     * @param parent
     * @param name
     * @return
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"parent", "name"})
    public NodeRef createCategory(NodeRef parent, String name);
    
    /**
     * Delete a classification
     * 
     * @param storeRef
     * @param aspectName
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"storeRef", "aspectName"})
    public void deleteClassification(StoreRef storeRef, QName aspectName);
    
    /**
     * Delete a category
     * 
     * @param nodeRef
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef"})
    public void deleteCategory(NodeRef nodeRef);
}
