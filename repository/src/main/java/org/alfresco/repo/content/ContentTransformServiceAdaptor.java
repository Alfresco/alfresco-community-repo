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
package org.alfresco.repo.content;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.transform.AbstractContentTransformer2;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.content.transform.LocalTransform;
import org.alfresco.repo.content.transform.LocalTransformServiceRegistry;
import org.alfresco.repo.content.transform.UnsupportedTransformationException;
import org.alfresco.repo.rendition2.LegacySynchronousTransformClient;
import org.alfresco.repo.rendition2.SynchronousTransformClient;
import org.alfresco.repo.rendition2.TransformationOptionsConverter;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentTransformService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NoTransformerException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.transform.client.registry.TransformServiceRegistry;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Contains deprecated methods originally from {@link org.alfresco.repo.content.ContentServiceImpl} that is used to
 * perform Legacy transforms.
 *
 * @author adavis
 */
@Deprecated
public class ContentTransformServiceAdaptor implements ContentTransformService
{
    private ContentTransformer imageMagickContentTransformer;
    private LegacySynchronousTransformClient legacySynchronousTransformClient;
    private LocalTransformServiceRegistry localTransformServiceRegistryImpl;
    private SynchronousTransformClient synchronousTransformClient;
    private TransformServiceRegistry localTransformServiceRegistry;
    private TransformationOptionsConverter converter;

    @Deprecated
    public void setImageMagickContentTransformer(ContentTransformer imageMagickContentTransformer)
    {
        this.imageMagickContentTransformer = imageMagickContentTransformer;
    }

    public void setLegacySynchronousTransformClient(LegacySynchronousTransformClient legacySynchronousTransformClient)
    {
        this.legacySynchronousTransformClient = legacySynchronousTransformClient;
    }

    public void setLocalTransformServiceRegistryImpl(LocalTransformServiceRegistry localTransformServiceRegistryImpl)
    {
        this.localTransformServiceRegistryImpl = localTransformServiceRegistryImpl;
    }

    public void setSynchronousTransformClient(SynchronousTransformClient synchronousTransformClient)
    {
        this.synchronousTransformClient = synchronousTransformClient;
    }

    public void setLocalTransformServiceRegistry(TransformServiceRegistry localTransformServiceRegistry)
    {
        this.localTransformServiceRegistry = localTransformServiceRegistry;
    }

    public void setConverter(TransformationOptionsConverter converter)
    {
        this.converter = converter;
    }

    @Deprecated
    @Override
    public void transform(ContentReader reader, ContentWriter writer)
    {
        synchronousTransformClient.transform(reader, writer, Collections.emptyMap(), null, null);
    }

    @Deprecated
    @Override
    public void transform(ContentReader reader, ContentWriter writer, Map<String, Object> legacyOptionsMap)
            throws NoTransformerException, ContentIOException
    {
        TransformationOptions transformationOptions = new TransformationOptions(legacyOptionsMap);
        Map<String, String> options = converter.getOptions(transformationOptions, null, null);
        synchronousTransformClient.transform(reader, writer, options, null, null);
    }

    @Deprecated
    @Override
    public void transform(ContentReader reader, ContentWriter writer, TransformationOptions transformationOptions)
            throws NoTransformerException, ContentIOException
    {
        try
        {
            Map<String, String> options = converter.getOptions(transformationOptions, null, null);
            synchronousTransformClient.transform(reader, writer, options, null, null);
        }
        catch (UnsupportedTransformationException ute)
        {
            throw newNoTransformerException(reader, writer);
        }
        catch (IllegalArgumentException iae)
        {
            if (iae.getMessage().contains("sourceNodeRef null has no content"))
            {
                throw newNoTransformerException(reader, writer);
            }
            throw new AlfrescoRuntimeException(iae.getMessage(), iae);
        }
    }

    private NoTransformerException newNoTransformerException(ContentReader reader, ContentWriter writer)
    {
        String sourceMimetype = reader == null ? "null" : reader.getMimetype();
        String targetMimetype = writer == null ? "null" : writer.getMimetype();
        return new NoTransformerException(sourceMimetype, targetMimetype);
    }

    @Deprecated
    @Override
    public ContentTransformer getTransformer(String sourceMimetype, String targetMimetype)
    {
        return getTransformer(sourceMimetype, targetMimetype, new TransformationOptions());
    }

    @Deprecated
    @Override
    public ContentTransformer getTransformer(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        return getTransformer(null, sourceMimetype, -1, targetMimetype, new TransformationOptions());
    }

    @Deprecated
    @Override
    public ContentTransformer getTransformer(String sourceUrl, String sourceMimetype, long sourceSize,
                                             String targetMimetype, TransformationOptions options)
    {
        ContentTransformer localTransformer = wrapLocalTransformer(sourceUrl, sourceMimetype, sourceSize, targetMimetype, options);
        return localTransformer == null
                ? legacySynchronousTransformClient.getTransformer(sourceUrl, sourceMimetype, sourceSize, targetMimetype, options)
                : localTransformer;
    }

