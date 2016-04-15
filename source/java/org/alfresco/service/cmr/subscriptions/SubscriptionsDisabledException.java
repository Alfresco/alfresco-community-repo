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
package org.alfresco.service.cmr.subscriptions;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * This exception is thrown if subscriptions are disabled.
 * 
 * @author Florian Mueller
 * @since 4.0
 */
public class SubscriptionsDisabledException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 6971869799749343887L;

    public SubscriptionsDisabledException(String msg)
    {
        super(msg);
    }

    public SubscriptionsDisabledException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
