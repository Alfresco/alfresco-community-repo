package org.alfresco.service.cmr.action;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Rule Service Exception Class
 * 
 * @author Roy Wetherall
 */
public class ActionServiceException extends AlfrescoRuntimeException 
{
	/**
	 * Serial version UID 
	 */
	private static final long serialVersionUID = 3257571685241467958L;

    public ActionServiceException(String msgId)
    {
        super(msgId);
    }

    public ActionServiceException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    public ActionServiceException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

    public ActionServiceException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }
}
