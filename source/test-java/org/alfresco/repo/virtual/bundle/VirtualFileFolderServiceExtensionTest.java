/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.repo.virtual.bundle;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.virtual.VirtualizationIntegrationTest;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.TempFileProvider;
import org.junit.Test;

public class VirtualFileFolderServiceExtensionTest extends VirtualizationIntegrationTest
{

    private static final String NEW_FILE_NAME_2 = "RenameTest2";

    private static final String NEW_FILE_NAME_1 = "RenamedTest";

    @Test
    public void testListOne() throws Exception
    {
        NodeRef node2 = nodeService.getChildByName(virtualFolder1NodeRef,
                                                   ContentModel.ASSOC_CHILDREN,
                                                   "Node2");
        final String folderName = "Node2_1";
        final String testFileName = "testfile.txt";
        createContent(node2,
                      testFileName);

        List<FileInfo> node2Contents = fileAndFolderService.list(node2);

        assertEquals(2,
                     node2Contents.size());
        FileInfo testFolderFileInfo = node2Contents.get(0);
        assertEquals(folderName,
                     testFolderFileInfo.getName());
        FileInfo testFileFileInfo = node2Contents.get(1);
        assertEquals(testFileName,
                     testFileFileInfo.getName());
    }

    @Test
    public void testListActualFolders() throws Exception
    {
        createFolder(virtualFolder1NodeRef,
                     "Actual1");
        List<FileInfo> contents = fileAndFolderService.list(virtualFolder1NodeRef);
        assertEquals(3,
                     contents.size());
        assertContainsNames(contents,
                            "Node1",
                            "Node2",
                            "Actual1");
    }

    private void assertContainsNames(List<FileInfo> contents, String... names)
    {
        List<String> fileNames = new ArrayList<>();
        for (FileInfo fileInfo : contents)
        {
            fileNames.add(fileInfo.getName());
        }
        assertTrue(fileNames.containsAll(Arrays.asList(names)));
    }

    private void assertMissesNames(List<FileInfo> contents, String... names)
    {
        List<String> fileNames = new ArrayList<>();
        for (FileInfo fileInfo : contents)
        {
            fileNames.add(fileInfo.getName());
        }
        List<String> missedNames = new ArrayList<>(Arrays.asList(names));
        assertFalse(missedNames.removeAll(fileNames));
    }

    @Test
    public void testListNonVirtualizable() throws Exception
    {
        NodeRef nv = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                             "TestVirtualFileFolderService_testListNonVirtualizable",
                                             null);
        List<FileInfo> emptyList = fileAndFolderService.list(nv);

