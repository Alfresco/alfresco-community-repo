/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2022 Alfresco Software Limited
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

import org.alfresco.repo.content.transform.LocalTransform;
import org.alfresco.repo.content.transform.LocalTransformServiceRegistry;
import org.alfresco.repo.content.transform.UnsupportedTransformationException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.DirectAccessUrl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.transform.config.CoreFunction;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.HashMap;
import java.util.Map;

import static org.alfresco.model.ContentModel.PROP_CONTENT;
import static org.alfresco.transform.common.RequestParamMap.DIRECT_ACCESS_URL;

/**
 * Request synchronous transforms.
 *
 * Transforms take place using transforms available on the local machine (based on {@link LocalTransform}).
 *
 * @author adavis
 */
public class LocalSynchronousTransformClient implements SynchronousTransformClient, InitializingBean
{
    private static final String TRANSFORM = "Local synchronous transform ";
    private static Log logger = LogFactory.getLog(LocalTransformClient.class);

    private LocalTransformServiceRegistry localTransformServiceRegistry;
    private ContentService contentService;
    private boolean directAccessUrlEnabled;

    public void setLocalTransformServiceRegistry(LocalTransformServiceRegistry localTransformServiceRegistry)
    {
        this.localTransformServiceRegistry = localTransformServiceRegistry;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public void setDirectAccessUrlEnabled(boolean directAccessUrlEnabled)
    {
        this.directAccessUrlEnabled = directAccessUrlEnabled;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "localTransformServiceRegistry", localTransformServiceRegistry);
        PropertyCheck.mandatory(this, "contentService", contentService);
        PropertyCheck.mandatory(this, "transformDirectAccessUrlEnabled", directAccessUrlEnabled);
    }

    @Override
    public boolean isSupported(String sourceMimetype, long sourceSizeInBytes, String contentUrl, String targetMimetype,
                               Map<String, String> actualOptions, String transformName, NodeRef sourceNodeRef)
    {
        String renditionName = TransformDefinition.convertToRenditionName(transformName);
        LocalTransform transform = localTransformServiceRegistry.getLocalTransform(sourceMimetype,
                sourceSizeInBytes, targetMimetype, actualOptions, renditionName);

        if (logger.isDebugEnabled())
        {
            logger.debug(TRANSFORM + renditionName + " from " + sourceMimetype +
                    (transform == null ? " is unsupported" : " is supported"));
        }
        return transform != null;
    }

    @Override
    public void transform(ContentReader reader, ContentWriter writer, Map<String, String> actualOptions,
                          String transformName, NodeRef sourceNodeRef)
    {
        String renditionName = TransformDefinition.convertToRenditionName(transformName);
        try
        {
            if (reader == null)
            {
                throw new IllegalArgumentException("The content reader must be set");
            }
            if (!reader.exists())
            {
                throw new IllegalArgumentException("sourceNodeRef "+sourceNodeRef+" has no content.");
            }

            String sourceMimetype = reader.getMimetype();
            long sourceSizeInBytes = reader.getSize();
            if (sourceMimetype == null)
            {
                throw new IllegalArgumentException("The content reader mimetype must be set");
            }

            String targetMimetype = writer.getMimetype();
            if (targetMimetype == null)
            {
                throw new IllegalArgumentException("The content writer mimetype must be set");
            }

            LocalTransform transform = localTransformServiceRegistry.getLocalTransform(sourceMimetype,
                    sourceSizeInBytes, targetMimetype, actualOptions, renditionName);

            if (transform == null)
            {
                throw new UnsupportedTransformationException("Transformation of " + sourceMimetype +
                        (sourceSizeInBytes > 0 ? " size "+sourceSizeInBytes : "")+ " to " + targetMimetype +
                        " unsupported");
            }

            if (logger.isDebugEnabled())
            {
                logger.debug(TRANSFORM + "requested " + renditionName);
            }

            actualOptions = addDirectAccessUrlToOptionsIfPossible(actualOptions, sourceNodeRef, transform);
            transform.transform(reader, writer, actualOptions, renditionName, sourceNodeRef);

            if (logger.isDebugEnabled())
            {
                logger.debug(TRANSFORM + "created " + renditionName);
            }
        }
        catch (Exception e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(TRANSFORM + "failed " + renditionName, e);
            }
            throw e;
        }
    }

    private Map<String, String> addDirectAccessUrlToOptionsIfPossible(Map<String, String> actualOptions,
                                                                      NodeRef sourceNodeRef, LocalTransform transform)
    {
        if (directAccessUrlEnabled &&
            localTransformServiceRegistry.isSupported(CoreFunction.DIRECT_ACCESS_URL, transform) &&
            contentService.isContentDirectUrlEnabled(sourceNodeRef, PROP_CONTENT))
        {
            DirectAccessUrl directAccessUrl = contentService.requestContentDirectUrl(sourceNodeRef, PROP_CONTENT, true);
            actualOptions = new HashMap<>(actualOptions);
            actualOptions.put(DIRECT_ACCESS_URL, directAccessUrl.getContentUrl());
        }
        return actualOptions;
    }

    @Override
    public String getName()
    {
        return "Local";
    }
}
