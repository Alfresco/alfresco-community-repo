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
