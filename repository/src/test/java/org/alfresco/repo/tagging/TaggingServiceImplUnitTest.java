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
package org.alfresco.repo.tagging;

import static java.util.Collections.emptyMap;

import static org.alfresco.model.ContentModel.ASPECT_TAGGABLE;
import static org.alfresco.model.ContentModel.ASSOC_SUBCATEGORIES;
import static org.alfresco.model.ContentModel.PROP_NAME;
import static org.alfresco.repo.tagging.TaggingServiceImpl.TAG_UPDATES;
import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
import static org.alfresco.service.cmr.search.SearchService.LANGUAGE_LUCENE;
import static org.alfresco.service.cmr.tagging.TaggingService.TAG_ROOT_NODE_REF;
import static org.alfresco.service.namespace.NamespaceService.CONTENT_MODEL_1_0_URI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.event2.EventGenerator;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TaggingServiceImplUnitTest
{
    private static final String TAG_ID = "tag-node-id";
    private static final String TAG_NAME = "tag-dummy-name";
    private static final NodeRef TAG_NODE_REF = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, TAG_ID);
    private static final NodeRef CONTENT_NODE_REF = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "content-id");

    @Mock
    private NodeService nodeServiceMock;
    @Mock
    private CategoryService categoryServiceMock;
    @Mock
    private PolicyComponent policyComponentMock;
    @Mock
    private SearchService searchServiceMock;
    @Mock
    private ResultSet resultSetMock;
    @Mock(extraInterfaces = List.class)
    private Serializable currentTagsMock;
    @Mock
    private EventGenerator eventGenerator;

    @InjectMocks
    private TaggingServiceImpl taggingService;

    @Before
    public void setUp() throws Exception
    {
        AlfrescoTransactionSupport.bindResource(TAG_UPDATES, new HashMap<>());
        taggingService.init();
    }

    @Test
    public void testCreateTags()
    {
        final ChildAssociationRef tagAssociationMock = mock(ChildAssociationRef.class);
        given(categoryServiceMock.getRootCategories(any(), any(), any(String.class), eq(true))).willReturn(List.of(tagAssociationMock));
        given(tagAssociationMock.getChildRef()).willReturn(TAG_NODE_REF);

        //when
        final List<Pair<String, NodeRef>> actualTagPairs = taggingService.createTags(STORE_REF_WORKSPACE_SPACESSTORE, List.of(TAG_NAME));

        then(categoryServiceMock).should().getRootCategories(STORE_REF_WORKSPACE_SPACESSTORE, ASPECT_TAGGABLE, TAG_NAME, false);
        then(categoryServiceMock).should().getRootCategories(STORE_REF_WORKSPACE_SPACESSTORE, ASPECT_TAGGABLE, TAG_NAME, true);
        then(categoryServiceMock).shouldHaveNoMoreInteractions();
        List<Pair<String, NodeRef>> expectedTagPairs = List.of(new Pair<>(TAG_NAME, TAG_NODE_REF));
        assertThat(actualTagPairs)
            .isNotNull()
            .isEqualTo(expectedTagPairs);
    }

    @Test
    public void testCreateTags_whileTagAlreadyExists()
    {
        given(categoryServiceMock.getRootCategories(any(), any(), any(String.class), eq(false))).willThrow(new DuplicateChildNodeNameException(null, null, null, null));

        //when
        final Throwable actualException = catchThrowable(() -> taggingService.createTags(STORE_REF_WORKSPACE_SPACESSTORE, List.of(TAG_NAME)));

        then(categoryServiceMock).should().getRootCategories(STORE_REF_WORKSPACE_SPACESSTORE, ASPECT_TAGGABLE, TAG_NAME, false);
        then(categoryServiceMock).shouldHaveNoMoreInteractions();
        assertThat(actualException).isInstanceOf(DuplicateChildNodeNameException.class);
    }

    @Test
    public void testChangeTag()
    {
        final String newTagName = "new-tag-name";
        given(categoryServiceMock.getRootCategories(STORE_REF_WORKSPACE_SPACESSTORE, ASPECT_TAGGABLE, TAG_NAME, false)).willReturn(childAssociationsOf(TAG_NODE_REF));
        given(searchServiceMock.query(STORE_REF_WORKSPACE_SPACESSTORE, LANGUAGE_LUCENE, "+PATH:\"/cm:taggable/cm:" + TAG_NAME + "/member\"")).willReturn(resultSetMock);
        given(resultSetMock.getNodeRefs()).willReturn(List.of(CONTENT_NODE_REF));

        //when
        taggingService.changeTag(STORE_REF_WORKSPACE_SPACESSTORE, TAG_NAME, newTagName);

        then(nodeServiceMock).should().setProperty(TAG_NODE_REF, PROP_NAME, newTagName);
        then(nodeServiceMock).should().moveNode(TAG_NODE_REF, TAG_ROOT_NODE_REF, ASSOC_SUBCATEGORIES, QName.createQName(CONTENT_MODEL_1_0_URI, newTagName));
        then(nodeServiceMock).should().getProperties(CONTENT_NODE_REF);
        then(nodeServiceMock).should().hasAspect(CONTENT_NODE_REF, ContentModel.ASPECT_TAGSCOPE);
        then(nodeServiceMock).should().getPrimaryParent(CONTENT_NODE_REF);
        then(nodeServiceMock).shouldHaveNoMoreInteractions();

        then(eventGenerator).should().onUpdateProperties(eq(CONTENT_NODE_REF), eq(emptyMap()), any());
        then(eventGenerator).shouldHaveNoMoreInteractions();
    }

    private static List<ChildAssociationRef> childAssociationsOf(final NodeRef... childNodeRefs)
    {
        return Arrays.stream(childNodeRefs)
            .map(childNodeRef -> new ChildAssociationRef(null, null, null, childNodeRef))
            .collect(Collectors.toList());
    }
}
