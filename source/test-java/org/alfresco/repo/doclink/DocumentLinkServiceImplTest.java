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
package org.alfresco.repo.doclink;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.DeleteLinksStatusReport;
import org.alfresco.service.cmr.repository.DocumentLinkService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.util.I18NUtil;

import junit.framework.TestCase;

/**
 * Test cases for {@link DocumentLinkServiceImpl}.
 * 
 * @author Ana Bozianu
 * @since 5.1
 */

public class DocumentLinkServiceImplTest extends TestCase
{

    private static final ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    private static final String TEST_USER = DocumentLinkServiceImplTest.class.getSimpleName() + "_testuser";
    
    private UserTransaction txn;
    
    private TransactionService transactionService;
    private DocumentLinkService documentLinkService;
    private PermissionService permissionService;
    private PersonService personService;
    private SiteService siteService;
    private FileFolderService fileFolderService;
    private NodeService nodeService;
    private CheckOutCheckInService cociService;

    // nodes the test user has read/write permission to
    private NodeRef site1;
    private NodeRef site1File1;
    private NodeRef site1File2; // do not create links of this file
    private NodeRef site1Folder1;
    private NodeRef site1Folder2;
    private NodeRef site1Folder3;

    // nodes the test user has no permission to
    private NodeRef site2File;
    private NodeRef site2Folder1;
    private NodeRef site2Folder2;
    private NodeRef linkOfFile1Site2;
    
    private String site1File1Name = GUID.generate();
    private String site1Folder1Name = GUID.generate();;

    @Override
    public void setUp() throws Exception
    {
        // Set up the services
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
        transactionService = serviceRegistry.getTransactionService();
        documentLinkService = serviceRegistry.getDocumentLinkService();
        permissionService = serviceRegistry.getPermissionService();
        personService = serviceRegistry.getPersonService();
        siteService = serviceRegistry.getSiteService();
        fileFolderService = serviceRegistry.getFileFolderService();
        nodeService = serviceRegistry.getNodeService();
        cociService = serviceRegistry.getCheckOutCheckInService();

        // Start the transaction
        txn = transactionService.getUserTransaction();
        txn.begin();

        // Authenticate
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        /* Create the test user */
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_USERNAME, TEST_USER);
        personService.createPerson(props);

