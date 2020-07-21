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
package org.alfresco.service.cmr.repository;

import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.rendition2.SynchronousTransformClient;
import org.alfresco.service.Auditable;
import org.alfresco.transform.client.registry.TransformServiceRegistry;

import java.util.List;
import java.util.Map;

/**
 * Contains deprecated code from {@link ContentService} that is used to perform Legacy transforms.
 *
 * @author adavis
 */
public interface ContentTransformService
{
    /**
     * @deprecated use {@link SynchronousTransformClient#transform(ContentReader, ContentWriter, Map, String, NodeRef)}.
     */
    @Deprecated
    @Auditable(parameters = {"reader", "writer"})
    void transform(ContentReader reader, ContentWriter writer)
            throws NoTransformerException, ContentIOException;


    /**
     * @deprecated use {@link SynchronousTransformClient#transform(ContentReader, ContentWriter, Map, String, NodeRef)}.
     */
    @Deprecated
    @Auditable(parameters = {"reader", "writer", "options"})
    void transform(ContentReader reader, ContentWriter writer, Map<String, Object> options)
            throws NoTransformerException, ContentIOException;

    /**
     * @deprecated use {@link SynchronousTransformClient#transform(ContentReader, ContentWriter, Map, String, NodeRef)}.
     */
    @Deprecated
    @Auditable(parameters = {"reader", "writer", "options"})
    void transform(ContentReader reader, ContentWriter writer, TransformationOptions options)
            throws NoTransformerException, ContentIOException;

    /**
     * @deprecated use {@link SynchronousTransformClient#isSupported(String, long, String, String, Map, String, NodeRef)}.
     */
    @Deprecated
    @Auditable(parameters = {"sourceMimetype", "targetMimetype"})
    ContentTransformer getTransformer(String sourceMimetype, String targetMimetype);

    /**
     * @deprecated use {@link SynchronousTransformClient#isSupported(String, long, String, String, Map, String, NodeRef)}.
     */
    @Deprecated
    @Auditable(parameters = {"sourceMimetype", "sourceSize", "targetMimetype", "options"})
    List<ContentTransformer> getTransformers(String sourceUrl, String sourceMimetype, long sourceSize, String targetMimetype, TransformationOptions options);

    /**
     * @deprecated use {@link SynchronousTransformClient#isSupported(String, long, String, String, Map, String, NodeRef)}.
     */
    @Deprecated
    @Auditable(parameters = {"sourceMimetype", "sourceSize", "targetMimetype", "options"})
    ContentTransformer getTransformer(String sourceUrl, String sourceMimetype, long sourceSize, String targetMimetype, TransformationOptions options);

    /**
     * @deprecated use {@link SynchronousTransformClient#isSupported(String, long, String, String, Map, String, NodeRef)}.
     */
    @Deprecated
    ContentTransformer getTransformer(String sourceMimetype, String targetMimetype, TransformationOptions options);

    /**
     * @deprecated use {@link TransformServiceRegistry#findMaxSize(String, String, Map, String)}.
     */
    @Deprecated
    @Auditable(parameters = {"sourceMimetype", "targetMimetype", "options"})
    long getMaxSourceSizeBytes(String sourceMimetype, String targetMimetype, TransformationOptions options);

    /**
     * @deprecated use {@link SynchronousTransformClient#isSupported(String, long, String, String, Map, String, NodeRef)}.
     */
    @Auditable(parameters = {"sourceMimetype", "sourceSize", "targetMimetype", "options"})
    List<ContentTransformer> getActiveTransformers(String sourceMimetype, long sourceSize, String targetMimetype, TransformationOptions options);

    /**
     * @deprecated use {@link SynchronousTransformClient#isSupported(String, long, String, String, Map, String, NodeRef)}.
     */
    List<ContentTransformer> getActiveTransformers(String sourceMimetype, String targetMimetype, TransformationOptions options);

    /**
     * @deprecated there is no longer a need to obtain a transformer specifically for images.
     * Use {@link SynchronousTransformClient#transform(ContentReader, ContentWriter, Map, String, NodeRef)}.
     */
    @Deprecated
    @Auditable
    ContentTransformer getImageTransformer();

    /**
     * @deprecated use {@link SynchronousTransformClient#isSupported(String, long, String, String, Map, String, NodeRef)}.
     */
    @Deprecated
    @Auditable(parameters = {"reader", "writer"})
    boolean isTransformable(ContentReader reader, ContentWriter writer);

    /**
     * @deprecated use {@link SynchronousTransformClient#isSupported(String, long, String, String, Map, String, NodeRef)}.
     */
    @Deprecated
    @Auditable(parameters = {"reader", "writer", "options"})
    boolean isTransformable(ContentReader reader, ContentWriter writer, TransformationOptions options);
}
