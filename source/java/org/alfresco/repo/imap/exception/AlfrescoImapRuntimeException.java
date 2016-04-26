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