        assertTrue(emptyList.isEmpty());
    }

    @Test
    public void testSearch() throws Exception
    {
        NodeRef node1 = nodeService.getChildByName(virtualFolder1NodeRef,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Node1");
        NodeRef node2 = nodeService.getChildByName(virtualFolder1NodeRef,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Node2");

        String fileName="testfile.txt";
        fileAndFolderService.create(node1, fileName, ContentModel.TYPE_CONTENT);
        fileAndFolderService.create(node2, fileName, ContentModel.TYPE_CONTENT);

        List<FileInfo> search = fileAndFolderService.search(virtualFolder1NodeRef,
                                                            "testfile.txt",
                                                            true,
                                                            false,
                                                            true);
        // one physical file and one from node1
        assertEquals(2,
                     search.size());

        search = fileAndFolderService.search(virtualFolder1NodeRef,
                                             "testfile-1.txt",
                                             true,
                                             false,
                                             true);
        // one physical file and one from node2
        assertEquals(2,
                     search.size());

        search = fileAndFolderService.search(virtualFolder1NodeRef,
                                             "testfile.txt",
                                             true,
                                             false,
                                             false);

        assertEquals(1,
                     search.size());

        search = fileAndFolderService.search(virtualFolder1NodeRef,
                                             null,
                                             true,
                                             false,
                                             true);
        assertEquals(4,
                     search.size());

        search = fileAndFolderService.search(virtualFolder1NodeRef,
                                             null,
                                             true,
                                             false,
                                             false);

        assertEquals(2,
                     search.size());

        search = fileAndFolderService.search(virtualFolder1NodeRef,
                                             null,
                                             false,
                                             true,
                                             false);

        assertEquals(2,
                     search.size());

        search = fileAndFolderService.search(virtualFolder1NodeRef,
                                             null,
                                             false,
                                             true,
                                             true);

        assertEquals(3,
                     search.size());

    }

    /**
     * List the contents of a virtualized folder retrieved as a virtual child.
     * 
     * @throws Exception
     */
    @Test
    public void testListVirtualizedVirtualChild() throws Exception
    {
        NodeRef vf = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                             "TestVirtualFileFolderService_testVirtualFolderVirtualChild",
                                             TEST_TEMPLATE_3_JSON_SYS_PATH);
        NodeRef node1 = nodeService.getChildByName(vf,
                                                   ContentModel.ASSOC_CHILDREN,
                                                   "Node1");

        final String virtualizedChildFolderName = "TestVirtualFileFolderService_testVirtualFolderVirtualChild_CVF";
        // CM-533 Suppress options to create folders in a virtual folder (repo)
        // we programmatically replicate the process
        NodeRef virtulizedFolder = createVirtualizedFolder(vf,
                                                           virtualizedChildFolderName,
                                                           TEST_TEMPLATE_3_JSON_SYS_PATH);
        nodeService.setProperty(virtulizedFolder,
                                ContentModel.PROP_DESCRIPTION,
                                "Node1_content_FR");

        NodeRef vfchildvf = nodeService.getChildByName(node1,
                                                       ContentModel.ASSOC_CHILDREN,
                                                       virtualizedChildFolderName);

        assertNotNull("Virtual child expected.",
                      vfchildvf);

        {
            List<FileInfo> vfchildvfList = fileAndFolderService.list(vfchildvf);
            assertContainsNames(vfchildvfList,
                                "Node1",
                                "Node2");
        }

        {
            PagingResults<FileInfo> vfchildvfResults = fileAndFolderService.list(vfchildvf,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 new PagingRequest(100));
            List<FileInfo> page = vfchildvfResults.getPage();
            assertContainsNames(page,
                                "Node1",
                                "Node2");
        }

        {
            PagingResults<FileInfo> vfchildvfResults = fileAndFolderService.list(vfchildvf,
                                                                                 true,
                                                                                 true,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 new PagingRequest(100));
            List<FileInfo> page = vfchildvfResults.getPage();
            assertContainsNames(page,
                                "Node1",
                                "Node2");
        }
    }

    @Test
    public void testListFileFolderDisjunction() throws Exception
    {
        NodeRef vf = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                             "TestVirtualFileFolderService_testVirtualFolderVirtualChild",
                                             TEST_TEMPLATE_3_JSON_SYS_PATH);
        NodeRef node2 = nodeService.getChildByName(vf,
                                                   ContentModel.ASSOC_CHILDREN,
                                                   "Node2");

        final String folderName = "FolderVirtualChild";
        // CM-533 Suppress options to create folders in a virtual folder (repo)
        // we programmatically replicate the process
        final HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_DESCRIPTION,
                       "Node2_folder_FR");
        createFolder(vf,
                     folderName,
                     properties);

        final String contentName = "ContentVirtualChild";
        createContent(node2,
                      contentName);

        {
            PagingResults<FileInfo> folderChildren = fileAndFolderService.list(node2,
                                                                               false,
                                                                               true,
                                                                               "*",
                                                                               Collections.<QName> emptySet(),
                                                                               Collections
                                                                                           .<Pair<QName, Boolean>> emptyList(),
                                                                               new PagingRequest(100));
            List<FileInfo> page = folderChildren.getPage();
            assertContainsNames(page,
                                folderName);
            assertMissesNames(page,
                              contentName);
        }

        {
            PagingResults<FileInfo> contentChildren = fileAndFolderService.list(node2,
                                                                                true,
                                                                                false,
                                                                                "*",
                                                                                Collections.<QName> emptySet(),
                                                                                Collections
                                                                                            .<Pair<QName, Boolean>> emptyList(),
                                                                                new PagingRequest(100));
            List<FileInfo> page = contentChildren.getPage();
            assertMissesNames(page,
                              folderName);
            assertContainsNames(page,
                                contentName);
        }

    }

    @Test
    public void testListWithIgnores() throws Exception
    {
        NodeRef vf = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                             "TestVirtualFileFolderService_testVirtualFolderVirtualChild",
                                             TEST_TEMPLATE_3_JSON_SYS_PATH);
        NodeRef node2 = nodeService.getChildByName(vf,
                                                   ContentModel.ASSOC_CHILDREN,
                                                   "Node2");

        final String contentName = "ContentVirtualChild";
        NodeRef contentNodeRef = createContent(node2,
                                               contentName).getChildRef();

        CheckOutCheckInService checkOutCheckInService = ctx.getBean("checkOutCheckInService",
                                                                    CheckOutCheckInService.class);

        checkOutCheckInService.checkout(contentNodeRef);

        Set<QName> searchTypeQNames = Collections.emptySet();
        Set<QName> ignoreAspectQNames = Collections.singleton(ContentModel.ASPECT_CHECKED_OUT);
        List<Pair<QName, Boolean>> sortProps = Collections.<Pair<QName, Boolean>> emptyList();
        PagingRequest pagingRequest = new PagingRequest(100);
        PagingResults<FileInfo> node2List = fileAndFolderService.list(node2,
                                                                      searchTypeQNames,
                                                                      ignoreAspectQNames,
                                                                      sortProps,
                                                                      pagingRequest);

        assertEquals(1,
                     node2List.getPage().size());

    }

    @Test
    public void testCreateTempFile() throws Exception
    {
        NodeRef vf = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                             "TestVirtualFileFolderService_testVirtualFolderVirtualChild",
                                             TEST_TEMPLATE_3_JSON_SYS_PATH);
        NodeRef node2 = nodeService.getChildByName(vf,
                                                   ContentModel.ASSOC_CHILDREN,
                                                   "Node2");
        String contentName = "ContentVirtualChild";
        createContent(node2,
                      contentName);
        NodeRef childByName = nodeService.getChildByName(node2,
                                                         ContentModel.ASSOC_CHILDREN,
                                                         contentName);

        // TODO to test with minimized nodeRef id when minimisation is finished
        File file = File.createTempFile("dedededede",
                                        ".tmp",
                                        TempFileProvider.getTempDir());
        assertNotNull(file);
    }

    @Test
    public void testRename() throws Exception
    {
        NodeRef vf = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                             "TestVirtualFileFolderService_testVirtualFolderVirtualChild",
                                             TEST_TEMPLATE_3_JSON_SYS_PATH);
        NodeRef node2 = nodeService.getChildByName(vf,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Node2");
        String contentName = "ContentVirtualChild";
        createContent(node2,
                      contentName);
        NodeRef childByName = nodeService.getChildByName(node2,
                                                         ContentModel.ASSOC_CONTAINS,
                                                         contentName);
        // rename file in virtual context
        FileInfo renamedFileInfo = fileAndFolderService.rename(childByName,
                                                               NEW_FILE_NAME_1);
        assertNotNull(renamedFileInfo);
        assertEquals(NEW_FILE_NAME_1,
                     nodeService.getProperty(childByName,
                                             ContentModel.PROP_NAME));
        assertNull(nodeService.getChildByName(node2,
                                              ContentModel.ASSOC_CONTAINS,
                                              contentName));
        assertNull(nodeService.getChildByName(vf,
                                              ContentModel.ASSOC_CONTAINS,
                                              contentName));
        assertNotNull(nodeService.getChildByName(node2,
                                                 ContentModel.ASSOC_CONTAINS,
                                                 NEW_FILE_NAME_1));
        assertNotNull(nodeService.getChildByName(vf,
                                                 ContentModel.ASSOC_CONTAINS,
                                                 NEW_FILE_NAME_1));

        // rename physical file
        childByName = nodeService.getChildByName(vf,
                                                 ContentModel.ASSOC_CONTAINS,
                                                 NEW_FILE_NAME_1);
        renamedFileInfo = fileAndFolderService.rename(childByName,
                                                      NEW_FILE_NAME_2);
        assertNotNull(renamedFileInfo);
        assertEquals(NEW_FILE_NAME_2,
                     nodeService.getProperty(childByName,
                                             ContentModel.PROP_NAME));
        assertNull(nodeService.getChildByName(node2,
                                              ContentModel.ASSOC_CONTAINS,
                                              NEW_FILE_NAME_1));
        assertNull(nodeService.getChildByName(vf,
                                              ContentModel.ASSOC_CONTAINS,
                                              NEW_FILE_NAME_1));
        assertNotNull(nodeService.getChildByName(node2,
                                                 ContentModel.ASSOC_CONTAINS,
                                                 NEW_FILE_NAME_2));
        assertNotNull(nodeService.getChildByName(vf,
                                                 ContentModel.ASSOC_CONTAINS,
                                                 NEW_FILE_NAME_2));

    }

    @Test
    public void testListNamePattern() throws Exception
    {
        List<Pair<QName, Boolean>> sortProps = new ArrayList<Pair<QName, Boolean>>(1);
        sortProps.add(new Pair<QName, Boolean>(ContentModel.PROP_NAME,
                                               false));
        String name = "A N.&ame.txt";
        String name1 = "A N.&ame-1.txt";
        String nameAfter = "XXName.txt";

        NodeRef vf = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                             "TestVirtualFileFolderService_testVirtualFolderVirtualChild",
                                             TEST_TEMPLATE_3_JSON_SYS_PATH);
        NodeRef node1 = nodeService.getChildByName(vf,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Node1");
        createContent(node1,
                      name,
                      "0",
                      MimetypeMap.MIMETYPE_TEXT_PLAIN,
                      "UTF-8");

        createContent(node1,
                      name1,
                      "01",
                      MimetypeMap.MIMETYPE_TEXT_PLAIN,
                      "UTF-8");

        createContent(node1,
                      nameAfter,
                      "1",
                      MimetypeMap.MIMETYPE_TEXT_PLAIN,
                      "UTF-8");

        String namePattern = "A N.&ame*.txt";
        PagingResults<FileInfo> results = fileAndFolderService.list(node1,
                                                                    true,
                                                                    false,
                                                                    namePattern,
                                                                    null,
                                                                    sortProps,
                                                                    new PagingRequest(CannedQueryPageDetails.DEFAULT_PAGE_SIZE));

        List<FileInfo> page = results.getPage();
        assertEquals(2,
                     page.size());
        assertEquals(page.get(0).getName(),
                     name1);
        assertEquals(page.get(1).getName(),
                     name);
    }

    @Test
    public void testLastDuplicate() throws Exception
    {
        NodeRef lastDup = null;
        List<Pair<QName, Boolean>> sortProps = new ArrayList<Pair<QName, Boolean>>(1);
        sortProps.add(new Pair<QName, Boolean>(ContentModel.PROP_NAME,
                                               false));
        String name = "AName.txt";
        String nameAfter = "XXName.txt";

        NodeRef vf = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                             "TestVirtualFileFolderService_testVirtualFolderVirtualChild",
                                             TEST_TEMPLATE_3_JSON_SYS_PATH);
        NodeRef node1 = nodeService.getChildByName(vf,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Node1");
        createContent(node1,
                      name,
                      "0",
                      MimetypeMap.MIMETYPE_TEXT_PLAIN,
                      "UTF-8");
        NodeRef aNameNodeRef = nodeService.getChildByName(node1,
                                                          ContentModel.ASSOC_CHILDREN,
                                                          name);

        createContent(node1,
                      nameAfter,
                      "1",
                      MimetypeMap.MIMETYPE_TEXT_PLAIN,
                      "UTF-8");
        NodeRef nameAfterNodeRef = nodeService.getChildByName(node1,
                                                              ContentModel.ASSOC_CHILDREN,
                                                              nameAfter);

        String namePattern = addWildCardInName(name,
                                               fileAndFolderService
                                                           .getFileInfo(aNameNodeRef)
                                                               .getContentData()
                                                               .getMimetype());
        PagingResults<FileInfo> results = fileAndFolderService
                    .list(nodeService.getPrimaryParent(aNameNodeRef).getParentRef(),
                          true,
                          false,
                          namePattern,
                          null,
                          sortProps,
                          new PagingRequest(CannedQueryPageDetails.DEFAULT_PAGE_SIZE));

        List<FileInfo> page = results.getPage();
        FileInfo fileInfo = null;
        if (page.size() > 0)
        {
            fileInfo = page.get(0);
            lastDup = fileInfo.getNodeRef();
        }
        assertFalse(lastDup.equals(nameAfterNodeRef));
    }

    private String addWildCardInName(String name, String mimetype)
    {
        MimetypeService mimetypeService = ctx.getBean("mimetypeService",
                                                      MimetypeService.class);
        String extension = mimetypeService.getExtension(mimetype);
        return name.substring(0,
                              name.length() - (extension.length() + 1))
                        .concat("*." + extension);
    }

    @Test
    public void testListUsedWithCMIS() throws Exception
    {
        NodeRef vf = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                             "TestVirtualFileFolderService_testVirtualFolderVirtualChild",
                                             TEST_TEMPLATE_3_JSON_SYS_PATH);
        NodeRef node2 = nodeService.getChildByName(vf,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Node2");
        String contentName = "ContentVirtualChild";
        createContent(node2,
                      contentName);

        NodeRef childByName = nodeService.getChildByName(node2,
                                                         ContentModel.ASSOC_CONTAINS,
                                                         contentName);

        FileInfo renamedFileInfo = fileAndFolderService.rename(childByName,
                                                               NEW_FILE_NAME_1);
        assertNotNull(renamedFileInfo);
        assertEquals(NEW_FILE_NAME_1,
                     nodeService.getProperty(childByName,
                                             ContentModel.PROP_NAME));

        List<Pair<QName, Boolean>> sortProps = Collections.<Pair<QName, Boolean>> emptyList();
        PagingRequest pagingRequest = new PagingRequest(100);

        PagingResults<FileInfo> results = fileAndFolderService.list(vf,
                                                                    true,
                                                                    true,
                                                                    null,
                                                                    sortProps,
                                                                    pagingRequest);

        assertNotNull(results);

        assertEquals(3,
                     results.getPage().size());

        String contentName1 = "ContentVirtualChild";
        createContent(node2,
                      contentName1);

        results = fileAndFolderService.list(vf,
                                            true,
                                            true,
                                            null,
                                            sortProps,
                                            pagingRequest);

        assertNotNull(results);

        assertEquals(4,
                     results.getPage().size());

    }
}
