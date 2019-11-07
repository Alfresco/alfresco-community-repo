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

import org.alfresco.repo.content.transform.LocalTransform;
import org.alfresco.repo.content.transform.LocalTransformServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;

/**
 * Request synchronous transforms. Used in refactoring deprecated code, which called Legacy transforms, so that it will
 * first try a Local transform, falling back to Legacy if not available. Transforms take place using transforms
 * available on the local machine (based on {@link LocalTransform}).
 *
 * @author adavis
 */
@Deprecated
public class LocalSynchronousTransformClient implements SynchronousTransformClient, InitializingBean
{
    private static final String TRANSFORM = "Local synchronous transform ";
    private static Log logger = LogFactory.getLog(LocalTransformClient.class);

    private LocalTransformServiceRegistry localTransformServiceRegistry;
    private ThreadLocal<LocalTransform> transform = new ThreadLocal<>();

    public void setLocalTransformServiceRegistry(LocalTransformServiceRegistry localTransformServiceRegistry)
    {
        this.localTransformServiceRegistry = localTransformServiceRegistry;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "localTransformServiceRegistry", localTransformServiceRegistry);
    }

    @Override
    public boolean isSupported(NodeRef sourceNodeRef, String sourceMimetype, long sourceSizeInBytes, String contentUrl,
                        String targetMimetype,  Map<String, String> actualOptions, String transformName)
    {
        String renditionName = TransformDefinition.convertToRenditionName(transformName);
        LocalTransform localTransform = localTransformServiceRegistry.getLocalTransform(sourceMimetype,
                sourceSizeInBytes, targetMimetype, actualOptions, renditionName);
        transform.set(localTransform);

        if (logger.isDebugEnabled())
        {
            logger.debug(TRANSFORM + renditionName + " from " + sourceMimetype +
                    (localTransform == null ? " is unsupported" : " is supported"));
        }
        return localTransform != null;
    }

    @Override
    public void transform(ContentReader reader, ContentWriter writer, Map<String, String> actualOptions,
                          String transformName, NodeRef sourceNodeRef) throws Exception
    {
        String renditionName = TransformDefinition.convertToRenditionName(transformName);
        LocalTransform localTransform = transform.get();
        try
        {
            if (localTransform == null)
            {
                throw new IllegalStateException("isSupported was not called prior to transform.");
            }

            if (null == reader || !reader.exists())
            {
                throw new IllegalArgumentException("sourceNodeRef "+sourceNodeRef+" has no content.");
            }

            if (logger.isDebugEnabled())
            {
                logger.debug(TRANSFORM + " requested " + renditionName);
            }

            localTransform.transform(reader, writer, actualOptions, renditionName, sourceNodeRef);

            if (logger.isDebugEnabled())
            {
                logger.debug(TRANSFORM + " created " + renditionName);
            }
        }
        catch (Exception e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(TRANSFORM + " failed " + renditionName, e);
            }
            throw e;
        }
    }
}
