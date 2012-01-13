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

import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.service.cmr.repository.MimetypeService;
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
        private boolean debugOutput = true;
        
        public static Deque<Frame> getStack()
        {
            return threadInfo.get().stack;
        }
        
        public static boolean getDebug()
        {
            return threadInfo.get().debugOutput;
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

        private final int id;
        private final String fromUrl;
        private final String sourceMimetype;
        private final String targetMimetype;
        private final long start;

        private Call callType;
        private int childId;
        private Set<UnavailableTransformer> unavailableTransformers;
// See debug(String, Throwable) as to why this is commented out
//      private Throwable lastThrowable;

        private Frame(Frame parent, String fromUrl, String sourceMimetype, String targetMimetype, Call pushCall)
        {
            this.id = parent == null ? uniqueId.getAndIncrement() : ++parent.childId;
            this.fromUrl = fromUrl;
            this.sourceMimetype = sourceMimetype;
            this.targetMimetype = targetMimetype;
            this.callType = pushCall;
            start = System.currentTimeMillis();
        }
    }
    
    private class UnavailableTransformer
    {
        private final String name;
        private final String reason;
        private final transient boolean debug;
        
        UnavailableTransformer(String name, String reason, boolean debug)
        {
            this.name = name;
            this.reason = reason;
            this.debug = debug;
        }
        
        @Override
        public int hashCode()
        {
            int hashCode = 37 * name.hashCode();
            hashCode += 37 * reason.hashCode();
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
                    EqualsHelper.nullSafeEquals(reason, that.reason);
            }
            else
            {
                return false;
            }
        }
    }
    
    private final MimetypeService mimetypeService;
    
    /**
     * Constructor
     */
    public TransformerDebug(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    /**
     * Called prior to working out what transformers are available.
     */
    public void pushAvailable(String fromUrl, String sourceMimetype, String targetMimetype)
    {
        if (isEnabled())
        {
            push(null, fromUrl, sourceMimetype, targetMimetype, -1, Call.AVAILABLE);
        }
    }
    
    /**
     * Called prior to performing a transform.
     */
    public void pushTransform(ContentTransformer transformer, String fromUrl, String sourceMimetype, String targetMimetype, long sourceSize)
    {
        if (isEnabled())
        {
            push(getName(transformer), fromUrl, sourceMimetype, targetMimetype, sourceSize, Call.TRANSFORM);
        }
    }
    
    private void push(String name, String fromUrl, String sourceMimetype, String targetMimetype, long sourceSize, Call callType)
    {
        Deque<Frame> ourStack = ThreadInfo.getStack();
        Frame frame = ourStack.peek();

        if (callType == Call.TRANSFORM && frame != null && frame.callType == Call.AVAILABLE)
        {
            frame.callType = Call.AVAILABLE_AND_TRANSFORM;
        }
        else
        {
            frame = new Frame(frame, fromUrl, sourceMimetype, targetMimetype, callType);
            ourStack.push(frame);
            
            if (callType == Call.TRANSFORM)
            {
                // Log the basic info about this transformation
                logBasicDetails(frame, sourceSize, name, (ourStack.size() == 1));
            }
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
                String name = getName(transformer);
                String reason = String.format("> %,dK", maxSourceSizeKBytes);
                boolean debug = (maxSourceSizeKBytes != 0);
                if (ourStack.size() == 1)
                {
                    if (frame.unavailableTransformers == null)
                    {
                        frame.unavailableTransformers = new HashSet<UnavailableTransformer>();
                    }
                    frame.unavailableTransformers.add(new UnavailableTransformer(name, reason, debug));
                }
                else
                {
                    log("-- " + name + ' ' + reason, debug);
                }
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
            
            // Log the basic info about this transformation
            logBasicDetails(frame, sourceSize,
                    calledFrom + ((transformers.size() == 0) ? " NO transformers" : ""),
                    (ourStack.size() == 1));

            // Report available and unavailable transformers
            char c = 'a';
            int longestNameLength = getLongestTransformerNameLength(transformers, frame);
            for (ContentTransformer trans : transformers)
            {
                String name = getName(trans);
                int pad = longestNameLength - name.length();
                log((c == 'a' ? "**" : "  ") + (c++) + ") " +
                    name + spaces(pad+1) + trans.getTransformationTime() + " ms");
            }
            if (frame.unavailableTransformers != null)
            {
                for (UnavailableTransformer unavailable: frame.unavailableTransformers)
                {
                    int pad = longestNameLength - unavailable.name.length();
                    log("--" + (c++) + ") " + unavailable.name + spaces(pad+1) + unavailable.reason,
                        unavailable.debug);
                }
            }
        }
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
        if (frame.unavailableTransformers != null)
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
            log(frame.fromUrl, firstLevel);
        }
        
        log(getMimetypeExt(frame.sourceMimetype)+getMimetypeExt(frame.targetMimetype) + String.format("%,dK ", (sourceSize/1024)) + message);

        log(frame.sourceMimetype+' '+frame.targetMimetype, false);
    }

    /**
     * Called after working out what transformers are available and any
     * resulting transform has been called.
     */
    public void popAvailable()
    {
        if (isEnabled())
        {
            pop(Call.AVAILABLE);
        }
    }
    
    /**
     * Called after performing a transform.
     */
    public void popTransform()
    {
        if (isEnabled())
        {
            pop(Call.TRANSFORM);
        }
    }

    private void pop(Call callType)
    {
        Deque<Frame> ourStack = ThreadInfo.getStack();
        if (!ourStack.isEmpty())
        {
            Frame frame = ourStack.peek();
            if ((frame.callType == callType) ||
                (frame.callType == Call.AVAILABLE_AND_TRANSFORM && callType == Call.AVAILABLE))
            {
                if (ourStack.size() == 1 || logger.isTraceEnabled())
                {
                    boolean topFrame = ourStack.size() == 1;
                    log("Finished in " +
                        (System.currentTimeMillis() - frame.start) + " ms" +
                        (frame.callType == Call.AVAILABLE ? " Transformer NOT called" : "") +
                        (topFrame ? "\n" : ""), 
                        topFrame);
                }
                
                ourStack.pop();
                
// See debug(String, Throwable) as to why this is commented out
//                if (ourStack.size() >= 1)
//                {
//                    ourStack.peek().lastThrowable = frame.lastThrowable;
//                }
            }
        }
    }

    /**
     * Indicates if any logging is required.
     */
    public boolean isEnabled()
    {
        return
            (logger.isDebugEnabled() && ThreadInfo.getDebug()) ||
             logger.isTraceEnabled();
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

//            // Generally the full stack is not needed as transformer
//            // Exceptions get logged as a Error higher up, so including
//            // the stack trace has been found not to be needed. Keeping
//            // the following code and code that sets lastThrowable just
//            // in case we need it after all.
//
//            Frame frame = ThreadInfo.getStack().peek();
//            boolean newThrowable = isNewThrowable(frame.lastThrowable, t);
//            frame.lastThrowable = t;
//
//            if (newThrowable)
//            {
//                log(message, t, true);
//            }
//            else
//            {
//                log(message + ' ' + t.getMessage());
//            }
        }
    }

