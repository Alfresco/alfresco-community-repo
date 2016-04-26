package org.alfresco.repo.audit;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Audit related exceptions.
 * 
 * @author Andy Hind
 */
public class AuditException extends AlfrescoRuntimeException
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -7947190775692164588L;

    /**
     * Simple message
     * 
     * @param msgId String
     */
    public AuditException(String msgId)
    {
        super(msgId);
    }

    /**
     * I18n message
     * 
     * @param msgId String
     * @param msgParams Object[]
     */
    public AuditException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    /**
     * Simple message ad nested exception
     * 
     * @param msgId String
     * @param cause Throwable
     */
    public AuditException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    /**
     * I18n message and exception.
     * 
     * @param msgId String
     * @param msgParams Object[]
     * @param cause Throwable
     */
    public AuditException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

}
