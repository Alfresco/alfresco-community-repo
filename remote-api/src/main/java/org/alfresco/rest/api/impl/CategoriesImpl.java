/*
 * #%L
 * Alfresco Remote API
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.api.impl;

import static org.alfresco.rest.api.Nodes.PATH_ROOT;
import static org.alfresco.service.cmr.security.AccessStatus.ALLOWED;
import static org.alfresco.service.cmr.security.PermissionService.CHANGE_PERMISSIONS;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

@Experimental
public class CategoriesImpl implements Categories
{
    static final String NOT_A_VALID_CATEGORY = "Node id does not refer to a valid category";
    static final String NO_PERMISSION_TO_MANAGE_A_CATEGORY = "Current user does not have permission to manage a category";
    static final String NO_PERMISSION_TO_READ_CONTENT = "Current user does not have read permission to content";
    static final String NOT_NULL_OR_EMPTY = "Category name must not be null or empty";
    static final String INVALID_NODE_TYPE = "Node of type: [%s] is expected!";

    private final AuthorityService authorityService;
    private final CategoryService categoryService;
    private final Nodes nodes;
    private final NodeService nodeService;
    private final PermissionService permissionService;

    public CategoriesImpl(AuthorityService authorityService, CategoryService categoryService, Nodes nodes, NodeService nodeService, PermissionService permissionService)
    {
        this.authorityService = authorityService;
        this.categoryService = categoryService;
        this.nodes = nodes;
        this.nodeService = nodeService;
        this.permissionService = permissionService;
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
        verifyAdminAuthority();
        final NodeRef parentNodeRef = getCategoryNodeRef(parentCategoryId);
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
        final NodeRef parentNodeRef = getCategoryNodeRef(parentCategoryId);
        final List<ChildAssociationRef> childCategoriesAssocs = nodeService.getChildAssocs(parentNodeRef).stream()
                .filter(ca -> ContentModel.ASSOC_SUBCATEGORIES.equals(ca.getTypeQName()))
                .collect(Collectors.toList());
        final List<Category> categories = childCategoriesAssocs.stream()
                .map(c -> mapToCategory(c.getChildRef()))
                .collect(Collectors.toList());
        return ListPage.of(categories, params.getPaging());
    }

    @Override
    public Category updateCategoryById(final String id, final Category fixedCategoryModel)
    {
        verifyAdminAuthority();
        final NodeRef categoryNodeRef = getCategoryNodeRef(id);
        if (isRootCategory(categoryNodeRef))
        {
            throw new InvalidArgumentException(NOT_A_VALID_CATEGORY, new String[]{id});
        }

        validateCategoryFields(fixedCategoryModel);

        return mapToCategory(changeCategoryName(categoryNodeRef, fixedCategoryModel.getName()));
    }

    @Override
    public void deleteCategoryById(String id, Parameters parameters)
    {
        verifyAdminAuthority();
        final NodeRef nodeRef = getCategoryNodeRef(id);
        if (isRootCategory(nodeRef))
        {
            throw new InvalidArgumentException(NOT_A_VALID_CATEGORY, new String[]{id});
        }

        nodeService.deleteNode(nodeRef);
    }

    @Override
    public List<Category> linkNodeToCategories(final String nodeId, final List<Category> categoryLinks)
    {
        if (CollectionUtils.isEmpty(categoryLinks))
        {
            throw new InvalidArgumentException(NOT_A_VALID_CATEGORY);
        }

        final NodeRef contentNodeRef = nodes.validateNode(nodeId);
        verifyChangePermission(contentNodeRef);

        final Collection<NodeRef> categoryNodeRefs = categoryLinks.stream()
            .filter(Objects::nonNull)
            .map(Category::getId)
            .filter(StringUtils::isNotEmpty)
            .map(this::getCategoryNodeRef)
            .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(categoryNodeRefs) || isRootCategoryPresent(categoryNodeRefs))
        {
            throw new InvalidArgumentException(NOT_A_VALID_CATEGORY);
        }

        linkNodeToCategories(contentNodeRef, categoryNodeRefs);

        return categoryNodeRefs.stream().map(this::mapToCategory).collect(Collectors.toList());
    }

    private void verifyAdminAuthority()
    {
        if (!authorityService.hasAdminAuthority())
        {
            throw new PermissionDeniedException(NO_PERMISSION_TO_MANAGE_A_CATEGORY);
        }
    }

    private void verifyChangePermission(final NodeRef nodeRef)
    {
        if (permissionService.hasPermission(nodeRef, CHANGE_PERMISSIONS) != ALLOWED)
        {
            throw new PermissionDeniedException(NO_PERMISSION_TO_READ_CONTENT);
        }
    }

    /**
     * This method gets category NodeRef for a given category id.
     * If '-root-' is passed as category id, then it's retrieved as a call to {@link org.alfresco.service.cmr.search.CategoryService#getRootCategoryNodeRef}
     * In all other cases it's retrieved as a node of a category type {@link #validateCategoryNode(String)}
     * @param nodeId category node id
     * @return NodRef of category node
     */
    private NodeRef getCategoryNodeRef(String nodeId)
    {
        return PATH_ROOT.equals(nodeId) ?
                categoryService.getRootCategoryNodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)
                        .orElseThrow(() -> new EntityNotFoundException(nodeId)) :
                validateCategoryNode(nodeId);
    }

    /**
     * Validates if the node exists and is a category.
     * @param nodeId (presumably) category node id
     * @return category NodeRef
     */
    private NodeRef validateCategoryNode(String nodeId)
    {
        final NodeRef nodeRef = nodes.validateNode(nodeId);
        if (isNotACategory(nodeRef))
        {
            throw new InvalidArgumentException(NOT_A_VALID_CATEGORY, new String[]{nodeId});
        }
        return nodeRef;
    }

    private NodeRef createCategoryNodeRef(NodeRef parentNodeRef, Category c)
    {
        validateCategoryFields(c);
        return categoryService.createCategory(parentNodeRef, c.getName());
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

    private boolean isNotACategory(NodeRef nodeRef)
    {
        return !nodes.isSubClass(nodeRef, ContentModel.TYPE_CATEGORY, false);
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

    /**
     * Change category qualified name.
     *
     * @param categoryNodeRef Category node reference.
     * @param newName New name.
     * @return Updated category.
     */
    private NodeRef changeCategoryName(final NodeRef categoryNodeRef, final String newName)
    {
        final ChildAssociationRef parentAssociation = nodeService.getPrimaryParent(categoryNodeRef);
        if (parentAssociation == null)
        {
            throw new InvalidArgumentException(NOT_A_VALID_CATEGORY, new String[]{categoryNodeRef.getId()});
        }

        nodeService.setProperty(categoryNodeRef, ContentModel.PROP_NAME, newName);
        final QName newQName = QName.createQName(parentAssociation.getQName().getNamespaceURI(), QName.createValidLocalName(newName));
        return nodeService.moveNode(parentAssociation.getChildRef(), parentAssociation.getParentRef(), parentAssociation.getTypeQName(), newQName).getChildRef();
    }

    /**
     * Validate if fixed category name is not empty.
     *
     * @param fixedCategoryModel Fixed category model.
     */
    private void validateCategoryFields(final Category fixedCategoryModel)
    {
        if (StringUtils.isEmpty(fixedCategoryModel.getName()))
        {
            throw new InvalidArgumentException(NOT_NULL_OR_EMPTY);
        }
    }

    private boolean isRootCategoryPresent(final Collection<NodeRef> categoryNodeRefs)
    {
        return categoryNodeRefs.stream().anyMatch(this::isRootCategory);
    }

    private boolean isCategoryAspectMissing(final NodeRef nodeRef)
    {
        return !nodeService.hasAspect(nodeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE);
    }

    /**
     * Merge already present and new categories ignoring repeating ones.
     *
     * @param currentCategories Already present categories.
     * @param newCategories Categories which should be added.
     * @return Merged categories.
     */
    private Collection<NodeRef> mergeCategories(final Serializable currentCategories, final Collection<NodeRef> newCategories)
    {
        if (currentCategories == null)
        {
            return newCategories;
        }

        final Collection<NodeRef> actualCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, currentCategories);
        final Collection<NodeRef> allCategories = new HashSet<>(actualCategories);
        allCategories.addAll(newCategories);

        return allCategories;
    }

    /**
     * Add to or update node's property cm:categories containing linked category references.
     *
     * @param nodeRef Node reference.
     * @param categoryNodeRefs Category node references.
     */
    private void linkNodeToCategories(final NodeRef nodeRef, final Collection<NodeRef> categoryNodeRefs)
    {
        if (isCategoryAspectMissing(nodeRef))
        {
            final Map<QName, Serializable> properties = Map.of(ContentModel.PROP_CATEGORIES, (Serializable) categoryNodeRefs);
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, properties);
        }
        else
        {
            final Serializable currentCategories = nodeService.getProperty(nodeRef, ContentModel.PROP_CATEGORIES);
            final Collection<NodeRef> allCategories = mergeCategories(currentCategories, categoryNodeRefs);
            nodeService.setProperty(nodeRef, ContentModel.PROP_CATEGORIES, (Serializable) allCategories);
        }
    }
}
