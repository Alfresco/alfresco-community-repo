/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2019 Alfresco Software Limited
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
import org.alfresco.repo.content.transform.LocalTransform;
import org.alfresco.repo.content.transform.LocalTransformServiceRegistry;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
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
 * Requests rendition transforms take place using transforms available on the local machine (based on
 * {@link LocalTransform}. The transform and consumption of the
 * resulting content is linked into a single operation that will take place at some point in the future on the local
 * machine.
 *
 * @author adavis
 */
public class LocalTransformClient implements TransformClient, InitializingBean
{
    private static final String TRANSFORM = "Local transform ";
    private static Log logger = LogFactory.getLog(LocalTransformClient.class);

    private LocalTransformServiceRegistry localTransformServiceRegistry;
    private TransactionService transactionService;
    private ContentService contentService;
    private RenditionService2Impl renditionService2;

    private ExecutorService executorService;
    private ThreadLocal<LocalTransform> transform = new ThreadLocal<>();

    public void setLocalTransformServiceRegistry(LocalTransformServiceRegistry localTransformServiceRegistry)
    {
        this.localTransformServiceRegistry = localTransformServiceRegistry;
    }

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

    public void setExecutorService(ExecutorService executorService)
    {
        this.executorService = executorService;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "localTransformServiceRegistry", localTransformServiceRegistry);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "contentService", contentService);
        PropertyCheck.mandatory(this, "renditionService2", renditionService2);
        if (executorService == null)
        {
            executorService = Executors.newCachedThreadPool();
        }
    }

    @Override
    public void checkSupported(NodeRef sourceNodeRef, RenditionDefinition2 renditionDefinition,
                               String sourceMimetype, long sourceSizeInBytes, String contentUrl)
    {
        String targetMimetype = renditionDefinition.getTargetMimetype();
        String renditionName = renditionDefinition.getRenditionName();

        Map<String, String> actualOptions = renditionDefinition.getTransformOptions();
        LocalTransform localTransform = localTransformServiceRegistry.getLocalTransform(sourceMimetype,
                sourceSizeInBytes, targetMimetype, actualOptions, renditionName);
        transform.set(localTransform);

        String message = TRANSFORM + renditionName + " from " + sourceMimetype +
                (localTransform == null ? " is unsupported" : " is supported");
        logger.debug(message);
        if (localTransform == null)
        {
            throw new UnsupportedOperationException(message);
        }
    }

    @Override
    public void transform(NodeRef sourceNodeRef, RenditionDefinition2 renditionDefinition, String user,
                          int sourceContentHashCode)
    {
        String renditionName = renditionDefinition.getRenditionName();
        String targetMimetype = renditionDefinition.getTargetMimetype();
        Map<String, String> actualOptions = renditionDefinition.getTransformOptions();
        LocalTransform localTransform = transform.get();

        executorService.submit(() ->
        {
            AuthenticationUtil.runAs((AuthenticationUtil.RunAsWork<Void>) () ->
                transactionService.getRetryingTransactionHelper().doInTransaction(() ->
                {
                    try
                    {
                        if (localTransform == null)
                        {
                            throw new IllegalStateException("isSupported was not called prior to an asynchronous transform.");
                        }

                        ContentReader reader = contentService.getReader(sourceNodeRef, ContentModel.PROP_CONTENT);
                        if (null == reader || !reader.exists())
                        {
                            throw new IllegalArgumentException("sourceNodeRef "+sourceNodeRef+" has no content.");
                        }

                        if (logger.isDebugEnabled())
                        {
                            logger.debug(TRANSFORM + "requested " + renditionName);
                        }
                        ContentWriter writer = contentService.getTempWriter();
                        writer.setMimetype(targetMimetype);
                        localTransform.transform(reader, writer, actualOptions, renditionName, sourceNodeRef);

                        InputStream inputStream = writer.getReader().getContentInputStream();
                        if (logger.isDebugEnabled())
                        {
                            logger.debug(TRANSFORM + "to be consumed " + renditionName);
                        }
                        renditionService2.consume(sourceNodeRef, inputStream, renditionDefinition, sourceContentHashCode);
                        if (logger.isDebugEnabled())
                        {
                            logger.debug(TRANSFORM + "consumed " + renditionName);
                        }
                    }
                    catch (Exception e)
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug(TRANSFORM + "failed " + renditionName, e);
                        }
                        if (renditionDefinition instanceof TransformDefinition)
                        {
                            ((TransformDefinition) renditionDefinition).setErrorMessage(e.getMessage());
                        }
                        renditionService2.failure(sourceNodeRef, renditionDefinition, sourceContentHashCode);
                        throw e;
                    }
                    return null;
                }), user);
        });
    }
}
