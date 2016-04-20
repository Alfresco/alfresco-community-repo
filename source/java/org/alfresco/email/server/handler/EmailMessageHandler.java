package org.alfresco.email.server.handler;

import org.alfresco.service.cmr.email.EmailMessage;
import org.alfresco.service.cmr.email.EmailMessageException;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Interface for email handler for processing email message.
 * 
 * @author maxim
 * @since 2.2
 */
public interface EmailMessageHandler
{
    /**
     * Method invokes for processing email message.
     * 
     * @param nodeRef Target node
     * @param message Email message
     * @exception EmailMessageException Exception is thrown if processing was failed
     * @exception DuplicateChildNodeNameException Exception is thrown if node name is duplicate.
     */
    void processMessage(NodeRef nodeRef, EmailMessage message);

}
