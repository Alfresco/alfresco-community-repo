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

import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;

/**
 * Implements {@link TransformServiceRegistry} providing a mechanism of validating if a local transformation request is
 * supported.
 *
 * @author adavis
 */
public class LocalTransformServiceRegistry extends AbstractTransformServiceRegistry implements InitializingBean
{
    private static ContentService contentService;

    public static void setContentService(ContentService contentService)
    {
        LocalTransformServiceRegistry.contentService = contentService;
    }

    @Override
    public void afterPropertiesSet()
    {
        PropertyCheck.mandatory(this, "contentService", contentService);
    }

    @Override
    public Long getMaxSize(String sourceMimetype, String targetMimetype, String renditionName, Map<String, String> options)
    {
        TransformationOptions transformationOptions = LocalTransformClient.getTransformationOptions(renditionName, options);
        long maxSize = contentService.getMaxSourceSizeBytes(sourceMimetype, targetMimetype, transformationOptions);
        return maxSize == 0 ? null : maxSize;
    }
}