    @Deprecated
    @Override
    // Same as getActiveTransformers, but with debug
    public List<ContentTransformer> getTransformers(String sourceUrl, String sourceMimetype, long sourceSize,
                                                    String targetMimetype, TransformationOptions options)
    {
        ContentTransformer localTransformer = wrapLocalTransformer(sourceUrl, sourceMimetype, sourceSize, targetMimetype, options);
        return localTransformer == null
                ? legacySynchronousTransformClient.getTransformers(sourceUrl, sourceMimetype, sourceSize, targetMimetype, options)
                : Collections.singletonList(localTransformer);
    }

    @Deprecated
    @Override
    // Same as getTransformers, but without debug
    public List<ContentTransformer> getActiveTransformers(String sourceMimetype,
                                                          String targetMimetype, TransformationOptions options)
    {
        return getActiveTransformers(sourceMimetype, -1, targetMimetype, options);
    }

    @Deprecated
    @Override
    // Same as getTransformers, but without debug
    public List<ContentTransformer> getActiveTransformers(String sourceMimetype, long sourceSize,
                                                          String targetMimetype, TransformationOptions options)
    {
        ContentTransformer localTransformer = wrapLocalTransformer(null, sourceMimetype, sourceSize, targetMimetype, options);
        return localTransformer == null
                ? legacySynchronousTransformClient.getActiveTransformers(sourceMimetype, sourceSize, targetMimetype, options)
                : Collections.singletonList(localTransformer);
    }

    private ContentTransformer wrapLocalTransformer(String sourceUrl, String sourceMimetype, long sourceSize,
                                                    String targetMimetype, TransformationOptions transformationOptions)
    {
        AbstractContentTransformer2 transformer = null;
        Map<String, String> options = converter.getOptions(transformationOptions, null, null);
        LocalTransform localTransform = localTransformServiceRegistryImpl.getLocalTransform(sourceMimetype,
                sourceSize, targetMimetype, options, null);
        if (localTransform != null)
        {
            transformer = new AbstractContentTransformer2() {

                @Override
                public void transform(ContentReader reader, ContentWriter writer, TransformationOptions options)
                        throws ContentIOException
                {
                    try
                    {
                        transformInternal(reader, writer, transformationOptions);
                    }
                    catch (Exception e)
                    {
                        throw new ContentIOException(e.getMessage(), e);
                    }
                }


                @Override
                protected void transformInternal(ContentReader reader, ContentWriter writer, TransformationOptions transformationOptions) throws Exception
                {
                    localTransform.transform(reader, writer, options, null, null);
                }

                @Override
                public void register()
                {
                }

                @Override
                public boolean isSupportedTransformation(String sourceMimetype, String targetMimetype, TransformationOptions options)
                {
                    return true;
                }

                @Override
                public boolean isTransformable(String sourceMimetype, long sourceSize, String targetMimetype, TransformationOptions options)
                {
                    return true;
                }

                @Override
                public boolean isTransformableMimetype(String sourceMimetype, String targetMimetype, TransformationOptions options)
                {
                    return true;
                }

                @Override
                public boolean isTransformableSize(String sourceMimetype, long sourceSize, String targetMimetype, TransformationOptions options)
                {
                    return true;
                }

                @Override
                public String getName()
                {
                    return "Wrapped<"+localTransformServiceRegistryImpl.findTransformerName(sourceMimetype, sourceSize,
                            targetMimetype, options, null)+">";
                }
            };
        }
        return transformer;
    }

    @Deprecated
    @Override
    public long getMaxSourceSizeBytes(String sourceMimetype,
                                      String targetMimetype, TransformationOptions transformationOptions)
    {
        Map<String, String> options = converter.getOptions(transformationOptions, null, null);
        return localTransformServiceRegistry.findMaxSize(sourceMimetype, targetMimetype, options, null);
    }

    @Deprecated
    @Override
    public ContentTransformer getImageTransformer()
    {
        return imageMagickContentTransformer;
    }

    @Deprecated
    @Override
    public boolean isTransformable(ContentReader reader, ContentWriter writer)
    {
        return isTransformable(reader, writer, null);
    }

    @Deprecated
    @Override
    public boolean isTransformable(ContentReader reader, ContentWriter writer, TransformationOptions transformationOptions)
    {
        String sourceMimetype = reader.getMimetype();
        long sourceSizeInBytes = reader.getSize();
        String contentUrl = reader.getContentUrl();
        String targetMimetype = writer.getMimetype();
        NodeRef sourceNodeRef = transformationOptions.getSourceNodeRef();
        Map<String, String> options = converter.getOptions(transformationOptions, null, null);
        return synchronousTransformClient.isSupported(sourceMimetype, sourceSizeInBytes, contentUrl, targetMimetype,
                options, null, sourceNodeRef);
    }
}
