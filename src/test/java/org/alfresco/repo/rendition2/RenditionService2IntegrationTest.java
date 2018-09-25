/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.repo.rendition2;

import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;

import static java.lang.Thread.sleep;
import static org.alfresco.model.ContentModel.PROP_CONTENT;

/**
 * Integration tests for {@link RenditionService2}
 */
public class RenditionService2IntegrationTest extends AbstractRenditionIntegrationTest
{
    @Autowired
    private RenditionService2Impl renditionService2;
    @Autowired
    private TransformClient transformClient;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private RenditionService renditionService;

    private static final String ADMIN = "admin";
    private static final String DOC_LIB = "doclib";

    @Before
    public void setUp()
    {
        assertTrue("The RenditionService2 needs to be enabled", renditionService2.isEnabled());
        assertTrue("A wrong type of transform client detected", transformClient instanceof LocalTransformClient);
    }

    @After
    public void cleanUp()
    {
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    private void checkRendition(String testFileName, String renditionName, boolean expectedToPass)
    {
        try
        {
            NodeRef sourceNodeRef = createSource(ADMIN, testFileName);
            render(ADMIN, sourceNodeRef, renditionName);
            waitForRendition(ADMIN, sourceNodeRef, renditionName);
        }
        catch(UnsupportedOperationException uoe)
        {
            if (expectedToPass)
            {
                fail("The " + renditionName + " rendition should be supported for " + testFileName);
            }
        }
    }

    // Creates a new source node as the given user in its own transaction.
    private NodeRef createSource(String user, String testFileName)
    {
        return AuthenticationUtil.runAs(() ->
                transactionService.getRetryingTransactionHelper().doInTransaction(() ->
                        createSource(testFileName)), user);
    }

    // Creates a new source node as the current user in the current transaction.
    private NodeRef createSource(String testFileName) throws FileNotFoundException
    {
        return createContentNodeFromQuickFile(testFileName);
    }

    // Changes the content of a source node as the given user in its own transaction.
    private void updateContent(String user, NodeRef sourceNodeRef, String testFileName)
    {
        AuthenticationUtil.runAs((AuthenticationUtil.RunAsWork<Void>) () ->
                transactionService.getRetryingTransactionHelper().doInTransaction(() ->
                {
                    updateContent(sourceNodeRef, testFileName);
                    return null;
                }), user);
    }

    // Changes the content of a source node as the current user in the current transaction.
    private NodeRef updateContent(NodeRef sourceNodeRef, String testFileName) throws FileNotFoundException
    {
        File file = ResourceUtils.getFile("classpath:quick/" + testFileName);
        nodeService.setProperty(sourceNodeRef, ContentModel.PROP_NAME, testFileName);

        ContentWriter contentWriter = contentService.getWriter(sourceNodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter.setMimetype(mimetypeService.guessMimetype(testFileName));
        contentWriter.putContent(file);

        return sourceNodeRef;
    }

    // Clears the content of a source node as the given user in its own transaction.
    private void clearContent(String user, NodeRef sourceNodeRef)
    {
        AuthenticationUtil.runAs((AuthenticationUtil.RunAsWork<Void>) () ->
                transactionService.getRetryingTransactionHelper().doInTransaction(() ->
                {
                    clearContent(sourceNodeRef);
                    return null;
                }), user);
    }

    // Clears the content of a source node as the current user in the current transaction.
    private void clearContent(NodeRef sourceNodeRef)
    {
        nodeService.removeProperty(sourceNodeRef, PROP_CONTENT);
    }

    // Requests a new rendition as the given user in its own transaction.
    private void render(String user, NodeRef sourceNode, String renditionName)
    {
        AuthenticationUtil.runAs((AuthenticationUtil.RunAsWork<Void>) () ->
                transactionService.getRetryingTransactionHelper().doInTransaction(() ->
                {
                    render(sourceNode, renditionName);
                    return null;
                }), user);
    }

    // Requests a new rendition as the current user in the current transaction.
    private void render(NodeRef sourceNodeRef, String renditionName)
    {
        renditionService2.render(sourceNodeRef, renditionName);
    }

    // As a given user waitForRendition for a rendition to appear. Creates new transactions to do this.
    private NodeRef waitForRendition(String user, NodeRef sourceNodeRef, String renditionName)
    {
        return AuthenticationUtil.runAs(() -> waitForRendition(sourceNodeRef, renditionName), user);
    }

    // As the current user waitForRendition for a rendition to appear. Creates new transactions to do this.
    private NodeRef waitForRendition(NodeRef sourceNodeRef, String renditionName) throws InterruptedException
    {
        long maxMillis = 20000;
        ChildAssociationRef assoc = null;
        for (int i = (int)(maxMillis / 500); i >= 0; i--)
        {
            // Must create a new transaction in order to see changes that take place after this method started.
            assoc = transactionService.getRetryingTransactionHelper().doInTransaction(() ->
                    renditionService2.getRenditionByName(sourceNodeRef, renditionName), true, true);
            if (assoc != null)
            {
                break;
            }
            logger.debug("RenditionService2.getRenditionByName(...) sleep "+i);
            sleep(500);
        }
        assertNotNull("Rendition " + renditionName + " failed", assoc);
        return assoc.getChildRef();
    }

    // PDF transformation

    @Test
    public void testLocalRenderPdfToJpegMedium() 
    {
        checkRendition("quick.pdf", "medium", true);
    }

    @Test
    public void testLocalRenderPdfToDoclib() 
    {
        checkRendition("quick.pdf", "doclib", true);
    }

    @Test
    public void testLocalRenderPdfJpegImgpreview() 
    {
        checkRendition("quick.pdf", "imgpreview", true);
    }

    @Test
    public void testLocalRenderPdfPngAvatar() 
    {
        checkRendition("quick.pdf", "avatar", true);
    }

    @Test
    public void testLocalRenderPdfPngAvatar32() 
    {
        checkRendition("quick.pdf", "avatar32", true);
    }

    @Test
    public void testLocalRenderPdfFlashWebpreview() 
    {
        checkRendition("quick.pdf", "webpreview", false);
    }

    // DOCX transformation

    @Test
    public void testLocalRenderDocxJpegMedium() 
    {
        checkRendition("quick.docx", "medium", true);
    }

    @Test
    public void testLocalRenderDocxDoclib() 
    {
        checkRendition("quick.docx", "doclib", true);
    }

    @Test
    public void testLocalRenderDocxJpegImgpreview() 
    {
        checkRendition("quick.docx", "imgpreview", true);
    }

    @Test
    public void testLocalRenderDocxPngAvatar() 
    {
        checkRendition("quick.docx", "avatar", true);
    }

    @Test
    public void testLocalRenderDocxPngAvatar32() 
    {
        checkRendition("quick.docx", "avatar32", true);
    }

    @Test
    public void testLocalRenderDocxFlashWebpreview() 
    {
        checkRendition("quick.docx", "webpreview", false);
    }

    @Test
    public void testLocalRenderDocxPdf() 
    {
        checkRendition("quick.docx", "pdf", true);
    }

    @Test
    public void basicRendition()
    {
        NodeRef sourceNodeRef = createSource(ADMIN, "quick.jpg");
        render(ADMIN, sourceNodeRef, DOC_LIB);
        waitForRendition(ADMIN, sourceNodeRef, DOC_LIB);
    }

    @Test
    public void changedSourceToNullContent() 
    {
        NodeRef sourceNodeRef = createSource(ADMIN, "quick.jpg");
        render(ADMIN, sourceNodeRef, DOC_LIB);
        waitForRendition(ADMIN, sourceNodeRef, DOC_LIB);

        clearContent(ADMIN, sourceNodeRef);
        render(ADMIN, sourceNodeRef, DOC_LIB);
        ChildAssociationRef assoc = AuthenticationUtil.runAs(() ->
                renditionService2.getRenditionByName(sourceNodeRef, DOC_LIB), ADMIN);
        assertNull("There should be no rendition as there was no content", assoc);
    }

    @Test
    public void changedSourceToNonNull() 
    {
        NodeRef sourceNodeRef = createSource(ADMIN, "quick.jpg");
        render(ADMIN, sourceNodeRef, DOC_LIB);
        waitForRendition(ADMIN, sourceNodeRef, DOC_LIB);

        clearContent(ADMIN, sourceNodeRef);
        render(ADMIN, sourceNodeRef, DOC_LIB);
        ChildAssociationRef assoc = AuthenticationUtil.runAs(() ->
                renditionService2.getRenditionByName(sourceNodeRef, DOC_LIB), ADMIN);
        assertNull("There should be no rendition as there was no content", assoc);

        updateContent(ADMIN, sourceNodeRef, "quick.png");
        waitForRendition(ADMIN, sourceNodeRef, DOC_LIB);
    }

    @Test
    public void testCreateRenditionByUser() 
    {
        String userName = createRandomUser();
        NodeRef sourceNodeRef = createSource(userName, "quick.jpg");
        render(userName, sourceNodeRef, DOC_LIB);
        NodeRef renditionNodeRef = waitForRendition(userName, sourceNodeRef, DOC_LIB);
        assertNotNull("The rendition was not generated for non-admin user", renditionNodeRef);
    }

    @Test
    public void testReadRenditionByOtherUser() 
    {
        String ownerUserName = createRandomUser();
        NodeRef sourceNodeRef = createSource(ownerUserName, "quick.jpg");
        String otherUserName = createRandomUser();
        permissionService.setPermission(sourceNodeRef, otherUserName, PermissionService.READ, true);
        render(ownerUserName, sourceNodeRef, DOC_LIB);
        NodeRef renditionNodeRef = waitForRendition(ownerUserName, sourceNodeRef, DOC_LIB);
        assertNotNull("The rendition is not visible for owner of source node", renditionNodeRef);
        renditionNodeRef = waitForRendition(otherUserName, sourceNodeRef, DOC_LIB);
        assertNotNull("The rendition is not visible for non-owner user with read permissions", renditionNodeRef);
        assertEquals("The creator of the rendition is not correct",
                ownerUserName, nodeService.getProperty(sourceNodeRef, ContentModel.PROP_CREATOR));
    }

    @Test
    public void testRenderByReader() 
    {
        String ownerUserName = createRandomUser();
        NodeRef sourceNodeRef = createSource(ownerUserName, "quick.jpg");
        String otherUserName = createRandomUser();
        permissionService.setPermission(sourceNodeRef, otherUserName, PermissionService.READ, true);
        render(otherUserName, sourceNodeRef, DOC_LIB);
        NodeRef renditionNodeRef = waitForRendition(ownerUserName, sourceNodeRef, DOC_LIB);
        assertNotNull("The rendition is not visible for owner of source node", renditionNodeRef);
        renditionNodeRef = waitForRendition(otherUserName, sourceNodeRef, DOC_LIB);
        assertNotNull("The rendition is not visible for owner of rendition node", renditionNodeRef);
        assertEquals("The creator of the rendition is not correct",
                ownerUserName, nodeService.getProperty(sourceNodeRef, ContentModel.PROP_CREATOR));
    }

    @Test
    public void testAccessWithNoPermissions() 
    {
        String ownerUserName = createRandomUser();
        NodeRef sourceNodeRef = createSource(ownerUserName, "quick.jpg");
        render(ownerUserName, sourceNodeRef, DOC_LIB);
        String noPermissionsUser = createRandomUser();
        permissionService.setPermission(sourceNodeRef, noPermissionsUser, PermissionService.ALL_PERMISSIONS, false);
        try
        {
            waitForRendition(noPermissionsUser, sourceNodeRef, DOC_LIB);
            fail("The rendition should not be visible for user with no permissions");
        }
        catch (AccessDeniedException ade)
        {
            // expected
        }
    }

    @Test
    public void testUpgradeRenditionService() throws InterruptedException
    {
        String ownerUserName = createRandomUser();
        NodeRef sourceNodeRef = createSource(ownerUserName, "quick.jpg");
        final QName doclibRendDefQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "doclib");
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() ->
                AuthenticationUtil.runAs(() ->
                        renditionService.render(sourceNodeRef, doclibRendDefQName), ownerUserName));

        NodeRef oldRendition = AuthenticationUtil.runAs(() ->
                renditionService.getRenditionByName(sourceNodeRef, doclibRendDefQName).getChildRef(), ownerUserName);
        assertFalse("The rendition should be generated by old Rendition Service",
                AuthenticationUtil.runAs(() -> nodeService.hasAspect(oldRendition, RenditionModel.ASPECT_RENDITION2), ownerUserName));

        updateContent(ownerUserName, sourceNodeRef, "quick.png");
        NodeRef newRendition = waitForRendition(ownerUserName, sourceNodeRef, DOC_LIB);
        assertNotNull("The rendition should be reported via RenditionService2", newRendition);
        Thread.sleep(200);
        boolean hasRenditionedAspect = false;
        for (int i = 0; i < 5; i++)
        {
            hasRenditionedAspect = AuthenticationUtil.runAs(() -> nodeService.hasAspect(newRendition, RenditionModel.ASPECT_RENDITION2), ownerUserName);
            if (hasRenditionedAspect)
            {
                break;
            }
            else
            {
                Thread.sleep(500);
            }
        }
        assertTrue("The rendition should be generated by new Rendition Service", hasRenditionedAspect);
    }

