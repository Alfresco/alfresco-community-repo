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

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.download.DownloadModel;
import org.alfresco.repo.download.DownloadStorage;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.virtual.VirtualContentModel;
import org.alfresco.repo.virtual.VirtualizationIntegrationTest;
import org.alfresco.repo.virtual.config.NodeRefExpression;
import org.alfresco.repo.virtual.ref.GetActualNodeRefMethod;
import org.alfresco.repo.virtual.ref.GetParentReferenceMethod;
import org.alfresco.repo.virtual.ref.Protocols;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.repo.virtual.ref.ReferenceEncodingException;
import org.alfresco.repo.virtual.ref.VirtualProtocol;
import org.alfresco.repo.virtual.store.VirtualStore;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;
import org.junit.Test;

public class VirtualNodeServiceExtensionTest extends VirtualizationIntegrationTest
{
    private static final String NODE2TEST1_2_TXT = "NODE2test1_2.txt";

    private static Log logger = LogFactory.getLog(VirtualNodeServiceExtensionTest.class);

    private VirtualStore smartStore;

    private DownloadStorage downloadStorage;

    private QName[] node2ChildrenQNames;

    private QName[] rootChildrenQNames;

    private QName[] node2_1ChildrenQNames;

    private NodeRef node2Test1_2_TXTNodeRef;

    @Override
    protected void setUp() throws Exception
    {
        // TODO:is the store really needed when testing node service ? why ?
        super.setUp();
        smartStore = ctx.getBean("smartStore",
                                   VirtualStore.class);
        downloadStorage = ctx.getBean("downloadStorage",
                                      DownloadStorage.class);
    }

    @Test
    public void testCreateNode_withFilingRuleAspects() throws Exception
    {
        NodeRef assocNode2 = nodeService.getChildByName(virtualFolder1NodeRef,
                                                        ContentModel.ASSOC_CONTAINS,
                                                        "Node2");
        NodeRef assocNode2_1 = nodeService.getChildByName(assocNode2,
                                                          ContentModel.ASSOC_CONTAINS,
                                                          "Node2_1");
        ChildAssociationRef childAssocRef = createContent(assocNode2_1,
                                                          "ContentWithAspects");
        assertNewVirtualChildAssocRef(assocNode2_1,
                                      childAssocRef);

        nodeService.hasAspect(childAssocRef.getChildRef(),
                              ContentModel.ASPECT_AUTHOR);

        nodeService.hasAspect(childAssocRef.getChildRef(),
                              ContentModel.ASPECT_DUBLINCORE);
    }

    @Test
    public void testCreateNode_noFilingRule() throws Exception
    {
        NodeRef testTemplate2 = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                                        "aVFTestTemplate2",
                                                        TEST_TEMPLATE_2_JSON_SYS_PATH);
        NodeRef testTemplate2Node2 = nodeService.getChildByName(testTemplate2,
                                                                ContentModel.ASSOC_CONTAINS,
                                                                "Node2");

