/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.EqualsHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private static final Log logger = LogFactory.getLog(TransformerDebug.class);
    private static final TransformerLog info = new TransformerLog();
    private static final String NO_TRANSFORMERS = "No transformers";

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
        private int childId;
        private Set<UnavailableTransformer> unavailableTransformers;
        private String failureReason;
        private long sourceSize;
        private String transformerName;
        
        private Frame(Frame parent, String transformerName, String fromUrl, String sourceMimetype, String targetMimetype,
                long sourceSize, TransformationOptions options, Call pushCall, boolean origDebugOutput)
        {
            this.id = parent == null ? -1 : ++parent.childId;
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
                id = uniqueId.getAndIncrement();
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
        private final long maxSourceSizeKBytes;
        private final transient boolean debug;
        
        UnavailableTransformer(String name, long maxSourceSizeKBytes, boolean debug)
        {
            this.name = name;
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
    
    /**
     * Constructor
     */
    public TransformerDebug(NodeService nodeService, MimetypeService mimetypeService)
    {
        this.nodeService = nodeService;
        this.mimetypeService = mimetypeService;
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
            logBasicDetails(frame, sourceSize, transformerName, (ourStack.size() == 1));
        }
    }
    
    /**
     * Called to identify a transformer that cannot be used during working out
     * available transformers.
     */
    public void unavailableTransformer(ContentTransformer transformer, long maxSourceSizeKBytes)
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
                frame.unavailableTransformers.add(new UnavailableTransformer(name, maxSourceSizeKBytes, debug));
            }
        }
    }

    /**
     * Called once all available transformers have been identified.
     */
    public void availableTransformers(List<ContentTransformer> transformers, long sourceSize, String calledFrom)
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
            logBasicDetails(frame, sourceSize,
                    calledFrom + ((transformers.size() == 0) ? " NO transformers" : ""),
                    firstLevel);

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
                log((c == 'a' ? "**" : "  ") + (c++) + ") " + name + spaces(padName) + 
                    size + spaces(padSize) + ms(trans.getTransformationTime()));
            }
            if (frame.unavailableTransformers != null)
            {
                for (UnavailableTransformer unavailable: frame.unavailableTransformers)
                {
                    int pad = longestNameLength - unavailable.name.length();
                    String reason = "> "+fileSize(unavailable.maxSourceSizeKBytes*1024);
                    log("--" + (c++) + ") " + unavailable.name + spaces(pad+1) + reason, unavailable.debug);
                }
            }
        }
    }

    public void inactiveTransformer(ContentTransformer transformer)
    {
        log(getName(transformer)+' '+ms(transformer.getTransformationTime())+" INACTIVE");
    }

    public void activeTransformer(int mimetypePairCount, ContentTransformer transformer, String sourceMimetype,
            String targetMimetype, long maxSourceSizeKBytes, Boolean explicit, boolean firstMimetypePair)
    {
        if (firstMimetypePair)
        {
            log(getName(transformer)+' '+ms(transformer.getTransformationTime()));
        }
        String i = Integer.toString(mimetypePairCount);
        log(spaces(5-i.length())+mimetypePairCount+") "+getMimetypeExt(sourceMimetype)+getMimetypeExt(targetMimetype)+
                ' '+fileSize((maxSourceSizeKBytes > 0) ? maxSourceSizeKBytes*1024 : maxSourceSizeKBytes)+
                (maxSourceSizeKBytes == 0 || (explicit != null && !explicit) ? " disabled" : "")+ 
                (explicit == null ? "" : explicit ? " EXPLICIT" : " not explicit"));
    }
    
    public void activeTransformer(String sourceMimetype, String targetMimetype, 
            int transformerCount, ContentTransformer transformer, long maxSourceSizeKBytes,
            Boolean explicit, boolean firstTransformer)
    {
        String mimetypes = firstTransformer
                ? getMimetypeExt(sourceMimetype)+getMimetypeExt(targetMimetype)
                : spaces(10);
        char c = (char)('a'+transformerCount);
        log(mimetypes+
                "  "+c+") "+getName(transformer)+' '+ms(transformer.getTransformationTime())+
                ' '+fileSize((maxSourceSizeKBytes > 0) ? maxSourceSizeKBytes*1024 : maxSourceSizeKBytes)+
                (maxSourceSizeKBytes == 0 || (explicit != null && !explicit) ? " disabled" : "")+ 
                (explicit == null ? "" : explicit ? " EXPLICIT" : " not explicit"));
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
    
    private void logBasicDetails(Frame frame, long sourceSize, String message, boolean firstLevel)
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
                ((sourceSize >= 0) ? fileSize(sourceSize)+' ' : "") + message);
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
                    log("Finished in " + ms +
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
                if (frame.unavailableTransformers != null)
                {
                    level = "WARN";
                    long smallestMaxSourceSizeKBytes = Long.MAX_VALUE;
                    for (UnavailableTransformer unavailable: frame.unavailableTransformers)
                    {
                        if (smallestMaxSourceSizeKBytes > unavailable.maxSourceSizeKBytes)
                        {
                            smallestMaxSourceSizeKBytes = unavailable.maxSourceSizeKBytes;
                        }
                    }
                    failureReason = "No transformers as file is > "+fileSize(smallestMaxSourceSizeKBytes*1024);
                }
            }
            else if (frame.callType == Call.TRANSFORM)
            {
                level = failureReason == null || failureReason.length() == 0 ? "INFO" : "ERROR";
                
                // Use TRACE logging for all but the first TRANSFORM
                debug = size == 1 || (size == 2 && ThreadInfo.getStack().peekLast().callType != Call.TRANSFORM);
            }
            
            if (level != null)
            {
                infoLog(getReference(debug), sourceExt, targetExt, level, fileName, sourceSize, transformerName, failureReason, ms, debug);
            }
        }
    }
    
    private void infoLog(String reference, String sourceExt, String targetExt, String level, String fileName,
            long sourceSize, String transformerName, String failureReason, String ms, boolean debug)
    {
        String message =
                "  "+reference +
                sourceExt +
                targetExt +
                (level == null ? "" : level+' ') +
                (fileName == null ? "" : fileName) +
                (sourceSize >= 0 ? ' '+fileSize(sourceSize) : "") +
                (transformerName == null ? "" : ' '+transformerName) +
                (failureReason == null ? "" : ' '+failureReason) +
                ' '+ms;
        info.log(message,  debug);
    }

    /**
     * Indicates if any logging is required.
     */
    public boolean isEnabled()
    {
        // Don't check ThreadInfo.getDebugOutput() as availableTransformers() may upgrade from trace to debug.
        return logger.isDebugEnabled() || info.isDebugEnabled();
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
            log(message + ' ' + t.getMessage());
            
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
            logger.debug(getReference(false)+message, t);
        }
        else if (logger.isTraceEnabled())
        {
            logger.trace(getReference(false)+message, t);
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
     * Returns a N.N.N style reference to the transformation.
     * @param firstLevelOnly indicates if only the top level should be included.
     * @return a padded (fixed length) reference.
     */
    private String getReference(boolean firstLevelOnly)
    {
        StringBuilder sb = new StringBuilder("");
        Frame frame = null;
        Iterator<Frame> iterator = ThreadInfo.getStack().descendingIterator();
        int lengthOfFirstId = 0;
        while (iterator.hasNext())
        {
            frame = iterator.next();
            if (sb.length() == 0)
            {
                sb.append(frame.getId());
                lengthOfFirstId = sb.length();
                if (firstLevelOnly)
                {
                    break;
                }
            }
            else
            {
                sb.append('.');
                sb.append(frame.getId());
            }
        }
        if (frame != null)
        {
            sb.append(spaces(11-sb.length()+lengthOfFirstId)); // Try to pad to level 7
        }
        return sb.toString();
    }

    public String getName(ContentTransformer transformer)
    {
        return
            (transformer instanceof AbstractContentTransformer2
             ? ((AbstractContentTransformerLimits)transformer).getBeanName()
             : transformer.getClass().getSimpleName())+
            
            (transformer instanceof ComplexContentTransformer
             ? "<<Complex>>"
             : transformer instanceof FailoverContentTransformer
             ? "<<Failover>>"
             : transformer instanceof ProxyContentTransformer
             ? (((ProxyContentTransformer)transformer).getWorker() instanceof RuntimeExecutableContentTransformerWorker)
               ? "<<Runtime>>"
               : "<<Proxy>>"
             : "");
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
}

class TransformerLog
{
    private static final Log logger = LogFactory.getLog(TransformerLog.class);

    void log(String message, boolean debug)
    {
        if (debug)
        {
            logger.debug(message);
        }
        else
        {
            logger.trace(message);
        }
    }

    public boolean isDebugEnabled()
    {
        return logger.isDebugEnabled();
    }

    public boolean isTraceEnabled()
    {
        return logger.isTraceEnabled();
    }
}
