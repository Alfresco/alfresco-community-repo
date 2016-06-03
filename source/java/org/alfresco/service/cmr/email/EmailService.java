package org.alfresco.service.cmr.email;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service to process email messages. The incoming messages are treated as content that need
 * to be created or modified. The target node can be the address of the node:
 * 
 * <pre>
 *    14232@alfresco.mycorp.com
 *    where
 *        14232 is a the node's unique identifier (sys:node-dbid)
 * </pre>
 * 
 * @since 2.2
 * @author Derek Hulley
 */
public interface EmailService
{
    /**
     * Processes an email message. The message's content is intended for a node found by
     * examining the email's target address.
     * @param delivery instructions - who gets the message and who is it from
     * @param message the email message
     * @throws org.alfresco.service.cmr.email.EmailMessageException if the message is rejected for <b>any</b> reason
     */
    @Auditable(parameters = { "message" })
    void importMessage(EmailDelivery delivery, EmailMessage message);

    /**
     * Process an email message. The message's content is intended for a specific node.
     * @param delivery instructions - who gets the message and who is it from
     * @param nodeRef the node to import the message to
     * @param message the email message
     * @throws org.alfresco.service.cmr.email.EmailMessageException if the message is rejected for <b>any</b> reason
     */
    @Auditable(parameters = { "nodeRef", "message" })
    void importMessage(EmailDelivery delivery, NodeRef nodeRef, EmailMessage message);
}
