package org.alfresco.repo.imap.exception;

import com.icegreen.greenmail.store.FolderException;

/**
 * Thrown on an inappropriate attempt to modify a folder.
 * 
 * @author Ivan Rybnikov
 */
public class AlfrescoImapFolderException extends FolderException
{

    private static final long serialVersionUID = -2721708848846740336L;

    public final static String PERMISSION_DENIED = "Cannot perform action - Permission denied";

    public AlfrescoImapFolderException(String message)
    {
        super(message);
    }

}
