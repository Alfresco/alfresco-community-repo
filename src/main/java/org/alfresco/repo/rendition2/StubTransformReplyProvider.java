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
 * Used to send transform response messages to remote transform clients.
 * The response is identical to that produced by the Alfresco Transform Service (ATS).<p>
 *
 * Not currently supported in community edition.
 */
public class StubTransformReplyProvider implements TransformReplyProvider
{
    public void produceTransformEvent(NodeRef sourceNodeRef, InputStream transformInputStream,
        TransformDefinition transformDefinition, int transformContentHashCode)
    {
        throw new UnsupportedOperationException("Not currently supported in community edition");
    }

}
