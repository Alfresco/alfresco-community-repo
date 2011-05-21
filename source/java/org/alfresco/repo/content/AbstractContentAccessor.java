/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.content;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.Locale;

import org.alfresco.error.StackTraceUtil;
import org.alfresco.service.cmr.repository.ContentAccessor;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Provides basic support for content accessors.
 * 
 * @author Derek Hulley
 */
public abstract class AbstractContentAccessor implements ContentAccessor
{
    private static Log logger = LogFactory.getLog(AbstractContentAccessor.class);
    private static final Log loggerTrace = LogFactory.getLog(AbstractContentAccessor.class.getName() + ".trace");
    static
    {
        if (loggerTrace.isDebugEnabled())
        {
            loggerTrace.warn("Trace channel assignment logging is on and will affect performance");
        }
    }
    
    private StackTraceElement[] traceLoggerChannelAssignTrace;
    
    private String contentUrl;
    private String mimetype;
    private String encoding;
    private Locale locale;

    /**
     * @param contentUrl the content URL
     */
    protected AbstractContentAccessor(String contentUrl)
    {
        ParameterCheck.mandatoryString("contentUrl", contentUrl);
        if (contentUrl == null || contentUrl.length() == 0)
        {
            throw new IllegalArgumentException("contentUrl is invalid:" + contentUrl);
        }
        this.contentUrl = contentUrl;
        
        // the default encoding is Java's default encoding
        encoding = "UTF-8";
        // the default locale
        locale = I18NUtil.getLocale();
    }
    
    @Override
    protected void finalize() throws Throwable
    {
        if (loggerTrace.isDebugEnabled() && traceLoggerChannelAssignTrace != null)
        {
            // check that the channel is closed if it was used
            if (isChannelOpen())
            {
                StringBuilder sb = new StringBuilder(1024);
                StackTraceUtil.buildStackTrace(
                        "Content IO Channel was opened but not closed: \n" + this,
                        traceLoggerChannelAssignTrace,
                        sb,
                        -1);
                loggerTrace.error(sb);
            }
        }
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(100);
        sb.append("ContentAccessor")
          .append("[ contentUrl=").append(getContentUrl())
          .append(", mimetype=").append(getMimetype())
          .append(", size=").append(getSize())
          .append(", encoding=").append(getEncoding())
          .append(", locale=").append(getLocale())
          .append("]");
        return sb.toString();
    }
    
    public ContentData getContentData()
    {
        ContentData property = new ContentData(contentUrl, mimetype, getSize(), encoding, locale);
        return property;
    }

    /**
     * Derived classes can call this method to ensure that necessary trace logging is performed
     * when the IO Channel is opened.
     */
    protected final void channelOpened()
    {
        // trace debug
        if (loggerTrace.isDebugEnabled())
        {
            Exception e = new Exception();
            e.fillInStackTrace();
            traceLoggerChannelAssignTrace = e.getStackTrace();
        }
    }
    
    public String getContentUrl()
    {
        return contentUrl;
    }
    
    /**
     * Allow derived implementations to set the Content URL.  This allows for implementations
     * where the URL is not known when the accessor is first constructed.
     * 
     * @param contentUrl            the new content URL
     */
    protected void setContentUrl(String contentUrl)
    {
        ParameterCheck.mandatoryString("contentUrl", contentUrl);
        this.contentUrl = contentUrl;
    }
    
    public String getMimetype()
    {
        return mimetype;
    }

    /**
     * @param mimetype the underlying content's mimetype - null if unknown
     */
    public void setMimetype(String mimetype)
    {
        this.mimetype = mimetype;
    }

    /**
     * @return Returns the content encoding - null if unknown
     */
    public String getEncoding()
    {
        return encoding;
    }

    /**
     * @param encoding the underlying content's encoding - null if unknown
     */
    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    /**
     * @return  Returns the content locale or <tt>null</tt> if unkown
     */
    public Locale getLocale()
    {
        return locale;
    }

