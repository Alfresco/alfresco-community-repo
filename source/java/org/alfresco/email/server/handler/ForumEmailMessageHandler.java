package org.alfresco.email.server.handler;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.email.EmailMessage;
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

        QName nodeTypeQName = getNodeService().getType(nodeRef);

        if (getDictionaryService().isSubClass(nodeTypeQName, ForumModel.TYPE_FORUM))
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
            throw new AlfrescoRuntimeException("\n" +
                    "Message handler " + this.getClass().getName() + " cannot handle type " + nodeTypeQName + ".\n" +
                    "Check the message handler mappings.");
        }
    }
}
