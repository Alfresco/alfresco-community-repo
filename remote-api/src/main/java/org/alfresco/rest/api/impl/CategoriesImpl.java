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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.rest.api.Categories;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Category;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.InvalidNodeTypeException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
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
import org.alfresco.util.Pair;
import org.alfresco.util.TypeConstraint;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

@Experimental
public class CategoriesImpl implements Categories
{
    static final String INCLUDE_COUNT_PARAM = "count";
    static final String NOT_A_VALID_CATEGORY = "Node id does not refer to a valid category";
    static final String NO_PERMISSION_TO_MANAGE_A_CATEGORY = "Current user does not have permission to manage a category";
    static final String NO_PERMISSION_TO_READ_CONTENT = "Current user does not have read permission to content";
    static final String NO_PERMISSION_TO_CHANGE_CONTENT = "Current user does not have change permission to content";
    static final String NOT_NULL_OR_EMPTY = "Category name must not be null or empty";
    static final String INVALID_NODE_TYPE = "Cannot categorize this type of node";

    private final AuthorityService authorityService;
    private final CategoryService categoryService;
    private final Nodes nodes;
    private final NodeService nodeService;
    private final PermissionService permissionService;
    private final TypeConstraint typeConstraint;

    public CategoriesImpl(AuthorityService authorityService, CategoryService categoryService, Nodes nodes, NodeService nodeService,
        PermissionService permissionService, TypeConstraint typeConstraint)
    {
        this.authorityService = authorityService;
        this.categoryService = categoryService;
        this.nodes = nodes;
        this.nodeService = nodeService;
        this.permissionService = permissionService;
        this.typeConstraint = typeConstraint;
    }

    @Override
    public Category getCategoryById(final StoreRef storeRef, final String id, final Parameters parameters)
    {
        final NodeRef nodeRef = getCategoryNodeRef(storeRef, id);
        if (isRootCategory(nodeRef))
        {
            throw new InvalidArgumentException(NOT_A_VALID_CATEGORY, new String[]{id});
        }

        final Category category = mapToCategory(nodeRef);

        if (parameters.getInclude().contains(INCLUDE_COUNT_PARAM))
        {
            final Map<String, Integer> categoriesCount = getCategoriesCount(storeRef);
            category.setCount(categoriesCount.getOrDefault(category.getId(), 0));
        }

        return category;
    }

