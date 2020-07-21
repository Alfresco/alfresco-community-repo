/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
import org.alfresco.repo.content.transform.UnsupportedTransformationException;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;

import java.util.Map;

/**
 * Request synchronous transforms.
 *
 * @author adavis
 */
public interface SynchronousTransformClient
{
    /**
     * Works out if it is possible to transform content of a given source mimetype and size into a target mimetype
     * given a list of actual transform option names and values. Ideally, just call the transform method and catch the
     * UnsupportedTransformationException to avoid extra work.
     *
     * @param sourceMimetype the mimetype of the source content
     * @param sourceSizeInBytes the size in bytes of the source content. Ignored if negative.
     * @param contentUrl The url of the content. Optional as it is only used in debug messages.
     * @param targetMimetype the mimetype of the target
     * @param actualOptions the actual name value pairs available that could be passed to the Transform Service.
     * @param transformName (optional) name for the set of options and target mimetype. If supplied is used to cache
     * results to avoid having to work out if a given transformation is supported a second time. The sourceMimetype
     * and sourceSizeInBytes may still change. In the case of ACS this is the rendition name.
     * @param sourceNodeRef (optional) NodeRef of the source content. Only used in debug messages.
     * @return {@code}true{@code} if it is supported.
     */
    boolean isSupported(String sourceMimetype, long sourceSizeInBytes, String contentUrl, String targetMimetype,
                        Map<String, String> actualOptions, String transformName, NodeRef sourceNodeRef);

    /**
     * Requests a synchronous transform.
     * @param reader of the source content
     * @param writer to the target node's content
     * @param actualOptions the actual name value pairs available that could be passed to the Transform Service.
     * @param transformName (optional) name for the set of options and target mimetype. If supplied is used to cache
     * results to avoid having to work out if a given transformation is supported a second time. The sourceMimetype
     * and sourceSizeInBytes may still change. In the case of ACS this is the rendition name.
     * @param sourceNodeRef the source node
     * @throws UnsupportedTransformationException if there is an unexpected failure to transform, normally in a
     *         pipeline, where an intermediate transform may not be performed after all because an intermediate
     *         converion is too big.
     * @throws ContentIOException  there is an unexpected communication or transformation failure.
     * @throws UnsupportedTransformationException if isSupported has not been called and
     */
    void transform(ContentReader reader, ContentWriter writer, Map<String, String> actualOptions,
                   String transformName, NodeRef sourceNodeRef)
            throws UnsupportedTransformationException, ContentIOException;

    /**
     * @return type of transform (Local, Legacy) for use in debug.
     */
    String getName();
}
