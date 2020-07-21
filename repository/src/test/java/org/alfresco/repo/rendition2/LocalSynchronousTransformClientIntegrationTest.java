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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.alfresco.model.ContentModel.PROP_CONTENT;

/**
 * Integration tests for {@link LocalSynchronousTransformClient}
 */
public class LocalSynchronousTransformClientIntegrationTest extends AbstractRenditionIntegrationTest
{
    @Autowired
    protected SynchronousTransformClient localSynchronousTransformClient;

    protected SynchronousTransformClient synchronousTransformClient;

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
        synchronousTransformClient = localSynchronousTransformClient;
    }

    @Test
    public void testTransformDocxJpegMedium() throws Exception
    {
        checkTransform("quick.docx", "medium", true);
    }

    @Test
    public void testTransformDocxDoclib() throws Exception
    {
        checkTransform("quick.docx", "doclib", true);
    }

    @Test
    public void testParallelTransforms() throws Exception
    {
        Collection<Callable<Void>> transforms = new ArrayList<>();
        ExecutorService executorService = Executors.newWorkStealingPool(10);
        for (int i=0; i<50; i++)
        {
            Callable<Void> callable = () ->
            {
                checkTransform("quick.txt", "text/plain", Collections.emptyMap(), true);
                return null;
            };
            transforms.add(callable);
        }
        executorService.invokeAll(transforms);
    }

    @Test
    public void testTransformDocxJpegImgpreview() throws Exception
    {
        checkTransform("quick.docx", "imgpreview", true);
    }

    @Test
    public void testTransformDocxPngAvatar() throws Exception
    {
        checkTransform("quick.docx", "avatar", true);
    }

    @Test
    public void testTransformDocxPngAvatar32() throws Exception
    {
        checkTransform("quick.docx", "avatar32", true);
    }

    @Test
    public void testTransformDocxFlashWebpreview() throws Exception
    {
        checkTransform("quick.docx", "webpreview", false);
    }

    @Test
    public void testTransformDocxPdf() throws Exception
    {
        checkTransform("quick.docx", "pdf", false);
    }

    @Test
    public void testRetryOnDifferentMimetype() throws Exception
    {
        boolean expectedToPass = synchronousTransformClient.getClass().isInstance(LocalSynchronousTransformClient.class);

        // File is actually an image masked as docx
        checkTransform("quick-differentMimetype.docx", "pdf", expectedToPass);
    }

    @Test
    public void testNonWhitelistedStrictMimetype() throws Exception
    {
        checkTransform("quickMaskedHtml.jpeg", "avatar32", false);
    }

    // Does a synchronous transform similar to the supplied rendition, which is done asynchronously.
    private void checkTransform(String testFileName, String renditionDefinitionName, boolean expectedToPass) throws Exception
    {
        if (expectedToPass)
        {
            RenditionDefinition2 renditionDefinition =
                    renditionDefinitionRegistry2.getRenditionDefinition(renditionDefinitionName);
            String targetMimetype = renditionDefinition.getTargetMimetype();
            Map<String, String> actualOptions = renditionDefinition.getTransformOptions();

            checkTransform(testFileName, targetMimetype, actualOptions, expectedToPass);
        }
    }

    private void checkTransform(String testFileName, String targetMimetype, Map<String, String> actualOptions, boolean expectedToPass)
    {
        if (expectedToPass)
        {
            NodeRef sourceNode = transactionService.getRetryingTransactionHelper().doInTransaction(() ->
                    createContentNodeFromQuickFile(testFileName));
            ContentReader reader = contentService.getReader(sourceNode, PROP_CONTENT);
            ContentWriter writer = contentService.getTempWriter();
            writer.setMimetype(targetMimetype);
            synchronousTransformClient.transform(reader, writer, actualOptions, null, sourceNode);

            ContentReader transformReader = writer.getReader();
            String content = transformReader == null ? null : transformReader.getContentString();
            content = content == null || content.isEmpty() ? null : content;
            assertNotNull("The synchronous transform resulted in no content", content);
        }
    }
}
