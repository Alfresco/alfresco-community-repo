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

package org.alfresco.repo.rendition.executer;

import static org.alfresco.repo.action.executer.TransformActionExecuter.TRANSFORMER_NOT_EXISTS_MESSAGE_PATTERN;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.content.transform.TransformerDebug;
import org.alfresco.repo.content.transform.UnsupportedTransformationException;
import org.alfresco.repo.rendition2.RenditionService2Impl;
import org.alfresco.repo.rendition2.SynchronousTransformClient;
import org.alfresco.repo.rendition2.TransformationOptionsConverter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.ActionServiceException;
import org.alfresco.service.cmr.action.ActionTrackingService;
import org.alfresco.service.cmr.action.ExecutionDetails;
import org.alfresco.service.cmr.action.ExecutionSummary;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.rendition.RenditionCancelledException;
import org.alfresco.service.cmr.rendition.RenditionServiceException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NoTransformerException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TransformationOptionLimits;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.repository.TransformationSourceOptions;
import org.alfresco.service.cmr.repository.TransformationSourceOptions.TransformationSourceOptionsSerializer;

/**
 * @author Nick Smith
 *
 * @deprecated The RenditionService is being replace by the simpler async RenditionService2.
 */
@Deprecated
public abstract class AbstractTransformationRenderingEngine extends AbstractRenderingEngine
{
    private static Log logger = LogFactory.getLog(AbstractTransformationRenderingEngine.class);

    /**
     * This optional {@link Long} parameter specifies the timeout for reading the source before an exception is thrown.
     */
    public static final String PARAM_TIMEOUT_MS = TransformationOptionLimits.OPT_TIMEOUT_MS;

    /**
     * This optional {@link Long} parameter specifies how timeout for reading the source before EOF is returned.
     */
    public static final String PARAM_READ_LIMIT_TIME_MS = TransformationOptionLimits.OPT_READ_LIMIT_TIME_MS;

    /**
     * This optional {@link Long} parameter specifies the maximum number of kbytes of the source may be read. An exception is thrown before any are read if larger.
     */
    public static final String PARAM_MAX_SOURCE_SIZE_K_BYTES = TransformationOptionLimits.OPT_MAX_SOURCE_SIZE_K_BYTES;

    /**
     * This optional {@link Long} parameter specifies how many kbytes of the source to read in order to create an image.
     */
    public static final String PARAM_READ_LIMIT_K_BYTES = TransformationOptionLimits.OPT_READ_LIMIT_K_BYTES;

    /**
     * This optional {@link Integer} parameter specifies the maximum number of pages of the source that may be read. An exception is thrown before any are read if larger.
     */
    public static final String PARAM_MAX_PAGES = TransformationOptionLimits.OPT_MAX_PAGES;

    /**
     * This optional {@link Integer} parameter specifies how many source pages should be read in order to create an image.
     */
    public static final String PARAM_PAGE_LIMIT = TransformationOptionLimits.OPT_PAGE_LIMIT;

    /**
     * The frequency in milliseconds with which the {@link ActionTrackingService} should be polled for cancellation of the action.
     */
    protected static final int CANCELLED_ACTION_POLLING_INTERVAL = 200;

    /**
     * This optional {@link String} parameter specifies the type (or use) of the rendition.
     */
    public static final String PARAM_USE = ".use.".replaceAll("\\.", "");

    private static final String NOT_TRANSFORMABLE_MESSAGE_PATTERN = TRANSFORMER_NOT_EXISTS_MESSAGE_PATTERN;
    private static final String TRANSFORMING_ERROR_MESSAGE = RenditionService2Impl.TRANSFORMING_ERROR_MESSAGE;

    private Collection<TransformationSourceOptionsSerializer> sourceOptionsSerializers;

    public Collection<TransformationSourceOptionsSerializer> getSourceOptionsSerializers()
    {
        return sourceOptionsSerializers;
    }

