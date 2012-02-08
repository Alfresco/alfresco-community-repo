/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.email.server.handler;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.email.server.EmailServiceImpl;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.email.EmailMessage;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handler implementation address to document node.
 * 
 * @author maxim
 * @since 2.2
 */
public class DocumentEmailMessageHandler extends AbstractForumEmailMessageHandler
{
    private static Log logger = LogFactory.getLog(DocumentEmailMessageHandler.class);

    private static final String forumNodeName = "EmailForum";

    public void processMessage(NodeRef contentNodeRef, EmailMessage message)
    {
        String messageSubject = message.getSubject();

        if (messageSubject != null && messageSubject.length() > 0)
        {
            messageSubject = message.getSubject();
        }
        else
        {
            messageSubject = "EMPTY_SUBJECT_" + System.currentTimeMillis();
        }
        if(logger.isDebugEnabled())
        {
            logger.debug("process message:" + messageSubject);
        }
        
        QName nodeTypeQName = getNodeService().getType(contentNodeRef);

        DictionaryService dictionaryService = getDictionaryService();
        if (dictionaryService.isSubClass(nodeTypeQName, ContentModel.TYPE_CONTENT))
        {
            // Find where the content resides
            NodeRef spaceNodeRef = getNodeService().getPrimaryParent(contentNodeRef).getParentRef();
            
            NodeRef forumNode = getForumNode(contentNodeRef);

            if (forumNode == null)
            {
                logger.debug("adding new forum node");
                forumNode = addForumNode(contentNodeRef);
            }

            // Try to find existed node
            NodeRef topicNodeRef = getTopicNode(forumNode, messageSubject);

            if (topicNodeRef == null)
            {
                logger.debug("adding new topic node");
                topicNodeRef = addTopicNode(forumNode, messageSubject);
            }

            // Create the post
            logger.debug("add a post to the topic");
            NodeRef postNodeRef = addPostNode(topicNodeRef, message);
            
            // Add attachments
            addAttachments(spaceNodeRef, postNodeRef, message);
        }
        else
        {
            throw new AlfrescoRuntimeException("\n" +
                    "Message handler " + this.getClass().getName() + " cannot handle type " + nodeTypeQName + ".\n" +
                    "Check the message handler mappings.");
        }
    }

    /**
     * Adds forum node
     * 
     * @param nodeRef Paren node
     * @return Reference to created node
     */
    private NodeRef addForumNode(NodeRef nodeRef)
    {
        NodeService nodeService = getNodeService();
        
//        //Add discussable aspect to content node
//        if (!nodeService.hasAspect(nodeRef, ForumModel.ASPECT_DISCUSSABLE))
//        {
//            nodeService.addAspect(nodeRef, ForumModel.ASPECT_DISCUSSABLE, null);
//        }

        //Create forum node and associate it with content node
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
        properties.put(ContentModel.PROP_NAME, forumNodeName);
        ChildAssociationRef childAssoc = nodeService.createNode(nodeRef, ForumModel.ASSOC_DISCUSSION, ForumModel.ASSOC_DISCUSSION, ForumModel.TYPE_FORUM, properties);
        NodeRef forumNode = childAssoc.getChildRef();        

        //Add necessary aspects to forum node
        properties.clear();
        properties.put(ApplicationModel.PROP_ICON, "forum");
        nodeService.addAspect(forumNode, ApplicationModel.ASPECT_UIFACETS, properties);
        
        return forumNode;
    }

    /**
     * Finds the first forum node
     * 
     * @param nodeService Alfresco Node Service
     * @param nodeRef Parent node
     * @return Found node or null
     */
    private NodeRef getForumNode(NodeRef nodeRef)
    {

        if (getNodeService().hasAspect(nodeRef, ForumModel.ASPECT_DISCUSSABLE))
        {
            List<ChildAssociationRef> assocRefList = getNodeService().getChildAssocs(nodeRef);
            Iterator<ChildAssociationRef> assocRefIter = assocRefList.iterator();

            while (assocRefIter.hasNext())
            {
                ChildAssociationRef assocRef = assocRefIter.next();
                QName nodeTypeName = getNodeService().getType(assocRef.getChildRef());

                if (nodeTypeName.equals(ForumModel.TYPE_FORUM))
                    return assocRef.getChildRef();
            }
        }
        return null;
    }
}
