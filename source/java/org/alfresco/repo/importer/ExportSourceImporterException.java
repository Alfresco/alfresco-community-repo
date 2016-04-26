package org.alfresco.repo.importer;

import org.alfresco.error.AlfrescoRuntimeException;

public class ExportSourceImporterException extends AlfrescoRuntimeException
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -2366069362776024153L;

    public ExportSourceImporterException(String msgId)
    {
        super(msgId);
    }

    public ExportSourceImporterException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    public ExportSourceImporterException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    public ExportSourceImporterException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

}
