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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Category;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CategoriesImplTest
{
    private static final String CATEGORY_ID = "category-node-id";
    private static final String CATEGORY_NAME = "categoryName";
    private static final String PARENT_ID = "parent-node-id";
    private static final String CAT_ROOT_NODE_ID = "cat-root-node-id";

    @Mock
    private Nodes nodesMock;
    @Mock
    private NodeService nodeServiceMock;
    @Mock
    private Parameters parametersMock;
    @Mock
    private ChildAssociationRef dummyChildAssociationRefMock;
    @Mock
    private ChildAssociationRef categoryRootChildAssociationRefMock;
    @Mock
    private ChildAssociationRef categoryChildAssociationRefMock;

    @InjectMocks
    private CategoriesImpl objectUnderTest;

    @Test
    public void testGetRootCategoryNodeRef()
    {
        final NodeRef rootNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, PATH_ROOT);
        given(nodeServiceMock.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)).willReturn(rootNodeRef);
        given(nodeServiceMock.getChildAssocs(rootNodeRef, Set.of(ContentModel.TYPE_CATEGORYROOT)))
                .willReturn(List.of(categoryRootChildAssociationRefMock));
        given(categoryChildAssociationRefMock.getQName()).willReturn(ContentModel.ASPECT_GEN_CLASSIFIABLE);
        final NodeRef categoryRootNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, CAT_ROOT_NODE_ID);
        given(categoryChildAssociationRefMock.getChildRef()).willReturn(categoryRootNodeRef);
        given(nodeServiceMock.getChildAssocs(categoryRootChildAssociationRefMock.getChildRef()))
                .willReturn(List.of(categoryChildAssociationRefMock));

        //when
        final NodeRef rooCategoryNodeRef = objectUnderTest.getRooCategoryNodeRef();

        then(nodesMock).shouldHaveNoInteractions();
        then(nodeServiceMock).should().getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        then(nodeServiceMock).should().getChildAssocs(rootNodeRef, Set.of(ContentModel.TYPE_CATEGORYROOT));
        then(nodeServiceMock).should().getChildAssocs(categoryRootChildAssociationRefMock.getChildRef());
        then(nodeServiceMock).shouldHaveNoMoreInteractions();

        assertEquals(CAT_ROOT_NODE_ID, rooCategoryNodeRef.getId());
    }

    @Test
    public void testGetRootCategoryById()
    {
        final NodeRef rootNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, PATH_ROOT);
        final NodeRef categoryRootNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, CAT_ROOT_NODE_ID);
        given(nodeServiceMock.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)).willReturn(rootNodeRef);
        given(nodeServiceMock.getChildAssocs(rootNodeRef, Set.of(ContentModel.TYPE_CATEGORYROOT)))
                .willReturn(List.of(categoryRootChildAssociationRefMock));
        given(categoryChildAssociationRefMock.getQName()).willReturn(ContentModel.ASPECT_GEN_CLASSIFIABLE);
        given(nodeServiceMock.getChildAssocs(categoryRootChildAssociationRefMock.getChildRef()))
                .willReturn(List.of(categoryChildAssociationRefMock));
        given(categoryChildAssociationRefMock.getChildRef()).willReturn(categoryRootNodeRef);
        given(nodesMock.isSubClass(categoryRootNodeRef, ContentModel.TYPE_CATEGORY, false)).willReturn(true);
        final Node categoryNode = new Node();
        categoryNode.setName(CATEGORY_NAME);
        categoryNode.setNodeId(CAT_ROOT_NODE_ID);
        given(nodesMock.getNode(CAT_ROOT_NODE_ID)).willReturn(categoryNode);
        final NodeRef parentNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, PARENT_ID);
        final ChildAssociationRef parentAssoc = new ChildAssociationRef(null, parentNodeRef, null, categoryRootNodeRef);
        given(nodeServiceMock.getPrimaryParent(categoryRootNodeRef)).willReturn(parentAssoc);
        given(nodeServiceMock.getParentAssocs(parentNodeRef)).willReturn(List.of(categoryChildAssociationRefMock));
        given(nodeServiceMock.getChildAssocs(categoryRootNodeRef, RegexQNamePattern.MATCH_ALL, RegexQNamePattern.MATCH_ALL, false))
                .willReturn(List.of(dummyChildAssociationRefMock));

        //when
        final Category category = objectUnderTest.getCategoryById(PATH_ROOT, parametersMock);

        then(nodesMock).should().isSubClass(categoryRootNodeRef, ContentModel.TYPE_CATEGORY, false);
        then(nodesMock).should().getNode(CAT_ROOT_NODE_ID);
        then(nodesMock).shouldHaveNoMoreInteractions();
        then(nodeServiceMock).should().getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        then(nodeServiceMock).should().getChildAssocs(rootNodeRef, Set.of(ContentModel.TYPE_CATEGORYROOT));
        then(nodeServiceMock).should().getChildAssocs(categoryRootChildAssociationRefMock.getChildRef());
        then(nodeServiceMock).should().getPrimaryParent(categoryRootNodeRef);
        then(nodeServiceMock).should().getParentAssocs(parentNodeRef);
        then(nodeServiceMock).should().getChildAssocs(categoryRootNodeRef, RegexQNamePattern.MATCH_ALL, RegexQNamePattern.MATCH_ALL, false);
        then(nodeServiceMock).shouldHaveNoMoreInteractions();

        assertEquals(categoryNode.getName(), category.getName());
        assertEquals(CAT_ROOT_NODE_ID, category.getId());
        assertEquals(PATH_ROOT, category.getParentId());
        assertTrue(category.getHasChildren());
    }

    @Test
    public void testGetCategoryById_withChildren()
    {
        final NodeRef categoryNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, CATEGORY_ID);
        given(nodesMock.validateNode(CATEGORY_ID)).willReturn(categoryNodeRef);
        given(nodesMock.isSubClass(categoryNodeRef, ContentModel.TYPE_CATEGORY, false)).willReturn(true);
        final Node categoryNode = new Node();
        categoryNode.setName(CATEGORY_NAME);
        categoryNode.setNodeId(CATEGORY_ID);
        final NodeRef parentNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, PARENT_ID);
        categoryNode.setParentId(parentNodeRef);
        given(nodesMock.getNode(CATEGORY_ID)).willReturn(categoryNode);
        final ChildAssociationRef parentAssoc = new ChildAssociationRef(null, parentNodeRef, null, categoryNodeRef);
        given(nodeServiceMock.getPrimaryParent(categoryNodeRef)).willReturn(parentAssoc);
        final List<ChildAssociationRef> dummyChildren = List.of(dummyChildAssociationRefMock);
        given(nodeServiceMock.getChildAssocs(categoryNodeRef, RegexQNamePattern.MATCH_ALL, RegexQNamePattern.MATCH_ALL, false))
                .willReturn(dummyChildren);
        //when
        final Category category = objectUnderTest.getCategoryById(CATEGORY_ID, parametersMock);

        then(nodesMock).should().validateNode(CATEGORY_ID);
        then(nodesMock).should().isSubClass(categoryNodeRef, ContentModel.TYPE_CATEGORY, false);
        then(nodesMock).should().getNode(CATEGORY_ID);
        then(nodesMock).shouldHaveNoMoreInteractions();

        then(nodeServiceMock).should().getPrimaryParent(categoryNodeRef);
        then(nodeServiceMock).should().getParentAssocs(parentNodeRef);
        then(nodeServiceMock).should().getChildAssocs(categoryNodeRef, RegexQNamePattern.MATCH_ALL, RegexQNamePattern.MATCH_ALL, false);
        then(nodeServiceMock).shouldHaveNoMoreInteractions();

        assertEquals(categoryNode.getName(), category.getName());
        assertEquals(CATEGORY_ID, category.getId());
        assertEquals(PARENT_ID, category.getParentId());
        assertTrue(category.getHasChildren());
    }

    @Test
    public void testGetCategoryById_withoutChildren()
    {
        final NodeRef categoryNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, CATEGORY_ID);
        given(nodesMock.validateNode(CATEGORY_ID)).willReturn(categoryNodeRef);
        given(nodesMock.isSubClass(categoryNodeRef, ContentModel.TYPE_CATEGORY, false)).willReturn(true);
        final Node categoryNode = new Node();
        categoryNode.setName(CATEGORY_NAME);
        categoryNode.setNodeId(CATEGORY_ID);
        final NodeRef parentNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, PARENT_ID);
        categoryNode.setParentId(parentNodeRef);
        given(nodesMock.getNode(CATEGORY_ID)).willReturn(categoryNode);
        final ChildAssociationRef parentAssoc = new ChildAssociationRef(null, parentNodeRef, null, categoryNodeRef);
        given(nodeServiceMock.getPrimaryParent(categoryNodeRef)).willReturn(parentAssoc);
        given(nodeServiceMock.getChildAssocs(categoryNodeRef, RegexQNamePattern.MATCH_ALL, RegexQNamePattern.MATCH_ALL, false))
                .willReturn(Collections.emptyList());
        //when
        final Category category = objectUnderTest.getCategoryById(CATEGORY_ID, parametersMock);

        then(nodesMock).should().validateNode(CATEGORY_ID);
        then(nodesMock).should().isSubClass(categoryNodeRef, ContentModel.TYPE_CATEGORY, false);
        then(nodesMock).should().getNode(CATEGORY_ID);
        then(nodesMock).shouldHaveNoMoreInteractions();

        then(nodeServiceMock).should().getPrimaryParent(categoryNodeRef);
        then(nodeServiceMock).should().getParentAssocs(parentNodeRef);
        then(nodeServiceMock).should().getChildAssocs(categoryNodeRef, RegexQNamePattern.MATCH_ALL, RegexQNamePattern.MATCH_ALL, false);
        then(nodeServiceMock).shouldHaveNoMoreInteractions();

        assertEquals(categoryNode.getName(), category.getName());
        assertEquals(CATEGORY_ID, category.getId());
        assertEquals(PARENT_ID, category.getParentId());
        assertFalse(category.getHasChildren());
    }

    @Test
    public void testGetCategoryById_notACategory()
    {
        final NodeRef categoryNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, CATEGORY_ID);
        given(nodesMock.validateNode(CATEGORY_ID)).willReturn(categoryNodeRef);
        given(nodesMock.isSubClass(categoryNodeRef, ContentModel.TYPE_CATEGORY, false)).willReturn(false);

        //when
        assertThrows(InvalidArgumentException.class, () -> objectUnderTest.getCategoryById(CATEGORY_ID, parametersMock));

        then(nodesMock).should().validateNode(CATEGORY_ID);
        then(nodesMock).should().isSubClass(categoryNodeRef, ContentModel.TYPE_CATEGORY, false);
        then(nodesMock).shouldHaveNoMoreInteractions();

        then(nodeServiceMock).shouldHaveNoInteractions();
    }

    @Test
    public void testGetCategoryById_nodeNotExists()
    {
        given(nodesMock.validateNode(CATEGORY_ID)).willThrow(EntityNotFoundException.class);

        //when
        assertThrows(EntityNotFoundException.class, () -> objectUnderTest.getCategoryById(CATEGORY_ID, parametersMock));

        then(nodesMock).should().validateNode(CATEGORY_ID);
        then(nodesMock).shouldHaveNoMoreInteractions();

        then(nodeServiceMock).shouldHaveNoInteractions();
    }

    @Test
    public void testGetRootCategoryById_categoryRootNotExists()
    {
        final NodeRef rootNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, PATH_ROOT);
        given(nodeServiceMock.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)).willReturn(rootNodeRef);
        given(nodeServiceMock.getChildAssocs(rootNodeRef, Set.of(ContentModel.TYPE_CATEGORYROOT))).willReturn(Collections.emptyList());

        //when
        assertThrows(EntityNotFoundException.class, () -> objectUnderTest.getCategoryById(PATH_ROOT, parametersMock));

        then(nodesMock).shouldHaveNoInteractions();
        then(nodeServiceMock).should().getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        then(nodeServiceMock).should().getChildAssocs(rootNodeRef, Set.of(ContentModel.TYPE_CATEGORYROOT));
        then(nodeServiceMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testGetRootCategoryById_categoryRootChildNotExists()
    {
        final NodeRef rootNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, PATH_ROOT);
        given(nodeServiceMock.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)).willReturn(rootNodeRef);
        given(nodeServiceMock.getChildAssocs(rootNodeRef, Set.of(ContentModel.TYPE_CATEGORYROOT))).willReturn(Collections.emptyList());
        given(nodeServiceMock.getChildAssocs(rootNodeRef, Set.of(ContentModel.TYPE_CATEGORYROOT)))
                .willReturn(List.of(categoryRootChildAssociationRefMock));
        given(categoryChildAssociationRefMock.getQName()).willReturn(QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "dummy"));
        given(nodeServiceMock.getChildAssocs(categoryRootChildAssociationRefMock.getChildRef()))
                .willReturn(List.of(categoryChildAssociationRefMock));

        //when
        assertThrows(EntityNotFoundException.class, () -> objectUnderTest.getCategoryById(PATH_ROOT, parametersMock));

        then(nodesMock).shouldHaveNoInteractions();
        then(nodeServiceMock).should().getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        then(nodeServiceMock).should().getChildAssocs(rootNodeRef, Set.of(ContentModel.TYPE_CATEGORYROOT));
        then(nodeServiceMock).should().getChildAssocs(categoryRootChildAssociationRefMock.getChildRef());
        then(nodeServiceMock).shouldHaveNoMoreInteractions();
    }
}
