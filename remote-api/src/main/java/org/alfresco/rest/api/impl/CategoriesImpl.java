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

import static org.alfresco.rest.api.Nodes.PATH_ROOT;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.rest.api.Categories;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Category;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.ListPage;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

@Experimental
public class CategoriesImpl implements Categories
{
    static final String NOT_A_VALID_CATEGORY = "Node id does not refer to a valid category";
    static final String NO_PERMISSION_TO_CREATE_A_CATEGORY = "Current user does not have permission to create a category";
    private static final String NOT_NULL_OR_EMPTY = "Category name must not be null or empty";

    private final AuthorityService authorityService;
    private final CategoryService categoryService;
    private final Nodes nodes;
    private final NodeService nodeService;

    public CategoriesImpl(AuthorityService authorityService, CategoryService categoryService, Nodes nodes, NodeService nodeService)
    {
        this.authorityService = authorityService;
        this.categoryService = categoryService;
        this.nodes = nodes;
        this.nodeService = nodeService;
    }

    @Override
    public Category getCategoryById(final String id, final Parameters params)
    {
        final NodeRef nodeRef = getCategoryNodeRef(id);
        if (isRootCategory(nodeRef))
        {
            throw new InvalidArgumentException(NOT_A_VALID_CATEGORY, new String[]{id});
        }

        return mapToCategory(nodeRef);
    }

    @Override
    public List<Category> createSubcategories(String parentCategoryId, List<Category> categories, Parameters parameters)
    {
        if (!authorityService.hasAdminAuthority())
        {
            throw new PermissionDeniedException(NO_PERMISSION_TO_CREATE_A_CATEGORY);
        }
        final NodeRef parentNodeRef = getParentNodeRef(parentCategoryId);
        final List<NodeRef> categoryNodeRefs = categories.stream()
                .map(c -> createCategoryNodeRef(parentNodeRef, c))
                .collect(Collectors.toList());
        return categoryNodeRefs.stream()
                .map(this::mapToCategory)
                .collect(Collectors.toList());
    }

    @Override
    public CollectionWithPagingInfo<Category> getCategoryChildren(String parentCategoryId, Parameters params)
    {
        final NodeRef parentNodeRef = getParentNodeRef(parentCategoryId);
        final List<ChildAssociationRef> childCategoriesAssocs =
                nodeService.getChildAssocs(parentNodeRef).stream()
                        .filter(ca -> ca.getTypeQName().equals(ContentModel.ASSOC_SUBCATEGORIES)).collect(Collectors.toList());
        final List<Category> categories = childCategoriesAssocs.stream().map(c -> mapToCategory(c.getChildRef())).collect(Collectors.toList());
        return ListPage.of(categories, params.getPaging());
    }

    private NodeRef getCategoryNodeRef(String nodeId)
    {
        final NodeRef nodeRef = nodes.validateNode(nodeId);
        if (isNotACategory(nodeRef))
        {
            throw new InvalidArgumentException(NOT_A_VALID_CATEGORY, new String[]{nodeId});
        }
        return nodeRef;
    }

    private NodeRef getParentNodeRef(String parentCategoryId)
    {
        return PATH_ROOT.equals(parentCategoryId) ?
                categoryService.getRootCategoryNodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)
                        .orElseThrow(() -> new EntityNotFoundException(parentCategoryId)) :
                getCategoryNodeRef(parentCategoryId);
    }

    private NodeRef createCategoryNodeRef(NodeRef parentNodeRef, Category c)
    {
        if (StringUtils.isEmpty(c.getName())) {
            throw new InvalidArgumentException(NOT_NULL_OR_EMPTY);
        }
        return categoryService.createCategory(parentNodeRef, c.getName());
    }

    private boolean isNotACategory(NodeRef nodeRef)
    {
        return !nodes.isSubClass(nodeRef, ContentModel.TYPE_CATEGORY, false);
    }

    private Category mapToCategory(NodeRef nodeRef)
    {
        final Node categoryNode = nodes.getNode(nodeRef.getId());
        final boolean hasChildren = CollectionUtils
                .isNotEmpty(nodeService.getChildAssocs(nodeRef, RegexQNamePattern.MATCH_ALL, RegexQNamePattern.MATCH_ALL, false));
        return Category.builder()
                .id(nodeRef.getId())
                .name(categoryNode.getName())
                .parentId(getParentId(nodeRef))
                .hasChildren(hasChildren)
                .create();
    }

    private boolean isRootCategory(final NodeRef nodeRef)
    {
        final List<ChildAssociationRef> parentAssocs = nodeService.getParentAssocs(nodeRef);
        return parentAssocs.stream().anyMatch(pa -> ContentModel.ASPECT_GEN_CLASSIFIABLE.equals(pa.getQName()));
    }

    private String getParentId(final NodeRef nodeRef)
    {
        final NodeRef parentRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
        return isRootCategory(parentRef) ? PATH_ROOT : parentRef.getId();
    }
}