    /**
     * @deprecated can be removed when we remove the original RenditionService
     */
    @Deprecated
    @Test
    public void testUseOldService() throws InterruptedException
    {
        renditionService2.setEnabled(false);
        try
        {
            NodeRef sourceNodeRef = createSource(ADMIN, "quick.jpg");
            final QName doclibRendDefQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "doclib");
            transactionService.getRetryingTransactionHelper()
                    .doInTransaction(() ->
                            AuthenticationUtil.runAs(() ->
                                    renditionService.render(sourceNodeRef, doclibRendDefQName), ADMIN));
            assertNotNull("The old renditions service did not render", waitForRendition(ADMIN, sourceNodeRef, DOC_LIB));
            List<String> lastThumbnailModification = transactionService.getRetryingTransactionHelper()
                    .doInTransaction(() ->
                            AuthenticationUtil.runAs(() ->
                                    (List<String>) nodeService.getProperty(sourceNodeRef, ContentModel.PROP_LAST_THUMBNAIL_MODIFICATION_DATA), ADMIN));
            updateContent(ADMIN, sourceNodeRef, "quick.png");
            List<String> newThumbnailModification = null;
            for (int i = 0; i < 5; i++)
            {
                newThumbnailModification = transactionService.getRetryingTransactionHelper()
                        .doInTransaction(() ->
                                AuthenticationUtil.runAs(() ->
                                        (List<String>) nodeService.getProperty(sourceNodeRef, ContentModel.PROP_LAST_THUMBNAIL_MODIFICATION_DATA), ADMIN));
                if (!newThumbnailModification.equals(lastThumbnailModification))
                {
                    break;
                }
                else
                {
                    Thread.sleep(500);
                }
            }
            assertFalse("The old rendition service did not update the rendition.", newThumbnailModification.equals(lastThumbnailModification));
            NodeRef renditionNodeRef = AuthenticationUtil.runAs(() -> renditionService.getRenditionByName(sourceNodeRef, doclibRendDefQName).getChildRef(), ADMIN);
            assertFalse("The rendition should be rendered by the old rendition service",
                    AuthenticationUtil.runAs(() -> nodeService.hasAspect(renditionNodeRef, RenditionModel.ASPECT_RENDITION2), ADMIN));
        }
        finally
        {
            renditionService2.setEnabled(true);
        }
    }

    /**
     * @deprecated can be removed when we remove the original RenditionService
     */
    @Deprecated
    @Test
    public void testSwitchBackToOldService() throws InterruptedException
    {
        NodeRef sourceNodeRef = createSource(ADMIN, "quick.jpg");
        render(ADMIN, sourceNodeRef, DOC_LIB);
        waitForRendition(ADMIN, sourceNodeRef, DOC_LIB);

        renditionService2.setEnabled(false);
        try
        {
            updateContent(ADMIN, sourceNodeRef, "quick.png");
            Thread.sleep(200);
            NodeRef renditionNodeRef = waitForRendition(ADMIN, sourceNodeRef, DOC_LIB);
            boolean hasRendition2Aspect = true;
            for (int i = 0; i < 5; i++)
            {
                hasRendition2Aspect = AuthenticationUtil.runAs(() -> nodeService.hasAspect(renditionNodeRef, RenditionModel.ASPECT_RENDITION2), ADMIN);
                if (!hasRendition2Aspect)
                {
                    break;
                }
                else
                {
                    Thread.sleep(500);
                }
            }
            assertFalse("Should have switched to the old rendition service", hasRendition2Aspect);
        }
        finally
        {
            renditionService2.setEnabled(true);
        }
    }

    /**
     * @deprecated can be removed when we remove the original RenditionService
     */
    @Deprecated
    @Test
    public void testSwitchToNewServiceViaContentUpdate()
    {
        renditionService2.setEnabled(false);
        try
        {
            NodeRef sourceNodeRef = createSource(ADMIN, "quick.jpg");
            final QName doclibRendDefQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "doclib");
            transactionService.getRetryingTransactionHelper()
                    .doInTransaction(() ->
                            AuthenticationUtil.runAs(() ->
                                    renditionService.render(sourceNodeRef, doclibRendDefQName), ADMIN));
            waitForRendition(ADMIN, sourceNodeRef, DOC_LIB);

            renditionService2.setEnabled(true);

            updateContent(ADMIN, sourceNodeRef, "quick.png");
            NodeRef renditionNodeRef = waitForRendition(ADMIN, sourceNodeRef, DOC_LIB);
            boolean hasAspect = nodeService.hasAspect(renditionNodeRef, RenditionModel.ASPECT_RENDITION2);
            assertFalse("Should have switched to the old rendition service", hasAspect);
        }
        finally
        {
            renditionService2.setEnabled(true);
        }
    }

    /**
     * @deprecated can be removed when we remove the original RenditionService
     */
    @Deprecated
    @Test
    public void testDowngradeRenditionService() throws InterruptedException
    {
        String ownerUserName = createRandomUser();
        NodeRef sourceNodeRef = createSource(ownerUserName, "quick.jpg");
        render(ownerUserName, sourceNodeRef, DOC_LIB);
        NodeRef newRendition = waitForRendition(ownerUserName, sourceNodeRef, DOC_LIB);
        boolean hasRendition2Aspect = AuthenticationUtil.runAs(() -> nodeService.hasAspect(newRendition, RenditionModel.ASPECT_RENDITION2), ownerUserName);
        assertTrue("The source should have the old renditioned aspect",
                AuthenticationUtil.runAs(() -> nodeService.hasAspect(sourceNodeRef, RenditionModel.ASPECT_RENDITIONED), ownerUserName));
        assertTrue("The rendition2 aspect should be present", hasRendition2Aspect);
        try
        {
            renditionService2.setEnabled(false);
            updateContent(ownerUserName, sourceNodeRef, "quick.png");
            NodeRef oldRendition = waitForRendition(ownerUserName, sourceNodeRef, DOC_LIB);
            Thread.sleep(200);
            hasRendition2Aspect = false;
            for (int i = 0; i < 5; i++)
            {
                hasRendition2Aspect = AuthenticationUtil.runAs(() -> nodeService.hasAspect(oldRendition, RenditionModel.ASPECT_RENDITION2), ownerUserName);
                if (!hasRendition2Aspect)
                {
                    break;
                }
                else
                {
                    Thread.sleep(500);
                }
            }
            assertFalse("The rendition should be generated by old Rendition Service", hasRendition2Aspect);
        }
        finally
        {
            renditionService2.setEnabled(true);
        }
    }

    /**
     * @deprecated can be removed when we remove the original RenditionService
     */
    @Deprecated
    @Test
    public void testUpgradeRenditionViaRender() throws InterruptedException
    {
        renditionService2.setEnabled(false);
        try
        {
            NodeRef sourceNodeRef = createSource(ADMIN, "quick.jpg");
            final QName doclibRendDefQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "doclib");
            transactionService.getRetryingTransactionHelper()
                    .doInTransaction(() ->
                            AuthenticationUtil.runAs(() ->
                                    renditionService.render(sourceNodeRef, doclibRendDefQName), ADMIN));
            waitForRendition(ADMIN, sourceNodeRef, DOC_LIB);

            renditionService2.setEnabled(true);
            render(ADMIN, sourceNodeRef, DOC_LIB);
            Thread.sleep(200);
            NodeRef renditionNodeRef = waitForRendition(ADMIN, sourceNodeRef, DOC_LIB);
            boolean hasRendition2Aspect = false;
            for (int i = 0; i < 5; i++)
            {
                hasRendition2Aspect = AuthenticationUtil.runAs(() -> nodeService.hasAspect(renditionNodeRef, RenditionModel.ASPECT_RENDITION2), ADMIN);
                if (hasRendition2Aspect)
                {
                    break;
                }
                else
                {
                    Thread.sleep(500);
                }
            }

            assertTrue("Should have switched to the new rendition service", hasRendition2Aspect);
        }
        finally
        {
            renditionService2.setEnabled(true);
        }
    }
}