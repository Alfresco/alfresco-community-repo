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
    /**
     * Constructor
     * 
     * @param nodeRef
     * @param services
     * @param resolver
     */
    public CategoryNode(NodeRef nodeRef, ServiceRegistry services)
    {
        super(nodeRef, services);
    }

    /**
     * Constructor
     * 
     * @param nodeRef
     * @param services
     * @param resolver
     * @param scope
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
     * Remove this category
     */
    public void removeCategory()
    {
        services.getCategoryService().deleteCategory(getNodeRef());
    }

    @Override
    public boolean getIsCategory()
    {
        return true;
    }
    
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
