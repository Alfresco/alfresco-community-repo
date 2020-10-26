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
package org.alfresco.repo.content.transform;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.transform.client.registry.SupportedTransform;
import org.alfresco.util.PropertyCheck;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Debugs Legacy transformers selection and activity. Will be removed when Legacy transforms are removed.
 *
 * @author Alan Davis
 */
@Deprecated
public class LegacyTransformerDebug extends AdminUiTransformerDebug
{
    private ContentTransformerRegistry transformerRegistry;
    private TransformerConfig transformerConfig;

    public void setTransformerRegistry(ContentTransformerRegistry transformerRegistry)
    {
        this.transformerRegistry = transformerRegistry;
    }

    public void setTransformerConfig(TransformerConfig transformerConfig)
    {
        this.transformerConfig = transformerConfig;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        super.afterPropertiesSet();
        PropertyCheck.mandatory(this, "transformerRegistry", transformerRegistry);
        PropertyCheck.mandatory(this, "transformerConfig", transformerConfig);
    }

    @Deprecated
    public void pushAvailable(String fromUrl, String sourceMimetype, String targetMimetype,
                              TransformationOptions options)
    {
        String renditionName = options == null ? null : options.getUse();
        NodeRef sourceNodeRef = options == null ? null : options.getSourceNodeRef();
        pushAvailable(fromUrl, sourceMimetype, targetMimetype, renditionName, sourceNodeRef);
    }

    /**
     * Called prior to working out what transformers are available.
     */
    @Deprecated
    public void pushAvailable(String fromUrl, String sourceMimetype, String targetMimetype,
                              String renditionName, NodeRef sourceNodeRef)
    {
        if (isEnabled())
        {
            push(null, fromUrl, sourceMimetype, targetMimetype, -1,
                    null, renditionName, sourceNodeRef, Call.AVAILABLE);
        }
    }

    /**
     * Called when a transformer has been ignored because of a blacklist entry.
     */
    @Deprecated
    public void blacklistTransform(ContentTransformer transformer, String sourceMimetype,
                                   String targetMimetype, TransformationOptions options)
    {
        log("Blacklist "+getName(transformer)+" "+getMimetypeExt(sourceMimetype)+getMimetypeExt(targetMimetype));
    }


    @Deprecated
    public void pushTransform(ContentTransformer transformer, String fromUrl, String sourceMimetype,
                              String targetMimetype, long sourceSize, TransformationOptions options)
    {
        String renditionName = options == null ? null : options.getUse();
        NodeRef sourceNodeRef = options == null ? null : options.getSourceNodeRef();
        pushTransform(transformer, fromUrl, sourceMimetype, targetMimetype, sourceSize, renditionName, sourceNodeRef);
    }

    /**
     * Called prior to performing a transform.
     */
    @Deprecated
    public void pushTransform(ContentTransformer transformer, String fromUrl, String sourceMimetype,
                              String targetMimetype, long sourceSize, String renditionName, NodeRef sourceNodeRef)
    {
        if (isEnabled())
        {
            push(getName(transformer), fromUrl, sourceMimetype, targetMimetype, sourceSize,
                    null, renditionName, sourceNodeRef, Call.TRANSFORM);
        }
    }

    /**
     * Called prior to calling a nested isTransformable.
     */
    @Deprecated
    public void pushIsTransformableSize(ContentTransformer transformer)
    {
        if (isEnabled())
        {
            ThreadInfo.getIsTransformableStack().push(getName(transformer));
        }
    }

    /**
     * Called to identify a transformer that cannot be used during working out
     * available transformers.
     */
    @Deprecated
    public void unavailableTransformer(ContentTransformer transformer, String sourceMimetype, String targetMimetype, long maxSourceSizeKBytes)
    {
        if (isEnabled())
        {
            Deque<Frame> ourStack = ThreadInfo.getStack();
            Frame frame = ourStack.peek();

            if (frame != null)
            {
                Deque<String> isTransformableStack = ThreadInfo.getIsTransformableStack();
                String name = (!isTransformableStack.isEmpty())
                        ? isTransformableStack.getFirst()
                        : getName(transformer);
                boolean debug = (maxSourceSizeKBytes != 0);
                if (frame.unavailableTransformers == null)
                {
                    frame.unavailableTransformers = new TreeSet<UnavailableTransformer>();
                }
                String priority = gePriority(transformer, sourceMimetype, targetMimetype);
                frame.unavailableTransformers.add(new UnavailableTransformer(name, priority, maxSourceSizeKBytes, debug));
            }
        }
    }

