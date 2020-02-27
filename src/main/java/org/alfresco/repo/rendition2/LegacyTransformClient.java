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
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Requests rendition transforms take place using legacy transforms available on the local machine (based on
 * {@link org.alfresco.repo.content.transform.AbstractContentTransformer2}. The transform and consumption of the
 * resulting content is linked into a single operation that will take place at some point in the future on the local
 * machine.
 *
 * @author adavis
 */
@Deprecated
public class LegacyTransformClient implements TransformClient, InitializingBean
{
    private static Log logger = LogFactory.getLog(LegacyTransformClient.class);

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
        PropertyCheck.mandatory(this, "transactionService", transactionService);
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
            String message = "Unsupported rendition " + renditionName + " from " + sourceMimetype + " size: " + size + " using legacy transform";
            logger.debug(message);
            throw new UnsupportedOperationException(message);
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("Rendition of " + renditionName + " from " + sourceMimetype + " will use legacy transform " + transformer.getName());
        }
    }

    @Override
    public void transform(NodeRef sourceNodeRef, RenditionDefinition2 renditionDefinition, String user, int sourceContentHashCode)
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

                        ContentReader reader = LegacyTransformClient.this.contentService.getReader(sourceNodeRef, ContentModel.PROP_CONTENT);
                        if (null == reader || !reader.exists())
                        {
                            throw new IllegalArgumentException("The supplied sourceNodeRef "+sourceNodeRef+" has no content.");
                        }

                        ContentWriter writer = contentService.getTempWriter();
                        writer.setMimetype(targetMimetype);
                        contentService.transform(reader, writer, transformationOptions);

                        InputStream inputStream = writer.getReader().getContentInputStream();
                        renditionService2.consume(sourceNodeRef, inputStream, renditionDefinition, sourceContentHashCode);
                    }
                    catch (Exception e)
                    {
                        if (logger.isDebugEnabled())
                        {
                            String renditionName = renditionDefinition.getRenditionName();
                            logger.debug("Rendition of "+renditionName+" failed", e);
                        }
                        renditionService2.failure(sourceNodeRef, renditionDefinition, sourceContentHashCode);
                        throw e;
                    }
                    return null;
                }), user);
        });
    }
}
