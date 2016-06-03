package org.alfresco.service.cmr.transfer;


/**
 * Transfer failure exception
 * 
 * @author davidc
 */
public class TransferFailureException extends TransferException 
{
    private static final long serialVersionUID = 9009938314128119981L;
    
    private TransferEventError event;

    public TransferFailureException(TransferEventError event)
    {
        super(event.getMessage(), event.getException());
        this.event = event;
    }

    /**
     * Gets the end event (representing the failure)
     * 
     * @return end event
     */
    public TransferEventError getErrorEvent()
    {
        return event; 
    }
}
