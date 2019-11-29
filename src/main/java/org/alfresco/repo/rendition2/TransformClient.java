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

import org.alfresco.repo.content.transform.UnsupportedTransformationException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Request asynchronous transforms and rendition transforms.
 *
 * @author adavis
 */
public interface TransformClient
{
    /**
     * Checks the transformation required for the rendition is supported.
     * @param sourceNodeRef the source node
     * @param renditionDefinition which rendition to perform
     * @param sourceMimetype the mometype of the source
     * @param sourceSizeInBytes the size in bytes of the source
     * @param contentUrl the url of the source (used in debug).
     * @return and object that contains information that will be passed to
     * {@link #transform(NodeRef, RenditionDefinition2, String, int)} as the {@code transformInfo} parameter to
     * avoid having to work it out again.
     * @throws UnsupportedOperationException if the transform is not supported.
     */
    void checkSupported(NodeRef sourceNodeRef, RenditionDefinition2 renditionDefinition, String sourceMimetype,
                        long sourceSizeInBytes, String contentUrl);

    /**
     * Requests an asynchronous transform and the subsequent linkage of that transform as a rendition.
     * The call to this method <b>MUST</b> be proceeded by a successful call to
     * {@link #checkSupported(NodeRef, RenditionDefinition2, String, long, String)} in the <b>SAME</b> Thread.
     * @param sourceNodeRef the source node
     * @param renditionDefinition which rendition to perform
     * @param user that requested the transform.
     * @param sourceContentHashCode the hash code of the source node's content URL. Used to check the transform result
     * @throws UnsupportedTransformationException if there is an unexpected failure to transform, normally in a
     *         pipeline, where an intermediate transform may not be performed after all because an intermediate
     *         conversion is too big.
     * @throws ContentIOException  there is an unexpected communication or transformation failure.
     */
    void transform(NodeRef sourceNodeRef, RenditionDefinition2 renditionDefinition, String user,
                   int sourceContentHashCode)
        throws UnsupportedTransformationException, ContentIOException;
}
