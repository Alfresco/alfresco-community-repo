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

import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@RunWith(MockitoJUnitRunner.class)
public class TagsImplTest
{
    private static final String TAG_ID = "tag-node-id";
    private static final String TAG_NAME = "tag-dummy-name";
    private static final NodeRef TAG_NODE_REF = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,TAG_ID);

    @Mock
    private Nodes nodesMock;
    @Mock
    private AuthorityService authorityServiceMock;
    @Mock
    private TaggingService taggingServiceMock;

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
}
