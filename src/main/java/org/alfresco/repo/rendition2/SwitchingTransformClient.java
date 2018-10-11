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

import org.alfresco.repo.rendition2.RenditionDefinition2;
import org.alfresco.repo.rendition2.TransformClient;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A transform client that falls back between different implementations if not supported.
 *
 * @author adavis
 */
public class SwitchingTransformClient implements TransformClient
{
    private final TransformClient primary;
    private final TransformClient secondary;
    private ThreadLocal<Boolean> usePrimary = new ThreadLocal<>();

    public SwitchingTransformClient(TransformClient primary, TransformClient secondary)
    {
        this.primary = primary;
        this.secondary = secondary;
    }

    @Override
    public void checkSupported(NodeRef sourceNodeRef, RenditionDefinition2 renditionDefinition, String sourceMimetype, long size, String contentUrl)
    {
        try
        {
            usePrimary.set(true);
            primary.checkSupported(sourceNodeRef, renditionDefinition, sourceMimetype, size, contentUrl);
        }
        catch (UnsupportedOperationException e)
        {
            usePrimary.set(false);
            secondary.checkSupported(sourceNodeRef, renditionDefinition, sourceMimetype, size, contentUrl);
        }
    }

    @Override
    public void transform(NodeRef sourceNodeRef, RenditionDefinition2 renditionDefinition, String user, int sourceContentUrlHashCode)
    {
        if (usePrimary.get())
        {
            primary.transform(sourceNodeRef, renditionDefinition, user, sourceContentUrlHashCode);
        }
        else
        {
            secondary.transform(sourceNodeRef, renditionDefinition, user, sourceContentUrlHashCode);
        }
    }
}
