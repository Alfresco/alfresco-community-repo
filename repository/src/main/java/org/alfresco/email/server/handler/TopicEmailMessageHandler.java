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
package org.alfresco.email.server.handler;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.email.EmailMessage;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Handler implementation address to topic node.
 * 
 * @author maxim
 * @since 2.2
 */
public class TopicEmailMessageHandler extends AbstractForumEmailMessageHandler
{
    /**
     * {@inheritDoc}
     */
    public void processMessage(NodeRef nodeRef, EmailMessage message)
    {
        QName nodeTypeQName = getNodeService().getType(nodeRef);
        NodeRef topicNode = null;

        if (getDictionaryService().isSubClass(nodeTypeQName, ForumModel.TYPE_TOPIC))
        {
            topicNode = nodeRef;
        }
        else if (getDictionaryService().isSubClass(nodeTypeQName, ForumModel.TYPE_POST))
        {
            topicNode = getNodeService().getPrimaryParent(nodeRef).getParentRef();
            if (topicNode == null)
            {
                throw new AlfrescoRuntimeException("A POST node has no primary parent: " + nodeRef);
            }
        }
        else
        {
            throw new AlfrescoRuntimeException("\n" +
                    "Message handler " + this.getClass().getName() + " cannot handle type " + nodeTypeQName + ".\n" +
                    "Check the message handler mappings.");
        }
        addPostNode(topicNode, message);
    }
}
