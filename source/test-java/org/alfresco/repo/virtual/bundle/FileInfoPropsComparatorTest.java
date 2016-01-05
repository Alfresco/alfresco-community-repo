/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.bundle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.virtual.VirtualizationIntegrationTest;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

public class FileInfoPropsComparatorTest extends VirtualizationIntegrationTest
{

    private static Log logger = LogFactory.getLog(FileInfoPropsComparatorTest.class);

    private void compare(List<FileInfo> fileInfoList, QName property)
    {

        List<Pair<QName, Boolean>> sortProps = new ArrayList<>();
        sortProps.add(new Pair<QName, Boolean>(QName.createQName("IS_FOLDER"),
                                               false));
        sortProps.add(new Pair<QName, Boolean>(property,
                                               false));

        FileInfoPropsComparator comparator = new FileInfoPropsComparator(sortProps);

        // compare 1 file and 1 folder, descending order, folder is first
        assertEquals(-1,
                     comparator.compare(fileInfoList.get(0),
                                        fileInfoList.get(1)));
        assertEquals(1,
                     comparator.compare(fileInfoList.get(1),
                                        fileInfoList.get(0)));

        // compare 1 file and 1 folder, ascending order, folder is first
        List<Pair<QName, Boolean>> localSortProps = sortProps;
        localSortProps.get(0).setSecond(true);
        comparator = new FileInfoPropsComparator(localSortProps);
        assertEquals(1,
                     comparator.compare(fileInfoList.get(0),
                                        fileInfoList.get(1)));
        assertEquals(-1,
                     comparator.compare(fileInfoList.get(1),
                                        fileInfoList.get(0)));

        // compare 2 files, use date mofified
        assertEquals(1,
                     comparator.compare(fileInfoList.get(1),
                                        fileInfoList.get(2)));
        assertEquals(-1,
                     comparator.compare(fileInfoList.get(2),
                                        fileInfoList.get(1)));
    }

    @Test
    public void testCompare()
    {
        createFolder(testRootFolder.getNodeRef(),
                     "FOLDER").getChildRef();
        NodeRef virtualFolder = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                                        VIRTUAL_FOLDER_2_NAME,
                                                        TEST_TEMPLATE_6_JSON_SYS_PATH);
        NodeRef nodeRef1 = nodeService.getChildByName(virtualFolder,
                                                      ContentModel.ASSOC_CONTAINS,
                                                      "Node1");

        QName assocQName1 = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                                              QName.createValidLocalName("testfile1.txt"));

        QName assocQName2 = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                                              QName.createValidLocalName("testfile2.txt"));

        ChildAssociationRef assoc2 = nodeService.createNode(virtualFolder,
                                                            ContentModel.ASSOC_CONTAINS,
                                                            assocQName1,
                                                            ContentModel.TYPE_CONTENT,
                                                            null);
        NodeRef nodeRef2 = assoc2.getChildRef();

        ChildAssociationRef assoc3 = nodeService.createNode(virtualFolder,
                                                            ContentModel.ASSOC_CONTAINS,
                                                            assocQName2,
                                                            ContentModel.TYPE_CONTENT,
                                                            null);
        NodeRef nodeRef3 = assoc3.getChildRef();

        List<NodeRef> nodeRefList = new ArrayList<NodeRef>();
        nodeRefList.add(nodeRef1);
        nodeRefList.add(nodeRef2);
        nodeRefList.add(nodeRef3);

        List<FileInfo> fileInfoList = fileAndFolderService.toFileInfoList(nodeRefList);

        this.compare(fileInfoList,
                     ContentModel.PROP_MODIFIED);
        this.compare(fileInfoList,
                     ContentModel.PROP_CREATED);

        nodeService.setProperty(nodeRef2,
                                ContentModel.PROP_TITLE,
                                "title2");
        nodeService.setProperty(nodeRef3,
                                ContentModel.PROP_TITLE,
                                "title3");
        fileInfoList = fileAndFolderService.toFileInfoList(nodeRefList);
        this.compare(fileInfoList,
                     ContentModel.PROP_TITLE);

        nodeService.setProperty(nodeRef2,
                                ContentModel.PROP_DESCRIPTION,
                                "descr2");
        nodeService.setProperty(nodeRef3,
                                ContentModel.PROP_DESCRIPTION,
                                "descr3");
        fileInfoList = fileAndFolderService.toFileInfoList(nodeRefList);
        this.compare(fileInfoList,
                     ContentModel.PROP_DESCRIPTION);

        nodeService.setProperty(nodeRef2,
                                ContentModel.PROP_DESCRIPTION,
                                "descr2");
        nodeService.setProperty(nodeRef3,
                                ContentModel.PROP_DESCRIPTION,
                                "descr2");
        nodeService.setProperty(nodeRef2,
                                ContentModel.PROP_TITLE,
                                "title");
        nodeService.setProperty(nodeRef3,
                                ContentModel.PROP_TITLE,
                                "title1");
        List<Pair<QName, Boolean>> sortProps = new ArrayList<>();
        sortProps.add(new Pair<QName, Boolean>(QName.createQName("IS_FOLDER"),
                                               false));
        sortProps.add(new Pair<QName, Boolean>(ContentModel.PROP_DESCRIPTION,
                                               false));
        sortProps.add(new Pair<QName, Boolean>(ContentModel.PROP_TITLE,
                                               false));
        fileInfoList = fileAndFolderService.toFileInfoList(nodeRefList);
        FileInfoPropsComparator comparator = new FileInfoPropsComparator(sortProps);
        assertEquals(1,
                     comparator.compare(fileInfoList.get(1),
                                        fileInfoList.get(2)));

        nodeService.setProperty(nodeRef2,
                                ContentModel.PROP_CREATED,
                                new Date(8099,
                                         11,
                                         31));
        nodeService.setProperty(nodeRef3,
                                ContentModel.PROP_CREATED,
                                new Date(0,
                                         0,
                                         0));
        fileInfoList = fileAndFolderService.toFileInfoList(nodeRefList);
        sortProps.remove((sortProps.size() - 1));
        sortProps.add(new Pair<QName, Boolean>(ContentModel.PROP_CREATED,
                                               false));
        comparator = new FileInfoPropsComparator(sortProps);
        assertEquals(1,
                     comparator.compare(fileInfoList.get(1),
                                        fileInfoList.get(2)));

        nodeService.setProperty(nodeRef2,
                                ContentModel.PROP_TITLE,
                                "");
        nodeService.setProperty(nodeRef3,
                                ContentModel.PROP_TITLE,
                                " ");
        fileInfoList = fileAndFolderService.toFileInfoList(nodeRefList);
        sortProps.remove((sortProps.size() - 1));
        sortProps.add(new Pair<QName, Boolean>(ContentModel.PROP_TITLE,
                                               false));
        comparator = new FileInfoPropsComparator(sortProps);
        assertEquals(1,
                     comparator.compare(fileInfoList.get(1),
                                        fileInfoList.get(2)));

    }
}
