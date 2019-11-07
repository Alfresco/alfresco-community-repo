/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

import static org.alfresco.model.ContentModel.PROP_CONTENT;

/**
 * Integration tests for {@link LocalTransformClient}
 */
public class LocalTransformClientIntegrationTest extends AbstractRenditionIntegrationTest
{
    @Autowired
    protected TransformClient localTransformClient;

    protected TransformClient transformClient;

    @BeforeClass
    public static void before()
    {
        AbstractRenditionIntegrationTest.before();
        local();
    }

    @AfterClass
    public static void after()
    {
        // Strict MimetypeCheck property cleanup
        System.clearProperty("transformer.strict.mimetype.check");
        // Retry on DifferentMimetype property cleanup
        System.clearProperty("content.transformer.retryOn.different.mimetype");
        AbstractRenditionIntegrationTest.after();
    }

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        AuthenticationUtil.setRunAsUser(AuthenticationUtil.getAdminUserName());
        transformClient = localTransformClient;
    }

    @Test
    public void testRenderPagesToJpeg() throws Exception
    {
        new RenditionDefinition2Impl("pagesToJpeg", "image/jpeg", new HashMap<>(), true, renditionDefinitionRegistry2 );
        try
        {
            checkClientRendition("quick2009.pages", "pagesToJpeg", true);
        }
        finally
        {
            // Remove rendition even if check throws an exception to not interfere with other tests
            renditionDefinitionRegistry2.unregister("pagesToJpeg");
        }
    }

    @Test
    public void testReloadOfStaticDefinitions()
    {
        new RenditionDefinition2Impl("dynamic1", "image/jpeg", new HashMap<>(), true, renditionDefinitionRegistry2 );
        new RenditionDefinition2Impl("dynamic2", "image/jpeg", new HashMap<>(), true, renditionDefinitionRegistry2 );
        new RenditionDefinition2Impl("static1", "image/jpeg", new HashMap<>(), false, renditionDefinitionRegistry2 );
        new RenditionDefinition2Impl("static2", "image/jpeg", new HashMap<>(), false, renditionDefinitionRegistry2 );

        try
        {
            assertNotNull(renditionDefinitionRegistry2.getRenditionDefinition("dynamic1"));
            assertNotNull(renditionDefinitionRegistry2.getRenditionDefinition("dynamic2"));
            assertNotNull(renditionDefinitionRegistry2.getRenditionDefinition("static1"));
            assertNotNull(renditionDefinitionRegistry2.getRenditionDefinition("static2"));

            renditionDefinitionRegistry2.reloadRegistry();

            assertNull(renditionDefinitionRegistry2.getRenditionDefinition("dynamic1"));
            assertNull(renditionDefinitionRegistry2.getRenditionDefinition("dynamic2"));
            assertNotNull(renditionDefinitionRegistry2.getRenditionDefinition("static1"));
            assertNotNull(renditionDefinitionRegistry2.getRenditionDefinition("static2"));
        }
        finally
        {
            renditionDefinitionRegistry2.unregister("static1");
            renditionDefinitionRegistry2.unregister("static2");
        }
    }

    @Test
    public void testRenderDocxJpegMedium() throws Exception
    {
        checkClientRendition("quick.docx", "medium", true);
    }

    @Test
    public void testRenderDocxDoclib() throws Exception
    {
        checkClientRendition("quick.docx", "doclib", true);
    }

    @Test
    public void testRenderDocxJpegImgpreview() throws Exception
    {
        checkClientRendition("quick.docx", "imgpreview", true);
    }

    @Test
    public void testRenderDocxPngAvatar() throws Exception
    {
        checkClientRendition("quick.docx", "avatar", true);
    }

    @Test
    public void testRenderDocxPngAvatar32() throws Exception
    {
        checkClientRendition("quick.docx", "avatar32", true);
    }

    @Test
    public void testRenderDocxFlashWebpreview() throws Exception
    {
        checkClientRendition("quick.docx", "webpreview", false);
    }

    @Test
    public void testRenderDocxPdf() throws Exception
    {
        checkClientRendition("quick.docx", "pdf", false);
    }

    @Test
    public void testRetryOnDifferentMimetype() throws Exception
    {
        boolean expectedToPass = transformClient.getClass().isInstance(LocalTransformClient.class);

        // File is actually an image masked as docx
        checkClientRendition("quick-differentMimetype.docx", "pdf", expectedToPass);
    }

    @Test
    public void testNonWhitelistedStrictMimetype() throws Exception
    {
        checkClientRendition("quickMaskedHtml.jpeg", "avatar32", false);
    }

    private void checkClientRendition(String testFileName, String renditionDefinitionName, boolean expectedToPass) throws InterruptedException
    {
        if (expectedToPass)
        {
            // split into separate transactions as the client is async
            NodeRef sourceNode = transactionService.getRetryingTransactionHelper().doInTransaction(() ->
                    createContentNodeFromQuickFile(testFileName));
            ContentData contentData = DefaultTypeConverter.INSTANCE.convert(ContentData.class, nodeService.getProperty(sourceNode, PROP_CONTENT));
            int sourceContentHashCode = (contentData == null ? "" : contentData.getContentUrl()+contentData.getMimetype()).hashCode();
            transactionService.getRetryingTransactionHelper().doInTransaction(() ->
            {
                RenditionDefinition2 renditionDefinition =
                        renditionDefinitionRegistry2.getRenditionDefinition(renditionDefinitionName);
                String contentUrl = contentData.getContentUrl();
                String sourceMimetype = contentData.getMimetype();
                long size = contentData.getSize();
                String adminUserName = AuthenticationUtil.getAdminUserName();
                transformClient.checkSupported(sourceNode, renditionDefinition, sourceMimetype, size, contentUrl);
                transformClient.transform(sourceNode, renditionDefinition, adminUserName, sourceContentHashCode);
                return null;
            });
            ChildAssociationRef childAssociationRef = null;
            for (int i = 0; i < 20; i++)
            {
                childAssociationRef = renditionService2.getRenditionByName(sourceNode, renditionDefinitionName);
                if (childAssociationRef != null)
                {
                    break;
                }
                else
                {
                    Thread.sleep(500);
                }
            }
            assertNotNull("The " + renditionDefinitionName + " rendition failed for " + testFileName, childAssociationRef);
        }
    }
}
