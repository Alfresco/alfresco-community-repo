/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
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
@PublicService
public interface EmailService
{
    /**
     * Processes an email message. The message's content is intended for a node found by
     * examining the email's target address.
     * 
     * @param message the email message
     * @throws EmailMessageRejectException if the message is rejected for <b>any</b> reason
     */
    @Auditable(parameters = { "message" })
    void importMessage(EmailMessage message);

    /**
     * Process an email message. The message's content is intended for a specific node.
     * 
     * @param nodeRef the node to import the message to
     * @param message the email message
     * @throws EmailMessageRejectException if the message is rejected for <b>any</b> reason
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = { "nodeRef", "message" })
    void importMessage(NodeRef nodeRef, EmailMessage message);
}