    @Deprecated
    public void availableTransformers(List<ContentTransformer> transformers, long sourceSize,
                                      TransformationOptions options, String calledFrom)
    {
        String renditionName = options == null ? null : options.getUse();
        NodeRef sourceNodeRef = options == null ? null : options.getSourceNodeRef();
        availableTransformers(transformers, sourceSize, renditionName, sourceNodeRef, calledFrom);
    }

    /**
     * Called once all available transformers have been identified.
     */
    @Deprecated
    public void availableTransformers(List<ContentTransformer> transformers, long sourceSize,
                                      String renditionName, NodeRef sourceNodeRef, String calledFrom)
    {
        if (isEnabled())
        {
            Deque<Frame> ourStack = ThreadInfo.getStack();
            Frame frame = ourStack.peek();
            boolean firstLevel = ourStack.size() == 1;

            // Override setDebugOutput(false) to allow debug when there are transformers but they are all unavailable
            // Note once turned on we don't turn it off again.
            if (transformers.size() == 0)
            {
                frame.setFailureReason(NO_TRANSFORMERS);
                if (frame.unavailableTransformers != null &&
                        frame.unavailableTransformers.size() != 0)
                {
                    ThreadInfo.setDebugOutput(true);
                }
            }
            frame.setSourceSize(sourceSize);

            // Log the basic info about this transformation
            logBasicDetails(frame, sourceSize, null, renditionName,
                    calledFrom + ((transformers.size() == 0) ? " NO transformers" : ""), firstLevel);

            // Report available and unavailable transformers
            char c = 'a';
            int longestNameLength = getLongestTransformerNameLength(transformers, frame);
            for (ContentTransformer trans : transformers)
            {
                String name = getName(trans);
                int padName = longestNameLength - name.length() + 1;
                TransformationOptions options = new TransformationOptions();
                options.setUse(frame.renditionName);
                options.setSourceNodeRef(frame.sourceNodeRef);
                long maxSourceSizeKBytes = trans.getMaxSourceSizeKBytes(frame.sourceMimetype, frame.targetMimetype, options);
                String size = maxSourceSizeKBytes > 0 ? "< "+fileSize(maxSourceSizeKBytes*1024) : "";
                int padSize = 10 - size.length();
                String priority = gePriority(trans, frame.sourceMimetype, frame.targetMimetype);
                log((c == 'a' ? "**" : "  ") + (c++) + ") " + priority + ' ' + name + spaces(padName) +
                        size + spaces(padSize) + ms(trans.getTransformationTime(frame.sourceMimetype, frame.targetMimetype)));
            }
            if (frame.unavailableTransformers != null)
            {
                for (UnavailableTransformer unavailable: frame.unavailableTransformers)
                {
                    int pad = longestNameLength - unavailable.name.length();
                    String reason = "> "+fileSize(unavailable.maxSourceSizeKBytes*1024);
                    if (unavailable.debug || logger.isTraceEnabled())
                    {
                        log("--" + (c++) + ") " + unavailable.priority + ' ' + unavailable.name + spaces(pad+1) + reason, unavailable.debug);
                    }
                }
            }
        }
    }

    private String gePriority(ContentTransformer transformer, String sourceMimetype, String targetMimetype)
    {
        String priority =
            '[' + (isComponentTransformer(transformer)
            ? "---"
            : Integer.toString(transformerConfig.getPriority(transformer, sourceMimetype, targetMimetype))) +
            ']';
        priority = spaces(5-priority.length())+priority;
        return priority;
    }

    @Deprecated
    public void inactiveTransformer(ContentTransformer transformer)
    {
        log(getName(transformer)+' '+ms(transformer.getTransformationTime(null, null))+" INACTIVE");
    }

    @Deprecated
    public void activeTransformer(int mimetypePairCount, ContentTransformer transformer, String sourceMimetype,
                                  String targetMimetype, long maxSourceSizeKBytes, boolean firstMimetypePair)
    {
        if (firstMimetypePair)
        {
            log(getName(transformer)+' '+ms(transformer.getTransformationTime(sourceMimetype, targetMimetype)));
        }
        String i = Integer.toString(mimetypePairCount);
        String priority = gePriority(transformer, sourceMimetype, targetMimetype);
        log(spaces(5-i.length())+mimetypePairCount+") "+getMimetypeExt(sourceMimetype)+getMimetypeExt(targetMimetype)+
                priority +
                ' '+fileSize((maxSourceSizeKBytes > 0) ? maxSourceSizeKBytes*1024 : maxSourceSizeKBytes)+
                (maxSourceSizeKBytes == 0 ? " disabled" : ""));
    }

