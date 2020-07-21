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

import org.alfresco.service.cmr.repository.NodeRef;

import java.io.InputStream;

/**
 * Produce TransformReply events.
 *
 * @author aepure
 */
public interface TransformReplyProvider
{
    /**
     * Stores content from transformInputStream on a defined store,
     * produces a TransformReply response message and send it to a
     * specific queue defined in transformDefinition.
     *
     * @param sourceNodeRef the node from which the content is retrieved.
     * @param transformInputStream content resulted after transformation.
     * @param transformDefinition which defines the transform, where to sent the response and some client specified data.
     * @param transformContentHashCode hash code of the resulted content.
     */
    void produceTransformEvent(NodeRef sourceNodeRef, InputStream transformInputStream,
        TransformDefinition transformDefinition, int transformContentHashCode);
}
