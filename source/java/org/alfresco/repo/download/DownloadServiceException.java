package org.alfresco.repo.download;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Download Service Exception class
 *
 * @author Alex Miller
 */
public class DownloadServiceException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 1826926526215676002L;

    public DownloadServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
