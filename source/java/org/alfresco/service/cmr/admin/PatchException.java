package org.alfresco.service.cmr.admin;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Thrown when a patch fails to execute successfully.
 * 
 * @author Derek Hulley
 */
public class PatchException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 7022368915143884315L;

    /**
     * @param msgId the patch failure message ID
     */
    public PatchException(String msgId)
    {
        super(msgId);
    }

    /**
     * @param msgId the patch failure message ID
     * @param args variable number of message arguments
     */
    public PatchException(String msgId, Object ... args)
    {
        super(msgId, args);
    }
}
