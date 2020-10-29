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

import org.alfresco.repo.content.transform.UnsupportedTransformationException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;

import java.io.ByteArrayInputStream;

import static org.alfresco.model.ContentModel.PROP_CONTENT;
import static org.alfresco.repo.rendition2.TestSynchronousTransformClient.doTest;
import static org.alfresco.repo.rendition2.TestSynchronousTransformClient.isATest;

/**
 * @author adavis
 */
public class TestAsynchronousTransformClient<T> implements TransformClient
{
    private ContentService contentService;
    private TransformClient delegate;
    private RenditionService2Impl renditionService2;

    public TestAsynchronousTransformClient(ContentService contentService, TransformClient delegate,
                                           RenditionService2Impl renditionService2)
    {
        this.contentService = contentService;
        this.delegate = delegate;
        this.renditionService2 = renditionService2;
    }

    @Override
    public void checkSupported(NodeRef sourceNodeRef, RenditionDefinition2 renditionDefinition, String sourceMimetype,
                               long sourceSizeInBytes, String contentUrl)
    {
        String targetMimetype = renditionDefinition.getTargetMimetype();
        if (!isATest(sourceMimetype, targetMimetype))
        {
            delegate.checkSupported(sourceNodeRef, renditionDefinition, sourceMimetype, sourceSizeInBytes, contentUrl);
        }
    }

    @Override
    public void transform(NodeRef sourceNodeRef, RenditionDefinition2 renditionDefinition, String user,
                   int sourceContentHashCode)
            throws UnsupportedTransformationException, ContentIOException
    {
        ContentReader reader = contentService.getReader(sourceNodeRef, PROP_CONTENT);
        String sourceMimetype = reader.getMimetype();
        String targetMimetype = renditionDefinition.getTargetMimetype();
        if (isATest(sourceMimetype, targetMimetype))
        {
            ContentWriter writer = contentService.getTempWriter();
            writer.setMimetype(targetMimetype);
            doTest(sourceMimetype, targetMimetype, writer,
                    new TestSynchronousTransformClient.TestTransformClientCallback()
                    {
                        @Override
                        public void successfulTransform(ContentWriter writer)
                        {
                            ByteArrayInputStream inputStream = new ByteArrayInputStream("SUCCESS".getBytes());
                            renditionService2.consume(sourceNodeRef, inputStream, renditionDefinition,
                                    sourceContentHashCode);
                        }
                    });
        }
        else
        {
            delegate.transform(sourceNodeRef, renditionDefinition, user, sourceContentHashCode);
        }
    }
}
