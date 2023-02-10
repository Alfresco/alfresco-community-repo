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
package org.alfresco.rest.api.tags;

import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;

import org.alfresco.rest.api.Tags;
import org.alfresco.rest.api.model.Tag;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TagsEntityResourceTest
{
    private static final String TAG_ID = "tag-dummy-id";
    private static final String TAG_NAME = "tag-dummy-name";
    private static final NodeRef TAG_NODE_REF = new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, TAG_ID);

    @Mock
    private Parameters parametersMock;
    @Mock
    private Tags tagsMock;

    @InjectMocks
    private TagsEntityResource tagsEntityResource;

    @Test
    public void testCreate()
    {
        final Tag tag = Tag.builder().tag(TAG_NAME).create();
        final List<Tag> tags = List.of(tag);
        given(tagsMock.createTags(any(), any())).willCallRealMethod();
        given(tagsMock.createTags(any(), any(), any())).willReturn(List.of(Tag.builder().nodeRef(TAG_NODE_REF).tag(TAG_NAME).create()));

        //when
        final List<Tag> actualCreatedTags = tagsEntityResource.create(tags, parametersMock);

        then(tagsMock).should().createTags(STORE_REF_WORKSPACE_SPACESSTORE, tags, parametersMock);
        final List<Tag> expectedTags = List.of(Tag.builder().nodeRef(TAG_NODE_REF).tag(TAG_NAME).create());
        assertThat(actualCreatedTags)
            .isNotEmpty()
            .isEqualTo(expectedTags);
    }
}