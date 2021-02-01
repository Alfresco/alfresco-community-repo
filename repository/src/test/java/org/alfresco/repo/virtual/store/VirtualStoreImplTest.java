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

package org.alfresco.repo.virtual.store;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.repo.virtual.VirtualizationIntegrationTest;
import org.alfresco.repo.virtual.ref.Encodings;
import org.alfresco.repo.virtual.ref.Protocols;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.repo.virtual.ref.VanillaProtocol;
import org.alfresco.repo.virtual.template.ApplyTemplateMethodTest;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

public class VirtualStoreImplTest extends VirtualizationIntegrationTest
{
    private static Log logger = LogFactory.getLog(VirtualStoreImplTest.class);

    private VirtualStoreImpl smartStore;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        smartStore = ctx.getBean("smartStore",
                                   VirtualStoreImpl.class);

    }

    @Test
    public void testResolveVirtualFolderDefinition_inactiveSynchronization() throws Exception
    {
        txnTamperHint = "VirtualStoreImplTest::testResolveVirtualFolderDefinition_inactiveSynchronization";
        txn.rollback();
        NodeRef ntVirtualizedFolder = null;
        NodeRef jsonTemplateContent = null;
        try
        {
            final String templateName = "template1.json";
            jsonTemplateContent = nodeService.getChildByName(companyHomeNodeRef,
                                                             ContentModel.ASSOC_CONTAINS,
                                                             templateName);
            if (jsonTemplateContent == null)
            {
                ChildAssociationRef templateChild = createContent(companyHomeNodeRef,
                                                                  templateName,
                                                                  ApplyTemplateMethodTest.class
                                                                              .getResourceAsStream(TEST_TEMPLATE_1_JSON_NAME),
                                                                  MimetypeMap.MIMETYPE_JSON,
                                                                  StandardCharsets.UTF_8.name());
                jsonTemplateContent = templateChild.getChildRef();
            }

            final String folderName = "testCanVirtualize_nonTransactional";
            ntVirtualizedFolder = nodeService.getChildByName(companyHomeNodeRef,
                                                             ContentModel.ASSOC_CONTAINS,
                                                             folderName);
            if (ntVirtualizedFolder == null)
            {
                ChildAssociationRef folderChild = createFolder(companyHomeNodeRef,
                                                               folderName);
                ntVirtualizedFolder = folderChild.getChildRef();
            }

            Reference aVanillaRef = ((VanillaProtocol) Protocols.VANILLA.protocol)
                        .newReference(VANILLA_PROCESSOR_JS_CLASSPATH,
                                      "/1",
                                      ntVirtualizedFolder,
                                      jsonTemplateContent);

            // We use transactional-synchronized resources for caching. In
            // non-transactional contexts they might not be available.
            smartStore.resolveVirtualFolderDefinition(aVanillaRef);

        }
        finally
        {

            txn = transactionService.getUserTransaction();
            txn.begin();
            if (ntVirtualizedFolder != null)
            {
                nodeService.deleteNode(ntVirtualizedFolder);
            }

            if (jsonTemplateContent != null)
            {
                nodeService.deleteNode(jsonTemplateContent);
            }
            txn.commit();
        }
    }

    @Test
    public void testNonVirtualizable() throws Exception
    {
        NodeRef aNodeRef = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                                   "TestVirtualStoreImpl_createVirtualizedFolder",
                                                   null);
        assertFalse(smartStore.canVirtualize(aNodeRef));

        try
        {
            smartStore.virtualize(aNodeRef);
            fail("Should not be able to virtualize non-virtualizable nodes.");
        }
        catch (VirtualizationException e)
        {
            logger.info(e);
        }
    }

    @Test
    public void testCanVirtualize() throws Exception
    {
        NodeRef solrFacetsNodeRef = new NodeRef("workspace://SpacesStore/solr_facets_root_space");
        boolean canVirtualize = smartStore.canVirtualize(solrFacetsNodeRef);
        assertEquals(false,
                     canVirtualize);
    }

    @Test
    public void testCanCreateFolderNamedV() throws Exception
    {
        // note: see Reference.VIRTUAL_TOKEN
        String v = "v";

        Reference reference = Reference.fromNodeRef(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, v));
        assertNull(reference);
        
        ChildAssociationRef folderChild = createFolder(companyHomeNodeRef, v);
        assertNotNull(folderChild);
    }

    private String asTypedPermission(String perm)
    {
        return smartStore.getUserPermissions().getPermissionTypeQName() + "." + perm;
    }

    private void assertHasQueryNodePermission(AccessStatus accessStatus, String perm)
    {
        VirtualUserPermissions virtualUserPermissions = smartStore.getUserPermissions();

        assertEquals(AccessStatus.DENIED,
                     virtualUserPermissions.hasQueryNodePermission(perm));
        assertEquals(AccessStatus.DENIED,
                     virtualUserPermissions.hasQueryNodePermission(asTypedPermission(perm)));
    }

    private void assertHasVirtualNodePermission(AccessStatus accessStatus, String perm, boolean readonly)
    {
        VirtualUserPermissions virtualUserPermissions = smartStore.getUserPermissions();

        assertEquals(AccessStatus.DENIED,
                     virtualUserPermissions.hasVirtualNodePermission(perm,
                                                                     readonly));
        assertEquals(AccessStatus.DENIED,
                     virtualUserPermissions.hasVirtualNodePermission(asTypedPermission(perm),
                                                                     readonly));
    }

    @Test
    public void testConfiguredUserPermissions() throws Exception
    {
        assertHasQueryNodePermission(AccessStatus.DENIED,
                                     PermissionService.DELETE);
        assertHasQueryNodePermission(AccessStatus.DENIED,
                                     PermissionService.DELETE_NODE);
        assertHasQueryNodePermission(AccessStatus.DENIED,
                                     PermissionService.CHANGE_PERMISSIONS);

        assertHasVirtualNodePermission(AccessStatus.DENIED,
                                       PermissionService.CREATE_ASSOCIATIONS,
                                       true);
        assertHasVirtualNodePermission(AccessStatus.DENIED,
                                       PermissionService.UNLOCK,
                                       true);
        assertHasVirtualNodePermission(AccessStatus.DENIED,
                                       PermissionService.CANCEL_CHECK_OUT,
                                       true);
        assertHasVirtualNodePermission(AccessStatus.DENIED,
                                       PermissionService.DELETE,
                                       true);
        assertHasVirtualNodePermission(AccessStatus.DENIED,
                                       PermissionService.DELETE_NODE,
                                       true);
        assertHasVirtualNodePermission(AccessStatus.DENIED,
                                       PermissionService.CHANGE_PERMISSIONS,
                                       true);
        assertHasVirtualNodePermission(AccessStatus.DENIED,
                                       PermissionService.WRITE_CONTENT,
                                       true);
        assertHasVirtualNodePermission(AccessStatus.DENIED,
                                       PermissionService.WRITE,
                                       true);
        assertHasVirtualNodePermission(AccessStatus.DENIED,
                                       PermissionService.WRITE_PROPERTIES,
                                       true);
        assertHasVirtualNodePermission(AccessStatus.DENIED,
                                       PermissionService.WRITE,
                                       false);
        assertHasVirtualNodePermission(AccessStatus.DENIED,
                                       PermissionService.WRITE_PROPERTIES,
                                       false);

    }

    // MNT-17845
    @Test
    public void testCanExistNodeIDwithV()
    {
        createAndCheckNodeId("nfile", "specialFile1.txt");

        createAndCheckNodeId("vfile", "specialFile2.txt");

        // v0...
        createAndCheckNodeId("v"+Encodings.ZERO.encoding.token+"file", "specialFile3.txt");

        // vH...
        createAndCheckNodeId("v"+Encodings.HASH.encoding.token+"file", "specialFile4.txt");

        // vp...
        createAndCheckNodeId("v"+Encodings.PLAIN.encoding.token+"file", "specialFile5.txt");

        // MNT-21968
        createAndCheckNodeId("v"+Encodings.ZERO.encoding.token+"0Draft.pdf", "specialFile6.txt");

        NodeRef virtualFolder = createVirtualizedFolder(testRootFolder.getNodeRef(), VIRTUAL_FOLDER_3_NAME, TEST_TEMPLATE_4_JSON_SYS_PATH);

        assertTrue(smartStore.canVirtualize(virtualFolder));
        virtualFolder = smartStore.virtualize(virtualFolder).toNodeRef();
        Reference reference = Reference.fromNodeRef(virtualFolder);
        assertNotNull(reference);
        assertTrue(nodeService.exists(virtualFolder));
    }

    private void createAndCheckNodeId(String nodeId, String nodeName)
    {
        Map<QName, Serializable> props = new HashMap<>(1);
        props.put(ContentModel.PROP_NODE_UUID, nodeId);
        props.put(ContentModel.PROP_NAME, nodeName);

        QName assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(nodeName));
        NodeRef nfileNodeRef = nodeService.createNode(companyHomeNodeRef, ContentModel.ASSOC_CONTAINS, assocQName, ContentModel.TYPE_CONTENT, props).getChildRef();

        Reference reference =  Reference.fromNodeRef(nfileNodeRef);
        assertNull(reference);
        assertTrue(nodeService.exists(nfileNodeRef));
    }
}
