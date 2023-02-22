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

import static org.alfresco.rest.api.impl.TagsImpl.NOT_A_VALID_TAG;
import static org.alfresco.rest.api.impl.TagsImpl.NO_PERMISSION_TO_MANAGE_A_TAG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Tag;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.util.Pair;
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
    private static final String TAG_NAME = "tag-dummy-name";
    private static final NodeRef TAG_NODE_REF = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, TAG_ID);

    @Mock
    private Nodes nodesMock;
    @Mock
    private AuthorityService authorityServiceMock;
    @Mock
    private TaggingService taggingServiceMock;
    @Mock
    private Parameters parametersMock;

    @InjectMocks
    private TagsImpl objectUnderTest;

    @Before
    public void setup()
    {
        given(authorityServiceMock.hasAdminAuthority()).willReturn(true);
        given(nodesMock.validateNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, TAG_ID)).willReturn(TAG_NODE_REF);
        given(taggingServiceMock.getTagName(TAG_NODE_REF)).willReturn(TAG_NAME);
    }
    @Test
    public void testGetTags() {
        final List<String> tagNames = List.of("testTag","tag11");
        final List<Tag> tagsToCreate = createTags(tagNames);
        given(taggingServiceMock.createTags(any(), any())).willAnswer(invocation -> createTagAndNodeRefPairs(invocation.getArgument(1)));
        given(parametersMock.getInclude()).willReturn(List.of("count"));
        final List<Tag> actualCreatedTags = objectUnderTest.createTags(tagsToCreate, parametersMock);
        final List<Tag> expectedTags = createTagsWithNodeRefs(tagNames).stream()
            .peek(tag -> tag.setCount(0))
            .collect(Collectors.toList());
        assertEquals(expectedTags, actualCreatedTags);
    }

    @Test
    public void testDeleteTagById()
    {
        //when
        objectUnderTest.deleteTagById(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, TAG_ID);

        then(authorityServiceMock).should().hasAdminAuthority();
        then(authorityServiceMock).shouldHaveNoMoreInteractions();

        then(nodesMock).should().validateNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, TAG_ID);
        then(nodesMock).shouldHaveNoMoreInteractions();

        then(taggingServiceMock).should().getTagName(TAG_NODE_REF);
        then(taggingServiceMock).should().deleteTag(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, TAG_NAME);
        then(taggingServiceMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testDeleteTagById_asNonAdminUser()
    {
        given(authorityServiceMock.hasAdminAuthority()).willReturn(false);

        //when
        assertThrows(PermissionDeniedException.class, () -> objectUnderTest.deleteTagById(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, TAG_ID));

        then(authorityServiceMock).should().hasAdminAuthority();
        then(authorityServiceMock).shouldHaveNoMoreInteractions();

        then(nodesMock).shouldHaveNoInteractions();

        then(taggingServiceMock).shouldHaveNoInteractions();
    }

    @Test
    public void testDeleteTagById_nonExistentTag()
    {
        //when
        assertThrows(EntityNotFoundException.class, () -> objectUnderTest.deleteTagById(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "dummy-id"));

        then(authorityServiceMock).should().hasAdminAuthority();
        then(authorityServiceMock).shouldHaveNoMoreInteractions();

        then(nodesMock).should().validateNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "dummy-id");
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
        then(taggingServiceMock).should().createTags(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, tagNames);
        then(taggingServiceMock).shouldHaveNoMoreInteractions();
        final List<Tag> expectedTags = createTagsWithNodeRefs(tagNames);
        assertThat(actualCreatedTags)
            .isNotNull()
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

        then(taggingServiceMock).should().createTags(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, List.of(TAG_NAME));
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

        then(taggingServiceMock).should().createTags(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, List.of(TAG_NAME));
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
            .collect(Collectors.toList());
        assertThat(actualCreatedTags)
            .isNotNull()
            .isEqualTo(expectedTags);
    }

    private static List<Pair<String, NodeRef>> createTagAndNodeRefPairs(final List<String> tagNames)
    {
        return tagNames.stream()
            .map(tagName -> createPair(tagName, new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, TAG_ID.concat("-").concat(tagName))))
            .collect(Collectors.toList());
    }

    private static Pair<String, NodeRef> createPair(final String tagName, final NodeRef nodeRef)
    {
        return new Pair<>(tagName, nodeRef);
    }

    private static List<Tag> createTags(final List<String> tagNames)
    {
        return tagNames.stream().map(TagsImplTest::createTag).collect(Collectors.toList());
    }

    private static List<Tag> createTagsWithNodeRefs(final List<String> tagNames)
    {
        return tagNames.stream().map(TagsImplTest::createTagWithNodeRef).collect(Collectors.toList());
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
            .nodeRef(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, TAG_ID.concat("-").concat(tagName)))
            .tag(tagName)
            .create();
    }
}