    @Deprecated
    public void activeTransformer(String sourceMimetype, String targetMimetype,
                                  int transformerCount, ContentTransformer transformer, long maxSourceSizeKBytes,
                                  boolean firstTransformer)
    {
        String priority = gePriority(transformer, sourceMimetype, targetMimetype);
        activeTransformer(sourceMimetype, targetMimetype, transformerCount, priority, getName(transformer),
                maxSourceSizeKBytes, firstTransformer);
    }

    private int getLongestTransformerNameLength(List<ContentTransformer> transformers,
                                                Frame frame)
    {
        int longestNameLength = 0;
        for (ContentTransformer trans : transformers)
        {
            int length = getName(trans).length();
            if (longestNameLength < length)
                longestNameLength = length;
        }
        if (frame != null && frame.unavailableTransformers != null)
        {
            for (UnavailableTransformer unavailable: frame.unavailableTransformers)
            {
                int length = unavailable.name.length();
                if (longestNameLength < length)
                    longestNameLength = length;
            }
        }
        return longestNameLength;
    }

    /**
     * Called after working out what transformers are available and any
     * resulting transform has been called.
     */
    public void popAvailable()
    {
        if (isEnabled())
        {
            pop(Call.AVAILABLE, false, false);
        }
    }


    /**
     * Called after returning from a nested isTransformable.
     */
    public void popIsTransformableSize()
    {
        if (isEnabled())
        {
            ThreadInfo.getIsTransformableStack().pop();
        }
    }

    /**
     * Returns a String and /or debug that provides a list of supported transformations for each
     * transformer.
     * @param transformerName restricts the list to one transformer. Unrestricted if null.
     * @param toString indicates that a String value should be returned in addition to any debug.
     * @param format42 indicates the old 4.1.4 format should be used which did not order the transformers
     *        and only included top level transformers.
     * @param renditionName to which the transformation will be put (such as "Index", "Preview", null).
     * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
     */
    @Deprecated
    public String transformationsByTransformer(String transformerName, boolean toString, boolean format42, String renditionName)
    {
        // Do not generate this type of debug if already generating other debug to a StringBuilder
        // (for example a test transform).
        if (getStringBuilder() != null)
        {
            return null;
        }

        Collection<ContentTransformer> transformers = format42 || transformerName != null
                ? sortTransformersByName(transformerName)
                : transformerRegistry.getTransformers();
        Collection<String> sourceMimetypes = format42
                ? getSourceMimetypes(null)
                : mimetypeService.getMimetypes();
        Collection<String> targetMimetypes = format42
                ? sourceMimetypes
                : mimetypeService.getMimetypes();

        TransformationOptions options = new TransformationOptions();
        options.setUse(renditionName);
        StringBuilder sb = null;
        try
        {
            if (toString)
            {
                sb = new StringBuilder();
                setStringBuilder(sb);
            }
            pushMisc();
            for (ContentTransformer transformer: transformers)
            {
                try
                {
                    pushMisc();
                    int mimetypePairCount = 0;
                    boolean first = true;
                    for (String sourceMimetype: sourceMimetypes)
                    {
                        for (String targetMimetype: targetMimetypes)
                        {
                            if (transformer.isTransformable(sourceMimetype, -1, targetMimetype, options))
                            {
                                long maxSourceSizeKBytes = transformer.getMaxSourceSizeKBytes(
                                        sourceMimetype, targetMimetype, options);
                                activeTransformer(++mimetypePairCount, transformer,
                                        sourceMimetype, targetMimetype, maxSourceSizeKBytes, first);
                                first = false;
                            }
                        }
                    }
                    if (first)
                    {
                        inactiveTransformer(transformer);
                    }
                }
                finally
                {
                    popMisc();
                }
            }
        }
        finally
        {
            popMisc();
            setStringBuilder(null);
        }
        stripFinishedLine(sb);
        return stripLeadingNumber(sb);
    }

