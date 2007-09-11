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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.email.EmailMessage;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Abstact class implements common logic for forum processing email mesages.
 * 
 * @author maxim
 * @since 2.2
 */
public abstract class AbstractForumEmailMessageHandler extends AbstractEmailMessageHandler
{
    /**
     * Posts content
     * 
     * @param nodeRef Reference to node
     * @param parser Mail parser
     */
    protected void addPostNode(NodeRef nodeRef, EmailMessage message)
    {
        NodeService nodeService = getNodeService();
        Date now = new Date();
        String nodeName = "posted-" + new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss").format(now) + ".html";

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
        properties.put(ContentModel.PROP_NAME, nodeName);

        ChildAssociationRef childAssoc = nodeService.createNode(nodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, nodeName),
                ForumModel.TYPE_POST, properties);
        NodeRef postNode = childAssoc.getChildRef();

        // Add necessary aspects
        properties.clear();
        properties.put(ContentModel.PROP_TITLE, nodeName);
        nodeService.addAspect(postNode, ContentModel.ASPECT_TITLED, properties);
        properties.clear();
        properties.put(ApplicationModel.PROP_EDITINLINE, true);
        nodeService.addAspect(postNode, ApplicationModel.ASPECT_INLINEEDITABLE, properties);

        // Write content
        if (message.getBody() != null)
        {
            writeContent(postNode, message.getBody().getContent(), message.getBody().getContentType(), message.getBody().getEncoding());
        }
        else
        {
            writeContent(postNode, "<The message was empty>");
        }
        addEmailedAspect(postNode, message);
    }

    /**
     * Finds first child with specified name
     * 
     * @param nodeRef Parent node for the search
     * @param subject String for search
     * @return Reference to found node or null if node isn't found
     */
    protected NodeRef getTopicNode(NodeRef nodeRef, String subject)
    {
        List<ChildAssociationRef> assocRefList = getNodeService().getChildAssocs(nodeRef);
        Iterator<ChildAssociationRef> assocRefIter = assocRefList.iterator();

        while (assocRefIter.hasNext())
        {

            ChildAssociationRef assocRef = assocRefIter.next();
            if (assocRef.getQName().getLocalName().equals(subject))
            {
                return assocRef.getChildRef();
            }
        }
        return null;
    }

    /**
     * Adds topic node into Alfresco repository
     * 
     * @param parentNode Parent node
     * @param name Topic name
     * @return Reference to created node
     */
    protected NodeRef addTopicNode(NodeRef parentNode, String name)
    {

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
        properties.put(ContentModel.PROP_NAME, name);

        ChildAssociationRef association = getNodeService().createNode(parentNode, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                ForumModel.TYPE_TOPIC, properties);
        NodeRef topic = association.getChildRef();

        // Add necessary aspects
        properties.clear();
        properties.put(ApplicationModel.PROP_ICON, "topic");
        getNodeService().addAspect(topic, ApplicationModel.ASPECT_UIFACETS, properties);

        return topic;
    }

}
