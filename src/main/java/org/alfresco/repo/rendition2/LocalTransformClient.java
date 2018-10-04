/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.repo.rendition2;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.content.transform.magick.ImageResizeOptions;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.repo.content.transform.swf.SWFTransformationOptions;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CropSourceOptions;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.PagedSourceOptions;
import org.alfresco.service.cmr.repository.TemporalSourceOptions;
import org.alfresco.service.cmr.repository.TransformationOptionLimits;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.repository.TransformationSourceOptions;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.alfresco.repo.rendition2.RenditionDefinition2.ALLOW_ENLARGEMENT;
import static org.alfresco.repo.rendition2.RenditionDefinition2.AUTO_ORIENT;
import static org.alfresco.repo.rendition2.RenditionDefinition2.CROP_GRAVITY;
import static org.alfresco.repo.rendition2.RenditionDefinition2.CROP_HEIGHT;
import static org.alfresco.repo.rendition2.RenditionDefinition2.CROP_PERCENTAGE;
import static org.alfresco.repo.rendition2.RenditionDefinition2.CROP_WIDTH;
import static org.alfresco.repo.rendition2.RenditionDefinition2.CROP_X_OFFSET;
import static org.alfresco.repo.rendition2.RenditionDefinition2.CROP_Y_OFFSET;
import static org.alfresco.repo.rendition2.RenditionDefinition2.DURATION;
import static org.alfresco.repo.rendition2.RenditionDefinition2.END_PAGE;
import static org.alfresco.repo.rendition2.RenditionDefinition2.FLASH_VERSION;
import static org.alfresco.repo.rendition2.RenditionDefinition2.HEIGHT;
import static org.alfresco.repo.rendition2.RenditionDefinition2.INCLUDE_CONTENTS;
import static org.alfresco.repo.rendition2.RenditionDefinition2.MAINTAIN_ASPECT_RATIO;
import static org.alfresco.repo.rendition2.RenditionDefinition2.OFFSET;
import static org.alfresco.repo.rendition2.RenditionDefinition2.PAGE;
import static org.alfresco.repo.rendition2.RenditionDefinition2.RESIZE_HEIGHT;
import static org.alfresco.repo.rendition2.RenditionDefinition2.RESIZE_PERCENTAGE;
import static org.alfresco.repo.rendition2.RenditionDefinition2.RESIZE_WIDTH;
import static org.alfresco.repo.rendition2.RenditionDefinition2.START_PAGE;
import static org.alfresco.repo.rendition2.RenditionDefinition2.THUMBNAIL;
import static org.alfresco.repo.rendition2.RenditionDefinition2.WIDTH;
import static org.alfresco.service.cmr.repository.TransformationOptionLimits.OPT_MAX_PAGES;
import static org.alfresco.service.cmr.repository.TransformationOptionLimits.OPT_MAX_SOURCE_SIZE_K_BYTES;
import static org.alfresco.service.cmr.repository.TransformationOptionLimits.OPT_PAGE_LIMIT;
import static org.alfresco.service.cmr.repository.TransformationOptionLimits.OPT_READ_LIMIT_K_BYTES;
import static org.alfresco.service.cmr.repository.TransformationOptionLimits.OPT_READ_LIMIT_TIME_MS;
import static org.alfresco.service.cmr.repository.TransformationOptionLimits.OPT_TIMEOUT_MS;
import static org.springframework.util.CollectionUtils.containsAny;

/**
 * Requests rendition transforms take place using transforms available on the local machine. The transform and
 * consumption of the resulting content is linked into a single operation that will take place at some point in
 * the future on the local machine.
 *
 * @author adavis
 */
public class LocalTransformClient extends AbstractTransformClient implements TransformClient
{
    private static Log logger = LogFactory.getLog(LocalTransformClient.class);

    private TransactionService transactionService;

    private ContentService contentService;

    private RenditionService2Impl renditionService2;