    /**
     * Returns a String and /or debug that provides a list of supported transformations
     * sorted by source and target mimetype extension.
     * @param sourceExtension restricts the list to one source extension. Unrestricted if null.
     * @param targetExtension restricts the list to one target extension. Unrestricted if null.
     * @param toString indicates that a String value should be returned in addition to any debug.
     * @param format42 indicates the new 4.2 rather than older 4.1.4 format should be used.
     *        The 4.1.4 format did not order the transformers or mimetypes and only included top
     *        level transformers.
     * @param onlyNonDeterministic if true only report transformations where there is more than
     *        one transformer available with the same priority.
     * @param renditionName to which the transformation will be put (such as "Index", "Preview", null).
     * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
     */
    @Deprecated
    @Override
    public String transformationsByExtension(String sourceExtension, String targetExtension, boolean toString,
                                             boolean format42, boolean onlyNonDeterministic, String renditionName)
    {
        // Do not generate this type of debug if already generating other debug to a StringBuilder
        // (for example a test transform).
        if (getStringBuilder() != null)
        {
            return null;
        }

        Collection<ContentTransformer> transformers = format42 && !onlyNonDeterministic
                ? sortTransformersByName(null)
                : transformerRegistry.getTransformers();
        Collection<String> sourceMimetypes = format42 || sourceExtension != null
                ? getSourceMimetypes(sourceExtension)
                : mimetypeService.getMimetypes();
        Collection<String> targetMimetypes = format42 || targetExtension != null
                ? getTargetMimetypes(sourceExtension, targetExtension, sourceMimetypes)
                : mimetypeService.getMimetypes();

        TransformationOptions options = new TransformationOptions();
        options.setUse(renditionName);
        StringBuilder sb = null;
        try
        {
            if (toString)
            {
                sb = new StringBuilder();
                setStringBuilder(sb);
            }
            pushMisc();
            for (String sourceMimetype: sourceMimetypes)
            {
                for (String targetMimetype: targetMimetypes)
                {
                    // Find available transformers
                    List<ContentTransformer> availableTransformer = new ArrayList<ContentTransformer>();
                    for (ContentTransformer transformer: transformers)
                    {
                        if (transformer.isTransformable(sourceMimetype, -1, targetMimetype, options))
                        {
                            availableTransformer.add(transformer);
                        }
                    }

                    // Sort by priority
                    final String currSourceMimetype = sourceExtension;
                    final String currTargetMimetype = targetExtension;
                    Collections.sort(availableTransformer, new Comparator<ContentTransformer>()
                    {
                        @Override
                        public int compare(ContentTransformer transformer1, ContentTransformer transformer2)
                        {
                            return transformerConfig.getPriority(transformer1, currSourceMimetype, currTargetMimetype) -
                                    transformerConfig.getPriority(transformer2, currSourceMimetype, currTargetMimetype);
                        }
                    });

                    // Do we need to produce any output?
                    int size = availableTransformer.size();
                    int priority = size >= 2
                            ? transformerConfig.getPriority(availableTransformer.get(0), sourceMimetype, targetMimetype)
                            : -1;
                    if (!onlyNonDeterministic || (size >= 2 && priority ==
                            transformerConfig.getPriority(availableTransformer.get(1), sourceMimetype, targetMimetype)))
                    {
                        // Log the transformers
                        boolean supportedByTransformService = remoteTransformServiceRegistry == null ||
                                remoteTransformServiceRegistry instanceof DummyTransformServiceRegistry
                                ? false
                                : remoteTransformServiceRegistry.isSupported(sourceMimetype,
                                -1, targetMimetype, Collections.emptyMap(), null);
                        List<SupportedTransform> localTransformers = localTransformServiceRegistryImpl == null
                                ? Collections.emptyList()
                                : localTransformServiceRegistryImpl.findTransformers(sourceMimetype,
                                targetMimetype, Collections.emptyMap(), null);
                        if (!localTransformers.isEmpty() || supportedByTransformService || size >= 1)
                        {
                            try
                            {
                                pushMisc();
                                int transformerCount = 0;
                                if (supportedByTransformService)
                                {
                                    long maxSourceSizeKBytes = remoteTransformServiceRegistry.findMaxSize(sourceMimetype,
                                            targetMimetype, Collections.emptyMap(), null);
                                    activeTransformer(sourceMimetype, targetMimetype, transformerCount, "     ",
                                            TRANSFORM_SERVICE_NAME, maxSourceSizeKBytes, transformerCount++ == 0);
                                }
                                for (SupportedTransform localTransformer : localTransformers)
                                {
                                    long maxSourceSizeKBytes = localTransformer.getMaxSourceSizeBytes();
                                    String transformName = "Local:" + localTransformer.getName();
                                    String transformerPriority = "[" + localTransformer.getPriority() + ']';
                                    transformerPriority = spaces(5-transformerPriority.length())+transformerPriority;
                                    activeTransformer(sourceMimetype, targetMimetype, transformerCount, transformerPriority,
                                            transformName, maxSourceSizeKBytes, transformerCount++ == 0);
                                }
                                for (ContentTransformer transformer: availableTransformer)
                                {
                                    if (!onlyNonDeterministic || transformerCount < 2 ||
                                            priority == transformerConfig.getPriority(transformer, sourceMimetype, targetMimetype))
                                    {
                                        long maxSourceSizeKBytes = transformer.getMaxSourceSizeKBytes(
                                                sourceMimetype, targetMimetype, options);
                                        activeTransformer(sourceMimetype, targetMimetype, transformerCount,
                                                transformer, maxSourceSizeKBytes, transformerCount++ == 0);
                                    }
                                }
                            }
                            finally
                            {
                                popMisc();
                            }
                        }
                    }
                }
            }
        }
        finally
        {
            popMisc();
            setStringBuilder(null);
        }
        stripFinishedLine(sb);
        return stripLeadingNumber(sb);
    }

