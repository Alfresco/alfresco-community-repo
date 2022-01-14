/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

import static org.alfresco.model.ContentModel.PROP_CONTENT;
import static org.junit.Assert.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Integration tests for {@link RenditionService2}
 */
public class RenditionService2IntegrationTest extends AbstractRenditionIntegrationTest
{
    @BeforeClass
    public static void before()
    {
        AbstractRenditionIntegrationTest.before();
        local();
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
        waitForRendition(ADMIN, sourceNodeRef, DOC_LIB, true);
    }

    @Test
    public void changedSourceToNullContent() 
    {
        NodeRef sourceNodeRef = createSource(ADMIN, "quick.jpg");
        render(ADMIN, sourceNodeRef, DOC_LIB);
        waitForRendition(ADMIN, sourceNodeRef, DOC_LIB, true);

        clearContent(ADMIN, sourceNodeRef);
        render(ADMIN, sourceNodeRef, DOC_LIB);
        ChildAssociationRef assoc = AuthenticationUtil.runAs(() ->
                renditionService2.getRenditionByName(sourceNodeRef, DOC_LIB), ADMIN);
        waitForRendition(ADMIN, sourceNodeRef, DOC_LIB, false);
        assertNull("There should be no rendition as there was no content", assoc);
    }

    @Test
    public void changedSourceToNonNull()
    {
        NodeRef sourceNodeRef = createSource(ADMIN, "quick.jpg");
        render(ADMIN, sourceNodeRef, DOC_LIB);
        NodeRef rendition1 = waitForRendition(ADMIN, sourceNodeRef, DOC_LIB, true);
        ContentData contentData1 = DefaultTypeConverter.INSTANCE.convert(ContentData.class, nodeService.getProperty(rendition1, PROP_CONTENT));

        updateContent(ADMIN, sourceNodeRef, "quick.png");
        render(ADMIN, sourceNodeRef, DOC_LIB);
        NodeRef rendition2 = waitForRendition(ADMIN, sourceNodeRef, DOC_LIB, true);
        ContentData contentData2 = DefaultTypeConverter.INSTANCE.convert(ContentData.class, nodeService.getProperty(rendition2, PROP_CONTENT));

        assertEquals("The rendition node should not change", rendition1, rendition2);
        assertNotEquals("The content should have change", contentData1.toString(), contentData2.toString());
    }

    @Test
    public void changedSourceFromNull()
    {
        NodeRef sourceNodeRef = createSource(ADMIN, "quick.jpg");
        render(ADMIN, sourceNodeRef, DOC_LIB);
        waitForRendition(ADMIN, sourceNodeRef, DOC_LIB, true);

        clearContent(ADMIN, sourceNodeRef);
        render(ADMIN, sourceNodeRef, DOC_LIB);
        ChildAssociationRef assoc = AuthenticationUtil.runAs(() ->
                renditionService2.getRenditionByName(sourceNodeRef, DOC_LIB), ADMIN);
        waitForRendition(ADMIN, sourceNodeRef, DOC_LIB, false);
        assertNull("There should be no rendition as there was no content", assoc);

        updateContent(ADMIN, sourceNodeRef, "quick.png");
        render(ADMIN, sourceNodeRef, DOC_LIB);
        waitForRendition(ADMIN, sourceNodeRef, DOC_LIB, true);
    }

    @Test
    public void testCreateRenditionByUser() 
    {
        String userName = createRandomUser();
        NodeRef sourceNodeRef = createSource(userName, "quick.jpg");
        render(userName, sourceNodeRef, DOC_LIB);
        NodeRef renditionNodeRef = waitForRendition(userName, sourceNodeRef, DOC_LIB, true);
        assertNotNull("The rendition was not generated for non-admin user", renditionNodeRef);
    }

