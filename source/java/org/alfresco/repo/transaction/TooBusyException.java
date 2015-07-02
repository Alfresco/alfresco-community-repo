/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.transaction;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.error.AlfrescoRuntimeException;

/**
 * An exception thrown by {@link RetryingTransactionHelper} when its maxExecutionMs property is set and there isn't
 * enough capacity to execute / retry the transaction.
 * 
 * @author dward
 */
@AlfrescoPublicApi
public class TooBusyException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 1L;

    /**
     * @param msgId String
     */
    public TooBusyException(String msgId)
    {
        super(msgId);
    }

    /**
     * @param msgId String
     * @param msgParams Object[]
     */
    public TooBusyException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    /**
     * @param msgId String
     * @param cause Throwable
     */
    public TooBusyException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    /**
     * @param msgId String
     * @param msgParams Object[]
     * @param cause Throwable
     */
    public TooBusyException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

}
