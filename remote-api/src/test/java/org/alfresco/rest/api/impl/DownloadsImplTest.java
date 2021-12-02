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
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.api.impl;

import static org.alfresco.model.ContentModel.TYPE_CONTENT;
import static org.alfresco.model.ContentModel.TYPE_FOLDER;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.alfresco.repo.content.ObjectStorageProps;
import org.alfresco.repo.module.ModuleDetailsImpl;
import org.alfresco.rest.api.Nodes;
import org.alfresco.service.cmr.download.DownloadService;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.service.cmr.repository.ArchivedIOException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DownloadsImplTest 
{
    
    @InjectMocks
    DownloadsImpl downloads = new DownloadsImpl();

    @Mock
    private DownloadService downloadService;
    @Mock
    private ModuleService moduleService;
    @Mock
    private NodeService nodeService;
    @Mock
    private ContentService contentService;
    @Mock
    private Nodes nodes;
    @Mock
    private PermissionService permissionService;


    @Test
    public void testCheckArchiveStatus()
    {
        //#region setup test objects and method stubbing
        // NodeRefs
        NodeRef contentNode1 = new NodeRef("://Content:/Node1");
        NodeRef contentNode2 = new NodeRef("://Content:/Node2");
        NodeRef contentNode3 = new NodeRef("://Content:/Node3");
        NodeRef contentNode4 = new NodeRef("://Content:/Node4");
        NodeRef contentNode5 = new NodeRef("://Content:/Node5");
        NodeRef contentNode6 = new NodeRef("://Content:/Node6");
        NodeRef folderParent1 = new NodeRef("://Folder:/Parent1");
        NodeRef folderParent2 = new NodeRef("://Folder:/Parent2");
        NodeRef folder1 = new NodeRef("://Folder:/1");
        NodeRef folder2 = new NodeRef("://Folder:/2");
        NodeRef[] nodeRefsToTest = { contentNode1, contentNode2, folderParent1, folder1 };

        // folderParent1
        ChildAssociationRef folderParent1ChildContent3 = new ChildAssociationRef(TYPE_FOLDER, folderParent1, TYPE_CONTENT, contentNode3);
        ChildAssociationRef folderParent1ChildContent4 = new ChildAssociationRef(TYPE_FOLDER, folderParent1, TYPE_CONTENT, contentNode4);
        ChildAssociationRef folderParent1ChildContent5 = new ChildAssociationRef(TYPE_FOLDER, folderParent1, TYPE_CONTENT, contentNode5);
        ChildAssociationRef folderParent1ChildFolder2 = new ChildAssociationRef(TYPE_FOLDER, folderParent1, TYPE_FOLDER, folder2);
        ChildAssociationRef folderParent1ChildParentFolder2 = new ChildAssociationRef(TYPE_FOLDER, folderParent1, TYPE_FOLDER, folderParent2);
        List<ChildAssociationRef> folderParent1ChildAssocs = List.of(folderParent1ChildContent3,
                folderParent1ChildContent4, folderParent1ChildContent5, folderParent1ChildFolder2,
                folderParent1ChildParentFolder2);
        NodeRef[] childNodeRefsExpectedfolderParent1 = { contentNode3, contentNode4, contentNode5, folder2, folderParent2 };
        // folderParent2
        ChildAssociationRef folderParent2ChildFolder1 = new ChildAssociationRef(TYPE_FOLDER, folderParent2, TYPE_FOLDER, folder1);
        List<ChildAssociationRef> folderParent2ChildAssocs = List.of(folderParent2ChildFolder1);
        NodeRef[] childNodeRefsExpectedFolderParent2 = { folder1 };
        // folder1
        ChildAssociationRef folder1ChildContent1 = new ChildAssociationRef(TYPE_FOLDER, folder1, TYPE_CONTENT, contentNode1);
        ChildAssociationRef folder1ChildContent5 = new ChildAssociationRef(TYPE_FOLDER, folder1, TYPE_CONTENT, contentNode5);
        ChildAssociationRef folder1ChildContent6 = new ChildAssociationRef(TYPE_FOLDER, folder1, TYPE_CONTENT, contentNode6);
        List<ChildAssociationRef> folder1ChildAssocs = List.of(folder1ChildContent1, folder1ChildContent5, folder1ChildContent6);
        NodeRef[] childNodeRefsExpectedFolder1 = { contentNode1, contentNode5, contentNode6 };
        // folder2 empty
        List<ChildAssociationRef> folder2ChildAssocs = List.of();
        NodeRef[] childNodeRefsExpectedFolder2 = {};

        Map<String, String> archivedProps = Map.of(ObjectStorageProps.X_ALF_ARCHIVED.getValue(), "true");
        Map<String, String> nonArchivedProps = Map.of(ObjectStorageProps.X_ALF_ARCHIVED.getValue(), "false");

        when(nodeService.getType(any())).thenReturn(TYPE_CONTENT);
        when(nodeService.getType(folderParent1)).thenReturn(TYPE_FOLDER);
        when(nodeService.getType(folderParent2)).thenReturn(TYPE_FOLDER);
        when(nodeService.getType(folder1)).thenReturn(TYPE_FOLDER);
        when(nodeService.getType(folder2)).thenReturn(TYPE_FOLDER);

        when(nodeService.getChildAssocs(folderParent1)).thenReturn(folderParent1ChildAssocs);
        when(nodeService.getChildAssocs(folderParent2)).thenReturn(folderParent2ChildAssocs);
        when(nodeService.getChildAssocs(folder1)).thenReturn(folder1ChildAssocs);
        when(nodeService.getChildAssocs(folder2)).thenReturn(folder2ChildAssocs);
    
        assertChildNodeRefMocks(folderParent1, childNodeRefsExpectedfolderParent1);
        assertChildNodeRefMocks(folderParent2, childNodeRefsExpectedFolderParent2);
        assertChildNodeRefMocks(folder1, childNodeRefsExpectedFolder1);
        assertChildNodeRefMocks(folder2, childNodeRefsExpectedFolder2);

        when(contentService.getStorageProperties(any(NodeRef.class), any(QName.class))).thenReturn(nonArchivedProps);

        ModuleDetails mockDetails = new ModuleDetailsImpl("id", null, "title", "description"); // doesn't need to do anything, just exist
        when(moduleService.getModule("org_alfresco_integrations_S3Connector")).thenReturn(mockDetails);
        //#endregion setup test objects and method stubbing

        // No archived nodes, each content node should only be checked once
        downloads.checkArchiveStatus(nodeRefsToTest, -1);
        verify(contentService, times(1)).getStorageProperties(contentNode1, TYPE_CONTENT);
        verify(contentService, times(1)).getStorageProperties(contentNode2, TYPE_CONTENT);
        verify(contentService, times(1)).getStorageProperties(contentNode3, TYPE_CONTENT);
        verify(contentService, times(1)).getStorageProperties(contentNode4, TYPE_CONTENT);
        verify(contentService, times(1)).getStorageProperties(contentNode5, TYPE_CONTENT);
        verify(contentService, times(1)).getStorageProperties(contentNode6, TYPE_CONTENT);
        
        reset(contentService);

        // first item archived in array
        when(contentService.getStorageProperties(contentNode1, TYPE_CONTENT)).thenReturn(archivedProps);
        assertThrows(ArchivedIOException.class, () -> downloads.checkArchiveStatus(nodeRefsToTest, -1));
        verify(contentService, times(1)).getStorageProperties(contentNode1, TYPE_CONTENT);
        verifyNoMoreInteractions(contentService);

        reset(contentService);

        // fail node3 (within another folder)
        when(contentService.getStorageProperties(any(NodeRef.class), any(QName.class))).thenReturn(nonArchivedProps);
        when(contentService.getStorageProperties(contentNode3, TYPE_CONTENT)).thenReturn(archivedProps);
        assertThrows(ArchivedIOException.class, () -> downloads.checkArchiveStatus(nodeRefsToTest, -1));
        verify(contentService, times(1)).getStorageProperties(contentNode1, TYPE_CONTENT);
        verify(contentService, times(1)).getStorageProperties(contentNode2, TYPE_CONTENT);
        verify(contentService, times(1)).getStorageProperties(contentNode3, TYPE_CONTENT);
        verifyNoMoreInteractions(contentService);
    } 

    private void assertChildNodeRefMocks(NodeRef nodeRef, NodeRef[] expecteds)
    {
        NodeRef[] actuals = nodeService.getChildAssocs(nodeRef).stream()
                                                            .map(childAssoc -> childAssoc.getChildRef())
                                                            .toArray(NodeRef[]::new);
        assertArrayEquals(expecteds, actuals);
    }
}
