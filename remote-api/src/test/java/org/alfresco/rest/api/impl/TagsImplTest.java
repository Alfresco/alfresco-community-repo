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

import static org.alfresco.rest.api.impl.TagsImpl.NOT_A_VALID_TAG;
import static org.alfresco.rest.api.impl.TagsImpl.NO_PERMISSION_TO_MANAGE_A_TAG;
import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Collections;
import java.util.List;
import java.util.Set;

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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
    }

    @Test
    public void testGetTags()
    {
        given(parametersMock.getPaging()).willReturn(pagingMock);
        given(taggingServiceMock.getTags(any(StoreRef.class), any(PagingRequest.class), any(), any())).willReturn(pagingResultsMock);
        given(pagingResultsMock.getTotalResultCount()).willReturn(new Pair<>(Integer.MAX_VALUE, 0));
        given(pagingResultsMock.getPage()).willReturn(List.of(new Pair<>(TAG_NODE_REF, TAG_NAME)));

        final CollectionWithPagingInfo<Tag> actualTags = objectUnderTest.getTags(STORE_REF_WORKSPACE_SPACESSTORE, parametersMock);

        then(taggingServiceMock).should().getTags(eq(STORE_REF_WORKSPACE_SPACESSTORE), any(PagingRequest.class), isNull(), isNull());
        then(taggingServiceMock).shouldHaveNoMoreInteractions();
        final List<Tag> expectedTags = createTagsWithNodeRefs(List.of(TAG_NAME));
        assertEquals(expectedTags, actualTags.getCollection());
    }

    @Test
    public void testGetTags_verifyIfCountIsZero()
    {
        given(parametersMock.getPaging()).willReturn(pagingMock);
        given(taggingServiceMock.getTags(any(StoreRef.class), any(PagingRequest.class), any(), any())).willReturn(pagingResultsMock);
        given(pagingResultsMock.getTotalResultCount()).willReturn(new Pair<>(Integer.MAX_VALUE, 0));
        given(pagingResultsMock.getPage()).willReturn(List.of(new Pair<>(TAG_NODE_REF, TAG_NAME)));
        given(parametersMock.getInclude()).willReturn(List.of("count"));

        final CollectionWithPagingInfo<Tag> actualTags = objectUnderTest.getTags(STORE_REF_WORKSPACE_SPACESSTORE, parametersMock);

        then(taggingServiceMock).should().findTaggedNodesAndCountByTagName(STORE_REF_WORKSPACE_SPACESSTORE);
        final List<Tag> expectedTags = createTagsWithNodeRefs(List.of(TAG_NAME)).stream()
            .peek(tag -> tag.setCount(0))
            .collect(toList());
        assertEquals(expectedTags, actualTags.getCollection());
    }

    /** Check that we can get counts for two tags - one in use and one not applied to any nodes. */
    @Test
    public void testGetTags_verifyCountPopulatedCorrectly()
    {
        NodeRef tagNodeA = new NodeRef("tag://A/");
        NodeRef tagNodeB = new NodeRef("tag://B/");
        List<Pair<NodeRef, String>> tagPairs = List.of(new Pair<>(tagNodeA, "tagA"), new Pair<>(tagNodeB, "tagB"));

        given(parametersMock.getPaging()).willReturn(pagingMock);
        given(taggingServiceMock.getTags(any(StoreRef.class), any(PagingRequest.class), any(), any())).willReturn(pagingResultsMock);
        given(pagingResultsMock.getTotalResultCount()).willReturn(new Pair<>(Integer.MAX_VALUE, 0));
        given(pagingResultsMock.getPage()).willReturn(tagPairs);
        given(parametersMock.getInclude()).willReturn(List.of("count"));
        // Only tagA is included in the returned list since tagB is not in use.
        given(taggingServiceMock.findTaggedNodesAndCountByTagName(STORE_REF_WORKSPACE_SPACESSTORE)).willReturn(List.of(new Pair<>("tagA", 5)));

        final CollectionWithPagingInfo<Tag> actualTags = objectUnderTest.getTags(STORE_REF_WORKSPACE_SPACESSTORE, parametersMock);

        then(taggingServiceMock).should().findTaggedNodesAndCountByTagName(STORE_REF_WORKSPACE_SPACESSTORE);
        final List<Tag> expectedTags = List.of(Tag.builder().tag("tagA").nodeRef(tagNodeA).count(5).create(),
                                               Tag.builder().tag("tagB").nodeRef(tagNodeB).count(0).create());
        assertEquals(expectedTags, actualTags.getCollection());
    }

    @Test
    public void testGetTags_withEqualsClauseWhereQuery()
    {
        given(parametersMock.getPaging()).willReturn(pagingMock);
        given(parametersMock.getQuery()).willReturn(queryExtractor.getWhereClause("(tag=expectedName)"));
        given(taggingServiceMock.getTags(any(StoreRef.class), any(PagingRequest.class), any(), any())).willReturn(pagingResultsMock);
        given(pagingResultsMock.getTotalResultCount()).willReturn(new Pair<>(Integer.MAX_VALUE, 0));

        //when
        final CollectionWithPagingInfo<Tag> actualTags = objectUnderTest.getTags(STORE_REF_WORKSPACE_SPACESSTORE, parametersMock);

        then(taggingServiceMock).should().getTags(eq(STORE_REF_WORKSPACE_SPACESSTORE), any(PagingRequest.class), eq(Set.of("expectedname")), isNull());
        then(taggingServiceMock).shouldHaveNoMoreInteractions();
        assertThat(actualTags).isNotNull();
    }

    @Test
    public void testGetTags_withInClauseWhereQuery()
    {
        given(parametersMock.getPaging()).willReturn(pagingMock);
        given(parametersMock.getQuery()).willReturn(queryExtractor.getWhereClause("(tag IN (expectedName1, expectedName2))"));
        given(taggingServiceMock.getTags(any(StoreRef.class), any(PagingRequest.class), any(), any())).willReturn(pagingResultsMock);
        given(pagingResultsMock.getTotalResultCount()).willReturn(new Pair<>(Integer.MAX_VALUE, 0));

        //when
        final CollectionWithPagingInfo<Tag> actualTags = objectUnderTest.getTags(STORE_REF_WORKSPACE_SPACESSTORE, parametersMock);

        then(taggingServiceMock).should().getTags(eq(STORE_REF_WORKSPACE_SPACESSTORE), any(PagingRequest.class), eq(Set.of("expectedname1", "expectedname2")), isNull());
        then(taggingServiceMock).shouldHaveNoMoreInteractions();
        assertThat(actualTags).isNotNull();
    }

    @Test
    public void testGetTags_withMatchesClauseWhereQuery()
    {
        given(parametersMock.getPaging()).willReturn(pagingMock);
        given(parametersMock.getQuery()).willReturn(queryExtractor.getWhereClause("(tag MATCHES ('expectedName*'))"));
        given(taggingServiceMock.getTags(any(StoreRef.class), any(PagingRequest.class), any(), any())).willReturn(pagingResultsMock);
        given(pagingResultsMock.getTotalResultCount()).willReturn(new Pair<>(Integer.MAX_VALUE, 0));

        //when
        final CollectionWithPagingInfo<Tag> actualTags = objectUnderTest.getTags(STORE_REF_WORKSPACE_SPACESSTORE, parametersMock);

        then(taggingServiceMock).should().getTags(eq(STORE_REF_WORKSPACE_SPACESSTORE), any(PagingRequest.class), isNull(), eq(Set.of("expectedname*")));
        then(taggingServiceMock).shouldHaveNoMoreInteractions();
        assertThat(actualTags).isNotNull();
    }

    @Test
    public void testGetTags_withBothInAndEqualsClausesInSingleWhereQuery()
    {
        given(parametersMock.getPaging()).willReturn(pagingMock);
        given(parametersMock.getQuery()).willReturn(queryExtractor.getWhereClause("(tag=expectedName AND tag IN (expectedName1, expectedName2))"));

        //when
        final Throwable actualException = catchThrowable(() -> objectUnderTest.getTags(STORE_REF_WORKSPACE_SPACESSTORE, parametersMock));

        then(taggingServiceMock).shouldHaveNoInteractions();
        assertThat(actualException).isInstanceOf(InvalidQueryException.class);
    }

    @Test
    public void testGetTags_withOtherClauseInWhereQuery()
    {
        given(parametersMock.getPaging()).willReturn(pagingMock);
        given(parametersMock.getQuery()).willReturn(queryExtractor.getWhereClause("(tag BETWEEN ('expectedName', 'expectedName2'))"));

        //when
        final Throwable actualException = catchThrowable(() -> objectUnderTest.getTags(STORE_REF_WORKSPACE_SPACESSTORE, parametersMock));

        then(taggingServiceMock).shouldHaveNoInteractions();
        assertThat(actualException).isInstanceOf(InvalidQueryException.class);
    }

    @Test
    public void testGetTags_withNotEqualsClauseInWhereQuery()
    {
        given(parametersMock.getPaging()).willReturn(pagingMock);
        given(parametersMock.getQuery()).willReturn(queryExtractor.getWhereClause("(NOT tag=expectedName)"));

        //when
        final Throwable actualException = catchThrowable(() -> objectUnderTest.getTags(STORE_REF_WORKSPACE_SPACESSTORE, parametersMock));

        then(taggingServiceMock).shouldHaveNoInteractions();
        assertThat(actualException).isInstanceOf(InvalidQueryException.class);
    }

    @Test
    public void testDeleteTagById()
    {
        //when
        given(primaryParentMock.getParentRef()).willReturn(TAG_PARENT_NODE_REF);
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

        //when
        assertThrows(PermissionDeniedException.class, () -> objectUnderTest.deleteTagById(STORE_REF_WORKSPACE_SPACESSTORE, TAG_ID));

        then(authorityServiceMock).should().hasAdminAuthority();
        then(authorityServiceMock).shouldHaveNoMoreInteractions();

        then(nodesMock).shouldHaveNoInteractions();

        then(taggingServiceMock).shouldHaveNoInteractions();
    }

    @Test
    public void testDeleteTagById_nonExistentTag()
    {
        //when
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

        //when
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

        //when
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
        //when
        final Throwable actualException = catchThrowable(() -> objectUnderTest.createTags(null, parametersMock));

        then(taggingServiceMock).shouldHaveNoInteractions();
        assertThat(actualException)
            .isInstanceOf(InvalidArgumentException.class)
            .hasMessageContaining(NOT_A_VALID_TAG);
    }

    @Test
    public void testCreateTags_passingEmptyList()
    {
        //when
        final Throwable actualException = catchThrowable(() -> objectUnderTest.createTags(Collections.emptyList(), parametersMock));

        then(taggingServiceMock).shouldHaveNoInteractions();
        assertThat(actualException)
            .isInstanceOf(InvalidArgumentException.class)
            .hasMessageContaining(NOT_A_VALID_TAG);
    }

    @Test
    public void testCreateTags_passingListOfNulls()
    {
        //when
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

        //when
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

        //when
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

        //when
        final List<Tag> actualCreatedTags = objectUnderTest.createTags(tagsToCreate, parametersMock);

        final List<Tag> expectedTags = createTagsWithNodeRefs(tagNames).stream()
            .peek(tag -> tag.setCount(0))
            .collect(toList());
        assertThat(actualCreatedTags)
            .isNotNull()
            .isEqualTo(expectedTags);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetTagByIdNotFoundValidation()
    {
        given(primaryParentMock.getParentRef()).willReturn(TAG_NODE_REF);
        objectUnderTest.getTag(STORE_REF_WORKSPACE_SPACESSTORE,TAG_ID);
        then(nodeServiceMock).shouldHaveNoInteractions();
        then(nodesMock).should().validateNode(STORE_REF_WORKSPACE_SPACESSTORE, TAG_ID);
        then(nodesMock).shouldHaveNoMoreInteractions();
        then(taggingServiceMock).shouldHaveNoInteractions();
    }

    @Test
    public void testAddTags()
    {
        given(nodesMock.validateOrLookupNode(CONTENT_NODE_ID)).willReturn(CONTENT_NODE_REF);
        given(typeConstraintMock.matches(CONTENT_NODE_REF)).willReturn(true);
        List<Pair<String, NodeRef>> pairs = List.of(new Pair<>("tagA", new NodeRef("tag://A/")), new Pair<>("tagB", new NodeRef("tag://B/")));
        List<String> tagNames = pairs.stream().map(Pair::getFirst).collect(toList());
        List<Tag> tags = tagNames.stream().map(name -> Tag.builder().tag(name).create()).collect(toList());
        given(taggingServiceMock.addTags(CONTENT_NODE_REF, tagNames)).willReturn(pairs);

        List<Tag> actual = objectUnderTest.addTags(CONTENT_NODE_ID, tags);

        List<Tag> expected = pairs.stream().map(pair -> new Tag(pair.getSecond(), pair.getFirst())).collect(toList());
        assertEquals("Unexpected tags returned.", expected, actual);
    }

    @Test(expected = InvalidArgumentException.class)
    public void testAddTagsToInvalidNode()
    {
        given(nodesMock.validateOrLookupNode(CONTENT_NODE_ID)).willThrow(new InvalidArgumentException());
        List<Tag> tags = List.of(Tag.builder().tag("tag1").create());

        objectUnderTest.addTags(CONTENT_NODE_ID, tags);
    }

    @Test(expected = UnsupportedResourceOperationException.class)
    public void testAddTagsToWrongTypeOfNode()
    {
        given(nodesMock.validateOrLookupNode(CONTENT_NODE_ID)).willReturn(CONTENT_NODE_REF);
        given(typeConstraintMock.matches(CONTENT_NODE_REF)).willReturn(false);

        List<Tag> tags = List.of(Tag.builder().tag("tag1").create());

        objectUnderTest.addTags(CONTENT_NODE_ID, tags);
    }

    @Test
    public void testGetTagsForNode()
    {
        given(nodesMock.validateOrLookupNode(CONTENT_NODE_ID)).willReturn(CONTENT_NODE_REF);
        given(parametersMock.getPaging()).willReturn(pagingMock);
        List<Pair<NodeRef, String>> pairs = List.of(new Pair<>(new NodeRef("tag://A/"), "tagA"), new Pair<>(new NodeRef("tag://B/"), "tagB"));
        given(taggingServiceMock.getTags(eq(CONTENT_NODE_REF), any(PagingRequest.class))).willReturn(pagingResultsMock);
        given(pagingResultsMock.getTotalResultCount()).willReturn(new Pair<>(null, null));
        given(pagingResultsMock.getPage()).willReturn(pairs);

        CollectionWithPagingInfo<Tag> actual = objectUnderTest.getTags(CONTENT_NODE_ID, parametersMock);

        List<Tag> tags = pairs.stream().map(pair -> Tag.builder().tag(pair.getSecond()).nodeRef(pair.getFirst()).create()).collect(toList());
        assertEquals(actual.getCollection(), tags);
    }

    @Test (expected = InvalidArgumentException.class)
    public void testGetTagsFromInvalidNode()
    {
        given(nodesMock.validateOrLookupNode(CONTENT_NODE_ID)).willThrow(new InvalidArgumentException());

        objectUnderTest.getTags(CONTENT_NODE_ID, parametersMock);
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
