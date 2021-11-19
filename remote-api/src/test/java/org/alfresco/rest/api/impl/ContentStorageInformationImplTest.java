/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

import org.alfresco.rest.api.model.ContentStorageInfo;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class ContentStorageInformationImplTest
{
    @Mock
    private ContentService contentService;
    @Mock
    private NamespaceService namespaceService;

    @InjectMocks
    private ContentStorageInformationImpl objectUnderTest;

    @Test
    public void shouldReturnStorageInfoResponseWithNonEmptyStorageProps()
    {

        final String nodeId = "dummy-node-id";
        final NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);
        final String contentPropName = "cm:content";

        final Map<String, String> storageProps = Map.of("x-amz-storage-class", "INTELLIGENT_TIERING", "x-alf-archived", "false");
        Mockito.when(contentService.getStorageProperties(Mockito.eq(nodeRef), Mockito.any())).thenReturn(storageProps);
        Mockito.when(namespaceService.getNamespaceURI(NamespaceService.CONTENT_MODEL_PREFIX))
                .thenReturn(NamespaceService.CONTENT_MODEL_1_0_URI);
        Mockito.when(namespaceService.getPrefixes(NamespaceService.CONTENT_MODEL_1_0_URI))
                .thenReturn(List.of(NamespaceService.CONTENT_MODEL_PREFIX));

        final ContentStorageInfo storageInfo = objectUnderTest.getStorageInfo(nodeId, contentPropName, null);

        Assert.assertEquals(storageProps, storageInfo.getStorageProperties());
        Assert.assertEquals(storageInfo.getId(), contentPropName);
    }

    @Test
    public void shouldReturnStorageInfoResponseWithEmptyStorageProps()
    {
        final String nodeId = "dummy-node-id";
        final NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);
        final String contentPropName = "cm:content";

        Mockito.when(contentService.getStorageProperties(Mockito.eq(nodeRef), Mockito.any())).thenCallRealMethod();
        Mockito.when(namespaceService.getNamespaceURI(NamespaceService.CONTENT_MODEL_PREFIX))
                .thenReturn(NamespaceService.CONTENT_MODEL_1_0_URI);
        Mockito.when(namespaceService.getPrefixes(NamespaceService.CONTENT_MODEL_1_0_URI))
                .thenReturn(List.of(NamespaceService.CONTENT_MODEL_PREFIX));

        final ContentStorageInfo storageInfo = objectUnderTest.getStorageInfo(nodeId, contentPropName, null);

        Assert.assertEquals(Collections.emptyMap(), storageInfo.getStorageProperties());
        Assert.assertEquals(storageInfo.getId(), contentPropName);
    }
}
