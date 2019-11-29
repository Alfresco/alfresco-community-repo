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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.rendition2.SynchronousTransformClient;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.LogTee;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * Debugs transformers selection and activity.<p>
 *
 * As transformations are frequently composed of lower level transformations, log
 * messages include a prefix to identify the transformation. A numeric dot notation
 * is used (such as {@code 123.1.2} indicating the second third level transformation
 * of the 123rd top level transformation).<p>
 * 
 * In order to track of the nesting of transforms, this class has a stack to represent
 * the Transformers. Each Transformer calls {@link #pushTransform} at the start of a
 * transform and {@link #popTransform} at the end. However the top level transform may
 * be selected from a list of available transformers. To record this activity,
 * {@link #pushAvailable}, {@link #unavailableTransformer} (to record the reason a
 * transformer is rejected), {@link #availableTransformers} (to record the available
 * transformers) and {@link #popAvailable} are called.<p>
 * 
 * @author Alan Davis
 */
public class TransformerDebug implements ApplicationContextAware
{
    private static final String FINISHED_IN = "Finished in ";
    private static final String NO_TRANSFORMERS = "No transformers";

    private Log info;
    private Log logger;
    private NodeService nodeService;
    private MimetypeService mimetypeService;
    private ContentTransformerRegistry transformerRegistry;
    private TransformerConfig transformerConfig;

    private ApplicationContext applicationContext;
    private ContentService contentService;
    private SynchronousTransformClient synchronousTransformClient;
    private Repository repositoryHelper;
    private TransactionService transactionService;

    private enum Call
    {
        AVAILABLE,
        TRANSFORM,
        AVAILABLE_AND_TRANSFORM
    };

    private static class ThreadInfo
    {
        private static final ThreadLocal<ThreadInfo> threadInfo = new ThreadLocal<ThreadInfo>()
        {
            @Override
            protected ThreadInfo initialValue()
            {
                return new ThreadInfo();
            }
        };

        private final Deque<Frame> stack = new ArrayDeque<Frame>();
        private final Deque<String> isTransformableStack = new ArrayDeque<String>();
        private boolean debugOutput = true;
        private StringBuilder sb;
        
        public static Deque<Frame> getStack()
        {
            return threadInfo.get().stack;
        }
        
        public static boolean getDebugOutput()
        {
            return threadInfo.get().debugOutput;
        }

        public static Deque<String> getIsTransformableStack()
        {
            return threadInfo.get().isTransformableStack;
        }
        
        public static boolean setDebugOutput(boolean debugOutput)
        {
            ThreadInfo thisThreadInfo = threadInfo.get();
            boolean orig = thisThreadInfo.debugOutput;
            thisThreadInfo.debugOutput = debugOutput;
            return orig;
        }
        
        public static StringBuilder getStringBuilder()
        {
            return threadInfo.get().sb;
        }

        public static void setStringBuilder(StringBuilder sb)
        {
            threadInfo.get().sb = sb;
        }
    }

    private static class Frame
    {
        private static final AtomicInteger uniqueId = new AtomicInteger(0);

        private int id;
        private final String fromUrl;
        private final String sourceMimetype;
        private final String targetMimetype;
        private final NodeRef sourceNodeRef;
        private final String renditionName;
        private final boolean origDebugOutput;
        private long start;

        private Call callType;
        private Frame parent;
        private int childId;
        private Set<UnavailableTransformer> unavailableTransformers;
        private String failureReason;
        private long sourceSize;
        private String transformerName;
        
        private Frame(Frame parent, String transformerName, String fromUrl, String sourceMimetype, String targetMimetype,
                      long sourceSize, String renditionName, NodeRef sourceNodeRef, Call pushCall, boolean origDebugOutput)
        {
            this.id = -1;
            this.parent = parent;
            this.fromUrl = fromUrl;
            this.transformerName = transformerName;
            this.sourceMimetype = sourceMimetype;
            this.targetMimetype = targetMimetype;
            this.sourceSize = sourceSize;
            this.renditionName = renditionName;
            this.sourceNodeRef = sourceNodeRef;
            this.callType = pushCall;
            this.origDebugOutput = origDebugOutput;
            start = System.currentTimeMillis();
        }
        
        private int getId()
        {
            if (id == -1)
            {
                id = parent == null ? uniqueId.getAndIncrement() : ++parent.childId;
            } 
            return id;
        }
        
        private void setFailureReason(String failureReason)
        {
            this.failureReason = failureReason;
        }

        private String getFailureReason()
        {
            return failureReason;
        }

        private void setSourceSize(long sourceSize)
        {
            this.sourceSize = sourceSize;
        }

        public long getSourceSize()
        {
            return sourceSize;
        }

        private void setTransformerName(String transformerName)
        {
            this.transformerName = transformerName;
        }

        public String getTransformerName()
        {
            return transformerName;
        }
    }

    @Deprecated
    private class UnavailableTransformer implements Comparable<UnavailableTransformer>
    {
        private final String name;
        private final String priority;
        private final long maxSourceSizeKBytes;
        private final transient boolean debug;
        
        UnavailableTransformer(String name, String priority, long maxSourceSizeKBytes, boolean debug)
        {
            this.name = name;
            this.priority = priority;
            this.maxSourceSizeKBytes = maxSourceSizeKBytes;
            this.debug = debug;
        }
        
        @Override
        public int hashCode()
        {
            int hashCode = 37 * name.hashCode();
            hashCode += 37 * maxSourceSizeKBytes;
            return hashCode;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            else if (obj instanceof UnavailableTransformer)
            {
                UnavailableTransformer that = (UnavailableTransformer) obj;
                return
                    EqualsHelper.nullSafeEquals(name, that.name) &&
                    maxSourceSizeKBytes == that.maxSourceSizeKBytes;
            }
            else
            {
                return false;
            }
        }

        @Override
        public int compareTo(UnavailableTransformer o)
        {
            return name.compareTo(o.name);
        }
    }

    public void setTransformerLog(Log transformerLog)
    {
        info = new LogTee(LogFactory.getLog(TransformerLog.class), transformerLog);
    }

    public void setTransformerDebugLog(Log transformerDebugLog)
    {
        logger = new LogTee(LogFactory.getLog(TransformerDebug.class), transformerDebugLog);
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    public void setTransformerRegistry(ContentTransformerRegistry transformerRegistry)
    {
        this.transformerRegistry = transformerRegistry;
    }

    public void setTransformerConfig(TransformerConfig transformerConfig)
    {
        this.transformerConfig = transformerConfig;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }

    private ContentService getContentService()
    {
        if (contentService == null)
        {
            contentService = (ContentService) applicationContext.getBean("contentService");
        }
        return contentService;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    private SynchronousTransformClient getSynchronousTransformClient()
    {
        if (synchronousTransformClient == null)
        {
            synchronousTransformClient = (SynchronousTransformClient) applicationContext.getBean("legacySynchronousTransformClient");
        }
        return synchronousTransformClient;
    }

    public void setSynchronousTransformClient(SynchronousTransformClient transformClient)
    {
        this.synchronousTransformClient = transformClient;
    }

    public Repository getRepositoryHelper()
    {
        if (repositoryHelper == null)
        {
            repositoryHelper = (Repository) applicationContext.getBean("repositoryHelper");
        }
        return repositoryHelper;
    }

    public void setRepositoryHelper(Repository repositoryHelper)
    {
        this.repositoryHelper = repositoryHelper;
    }

    public TransactionService getTransactionService()
    {
        if (transactionService == null)
        {
            transactionService = (TransactionService) applicationContext.getBean("transactionService");
        }
        return transactionService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "transformerLog", info);
        PropertyCheck.mandatory(this, "transformerDebugLog", logger);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "mimetypeService", mimetypeService);
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
            push(null, fromUrl, sourceMimetype, targetMimetype, -1, renditionName,
                    sourceNodeRef, Call.AVAILABLE);
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
                    renditionName, sourceNodeRef, Call.TRANSFORM);
        }
    }

    public void pushTransform(String transformerName, String fromUrl, String sourceMimetype,
                              String targetMimetype, long sourceSize, String renditionName, NodeRef sourceNodeRef)
    {
        if (isEnabled())
        {
            push(transformerName, fromUrl, sourceMimetype, targetMimetype, sourceSize,
                    renditionName, sourceNodeRef, Call.TRANSFORM);
        }
    }

    /**
     * Adds a new level to the stack to get a new request number or nesting number.
     * Called prior to working out what transformers are active
     * and prior to listing the supported mimetypes for an active transformer.
     */
    public void pushMisc()
    {
        if (isEnabled())
        {
            push(null, null, null, null, -1, null, null, Call.AVAILABLE);
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
    
    private void push(String transformerName, String fromUrl, String sourceMimetype, String targetMimetype,
                      long sourceSize, String renditionName, NodeRef sourceNodeRef, Call callType)
    {
        Deque<Frame> ourStack = ThreadInfo.getStack();
        Frame frame = ourStack.peek();

        if (callType == Call.TRANSFORM && frame != null && frame.callType == Call.AVAILABLE)
        {
            frame.setTransformerName(transformerName);
            frame.setSourceSize(sourceSize);
            frame.callType = Call.AVAILABLE_AND_TRANSFORM;
        }

        // Create a new frame. Logging level is set to trace if the file size is 0
        boolean origDebugOutput = ThreadInfo.setDebugOutput(ThreadInfo.getDebugOutput() && sourceSize != 0);
        frame = new Frame(frame, transformerName, fromUrl, sourceMimetype, targetMimetype, sourceSize, renditionName,
                sourceNodeRef, callType, origDebugOutput);
        ourStack.push(frame);
            
        if (callType == Call.TRANSFORM)
        {
            // Log the basic info about this transformation
            logBasicDetails(frame, sourceSize, renditionName, transformerName, (ourStack.size() == 1));
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
            logBasicDetails(frame, sourceSize, renditionName,
                    calledFrom + ((transformers.size() == 0) ? " NO transformers" : ""), firstLevel);

            // Report available and unavailable transformers
            char c = 'a';
            int longestNameLength = getLongestTransformerNameLength(transformers, frame);
            for (ContentTransformer trans : transformers)
            {
                String name = getName(trans);
                int padName = longestNameLength - name.length() + 1;
                // TODO replace with call to RenditionService2 or leave as a deprecated method using ContentService.
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
            '[' +
             (isComponentTransformer(transformer)
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
        String mimetypes = firstTransformer
                ? getMimetypeExt(sourceMimetype)+getMimetypeExt(targetMimetype)
                : spaces(10);
        char c = (char)('a'+transformerCount);
        String priority = gePriority(transformer, sourceMimetype, targetMimetype);
        log(mimetypes+
                "  "+c+") " + priority + ' ' + getName(transformer)+' '+ms(transformer.getTransformationTime(sourceMimetype, targetMimetype))+
                ' '+fileSize((maxSourceSizeKBytes > 0) ? maxSourceSizeKBytes*1024 : maxSourceSizeKBytes)+
                (maxSourceSizeKBytes == 0 ? " disabled" : ""));
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
    
    private void logBasicDetails(Frame frame, long sourceSize, String renditionName, String message, boolean firstLevel)
    {
        // Log the source URL, but there is no point if the parent has logged it
        if (frame.fromUrl != null && (firstLevel || frame.id != 1))
        {
            log(frame.fromUrl, false);
        }
        log(frame.sourceMimetype+' '+frame.targetMimetype, false);
        
        String fileName = getFileName(frame.sourceNodeRef, firstLevel, sourceSize);
        log(getMimetypeExt(frame.sourceMimetype)+getMimetypeExt(frame.targetMimetype) +
                ((fileName != null) ? fileName+' ' : "")+
                ((sourceSize >= 0) ? fileSize(sourceSize)+' ' : "") +
                (firstLevel && renditionName != null ? "-- "+renditionName+" -- " : "") + message);
        if (firstLevel)
        {
            String nodeRef = getNodeRef(frame.sourceNodeRef, firstLevel, sourceSize);
            if (!nodeRef.isEmpty())
            {
                log(nodeRef);
            }
        }
    }

    public void debug(String sourceMimetype, String targetMimetype, NodeRef sourceNodeRef, long sourceSize,
                      String renditionName, String message)
    {
        String fileName = getFileName(sourceNodeRef, true, -1);
        log("              "+getMimetypeExt(sourceMimetype)+getMimetypeExt(targetMimetype) +
                ((fileName != null) ? fileName+' ' : "")+
                ((sourceSize >= 0) ? fileSize(sourceSize)+' ' : "") +
                (renditionName != null ? "-- "+renditionName+" -- " : "") + message);
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
     * Called after performing a transform.
     */
    public void popTransform()
    {
        if (isEnabled())
        {
            pop(Call.TRANSFORM, false, false);
        }
    }

    /**
     * Removes a frame from the stack. Called prior to working out what transformers are active
     * and prior to listing the supported mimetypes for an active transformer.
     */
    public void popMisc()
    {
        if (isEnabled())
        {
            pop(Call.AVAILABLE, ThreadInfo.getStack().size() > 1, false);
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

    private int pop(Call callType, boolean suppressFinish, boolean suppressChecking)
    {
        int id = -1;
        Deque<Frame> ourStack = ThreadInfo.getStack();
        if (!ourStack.isEmpty())
        {
            Frame frame = ourStack.peek();
            id = frame.getId();

            if ((frame.callType == callType) ||
                (frame.callType == Call.AVAILABLE_AND_TRANSFORM && callType == Call.AVAILABLE))
            {
                int size = ourStack.size();
                String ms = ms(System.currentTimeMillis() - frame.start);

                logInfo(frame, size, ms);
                
                boolean firstLevel = size == 1;
                if (!suppressFinish && (firstLevel || logger.isTraceEnabled()))
                {
                    log(FINISHED_IN + ms +
                        (frame.callType == Call.AVAILABLE && !suppressChecking? " Just checking if a transformer is available" : "") +
                        (firstLevel ? "\n" : ""), 
                        firstLevel);
                }
                
                setDebugOutput(frame.origDebugOutput);
                ourStack.pop();
            }
        }
        return id;
    }

    private void logInfo(Frame frame, int size, String ms)
    {
        if (info.isDebugEnabled())
        {
            String failureReason = frame.getFailureReason();
            boolean firstLevel = size == 1;
            String sourceExt = getMimetypeExt(frame.sourceMimetype);
            String targetExt = getMimetypeExt(frame.targetMimetype);
            String fileName = getFileName(frame.sourceNodeRef, firstLevel, frame.sourceSize);
            long sourceSize = frame.getSourceSize();
            String transformerName = frame.getTransformerName();
            String level = null;
            boolean debug = false;
            if (NO_TRANSFORMERS.equals(failureReason))
            {
                debug = firstLevel;
                level = "INFO";
                failureReason = NO_TRANSFORMERS;
                
                // If trace and trace is disabled do nothing
                if (debug || info.isTraceEnabled())
                {
                    // Work out size reason that there are no transformers
                    if (frame.unavailableTransformers != null)
                    {
                        level = "WARN";
                        long smallestMaxSourceSizeKBytes = Long.MAX_VALUE;
                        for (UnavailableTransformer unavailable: frame.unavailableTransformers)
                        {
                            if (smallestMaxSourceSizeKBytes > unavailable.maxSourceSizeKBytes && unavailable.maxSourceSizeKBytes > 0)
                            {
                                smallestMaxSourceSizeKBytes = unavailable.maxSourceSizeKBytes;
                            }
                        }
                        smallestMaxSourceSizeKBytes = smallestMaxSourceSizeKBytes == Long.MAX_VALUE ? 0 : smallestMaxSourceSizeKBytes;
                        failureReason = "No transformers as file is > "+fileSize(smallestMaxSourceSizeKBytes*1024);
                    }
                }
            }
            else if (frame.callType == Call.TRANSFORM)
            {
                level = failureReason == null || failureReason.length() == 0 ? "INFO" : "ERROR";
                
                // Use TRACE logging for all but the first TRANSFORM
                debug = size == 1 || (size == 2 && ThreadInfo.getStack().peekLast().callType != Call.TRANSFORM);
            }
// Comment out for the moment
//            else if (firstLevel && frame.callType == Call.AVAILABLE)
//            {
//                level = "INFO";
//                debug = true;
//                failureReason = "checking availability";
//            }
            
            if (level != null)
            {
                infoLog(getReference(debug, false), sourceExt, targetExt, level, fileName, sourceSize, transformerName, failureReason, ms, debug);
            }
        }
    }
    
    private void infoLog(String reference, String sourceExt, String targetExt, String level, String fileName,
            long sourceSize, String transformerName, String failureReason, String ms, boolean debug)
    {
        String message =
                reference +
                sourceExt +
                targetExt +
                (level == null ? "" : level+' ') +
                (fileName == null ? "" : fileName) +
                (sourceSize >= 0 ? ' '+fileSize(sourceSize) : "") +
                ' '+ms +
                (transformerName == null ? "" : ' '+transformerName) +
                (failureReason == null ? "" : ' '+failureReason.trim());
        if (debug)
        {
            info.debug(message);
        }
        else
        {
            info.trace(message);
        }
    }

    /**
     * Indicates if any logging is required.
     */
    public boolean isEnabled()
    {
        // Don't check ThreadInfo.getDebugOutput() as availableTransformers() may upgrade from trace to debug.
        return logger.isDebugEnabled() || info.isDebugEnabled() || ThreadInfo.getStringBuilder() != null;
    }
    
    /**
     * Enable or disable debug log output. Normally used to hide calls to 
     * getTransformer as trace rather than debug level log messages. There
     * are lots of these and it makes it hard to see what is going on.
     * @param debugOutput if {@code true} both debug and trace is generated. Otherwise all output is trace.
     * @return the original value.
     */
    public static boolean setDebugOutput(boolean debugOutput)
    {
        return ThreadInfo.setDebugOutput(debugOutput);
    }

    /**
     * Log a message prefixed with the current transformation reference.
     * @param message
     */
    public void debug(String message)
    {
        if (isEnabled() && message != null)
        {
            log(message);
        }
    }

    /**
     * Log a message prefixed with the current transformation reference
     * and include a exception, suppressing the stack trace if repeated
     * as we return up the stack of transformers.
     * @param message
     */
    public void debug(String message, Throwable t)
    {
        if (isEnabled())
        {
            // Trim messages of the form: "Failed... : \n   reader:...\n    writer:..."
            String msg = t.getMessage();
            if (msg != null)
            {
                int i = msg.indexOf(": \n");
                if (i != -1)
                {
                    msg = msg.substring(0, i);
                }
                log(message + ' ' + msg);
            }
            else
            {
                log(message);
            }
            
            
            Deque<Frame> ourStack = ThreadInfo.getStack();
            if (!ourStack.isEmpty())
            {
                Frame frame = ourStack.peek();
                frame.setFailureReason(message +' '+ getRootCauseMessage(t));
            }
        }
    }

    private String getRootCauseMessage(Throwable t)
    {
        Throwable cause = t;
        while (cause != null)
        {
            t = cause;
            cause = t.getCause();
        }
        
        String message = t.getMessage();
        if (message == null || message.length() == 0)
        {
            message = t.getClass().getSimpleName();
        }
        return message;
    }

    private void log(String message)
    {
        log(message, true);
    }
    
    private void log(String message, boolean debug)
    {
        log(message, null, debug);
    }
    
    private void log(String message, Throwable t, boolean debug)
    {
        if (debug && ThreadInfo.getDebugOutput() && logger.isDebugEnabled())
        {
            logger.debug(getReference(false, false)+message, t);
        }
        else if (logger.isTraceEnabled())
        {
            logger.trace(getReference(false, false)+message, t);
        }

        if (debug)
        {
            StringBuilder sb = ThreadInfo.getStringBuilder();
            if (sb != null)
            {
                sb.append(getReference(false, true));
                sb.append(message);
                if (t != null)
                {
                    sb.append(t.getMessage());
                }
                sb.append('\n');
            }
        }
    }

    /**
     * Sets the cause of a transformation failure, so that only the
     * message of the Throwable is reported later rather than the full
     * stack trace over and over.
     */
    public <T extends Throwable> T setCause(T t)
    {
        return t;
    }
    
    /**
     * Returns the current StringBuilder (if any) being used to capture debug
     * information for the current Thread.
     */
    public StringBuilder getStringBuilder()
    {
        return ThreadInfo.getStringBuilder();
    }

    /**
     * Sets the StringBuilder to be used to capture debug information for the
     * current Thread.
     */
    public void setStringBuilder(StringBuilder sb)
    {
        ThreadInfo.setStringBuilder(sb);
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
                        try
                        {
                            pushMisc();
                            int transformerCount = 0;
                            for (ContentTransformer transformer: availableTransformer)
                            {
                                if (!onlyNonDeterministic || transformerCount < 2 ||
                                        priority == transformerConfig.getPriority(transformer, sourceMimetype, targetMimetype))
                                {
                                    long maxSourceSizeKBytes = transformer.getMaxSourceSizeKBytes(
                                            sourceMimetype, targetMimetype, options);
                                    activeTransformer(sourceMimetype, targetMimetype,
                                            transformerCount, transformer, maxSourceSizeKBytes, transformerCount++ == 0);
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
        finally
        {
            popMisc();
            setStringBuilder(null);
        }
        stripFinishedLine(sb);
        return stripLeadingNumber(sb);
    }
    
    /**
     * Removes the final "Finished in..." message from a StringBuilder
     * @param sb
     */
    private void stripFinishedLine(StringBuilder sb)
    {
        if (sb != null)
        {
            int i = sb.lastIndexOf(FINISHED_IN);
            if (i != -1)
            {
                sb.setLength(i);
                i = sb.lastIndexOf("\n", i);
                sb.setLength(i != -1 ? i : 0);
            }
        }
    }
    
    /**
     * Strips the leading number in a reference
     */
    private String stripLeadingNumber(StringBuilder sb)
    {
        return sb == null
            ? null
            : Pattern.compile("^\\d+\\.", Pattern.MULTILINE).matcher(sb).replaceAll("");
    }

    /**
     * Returns a collection of mimetypes ordered by extension, but unlike the version in MimetypeService
     * throws an exception if the sourceExtension is supplied but does not match a mimetype.
     * @param sourceExtension to restrict the collection to one entry
     * @throws IllegalArgumentException if there is no match. The message indicates this.
     */
    public Collection<String> getSourceMimetypes(String sourceExtension)
    {
        Collection<String> sourceMimetypes = mimetypeService.getMimetypes(sourceExtension);
        if (sourceMimetypes.isEmpty())
        {
            throw new IllegalArgumentException("Unknown source extension "+sourceExtension);
        }
        return sourceMimetypes;
    }

    /**
     * Identical to getSourceMimetypes for the target, but avoids doing the look up if the sourceExtension
     * is the same as the tragetExtension, so will have the same result.
     * @param sourceExtension used to restrict the sourceMimetypes
     * @param targetExtension to restrict the collection to one entry
     * @param sourceMimetypes that match the sourceExtension
     * @throws IllegalArgumentException if there is no match. The message indicates this.
     */
    public Collection<String> getTargetMimetypes(String sourceExtension, String targetExtension,
            Collection<String> sourceMimetypes)
    {
        Collection<String> targetMimetypes =
                (targetExtension == null && sourceExtension == null) ||
                (targetExtension != null && targetExtension.equals(sourceExtension))
                ? sourceMimetypes
                : mimetypeService.getMimetypes(targetExtension);
        if (targetMimetypes.isEmpty())
        {
            throw new IllegalArgumentException("Unknown target extension "+targetExtension);
        }
        return targetMimetypes;
    }
    
    /**
     * Returns a N.N.N style reference to the transformation.
     * @param firstLevelOnly indicates if only the top level should be included and no extra padding.
     * @param overrideFirstLevel if the first level id should just be set to 1 (used in test methods)
     * @return a padded (fixed length) reference.
     */
    private String getReference(boolean firstLevelOnly, boolean overrideFirstLevel)
    {
        StringBuilder sb = new StringBuilder("");
        Frame frame = null;
        Iterator<Frame> iterator = ThreadInfo.getStack().descendingIterator();
        int lengthOfFirstId = 0;
        boolean firstLevel = true;
        while (iterator.hasNext())
        {
            frame = iterator.next();
            if (firstLevel)
            {
                if (!overrideFirstLevel)
                {
                    sb.append(frame.getId());
                }
                else
                {
                    sb.append("1");
                }
                lengthOfFirstId = sb.length();
                if (firstLevelOnly)
                {
                    break;
                }
            }
            else
            {
                if (sb.length() != 0)
                {
                    sb.append('.');
                }
                sb.append(frame.getId());
            }
            firstLevel = false;
        }
        if (frame != null)
        {
            if (firstLevelOnly)
            {
                sb.append(' ');
            }
            else
            {
            sb.append(spaces(13-sb.length()+lengthOfFirstId)); // Try to pad to level 7
            }
        }
        return sb.toString();
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

    public String getFileName(NodeRef sourceNodeRef, boolean firstLevel, long sourceSize)
    {
        return getFileNameOrNodeRef(sourceNodeRef, firstLevel, sourceSize, true);
    }

    private String getNodeRef(NodeRef sourceNodeRef, boolean firstLevel, long sourceSize)
    {
        return getFileNameOrNodeRef(sourceNodeRef, firstLevel, sourceSize, false);
    }
    
    private String getFileNameOrNodeRef(NodeRef sourceNodeRef, boolean firstLevel, long sourceSize, boolean getName)
    {
        String result = getName ? null : "";
        if (sourceNodeRef != null)
        {
            try
            {
                result = getName
                        ? (String)nodeService.getProperty(sourceNodeRef, ContentModel.PROP_NAME)
                        : sourceNodeRef.toString()+" ";
            }
            catch (RuntimeException e)
            {
                ; // ignore (normally InvalidNodeRefException) but we should ignore other RuntimeExceptions too
            }
        }
        if (result == null)
        {
            if (!firstLevel)
            {
                result = getName ? "<<TemporaryFile>>" : "";
            }
            else if (sourceSize < 0)
            {
                // fileName = "<<AnyFile>>"; commented out as it does not add to debug readability
            }
        }
        return result;
    }

    private String getMimetypeExt(String mimetype)
    {
        StringBuilder sb = new StringBuilder("");
        if (mimetypeService == null)
        {
            sb.append(mimetype);
        }
        else
        {
            String mimetypeExt = mimetypeService.getExtension(mimetype);
            sb.append(mimetypeExt);
            sb.append(spaces(4-mimetypeExt.length()));   // Pad to normal max ext (4)
        }
        sb.append(' ');
        return sb.toString();
    }
    
    private String spaces(int i)
    {
        StringBuilder sb = new StringBuilder("");
        while (--i >= 0)
        {
            sb.append(' ');
        }
        return sb.toString();
    }
    
    public String ms(long time)
    {
        return String.format("%,d ms", time);
    }
    
    public String fileSize(long size)
    {
        if (size < 0)
        {
            return "unlimited";
        }
        if (size == 1)
        {
            return "1 byte";
        }
        final String[] units = new String[] { "bytes", "KB", "MB", "GB", "TB" };
        long divider = 1;
        for(int i = 0; i < units.length-1; i++)
        {
            long nextDivider = divider * 1024;
            if(size < nextDivider)
            {
                return fileSizeFormat(size, divider, units[i]);
            }
            divider = nextDivider;
        }
        return fileSizeFormat(size, divider, units[units.length-1]);
    }
    
    private String fileSizeFormat(long size, long divider, String unit)
    {
        size = size * 10 / divider;
        int decimalPoint = (int) size % 10;
        
        StringBuilder sb = new StringBuilder();
        sb.append(size/10);
        if (decimalPoint != 0)
        {
            sb.append(".");
            sb.append(decimalPoint);
        }
        sb.append(' ');
        sb.append(unit);

        return sb.toString();
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
     * Debugs a request to the Transform Service
     */
    public int debugTransformServiceRequest(String sourceMimetype, long sourceSize, NodeRef sourceNodeRef,
                                             int contentHashcode, String fileName, String targetMimetype,
                                            String renditionName)
    {
        pushMisc();
        debug(getMimetypeExt(sourceMimetype)+getMimetypeExt(targetMimetype) +
              ((fileName != null) ? fileName+' ' : "")+
              ((sourceSize >= 0) ? fileSize(sourceSize)+' ' : "") +
              (renditionName != null ? "-- "+renditionName+" -- " : "") + " RenditionService2");
        debug(sourceNodeRef.toString() + ' ' +contentHashcode);
        debug(" **a)  [01] TransformService");
        return pop(Call.AVAILABLE, true, false);
    }

    /**
     * Debugs a response to the Transform Service
     */
    public void debugTransformServiceResponse(NodeRef sourceNodeRef, int contentHashcode,
                                              long requested, int seq, String sourceExt, String targetExt, String msg)
    {
        pushMisc();
        Frame frame = ThreadInfo.getStack().getLast();
        frame.id = seq;
        boolean suppressFinish = seq == -1 || requested == -1;
        if (!suppressFinish)
        {
            frame.start = requested;
// TODO Create a dummy (available == false) transformer for TransformService before we can record the TS's stats
//            String sourceMimetype = mimetypeService.getMimetype(sourceExt);
//            String targetMimetype = mimetypeService.getMimetype(targetExt);
//            long ms = System.currentTimeMillis()-requested;
//            AbstractContentTransformer2 transformer = null;
//            transformer.recordTime(sourceMimetype, targetMimetype, ms);
        }
        debug(msg);
        debug(sourceNodeRef.toString() + ' ' +contentHashcode);
        pop(Call.AVAILABLE, suppressFinish, true);
    }

    public String testTransform(String sourceExtension, String targetExtension, String renditionName)
    {
        return new TestTransform().run(sourceExtension, targetExtension, renditionName);
    }

    /**
     * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
     */
    @Deprecated
    public String testTransform(final String transformerName, String sourceExtension,
            String targetExtension, String renditionName)
    {
        logger.error("The testTransform operation for a specific transformer is no longer supported. " +
                "Direct transforms have been deprecated in favour of async renditions. " +
                "Request redirected to the version of this method without a transformerName.");
        return testTransform(sourceExtension, targetExtension, renditionName);
    }
    
    public String[] getTestFileExtensionsAndMimetypes()
    {
        List<String> sourceExtensions = new ArrayList<String>();
        Collection<String> sourceMimetypes = mimetypeService.getMimetypes(null);
        for (String sourceMimetype: sourceMimetypes)
        {
            String sourceExtension = mimetypeService.getExtension(sourceMimetype);
            if (loadQuickTestFile(sourceExtension) != null)
            {
                sourceExtensions.add(sourceExtension+" - "+sourceMimetype);
            }
        }

        return sourceExtensions.toArray(new String[sourceExtensions.size()]);
    }

    /**
     * Load one of the "The quick brown fox" files from the classpath.
     * @param extension required, eg <b>txt</b> for the file quick.txt
     * @return Returns a test resource loaded from the classpath or <tt>null</tt> if
     *      no resource could be found.
     */
    private URL loadQuickTestFile(String extension)
    {
        final URL result;
        
        URL url = this.getClass().getClassLoader().getResource("quick/quick." + extension);
        if (url == null)
        {
            result = null;
        }
        else
        {
            // Note that this URL may point to a file on the filesystem or to an entry in a jar file.
            // The handling should be the same either way.
            result = url;
        }
        
        return result;
    }

    @Deprecated
    private class TestTransform
    {
        protected LinkedList<NodeRef> nodesToDeleteAfterTest = new LinkedList<NodeRef>();

        String run(String sourceExtension, String targetExtension, String renditionName)
        {
            RetryingTransactionHelper.RetryingTransactionCallback<String> makeNodeCallback = new RetryingTransactionHelper.RetryingTransactionCallback<String>()
            {
                public String execute() throws Throwable
                {
                    return runWithinTransaction(sourceExtension, targetExtension);
                }
            };
            return getTransactionService().getRetryingTransactionHelper().doInTransaction(makeNodeCallback, false, true);
        }

        private String runWithinTransaction(String sourceExtension, String targetExtension)
        {
            String targetMimetype = getMimetype(targetExtension, false);
            String sourceMimetype = getMimetype(sourceExtension, true);
            File tempFile = TempFileProvider.createTempFile(
                    "TestTransform_" + sourceExtension + "_", "." + targetExtension);
            ContentWriter writer = new FileContentWriter(tempFile);
            writer.setMimetype(targetMimetype);

            NodeRef sourceNodeRef = null;
            StringBuilder sb = new StringBuilder();
            try
            {
                setStringBuilder(sb);
                sourceNodeRef = createSourceNode(sourceExtension, sourceMimetype);
                ContentReader reader = contentService.getReader(sourceNodeRef, ContentModel.PROP_CONTENT);
                SynchronousTransformClient synchronousTransformClient = getSynchronousTransformClient();
                Map<String, String> actualOptions = Collections.emptyMap();
                synchronousTransformClient.transform(reader, writer, actualOptions, null, sourceNodeRef);
            }
            catch (Exception e)
            {
                logger.debug("Unexpected test transform error", e);
            }
            finally
            {
                setStringBuilder(null);
                deleteSourceNode(sourceNodeRef);
            }
            return sb.toString();
        }

        private String getMimetype(String extension, boolean isSource)
        {
            String mimetype = null;
            if (extension != null)
            {
                Iterator<String> iterator = mimetypeService.getMimetypes(extension).iterator();
                if (iterator.hasNext())
                {
                    mimetype = iterator.next(); 
                }
            }
            if (mimetype == null)
            {
                throw new IllegalArgumentException("Unknown "+(isSource ? "source" : "target")+" extension: "+extension);
            }
            return mimetype;
        }

        public NodeRef createSourceNode(String extension, String sourceMimetype)
        {
            // Create a content node which will serve as test data for our transformations.
            RetryingTransactionHelper.RetryingTransactionCallback<NodeRef> makeNodeCallback = new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
            {
                public NodeRef execute() throws Throwable
                {
                    // Create a source node loaded with a quick file.
                    URL url = loadQuickTestFile(extension);
                    URI uri = url.toURI();
                    File sourceFile = new File(uri);

                    final NodeRef companyHome = getRepositoryHelper().getCompanyHome();

                    Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                    String localName = "TestTransform." + extension;
                    props.put(ContentModel.PROP_NAME, localName);
                    NodeRef node = nodeService.createNode(
                            companyHome,
                            ContentModel.ASSOC_CONTAINS,
                            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, localName),
                            ContentModel.TYPE_CONTENT,
                            props).getChildRef();

                    ContentWriter writer = getContentService().getWriter(node, ContentModel.PROP_CONTENT, true);
                    writer.setMimetype(sourceMimetype);
                    writer.setEncoding("UTF-8");
                    writer.putContent(sourceFile);

                    return node;
                }
            };
            NodeRef contentNodeRef = getTransactionService().getRetryingTransactionHelper().doInTransaction(makeNodeCallback);
            this.nodesToDeleteAfterTest.add(contentNodeRef);
            return contentNodeRef;
        }

        public void deleteSourceNode(NodeRef sourceNodeRef)
        {
            if (sourceNodeRef != null)
            {
                getTransactionService().getRetryingTransactionHelper().doInTransaction(
                        (RetryingTransactionHelper.RetryingTransactionCallback<Void>) () ->
                        {
                            if (nodeService.exists(sourceNodeRef))
                            {
                                nodeService.deleteNode(sourceNodeRef);
                            }
                            return null;
                        });
            }
        }
    }
}
