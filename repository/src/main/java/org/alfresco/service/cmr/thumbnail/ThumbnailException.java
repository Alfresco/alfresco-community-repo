/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.service.cmr.thumbnail;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Thumbnail service exception class
 * 
 * @author Roy Wetherall
 *
 * @deprecated We are introducing the new async RenditionService2.
 */
@Deprecated
public class ThumbnailException extends AlfrescoRuntimeException
{
    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 3257571685241467958L;

    public ThumbnailException(String msgId)
    {
        super(msgId);
    }

    public ThumbnailException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    public ThumbnailException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

    public ThumbnailException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }
}
