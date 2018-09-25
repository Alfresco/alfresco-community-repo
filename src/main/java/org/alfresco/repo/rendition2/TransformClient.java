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

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Request rendition transforms.
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
     * @param size the size in bytes of the source
     * @param contentUrl the url of the source (used in debug).
     * @return and object that contains information that will be passed to
     * {@link #transform(NodeRef, RenditionDefinition2, String, int)} as the {@code transformInfo} parameter to
     * avoid having to work it out again.
     * @throws UnsupportedOperationException if the transform is not supported.
     */
    void checkSupported(NodeRef sourceNodeRef, RenditionDefinition2 renditionDefinition, String sourceMimetype, long size, String contentUrl);

    /**
     * Requests an asynchronous transform and the subsequent linkage of that transform as a rendition.
     * @param sourceNodeRef the source node
     * @param renditionDefinition which rendition to perform
     * @param user that requested the transform.
     * @param sourceContentUrlHashCode the hash code of the source node's content URL. Used to check the transform result
     */
    void transform(NodeRef sourceNodeRef, RenditionDefinition2 renditionDefinition, String user, int sourceContentUrlHashCode);
}
