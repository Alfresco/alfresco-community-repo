/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.service.cmr.repository;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.Experimental;

/**
 * Content Restoration in progress
 * 
 * @author David Edwards
 */
@AlfrescoPublicApi
@Experimental
public class RestoreInProgressException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 5483215922384016269L;

    public RestoreInProgressException(String msgId)
    {
        super(msgId);
    }

    public RestoreInProgressException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    public RestoreInProgressException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    public RestoreInProgressException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }
}
