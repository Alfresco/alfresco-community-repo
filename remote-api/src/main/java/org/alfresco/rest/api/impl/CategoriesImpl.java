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

import org.alfresco.model.ContentModel;
import org.alfresco.rest.api.Categories;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Category;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.collections.CollectionUtils;

@Experimental
public class CategoriesImpl implements Categories
{
    static final String NOT_A_VALID_CATEGORY = "Node id does not refer to a valid category";

    private final Nodes nodes;
    private final NodeService nodeService;

    public CategoriesImpl(Nodes nodes, NodeService nodeService)
    {
        this.nodes = nodes;
        this.nodeService = nodeService;
    }

    @Override
    public Category getCategoryById(final String id, final Parameters params)
    {
        final NodeRef nodeRef = nodes.validateNode(id);
        final boolean isCategory = nodes.isSubClass(nodeRef, ContentModel.TYPE_CATEGORY, false);
        if (!isCategory || isRootCategory(nodeRef))
        {
            throw new InvalidArgumentException(NOT_A_VALID_CATEGORY, new String[]{id});
        }
        final Node categoryNode = nodes.getNode(nodeRef.getId());
        final Category category = new Category();
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
        return parentAssocs.stream().anyMatch(pa -> pa.getQName().equals(ContentModel.ASPECT_GEN_CLASSIFIABLE));
    }

    private String getParentId(final NodeRef nodeRef)
    {
        final NodeRef parentRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
        return isRootCategory(parentRef) ? PATH_ROOT : parentRef.getId();
    }
}
