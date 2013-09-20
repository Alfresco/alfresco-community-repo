/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.content.transform;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.LogTee;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ResourceUtils;

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
public class TransformerDebug
{
    private static final String FINISHED_IN = "Finished in ";
    private static final String NO_TRANSFORMERS = "No transformers";

    private final Log logger;
    private final Log info;

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
        private final TransformationOptions options;
        private final boolean origDebugOutput;
        private final long start;

        private Call callType;
        private Frame parent;
        private int childId;
        private Set<UnavailableTransformer> unavailableTransformers;
        private String failureReason;
        private long sourceSize;
        private String transformerName;
        
        private Frame(Frame parent, String transformerName, String fromUrl, String sourceMimetype, String targetMimetype,
                long sourceSize, TransformationOptions options, Call pushCall, boolean origDebugOutput)
        {
            this.id = -1;
            this.parent = parent;
            this.fromUrl = fromUrl;
            this.transformerName = transformerName;
            this.sourceMimetype = sourceMimetype;
            this.targetMimetype = targetMimetype;
            this.sourceSize = sourceSize;
            this.options = options;
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
    
    private class UnavailableTransformer
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
    }
    
    private final NodeService nodeService;
    private final MimetypeService mimetypeService;
    private final ContentTransformerRegistry transformerRegistry;
    private final TransformerConfig transformerConfig;
    private ContentService contentService;
    
    /**
     * Constructor
     */
    public TransformerDebug(NodeService nodeService, MimetypeService mimetypeService, 
            ContentTransformerRegistry transformerRegistry, TransformerConfig transformerConfig,
            Log transformerLog, Log transformerDebugLog)
    {
        this.nodeService = nodeService;
        this.mimetypeService = mimetypeService;
        this.transformerRegistry = transformerRegistry;
        this.transformerConfig = transformerConfig;
        
        logger = new LogTee(LogFactory.getLog(TransformerDebug.class), transformerDebugLog);
        info = new LogTee(LogFactory.getLog(TransformerLog.class), transformerLog);
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * Called prior to working out what transformers are available.
     */
    public void pushAvailable(String fromUrl, String sourceMimetype, String targetMimetype,
            TransformationOptions options)
    {
        if (isEnabled())
        {
            push(null, fromUrl, sourceMimetype, targetMimetype, -1, options, Call.AVAILABLE);
        }
    }
    
    /**
     * Called prior to performing a transform.
     */
    public void pushTransform(ContentTransformer transformer, String fromUrl, String sourceMimetype,
            String targetMimetype, long sourceSize, TransformationOptions options)
    {
        if (isEnabled())
        {
            push(getName(transformer), fromUrl, sourceMimetype, targetMimetype, sourceSize,
                    options, Call.TRANSFORM);
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
            push(null, null, null, null, -1, null, Call.AVAILABLE);
        }
    }
    
    /**
     * Called prior to calling a nested isTransformable.
     */
    public void pushIsTransformableSize(ContentTransformer transformer)
    {
        if (isEnabled())
        {
            ThreadInfo.getIsTransformableStack().push(getName(transformer));
        }
    }
    
    private void push(String transformerName, String fromUrl, String sourceMimetype, String targetMimetype,
            long sourceSize, TransformationOptions options, Call callType)
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
        frame = new Frame(frame, transformerName, fromUrl, sourceMimetype, targetMimetype, sourceSize, options, callType, origDebugOutput);
        ourStack.push(frame);
            
        if (callType == Call.TRANSFORM)
        {
            // Log the basic info about this transformation
            logBasicDetails(frame, sourceSize, options.getUse(), transformerName, (ourStack.size() == 1));
        }
    }
    
