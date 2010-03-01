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
package org.alfresco.service.cmr.avm;

/**
 * A Debugging exception.
 * @author britt
 */
public class AVMReadOnlyException extends AVMException 
{
    private static final long serialVersionUID = 5074287797390504317L;

    /**
     * @param msgId
     */
    public AVMReadOnlyException(String msgId) 
    {
        super(msgId);
    }

    /**
     * @param msgId
     * @param msgParams
     */
    public AVMReadOnlyException(String msgId, Object[] msgParams) 
    {
        super(msgId, msgParams);
    }

    /**
     * @param msgId
     * @param cause
     */
    public AVMReadOnlyException(String msgId, Throwable cause) 
    {
        super(msgId, cause);
    }

    /**
     * @param msgId
     * @param msgParams
     * @param cause
     */
    public AVMReadOnlyException(String msgId, Object[] msgParams,
            Throwable cause) 
    {
        super(msgId, msgParams, cause);
    }
}
