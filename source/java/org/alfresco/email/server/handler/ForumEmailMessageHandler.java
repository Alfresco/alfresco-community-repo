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
package org.alfresco.email.server.handler;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.email.EmailMessage;
import org.alfresco.service.cmr.email.EmailMessageException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Handler implementation address to forum node.
 * 
 * @author maxim
 * @since 2.2
 */
public class ForumEmailMessageHandler extends AbstractForumEmailMessageHandler
{
    /**
     * {@inheritDoc}
     */
    public void processMessage(NodeRef nodeRef, EmailMessage message)
    {
        String messageSubject;

        if (message.getSubject() != null)
        {
            messageSubject = message.getSubject();
        }
        else
        {
            messageSubject = "EMPTY_SUBJECT_" + System.currentTimeMillis();
        }

        QName nodeType = getNodeService().getType(nodeRef);

        if (nodeType.equals(ForumModel.TYPE_FORUM))
        {
            NodeRef topicNode = getTopicNode(nodeRef, messageSubject);

            if (topicNode == null)
            {
                topicNode = addTopicNode(nodeRef, messageSubject);
            }
            addPostNode(topicNode, message);
        }
        else
        {
            throw new EmailMessageException(I18NUtil.getMessage("email.server.incorrect-node-type"));
        }
    }
}