    private String getName(ContentTransformer transformer)
    {
        String name =
                transformer instanceof ContentTransformerHelper
                        ? ContentTransformerHelper.getSimpleName(transformer)
                        : transformer.getClass().getSimpleName();

        String type =
                ((transformer instanceof AbstractRemoteContentTransformer &&
                        ((AbstractRemoteContentTransformer)transformer).remoteTransformerClientConfigured()) ||
                        (transformer instanceof ProxyContentTransformer &&
                                ((ProxyContentTransformer)transformer).remoteTransformerClientConfigured())
                        ? "Remote" : "")+
                        (transformer instanceof ComplexContentTransformer
                                ? "Complex"
                                : transformer instanceof FailoverContentTransformer
                                ? "Failover"
                                : transformer instanceof ProxyContentTransformer
                                ? (((ProxyContentTransformer)transformer).getWorker() instanceof RuntimeExecutableContentTransformerWorker)
                                ? "Runtime"
                                : "Proxy"
                                : "");

        boolean componentTransformer = isComponentTransformer(transformer);

        StringBuilder sb = new StringBuilder("Legacy:").append(name);
        if (componentTransformer || type.length() > 0)
        {
            sb.append("<<");
            sb.append(type);
            if (componentTransformer)
            {
                sb.append("Component");
            }
            sb.append(">>");
        }

        return sb.toString();
    }


    private boolean isComponentTransformer(ContentTransformer transformer)
    {
        return !transformerRegistry.getTransformers().contains(transformer);
    }

    @Deprecated
    public String getFileName(TransformationOptions options, boolean firstLevel, long sourceSize)
    {
        NodeRef sourceNodeRef = options == null ? null : options.getSourceNodeRef();
        return getFileName(sourceNodeRef, firstLevel, sourceSize);
    }

    /**
     * Returns a sorted list of all transformers sorted by name.
     * @param transformerName to restrict the collection to one entry
     * @return a new Collection of sorted transformers
     * @throws IllegalArgumentException if transformerName is not found.
     * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
     */
    @Deprecated
    public Collection<ContentTransformer> sortTransformersByName(String transformerName)
    {
        Collection<ContentTransformer> transformers = (transformerName != null)
                ? Collections.singleton(transformerRegistry.getTransformer(transformerName))
                : transformerRegistry.getAllTransformers();

        SortedMap<String, ContentTransformer> map = new TreeMap<String, ContentTransformer>();
        for (ContentTransformer transformer: transformers)
        {
            String name = transformer.getName();
            map.put(name, transformer);
        }
        Collection<ContentTransformer> sorted = map.values();
        return sorted;
    }

    /**
     * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
     */
    @Deprecated
    public String testTransform(final String transformerName, String sourceExtension,
                                String targetExtension, String renditionName)
    {
        logger.error("The testTransform operation for a specific transformer is no longer supported. " +
                "Request redirected to the version of this method without a transformerName.");
        return testTransform(sourceExtension, targetExtension, renditionName);
    }
}
