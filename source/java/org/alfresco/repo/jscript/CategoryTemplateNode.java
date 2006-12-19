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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateNode;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.namespace.QName;

/**
 * Category Nodes from the classification helper have special support.
 */
public class CategoryTemplateNode extends TemplateNode
{
    private static final long serialVersionUID = -2595282439089450151L;

    /**
     * Constructor
     * 
     * @param nodeRef
     * @param services
     * @param resolver
     */
    public CategoryTemplateNode(NodeRef nodeRef, ServiceRegistry services)
    {
        super(nodeRef, services);
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
     * @param childRefs
     * 
     * @return List of TemplateNode
     */
    private List<TemplateNode> buildTemplateNodeList(Collection<ChildAssociationRef> childRefs)
    {
        List<TemplateNode> answer = new ArrayList<TemplateNode>(childRefs.size());
        for (ChildAssociationRef ref : childRefs)
        {
            // create our Node representation from the NodeRef
            TemplateNode child = new TemplateNode(ref.getChildRef(), this.services);
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
            CategoryTemplateNode child = new CategoryTemplateNode(ref.getChildRef(), this.services);
            answer.add(child);
        }
        return answer;
    }
    
    private List<TemplateNode> buildMixedNodeList(Collection<ChildAssociationRef> cars)
    {
        List<TemplateNode> nodes = new ArrayList<TemplateNode>(cars.size());
        for (ChildAssociationRef car : cars)
        {
            QName type = services.getNodeService().getType(car.getChildRef());
            if (services.getDictionaryService().isSubClass(type, ContentModel.TYPE_CATEGORY))
            {
                nodes.add(new CategoryTemplateNode(car.getChildRef(), this.services));
            }
            else
            {
                nodes.add(new TemplateNode(car.getChildRef(), this.services));
            }
        }
        return nodes;
    }
}
