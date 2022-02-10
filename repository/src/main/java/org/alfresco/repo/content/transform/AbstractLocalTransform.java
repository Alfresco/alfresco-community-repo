/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2019 - 2022 Alfresco Software Limited
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
package org.alfresco.repo.content.transform;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.transform.client.model.config.TransformOption;
import org.alfresco.transform.client.model.config.TransformOptionGroup;
import org.alfresco.transform.client.model.config.TransformOptionValue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Abstract supper class for local transformer using flat transform options.
 */
public abstract class AbstractLocalTransform implements LocalTransform
{
    protected static final Log log = LogFactory.getLog(LocalTransform.class);

    protected final String name;
    protected final MimetypeService mimetypeService;
    protected final TransformerDebug transformerDebug;

    protected final Set<String> transformsTransformOptionNames = new HashSet<>();
    private final LocalTransformServiceRegistry localTransformServiceRegistry;
    private final boolean strictMimeTypeCheck;
    private final Map<String, Set<String>> strictMimetypeExceptions;
    private final boolean retryTransformOnDifferentMimeType;
    private final static ThreadLocal<Integer> depth = ThreadLocal.withInitial(()->0);

    AbstractLocalTransform(String name, TransformerDebug transformerDebug,
                           MimetypeService mimetypeService, boolean strictMimeTypeCheck,
                           Map<String, Set<String>> strictMimetypeExceptions, boolean retryTransformOnDifferentMimeType,
                           Set<TransformOption> transformsTransformOptions,
                           LocalTransformServiceRegistry localTransformServiceRegistry)
    {
        this.name = name;
        this.transformerDebug = transformerDebug;
        this.mimetypeService = mimetypeService;
        this.strictMimeTypeCheck = strictMimeTypeCheck;
        this.strictMimetypeExceptions = strictMimetypeExceptions;
        this.retryTransformOnDifferentMimeType = retryTransformOnDifferentMimeType;
        this.localTransformServiceRegistry = localTransformServiceRegistry;

        addOptionNames(transformsTransformOptionNames, transformsTransformOptions);
    }

    public abstract boolean isAvailable();

    protected abstract void transformImpl(ContentReader reader,
                                          ContentWriter writer, Map<String, String> transformOptions,
                                          String sourceMimetype, String targetMimetype,
                                          String sourceExtension, String targetExtension,
                                          String renditionName, NodeRef sourceNodeRef)
            throws UnsupportedTransformationException, ContentIOException;

    @Override
    public String getName()
    {
        return name;
    }

    public Set<String> getTransformsTransformOptionNames()
    {
        return transformsTransformOptionNames;
    }

    @Override
    public void transform(ContentReader reader, ContentWriter writer, Map<String, String> transformOptions,
                          String renditionName, NodeRef sourceNodeRef)
    {
        if (isAvailable())
        {
            String sourceMimetype = reader.getMimetype();
            String targetMimetype = writer.getMimetype();

            String sourceExtension = mimetypeService.getExtension(sourceMimetype);
            String targetExtension = mimetypeService.getExtension(targetMimetype);
            if (sourceExtension == null || targetExtension == null)
            {
                throw new AlfrescoRuntimeException("Unknown extensions for mimetypes: \n" +
                        "   source mimetype: " + sourceMimetype + "\n" +
                        "   source extension: " + sourceExtension + "\n" +
                        "   target mimetype: " + targetMimetype + "\n" +
                        "   target extension: " + targetExtension);
            }

            transformOptions = getStrippedTransformOptions(transformOptions);
            transformWithDebug(reader, writer, transformOptions, renditionName, sourceNodeRef, sourceMimetype,
                    targetMimetype, sourceExtension, targetExtension);

            if (log.isDebugEnabled())
            {
                log.debug("Local transformation completed: \n" +
                        "   source: " + reader + "\n" +
                        "   target: " + writer + "\n" +
                        "   options: " + transformOptions);
            }
        }
        else
        {
            if (log.isDebugEnabled())
            {
                transformOptions = getStrippedTransformOptions(transformOptions);
                log.debug("Local transformer not available: \n" +
                        "   source: " + reader + "\n" +
                        "   target: " + writer + "\n" +
                        "   options: " + transformOptions);
            }
        }
    }

    private void transformWithDebug(ContentReader reader, ContentWriter writer, Map<String, String> transformOptions,
                                    String renditionName, NodeRef sourceNodeRef, String sourceMimetype, String targetMimetype,
                                    String sourceExtension, String targetExtension)
    {
        try
        {
            depth.set(depth.get()+1);

            if (transformerDebug.isEnabled())
            {
                transformerDebug.pushTransform("Local:"+name, reader.getContentUrl(), sourceMimetype,
                        targetMimetype, reader.getSize(), transformOptions, renditionName, sourceNodeRef);
            }

            strictMimetypeCheck(reader, sourceNodeRef, sourceMimetype);
            transformImpl(reader, writer, transformOptions, sourceMimetype,
                    targetMimetype, sourceExtension, targetExtension, renditionName, sourceNodeRef);
        }
        catch (Throwable e)
        {
            retryWithDifferentMimetype(reader, writer, targetMimetype, transformOptions, renditionName, sourceNodeRef, e);
        }
        finally
        {
            transformerDebug.popTransform();
            depth.set(depth.get()-1);
        }
    }