        try
        {
            createContent(testTemplate2Node2,
                          "shouldNotBeCreated");
            fail("Should not be able to create node in a readonly context.");
        }
        catch (AccessDeniedException e)
        {
            logger.info("Succesfully denied creation in readonly",
                        e);
        }
    }
    
    @Test
    public void testCreate_NodeProtocolParent() throws Exception
    {
        NodeRef assocNode2 = nodeService.getChildByName(virtualFolder1NodeRef,
                                                        ContentModel.ASSOC_CONTAINS,
                                                        "Node2");
        NodeRef assocNode2_1 = nodeService.getChildByName(assocNode2,
                                                          ContentModel.ASSOC_CONTAINS,
                                                          "Node2_1");
        ChildAssociationRef childAssocRef = createContent(assocNode2_1,
                                                          "Content");
        NodeRef node = childAssocRef.getChildRef();
        assertTrue(Reference.isReference(node));
        assertTrue(Reference.fromNodeRef(node).getProtocol().equals(Protocols.NODE.protocol));
        
        QName nodeTypeQName = ContentModel.TYPE_THUMBNAIL;
        QName assocQName = QName.createQName("cm", "contentThumbnail", environment.getNamespacePrefixResolver());
        QName assocTypeQName = RenditionModel.ASSOC_RENDITION;
        ChildAssociationRef assoc = nodeService.createNode(node,
                                              assocTypeQName,
                                              assocQName,
                                              nodeTypeQName);
        NodeRef virtualRenditionNode = assoc.getChildRef();
        NodeRef virtualRenditionParent = assoc.getParentRef();        
        assertEquals(node, virtualRenditionParent);
        
        Reference child = Reference.fromNodeRef(virtualRenditionNode);
        Reference parent = Reference.fromNodeRef(virtualRenditionParent);
        NodeRef physicalRenditionNode = child.execute(new GetActualNodeRefMethod(environment));
        NodeRef physicalRenditionParent = parent.execute(new GetActualNodeRefMethod(environment));
        List<ChildAssociationRef> refs = nodeService.getChildAssocs(physicalRenditionParent);
        assertEquals(physicalRenditionNode, refs.get(0).getChildRef()); // the association exists for the physical nodes
        
        List<ChildAssociationRef> virtualRefs = nodeService.getChildAssocs(virtualRenditionParent);
        assertEquals(physicalRenditionNode, virtualRefs.get(0).getChildRef()); // the association exists for the virtual nodes
    }

    @Test
    public void testCreateNode_CM_528_folder_filing_type() throws Exception
    {
        NodeRef testTemplate5 = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                                        "aVFTestTemplate5",
                                                        TEST_TEMPLATE_5_JSON_SYS_PATH);
        NodeRef folderFilingTypeNode = nodeService.getChildByName(testTemplate5,
                                                                  ContentModel.ASSOC_CONTAINS,
                                                                  "FolderFilingType");

        ChildAssociationRef forcedCmContentAssocRef = createContent(folderFilingTypeNode,
                                                                    "forcedCmContent");
        NodeRef forcedCmContent = forcedCmContentAssocRef.getChildRef();

        QName actualType = nodeService.getType(forcedCmContent);
        assertEquals(ContentModel.TYPE_CONTENT,
                     actualType);
    }

    @Test
    public void testCreateNode() throws Exception
    {

        assertTrue(smartStore.canVirtualize(virtualFolder1NodeRef));

        Reference semiVirtualFolder = smartStore.virtualize(virtualFolder1NodeRef);
        assertNotNull(semiVirtualFolder);
        assertTrue(semiVirtualFolder.getProtocol() instanceof VirtualProtocol);

        Reference firstChild = smartStore.getChildByName(semiVirtualFolder,
                                                           ContentModel.ASSOC_CONTAINS,
                                                           "Node1");
        assertNotNull(firstChild);

        Reference secondChild = smartStore.getChildByName(semiVirtualFolder,
                                                            ContentModel.ASSOC_CONTAINS,
                                                            "Node2");
        assertNotNull(secondChild);

        authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        // add testfile.txt to first virtual child

        String fileName="testfile.txt";
        uploadNode(firstChild,
                   fileName);
        assertNotNull(nodeService.getChildByName(firstChild.toNodeRef(),
                                                 ContentModel.ASSOC_CONTAINS,
                                                 "testfile.txt"));
        uploadNode(firstChild,
                   fileName);
        assertNotNull(nodeService.getChildByName(firstChild.toNodeRef(),
                                                 ContentModel.ASSOC_CONTAINS,
                                                 "testfile-1.txt"));

        // add testfile.txt to second virtual child
        uploadNode(secondChild,
                   fileName);
        assertNotNull(nodeService.getChildByName(secondChild.toNodeRef(),
                                                 ContentModel.ASSOC_CONTAINS,
                                                 "testfile-2.txt"));
        uploadNode(secondChild,
                   fileName);
        assertNotNull(nodeService.getChildByName(secondChild.toNodeRef(),
                                                 ContentModel.ASSOC_CONTAINS,
                                                 "testfile-3.txt"));

        // add again to first virtual child starting from the last index found
        // (this is the index that comes from
        // upload-post.js)
       fileName="testfile-2.txt";
       uploadNode(firstChild,
                  fileName);
        assertNotNull(nodeService.getChildByName(firstChild.toNodeRef(),
                                                 ContentModel.ASSOC_CONTAINS,
                                                 "testfile-4.txt"));

        // test create node for actual node starting from the last index found
        // (this is the index that comes from
        // upload-post.js)
        fileName="testfile-5.txt";
        fileAndFolderService.create(virtualFolder1NodeRef, fileName, ContentModel.TYPE_CONTENT);
        assertNotNull(nodeService.getChildByName(virtualFolder1NodeRef,
                                                 ContentModel.ASSOC_CONTAINS,
                                                 "testfile-5.txt"));

        fileName="testfile-6.txt";
        fileAndFolderService.create(virtualFolder1NodeRef, fileName, ContentModel.TYPE_CONTENT);
        assertNotNull(nodeService.getChildByName(virtualFolder1NodeRef,
                                                 ContentModel.ASSOC_CONTAINS,
                                                 "testfile-6.txt"));

        // add again to second child starting from the last index found (this is
        // the index that comes from
        // upload-post.js)
        fileName="testfile-4.txt";
        uploadNode(secondChild,
                   fileName);
        assertNotNull(nodeService.getChildByName(secondChild.toNodeRef(),
                                                 ContentModel.ASSOC_CONTAINS,
                                                 "testfile-7.txt"));

        // test situation when file name is of form testfile1-1.txt
        fileName="testfile1-1.txt";
        fileAndFolderService.create(secondChild.toNodeRef(), fileName, ContentModel.TYPE_CONTENT);
        assertNotNull(nodeService.getChildByName(secondChild.toNodeRef(),
                                                 ContentModel.ASSOC_CONTAINS,
                                                 "testfile1-1.txt"));
        fileAndFolderService.create(secondChild.toNodeRef(), fileName, ContentModel.TYPE_CONTENT);
        assertNotNull(nodeService.getChildByName(secondChild.toNodeRef(),
                                                 ContentModel.ASSOC_CONTAINS,
                                                 "testfile1-1-1.txt"));

    }

    @Test
    @Ignore("CM-533 Suppress options to create folders in a virtual folder (repo)")
    public void ignore_testCreateFilingIrregularNode() throws Exception
    {
        NodeRef vf = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                             "testCreateFolderOnContentFilingRule",
                                             TEST_TEMPLATE_3_JSON_SYS_PATH);
        {
            NodeRef node1 = nodeService.getChildByName(vf,
                                                       ContentModel.ASSOC_CHILDREN,
                                                       "Node1");
            HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
            properties.put(ContentModel.PROP_NAME,
                           "Folder1");
            QName assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                                                 QName.createValidLocalName("Folder1"));

            ChildAssociationRef folderChilAssoc = nodeService.createNode(node1,
                                                                         ContentModel.ASSOC_CONTAINS,
                                                                         assocQName,
                                                                         ContentModel.TYPE_FOLDER,
                                                                         properties);
            NodeRef folderChildRef = folderChilAssoc.getChildRef();
            assertEquals(ContentModel.TYPE_FOLDER,
                         environment.getType(folderChildRef));
            assertEquals("Node1_content_FR",
                         environment.getProperties(folderChildRef).get(ContentModel.PROP_DESCRIPTION));
        }

        {
            NodeRef node2 = nodeService.getChildByName(vf,
                                                       ContentModel.ASSOC_CHILDREN,
                                                       "Node2");
            HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
            properties.put(ContentModel.PROP_NAME,
                           "Content2");
            QName assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                                                 QName.createValidLocalName("Content2"));

            ChildAssociationRef folderChilAssoc = nodeService.createNode(node2,
                                                                         ContentModel.ASSOC_CONTAINS,
                                                                         assocQName,
                                                                         ContentModel.TYPE_CONTENT,
                                                                         properties);
            NodeRef folderChildRef = folderChilAssoc.getChildRef();
            assertEquals(ContentModel.TYPE_CONTENT,
                         environment.getType(folderChildRef));
            assertEquals("Node2_folder_FR",
                         environment.getProperties(folderChildRef).get(ContentModel.PROP_DESCRIPTION));
        }
    }

    private void createNode(Reference reference, final QName assocQName, HashMap<QName, Serializable> properties)
                throws ReferenceEncodingException
    {
        ChildAssociationRef childAssocsRef = nodeService.createNode(reference.toNodeRef(),
                                                                    ContentModel.ASSOC_CONTAINS,
                                                                    assocQName,
                                                                    ContentModel.TYPE_CONTENT,
                                                                    properties);
        assertNewVirtualChildAssocRef(reference,
                                      childAssocsRef);
    }

    private void uploadNode(Reference reference, String name)
    {
        fileAndFolderService.create(reference.toNodeRef(),
                                    name,
                                    ContentModel.TYPE_CONTENT);
    }

    /**
     * Assets that the given {@link ChildAssociationRef} was created within the
     * given virtualizable nodeRef container reference.
     * 
     * @param nodeRef
     * @param childAssocsRef
     */
    private void assertNewVirtualChildAssocRef(NodeRef nodeRef, ChildAssociationRef childAssocsRef)
    {
        assertTrue(Reference.isReference(nodeRef));
        assertNewVirtualChildAssocRef(Reference.fromNodeRef(nodeRef),
                                      childAssocsRef);
    }

    /**
     * Assets that the given {@link ChildAssociationRef} was created within the
     * given container {@link Reference}.
     * 
     * @param nodeRef
     * @param childAssocsRef
     */
    private void assertNewVirtualChildAssocRef(Reference reference, ChildAssociationRef childAssocsRef)
    {
        assertNotNull(childAssocsRef);
        NodeRef childNodeRef = childAssocsRef.getChildRef();
        NodeRef parentNodeRef = childAssocsRef.getParentRef();

        assertTrue(Reference.isReference(parentNodeRef));
        assertEquals(reference,
                     Reference.fromNodeRef(parentNodeRef));

        assertTrue(Reference.isReference(childNodeRef));
        Reference.fromNodeRef(childNodeRef);
        Reference childReference = Reference.fromNodeRef(childNodeRef);
        Reference parent = childReference.execute(new GetParentReferenceMethod());
        assertEquals(reference,
                     parent);
    }

    @Test
    public void testGetPath() throws Exception
    {
        NodeRef node2 = nodeService.getChildByName(virtualFolder1NodeRef,
                                                   ContentModel.ASSOC_CHILDREN,
                                                   "Node2");
        assertNotNull(node2);
        Path node2Path = nodeService.getPath(node2);
        assertNotNull(node2Path);
        assertEquals("/app:company_home/cm:TestFolder/cm:VirtualFolder1/sf:Node2",
                     node2Path.toPrefixString(environment.getNamespacePrefixResolver()));
    }

    @Test
    public void testNodeProtocolReferencePath() throws Exception
    {
        NodeRef node2 = nodeService.getChildByName(virtualFolder1NodeRef,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Node2");

        String fileName="testfile.txt";

        // add testfile.txt to first virtual child
        fileAndFolderService.create(node2, fileName, ContentModel.TYPE_CONTENT);

        NodeRef childRef = nodeService.getChildByName(node2,
                                                      ContentModel.ASSOC_CONTAINS,
                                                      "testfile.txt");
        Path path = nodeService.getPath(childRef);
        assertEquals("/app:company_home/cm:TestFolder/cm:VirtualFolder1/sf:Node2/cm:testfile.txt",
                     path.toPrefixString(environment.getNamespacePrefixResolver()));

        NodeRef physicalNode = nodeService.getChildByName(virtualFolder1NodeRef,
                                                          ContentModel.ASSOC_CONTAINS,
                                                          "testfile.txt");
        assertNotNull(physicalNode);

        Path physicalPath = nodeService.getPath(physicalNode);

        assertEquals("/app:company_home/cm:TestFolder/cm:VirtualFolder1/cm:testfile.txt",
                     physicalPath.toPrefixString(environment.getNamespacePrefixResolver()));

        NodeRef node2_1 = nodeService.getChildByName(node2,
                                                     ContentModel.ASSOC_CONTAINS,
                                                     "Node2_1");
        assertNotNull(node2_1);

        fileAndFolderService.create(node2_1, fileName, ContentModel.TYPE_CONTENT);

        NodeRef childRef_1 = nodeService.getChildByName(node2_1,
                                                        ContentModel.ASSOC_CONTAINS,
                                                        "testfile-1.txt");
        Path path_1 = nodeService.getPath(childRef_1);
        assertEquals("/app:company_home/cm:TestFolder/cm:VirtualFolder1/sf:Node2/sf:Node2_1/cm:testfile-1.txt",
                     path_1.toPrefixString(environment.getNamespacePrefixResolver()));
    }

    private void setUpTestAssociations(NodeRef actualNodeRef)
    {
        rootChildrenQNames = new QName[13];
        rootChildrenQNames[0] = QName.createQNameWithValidLocalName(VirtualContentModel.VIRTUAL_CONTENT_MODEL_1_0_URI,
                                                                    "Node2");
        rootChildrenQNames[1] = QName.createQNameWithValidLocalName(VirtualContentModel.VIRTUAL_CONTENT_MODEL_1_0_URI,
                                                                    "Node1");

        NodeRef node2 = nodeService.getChildByName(virtualFolder1NodeRef,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Node2");

        String node2ChildNameString = "test1_2.txt";
        ChildAssociationRef node2ChildAssoc = createContent(node2,
                                                            node2ChildNameString);
        node2Test1_2_TXTNodeRef = node2ChildAssoc.getChildRef();
        rootChildrenQNames[2] = QName.createQNameWithValidLocalName(NamespaceService.CONTENT_MODEL_1_0_URI,
                                                                    node2ChildNameString);

        nodeService.setProperty(node2ChildAssoc.getChildRef(),
                                ContentModel.PROP_TITLE,
                                NODE2TEST1_2_TXT);

        node2ChildrenQNames = new QName[2];
        node2ChildrenQNames[0] = QName.createQNameWithValidLocalName(VirtualContentModel.VIRTUAL_CONTENT_MODEL_1_0_URI,
                                                                     "Node2_1");
        node2ChildrenQNames[1] = node2ChildAssoc.getQName();

        NodeRef node2_1 = nodeService.getChildByName(node2,
                                                     ContentModel.ASSOC_CONTAINS,
                                                     "Node2_1");

        node2_1ChildrenQNames = new QName[10];
        for (int i = 1; i <= 10; i++)
        {
            ChildAssociationRef childAssoc = createContent(node2_1,
                                                           "test" + i + "_2_1.txt");
            rootChildrenQNames[2 + i] = QName.createQNameWithValidLocalName(NamespaceService.CONTENT_MODEL_1_0_URI,
                                                                            childAssoc.getQName().getLocalName());
            node2_1ChildrenQNames[i - 1] = childAssoc.getQName();
        }
    }

    protected QName[] copyOf(QName[] from, int count)
    {
        QName[] tmp = new QName[count];

        if (count < from.length)
        {
            System.arraycopy(from,
                             0,
                             tmp,
                             0,
                             count);
        }
        else
        {
            System.arraycopy(from,
                             0,
                             tmp,
                             0,
                             from.length);
        }

        return tmp;
    }

    private ChildAssociationRef findActualAssocPeer(ChildAssociationRef virtualAssoc, NodeRef actualParentNodeRef)
    {
        List<ChildAssociationRef> actualAssocs = nodeService.getChildAssocs(actualParentNodeRef);
        NodeRef virtualChildNodeRef = virtualAssoc.getChildRef();
        assertTrue(Reference.isReference(virtualChildNodeRef));
        NodeRef materialNodeRef = smartStore.materialize(Reference.fromNodeRef(virtualChildNodeRef));

        for (ChildAssociationRef actualAssocRef : actualAssocs)
        {
            if (materialNodeRef.equals(actualAssocRef.getChildRef()))
            {
                if (virtualAssoc.getQName().getLocalName().equals(actualAssocRef.getQName().getLocalName())
                            && virtualAssoc.getTypeQName().equals(actualAssocRef.getTypeQName()))
                {
                    return actualAssocRef;
                }
            }
        }

        return null;
    }

    @Test
    public void testRemoveChildAssoc_1() throws Exception
    {
        NodeRef node2 = nodeService.getChildByName(virtualFolder1NodeRef,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Node2");

        ChildAssociationRef node2ChildAssoc = createContent(node2,
                                                            "TestPresentation4_pdf_removeAssoc");

        assertNotNull(findActualAssocPeer(node2ChildAssoc,
                                          virtualFolder1NodeRef));

        boolean removed = nodeService.removeChildAssociation(node2ChildAssoc);

        assertTrue("No association",
                   removed);

        assertNull(findActualAssocPeer(node2ChildAssoc,
                                       virtualFolder1NodeRef));
    }

    @Test
    public void testGetChildAssocs_1() throws Exception
    {
        // semi-virtual folder with 2 virtual folder nodes
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(virtualFolder1NodeRef);
        assertEquals(2,
                     childAssocs.size());

        setUpTestAssociations(virtualFolder1NodeRef);

        NodeRef node2 = nodeService.getChildByName(virtualFolder1NodeRef,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Node2");

        NodeRef node2_1 = nodeService.getChildByName(node2,
                                                     ContentModel.ASSOC_CONTAINS,
                                                     "Node2_1");

        childAssocs = nodeService.getChildAssocs(virtualFolder1NodeRef);
        assertEquals(13,
                     childAssocs.size());
        assertAssocNames(childAssocs,
                         rootChildrenQNames);

        // one virtual folder with a virtual folder child and a virtual node
        // reference child
        if (logger.isDebugEnabled())
        {
            logger.debug("Getting children of node2 " + node2);
        }
        childAssocs = nodeService.getChildAssocs(node2);
        if (logger.isDebugEnabled())
        {
            logger.debug("Got children of node2 " + childAssocs);
        }
        assertEquals(2,
                     childAssocs.size());

        // one virtual folder with a virtual node reference child
        childAssocs = nodeService.getChildAssocs(node2_1);
        assertEquals(10,
                     childAssocs.size());
        assertAssocNames(childAssocs,
                         node2_1ChildrenQNames);

    }

    protected void assertAssocNames(List<ChildAssociationRef> assocs, QName... expectedNames)
    {
        List<QName> actualNames = new LinkedList<>();
        for (ChildAssociationRef childAssociationRef : assocs)
        {
            actualNames.add(childAssociationRef.getQName());
        }

        assertEquals(expectedNames.length,
                     actualNames.size());
        for (int i = 0; i < expectedNames.length; i++)
        {
            assertTrue(expectedNames[i] + " assoc name was expected  within " + actualNames,
                       actualNames.contains(expectedNames[i]));
            actualNames.remove(expectedNames[i]);
        }
    }

    @Test
    public void testGetChildAssocs_2() throws Exception
    {
        // semi-virtual folder with 2 virtual folder nodes
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(virtualFolder1NodeRef);
        assertEquals(2,
                     childAssocs.size());

        setUpTestAssociations(virtualFolder1NodeRef);

        NodeRef node2 = nodeService.getChildByName(virtualFolder1NodeRef,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Node2");

        NodeRef node2_1 = nodeService.getChildByName(node2,
                                                     ContentModel.ASSOC_CONTAINS,
                                                     "Node2_1");

        childAssocs = nodeService.getChildAssocs(node2,
                                                 ContentModel.ASSOC_ARCHIVED_LINK,
                                                 ContentModel.ASSOC_ARCHIVED_LINK,
                                                 7,
                                                 true);

        assertTrue(childAssocs.isEmpty());

        childAssocs = nodeService.getChildAssocs(node2,
                                                 RegexQNamePattern.MATCH_ALL,
                                                 RegexQNamePattern.MATCH_ALL,
                                                 7,
                                                 true);
        assertEquals(2,
                     childAssocs.size());

        assertAssocNames(childAssocs,
                         copyOf(node2ChildrenQNames,
                                2));

        childAssocs = nodeService.getChildAssocs(virtualFolder1NodeRef,

                                                 RegexQNamePattern.MATCH_ALL,
                                                 new QNamePattern()
                                                 {

                                                     @Override
                                                     public boolean isMatch(QName qname)
                                                     {
                                                         return qname.getLocalName().startsWith("test")
                                                                     && qname.getLocalName().endsWith("txt");
                                                     }
                                                 },
                                                 15,
                                                 true);

        assertEquals(11,
                     childAssocs.size());

        // one virtual folder with a virtual folder child and a virtual node
        // reference child
        if (logger.isDebugEnabled())
        {
            logger.debug("Getting children of node2 " + node2);
        }
        childAssocs = nodeService.getChildAssocs(node2,
                                                 RegexQNamePattern.MATCH_ALL,
                                                 RegexQNamePattern.MATCH_ALL,
                                                 2,
                                                 true);
        if (logger.isDebugEnabled())
        {
            logger.debug("Got children of node2 " + childAssocs);
        }
        assertEquals(2,
                     childAssocs.size());

        // one virtual folder with a virtual node reference child
        childAssocs = nodeService.getChildAssocs(node2_1,
                                                 RegexQNamePattern.MATCH_ALL,
                                                 RegexQNamePattern.MATCH_ALL,
                                                 10,
                                                 true);
        assertEquals(10,
                     childAssocs.size());

        assertAssocNames(childAssocs,
                         copyOf(node2_1ChildrenQNames,
                                10));

    }

    @Test
    public void testGetChildAssocs_3() throws Exception
    {
        // semi-virtual folder with 2 virtual folder nodes
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(virtualFolder1NodeRef);
        assertEquals(2,
                     childAssocs.size());

        setUpTestAssociations(virtualFolder1NodeRef);

        List<ChildAssociationRef> folderAssocs = nodeService
                    .getChildAssocs(virtualFolder1NodeRef,
                                    new HashSet<>(Arrays.asList(ContentModel.TYPE_FOLDER)));

        assertEquals(2,
                     folderAssocs.size());

        List<ChildAssociationRef> contentAssocs = nodeService
                    .getChildAssocs(virtualFolder1NodeRef,
                                    new HashSet<>(Arrays.asList(ContentModel.TYPE_CONTENT)));

        assertEquals(11,
                     contentAssocs.size());
    }

    @Test
    public void testGetChildAssocsByPropertyValue() throws Exception
    {
        setUpTestAssociations(virtualFolder1NodeRef);

        NodeRef node2 = nodeService.getChildByName(virtualFolder1NodeRef,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Node2");
        {
            List<ChildAssociationRef> children = nodeService.getChildAssocsByPropertyValue(node2,
                                                                                           ContentModel.PROP_TITLE,
                                                                                           NODE2TEST1_2_TXT);
            assertNotNull(children);
            assertEquals(1,
                         children.size());
            assertEquals(node2Test1_2_TXTNodeRef,
                         children.get(0).getChildRef());

        }

        {
            List<ChildAssociationRef> children = nodeService.getChildAssocsByPropertyValue(node2,
                                                                                           ContentModel.PROP_TITLE,
                                                                                           "non" + NODE2TEST1_2_TXT);
            assertTrue(children.isEmpty());
        }
    }

    @Test
    public void testGetProperties() throws Exception
    {
        NodeRef template2VF = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                                      VIRTUAL_FOLDER_2_NAME,
                                                      TEST_TEMPLATE_2_JSON_SYS_PATH);

        final String expectedDescription = "ParentDescription";
        nodeService.setProperty(template2VF,
                                ContentModel.PROP_DESCRIPTION,
                                expectedDescription);

        NodeRef node1 = nodeService.getChildByName(template2VF,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Node1");

        assertVirtualNode(node1);

        NodeRef node2 = nodeService.getChildByName(template2VF,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Node2");

        Map<QName, Serializable> expectedProperties = new HashMap<>();
        expectedProperties.put(ContentModel.PROP_DESCRIPTION,
                               expectedDescription);
        assertVirtualNode(node2,
                          expectedProperties);
    }

    @Test
    public void testCreateAssociation() throws Exception
    {
        NodeRef createDownloadNode = createDownloadNode();
        NodeRef node2 = nodeService.getChildByName(virtualFolder1NodeRef,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Node2");
        nodeService.createAssociation(createDownloadNode,
                                      node2,
                                      DownloadModel.ASSOC_REQUESTED_NODES);

    }

    @Test
    public void testGetParentAssocs() throws Exception
    {
        NodeRef node2 = nodeService.getChildByName(virtualFolder1NodeRef,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Node2");

        {
            List<ChildAssociationRef> node2Parents = nodeService.getParentAssocs(node2);
            assertNotNull(node2Parents);
            assertTrue(!node2Parents.isEmpty());
            ChildAssociationRef firstAssoc = node2Parents.get(0);
            NodeRef child = firstAssoc.getChildRef();
            NodeRef parent = firstAssoc.getParentRef();

            assertEquals(ContentModel.ASSOC_CONTAINS,
                         firstAssoc.getTypeQName());
            assertEquals(node2,
                         child);
            assertEquals(virtualFolder1NodeRef,
                         parent);
            assertNotNull(firstAssoc.getQName());
            assertEquals(QName.createQNameWithValidLocalName(VirtualContentModel.VIRTUAL_CONTENT_MODEL_1_0_URI,
                                                             "Node2"),
                         firstAssoc.getQName());
        }

        {
            List<ChildAssociationRef> node2Parents = nodeService
                        .getParentAssocs(node2,
                                         RegexQNamePattern.MATCH_ALL,
                                         new RegexQNamePattern(VirtualContentModel.VIRTUAL_CONTENT_MODEL_1_0_URI,
                                                               "Node."));
            assertNotNull(node2Parents);
            assertTrue(!node2Parents.isEmpty());
            ChildAssociationRef firstAssoc = node2Parents.get(0);
            NodeRef child = firstAssoc.getChildRef();
            NodeRef parent = firstAssoc.getParentRef();

            assertEquals(ContentModel.ASSOC_CONTAINS,
                         firstAssoc.getTypeQName());
            assertEquals(node2,
                         child);
            assertEquals(virtualFolder1NodeRef,
                         parent);
            assertNotNull(firstAssoc.getQName());
            assertEquals(QName.createQName(VirtualContentModel.VIRTUAL_CONTENT_MODEL_1_0_URI,
                                           "Node2"),
                         firstAssoc.getQName());
        }

        {
            List<ChildAssociationRef> node2Parents = nodeService
                        .getParentAssocs(node2,
                                         RegexQNamePattern.MATCH_ALL,
                                         new RegexQNamePattern(VirtualContentModel.VIRTUAL_CONTENT_MODEL_1_0_URI,
                                                               "Foo."));
            assertNotNull(node2Parents);
            assertTrue(node2Parents.isEmpty());
        }

    }

    @Test
    public void testGetSourceAssocs_download() throws Exception
    {
        NodeRef node2 = nodeService.getChildByName(virtualFolder1NodeRef,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Node2");

        List<AssociationRef> sourceAssocs = null;
        sourceAssocs = nodeService.getSourceAssocs(node2,
                                                   RegexQNamePattern.MATCH_ALL);
        assertEquals(0,
                     sourceAssocs.size());

        sourceAssocs = nodeService.getSourceAssocs(node2,
                                                   DownloadModel.ASSOC_REQUESTED_NODES);
        assertEquals(0,
                     sourceAssocs.size());

        // one virtual noderef
        NodeRef createDownloadNode = createDownloadNode();
        nodeService.createAssociation(createDownloadNode,
                                      node2,
                                      DownloadModel.ASSOC_REQUESTED_NODES);
        sourceAssocs = nodeService.getSourceAssocs(node2,
                                                   DownloadModel.ASSOC_REQUESTED_NODES);

        // sources are deliberately not virtualized due to performance and
        // complexity issues
        assertEquals(0,
                     sourceAssocs.size());

    }

    @Test
    public void testGetTargetAssocs_download() throws Exception
    {

        NodeRef node2 = nodeService.getChildByName(virtualFolder1NodeRef,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Node2");

        List<AssociationRef> targetAssocs = nodeService.getTargetAssocs(node2,
                                                                        DownloadModel.ASSOC_REQUESTED_NODES);
        assertEquals(0,
                     targetAssocs.size());

        // one virtual noderef
        NodeRef createDownloadNode = createDownloadNode();
        nodeService.createAssociation(createDownloadNode,
                                      node2,
                                      DownloadModel.ASSOC_REQUESTED_NODES);
        targetAssocs = nodeService.getTargetAssocs(createDownloadNode,
                                                   ContentModel.ASSOC_CONTAINS);
        assertEquals(0,
                     targetAssocs.size());
        targetAssocs = nodeService.getTargetAssocs(createDownloadNode,
                                                   DownloadModel.ASSOC_REQUESTED_NODES);
        assertEquals(1,
                     targetAssocs.size());

        // 2 virtual node ref ...associations have to be created again since
        // they are removed after they are obtained in
        // order to cleanup temp node refs from repository.
        NodeRef node2_1 = nodeService.getChildByName(node2,
                                                     ContentModel.ASSOC_CONTAINS,
                                                     "Node2_1");
        nodeService.createAssociation(createDownloadNode,
                                      node2,
                                      DownloadModel.ASSOC_REQUESTED_NODES);
        nodeService.createAssociation(createDownloadNode,
                                      node2_1,
                                      DownloadModel.ASSOC_REQUESTED_NODES);
        targetAssocs = nodeService.getTargetAssocs(createDownloadNode,
                                                   ContentModel.ASSOC_CONTAINS);
        assertEquals(0,
                     targetAssocs.size());
        for (int i = 0; i < 2; i++)
        {
            targetAssocs = nodeService.getTargetAssocs(createDownloadNode,
                                                       DownloadModel.ASSOC_REQUESTED_NODES);
            assertEquals("Try # " + (i + 1),
                         2,
                         targetAssocs.size());
        }

        List<NodeRef> targets = new LinkedList<>();
        for (AssociationRef assocs : targetAssocs)
        {
            targets.add(assocs.getTargetRef());
        }

        assertTrue(targets.contains(node2));

        assertTrue(targets.contains(node2_1));

        NodeRefExpression downloadAsocsFolderExpr = virtualizationConfigTestBootstrap.getDownloadAssocaiationsFolder();
        NodeRef downloadAssocsFolder = downloadAsocsFolderExpr.resolve();

        NodeRef tempDownloadSourceAssocs = nodeService.getChildByName(downloadAssocsFolder,
                                                                      ContentModel.ASSOC_CONTAINS,
                                                                      createDownloadNode.getId());

        assertNotNull(tempDownloadSourceAssocs);

        downloadStorage.delete(createDownloadNode);

        assertFalse("Association information was not removed when removing the source.",
                    nodeService.exists(tempDownloadSourceAssocs));
    }

    @Test
    public void testGetChildByName()
    {

        NodeRef virtualFolder = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                                        VIRTUAL_FOLDER_2_NAME,
                                                        TEST_TEMPLATE_4_JSON_SYS_PATH);

        assertTrue(smartStore.canVirtualize(virtualFolder));
        Reference semiVirtualFolder = smartStore.virtualize(virtualFolder);
        assertNotNull(semiVirtualFolder);

        // access virtual entry
        NodeRef virtualChild = nodeService.getChildByName(semiVirtualFolder.toNodeRef(),
                                                          ContentModel.ASSOC_CONTAINS,
                                                          "All My Content");
        assertNotNull(virtualChild);
        assertVirtualNode(virtualChild);

        // access physical child on first level
        NodeRef phys1 = nodeService.getChildByName(virtualChild,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Company Home");
        assertNotNull(phys1);
        assertVirtualNode(virtualChild);

        // access physical child on second level
        NodeRef phys2 = nodeService.getChildByName(phys1,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Data Dictionary");
        assertNotNull(phys2);
        assertVirtualNode(virtualChild);
    }

    @Test
    public void testGetPrimaryParent() throws Exception
    {
        // check that the primary parent node of level 1 virtual folder is
        // actual folder and not the root node from json template
        NodeRef node2 = nodeService.getChildByName(virtualFolder1NodeRef,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Node2");
        assertNotNull(node2);
        ChildAssociationRef primaryParent = nodeService.getPrimaryParent(node2);
        assertNotNull(primaryParent);
        NodeRef parentRef = primaryParent.getParentRef();
        assertEquals(virtualFolder1NodeRef,
                     parentRef);

        // check that the primary parent node of level 2 virtual folder is his
        // virtual folder parent
        NodeRef node2_1 = nodeService.getChildByName(node2,
                                                     ContentModel.ASSOC_CONTAINS,
                                                     "Node2_1");
        assertNotNull(node2_1);
        primaryParent = nodeService.getPrimaryParent(node2_1);
        assertNotNull(primaryParent);
        parentRef = primaryParent.getParentRef();
        assertEquals(node2,
                     parentRef);
    }

    @Test
    public void testGetAssocs_CM_673() throws Exception
    {
        NodeRef createFolder = createFolder(testRootFolder.getNodeRef(),
                                            "FOLDER").getChildRef();
        createContent(createFolder,
                      "testFile1",
                      "0",
                      MimetypeMap.MIMETYPE_TEXT_PLAIN,
                      "UTF-8");
        NodeRef virtualFolder = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                                        VIRTUAL_FOLDER_2_NAME,
                                                        TEST_TEMPLATE_6_JSON_SYS_PATH);
        NodeRef node1 = nodeService.getChildByName(virtualFolder,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Node1");
        assertNotNull(node1);

        NodeRef physicalFolderInVirtualContext = nodeService.getChildByName(node1,
                                                                            ContentModel.ASSOC_CONTAINS,
                                                                            "FOLDER");
        assertNotNull(physicalFolderInVirtualContext);

        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(physicalFolderInVirtualContext);
        assertNotNull(childAssocs);
        assertEquals(1,
                     childAssocs.size());
        assertEquals("testFile1",
                     nodeService.getProperty(childAssocs.get(0).getChildRef(),
                                             ContentModel.PROP_NAME));
    }

    @Test
    public void testCopySemivirtualFolder() throws Exception
    {
        configuredTemplatesClassPath = constraints.getTemplatesParentClasspath();
        constraints.setTemplatesParentClasspath("/org/alfresco/repo/virtual/template");
        IntegrityChecker integrityChecker = (IntegrityChecker) ctx.getBean("integrityChecker");
        NodeRef childRef = createFolder(testRootFolder.getNodeRef(),
                                        "TT").getChildRef();
        CopyService copyService = ctx.getBean("copyService",
                                  CopyService.class);
        copyService.copyAndRename(virtualFolder1NodeRef,
                                  childRef,
                                  ContentModel.ASSOC_CONTAINS,
                                  null,
                                  true);
        NodeRef copiedNodeRef = nodeService.getChildByName(childRef,
                                                           ContentModel.ASSOC_CONTAINS,
                                                           VIRTUAL_FOLDER_1_NAME);
        assertNotNull(copiedNodeRef);
        NodeRef node2 = nodeService.getChildByName(copiedNodeRef,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Node2");
        assertNotNull(node2);

        NodeRef node2_1 = nodeService.getChildByName(node2,
                                                     ContentModel.ASSOC_CONTAINS,
                                                     "Node2_1");
        assertNotNull(node2_1);

        integrityChecker.checkIntegrity();
    }

    @Test
    public void testCreateFileAndFolderInFolderInVirtualContext() throws Exception
    {
        NodeRef physicalFolder = createFolder(testRootFolder.getNodeRef(),
                                            "FOLDER").getChildRef();
        NodeRef virtualFolder = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                                        VIRTUAL_FOLDER_2_NAME,
                                                        TEST_TEMPLATE_6_JSON_SYS_PATH);
        NodeRef node1 = nodeService.getChildByName(virtualFolder,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Node1");
        assertNotNull(node1);

        NodeRef physicalFolderInVirtualContext = nodeService.getChildByName(node1,
                                                                            ContentModel.ASSOC_CONTAINS,
                                                                            "FOLDER");
        assertNotNull(physicalFolderInVirtualContext);

        createContent(physicalFolderInVirtualContext,
                      "testFile1",
                      "0",
                      MimetypeMap.MIMETYPE_TEXT_PLAIN,
                      "UTF-8");

      NodeRef childFileNodeRef = nodeService.getChildByName(physicalFolderInVirtualContext,ContentModel.ASSOC_CONTAINS, "testFile1");
      assertNotNull(childFileNodeRef);

      childFileNodeRef = nodeService.getChildByName(physicalFolder,ContentModel.ASSOC_CONTAINS, "testFile1");
      assertNotNull(childFileNodeRef);

      createFolder(physicalFolderInVirtualContext, "testFolder1");

      NodeRef childFolderNodeRef = nodeService.getChildByName(physicalFolderInVirtualContext,ContentModel.ASSOC_CONTAINS, "testFolder1");
      assertNotNull(childFolderNodeRef);

      childFolderNodeRef = nodeService.getChildByName(physicalFolder,ContentModel.ASSOC_CONTAINS, "testFolder1");
      assertNotNull(childFolderNodeRef);
    }

    public void testChildByName_ACE_4700() throws Exception
    {
        NodeRef node1 = nodeService.getChildByName(virtualFolder1NodeRef,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Node1");
        assertNotNull(node1);
        String filename = "2015-11_11_1557_folder_empty_space.txt";
        fileAndFolderService.create(node1,
                                    filename,
                                    ContentModel.TYPE_CONTENT);
        fileAndFolderService.create(node1,
                                    filename,
                                    ContentModel.TYPE_CONTENT);
        assertNotNull(nodeService.getChildByName(node1,
                                                 ContentModel.ASSOC_CONTAINS,
                                                 "2015-11_11_1557_folder_empty_space.txt"));
        assertNotNull(nodeService.getChildByName(node1,
                                                 ContentModel.ASSOC_CONTAINS,
                                                 "2015-11_11_1557_folder_empty_space-1.txt"));
        assertNull(nodeService.getChildByName(node1,
                                              ContentModel.ASSOC_CONTAINS,
                                              "2015-11_11_1557_folder_empty_space-2.txt"));

        String suportedCharsFileName = "file~!@#$%^&-=+][';.,.txt";
        String suportedCharsFileName1 = "file~!@#$%^&-=+][';.,-1.txt";
        String suportedCharsFileName2 = "file~!@#$%^&-=+][';.,-2.txt";
        fileAndFolderService.create(node1,
                                    suportedCharsFileName,
                                    ContentModel.TYPE_CONTENT);
        fileAndFolderService.create(node1,
                                    suportedCharsFileName,
                                    ContentModel.TYPE_CONTENT);
        assertNotNull(nodeService.getChildByName(node1,
                                                 ContentModel.ASSOC_CONTAINS,
                                                 suportedCharsFileName));
        assertNotNull(nodeService.getChildByName(node1,
                                                 ContentModel.ASSOC_CONTAINS,
                                                 suportedCharsFileName1));
        assertNull(nodeService.getChildByName(node1,
                                              ContentModel.ASSOC_CONTAINS,
                                              suportedCharsFileName2));
    }

    public void testHasAspect() throws Exception
    {
        // test for virtual folder
        NodeRef node1 = nodeService.getChildByName(virtualFolder1NodeRef,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Node1");
        assertNotNull(node1);
        assertTrue(nodeService.hasAspect(node1,
                                         VirtualContentModel.ASPECT_VIRTUAL));
        assertFalse(nodeService.hasAspect(node1,
                                          VirtualContentModel.ASPECT_VIRTUAL_DOCUMENT));

        // test for document in virtual context
        String filename = "testName.txt";
        NodeRef fileNodeRef = fileAndFolderService.create(node1,
                                                          filename,
                                                          ContentModel.TYPE_CONTENT).getNodeRef();
        assertFalse(nodeService.hasAspect(fileNodeRef,
                                          VirtualContentModel.ASPECT_VIRTUAL));
        assertTrue(nodeService.hasAspect(fileNodeRef,
                                         VirtualContentModel.ASPECT_VIRTUAL_DOCUMENT));

        // test for folder in virtual context
        createFolder(testRootFolder.getNodeRef(),
                                              "FOLDER").getChildRef();
        NodeRef virtualFolder = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                                        VIRTUAL_FOLDER_2_NAME,
                                                        TEST_TEMPLATE_6_JSON_SYS_PATH);
        NodeRef node1_1 = nodeService.getChildByName(virtualFolder,
                                                     ContentModel.ASSOC_CONTAINS,
                                                     "Node1");
        assertNotNull(node1_1);

        NodeRef physicalFolderInVirtualContext = nodeService.getChildByName(node1_1,
                                                                            ContentModel.ASSOC_CONTAINS,
                                                                            "FOLDER");
        assertNotNull(physicalFolderInVirtualContext);

        assertFalse(nodeService.hasAspect(physicalFolderInVirtualContext,
                                          VirtualContentModel.ASPECT_VIRTUAL));
        assertTrue(nodeService.hasAspect(physicalFolderInVirtualContext,
                                         VirtualContentModel.ASPECT_VIRTUAL_DOCUMENT));

        //test the document created in a folder in virtual context
        createContent(physicalFolderInVirtualContext,
                      "testFile1",
                      "0",
                      MimetypeMap.MIMETYPE_TEXT_PLAIN,
                      "UTF-8");

        NodeRef childFileNodeRef = nodeService.getChildByName(physicalFolderInVirtualContext,
                                                              ContentModel.ASSOC_CONTAINS,
                                                              "testFile1");
        assertNotNull(childFileNodeRef);
        assertFalse(nodeService.hasAspect(childFileNodeRef,
                                          VirtualContentModel.ASPECT_VIRTUAL));
        assertFalse(nodeService.hasAspect(childFileNodeRef,
                                         VirtualContentModel.ASPECT_VIRTUAL_DOCUMENT));

    }

    private NodeRef createDownloadNode()
    {
        NodeRef createDownloadNode = downloadStorage.createDownloadNode(true);
        return createDownloadNode;
    }
}
