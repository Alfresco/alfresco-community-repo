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
package org.alfresco.transform.client.model.config;

import org.quartz.CronExpression;

import java.util.Map;

/**
 * Used by clients work out if a transformation is supported by a Transform Service.
 */
public interface TransformServiceRegistry
{
    /**
     * Works out if the Transform Server should be able to transform content of a given source mimetype and size into a
     * target mimetype given a list of actual transform option names and values (Strings) plus the data contained in the
     * {@Transform} objects registered with this class.
     * @param sourceMimetype the mimetype of the source content
     * @param sourceSizeInBytes the size in bytes of the source content. Ignored if negative.
     * @param targetMimetype the mimetype of the target
     * @param actualOptions the actual name value pairs available that could be passed to the Transform Service.
     * @param transformName (optional) name for the set of options and target mimetype. If supplied is used to cache
     *                      results to avoid having to work out if a given transformation is supported a second time.
     *                      The sourceMimetype and sourceSizeInBytes may still change. In the case of ACS this is the
     *                      rendition name.
     */
    boolean isSupported(String sourceMimetype, long sourceSizeInBytes, String targetMimetype,
                        Map<String, String> actualOptions, String transformName);

    /**
     * Returns the maximun size (in bytes) of the source content that can be transformed.
     * @param sourceMimetype the mimetype of the source content
     * @param targetMimetype the mimetype of the target
     * @param actualOptions the actual name value pairs available that could be passed to the Transform Service.
     * @param transformName (optional) name for the set of options and target mimetype. If supplied is used to cache
     *                      results to avoid having to work out if a given transformation is supported a second time.
     *                      The sourceMimetype and sourceSizeInBytes may still change. In the case of ACS this is the
     *                      rendition name.
     * @return the maximum size (in bytes) of the source content that can be transformed. If {@code -1} there is no
     * limit, but if {@code 0} the transform is not supported.
     */
    long getMaxSize(String sourceMimetype, String targetMimetype,
                    Map<String, String> actualOptions, String transformName);
}