    private TransformationOptionsConverter converter;

    private ExecutorService executorService;

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public void setRenditionService2(RenditionService2Impl renditionService2)
    {
        this.renditionService2 = renditionService2;
    }

    public void setConverter(TransformationOptionsConverter converter)
    {
        this.converter = converter;
    }

    public void setExecutorService(ExecutorService executorService)
    {
        this.executorService = executorService;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        super.afterPropertiesSet();
        PropertyCheck.mandatory(this, "contentService", contentService);
        PropertyCheck.mandatory(this, "renditionService2", renditionService2);
        PropertyCheck.mandatory(this, "converter", converter);
        if (executorService == null)
        {
            executorService = Executors.newCachedThreadPool();
        }
    }

    @Override
    public void checkSupported(NodeRef sourceNodeRef, RenditionDefinition2 renditionDefinition, String sourceMimetype, long size, String contentUrl)
    {
        String targetMimetype = renditionDefinition.getTargetMimetype();
        String renditionName = renditionDefinition.getRenditionName();
        Map<String, String> options = renditionDefinition.getTransformOptions();

        TransformationOptions transformationOptions = converter.getTransformationOptions(renditionName, options);
        transformationOptions.setSourceNodeRef(sourceNodeRef);

        ContentTransformer transformer = contentService.getTransformer(contentUrl, sourceMimetype, size, targetMimetype, transformationOptions);
        if (transformer == null)
        {
            String message = "Unsupported rendition " + renditionName + " from " + sourceMimetype + " size: " + size;
            logger.debug(message);
            throw new UnsupportedOperationException(message);
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("Rendition of " + renditionName + " from " + sourceMimetype + " will use " + transformer.getName());
        }
    }

    @Override
    public void transform(NodeRef sourceNodeRef, RenditionDefinition2 renditionDefinition, String user, int sourceContentUrlHashCode)
    {
        executorService.submit(() ->
        {
            AuthenticationUtil.runAs((AuthenticationUtil.RunAsWork<Void>) () ->
                transactionService.getRetryingTransactionHelper().doInTransaction(() ->
                {
                    try
                    {
                        String targetMimetype = renditionDefinition.getTargetMimetype();
                        String renditionName = renditionDefinition.getRenditionName();
                        Map<String, String> options = renditionDefinition.getTransformOptions();

                        TransformationOptions transformationOptions = converter.getTransformationOptions(renditionName, options);
                        transformationOptions.setSourceNodeRef(sourceNodeRef);

                        ContentReader reader = LocalTransformClient.this.contentService.getReader(sourceNodeRef, ContentModel.PROP_CONTENT);
                        if (null == reader || !reader.exists())
                        {
                            throw new IllegalArgumentException("The supplied sourceNodeRef "+sourceNodeRef+" has no content.");
                        }

                        ContentWriter writer = contentService.getTempWriter();
                        writer.setMimetype(targetMimetype);
                        contentService.transform(reader, writer, transformationOptions);

                        InputStream inputStream = writer.getReader().getContentInputStream();
                        renditionService2.consume(sourceNodeRef, inputStream, renditionDefinition, sourceContentUrlHashCode);
                    }
                    catch (Exception e)
                    {
                        if (logger.isDebugEnabled())
                        {
                            String renditionName = renditionDefinition.getRenditionName();
                            logger.debug("Rendition of "+renditionName+" failed", e);
                        }
                        renditionService2.failure(sourceNodeRef, renditionDefinition, sourceContentUrlHashCode);
                        throw e;
                    }
                    return null;
                }), user);
        });
    }

    /**
     * @deprecated as we do not plan to use TransformationOptions moving forwards as local transformations will also
     * use the same options as the Transform Service.
     */
    @Deprecated
    static TransformationOptions getTransformationOptions(String renditionName, Map<String, String> options)
    {
        return null; // TODO cahnge caller so they call the new class.
    }
}
