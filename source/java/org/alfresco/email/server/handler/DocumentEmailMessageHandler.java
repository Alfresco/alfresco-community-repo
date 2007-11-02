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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

/**
 * Handler implementation address to document node.
 * 
 * @author maxim
 * @since 2.2
 */
public class DocumentEmailMessageHandler extends AbstractForumEmailMessageHandler
{
    private static final String forumNodeName = "EmailForum";

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

        DictionaryService dictionaryService = getDictionaryService();
        if (dictionaryService.isSubClass(nodeTypeQName, ContentModel.TYPE_CONTENT))
        {
            NodeRef forumNode = getForumNode(nodeRef);

            if (forumNode == null)
            {
                forumNode = addForumNode(nodeRef);
            }

            // Try to find existed node
            NodeRef topicNode = getTopicNode(forumNode, messageSubject);

            if (topicNode == null)
            {
                topicNode = addTopicNode(forumNode, messageSubject);
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

    /**
     * Adds forum node
     * 
     * @param nodeRef Paren node
     * @return Reference to created node
     */
    private NodeRef addForumNode(NodeRef nodeRef)
    {
        NodeService nodeService = getNodeService();
        //Add discussable aspect to content node
        if (!nodeService.hasAspect(nodeRef, ForumModel.ASPECT_DISCUSSABLE))
        {
            nodeService.addAspect(nodeRef, ForumModel.ASPECT_DISCUSSABLE, null);
        }

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
