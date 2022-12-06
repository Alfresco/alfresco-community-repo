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
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.rest.api.Categories;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Category;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
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

@Experimental
public class CategoriesImpl implements Categories
{
    static final String NOT_A_VALID_CATEGORY = "Node id does not refer to a valid category";
    static final String NO_PERMISSION_TO_CREATE_A_CATEGORY = "Current user does not have permission to create a category";

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
        final NodeRef nodeRef = nodes.validateNode(id);
        if (isNotACategory(nodeRef) || isRootCategory(nodeRef))
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
        final NodeRef parentNodeRef = PATH_ROOT.equals(parentCategoryId) ?
                categoryService.getRootCategoryNodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)
                        .orElseThrow(() -> new EntityNotFoundException(parentCategoryId)) :
                nodes.validateNode(parentCategoryId);
        if (isNotACategory(parentNodeRef))
        {
            throw new InvalidArgumentException(NOT_A_VALID_CATEGORY, new String[]{parentCategoryId});
        }
        final List<NodeRef> categoryNodeRefs = categories.stream()
                .map(c -> categoryService.createCategory(parentNodeRef, c.getName()))
                .collect(Collectors.toList());
        return categoryNodeRefs.stream()
                .map(this::mapToCategory)
                .collect(Collectors.toList());
    }

    private boolean isNotACategory(NodeRef nodeRef)
    {
        return !nodes.isSubClass(nodeRef, ContentModel.TYPE_CATEGORY, false);
    }

    private Category mapToCategory(NodeRef nodeRef)
    {
        final Category category = new Category();
        final Node categoryNode = nodes.getNode(nodeRef.getId());
        category.setId(nodeRef.getId());
        category.setName(categoryNode.getName());
        category.setParentId(getParentId(nodeRef));
        final boolean hasChildren = CollectionUtils
                .isNotEmpty(nodeService.getChildAssocs(nodeRef, RegexQNamePattern.MATCH_ALL, RegexQNamePattern.MATCH_ALL, false));
        category.setHasChildren(hasChildren);
        return category;
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