    public void setSourceOptionsSerializers(Collection<TransformationSourceOptionsSerializer> sourceOptionsSerializers)
    {
        this.sourceOptionsSerializers = sourceOptionsSerializers;
    }

    /**
     * The <code>ExecutorService</code> to be used for cancel-aware transforms.
     */
    private ExecutorService executorService;

    private SynchronousTransformClient synchronousTransformClient;
    private TransformationOptionsConverter converter;

    /**
     * Gets the <code>ExecutorService</code> to be used for cancel-aware transforms.
     * <p>
     * If no <code>ExecutorService</code> has been defined a default of <code>Executors.newCachedThreadPool()</code> is used during {@link AbstractTransformationRenderingEngine#init()}.
     * 
     * @return the defined or default <code>ExecutorService</code>
     */
    protected ExecutorService getExecutorService()
    {
        return executorService;
    }

    public void setSynchronousTransformClient(SynchronousTransformClient synchronousTransformClient)
    {
        this.synchronousTransformClient = synchronousTransformClient;
    }

    public void setConverter(TransformationOptionsConverter converter)
    {
        this.converter = converter;
    }

    public void init()
    {
        super.init();
        if (executorService == null)
        {
            executorService = Executors.newCachedThreadPool();
        }
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.rendition.executer.AbstractRenderingEngine#render(org.alfresco.repo.rendition.executer.AbstractRenderingEngine.RenderingContext) */
    @Override
    protected void render(RenderingContext context)
    {
        ContentReader contentReader = context.makeContentReader();
        // There will have been an exception if there is no content data so contentReader is not null.
        String contentUrl = contentReader.getContentUrl();
        String sourceMimeType = contentReader.getMimetype();
        String targetMimeType = getTargetMimeType(context);

        // The child NodeRef gets created here
        TransformationOptions transformationOptions = getTransformOptions(context);

        long sourceSizeInBytes = contentReader.getSize();
        Map<String, String> options = converter.getOptions(transformationOptions, sourceMimeType, targetMimeType);
        NodeRef sourceNodeRef = transformationOptions.getSourceNodeRef();
        if (!synchronousTransformClient.isSupported(sourceMimeType, sourceSizeInBytes, contentUrl, targetMimeType,
                options, null, sourceNodeRef))
        {
            String optionsString = TransformerDebug.toString(options);
            throw new RenditionServiceException(String.format(NOT_TRANSFORMABLE_MESSAGE_PATTERN, sourceMimeType,
                    targetMimeType, optionsString));
        }

        long startTime = new Date().getTime();
        boolean actionCancelled = false;
        boolean actionCompleted = false;

        // Cache the execution summary to get details later
        ExecutionSummary executionSummary = null;
        try
        {
            executionSummary = getExecutionSummary(context);
        }
        catch (ActionServiceException e)
        {
            if (logger.isInfoEnabled())
            {
                logger.info("Cancelling of multiple concurrent action instances " +
                        "currently unsupported, this action can't be cancelled");
            }
        }

        // Call the transform in a different thread so we can move on if cancelled
        FutureTask<ContentWriter> transformTask = new FutureTask<ContentWriter>(
                new TransformationCallable(contentReader, targetMimeType, transformationOptions, context,
                        AuthenticationUtil.getFullyAuthenticatedUser()));
        getExecutorService().execute(transformTask);

        // Start checking for cancellation or timeout
        while (true)
        {
            try
            {
                Thread.sleep(CANCELLED_ACTION_POLLING_INTERVAL);
                if (transformTask.isDone())
                {
                    actionCompleted = true;
                    break;
                }
                // Check timeout in case transformer doesn't obey it
                if (transformationOptions.getTimeoutMs() > 0 &&
                        new Date().getTime() - startTime > (transformationOptions.getTimeoutMs() + CANCELLED_ACTION_POLLING_INTERVAL))
                {
                    // We hit a timeout, let the transform thread continue but results will be ignored
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Transformation did not obey timeout limit, " +
                                "rendition action is moving on");
                    }
                    break;
                }
                if (executionSummary != null)
                {
                    ExecutionDetails executionDetails = actionTrackingService.getExecutionDetails(executionSummary);
                    if (executionDetails != null)
                    {
                        actionCancelled = executionDetails.isCancelRequested();
                        if (actionCancelled)
                        {
                            if (logger.isDebugEnabled())
                            {
                                logger.debug("Cancelling transformation");
                            }
                            transformTask.cancel(true);
                            break;
                        }
                    }
                }
            }
            catch (InterruptedException e)
            {
                // entire thread was asked to stop
                actionCancelled = true;
                transformTask.cancel(true);
                break;
            }
        }

