/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.service.cmr.email;

import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service to process email messages. The incoming messages are treated as content that need to be created or modified. The target node can be the address of the node:
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
     * Processes an email message. The message's content is intended for a node found by examining the email's target address.
     * 
     * @param delivery
     *            instructions - who gets the message and who is it from
     * @param message
     *            the email message
     * @throws org.alfresco.service.cmr.email.EmailMessageException
     *             if the message is rejected for <b>any</b> reason
     */
    @Auditable(parameters = {"message"})
    void importMessage(EmailDelivery delivery, EmailMessage message);

    /**
     * Process an email message. The message's content is intended for a specific node.
     * 
     * @param delivery
     *            instructions - who gets the message and who is it from
     * @param nodeRef
     *            the node to import the message to
     * @param message
     *            the email message
     * @throws org.alfresco.service.cmr.email.EmailMessageException
     *             if the message is rejected for <b>any</b> reason
     */
    @Auditable(parameters = {"nodeRef", "message"})
    void importMessage(EmailDelivery delivery, NodeRef nodeRef, EmailMessage message);
}
