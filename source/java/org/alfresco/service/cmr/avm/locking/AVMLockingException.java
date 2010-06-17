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
package org.alfresco.service.cmr.avm.locking;

import org.alfresco.service.cmr.avm.AVMException;

/**
 * Exception thrown when a lock is denied.
 * 
 * @author Derek Hulley
 */
public class AVMLockingException extends AVMException
{
    private static final long serialVersionUID = -8446855530834146895L;

    public AVMLockingException(String msgId, Object ... msgParams)
    {
        super(msgId, msgParams);
    }

    public AVMLockingException(Throwable cause, String msgId, Object ... msgParams)
    {
        super(msgId, msgParams, cause);
    }
}