    @Test
    public void testReadRenditionByOtherUser() 
    {
        String ownerUserName = createRandomUser();
        NodeRef sourceNodeRef = createSource(ownerUserName, "quick.jpg");
        String otherUserName = createRandomUser();
        transactionService.getRetryingTransactionHelper().doInTransaction(() ->
        {
            permissionService.setPermission(sourceNodeRef, otherUserName, PermissionService.READ, true);
            return null;
        });
        render(ownerUserName, sourceNodeRef, DOC_LIB);
        NodeRef renditionNodeRef = waitForRendition(ownerUserName, sourceNodeRef, DOC_LIB, true);
        assertNotNull("The rendition is not visible for owner of source node", renditionNodeRef);
        renditionNodeRef = waitForRendition(otherUserName, sourceNodeRef, DOC_LIB, true);
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
        transactionService.getRetryingTransactionHelper().doInTransaction(() ->
        {
            permissionService.setPermission(sourceNodeRef, otherUserName, PermissionService.READ, true);
            return null;
        });
        render(otherUserName, sourceNodeRef, DOC_LIB);
        NodeRef renditionNodeRef = waitForRendition(ownerUserName, sourceNodeRef, DOC_LIB, true);
        assertNotNull("The rendition is not visible for owner of source node", renditionNodeRef);
        renditionNodeRef = waitForRendition(otherUserName, sourceNodeRef, DOC_LIB, true);
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
        transactionService.getRetryingTransactionHelper().doInTransaction(() ->
        {
            permissionService.setPermission(sourceNodeRef, noPermissionsUser, PermissionService.ALL_PERMISSIONS, false);
            return null;
        });
        try
        {
            waitForRendition(noPermissionsUser, sourceNodeRef, DOC_LIB, true);
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
        NodeRef newRendition = waitForRendition(ownerUserName, sourceNodeRef, DOC_LIB, true);
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
     * Tests new {@link RenditionService2Impl#clearRenditionContentData(NodeRef, String)} method.
     * <p>
     * This method cleans a rendition content and content hash code.
     * </p>
     */
    @Test
    public void testClearRenditionContentData()
    {
        // Create a node
        NodeRef sourceNodeRef = createSource(ADMIN, "quick.jpg");

        // Trigger the rendition
        render(ADMIN, sourceNodeRef, DOC_LIB);
        NodeRef renditionNodeRef = waitForRendition(ADMIN, sourceNodeRef, DOC_LIB, true);
        assertNotNull("Rendition was not generated", renditionNodeRef);
        assertNotNull("Rendition was not generated", nodeService.getProperty(renditionNodeRef, ContentModel.PROP_CONTENT));

        // Check the rendition content hash code is valid
        int contentHashCode = getRenditionContentHashCode(renditionNodeRef);
        assertTrue("Rendition content hash code was not generated", isValidRenditionContentHashCode(contentHashCode));

        // Disable renditionService2
        renditionService2.setEnabled(false);

        // Call 'clearRenditionContentData' method directly to prove rendition content will be cleaned
        AuthenticationUtil.runAs((AuthenticationUtil.RunAsWork<Void>) () ->
            transactionService.getRetryingTransactionHelper().doInTransaction(() ->
            {
                renditionService2.clearRenditionContentData(sourceNodeRef, DOC_LIB);
                return null;
            }), ADMIN);

        // The rendition should not have content by now
        assertNull("Rendition has content", nodeService.getProperty(renditionNodeRef, ContentModel.PROP_CONTENT));

        // The rendition should not have a content hash code by now
        contentHashCode = getRenditionContentHashCode(renditionNodeRef);
        assertFalse("Rendition has content hash code", isValidRenditionContentHashCode(contentHashCode));

        // Enable renditionService2
        renditionService2.setEnabled(true);
    }

    /**
     * Tests if a rendition without content (but with contentHashCode) can be generated again.
     * <p>
     * If the rendition consumption receives a null InputStream, the contentHashCode should be cleaned from the
     * rendition node, allowing new requests to generate the rendition.
     * </p>
     */
    @Test
    public void testRenditionWithNullContentButWithContentHashCode()
    {
        testRenditionWithoutContentButWithContentHashCode(null);
    }

    /**
     * Tests if a rendition without content (but with contentHashCode) can be generated again.
     * <p>
     * If the rendition consumption receives a zero length InputStream, the contentHashCode should be cleaned from the
     * rendition node, allowing new requests to generate the rendition.
     * </p>
     */
    @Test
    public void testRenditionWithZeroContentButWithContentHashCode()
    {
        testRenditionWithoutContentButWithContentHashCode(new ByteArrayInputStream(new byte[0]));
    }

    private void testRenditionWithoutContentButWithContentHashCode(InputStream transformInputStream)
    {
        // Create a node with an actual rendition
        NodeRef sourceNodeRef = createSource(ADMIN, "quick.jpg");
        render(ADMIN, sourceNodeRef, DOC_LIB);
        NodeRef renditionNodeRef = waitForRendition(ADMIN, sourceNodeRef, DOC_LIB, true);
        assertNotNull("Rendition was not generated", renditionNodeRef);
        assertNotNull("Rendition was not generated", nodeService.getProperty(renditionNodeRef, ContentModel.PROP_CONTENT));

        // Clear rendition content (at this point we're forcing a rendition without content but with hash code)
        clearContent(ADMIN, renditionNodeRef);
        assertNull("Rendition has content", nodeService.getProperty(renditionNodeRef, ContentModel.PROP_CONTENT));

        // Check that rendition still has the content hash code
        final int contentHashCode = getRenditionContentHashCode(renditionNodeRef);
        assertTrue("Rendition content hash code was not generated", isValidRenditionContentHashCode(contentHashCode));

        // Call the 'consume' method directly supplying a null InputStream
        // This is explicitly called to prove that rendition hash code will be cleaned (as opposite to previous behavior)
        RenditionDefinition2 renditionDefinition = renditionDefinitionRegistry2.getRenditionDefinition(DOC_LIB);
        AuthenticationUtil.runAs(() -> {
            renditionService2.consume(sourceNodeRef, transformInputStream, renditionDefinition, contentHashCode);
            return null;
        }, ADMIN);
        waitForRendition(ADMIN, sourceNodeRef, DOC_LIB, false);

        // The content hash code should have been cleaned from the rendition node
        int contentHashCode2 = getRenditionContentHashCode(renditionNodeRef);
        assertFalse("Rendition content hash code was not cleaned", isValidRenditionContentHashCode(contentHashCode2));

        // Attempts to render the rendition again
        render(ADMIN, sourceNodeRef, DOC_LIB);
        NodeRef renditionNodeRef2 = waitForRendition(ADMIN, sourceNodeRef, DOC_LIB, true);
        assertEquals("New rendition nodeRef is different", renditionNodeRef.toString(), renditionNodeRef2.toString());
        assertNotNull("New rendition content was not generated", nodeService.getProperty(renditionNodeRef2, ContentModel.PROP_CONTENT));

        // Check the new rendition content hash code
        int contentHashCode3 = getRenditionContentHashCode(renditionNodeRef2);
        assertTrue("New rendition content hash code was not generated", isValidRenditionContentHashCode(contentHashCode3));
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
            assertNotNull("The old renditions service did not render", waitForRendition(ADMIN, sourceNodeRef, DOC_LIB, true));
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
        waitForRendition(ADMIN, sourceNodeRef, DOC_LIB, true);

        renditionService2.setEnabled(false);
        try
        {
            updateContent(ADMIN, sourceNodeRef, "quick.png");
            Thread.sleep(200);
            NodeRef renditionNodeRef = waitForRendition(ADMIN, sourceNodeRef, DOC_LIB, true);
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
            waitForRendition(ADMIN, sourceNodeRef, DOC_LIB, true);

            renditionService2.setEnabled(true);

            updateContent(ADMIN, sourceNodeRef, "quick.png");
            NodeRef renditionNodeRef = waitForRendition(ADMIN, sourceNodeRef, DOC_LIB, true);
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
        NodeRef newRendition = waitForRendition(ownerUserName, sourceNodeRef, DOC_LIB, true);
        boolean hasRendition2Aspect = AuthenticationUtil.runAs(() -> nodeService.hasAspect(newRendition, RenditionModel.ASPECT_RENDITION2), ownerUserName);
        assertTrue("The source should have the old renditioned aspect",
                AuthenticationUtil.runAs(() -> nodeService.hasAspect(sourceNodeRef, RenditionModel.ASPECT_RENDITIONED), ownerUserName));
        assertTrue("The rendition2 aspect should be present", hasRendition2Aspect);
        try
        {
            renditionService2.setEnabled(false);
            updateContent(ownerUserName, sourceNodeRef, "quick.png");
            NodeRef oldRendition = waitForRendition(ownerUserName, sourceNodeRef, DOC_LIB, true);
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
            waitForRendition(ADMIN, sourceNodeRef, DOC_LIB, true);

            renditionService2.setEnabled(true);
            render(ADMIN, sourceNodeRef, DOC_LIB);
            Thread.sleep(200);
            NodeRef renditionNodeRef = waitForRendition(ADMIN, sourceNodeRef, DOC_LIB, true);
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