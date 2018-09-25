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

import java.util.Map;

/**
 * This interface is responsible for determining if the a transformation request can be routed to the Transform Service
 *
 * @author aepure
 */

public interface TransformServiceRegistry
{
    /**
     * Validate if a transformation request can pe processed by Transformation Service.
     * @param sourceMimetype Source mimetype of the content.
     * @param size in bytes of the source content. May be {@code -1} if the size should be ignored.
     * @param targetMimetype Target mimetype of the content.
     * @param renditionName the name of the rendition.
     * @param options Transformation request parameters.
     * @return <code>true</code> if the transformation can be processed by Transformation Service.
     *         <code>false</code> otherwise.
     */
    public boolean isSupported(String sourceMimetype, long size, String targetMimetype, String renditionName, Map<String, String> options);

    /**
     * Returns the maximum size the source may be in bytes to still be supported.
     * @param sourceMimetype Source mimetype of the content.
     * @param targetMimetype Target mimetype of the content.
     * @param renditionName the name of the rendition.
     * @param options Transformation request parameters.
     * @return the number of bytes, {@code -1} if not limited or {@code null} if not supported.
     */
    public Long getMaxSize(String sourceMimetype, String targetMimetype, String renditionName, Map<String, String> options);
}
