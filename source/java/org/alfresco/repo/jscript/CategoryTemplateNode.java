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
package org.alfresco.repo.jscript;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.namespace.QName;

/**
 * Category Nodes from the classification helper have special support.
 */
public class CategoryTemplateNode extends TemplateNode
{
    /**
     * Constructor
     * 
     * @param nodeRef NodeRef
     * @param services ServiceRegistry
     * @param resolver TemplateImageResolver
     */
    public CategoryTemplateNode(NodeRef nodeRef, ServiceRegistry services, TemplateImageResolver resolver)
    {
        super(nodeRef, services, resolver);
    }
    
    @Override
    public boolean getIsCategory()
    {
        return true;
    }
    
    /**
     * @return all the member of a category
     */
    public List<TemplateNode> getCategoryMembers()
    {
        if (getIsCategory())
        {
            return buildTemplateNodeList(services.getCategoryService().getChildren(getNodeRef(),
                    CategoryService.Mode.MEMBERS, CategoryService.Depth.ANY));
        }
        else
        {
            return Collections.<TemplateNode>emptyList();
        }
    }

    /**
     * @return all the subcategories of a category
     */
    public List<CategoryTemplateNode> getSubCategories()
    {
        if (getIsCategory())
        {
            return buildCategoryNodeList(services.getCategoryService().getChildren(getNodeRef(),
                    CategoryService.Mode.SUB_CATEGORIES, CategoryService.Depth.ANY));
        }
        else
        {
            return Collections.<CategoryTemplateNode>emptyList();
        }
    }

    /**
     * @return members and subcategories of a category
     */
    public List<TemplateNode> getMembersAndSubCategories()
    {
        if (getIsCategory())
        {

            return buildMixedNodeList(services.getCategoryService().getChildren(getNodeRef(), CategoryService.Mode.ALL,
                    CategoryService.Depth.ANY));
        }
        else
        {
            return Collections.<TemplateNode>emptyList();
        }
    }

    /**
     * @return all the immediate member of a category
     */
    public List<TemplateNode> getImmediateCategoryMembers()
    {
        if (getIsCategory())
        {
            return buildTemplateNodeList(services.getCategoryService().getChildren(getNodeRef(),
                    CategoryService.Mode.MEMBERS, CategoryService.Depth.IMMEDIATE));
        }
        else
        {
            return Collections.<TemplateNode>emptyList();
        }
    }

    /**
     * @return all the immediate subcategories of a category
     */
    public List<CategoryTemplateNode> getImmediateSubCategories()
    {
        if (getIsCategory())
        {
            return buildCategoryNodeList(services.getCategoryService().getChildren(getNodeRef(),
                    CategoryService.Mode.SUB_CATEGORIES, CategoryService.Depth.IMMEDIATE));
        }
        else
        {
            return Collections.<CategoryTemplateNode>emptyList();
        }
    }

    /**
     * @return immediate members and subcategories of a category
     */
    public List<TemplateNode> getImmediateMembersAndSubCategories()
    {
        if (getIsCategory())
        {
            return buildMixedNodeList(services.getCategoryService().getChildren(getNodeRef(),
                    CategoryService.Mode.ALL, CategoryService.Depth.IMMEDIATE));
        }
        else
        {
            return Collections.<TemplateNode>emptyList();
        }
    }

    /**
     * Support to build node lists from category service API calls.
     * 
     * @param childRefs Collection<ChildAssociationRef>
     * 
     * @return List of TemplateNode
     */
    private List<TemplateNode> buildTemplateNodeList(Collection<ChildAssociationRef> childRefs)
    {
        List<TemplateNode> answer = new ArrayList<TemplateNode>(childRefs.size());
        for (ChildAssociationRef ref : childRefs)
        {
            // create our Node representation from the NodeRef
            TemplateNode child = new TemplateNode(ref.getChildRef(), this.services, this.imageResolver);
            answer.add(child);
        }
        return answer;
    }
    
    private List<CategoryTemplateNode> buildCategoryNodeList(Collection<ChildAssociationRef> childRefs)
    {
        List<CategoryTemplateNode> answer = new ArrayList<CategoryTemplateNode>(childRefs.size());
        for (ChildAssociationRef ref : childRefs)
        {
            // create our Node representation from the NodeRef
            CategoryTemplateNode child = new CategoryTemplateNode(ref.getChildRef(), this.services, this.imageResolver);
            answer.add(child);
        }
        return answer;
    }
    
    private List<TemplateNode> buildMixedNodeList(Collection<ChildAssociationRef> cars)
    {
        List<TemplateNode> nodes = new ArrayList<TemplateNode>(cars.size());
        int i = 0;
        for (ChildAssociationRef car : cars)
        {
            QName type = services.getNodeService().getType(car.getChildRef());
            if (services.getDictionaryService().isSubClass(type, ContentModel.TYPE_CATEGORY))
            {
                nodes.add(new CategoryTemplateNode(car.getChildRef(), this.services, this.imageResolver));
            }
            else
            {
                nodes.add(new TemplateNode(car.getChildRef(), this.services, this.imageResolver));
            }
        }
        return nodes;
    }
}
