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

import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.CategoryService;
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

    @Mock
    private CategoryService categoryServiceMock;
    @Mock
    private PolicyComponent policyComponentMock;

    @InjectMocks
    private TaggingServiceImpl taggingService;

    @Before
    public void setUp() throws Exception
    {
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

        then(categoryServiceMock).should().getRootCategories(STORE_REF_WORKSPACE_SPACESSTORE, ContentModel.ASPECT_TAGGABLE, TAG_NAME, false);
        then(categoryServiceMock).should().getRootCategories(STORE_REF_WORKSPACE_SPACESSTORE, ContentModel.ASPECT_TAGGABLE, TAG_NAME, true);
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

        then(categoryServiceMock).should().getRootCategories(STORE_REF_WORKSPACE_SPACESSTORE, ContentModel.ASPECT_TAGGABLE, TAG_NAME, false);
        then(categoryServiceMock).shouldHaveNoMoreInteractions();
        assertThat(actualException).isInstanceOf(DuplicateChildNodeNameException.class);
    }
}