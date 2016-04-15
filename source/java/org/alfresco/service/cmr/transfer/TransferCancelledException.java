package org.alfresco.service.cmr.transfer;

/**
 * Transfer service exception class for when the 
 *  transfer was halted through a cancel
 * 
 * @author Mark Rogers
 */
public class TransferCancelledException extends TransferException 
{
    private static final String MSG_CANCELLED = "transfer_service.cancelled";
    private static final long serialVersionUID = -1644569346701052090L;

    public TransferCancelledException()
    {
        super(MSG_CANCELLED);
    }
}
