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

import static java.util.stream.Collectors.toList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import static org.alfresco.rest.api.impl.TagsImpl.NOT_A_VALID_TAG;
import static org.alfresco.rest.api.impl.TagsImpl.NO_PERMISSION_TO_MANAGE_A_TAG;
import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Tag;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.SortColumn;
import org.alfresco.rest.framework.resource.parameters.where.InvalidQueryException;
import org.alfresco.rest.framework.tools.RecognizedParamsExtractor;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.util.Pair;
import org.alfresco.util.TypeConstraint;

@RunWith(MockitoJUnitRunner.class)
public class TagsImplTest
{
    private static final String TAG_ID = "tag-node-id";
    private static final String PARENT_NODE_ID = "tag:tag-root";
    private static final String TAG_NAME = "tag-dummy-name";
    private static final NodeRef TAG_NODE_REF = new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, TAG_ID.concat("-").concat(TAG_NAME));
    private static final NodeRef TAG_PARENT_NODE_REF = new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, PARENT_NODE_ID);
    private static final String CONTENT_NODE_ID = "content-node-id";
    private static final NodeRef CONTENT_NODE_REF = new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, CONTENT_NODE_ID);
    private static final String PARAM_INCLUDE_COUNT = "count";

    private final RecognizedParamsExtractor queryExtractor = new RecognizedParamsExtractor() {};

    @Mock
    private Nodes nodesMock;
    @Mock
    private ChildAssociationRef primaryParentMock;
    @Mock
    private NodeService nodeServiceMock;
    @Mock
    private AuthorityService authorityServiceMock;
    @Mock
    private TaggingService taggingServiceMock;
    @Mock
    private Parameters parametersMock;
    @Mock
    private Paging pagingMock;
    @Mock
    private PagingResults<Pair<NodeRef, String>> pagingResultsMock;
    @Mock
    private TypeConstraint typeConstraintMock;

    @InjectMocks
    private TagsImpl objectUnderTest;

    @Before
    public void setup()
    {
        given(authorityServiceMock.hasAdminAuthority()).willReturn(true);
        given(nodesMock.validateNode(STORE_REF_WORKSPACE_SPACESSTORE, TAG_ID)).willReturn(TAG_NODE_REF);
        given(taggingServiceMock.getTagName(TAG_NODE_REF)).willReturn(TAG_NAME);
        given(nodeServiceMock.getPrimaryParent(TAG_NODE_REF)).willReturn(primaryParentMock);
        given(primaryParentMock.getParentRef()).willReturn(TAG_PARENT_NODE_REF);
    }

    @Test
    public void testGetTags()
    {
        given(parametersMock.getPaging()).willReturn(pagingMock);
        given(parametersMock.getSorting()).willReturn(new ArrayList<>());
        given(parametersMock.getInclude()).willReturn(new ArrayList<>());

        // given(taggingServiceMock.getTags(eq(STORE_REF_WORKSPACE_SPACESSTORE), any(), any(), isNull(), isNull())).willReturn(List.of(new Pair<>(TAG_NODE_REF, null)));
        given(taggingServiceMock.getTags(eq(STORE_REF_WORKSPACE_SPACESSTORE), any(), any(), isNull(), isNull())).willReturn(Map.of(TAG_NODE_REF, 0L));
        given(nodeServiceMock.getProperty(any(NodeRef.class), eq(ContentModel.PROP_NAME))).willReturn("tag-dummy-name");

        final CollectionWithPagingInfo<Tag> actualTags = objectUnderTest.getTags(STORE_REF_WORKSPACE_SPACESSTORE, parametersMock);

        then(taggingServiceMock).should().getTags(eq(STORE_REF_WORKSPACE_SPACESSTORE), any(), any(), isNull(), isNull());
        then(taggingServiceMock).shouldHaveNoMoreInteractions();
        final List<Tag> expectedTags = createTagsWithNodeRefs(List.of(TAG_NAME));
        assertEquals(expectedTags, actualTags.getCollection());
    }

    @Test
    public void testGetTags_verifyIfCountIsZero()
    {
        given(parametersMock.getPaging()).willReturn(pagingMock);
        given(parametersMock.getSorting()).willReturn(new ArrayList<>());
        given(parametersMock.getInclude()).willReturn(List.of(PARAM_INCLUDE_COUNT));
        given(taggingServiceMock.getTags(any(StoreRef.class), any(), any(), any(), any())).willReturn(Map.of(TAG_NODE_REF, 0L));

        given(nodeServiceMock.getProperty(any(NodeRef.class), eq(ContentModel.PROP_NAME))).willReturn("tag-dummy-name");
        given(parametersMock.getInclude()).willReturn(List.of("count"));

        final CollectionWithPagingInfo<Tag> actualTags = objectUnderTest.getTags(STORE_REF_WORKSPACE_SPACESSTORE, parametersMock);

        final List<Tag> expectedTags = createTagsWithNodeRefs(List.of(TAG_NAME)).stream()
                .peek(tag -> tag.setCount(0L))
                .collect(toList());
        assertEquals(expectedTags, actualTags.getCollection());
    }

    /** Check that we can get counts for two tags - one in use and one not applied to any nodes. */
    @Test
    public void testGetTags_verifyCountPopulatedCorrectly()
    {
        NodeRef tagNodeA = new NodeRef("tag://A/");
        NodeRef tagNodeB = new NodeRef("tag://B/");

        given(parametersMock.getSorting()).willReturn(Collections.emptyList());
        given(parametersMock.getPaging()).willReturn(pagingMock);
        given(parametersMock.getInclude()).willReturn(List.of("count"));

        final LinkedHashMap<NodeRef, Long> results = new LinkedHashMap<>();
        results.put(tagNodeA, 5L);
        results.put(tagNodeB, 0L);

        given(taggingServiceMock.getTags(any(StoreRef.class), eq(List.of(PARAM_INCLUDE_COUNT)), isNull(), any(), any())).willReturn(results);
        given(nodeServiceMock.getProperty(any(NodeRef.class), eq(ContentModel.PROP_NAME))).willReturn("taga", "tagb");

        final CollectionWithPagingInfo<Tag> actualTags = objectUnderTest.getTags(STORE_REF_WORKSPACE_SPACESSTORE, parametersMock);

        final List<Tag> expectedTags = List.of(Tag.builder().tag("tagA").nodeRef(tagNodeA).count(5L).create(),
                Tag.builder().tag("tagB").nodeRef(tagNodeB).count(0L).create());
        assertEquals(expectedTags, actualTags.getCollection());
    }

    @Test
    public void testGetTags_orderByCountAscendingOrder()
    {
        NodeRef tagNodeA = new NodeRef("tag://A/");
        NodeRef tagNodeB = new NodeRef("tag://B/");
        NodeRef tagNodeC = new NodeRef("tag://C/");

        given(parametersMock.getPaging()).willReturn(pagingMock);
        given(parametersMock.getInclude()).willReturn(List.of("count"));
        given(parametersMock.getSorting()).willReturn(List.of(new SortColumn("count", true)));

        final LinkedHashMap<NodeRef, Long> results = new LinkedHashMap<>();
        results.put(tagNodeB, 0L);
        results.put(tagNodeC, 2L);
        results.put(tagNodeA, 5L);

        given(taggingServiceMock.getTags(any(StoreRef.class), eq(List.of(PARAM_INCLUDE_COUNT)), eq(new Pair<>("count", true)), any(), any())).willReturn(results);
        given(nodeServiceMock.getProperty(tagNodeA, ContentModel.PROP_NAME)).willReturn("taga");
        given(nodeServiceMock.getProperty(tagNodeB, ContentModel.PROP_NAME)).willReturn("tagb");
        given(nodeServiceMock.getProperty(tagNodeC, ContentModel.PROP_NAME)).willReturn("tagc");

        // when
        final CollectionWithPagingInfo<Tag> actualTags = objectUnderTest.getTags(STORE_REF_WORKSPACE_SPACESSTORE, parametersMock);

        final List<Tag> expectedTags = List.of(Tag.builder().tag("tagb").nodeRef(tagNodeB).count(0L).create(),
                Tag.builder().tag("tagc").nodeRef(tagNodeC).count(2L).create(),
                Tag.builder().tag("taga").nodeRef(tagNodeA).count(5L).create());
        assertEquals(expectedTags, actualTags.getCollection());
    }

    @Test
    public void testGetTags_orderByCountDescendingOrder()
    {
        NodeRef tagNodeA = new NodeRef("tag://A/");
        NodeRef tagNodeB = new NodeRef("tag://B/");
        NodeRef tagNodeC = new NodeRef("tag://C/");

        given(parametersMock.getPaging()).willReturn(pagingMock);
        given(parametersMock.getInclude()).willReturn(List.of("count"));
        given(parametersMock.getSorting()).willReturn(List.of(new SortColumn("count", false)));

        final LinkedHashMap<NodeRef, Long> results = new LinkedHashMap<>();
        results.put(tagNodeA, 5L);
        results.put(tagNodeC, 2L);
        results.put(tagNodeB, 0L);

        given(taggingServiceMock.getTags(any(StoreRef.class), eq(List.of(PARAM_INCLUDE_COUNT)), eq(new Pair<>("count", false)), any(), any())).willReturn(results);
        given(nodeServiceMock.getProperty(tagNodeA, ContentModel.PROP_NAME)).willReturn("taga");
        given(nodeServiceMock.getProperty(tagNodeB, ContentModel.PROP_NAME)).willReturn("tagb");
        given(nodeServiceMock.getProperty(tagNodeC, ContentModel.PROP_NAME)).willReturn("tagc");

        // when
        final CollectionWithPagingInfo<Tag> actualTags = objectUnderTest.getTags(STORE_REF_WORKSPACE_SPACESSTORE, parametersMock);

        final List<Tag> expectedTags = List.of(Tag.builder().tag("taga").nodeRef(tagNodeA).count(5L).create(),
                Tag.builder().tag("tagc").nodeRef(tagNodeC).count(2L).create(),
                Tag.builder().tag("tagb").nodeRef(tagNodeB).count(0L).create());
        assertEquals(expectedTags, actualTags.getCollection());
    }

    @Test
    public void testGetTags_orderByTagAscendingOrder()
    {
        NodeRef tagApple = new NodeRef("tag://apple/");
        NodeRef tagBanana = new NodeRef("tag://banana/");
        NodeRef tagCoconut = new NodeRef("tag://coconut/");

        given(parametersMock.getPaging()).willReturn(pagingMock);
        given(parametersMock.getInclude()).willReturn(Collections.emptyList());
        given(parametersMock.getSorting()).willReturn(List.of(new SortColumn("tag", true)));

        final LinkedHashMap<NodeRef, Long> results = new LinkedHashMap<>();
        results.put(tagApple, 0L);
        results.put(tagBanana, 0L);
        results.put(tagCoconut, 0L);

        given(taggingServiceMock.getTags(any(StoreRef.class), any(), eq(new Pair<>("tag", true)), any(), any())).willReturn(results);
        given(nodeServiceMock.getProperty(tagApple, ContentModel.PROP_NAME)).willReturn("apple");
        given(nodeServiceMock.getProperty(tagBanana, ContentModel.PROP_NAME)).willReturn("banana");
        given(nodeServiceMock.getProperty(tagCoconut, ContentModel.PROP_NAME)).willReturn("coconut");

        // when
        final CollectionWithPagingInfo<Tag> actualTags = objectUnderTest.getTags(STORE_REF_WORKSPACE_SPACESSTORE, parametersMock);

        final List<Tag> expectedTags = List.of(Tag.builder().tag("apple").nodeRef(tagApple).create(),
                Tag.builder().tag("banana").nodeRef(tagBanana).create(),
                Tag.builder().tag("coconut").nodeRef(tagCoconut).create());
        assertEquals(expectedTags, actualTags.getCollection());
    }

    @Test
    public void testGetTags_orderByTagDescendingOrder()
    {
        NodeRef tagApple = new NodeRef("tag://apple/");
        NodeRef tagBanana = new NodeRef("tag://banana/");
        NodeRef tagCoconut = new NodeRef("tag://coconut/");

        given(parametersMock.getPaging()).willReturn(pagingMock);
        given(parametersMock.getInclude()).willReturn(Collections.emptyList());
        given(parametersMock.getSorting()).willReturn(List.of(new SortColumn("tag", false)));

        final LinkedHashMap<NodeRef, Long> results = new LinkedHashMap<>();
        results.put(tagCoconut, 0L);
        results.put(tagBanana, 0L);
        results.put(tagApple, 0L);

        given(taggingServiceMock.getTags(any(StoreRef.class), any(), eq(new Pair<>("tag", false)), any(), any())).willReturn(results);
        given(nodeServiceMock.getProperty(tagApple, ContentModel.PROP_NAME)).willReturn("apple");
        given(nodeServiceMock.getProperty(tagBanana, ContentModel.PROP_NAME)).willReturn("banana");
        given(nodeServiceMock.getProperty(tagCoconut, ContentModel.PROP_NAME)).willReturn("coconut");

        // when
        final CollectionWithPagingInfo<Tag> actualTags = objectUnderTest.getTags(STORE_REF_WORKSPACE_SPACESSTORE, parametersMock);

        final List<Tag> expectedTags = List.of(Tag.builder().tag("coconut").nodeRef(tagCoconut).create(),
                Tag.builder().tag("banana").nodeRef(tagBanana).create(),
                Tag.builder().tag("apple").nodeRef(tagApple).create());
        assertEquals(expectedTags, actualTags.getCollection());
    }

    @Test
    public void testGetTags_withEqualsClauseWhereQuery()
    {
        given(parametersMock.getPaging()).willReturn(pagingMock);
        given(parametersMock.getQuery()).willReturn(queryExtractor.getWhereClause("(tag=expectedName)"));
        given(parametersMock.getSorting()).willReturn(new ArrayList<>());
        given(parametersMock.getInclude()).willReturn(new ArrayList<>());

        // when
        final CollectionWithPagingInfo<Tag> actualTags = objectUnderTest.getTags(STORE_REF_WORKSPACE_SPACESSTORE, parametersMock);

        then(taggingServiceMock).should().getTags(eq(STORE_REF_WORKSPACE_SPACESSTORE), eq(new ArrayList<>()), any(), eq(Set.of("expectedname")), isNull());
        then(taggingServiceMock).shouldHaveNoMoreInteractions();
        assertThat(actualTags).isNotNull();
    }

    @Test
    public void testGetTags_withInClauseWhereQuery()
    {
        given(parametersMock.getPaging()).willReturn(pagingMock);
        given(parametersMock.getQuery()).willReturn(queryExtractor.getWhereClause("(tag IN (expectedName1, expectedName2))"));
        given(parametersMock.getSorting()).willReturn(new ArrayList<>());
        given(parametersMock.getInclude()).willReturn(new ArrayList<>());

        // when
        final CollectionWithPagingInfo<Tag> actualTags = objectUnderTest.getTags(STORE_REF_WORKSPACE_SPACESSTORE, parametersMock);

        then(taggingServiceMock).should().getTags(eq(STORE_REF_WORKSPACE_SPACESSTORE), any(), any(), eq(Set.of("expectedname1", "expectedname2")), isNull());
        then(taggingServiceMock).shouldHaveNoMoreInteractions();
        assertThat(actualTags).isNotNull();
    }

    @Test
    public void testGetTags_withMatchesClauseWhereQuery()
    {
        given(parametersMock.getPaging()).willReturn(pagingMock);
        given(parametersMock.getQuery()).willReturn(queryExtractor.getWhereClause("(tag MATCHES ('expectedName*'))"));
        given(parametersMock.getSorting()).willReturn(new ArrayList<>());
        given(parametersMock.getInclude()).willReturn(new ArrayList<>());

        // when
        final CollectionWithPagingInfo<Tag> actualTags = objectUnderTest.getTags(STORE_REF_WORKSPACE_SPACESSTORE, parametersMock);

        then(taggingServiceMock).should().getTags(eq(STORE_REF_WORKSPACE_SPACESSTORE), any(), any(), isNull(), eq(Set.of("expectedname*")));
        then(taggingServiceMock).shouldHaveNoMoreInteractions();
        assertThat(actualTags).isNotNull();
    }

    @Test
    public void testGetTags_withBothInAndEqualsClausesInSingleWhereQuery()
    {
        given(parametersMock.getPaging()).willReturn(pagingMock);
        given(parametersMock.getQuery()).willReturn(queryExtractor.getWhereClause("(tag=expectedName AND tag IN (expectedName1, expectedName2))"));
        given(parametersMock.getSorting()).willReturn(new ArrayList<>());

        // when
        final Throwable actualException = catchThrowable(() -> objectUnderTest.getTags(STORE_REF_WORKSPACE_SPACESSTORE, parametersMock));

        then(taggingServiceMock).shouldHaveNoInteractions();
        assertThat(actualException).isInstanceOf(InvalidQueryException.class);
    }

    @Test
    public void testGetTags_withOtherClauseInWhereQuery()
    {
        given(parametersMock.getPaging()).willReturn(pagingMock);
        given(parametersMock.getQuery()).willReturn(queryExtractor.getWhereClause("(tag BETWEEN ('expectedName', 'expectedName2'))"));
        given(parametersMock.getSorting()).willReturn(new ArrayList<>());

        // when
        final Throwable actualException = catchThrowable(() -> objectUnderTest.getTags(STORE_REF_WORKSPACE_SPACESSTORE, parametersMock));

        then(taggingServiceMock).shouldHaveNoInteractions();
        assertThat(actualException).isInstanceOf(InvalidQueryException.class);
    }

    @Test
    public void testGetTags_withNotEqualsClauseInWhereQuery()
    {
        given(parametersMock.getPaging()).willReturn(pagingMock);
        given(parametersMock.getQuery()).willReturn(queryExtractor.getWhereClause("(NOT tag=expectedName)"));
        given(parametersMock.getSorting()).willReturn(new ArrayList<>());

        // when
        final Throwable actualException = catchThrowable(() -> objectUnderTest.getTags(STORE_REF_WORKSPACE_SPACESSTORE, parametersMock));

        then(taggingServiceMock).shouldHaveNoInteractions();
        assertThat(actualException).isInstanceOf(InvalidQueryException.class);
    }

    @Test
    public void testDeleteTagById()
    {
        // when
        objectUnderTest.deleteTagById(STORE_REF_WORKSPACE_SPACESSTORE, TAG_ID);

        then(authorityServiceMock).should().hasAdminAuthority();
        then(authorityServiceMock).shouldHaveNoMoreInteractions();

        then(nodesMock).should().validateNode(STORE_REF_WORKSPACE_SPACESSTORE, TAG_ID);
        then(nodesMock).shouldHaveNoMoreInteractions();

        then(taggingServiceMock).should().getTagName(TAG_NODE_REF);
        then(taggingServiceMock).should().deleteTag(STORE_REF_WORKSPACE_SPACESSTORE, TAG_NAME);
        then(taggingServiceMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testDeleteTagById_asNonAdminUser()
    {
        given(authorityServiceMock.hasAdminAuthority()).willReturn(false);

        // when
        assertThrows(PermissionDeniedException.class, () -> objectUnderTest.deleteTagById(STORE_REF_WORKSPACE_SPACESSTORE, TAG_ID));

        then(authorityServiceMock).should().hasAdminAuthority();
        then(authorityServiceMock).shouldHaveNoMoreInteractions();

        then(nodesMock).shouldHaveNoInteractions();

        then(taggingServiceMock).shouldHaveNoInteractions();
    }

    @Test
    public void testDeleteTagById_nonExistentTag()
    {
        // when
        assertThrows(EntityNotFoundException.class, () -> objectUnderTest.deleteTagById(STORE_REF_WORKSPACE_SPACESSTORE, "dummy-id"));

        then(authorityServiceMock).should().hasAdminAuthority();
        then(authorityServiceMock).shouldHaveNoMoreInteractions();

        then(nodesMock).should().validateNode(STORE_REF_WORKSPACE_SPACESSTORE, "dummy-id");
        then(nodesMock).shouldHaveNoMoreInteractions();

        then(taggingServiceMock).shouldHaveNoInteractions();
    }

    @Test
    public void testCreateTags()
    {
        final List<String> tagNames = List.of("tag1", "99gat");
        final List<Tag> tagsToCreate = createTags(tagNames);
        given(taggingServiceMock.createTags(any(), any())).willAnswer(invocation -> createTagAndNodeRefPairs(invocation.getArgument(1)));

        // when
        final List<Tag> actualCreatedTags = objectUnderTest.createTags(tagsToCreate, parametersMock);

        then(authorityServiceMock).should().hasAdminAuthority();
        then(authorityServiceMock).shouldHaveNoMoreInteractions();
        then(taggingServiceMock).should().createTags(STORE_REF_WORKSPACE_SPACESSTORE, tagNames);
        then(taggingServiceMock).shouldHaveNoMoreInteractions();
        final List<Tag> expectedTags = createTagsWithNodeRefs(tagNames);
        assertThat(actualCreatedTags)
                .isNotNull().usingRecursiveComparison()
                .isEqualTo(expectedTags);
    }

    @Test
    public void testCreateTags_withoutPermission()
    {
        given(authorityServiceMock.hasAdminAuthority()).willReturn(false);

        // when
        final Throwable actualException = catchThrowable(() -> objectUnderTest.createTags(List.of(createTag(TAG_NAME)), parametersMock));

        then(authorityServiceMock).should().hasAdminAuthority();
        then(authorityServiceMock).shouldHaveNoMoreInteractions();
        then(taggingServiceMock).shouldHaveNoInteractions();
        assertThat(actualException)
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining(NO_PERMISSION_TO_MANAGE_A_TAG);
    }

    @Test
    public void testCreateTags_passingNullInsteadList()
    {
        // when
        final Throwable actualException = catchThrowable(() -> objectUnderTest.createTags(null, parametersMock));

        then(taggingServiceMock).shouldHaveNoInteractions();
        assertThat(actualException)
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining(NOT_A_VALID_TAG);
    }

    @Test
    public void testCreateTags_passingEmptyList()
    {
        // when
        final Throwable actualException = catchThrowable(() -> objectUnderTest.createTags(Collections.emptyList(), parametersMock));

        then(taggingServiceMock).shouldHaveNoInteractions();
        assertThat(actualException)
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining(NOT_A_VALID_TAG);
    }

    @Test
    public void testCreateTags_passingListOfNulls()
    {
        // when
        final Throwable actualException = catchThrowable(() -> objectUnderTest.createTags(Collections.singletonList(null), parametersMock));

        then(taggingServiceMock).shouldHaveNoInteractions();
        assertThat(actualException)
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining(NOT_A_VALID_TAG);
    }

    @Test
    public void testCreateTags_whileTagAlreadyExists()
    {
        given(taggingServiceMock.createTags(any(), any())).willThrow(new DuplicateChildNodeNameException(null, null, TAG_NAME, null));

        // when
        final Throwable actualException = catchThrowable(() -> objectUnderTest.createTags(List.of(createTag(TAG_NAME)), parametersMock));

        then(taggingServiceMock).should().createTags(STORE_REF_WORKSPACE_SPACESSTORE, List.of(TAG_NAME));
        then(taggingServiceMock).shouldHaveNoMoreInteractions();
        assertThat(actualException).isInstanceOf(DuplicateChildNodeNameException.class);
    }

    @Test
    public void testCreateTags_withRepeatedTagName()
    {
        final List<String> tagNames = List.of(TAG_NAME, TAG_NAME);
        final List<Tag> tagsToCreate = createTags(tagNames);
        given(taggingServiceMock.createTags(any(), any())).willAnswer(invocation -> createTagAndNodeRefPairs(invocation.getArgument(1)));

        // when
        final List<Tag> actualCreatedTags = objectUnderTest.createTags(tagsToCreate, parametersMock);

        then(taggingServiceMock).should().createTags(STORE_REF_WORKSPACE_SPACESSTORE, List.of(TAG_NAME));
        final List<Tag> expectedTags = List.of(createTagWithNodeRef(TAG_NAME));
        assertThat(actualCreatedTags)
                .isNotNull()
                .isEqualTo(expectedTags);
    }

    @Test
    public void testCreateTags_includingCount()
    {
        final List<String> tagNames = List.of("tag1", "99gat");
        final List<Tag> tagsToCreate = createTags(tagNames);
        given(taggingServiceMock.createTags(any(), any())).willAnswer(invocation -> createTagAndNodeRefPairs(invocation.getArgument(1)));
        given(parametersMock.getInclude()).willReturn(List.of("count"));

        // when
        final List<Tag> actualCreatedTags = objectUnderTest.createTags(tagsToCreate, parametersMock);

        final List<Tag> expectedTags = createTagsWithNodeRefs(tagNames).stream()
                .peek(tag -> tag.setCount(0L))
                .collect(toList());
        assertThat(actualCreatedTags)
                .isNotNull()
                .isEqualTo(expectedTags);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetTagByIdNotFoundValidation()
    {
        given(nodesMock.validateNode(STORE_REF_WORKSPACE_SPACESSTORE, TAG_ID)).willThrow(EntityNotFoundException.class);
        objectUnderTest.getTag(STORE_REF_WORKSPACE_SPACESSTORE, TAG_ID, null);
        then(nodeServiceMock).shouldHaveNoInteractions();
        then(nodesMock).should().validateNode(STORE_REF_WORKSPACE_SPACESSTORE, TAG_ID);
        then(nodesMock).shouldHaveNoMoreInteractions();
        then(taggingServiceMock).shouldHaveNoInteractions();
    }

    @Test
    public void testAddTags()
    {
        NodeRef tagNodeA = new NodeRef("tag://A/");
        NodeRef tagNodeB = new NodeRef("tag://B/");
        given(nodesMock.validateOrLookupNode(CONTENT_NODE_ID)).willReturn(CONTENT_NODE_REF);
        given(typeConstraintMock.matches(CONTENT_NODE_REF)).willReturn(true);
        List<Pair<String, NodeRef>> pairs = List.of(new Pair<>("taga", new NodeRef("tag://A/")), new Pair<>("tagb", new NodeRef("tag://B/")));
        List<String> tagNames = pairs.stream().map(Pair::getFirst).collect(toList());
        List<Tag> tags = tagNames.stream().map(name -> Tag.builder().tag(name).create()).collect(toList());
        given(taggingServiceMock.addTags(CONTENT_NODE_REF, tagNames)).willReturn(pairs);
        given(taggingServiceMock.findTaggedNodesAndCountByTagName(STORE_REF_WORKSPACE_SPACESSTORE)).willReturn(List.of(new Pair<>("taga", 4)));
        given(parametersMock.getInclude()).willReturn(List.of("count"));

        List<Tag> actual = objectUnderTest.addTags(CONTENT_NODE_ID, tags, parametersMock);

        final List<Tag> expected = List.of(Tag.builder().tag("taga").nodeRef(tagNodeA).count(5L).create(),
                Tag.builder().tag("tagb").nodeRef(tagNodeB).count(1L).create());
        assertEquals("Unexpected tags returned.", expected, actual);
    }

    @Test(expected = InvalidArgumentException.class)
    public void testAddTagsToInvalidNode()
    {
        given(nodesMock.validateOrLookupNode(CONTENT_NODE_ID)).willThrow(new InvalidArgumentException());
        List<Tag> tags = List.of(Tag.builder().tag("tag1").create());

        objectUnderTest.addTags(CONTENT_NODE_ID, tags, parametersMock);
    }

    @Test(expected = UnsupportedResourceOperationException.class)
    public void testAddTagsToWrongTypeOfNode()
    {
        given(nodesMock.validateOrLookupNode(CONTENT_NODE_ID)).willReturn(CONTENT_NODE_REF);
        given(typeConstraintMock.matches(CONTENT_NODE_REF)).willReturn(false);

        List<Tag> tags = List.of(Tag.builder().tag("tag1").create());

        objectUnderTest.addTags(CONTENT_NODE_ID, tags, parametersMock);
    }

    @Test
    public void testGetTagsForNode()
    {
        given(nodesMock.validateOrLookupNode(CONTENT_NODE_ID)).willReturn(CONTENT_NODE_REF);
        given(parametersMock.getPaging()).willReturn(pagingMock);
        List<Pair<NodeRef, String>> pairs = List.of(new Pair<>(new NodeRef("tag://A/"), "taga"), new Pair<>(new NodeRef("tag://B/"), "tagb"));
        given(taggingServiceMock.getTags(eq(CONTENT_NODE_REF), any(PagingRequest.class))).willReturn(pagingResultsMock);
        given(pagingResultsMock.getTotalResultCount()).willReturn(new Pair<>(null, null));
        given(pagingResultsMock.getPage()).willReturn(pairs);

        CollectionWithPagingInfo<Tag> actual = objectUnderTest.getTags(CONTENT_NODE_ID, parametersMock);

        List<Tag> tags = pairs.stream().map(pair -> Tag.builder().tag(pair.getSecond()).nodeRef(pair.getFirst()).create()).collect(toList());
        assertEquals(actual.getCollection(), tags);
    }

    @Test(expected = InvalidArgumentException.class)
    public void testGetTagsFromInvalidNode()
    {
        given(nodesMock.validateOrLookupNode(CONTENT_NODE_ID)).willThrow(new InvalidArgumentException());

        objectUnderTest.getTags(CONTENT_NODE_ID, parametersMock);
    }

    @Test
    public void testChangeTag()
    {
        Tag suppliedTag = Tag.builder().tag("new-name").create();
        given(taggingServiceMock.changeTag(STORE_REF_WORKSPACE_SPACESSTORE, TAG_NAME, "new-name")).willReturn(TAG_NODE_REF);

        Tag tag = objectUnderTest.changeTag(STORE_REF_WORKSPACE_SPACESSTORE, TAG_ID, suppliedTag, parametersMock);

        Tag expected = Tag.builder().nodeRef(TAG_NODE_REF).tag("new-name").create();
        assertEquals("Unexpected return value", expected, tag);
    }

    @Test
    public void testChangeTagAndGetCount()
    {
        Tag suppliedTag = Tag.builder().tag("new-name").create();
        given(taggingServiceMock.changeTag(STORE_REF_WORKSPACE_SPACESSTORE, TAG_NAME, "new-name")).willReturn(TAG_NODE_REF);
        given(parametersMock.getInclude()).willReturn(List.of(PARAM_INCLUDE_COUNT));
        given(taggingServiceMock.findCountByTagName(STORE_REF_WORKSPACE_SPACESSTORE, TAG_NAME)).willReturn(3L);

        Tag tag = objectUnderTest.changeTag(STORE_REF_WORKSPACE_SPACESSTORE, TAG_ID, suppliedTag, parametersMock);

        Tag expected = Tag.builder().nodeRef(TAG_NODE_REF).tag("new-name").count(3L).create();
        assertEquals("Unexpected return value", expected, tag);
    }

    @Test
    public void testGetTag()
    {
        Tag tag = objectUnderTest.getTag(STORE_REF_WORKSPACE_SPACESSTORE, TAG_ID, parametersMock);

        Tag expected = Tag.builder().nodeRef(TAG_NODE_REF).tag(TAG_NAME).create();
        assertEquals("Unexpected tag returned", expected, tag);
    }

    @Test
    public void testGetTagWithCount()
    {
        given(parametersMock.getInclude()).willReturn(List.of(PARAM_INCLUDE_COUNT));
        given(taggingServiceMock.findCountByTagName(STORE_REF_WORKSPACE_SPACESSTORE, TAG_NAME)).willReturn(0L);

        Tag tag = objectUnderTest.getTag(STORE_REF_WORKSPACE_SPACESSTORE, TAG_ID, parametersMock);

        Tag expected = Tag.builder().nodeRef(TAG_NODE_REF).tag(TAG_NAME).count(0L).create();
        assertEquals("Unexpected tag returned", expected, tag);
    }

    private static List<Pair<String, NodeRef>> createTagAndNodeRefPairs(final List<String> tagNames)
    {
        return tagNames.stream()
                .map(tagName -> createPair(tagName, new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, TAG_ID.concat("-").concat(tagName))))
                .collect(toList());
    }

    private static Pair<String, NodeRef> createPair(final String tagName, final NodeRef nodeRef)
    {
        return new Pair<>(tagName, nodeRef);
    }

    private static List<Tag> createTags(final List<String> tagNames)
    {
        return tagNames.stream().map(TagsImplTest::createTag).collect(toList());
    }

    private static List<Tag> createTagsWithNodeRefs(final List<String> tagNames)
    {
        return tagNames.stream().map(TagsImplTest::createTagWithNodeRef).collect(toList());
    }

    private static Tag createTag(final String tagName)
    {
        return Tag.builder()
                .tag(tagName)
                .create();
    }

    private static Tag createTagWithNodeRef(final String tagName)
    {
        return Tag.builder()
                .nodeRef(new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, TAG_ID.concat("-").concat(tagName)))
                .tag(tagName)
                .create();
    }
}
