/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.imap.exception;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Throw runtime exceptions with following Id and cause:<br />
 * - ERROR_CANNOT_GET_A_FOLDER, new FolderException(FolderException.NOT_LOCAL) cause, when folder wasn't found<br />
 * - ERROR_FOLDER_ALREADY_EXISTS, new FolderException(FolderException.ALREADY_EXISTS_LOCALLY), when folder already exists<br />
 * 
 * @author Alex Bykov
 */
public class AlfrescoImapRuntimeException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = -2721708848878740336L;

    public AlfrescoImapRuntimeException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

    public AlfrescoImapRuntimeException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }
}