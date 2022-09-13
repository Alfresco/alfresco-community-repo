/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

import org.alfresco.transform.client.model.config.CoreFunction;
import org.alfresco.transform.client.registry.TransformServiceRegistry;

import java.util.Map;

/**
 * A transform service registry that falls back between different implementations if not supported.
 *
 * @author adavis
 */
public class SwitchingTransformServiceRegistry implements TransformServiceRegistry
{
    private final TransformServiceRegistry primary;
    private final TransformServiceRegistry secondary;

    public SwitchingTransformServiceRegistry(TransformServiceRegistry primary, TransformServiceRegistry secondary)
    {
        this.primary = primary;
        this.secondary = secondary;
    }

    @Override
    public long findMaxSize(String sourceMimetype, String targetMimetype, Map<String, String> options, String renditionName)
    {
        long maxSize;
        long primaryMaxSize = primary.findMaxSize(sourceMimetype, targetMimetype, options, renditionName);
        if (primaryMaxSize == -1L)
        {
            maxSize = -1L;
        }
        else
        {
            long secondaryMaxSize = secondary.findMaxSize(sourceMimetype, targetMimetype, options, renditionName);
            maxSize = primaryMaxSize == 0
                ? secondaryMaxSize
                : secondaryMaxSize == 0
                    ? primaryMaxSize
                    : secondaryMaxSize == -1L
                        ? Long.valueOf(-1L)
                        : Long.valueOf(Math.max(primaryMaxSize, secondaryMaxSize));
        }
        return maxSize;
    }

    @Override
    public String findTransformerName(String sourceMimetype, long sourceSizeInBytes, String targetMimetype, Map<String, String> actualOptions, String renditionName)
    {
        String name = primary.findTransformerName(sourceMimetype, sourceSizeInBytes, targetMimetype, actualOptions, renditionName);
        if (name == null)
        {
            name = secondary.findTransformerName(sourceMimetype, sourceSizeInBytes, targetMimetype, actualOptions, renditionName);
        }
        return name;
    }

    @Override
    public boolean isSupported(CoreFunction function, String transformerName)
    {
        return primary.isSupported(function, transformerName) && secondary.isSupported(function, transformerName);
    }
}