//    private boolean isNewThrowable(Throwable lastThrowable, Throwable t)
//    {
//        while (t != null)
//        {
//            if (lastThrowable == t)
//            {
//                return false;
//            }
//            t = t.getCause();
//        }
//        return true;
//    }

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
        if (debug && ThreadInfo.getDebug())
        {
            logger.debug(getReference()+message, t);
        }
        else
        {
            logger.trace(getReference()+message, t);
        }
    }

    /**
     * Sets the cause of a transformation failure, so that only the
     * message of the Throwable is reported later rather than the full
     * stack trace over and over.
     */
    public <T extends Throwable> T setCause(T t)
    {
// See debug(String, Throwable) as to why this is commented out
//        if (isEnabled())
//        {
//            Deque<Frame> ourStack = ThreadInfo.getStack();
//            if (!ourStack.isEmpty())
//            {
//                ourStack.peek().lastThrowable = t;
//            }
//        }
        return t;
    }
    
    private String getReference()
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
                sb.append(frame.id);
                lengthOfFirstId = sb.length();
            }
            else
            {
                sb.append('.');
                sb.append(frame.id);
            }
        }
        if (frame != null)
        {
            sb.append(spaces(9-sb.length()+lengthOfFirstId)); // Try to pad to level 5
        }
        return sb.toString();
    }

    private String getName(ContentTransformer transformer)
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

    private String getMimetypeExt(String mimetype)
    {
        StringBuilder sb = new StringBuilder("");
        if (mimetypeService == null)
        {
            sb.append(mimetype);
            sb.append(' ');
        }
        else
        {
            String mimetypeExt = mimetypeService.getExtension(mimetype);
            sb.append(mimetypeExt);
            sb.append(spaces(5-mimetypeExt.length()));   // Pad to normal max ext (4) plus 1
        }
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
}
