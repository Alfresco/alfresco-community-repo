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

package org.alfresco.repo.search.impl.solr;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.noindex.NoIndexCategoryServiceImpl;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * This test class covers part of the code implemented in abstract class {@link org.alfresco.repo.search.impl.AbstractCategoryServiceImpl}.
 * That's because abstract class cannot be instantiated and directly tested.
 */
@RunWith(MockitoJUnitRunner.class)
public class SolrCategoryServiceImplTest
{
    private static final String PATH_ROOT = "-root-";
    private static final String CAT_ROOT_NODE_ID = "cat-root-node-id";

    @Mock
    private NodeService nodeServiceMock;
    @Mock
    private ChildAssociationRef categoryRootChildAssociationRefMock;
    @Mock
    private ChildAssociationRef categoryChildAssociationRefMock;

    @InjectMocks
    private NoIndexCategoryServiceImpl objectUnderTest;

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
        final Optional<NodeRef> rooCategoryNodeRef = objectUnderTest.getRootCategoryNodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

        then(nodeServiceMock).should().getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        then(nodeServiceMock).should().getChildAssocs(rootNodeRef, Set.of(ContentModel.TYPE_CATEGORYROOT));
        then(nodeServiceMock).should().getChildAssocs(categoryRootChildAssociationRefMock.getChildRef());
        then(nodeServiceMock).shouldHaveNoMoreInteractions();

        assertTrue(rooCategoryNodeRef.isPresent());
        assertEquals(CAT_ROOT_NODE_ID, rooCategoryNodeRef.get().getId());
    }
}
