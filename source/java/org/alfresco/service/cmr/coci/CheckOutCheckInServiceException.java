package org.alfresco.service.cmr.coci;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Version opertaions service exception class
 * 
 * @author Roy Wetherall
 */
public class CheckOutCheckInServiceException extends AlfrescoRuntimeException 
{
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 3258410621186618417L;

	/**
	 * Constructor
	 * 
	 * @param message  the error message
	 */
	public CheckOutCheckInServiceException(String message) 
	{
		super(message);
	}

	/**
	 * Constructor
	 * 
	 * @param message		the error message	
	 * @param throwable		the cause of the exeption
	 */
	public CheckOutCheckInServiceException(String message, Throwable throwable)
	{
		super(message, throwable);
	}

    /**
     * Constructor
     * 
     * @param message       the error message   
     * @param throwable     the cause of the exeption
     * @param objects       message arguments
     */
    public CheckOutCheckInServiceException(Throwable throwable, String message, Object ...objects)
    {
        super(message, objects, throwable);
    }

}
