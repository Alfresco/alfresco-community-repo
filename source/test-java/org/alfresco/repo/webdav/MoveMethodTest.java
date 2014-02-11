/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.webdav;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests for the {@link MoveMethod} class.
 * 
 * @author Matt Ward
 */
@RunWith(MockitoJUnitRunner.class)
public class MoveMethodTest
{
    private MoveMethod moveMethod;
    private @Mock WebDAVHelper davHelper;
    private MockHttpServletRequest req;
    private MockHttpServletResponse resp;
    private NodeRef rootNode;
    private @Mock FileFolderService fileFolderService;
    private @Mock WebDAVLockService davLockService;
    private String destPath;
    private String sourcePath;
    private FileInfo sourceFileInfo;
    private NodeRef sourceParentNodeRef;
    private NodeRef destParentNodeRef;
    private NodeRef sourceNodeRef;
    
    @Before
    public void setUp() throws Exception
    {
        req = new MockHttpServletRequest();
        resp = new MockHttpServletResponse();
        rootNode = new NodeRef("workspace://SpacesStore/node1");
        moveMethod = new MoveMethod()
        {
            @Override
            protected LockInfo checkNode(FileInfo fileInfo, boolean ignoreShared, boolean lockMethod)
                        throws WebDAVServerException
            {
                return new LockInfoImpl();
            }

            @Override
            protected LockInfo checkNode(FileInfo fileInfo) throws WebDAVServerException
            {
                return new LockInfoImpl();
            }
        };
        moveMethod.setDetails(req, resp, davHelper, rootNode);
        
        sourceFileInfo = Mockito.mock(FileInfo.class);
        when(sourceFileInfo.isFolder()).thenReturn(true);
        
        destPath = "/path/to/dest.doc";
        moveMethod.m_strDestinationPath = destPath;
        
        sourcePath = "/path/to/source.doc";
        moveMethod.m_strPath = sourcePath;
        
        when(davHelper.getFileFolderService()).thenReturn(fileFolderService);
        
        List<String> sourcePathSplit = Arrays.asList("path", "to", "source.doc");
        when(davHelper.splitAllPaths(sourcePath)).thenReturn(sourcePathSplit);
        
        
        List<String> destPathSplit = Arrays.asList("path", "to", "dest.doc");
        when(davHelper.splitAllPaths(destPath)).thenReturn(destPathSplit);
        
        
        when(fileFolderService.resolveNamePath(rootNode, sourcePathSplit)).thenReturn(sourceFileInfo);
        
        FileInfo destFileInfo = Mockito.mock(FileInfo.class);
        when(fileFolderService.resolveNamePath(rootNode, destPathSplit)).thenReturn(destFileInfo);
        
        sourceParentNodeRef = new NodeRef("workspace://SpacesStore/parent");
        destParentNodeRef = new NodeRef("workspace://SpacesStore/parent");
        
        
        sourceNodeRef = new NodeRef("workspace://SpacesStore/sourcefile");
        
        when(davHelper.getLockService()).thenReturn(davLockService);
    }

    
    @Test
    public void canRenameFolders() throws Exception
    {
        moveMethod.moveOrCopy(sourceNodeRef, sourceParentNodeRef, destParentNodeRef, "dest.doc");
        
        verify(fileFolderService).rename(sourceNodeRef, "dest.doc");
        verify(davLockService).unlock(sourceNodeRef);
        verify(fileFolderService, never()).create(destParentNodeRef, "dest.doc", ContentModel.TYPE_CONTENT);
    }
    
    
    @Test
    public void canRenameFoldersWhenNewNameMatchesShufflePattern() throws Exception
    {
        when(davHelper.isRenameShuffle(destPath)).thenReturn(true);
        when(davHelper.isRenameShuffle(sourcePath)).thenReturn(false);
        
        // Test: Perform the rename
        moveMethod.moveOrCopy(sourceNodeRef, sourceParentNodeRef, destParentNodeRef, "dest.doc");
        
        
        verify(fileFolderService).rename(sourceNodeRef, "dest.doc");
        verify(davLockService).unlock(sourceNodeRef);
        verify(fileFolderService, never()).create(destParentNodeRef, "dest.doc", ContentModel.TYPE_CONTENT);
    }
}
