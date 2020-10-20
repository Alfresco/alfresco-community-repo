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
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.LogTee;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;

import static org.alfresco.repo.rendition2.RenditionDefinition2.SOURCE_ENCODING;
import static org.alfresco.repo.rendition2.RenditionDefinition2.SOURCE_NODE_REF;
import static org.alfresco.repo.rendition2.RenditionDefinition2.TARGET_ENCODING;
import static org.alfresco.repo.rendition2.RenditionDefinition2.TIMEOUT;

/**
 * Debugs transformers selection and activity.<p>
 *
 * As transformations are frequently composed of lower level transformations, log
 * messages include a prefix to identify the transformation. A numeric dot notation
 * is used (such as {@code 123.1.2} indicating the second third level transformation
 * of the 123rd top level transformation).
 * @author Alan Davis
 */
public class TransformerDebug
{
    protected static final String FINISHED_IN = "Finished in ";
    protected static final String NO_TRANSFORMERS = "No transformers";
    protected static final String TRANSFORM_SERVICE_NAME = "TransformService";

    private Log info;
    protected Log logger;
    protected NodeService nodeService;
    protected MimetypeService mimetypeService;

    protected enum Call
    {
        AVAILABLE,
        TRANSFORM,
        AVAILABLE_AND_TRANSFORM
    };

    protected static class ThreadInfo
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

    protected static class Frame
    {
        private static final AtomicInteger uniqueId = new AtomicInteger(0);

        private int id;
        private final String fromUrl;
        protected final String sourceMimetype;
        protected final String targetMimetype;
        protected final NodeRef sourceNodeRef;
        protected final String renditionName;
        private final boolean origDebugOutput;
        private long start;

        private Call callType;
        private Frame parent;
        private int childId;
        protected Set<UnavailableTransformer> unavailableTransformers;
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

        protected void setFailureReason(String failureReason)
        {
            this.failureReason = failureReason;
        }

        protected String getFailureReason()
        {
            return failureReason;
        }

        protected void setSourceSize(long sourceSize)
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

        public String getRenditionName()
        {
            return renditionName;
        }
    }

