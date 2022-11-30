/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.api.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.rest.api.Categories;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Category;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.namespace.RegexQNamePattern;

public class CategoriesImpl implements Categories
{
    private final Nodes nodes;
    private final NodeService nodeService;

    public CategoriesImpl(Nodes nodes, NodeService nodeService)
    {
        this.nodes = nodes;
        this.nodeService = nodeService;
    }

    @Override
    public Category getCategoryById(String id, Parameters params)
    {
        final NodeRef nodeRef = Nodes.PATH_ROOT.equals(id) ? getRooCategoryNodeRef() : nodes.validateNode(id);
        final boolean isCategory = nodes.isSubClass(nodeRef, ContentModel.TYPE_CATEGORY, false);
        if (!isCategory)
        {
            throw new InvalidArgumentException("Node id does not refer to a category", new String[]{id});
        }
        final Node categoryNode = nodes.getNode(nodeRef.getId());
        final Category category = new Category();
        category.setId(nodeRef.getId());
        category.setName(categoryNode.getName());
        final ChildAssociationRef primaryParent = nodeService.getPrimaryParent(nodeRef);
        final NodeRef parentNode = primaryParent.getParentRef();
        category.setParentId(parentNode.getId());
        final boolean hasChildren =
                !nodeService.getChildAssocs(nodeRef, RegexQNamePattern.MATCH_ALL, RegexQNamePattern.MATCH_ALL, false).isEmpty();
        category.setHasChildren(hasChildren);

        return category;
    }

    private NodeRef getRooCategoryNodeRef()
    {
        final NodeRef rootNode = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        final List<ChildAssociationRef> rootChildAssocs = nodeService.getChildAssocs(rootNode, Set.of(ContentModel.TYPE_CATEGORYROOT));
        final ChildAssociationRef categoryRootChild = rootChildAssocs.iterator().next();
        final List<ChildAssociationRef> categoryRootChildAssocs = nodeService.getChildAssocs(categoryRootChild.getChildRef()).stream()
                .filter(ca -> ca.getQName().equals(ContentModel.ASPECT_GEN_CLASSIFIABLE))
                .collect(Collectors.toList());
        return categoryRootChildAssocs.iterator().next().getChildRef();
    }
}