    @Override
    public List<Category> createSubcategories(final StoreRef storeRef, final String parentCategoryId, final List<Category> categories, final Parameters parameters)
    {
        verifyAdminAuthority();
        final NodeRef parentNodeRef = getCategoryNodeRef(storeRef, parentCategoryId);

        return categories.stream()
                .map(c -> createCategoryNodeRef(parentNodeRef, c))
                .map(this::mapToCategory)
                .peek(category -> {
                    if (parameters.getInclude().contains(INCLUDE_COUNT_PARAM))
                    {
                        category.setCount(0);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> getCategoryChildren(final StoreRef storeRef, final String parentCategoryId, final Parameters parameters)
    {
        final NodeRef parentNodeRef = getCategoryNodeRef(storeRef, parentCategoryId);
        final List<Category> categories = nodeService.getChildAssocs(parentNodeRef).stream()
            .filter(ca -> ContentModel.ASSOC_SUBCATEGORIES.equals(ca.getTypeQName()))
            .map(ChildAssociationRef::getChildRef)
            .map(this::mapToCategory)
            .collect(Collectors.toList());

        if (parameters.getInclude().contains(INCLUDE_COUNT_PARAM))
        {
            final Map<String, Integer> categoriesCount = getCategoriesCount(storeRef);
            categories.forEach(category -> category.setCount(categoriesCount.getOrDefault(category.getId(), 0)));
        }

        return categories;
    }

    @Override
    public Category updateCategoryById(final StoreRef storeRef, final String id, final Category fixedCategoryModel, final Parameters parameters)
    {
        verifyAdminAuthority();
        final NodeRef categoryNodeRef = getCategoryNodeRef(storeRef, id);
        if (isRootCategory(categoryNodeRef))
        {
            throw new InvalidArgumentException(NOT_A_VALID_CATEGORY, new String[]{id});
        }

        validateCategoryFields(fixedCategoryModel);
        final Category category = mapToCategory(changeCategoryName(categoryNodeRef, fixedCategoryModel.getName()));

        if (parameters.getInclude().contains(INCLUDE_COUNT_PARAM))
        {
            final Map<String, Integer> categoriesCount = getCategoriesCount(storeRef);
            category.setCount(categoriesCount.getOrDefault(category.getId(), 0));
        }

        return category;
    }

    @Override
    public void deleteCategoryById(final StoreRef storeRef, final String id, final Parameters parameters)
    {
        verifyAdminAuthority();
        final NodeRef nodeRef = getCategoryNodeRef(storeRef, id);
        if (isRootCategory(nodeRef))
        {
            throw new InvalidArgumentException(NOT_A_VALID_CATEGORY, new String[]{id});
        }

        nodeService.deleteNode(nodeRef);
    }

    @Override
    public List<Category> listCategoriesForNode(final String nodeId, final Parameters parameters)
    {
        final NodeRef contentNodeRef = nodes.validateNode(nodeId);
        verifyReadPermission(contentNodeRef);
        verifyNodeType(contentNodeRef);

        final Serializable currentCategories = nodeService.getProperty(contentNodeRef, ContentModel.PROP_CATEGORIES);
        if (currentCategories == null)
        {
            return Collections.emptyList();
        }
        final Collection<NodeRef> actualCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, currentCategories);

        return actualCategories.stream().map(this::mapToCategory).collect(Collectors.toList());
    }

    @Override
    public List<Category> linkNodeToCategories(final StoreRef storeRef, final String nodeId, final List<Category> categoryLinks, final Parameters parameters)
    {
        if (CollectionUtils.isEmpty(categoryLinks))
        {
            throw new InvalidArgumentException(NOT_A_VALID_CATEGORY);
        }

        final NodeRef contentNodeRef = nodes.validateNode(nodeId);
        verifyChangePermission(contentNodeRef);
        verifyNodeType(contentNodeRef);

        final Collection<NodeRef> categoryNodeRefs = categoryLinks.stream()
            .filter(Objects::nonNull)
            .map(Category::getId)
            .filter(StringUtils::isNotEmpty)
            .distinct()
            .map(id -> getCategoryNodeRef(storeRef, id))
            .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(categoryNodeRefs) || isRootCategoryPresent(categoryNodeRefs))
        {
            throw new InvalidArgumentException(NOT_A_VALID_CATEGORY);
        }

        linkNodeToCategories(contentNodeRef, categoryNodeRefs);

        return categoryNodeRefs.stream().map(this::mapToCategory).collect(Collectors.toList());
    }

    @Override
    public void unlinkNodeFromCategory(final StoreRef storeRef, final String nodeId, final String categoryId, final Parameters parameters)
    {
        final NodeRef categoryNodeRef = getCategoryNodeRef(storeRef, categoryId);
        final NodeRef contentNodeRef = nodes.validateNode(nodeId);
        verifyChangePermission(contentNodeRef);
        verifyNodeType(contentNodeRef);

        if (isCategoryAspectMissing(contentNodeRef))
        {
            throw new InvalidArgumentException("Node with id: " + nodeId + " does not belong to a category");
        }
        if (isRootCategory(categoryNodeRef))
        {
            throw new InvalidArgumentException(NOT_A_VALID_CATEGORY, new String[]{categoryId});
        }

        final Collection<NodeRef> allCategories = removeCategory(contentNodeRef, categoryNodeRef);

        if (allCategories.size()==0)
        {
            nodeService.removeAspect(contentNodeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE);
            nodeService.removeProperty(contentNodeRef, ContentModel.PROP_CATEGORIES);
            return;
        }

        nodeService.setProperty(contentNodeRef, ContentModel.PROP_CATEGORIES, (Serializable) allCategories);
    }

    private void verifyAdminAuthority()
    {
        if (!authorityService.hasAdminAuthority())
        {
            throw new PermissionDeniedException(NO_PERMISSION_TO_MANAGE_A_CATEGORY);
        }
    }

    private void verifyReadPermission(final NodeRef nodeRef)
    {
        if (permissionService.hasReadPermission(nodeRef) != ALLOWED)
        {
            throw new PermissionDeniedException(NO_PERMISSION_TO_READ_CONTENT);
        }
    }

    private void verifyChangePermission(final NodeRef nodeRef)
    {
        if (permissionService.hasPermission(nodeRef, CHANGE_PERMISSIONS) != ALLOWED)
        {
            throw new PermissionDeniedException(NO_PERMISSION_TO_CHANGE_CONTENT);
        }
    }

    private void verifyNodeType(final NodeRef nodeRef)
    {
        if (!typeConstraint.matches(nodeRef))
        {
            throw new InvalidNodeTypeException(INVALID_NODE_TYPE);
        }
    }

    /**
     * This method gets category NodeRef for a given category id.
     * If '-root-' is passed as category id, then it's retrieved as a call to {@link org.alfresco.service.cmr.search.CategoryService#getRootCategoryNodeRef}
     * In all other cases it's retrieved as a node of a category type {@link #validateCategoryNode(String)}
     * @param storeRef Reference to node store.
     * @param nodeId category node id
     * @return NodRef of category node
     */
    private NodeRef getCategoryNodeRef(StoreRef storeRef, String nodeId)
    {
        return PATH_ROOT.equals(nodeId) ?
                categoryService.getRootCategoryNodeRef(storeRef)
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
     * Remove specified category from present categories.
     * @param contentNodeRef the nodeRef that contains the categories.
     * @param categoryToRemove category that should be removed.
     * @return updated category list.
     */
    private Collection<NodeRef> removeCategory(final NodeRef contentNodeRef, final NodeRef categoryToRemove)
    {
        final Serializable currentCategories = nodeService.getProperty(contentNodeRef, ContentModel.PROP_CATEGORIES);
        final Collection<NodeRef> actualCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, currentCategories);
        final Collection<NodeRef> updatedCategories = new HashSet<>(actualCategories);
        updatedCategories.remove(categoryToRemove);

        return updatedCategories;
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

    /**
     * Get categories by usage count. Result is a map of category IDs (short form - UUID) as key and usage count as value.
     *
     * @param storeRef Reference to node store.
     * @return Map of categories IDs and usage count.
     */
    private Map<String, Integer> getCategoriesCount(final StoreRef storeRef)
    {
        final String idPrefix = storeRef + "/";
        return categoryService.getTopCategories(storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, Integer.MAX_VALUE)
            .stream()
            .collect(Collectors.toMap(pair -> pair.getFirst().toString().replace(idPrefix, StringUtils.EMPTY), Pair::getSecond));
    }
}