    private void strictMimetypeCheck(ContentReader reader, NodeRef sourceNodeRef, String declaredMimetype)
    {
        if (mimetypeService != null && strictMimeTypeCheck && depth.get() == 1)
        {
            String detectedMimetype = mimetypeService.getMimetypeIfNotMatches(reader.getReader());

            if (!strictMimetypeCheck(declaredMimetype, detectedMimetype))
            {
                Set<String> allowedMimetypes = strictMimetypeExceptions.get(declaredMimetype);
                if (allowedMimetypes != null && allowedMimetypes.contains(detectedMimetype))
                {
                    String filename = transformerDebug.getFilename(sourceNodeRef, true);
                    String readerSourceMimetype = reader.getMimetype();
                    String message = "Transformation of ("+filename+
                            ") has not taken place because the declared mimetype ("+
                            readerSourceMimetype+") does not match the detected mimetype ("+
                            detectedMimetype+").";
                    log.warn(message);
                    throw new UnsupportedTransformationException(message);
                }
            }
        }
    }

    /**
     * When strict mimetype checking is performed before a transformation, this method is called.
     * There are a few issues with the Tika mimetype detection. As a result we still allow some
     * transformations to take place even if there is a discrepancy between the detected and
     * declared mimetypes.
     * @param declaredMimetype the mimetype on the source node
     * @param detectedMimetype returned by Tika having looked at the content.
     * @return true if the transformation should take place. This includes the case where the
     *         detectedMimetype is null (returned by Tika when the mimetypes are the same), or
     *         the supplied pair of mimetypes have been added to the
     *         {@code}transformer.strict.mimetype.check.whitelist{@code}.
     */
    private boolean strictMimetypeCheck(String declaredMimetype, String detectedMimetype)
    {
        if (detectedMimetype == null)
        {
            return true;
        }

        Set<String> detectedMimetypes = strictMimetypeExceptions.get(declaredMimetype);
        return detectedMimetypes != null && detectedMimetypes.contains(detectedMimetype);
    }

    private void retryWithDifferentMimetype(ContentReader reader, ContentWriter writer, String targetMimetype,
                                            Map<String, String> transformOptions, String renditionName,
                                            NodeRef sourceNodeRef, Throwable e)
    {
        if (mimetypeService != null && localTransformServiceRegistry != null)
        {
            String differentType = mimetypeService.getMimetypeIfNotMatches(reader.getReader());
            if (differentType == null)
            {
                transformerDebug.debug("          Failed", e);
                throw new ContentIOException("Content conversion failed: \n" +
                        "   reader: " + reader + "\n" +
                        "   writer: " + writer + "\n" +
                        "   options: " + transformOptions,
                        e);
            }
            else
            {
                transformerDebug.debug("          Failed: Mimetype was '" + differentType + "'", e);
                String claimedMimetype = reader.getMimetype();

                if (retryTransformOnDifferentMimeType)
                {
                    reader = reader.getReader();
                    reader.setMimetype(differentType);
                    long sourceSizeInBytes = reader.getSize();

                    LocalTransform localTransform = localTransformServiceRegistry.getLocalTransform(
                            differentType, sourceSizeInBytes, targetMimetype, transformOptions, renditionName);
                    if (localTransform == null)
                    {
                        transformerDebug.debug("          Failed", e);
                        throw new ContentIOException("Content conversion failed: \n" +
                                "   reader: " + reader + "\n" +
                                "   writer: " + writer + "\n" +
                                "   options: " + transformOptions + "\n" +
                                "   claimed mime type: " + claimedMimetype + "\n" +
                                "   detected mime type: " + differentType + "\n" +
                                "   transformer not found" + "\n",
                                e
                        );
                    }
                    localTransform.transform(reader, writer, transformOptions, renditionName, sourceNodeRef);
                }
                else
                {
                    throw new ContentIOException("Content conversion failed: \n" +
                            "   reader: " + reader + "\n" +
                            "   writer: " + writer + "\n" +
                            "   options: " + transformOptions + "\n" +
                            "   claimed mime type: " + claimedMimetype + "\n" +
                            "   detected mime type: " + differentType,
                            e
                    );
                }
            }
        }
    }

    /**
     * Returns a list of transform option names known to this transformer. When a transform is part of a pipeline or a
     * failover, the rendition options may include options needed for other transforms. So that extra options are not
     * passed to the T-Engine for this transform and rejected, {@link #getStrippedTransformOptions(Map)} removes them
     * using the names obtained here.
     */
    private static void addOptionNames(Set<String> transformsTransformOptionNames, Set<TransformOption> transformsTransformOptions)
    {
        for (TransformOption transformOption : transformsTransformOptions)
        {
            if (transformOption instanceof TransformOptionValue)
            {
                transformsTransformOptionNames.add(((TransformOptionValue)transformOption).getName());
            }
            else
            {
                addOptionNames(transformsTransformOptionNames, ((TransformOptionGroup)transformOption).getTransformOptions());
            }
        }
    }

    /**
     * Returns a subset of the supplied actual transform options from the rendition definition that are known to this
     * transformer. The ones that will be passed to the T-Engine. It strips out extra ones.
     * @param transformOptions the complete set of actual transform options. This will be returned if all options are
     *                         known to this transformer. Otherwise a new Map is returned.
     * @return the transformOptions to be past to the T-Engine.
     */
    public Map<String, String> getStrippedTransformOptions(Map<String, String> transformOptions)
    {
        Set<String> optionNames = transformOptions.keySet();
        if (transformsTransformOptionNames.containsAll(optionNames))
        {
            return transformOptions;
        }

        Map<String, String> strippedTransformOptions = new HashMap<>(transformOptions.size());
        for (Map.Entry<String, String> entry : transformOptions.entrySet())
        {
            String key = entry.getKey();
            if (transformsTransformOptionNames.contains(key))
            {
                String value = entry.getValue();
                strippedTransformOptions.put(key, value);
            }
        }

        return strippedTransformOptions;
    }
}
