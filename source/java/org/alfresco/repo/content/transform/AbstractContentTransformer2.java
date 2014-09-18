/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.AbstractStreamAwareProxy;
import org.alfresco.repo.content.StreamAwareContentReaderProxy;
import org.alfresco.repo.content.StreamAwareContentWriterProxy;
import org.alfresco.repo.content.metadata.AbstractMappingMetadataExtracter;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentServiceTransientException;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptionLimits;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides basic services for {@link org.alfresco.repo.content.transform.ContentTransformer}
 * implementations.
 * <p>
 * This class maintains the performance measures for the transformers as well, making sure that
 * there is an extra penalty for transformers that fail regularly.
 * 
 * @author Derek Hulley
 * @author Roy Wetherall
 */
public abstract class AbstractContentTransformer2 extends AbstractContentTransformerLimits
{
    private static final Log logger = LogFactory.getLog(AbstractContentTransformer2.class);

    private ExecutorService executorService;

    private ContentTransformerRegistry registry;
    private boolean registerTransformer;
    private boolean retryTransformOnDifferentMimeType;

    /**
     * A flag that indicates that the transformer should be started in it own Thread so
     * that it may be interrupted rather than using the timeout in the Reader.
     * Need only be set for transformers that read their source data quickly but then
     * take a long time to process the data (such as {@link PoiOOXMLContentTransformer}.
     */
    private Boolean useTimeoutThread = false;

    /**
     * Extra time added the timeout when using a Thread for the transformation so that
     * a timeout from the Reader has a chance to happen first.
     */
    private long additionalThreadTimout = 2000;

    private static ThreadLocal<Integer> depth = new ThreadLocal<Integer>()
    {
        @Override
        protected Integer initialValue()
        {
            return 0;
        }
    };

    /**
     * All transformers start with an average transformation time of 0.0 ms,
     * unless there is an Alfresco global property {@code <beanName>.time}.
     * May also be set for given combinations of source and target mimetypes.
     */
    protected AbstractContentTransformer2()
    {
    }

    /**
     * The registry to auto-register with
     * 
     * @param registry the transformer registry
     */
    public void setRegistry(ContentTransformerRegistry registry)
    {
        this.registry = registry;
    }    

    /**
     * @param registerTransformer as been available for selection.
     *        If {@code false} this indicates that the transformer may only be
     *        used as part of another transformer.
     */
    public void setRegisterTransformer(boolean registerTransformer)
    {
        this.registerTransformer = registerTransformer;
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName();
    }
    
    /**
     * Registers this instance with the {@link #setRegistry(ContentTransformerRegistry) registry}
     * if it is present.
     * 
     * THIS IS A CUSTOM SPRING INIT METHOD
     */
    public void register()
    {
        super.register();
        if (registry == null)
        {
            logger.warn("Property 'registry' has not been set.  Ignoring auto-registration: \n" +
                    "   transformer: " + this.getName());
        }
        else if (registerTransformer)
        {
            registry.addTransformer(this);
        }
        else
        {
            registry.addComponentTransformer(this);
            logger.debug("Property 'registerTransformer' have not been set, so transformer (" +
                    this.getName() + ") may only be used as a component of a complex transformer.");
        }
    }
    
    /**
     * Convenience method to check the transformability of a transformation
     * 
     * @param reader    content reader
     * @param writer    content writer
     * @param options   transformation options
     * @throws AlfrescoRuntimeException if the the transformation isn't supported
     */
    protected void checkTransformable(ContentReader reader, ContentWriter writer, TransformationOptions options)
    {
        String sourceMimetype = getMimetype(reader);
        String targetMimetype = getMimetype(writer);
        long sourceSize = reader.getSize();
        boolean transformable = isTransformable(sourceMimetype, sourceSize, targetMimetype, options);
        if (transformable == false)
        {
            // This method is only called once a transformer has been selected, so it should be able to
            // handle the mimetypes but might not be able to handle all the limits as it might be part of
            // of a complex (compound) transformer. So report the max size if set.
            long maxSourceSizeKBytes = getMaxSourceSizeKBytes(sourceMimetype, targetMimetype, options);
            boolean sizeOkay = maxSourceSizeKBytes < 0 || (maxSourceSizeKBytes > 0 && sourceSize <= maxSourceSizeKBytes*1024);
            AlfrescoRuntimeException e = new UnsupportedTransformationException("Unsupported transformation: " +
                    getBeanName()+' '+sourceMimetype+" to "+targetMimetype+' '+
                    (sizeOkay
                    ? ""
                    : transformerDebug.fileSize(sourceSize)+" > "+ transformerDebug.fileSize(maxSourceSizeKBytes*1024)));
            throw transformerDebug.setCause(e);
        }
        // it all checks out OK
    }

