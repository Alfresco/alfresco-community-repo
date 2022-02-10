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
import org.alfresco.service.Experimental;

/**
 * Unable to access as content is in an Archived state.
 * Default status is <i>Precondition Failed<i> Client Error = 412
 * 
 * @author David Edwards
 */
@Experimental
@AlfrescoPublicApi
public class ArchivedIOException extends ContentIOException
{
    private static final long serialVersionUID = 3258135874596276087L;

    public ArchivedIOException(String msg) 
    {
        super(msg);
    }

    public ArchivedIOException(String msg, Throwable cause) 
    {
        super(msg, cause);
    }

}
