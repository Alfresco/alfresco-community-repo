/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.service.cmr.subscriptions;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * This exception is thrown if a subscription list is private and the accessing
 * user is not allowed to see it.
 * 
 * @author Florian Mueller
 * @since 4.0
 */
public class PrivateSubscriptionListException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 6971869799749343887L;

    public PrivateSubscriptionListException(String msg)
    {
        super(msg);
    }

    public PrivateSubscriptionListException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