    @Deprecated
    protected class UnavailableTransformer implements Comparable<UnavailableTransformer>
    {
        protected final String name;
        protected final String priority;
        protected final long maxSourceSizeKBytes;
        protected final transient boolean debug;
        
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

    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "mimetypeService", mimetypeService);
        PropertyCheck.mandatory(this, "transformerLog", info);
        PropertyCheck.mandatory(this, "transformerDebugLog", logger);
    }

    public void pushTransform(String transformerName, String fromUrl, String sourceMimetype,
                              String targetMimetype, long sourceSize, Map<String, String> options,
                              String renditionName, NodeRef sourceNodeRef)
    {
        if (isEnabled())
        {
            push(transformerName, fromUrl, sourceMimetype, targetMimetype, sourceSize,
                    options, renditionName, sourceNodeRef, Call.TRANSFORM);
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
            push(null, null, null, null, -1, null,
                    null, null, Call.AVAILABLE);
        }
    }

    void push(String transformerName, String fromUrl, String sourceMimetype, String targetMimetype,
              long sourceSize, Map<String, String> options,
              String renditionName, NodeRef sourceNodeRef, Call callType)
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
            logBasicDetails(frame, sourceSize, options, renditionName, transformerName, (ourStack.size() == 1));
        }
    }

    protected void logBasicDetails(Frame frame, long sourceSize, Map<String, String> options, String renditionName,
                                   String message, boolean firstLevel)
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
                (firstLevel ? getRenditionName(renditionName) : "") + message);
        if (firstLevel)
        {
            log(options);
            String nodeRef = getNodeRef(frame.sourceNodeRef, firstLevel, sourceSize);
            if (!nodeRef.isEmpty())
            {
                log(nodeRef);
            }
        }
    }

    public void debug(String sourceMimetype, String targetMimetype, NodeRef sourceNodeRef, long sourceSize,
                      Map<String, String> options, String renditionName, String message)
    {
        String fileName = getFileName(sourceNodeRef, true, -1);
        log("              "+getMimetypeExt(sourceMimetype)+getMimetypeExt(targetMimetype) +
                ((fileName != null) ? fileName+' ' : "")+
                ((sourceSize >= 0) ? fileSize(sourceSize)+' ' : "") +
                (getRenditionName(renditionName)) + message);
        log(options);
    }

    private void log(Map<String, String> options)
    {
        if (options != null)
        {
            for (Map.Entry<String, String> option : options.entrySet())
            {
                String key = option.getKey();
                if (!TIMEOUT.equals(key))
                {
                    String value = option.getValue();
                    value = value != null
                            ? "=\"" + value.replaceAll("\"", "\\\"") + "\""
                            : "=null"+
                              (SOURCE_NODE_REF.equals(key) ||
                               SOURCE_ENCODING.equals(key) ||
                               TARGET_ENCODING.equals(key)
                               ? " - set automatically" : "");
                    log("  " + key + value);
                }
            }
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

    protected int pop(Call callType, boolean suppressFinish, boolean suppressChecking)
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
            String renditionName = frame.getRenditionName();
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

            if (level != null)
            {
                infoLog(getReference(debug, false), sourceExt, targetExt, level, fileName, sourceSize,
                        transformerName, renditionName, failureReason, ms, debug);
            }
        }
    }
    
    private void infoLog(String reference, String sourceExt, String targetExt, String level, String fileName,
            long sourceSize, String transformerName, String renditionName, String failureReason, String ms, boolean debug)
    {
        String message =
                reference +
                sourceExt +
                targetExt +
                (level == null ? "" : level+' ') +
                (fileName == null ? "" : fileName) +
                (sourceSize >= 0 ? ' '+fileSize(sourceSize) : "") +
                (ms == null || ms.isEmpty() ? "" : ' '+ms)+
                (transformerName == null ? "" : ' '+transformerName) +
                (renditionName == null ? "" : ' '+getRenditionName(renditionName)) +
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

    protected void log(String message)
    {
        log(message, true);
    }
    
    protected void log(String message, boolean debug)
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
                // ignore (normally InvalidNodeRefException) but we should ignore other RuntimeExceptions too
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

    protected String getMimetypeExt(String mimetype)
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
    
    protected String spaces(int i)
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
     * Debugs a request to the Transform Service
     */
    public int debugTransformServiceRequest(String sourceMimetype, long sourceSize, NodeRef sourceNodeRef,
                                            int contentHashcode, String fileName, String targetMimetype,
                                            Map<String, String> options, String renditionName)
    {
        if (isEnabled())
        {
            pushMisc();
            String sourceExt = getMimetypeExt(sourceMimetype);
            String targetExt = getMimetypeExt(targetMimetype);
            debug(sourceExt + targetExt +
                    ((fileName != null) ? fileName + ' ' : "") +
                    ((sourceSize >= 0) ? fileSize(sourceSize) + ' ' : "") +
                    getRenditionName(renditionName) + " "+ TRANSFORM_SERVICE_NAME);
            log(options);
            log(sourceNodeRef.toString() + ' ' + contentHashcode);
            String reference = getReference(true, false);
            infoLog(reference, sourceExt, targetExt, null, fileName, sourceSize, TRANSFORM_SERVICE_NAME,
                    renditionName, null, "", true);
        }
        return pop(Call.AVAILABLE, true, false);
    }

    private String getRenditionName(String renditionName)
    {
        return renditionName != null ? "-- "+renditionName+" -- " : "";
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
        }
        debug(msg);
        debug(sourceNodeRef.toString() + ' ' +contentHashcode);
        pop(Call.AVAILABLE, suppressFinish, true);
    }

    /**
     * Obtains a String for log messages.
     * @param options to be turned into a string.
     * @return a string of options that may be included in debug messages.
     */
    public static String toString(Map<String, String> options)
    {
        StringJoiner sj = new StringJoiner(", ");
        options.entrySet().forEach(option->sj.add(option.getKey()+"=\""+option.getValue().replaceAll("\"", "\\\"")+"\""));
        return sj.toString();
    }
}