    /**
     * Called to identify a transformer that cannot be used during working out
     * available transformers.
     */
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
                    frame.unavailableTransformers = new HashSet<UnavailableTransformer>();
                }
                String priority = gePriority(transformer, sourceMimetype, targetMimetype);
                frame.unavailableTransformers.add(new UnavailableTransformer(name, priority, maxSourceSizeKBytes, debug));
            }
        }
    }

    /**
     * Called once all available transformers have been identified.
     */
    public void availableTransformers(List<ContentTransformer> transformers, long sourceSize, 
            TransformationOptions options, String calledFrom)
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
            logBasicDetails(frame, sourceSize, options.getUse(),
                    calledFrom + ((transformers.size() == 0) ? " NO transformers" : ""), firstLevel);

            // Report available and unavailable transformers
            char c = 'a';
            int longestNameLength = getLongestTransformerNameLength(transformers, frame);
            for (ContentTransformer trans : transformers)
            {
                String name = getName(trans);
                int padName = longestNameLength - name.length() + 1;
                long maxSourceSizeKBytes = trans.getMaxSourceSizeKBytes(frame.sourceMimetype, frame.targetMimetype, frame.options);
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

    public void inactiveTransformer(ContentTransformer transformer)
    {
        log(getName(transformer)+' '+ms(transformer.getTransformationTime(null, null))+" INACTIVE");
    }

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
    
    private void logBasicDetails(Frame frame, long sourceSize, String use, String message, boolean firstLevel)
    {
        // Log the source URL, but there is no point if the parent has logged it
        if (frame.fromUrl != null && (firstLevel || frame.id != 1))
        {
            log(frame.fromUrl, false);
        }
        log(frame.sourceMimetype+' '+frame.targetMimetype, false);
        
        String fileName = getFileName(frame.options, firstLevel, sourceSize);
        log(getMimetypeExt(frame.sourceMimetype)+getMimetypeExt(frame.targetMimetype) +
                ((fileName != null) ? fileName+' ' : "")+
                ((sourceSize >= 0) ? fileSize(sourceSize)+' ' : "") +
                (firstLevel && use != null ? "-- "+use+" -- " : "") + message);
    }

    /**
     * Called after working out what transformers are available and any
     * resulting transform has been called.
     */
    public void popAvailable()
    {
        if (isEnabled())
        {
            pop(Call.AVAILABLE, false);
        }
    }
    
    /**
     * Called after performing a transform.
     */
    public void popTransform()
    {
        if (isEnabled())
        {
            pop(Call.TRANSFORM, false);
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
            pop(Call.AVAILABLE, ThreadInfo.getStack().size() > 1);
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

    private void pop(Call callType, boolean suppressFinish)
    {
        Deque<Frame> ourStack = ThreadInfo.getStack();
        if (!ourStack.isEmpty())
        {
            Frame frame = ourStack.peek();

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
                        (frame.callType == Call.AVAILABLE ? " Transformer NOT called" : "") +
                        (firstLevel ? "\n" : ""), 
                        firstLevel);
                }
                
                setDebugOutput(frame.origDebugOutput);
                ourStack.pop();
            }
        }
    }

    private void logInfo(Frame frame, int size, String ms)
    {
        if (info.isDebugEnabled())
        {
            String failureReason = frame.getFailureReason();
            boolean firstLevel = size == 1;
            String sourceExt = getMimetypeExt(frame.sourceMimetype);
            String targetExt = getMimetypeExt(frame.targetMimetype);
            String fileName = getFileName(frame.options, firstLevel, frame.sourceSize);
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
     * @param use to which the transformation will be put (such as "Index", "Preview", null).
     */
    public String transformationsByTransformer(String transformerName, boolean toString, boolean format42, String use)
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
        options.setUse(use);
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
     * @param use to which the transformation will be put (such as "Index", "Preview", null).
     */
    public String transformationsByExtension(String sourceExtension, String targetExtension, boolean toString,
            boolean format42, boolean onlyNonDeterministic, String use)
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
        options.setUse(use);
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
        
        StringBuilder sb = new StringBuilder(name);
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

    public String getFileName(TransformationOptions options, boolean firstLevel, long sourceSize)
    {
        String fileName = null;
        if (options != null)
        {
            try
            {
                NodeRef sourceNodeRef = options.getSourceNodeRef();
                fileName = (String)nodeService.getProperty(sourceNodeRef, ContentModel.PROP_NAME);
            }
            catch (RuntimeException e)
            {
                ; // ignore (normally InvalidNodeRefException) but we should ignore other RuntimeExceptions too
            }
        }
        if (fileName == null)
        {
            if (!firstLevel)
            {
                fileName = "<<TemporaryFile>>";
            }
            else if (sourceSize < 0)
            {
                // fileName = "<<AnyFile>>"; commented out as it does not add to debug readability
            }
        }
        return fileName;
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
     */
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

    public String testTransform(String sourceExtension, String targetExtension, String use)
    {
        return new TestTransform()
        {
            protected void transform(ContentReader reader, ContentWriter writer, TransformationOptions options)
            {
                contentService.transform(reader, writer, options);
            }
        }.run(sourceExtension, targetExtension, use);
    }
    
    public String testTransform(final String transformerName, String sourceExtension,
            String targetExtension, String use)
    {
        final ContentTransformer transformer = transformerRegistry.getTransformer(transformerName);
        return new TestTransform()
        {
            protected String isTransformable(String sourceMimetype, long sourceSize, String targetMimetype, TransformationOptions options)
            {
                return transformer.isTransformable(sourceMimetype, sourceSize, targetMimetype, options)
                    ? null
                    : transformerName+" does not support this transformation.";
            }

            protected void transform(ContentReader reader, ContentWriter writer, TransformationOptions options)
            {
                transformer.transform(reader, writer, options);
            }
        }.run(sourceExtension, targetExtension, use);
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
    private File loadQuickTestFile(String extension)
    {
        try
        {
            URL url = this.getClass().getClassLoader().getResource("quick/quick." + extension);
            if (url == null)
            {
                return null;
            }
            return ResourceUtils.getFile(url);
        }
        catch (IOException e)
        {
            return null;
        }
    }

    private abstract class TestTransform
    {
        String run(String sourceExtension, String targetExtension, String use)
        {
            String debug;
            
            String targetMimetype = getMimetype(targetExtension, false);
            String sourceMimetype = getMimetype(sourceExtension, true);
            File sourceFile = loadQuickTestFile(sourceExtension);
            if (sourceFile == null)
            {
                throw new IllegalArgumentException("There is no test file with a "+sourceExtension+" extension.");
            }

            ContentReader reader = new FileContentReader(sourceFile);
            reader.setMimetype(sourceMimetype);
            File tempFile = TempFileProvider.createTempFile(
                    "TestTransform_" + sourceExtension + "_", "." + targetExtension);
            ContentWriter writer = new FileContentWriter(tempFile);
            writer.setMimetype(targetMimetype);

            long sourceSize = reader.getSize();
            TransformationOptions options = new TransformationOptions();
            options.setUse(use);

            debug = isTransformable(sourceMimetype, sourceSize, targetMimetype, options);
            if (debug == null)
            {
                StringBuilder sb = new StringBuilder();
                try
                {
                    setStringBuilder(sb);
                    transform(reader, writer, options);
                }
                catch (AlfrescoRuntimeException e)
                {
                    sb.append(e.getMessage());
                }
                finally
                {
                    setStringBuilder(null);
                }
                debug = sb.toString();
            }
            return debug;
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

        protected String isTransformable(String sourceMimetype, long sourceSize, String targetMimetype, TransformationOptions options)
        {
            return null;
        }
        
        protected abstract void transform(ContentReader reader, ContentWriter writer, TransformationOptions options);
    }
}
