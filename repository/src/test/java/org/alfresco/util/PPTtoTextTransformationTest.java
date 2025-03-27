/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.util;

import org.alfresco.repo.rendition2.AbstractRenditionIntegrationTest;
import org.alfresco.repo.rendition2.SynchronousTransformClient;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Map;

import static org.alfresco.model.ContentModel.PROP_CONTENT;

public class PPTtoTextTransformationTest extends AbstractRenditionIntegrationTest
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
    public void testTransformPttToTxt() throws Exception
    {
        checkTransform("quick.ppt", "text/plain", Collections.emptyMap(), true);
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
