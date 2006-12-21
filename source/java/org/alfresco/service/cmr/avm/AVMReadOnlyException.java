/**
 * 
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