        if (actionCancelled)
        {
            throw new RenditionCancelledException("Rendition action cancelled");
        }

        if (!actionCompleted && !actionCancelled)
        {
            throw new RenditionServiceException("Transformation failed to obey timeout limit");
        }

        if (actionCompleted)
        {
            // Copy content from temp writer to real writer
            ContentWriter writer = context.makeContentWriter();
            try
            {
                // We should not need another timeout here, things should be ready for us
                ContentWriter tempTarget = transformTask.get();
                if (tempTarget == null)
                {
                    // We should never be in this state, but just in case
                    throw new RenditionServiceException("Target of transformation not present");
                }
                writer.putContent(tempTarget.getReader().getContentInputStream());
            }
            catch (ExecutionException e)
            {
                // Unwrap our cause and throw that
                Throwable transformException = e.getCause();
                if (transformException instanceof RuntimeException)
                {
                    throw (RuntimeException) e.getCause();
                }
                throw new RenditionServiceException(TRANSFORMING_ERROR_MESSAGE + e.getCause().getMessage(), e.getCause());
            }
            catch (InterruptedException e)
            {
                // We were asked to stop
                transformTask.cancel(true);
            }
        }
    }

    protected abstract TransformationOptions getTransformOptions(RenderingContext context);

    protected TransformationOptions getTransformOptionsImpl(TransformationOptions options, RenderingContext context)
    {
        Long timeoutMs = context.getCheckedParam(PARAM_TIMEOUT_MS, Long.class);
        if (timeoutMs != null)
        {
            options.setTimeoutMs(timeoutMs);
        }

        Long readLimitTimeMs = context.getCheckedParam(PARAM_READ_LIMIT_TIME_MS, Long.class);
        if (readLimitTimeMs != null)
        {
            options.setReadLimitTimeMs(readLimitTimeMs);
        }

        Long maxSourceSizeKBytes = context.getCheckedParam(PARAM_MAX_SOURCE_SIZE_K_BYTES, Long.class);
        if (maxSourceSizeKBytes != null)
        {
            options.setMaxSourceSizeKBytes(maxSourceSizeKBytes);
        }

        Long readLimitKBytes = context.getCheckedParam(PARAM_READ_LIMIT_K_BYTES, Long.class);
        if (readLimitKBytes != null)
        {
            options.setReadLimitKBytes(readLimitKBytes);
        }

        Integer maxPages = context.getCheckedParam(PARAM_MAX_PAGES, Integer.class);
        if (maxPages != null)
        {
            options.setMaxPages(maxPages);
        }

        Integer pageLimit = context.getCheckedParam(PARAM_PAGE_LIMIT, Integer.class);
        if (pageLimit != null)
        {
            options.setPageLimit(pageLimit);
        }

        String use = context.getCheckedParam(PARAM_USE, String.class);
        if (use != null)
        {
            options.setUse(use);
        }

        if (getSourceOptionsSerializers() != null)
        {
            for (TransformationSourceOptionsSerializer sourceSerializer : getSourceOptionsSerializers())
            {
                TransformationSourceOptions sourceOptions = sourceSerializer.deserialize(context);
                if (sourceOptions != null)
                {
                    options.addSourceOptions(sourceOptions);
                }
            }
        }

        return options;
    }

    /* @seeorg.alfresco.repo.rendition.executer.AbstractRenderingEngine#getParameterDefinitions() */
    protected Collection<ParameterDefinition> getParameterDefinitions()
    {
        Collection<ParameterDefinition> paramList = super.getParameterDefinitions();

        paramList.add(new ParameterDefinitionImpl(PARAM_TIMEOUT_MS, DataTypeDefinition.LONG, false,
                getParamDisplayLabel(PARAM_TIMEOUT_MS)));
        paramList.add(new ParameterDefinitionImpl(PARAM_READ_LIMIT_TIME_MS, DataTypeDefinition.LONG, false,
                getParamDisplayLabel(PARAM_READ_LIMIT_TIME_MS)));
        paramList.add(new ParameterDefinitionImpl(PARAM_MAX_SOURCE_SIZE_K_BYTES, DataTypeDefinition.LONG, false,
                getParamDisplayLabel(PARAM_MAX_SOURCE_SIZE_K_BYTES)));
        paramList.add(new ParameterDefinitionImpl(PARAM_READ_LIMIT_K_BYTES, DataTypeDefinition.LONG, false,
                getParamDisplayLabel(PARAM_READ_LIMIT_K_BYTES)));
        paramList.add(new ParameterDefinitionImpl(PARAM_MAX_PAGES, DataTypeDefinition.INT, false,
                getParamDisplayLabel(PARAM_MAX_PAGES)));
        paramList.add(new ParameterDefinitionImpl(PARAM_PAGE_LIMIT, DataTypeDefinition.INT, false,
                getParamDisplayLabel(PARAM_PAGE_LIMIT)));
        paramList.add(new ParameterDefinitionImpl(PARAM_USE, DataTypeDefinition.TEXT, false,
                getParamDisplayLabel(PARAM_USE)));

        return paramList;
    }

    /**
     * Implementation of <code>Callable</code> for doing the work of the transformation which returns the temporary content writer if successful.
     */
    protected class TransformationCallable implements Callable<ContentWriter>
    {
        private ContentReader contentReader;
        private String targetMimeType;
        private Map<String, String> options;
        private RenderingContext context;
        private String initiatingUsername;

        public TransformationCallable(ContentReader contentReader, String targetMimeType,
                TransformationOptions transformationOptions, RenderingContext context, String initiatingUsername)
        {
            this.contentReader = contentReader;
            this.targetMimeType = targetMimeType;
            String sourceMimetype = contentReader.getMimetype();
            this.options = converter.getOptions(transformationOptions, sourceMimetype, targetMimeType);
            this.context = context;
            this.initiatingUsername = initiatingUsername;
        }

        @Override
        public ContentWriter call() throws Exception
        {
            AuthenticationUtil.setFullyAuthenticatedUser(initiatingUsername);
            Serializable runAsParam = context.getDefinition().getParameterValue(AbstractRenderingEngine.PARAM_RUN_AS);
            String runAsName = runAsParam == null ? AbstractRenderingEngine.DEFAULT_RUN_AS_NAME : (String) runAsParam;

            return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<ContentWriter>() {
                @Override
                public ContentWriter doWork() throws Exception
                {
                    // ALF-15715: Use temporary write to avoid operating on the real node for fear of row locking while long transforms are in progress
                    ContentWriter tempContentWriter = contentService.getTempWriter();
                    NodeRef sourceNode = context.getSourceNode();
                    tempContentWriter.setMimetype(targetMimeType);
                    try
                    {
                        synchronousTransformClient.transform(contentReader, tempContentWriter, options,
                                null, sourceNode);
                        return tempContentWriter;
                    }
                    catch (NoTransformerException | UnsupportedTransformationException ntx)
                    {
                        {
                            logger.debug("No transformer found to execute rule: \n" + "   reader: " + contentReader + "\n"
                                    + "   writer: " + tempContentWriter + "\n" + "   action: " + this);
                        }
                        throw new RenditionServiceException(TRANSFORMING_ERROR_MESSAGE + ntx.getMessage(), ntx);
                    }
                };
            }, runAsName);
        }

    }
}
