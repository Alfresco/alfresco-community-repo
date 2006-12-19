/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
public class CategoryNode extends Node
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
    public Node[] getCategoryMembers()
    {
        return buildNodes(services.getCategoryService().getChildren(getNodeRef(), CategoryService.Mode.MEMBERS, CategoryService.Depth.ANY));
    }
    
    public Node[] jsGet_categoryMembers()
    {
        return getCategoryMembers();
    }

    /**
     * @return all the subcategories of a category
     */
    public CategoryNode[] getSubCategories()
    {
        return buildCategoryNodes(services.getCategoryService().getChildren(getNodeRef(), CategoryService.Mode.SUB_CATEGORIES, CategoryService.Depth.ANY));
    }
    
    public CategoryNode[] jsGet_subCategories()
    {
        return getSubCategories();
    }

    /**
     * @return members and subcategories of a category
     */
    public Node[] getMembersAndSubCategories()
    {
        return buildMixedNodes(services.getCategoryService().getChildren(getNodeRef(), CategoryService.Mode.ALL, CategoryService.Depth.ANY));
    }
    
    public Node[] jsGet_membersAndSubCategories()
    {
        return getMembersAndSubCategories();
    }

    /**
     * @return all the immediate member of a category
     */
    public Node[] getImmediateCategoryMembers()
    {
        return buildNodes(services.getCategoryService().getChildren(getNodeRef(), CategoryService.Mode.MEMBERS, CategoryService.Depth.IMMEDIATE));
    }
    
    public Node[] jsGet_immediateCategoryMembers()
    {
        return getImmediateCategoryMembers();
    }

    /**
     * @return all the immediate subcategories of a category
     */
    public CategoryNode[] getImmediateSubCategories()
    {
        return buildCategoryNodes(services.getCategoryService().getChildren(getNodeRef(), CategoryService.Mode.SUB_CATEGORIES, CategoryService.Depth.IMMEDIATE));
    }
    
    public CategoryNode[] jsGet_immediateSubCategories()
    {
        return getImmediateSubCategories();
    }

    /**
     * @return immediate members and subcategories of a category 
     */
    public Node[] getImmediateMembersAndSubCategories()
    {
        return buildMixedNodes(services.getCategoryService().getChildren(getNodeRef(), CategoryService.Mode.ALL, CategoryService.Depth.IMMEDIATE));
    }
    
    public Node[] jsGet_immediateMembersAndSubCategories()
    {
        return getImmediateMembersAndSubCategories();
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

    private Node[] buildNodes(Collection<ChildAssociationRef> cars)
    {
        Node[] nodes = new Node[cars.size()];
        int i = 0;
        for (ChildAssociationRef car : cars)
        {
            nodes[i++] = new Node(car.getChildRef(), this.services, this.scope);
        }
        return nodes;
    }

    private Node[] buildMixedNodes(Collection<ChildAssociationRef> cars)
    {
        Node[] nodes = new Node[cars.size()];
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
                nodes[i++] = new Node(car.getChildRef(), this.services, this.scope);
            }
        }
        return nodes;
    }
}
