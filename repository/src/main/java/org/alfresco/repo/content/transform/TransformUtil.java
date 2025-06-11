/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

public class TransformUtil
{
    protected NodeService nodeService;

    public NodeService getNodeService()
    {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public String getFilenameFromNodeRef(NodeRef sourceNodeRef, boolean firstLevel)
    {
        String result = null;
        if (sourceNodeRef != null)
        {
            try
            {
                result = (String) nodeService.getProperty(sourceNodeRef, ContentModel.PROP_NAME);
            }
            catch (RuntimeException e)
            {
                // ignore (InvalidNodeRefException/MalformedNodeRefException) but we should ignore other RuntimeExceptions too
            }
        }
        if (result == null && !firstLevel)
        {
            result = "<<TemporaryFile>>";
        }
        return result;
    }
}