        /*
         * Create the working test root 1 to which the user has read/write
         * permission
         */
        site1 = siteService.createSite("site1", GUID.generate(), "myTitle", "myDescription", SiteVisibility.PUBLIC).getNodeRef();
        permissionService.setPermission(site1, TEST_USER, PermissionService.ALL_PERMISSIONS, true);
        site1Folder1 = fileFolderService.create(site1, site1Folder1Name, ContentModel.TYPE_FOLDER).getNodeRef();
        site1File1 = fileFolderService.create(site1Folder1, site1File1Name, ContentModel.TYPE_CONTENT).getNodeRef();
        site1File2 = fileFolderService.create(site1Folder1, GUID.generate(), ContentModel.TYPE_CONTENT).getNodeRef();
        site1Folder2 = fileFolderService.create(site1, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
        site1Folder3 = fileFolderService.create(site1, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
        // create a link of site1File1 in site1Folder3 to test regular deletion
         documentLinkService.createDocumentLink(site1File2, site1Folder3);

        /* Create the working test root 2 to which the user has no permission */
        NodeRef site2 = siteService.createSite("site2", GUID.generate(), "myTitle", "myDescription", SiteVisibility.PRIVATE).getNodeRef();
        permissionService.setPermission(site2, TEST_USER, PermissionService.ALL_PERMISSIONS, false);
        site2File = fileFolderService.create(site2, GUID.generate(), ContentModel.TYPE_CONTENT).getNodeRef();
        site2Folder1 = fileFolderService.create(site2, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
        site2Folder2 = fileFolderService.create(site2, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
        // Create a link of site1File1 in site2Folder2 to test the deletion
        // without permission
        linkOfFile1Site2 = documentLinkService.createDocumentLink(site1File2, site2Folder2);
    }

    @Override
    protected void tearDown() throws Exception
    {
        try
        {
            if (txn.getStatus() != Status.STATUS_ROLLEDBACK && txn.getStatus() != Status.STATUS_COMMITTED)
            {
                txn.rollback();
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Tests the creation of a file link
     * 
     * @throws Exception
     */
    public void testCreateFileLink() throws Exception
    {
        // create a link of file site1File1_1 in folder site1Folder2
        NodeRef linkNodeRef = documentLinkService.createDocumentLink(site1File1, site1Folder2);
        assertNotNull(linkNodeRef);

        // test if the link node is listed as a child of site1Folder2
        String site1File1LinkName =  I18NUtil.getMessage("doclink_service.link_to_label", (site1File1Name + ".url"));
        NodeRef linkNodeRef2 = fileFolderService.searchSimple(site1Folder2, site1File1LinkName);
        assertNotNull(linkNodeRef2);
        assertEquals(linkNodeRef, linkNodeRef2);

        // check the type of the link
        assertEquals(nodeService.getType(linkNodeRef), ApplicationModel.TYPE_FILELINK);

        // check if the link destination is site1File1_1
        NodeRef linkDestination = documentLinkService.getLinkDestination(linkNodeRef);
        assertNotNull(linkDestination);
        assertEquals(linkDestination, site1File1);
    }

    /**
     * Tests the creation of a folder link
     * 
     * @throws Exception
     */
    public void testCreateFolderLink() throws Exception
    {
        // create a link of file site1File1_1 in folder site1Folder2
        NodeRef linkNodeRef = documentLinkService.createDocumentLink(site1Folder1, site1Folder2);
        assertNotNull(linkNodeRef);

        // test if the link node is listed as a child of site1Folder2
        String site1Folder1LinkName =  I18NUtil.getMessage("doclink_service.link_to_label", (site1Folder1Name + ".url"));
        NodeRef linkNodeRef2 = fileFolderService.searchSimple(site1Folder2, site1Folder1LinkName);
        assertNotNull(linkNodeRef2);
        assertEquals(linkNodeRef, linkNodeRef2);

        // check the type of the link
        assertEquals(nodeService.getType(linkNodeRef), ApplicationModel.TYPE_FOLDERLINK);

        // check if the link destination is site1File1_1
        NodeRef linkDestination = documentLinkService.getLinkDestination(linkNodeRef);
        assertNotNull(linkDestination);
        assertEquals(linkDestination, site1Folder1);
    }

    /**
     * Tests the behavior of createDocumentLink when providing null arguments
     * 
     * @throws Exception
     */
    public void testCreateDocumentWithNullArguments() throws Exception
    {
        try
        {
            documentLinkService.createDocumentLink(null, null);
            fail("null arguments must generate AlfrescoRuntimeException.");
        }
        catch (AlfrescoRuntimeException e)
        {
            // Expected
        }
    }

    /**
     * Test the behavior of createDocumentLink when provided a file as a
     * destination
     * 
     * @throws Exception
     */
    public void testInvalidDestination() throws Exception
    {
        try
        {
            documentLinkService.createDocumentLink(site1Folder2, site1File1);
            fail("invalid destination argument must generate IllegalArgumentException.");
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }
    }

    /**
     * Tests the creation of a document link when the user doesn't have read
     * permission on the source node
     * 
     * @throws Exception
     */
    public void testCreateDocLinkWithoutReadPermissionOnSource() throws Exception
    {
        try
        {
            AuthenticationUtil.runAs(new RunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    documentLinkService.createDocumentLink(site2File, site1Folder2);
                    return null;
                }
            }, TEST_USER);

            fail("no read permission on the source node must generate AccessDeniedException.");
        }
        catch (AccessDeniedException ex)
        {
            // Expected
        }
    }

    /**
     * Tests the creation of a document link when the user doesn't have write
     * permission on the destination node
     * 
     * @throws Exception
     */
    public void testCreateDocLinkWithoutWritePermissionOnDestination() throws Exception
    {
        try
        {
            AuthenticationUtil.runAs(new RunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    documentLinkService.createDocumentLink(site1File1, site2Folder1);
                    return null;
                }
            }, TEST_USER);

            fail("no write permission on the destination node must generate AccessDeniedException.");
        }
        catch (AccessDeniedException ex)
        {
            // Expected
        }
    }

    /**
     * Tests the deletion of a document's links, with and without write
     * permissions
     * 
     * @throws Exception
     */
    public void testDeleteLinks() throws Exception
    {
        DeleteLinksStatusReport report = AuthenticationUtil.runAs(new RunAsWork<DeleteLinksStatusReport>()
        {
            @Override
            public DeleteLinksStatusReport doWork() throws Exception
            {
                return documentLinkService.deleteLinksToDocument(site1File2);
            }
        }, TEST_USER);

        // check if the service found 2 links of the document
        assertEquals(2, report.getTotalLinksFoundCount());

        // check if the service successfully deleted one
        assertEquals(1, report.getDeletedLinksCount());

        assertEquals(true, nodeService.hasAspect(site1File2, ApplicationModel.ASPECT_LINKED));

        // check if the second one failed with access denied
        Throwable ex = report.getErrorDetails().get(linkOfFile1Site2);
        assertNotNull(ex);
        assertEquals(ex.getClass(), AccessDeniedException.class);
    }

    /**
     * Tests the creation of a Site link, an locked node or a checked out node
     * 
     * @throws Exception
     */
    public void testCreateLinksNotAllowed() throws Exception
    {
        NodeRef invalidLinkNodeRef;

        // Create link for Site
        try
        {
            invalidLinkNodeRef = documentLinkService.createDocumentLink(site1, site2Folder1);
            fail("unsupported source node type : " + nodeService.getType(site1));
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }

        NodeRef firstLinkNodeRef = documentLinkService.createDocumentLink(site1File1, site1Folder1);
        assertEquals(true, nodeService.hasAspect(site1File1, ApplicationModel.ASPECT_LINKED));

        // Create link for working copy
        NodeRef workingCopyNodeRef = cociService.checkout(site1File1);
        try
        {
            invalidLinkNodeRef = documentLinkService.createDocumentLink(workingCopyNodeRef, site1Folder1);
            fail("Cannot perform operation since the node (id:" + workingCopyNodeRef.getId() + ") is locked.");
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }

        // Create link for locked node (original)
        try
        {
            invalidLinkNodeRef = documentLinkService.createDocumentLink(site1File1, site1Folder1);
            fail("Cannot perform operation since the node (id:" + site1File1.getId() + ") is locked.");
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }

        // Even the node is locked, user can delete previous created links
        nodeService.deleteNode(firstLinkNodeRef);
        assertEquals(false, nodeService.hasAspect(site1File1, ApplicationModel.ASPECT_LINKED));

        cociService.cancelCheckout(workingCopyNodeRef);
    }

}
