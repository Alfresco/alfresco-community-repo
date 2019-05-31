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
    public void testLocalRenderDocxJpegMedium() throws Exception
    {
        checkClientRendition("quick.docx", "medium", true);
    }

    @Test
    public void testLocalRenderDocxDoclib() throws Exception
    {
        checkClientRendition("quick.docx", "doclib", true);
    }

    @Test
    public void testLocalRenderDocxJpegImgpreview() throws Exception
    {
        checkClientRendition("quick.docx", "imgpreview", true);
    }

    @Test
    public void testLocalRenderDocxPngAvatar() throws Exception
    {
        checkClientRendition("quick.docx", "avatar", true);
    }

    @Test
    public void testLocalRenderDocxPngAvatar32() throws Exception
    {
        checkClientRendition("quick.docx", "avatar32", true);
    }

    @Test
    public void testLocalRenderDocxFlashWebpreview() throws Exception
    {
        checkClientRendition("quick.docx", "webpreview", false);
    }

    @Test
    public void testLocalRenderDocxPdf() throws Exception
    {
        checkClientRendition("quick.docx", "pdf", false);
    }

    protected void checkClientRendition(String testFileName, String renditionDefinitionName, boolean expectedToPass) throws InterruptedException
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
