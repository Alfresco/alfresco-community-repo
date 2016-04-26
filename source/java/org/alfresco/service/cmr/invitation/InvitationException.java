package org.alfresco.service.cmr.invitation;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Thrown when there is a problem with an invitation.
 */
public class InvitationException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = -3925105163386197586L;
    
    public InvitationException(String msgId, Object ... args)
    {
        super(msgId, args);
    }
}
