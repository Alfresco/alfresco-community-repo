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
package org.alfresco.repo.content.transform;

import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.Map;

/**
 * Interface of a local transformer using flat transform options.
 */
public interface LocalTransform
{
    /**
     * Requests a synchronous transform.
     * @param reader of the source content
     * @param writer to the target node's content
     * @param transformOptions the actual name value pairs available that could be passed to the Transform Service.
     * @param renditionName (optional) name for the set of options and target mimetype. If supplied is used to cache
     * results to avoid having to work out if a given transformation is supported a second time. The sourceMimetype
     * and sourceSizeInBytes may still change. In the case of ACS this is the rendition name.
     * @param sourceNodeRef the source node
     * @throws UnsupportedTransformationException if there is an unexpected failure to transform, normally in a
     *         pipeline, where an intermediate transform may not be performed after all because an intermediate
     *         converion is too big.
     * @throws ContentIOException  there is an unexpected communication or transformation failure.
     */
    void transform(ContentReader reader, ContentWriter writer, Map<String, String> transformOptions,
                   String renditionName, NodeRef sourceNodeRef)
            throws UnsupportedTransformationException, ContentIOException;

    /**
     * @return the name of the transform.
     */
    String getName();
}
