package org.alfresco.repo.importer;

import org.alfresco.error.AlfrescoRuntimeException;

public class FileImporterException extends AlfrescoRuntimeException
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3544669594364490545L;

    public FileImporterException(String msg)
    {
        super(msg);
    }

    public FileImporterException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

}