    /**
     * @param locale    the content's locale, if known.
     */
    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }

    /**
     * Generate a callback instance of the {@link FileChannel FileChannel}.
     *  
     * @param directChannel the delegate that to perform the actual operations
     * @param listeners the listeners to call
     * @return Returns a new channel that functions just like the original, except
     *      that it issues callbacks to the listeners
     * @throws ContentIOException
     */
    protected FileChannel getCallbackFileChannel(
            FileChannel directChannel,
            List<ContentStreamListener> listeners)
            throws ContentIOException
    {
        FileChannel ret = new CallbackFileChannel(directChannel, listeners);
        // done
        return ret;
    }

    /**
     * Advise that listens for the completion of specific methods on the
     * {@link java.nio.channels.ByteChannel} interface.  This advise reacts
     * only in the {@link #afterReturning(Object, Method, Object[], Object) afterReturning} phase
     * so that the underlying stream methods have been successfully completed.
     * 
     * @author Derek Hulley
     */
    protected class ChannelCloseCallbackAdvise implements AfterReturningAdvice
    {
        private List<ContentStreamListener> listeners;

        public ChannelCloseCallbackAdvise(List<ContentStreamListener> listeners)
        {
            this.listeners = listeners;
        }
        
        /**
         * Provides transactional callbacks to the listeners 
         */
        public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable
        {
            // check for specific events
            if (method.getName().equals("close"))
            {
                fireChannelClosed();
            }
        }
        
        private void fireChannelClosed()
        {
            if (listeners.size() == 0)
            {
                // nothing to do
                return;
            }

            for (ContentStreamListener listener : listeners)
            {
                listener.contentStreamClosed();
            }
                    
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("" + listeners.size() + " content listeners called: close");
            }
        }
    }
    
    /**
     * Wraps a <code>FileChannel</code> to provide callbacks to listeners when the
     * channel is {@link java.nio.channels.Channel#close() closed}.
     * <p>
     * This class is unfortunately necessary as the {@link FileChannel} doesn't have
     * an single interface defining its methods, making it difficult to put an
     * advice around the methods that require overriding.
     * 
     * @author Derek Hulley
     */
    protected class CallbackFileChannel extends FileChannel
    {
        /** the channel to route all calls to */
        private FileChannel delegate;
        /** listeners waiting for the stream close */
        private List<ContentStreamListener> listeners;

        /**
         * @param delegate the channel that will perform the work
         * @param listeners listeners for events coming from this channel
         */
        public CallbackFileChannel(
                FileChannel delegate,
                List<ContentStreamListener> listeners)
        {
            if (delegate == null)
            {
                throw new IllegalArgumentException("FileChannel delegate is required");
            }
            if (delegate instanceof CallbackFileChannel)
            {
                throw new IllegalArgumentException("FileChannel delegate may not be a CallbackFileChannel");
            }
            
            this.delegate = delegate;
            this.listeners = listeners;
        }
        
        /**
         * Closes the channel and makes the callbacks to the listeners
         */
        @Override
        protected void implCloseChannel() throws IOException
        {
            delegate.close();
            fireChannelClosed();
        }

        /**
         * Helper method to notify stream listeners
         */
        private void fireChannelClosed()
        {
            if (listeners.size() == 0)
            {
                // nothing to do
                return;
            }
          
            for (ContentStreamListener listener : listeners)
            {
                listener.contentStreamClosed();
            }
                   
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("" + listeners.size() + " content listeners called: close");
            }
        }
            
        @Override
        public void force(boolean metaData) throws IOException
        {
            delegate.force(metaData);
        }

        @Override
        public FileLock lock(long position, long size, boolean shared) throws IOException
        {
            return delegate.lock(position, size, shared);
        }

        @Override
        public MappedByteBuffer map(MapMode mode, long position, long size) throws IOException
        {
            return delegate.map(mode, position, size);
        }

        @Override
        public long position() throws IOException
        {
            return delegate.position();
        }

        @Override
        public FileChannel position(long newPosition) throws IOException
        {
            return delegate.position(newPosition);
        }

        @Override
        public int read(ByteBuffer dst) throws IOException
        {
            return delegate.read(dst);
        }

        @Override
        public int read(ByteBuffer dst, long position) throws IOException
        {
            return delegate.read(dst, position);
        }

        @Override
        public long read(ByteBuffer[] dsts, int offset, int length) throws IOException
        {
            return delegate.read(dsts, offset, length);
        }

        @Override
        public long size() throws IOException
        {
            return delegate.size();
        }

        @Override
        public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException
        {
            return delegate.transferFrom(src, position, count);
        }

        @Override
        public long transferTo(long position, long count, WritableByteChannel target) throws IOException
        {
            return delegate.transferTo(position, count, target);
        }

        @Override
        public FileChannel truncate(long size) throws IOException
        {
            return delegate.truncate(size);
        }

        @Override
        public FileLock tryLock(long position, long size, boolean shared) throws IOException
        {
            return delegate.tryLock(position, size, shared);
        }

        @Override
        public int write(ByteBuffer src) throws IOException
        {
            return delegate.write(src);
        }

        @Override
        public int write(ByteBuffer src, long position) throws IOException
        {
            return delegate.write(src, position);
        }

        @Override
        public long write(ByteBuffer[] srcs, int offset, int length) throws IOException
        {
            return delegate.write(srcs, offset, length);
        }
    }
}
