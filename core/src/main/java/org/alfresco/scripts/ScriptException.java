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
package org.alfresco.scripts;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * @author Kevin Roast
 */
public class ScriptException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 1739480648583299623L;

    /**
     * @param msgId String
     */
    public ScriptException(String msgId)
    {
        super(msgId);
    }

    /**
     * @param msgId String
     * @param cause Throwable
     */
    public ScriptException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }
    
    /**
     * @param msgId String
     * @param params Object[]
     */
    public ScriptException(String msgId, Object[] params)
    {
        super(msgId, params);
    }
    
    /**
     * @param msgId String
     * @param msgParams Object[]
     * @param cause Throwable
     */
    public ScriptException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }
}
