/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.jscript;

import java.util.Collection;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.namespace.QName;
import org.mozilla.javascript.Scriptable;

/**
 * Category Nodes from the classification helper have special support.
 * 
 * @author Andy Hind
 */
public class CategoryNode extends ScriptNode
{
    /** Serial version UID */
    private static final long serialVersionUID = 5757485873550742331L;

    /**
     * Constructor
     * 
     * @param nodeRef   node reference
     * @param services  service registry
     */
    public CategoryNode(NodeRef nodeRef, ServiceRegistry services)
    {
        super(nodeRef, services);
    }

    /**
     * Constructor
     * 
     * @param nodeRef   node reference
     * @param services  service registry
     * @param scope     scriptable scope
     */
    public CategoryNode(NodeRef nodeRef, ServiceRegistry services, Scriptable scope)
    {
        super(nodeRef, services, scope);
    }

    /**
     * @return all the members of a category
     */
    public ScriptNode[] getCategoryMembers()
    {
        return buildNodes(services.getCategoryService().getChildren(getNodeRef(), CategoryService.Mode.MEMBERS, CategoryService.Depth.ANY));
    }
    
    /**
     * @return all the subcategories of a category
     */
    public CategoryNode[] getSubCategories()
    {
        return buildCategoryNodes(services.getCategoryService().getChildren(getNodeRef(), CategoryService.Mode.SUB_CATEGORIES, CategoryService.Depth.ANY));
    }
    
    /**
     * @return members and subcategories of a category
     */
    public ScriptNode[] getMembersAndSubCategories()
    {
        return buildMixedNodes(services.getCategoryService().getChildren(getNodeRef(), CategoryService.Mode.ALL, CategoryService.Depth.ANY));
    }
    
    /**
     * @return all the immediate member of a category
     */
    public ScriptNode[] getImmediateCategoryMembers()
    {
        return buildNodes(services.getCategoryService().getChildren(getNodeRef(), CategoryService.Mode.MEMBERS, CategoryService.Depth.IMMEDIATE));
    }
    
    /**
     * @return all the immediate subcategories of a category
     */
    public CategoryNode[] getImmediateSubCategories()
    {
        return buildCategoryNodes(services.getCategoryService().getChildren(getNodeRef(), CategoryService.Mode.SUB_CATEGORIES, CategoryService.Depth.IMMEDIATE));
    }
    
    /**
     * @return immediate members and subcategories of a category 
     */
    public ScriptNode[] getImmediateMembersAndSubCategories()
    {
        return buildMixedNodes(services.getCategoryService().getChildren(getNodeRef(), CategoryService.Mode.ALL, CategoryService.Depth.IMMEDIATE));
    }
    
    /**
     * Create a new subcategory
     * 
     * @param name      Of the category to create
     * 
     * @return CategoryNode
     */
    public CategoryNode createSubCategory(String name)
    {
       return  new CategoryNode(services.getCategoryService().createCategory(getNodeRef(), name), this.services, this.scope);
    }
    
    /**
     * Renames the category.
     * 
     * @param name  new cateogory name
     */
    public void rename(String name)
    {
        // Rename the category node
        services.getNodeService().setProperty(getNodeRef(), ContentModel.PROP_NAME, name);

        // ALF-1788 Need to rename the association
        ChildAssociationRef assocRef = services.getNodeService().getPrimaryParent(nodeRef);
        if (assocRef != null)
        {
            QName qname = QName.createQName(assocRef.getQName().getNamespaceURI(), QName.createValidLocalName(name));
            services.getNodeService().moveNode(assocRef.getChildRef(), assocRef.getParentRef(), assocRef.getTypeQName(), qname);
        }
    }

    /**
     * Remove this category
     */
    public void removeCategory()
    {
        services.getCategoryService().deleteCategory(getNodeRef());
    }

    /**
     * Indicates whether this is a category or not.
     * 
     * @return boolean  true if category, false otherwise
     */
    @Override
    public boolean getIsCategory()
    {
        return true;
    }
    
    /**
     * Build category nodes from collection of association references.
     * 
     * @param cars
     * @return
     */
    private CategoryNode[] buildCategoryNodes(Collection<ChildAssociationRef> cars)
    {
        CategoryNode[] categoryNodes = new CategoryNode[cars.size()];
        int i = 0;
        for (ChildAssociationRef car : cars)
        {
            categoryNodes[i++] = new CategoryNode(car.getChildRef(), this.services, this.scope);
        }
        return categoryNodes;
    }

    /**
     * Build script nodes from a collection of association references.
     * 
     * @param cars
     * @return
     */
    private ScriptNode[] buildNodes(Collection<ChildAssociationRef> cars)
    {
        ScriptNode[] nodes = new ScriptNode[cars.size()];
        int i = 0;
        for (ChildAssociationRef car : cars)
        {
            nodes[i++] = new ScriptNode(car.getChildRef(), this.services, this.scope);
        }
        return nodes;
    }

    /**
     * Build script nodes and category nodes from a mixed collection of association references.
     * 
     * @param cars
     * @return
     */
    private ScriptNode[] buildMixedNodes(Collection<ChildAssociationRef> cars)
    {
        ScriptNode[] nodes = new ScriptNode[cars.size()];
        int i = 0;
        for (ChildAssociationRef car : cars)
        {
            QName type = services.getNodeService().getType(car.getChildRef());
            if (services.getDictionaryService().isSubClass(type, ContentModel.TYPE_CATEGORY))
            {
                nodes[i++] = new CategoryNode(car.getChildRef(), this.services, this.scope);
            }
            else
            {
                nodes[i++] = new ScriptNode(car.getChildRef(), this.services, this.scope);
            }
        }
        return nodes;
    }
}
