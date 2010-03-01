/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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

package org.alfresco.repo.transfer;

import org.alfresco.service.cmr.transfer.TransferException;

/**
 * @author brian
 *
 */
public class TransferProcessingException extends TransferException
{
    /**
     * 
     */
    private static final long serialVersionUID = 2547803698674661069L;

    /**
     * @param msgId
     * @param msgParams
     * @param cause
     */
    public TransferProcessingException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

    /**
     * @param msgId
     * @param msgParams
     */
    public TransferProcessingException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    /**
     * @param msgId
     * @param cause
     */
    public TransferProcessingException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    /**
     * @param msgId
     */
    public TransferProcessingException(String msgId)
    {
        super(msgId);
    }
}