    /**
     * Method to be implemented by subclasses wishing to make use of the common infrastructural code
     * provided by this class.
     * 
     * @param reader the source of the content to transform
     * @param writer the target to which to write the transformed content
     * @param options a map of options to use when performing the transformation.  The map
     *      will never be null.
     * @throws Exception exceptions will be handled by this class - subclasses can throw anything
     */
    protected abstract void transformInternal(
            ContentReader reader,
            ContentWriter writer,
            TransformationOptions options) throws Exception;
    
    /**
     * @see #transform(ContentReader, ContentWriter, Map)
     * @see #transformInternal(ContentReader, ContentWriter, Map)
     */
    public final void transform(ContentReader reader, ContentWriter writer) throws ContentIOException
    {
        transform(reader, writer, new TransformationOptions());
    }
    
    /**
     * @see org.alfresco.repo.content.transform.ContentTransformer#transform(org.alfresco.service.cmr.repository.ContentReader, org.alfresco.service.cmr.repository.ContentWriter, org.alfresco.service.cmr.repository.TransformationOptions)
     */
    public final void transform(ContentReader reader, ContentWriter writer, TransformationOptions options)
        throws ContentIOException
    {
        try
        {
            depth.set(depth.get()+1);
            
            // begin timing
            long before = System.currentTimeMillis();
            
            String sourceMimetype = reader.getMimetype();
            String targetMimetype = writer.getMimetype();

            // check options map
            if (options == null)
            {
                options = new TransformationOptions();
            }
            
            try
            {
                if (transformerDebug.isEnabled())
                {
                    transformerDebug.pushTransform(this, reader.getContentUrl(), sourceMimetype,
                            targetMimetype, reader.getSize(), options);
                }
                
                // Check the transformability
                checkTransformable(reader, writer, options);
                
                // Pass on any limits to the reader
                setReaderLimits(reader, writer, options);

                // Transform
                // MNT-12238: CLONE - CLONE - Upload of PPTX causes very high memory usage leading to system instability
                // Limiting transformation up to configured amount of milliseconds to avoid very high RAM consumption
                // and OOM during transforming problematic documents
                TransformationOptionLimits limits = getLimits(reader.getMimetype(), writer.getMimetype(), options);

                long timeoutMs = limits.getTimeoutMs();
                if (!useTimeoutThread || (null == limits) || (-1 == timeoutMs))
                {
                    transformInternal(reader, writer, options);
                }
                else
                {
                    Future<?> submittedTask = null;
                    StreamAwareContentReaderProxy proxiedReader = new StreamAwareContentReaderProxy(reader);
                    StreamAwareContentWriterProxy proxiedWriter = new StreamAwareContentWriterProxy(writer);

                    try
                    {
                        submittedTask = getExecutorService().submit(new TransformInternalCallable(proxiedReader, proxiedWriter, options));
                        submittedTask.get(timeoutMs + additionalThreadTimout, TimeUnit.MILLISECONDS);
                    }
                    catch (TimeoutException e)
                    {
                        releaseResources(submittedTask, proxiedReader, proxiedWriter);
                        throw new TimeoutException("Transformation failed due to timeout limit");
                    }
                    catch (InterruptedException e)
                    {
                        releaseResources(submittedTask, proxiedReader, proxiedWriter);
                        throw new InterruptedException("Transformation failed, because the thread of the transformation was interrupted");
                    }
                    catch (ExecutionException e)
                    {
                        Throwable cause = e.getCause();
                        if (cause instanceof TransformInternalCallableException)
                        {
                            cause = ((TransformInternalCallableException) cause).getCause();
                        }

                        throw cause;
                    }
                }

                // record time
                long after = System.currentTimeMillis();
                recordTime(sourceMimetype, targetMimetype, after - before);
            }
            catch (ContentServiceTransientException cste)
            {
                // A transient failure has occurred within the content transformer.
                // This should not be interpreted as a failure and therefore we should not
                // update the transformer's average time.
                if (logger.isDebugEnabled())
                {
                    logger.debug("Transformation has been transiently declined: \n" +
                            "   reader: " + reader + "\n" +
                            "   writer: " + writer + "\n" +
                            "   options: " + options + "\n" +
                            "   transformer: " + this);
                }
                // the finally block below will still perform tidyup. Otherwise we're done.
                // We rethrow the exception
                throw cste;
            }
            catch (UnsupportedTransformationException e)
            {
                // Don't record an error or even the time, as this is normal in compound transformations.
                transformerDebug.debug("          Failed", e);
                throw e;
            }
            catch (Throwable e)
            {
                // Make sure that this transformation gets set back i.t.o. time taken.
                // This will ensure that transformers that compete for the same transformation
                // will be prejudiced against transformers that tend to fail
                long after = System.currentTimeMillis();
                recordError(sourceMimetype, targetMimetype, after - before);
                
                // Ask Tika to detect the document, and report back on if
                //  the current mime type is plausible
                String differentType = getMimetypeService().getMimetypeIfNotMatches(reader.getReader());
        
                // Report the error
                if(differentType == null)
                {
                transformerDebug.debug("          Failed", e);
                    throw new ContentIOException("Content conversion failed: \n" +
                           "   reader: " + reader + "\n" +
                           "   writer: " + writer + "\n" +
                           "   options: " + options.toString(false) + "\n" +
                           "   limits: " + getLimits(reader, writer, options),
                           e);
                }
                else
                {
                    transformerDebug.debug("          Failed: Mime type was '"+differentType+"'", e);

                    if (this.retryTransformOnDifferentMimeType)
                    {
                        // MNT-11015 fix.
                        // Set a new reader to refresh the input stream.
                        reader = reader.getReader();
                        // set the actual file MIME type detected by Tika for content reader
                        reader.setMimetype(differentType);

                        // Get correct transformer according actual file MIME type and try to transform file with
                        // actual transformer
                        ContentTransformer transformer = this.registry.getTransformer(differentType, reader.getSize(),
                                                                                     targetMimetype, options);
                        if (null != transformer)
                        {
                            transformer.transform(reader, writer, options);
                        }
                        else
                        {
                            transformerDebug.debug("          Failed", e);
                            throw new ContentIOException("Content conversion failed: \n" +
                                    "   reader: " + reader + "\n" +
                                    "   writer: " + writer + "\n" +
                                    "   options: " + options.toString(false) + "\n" +
                                    "   limits: " + getLimits(reader, writer, options) + "\n" +
                                    "   claimed mime type: " + reader.getMimetype() + "\n" +
                                    "   detected mime type: " + differentType + "\n" +
                                    "   transformer not found" + "\n",
                                    e
                            );
                        }
                    }
                    else
                    {
                        throw new ContentIOException("Content conversion failed: \n" +
                                "   reader: " + reader + "\n" +
                                "   writer: " + writer + "\n" +
                                "   options: " + options.toString(false) + "\n" +
                                "   limits: " + getLimits(reader, writer, options) + "\n" +
                                "   claimed mime type: " + reader.getMimetype() + "\n" +
                                "   detected mime type: " + differentType,
                                e
                        );
                    }
                }
            }
            finally
            {
                transformerDebug.popTransform();
                
                // check that the reader and writer are both closed
                if (reader.isChannelOpen())
                {
                    logger.error("Content reader not closed by transformer: \n" +
                            "   reader: " + reader + "\n" +
                            "   transformer: " + this);
                }
                if (writer.isChannelOpen())
                {
                    logger.error("Content writer not closed by transformer: \n" +
                            "   writer: " + writer + "\n" +
                            "   transformer: " + this);
                }
            }
            
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Completed transformation: \n" +
                        "   reader: " + reader + "\n" +
                        "   writer: " + writer + "\n" +
                        "   options: " + options + "\n" +
                        "   transformer: " + this);
            }
        }
        finally
        {
            depth.set(depth.get()-1);
        }
    }

    /**
     * Cancels <code>task</code> and closes content accessors
     * 
     * @param task - {@link Future} task instance which specifies a transformation action
     * @param proxiedReader - {@link AbstractStreamAwareProxy} instance which represents channel closing mechanism for content reader
     * @param proxiedWriter - {@link AbstractStreamAwareProxy} instance which represents channel closing mechanism for content writer
     */
    private void releaseResources(Future<?> task, AbstractStreamAwareProxy proxiedReader, AbstractStreamAwareProxy proxiedWriter)
    {
        if (null != task)
        {
            task.cancel(true);
        }

        if (null != proxiedReader)
        {
            proxiedReader.release();
        }

        if (null != proxiedWriter)
        {
            proxiedWriter.release();
        }
    }

    public final void transform(
            ContentReader reader,
            ContentWriter writer,
            Map<String, Object> options) throws ContentIOException
    {
        this.transform(reader, writer, new TransformationOptions(options));
    }
    
    /**
     * @return Returns the calculated running average of the current transformations
     */
    public synchronized long getTransformationTime()
    {
        return transformerConfig.getStatistics(this, null, null, true).getAverageTime();
    }

    /**
     * @return Returns the calculated running average of the current transformations
     */
    public synchronized long getTransformationTime(String sourceMimetype, String targetMimetype)
    {
        return transformerConfig.getStatistics(this, sourceMimetype, targetMimetype, true).getAverageTime();
    }

    /**
     * @deprecated use method with mimetypes.
     */
    protected final synchronized void recordTime(long transformationTime)
    {
        recordTime(TransformerConfig.ANY, TransformerConfig.ANY, transformationTime);
    }

    /**
     * Records and updates the average transformation time for this transformer.
     * <p>
     * Subclasses should call this after every transformation in order to keep
     * the running average of the transformation times up to date.
     * <p>
     * This method is thread-safe.  The time spent in this method is negligible
     * so the impact will be minor.
     * 
     * @param sourceMimetype
     * @param targetMimetype
     * @param transformationTime the time it took to perform the transformation.
     */
    protected final synchronized void recordTime(String sourceMimetype, String targetMimetype,
            long transformationTime)
    {
        transformerConfig.getStatistics(this, sourceMimetype, targetMimetype, true).recordTime(transformationTime);
        if (depth.get() == 1)
        {
            transformerConfig.getStatistics(null, sourceMimetype, targetMimetype, true).recordTime(transformationTime);
        }
    }

    /**
     * Gets the <code>ExecutorService</code> to be used for timeout-aware extraction.
     * <p>
     * If no <code>ExecutorService</code> has been defined a default of <code>Executors.newCachedThreadPool()</code> is used during {@link AbstractMappingMetadataExtracter#init()}.
     * 
     * @return the defined or default <code>ExecutorService</code>
     */
    protected ExecutorService getExecutorService()
    {
        if (null == executorService)
        {
            executorService = Executors.newCachedThreadPool();
        }

        return executorService;
    }

    /**
     * Sets the <code>ExecutorService</code> to be used for timeout-aware transformation.
     * 
     * @param executorService - {@link ExecutorService} instance for timeouts
     */
    public void setExecutorService(ExecutorService executorService)
    {
        this.executorService = executorService;
    }

    /**
     * {@link Callable} wrapper for the {@link AbstractContentTransformer2#transformInternal(ContentReader, ContentWriter, TransformationOptions)} method to handle timeouts.
     */
    private class TransformInternalCallable implements Callable<Void>
    {
        private ContentReader reader;

        private ContentWriter writer;

        private TransformationOptions options;

        public TransformInternalCallable(ContentReader reader, ContentWriter writer, TransformationOptions options)
        {
            this.reader = reader;
            this.writer = writer;
            this.options = options;
        }

        @Override
        public Void call() throws Exception
        {
            try
            {
                transformInternal(reader, writer, options);
                return null;
            }
            catch (Throwable e)
            {
                throw new TransformInternalCallableException(e);
            }
        }
    }

    /**
     * Exception wrapper to handle any {@link Throwable} from {@link AbstractContentTransformer2#transformInternal(ContentReader, ContentWriter, TransformationOptions)}
     */
    private class TransformInternalCallableException extends Exception
    {
        private static final long serialVersionUID = 7740560508772740658L;

        public TransformInternalCallableException(Throwable cause)
        {
            super(cause);
        }
    }

    /**
     * @param useTimeoutThread - {@link Boolean} value which specifies timeout limiting mechanism for the current transformer
     * @see AbstractContentTransformer2#useTimeoutThread
     */
    public void setUseTimeoutThread(Boolean useTimeoutThread)
    {
        if (null == useTimeoutThread)
        {
            useTimeoutThread = true;
        }

        this.useTimeoutThread = useTimeoutThread;
    }

    public void setAdditionalThreadTimout(long additionalThreadTimout)
    {
        this.additionalThreadTimout = additionalThreadTimout;
    }

    public Boolean isTransformationLimitedInternally()
    {
        return useTimeoutThread;
    }

    /**
     * Records an error and updates the average time as if the transformation took a
     * long time, so that it is less likely to be called again.
     * @param sourceMimetype
     * @param targetMimetype
     * @param transformationTime the time it took to perform the transformation.
     */
    protected final synchronized void recordError(String sourceMimetype, String targetMimetype,
            long transformationTime)
    {
        transformerConfig.getStatistics(this, sourceMimetype, targetMimetype, true).recordError(transformationTime);
        if (depth.get() == 1)
        {
            transformerConfig.getStatistics(null, sourceMimetype, targetMimetype, true).recordError(transformationTime);
        }
    }

    public void setRetryTransformOnDifferentMimeType(boolean retryTransformOnDifferentMimeType)
    {
        this.retryTransformOnDifferentMimeType = retryTransformOnDifferentMimeType;
    }
}
