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

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;

import org.alfresco.email.server.EmailServerModel;
import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.email.EmailMessage;
import org.alfresco.service.cmr.email.EmailMessageException;
import org.alfresco.service.cmr.email.EmailMessagePart;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handler implementation address to folder node.
 * 
 * @author Yan O
 * @since 2.2
 */
public class FolderEmailMessageHandler extends AbstractEmailMessageHandler
{
    private static final Log log = LogFactory.getLog(FolderEmailMessageHandler.class);

    /**
     * {@inheritDoc}
     */
    public void processMessage(NodeRef nodeRef, EmailMessage message)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Message is psocessing by SpaceMailMessageHandler");
        }
        try
        {
            // Check type of the node. It must be a SPACE
            QName nodeTypeName = getNodeService().getType(nodeRef);

            if (nodeTypeName.equals(ContentModel.TYPE_FOLDER))
            {
                // Add the content into the system
                addAlfrescoContent(nodeRef, message, null);
            }
            else
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Addressed node type isn't a folder. Message has been passed without any actions.");
                }
            }
        }
        catch (IOException ex)
        {
            throw new EmailMessageException(I18NUtil.getMessage("email.server.content-error"), ex);
        }
    }

    /**
     * Add content to Alfresco repository
     * 
     * @param spaceNodeRef Addressed node
     * @param mailParser Mail message
     * @param nameConflictResolver String that can be used as part of name for resolving name conflict.
     * @throws IOException Exception can be thrown while saving a content into Alfresco repository.
     * @throws MessagingException Exception can be thrown while parsing e-mail message.
     */
    public void addAlfrescoContent(NodeRef spaceNodeRef, EmailMessage message, String nameConflictResolver) throws IOException
    {
        // Set default values for email fields
        if (nameConflictResolver == null)
            nameConflictResolver = "";
        String messageSubject = "EMPTY_SUBJECT_" + nameConflictResolver;
        if (message.getSubject().length() != 0)
        {
            messageSubject = message.getSubject() + nameConflictResolver;
        }

        // Create node
        if (log.isDebugEnabled())
        {
            log.debug("Adding main content node ...");
        }
        NodeRef contentNodeRef;
        contentNodeRef = addContentNode(getNodeService(), spaceNodeRef, messageSubject);

        // Add titled aspect
        addTitledAspect(contentNodeRef, messageSubject);

        // Add emailed aspect
        addEmailedAspect(contentNodeRef, message);

        // Write the message content

        if (message.getBody() != null)
            writeContent(contentNodeRef, message.getBody().getContent(), message.getBody().getContentType(), message.getBody().getEncoding());
        else
            writeContent(contentNodeRef, "<The message was empty>");
        if (log.isDebugEnabled())
        {
            log.debug("Main content node has been added.");
        }

        // Add attachments
        EmailMessagePart[] attachments = message.getAttachments();
        for (EmailMessagePart attachment : attachments)
        {
            NodeRef attachmentNode;
            String fileName = attachment.getFileName();

            // Add name conflict resolver if necessary
            if (nameConflictResolver.length() != 0)
            {
                if (fileName.lastIndexOf('.') != -1)
                    fileName = fileName.substring(0, fileName.lastIndexOf('.')) + " (" + nameConflictResolver + ")" + fileName.substring(fileName.lastIndexOf('.'));
                else
                    fileName += " (" + nameConflictResolver + ")";
            }

            attachmentNode = addAttachment(getNodeService(), spaceNodeRef, contentNodeRef, fileName);
            writeContent(attachmentNode, attachment.getContent(), attachment.getContentType(), attachment.getEncoding());
        }
    }

    /**
     * Add new node into Alfresco repository with specified parameters. Node content isn't added. New node will be created with ContentModel.ASSOC_CONTAINS association with parent.
     * 
     * @param nodeService Alfresco Node Service
     * @param parent Parent node
     * @param name Name of the new node
     * @return Reference to created node
     */
    private NodeRef addContentNode(NodeService nodeService, NodeRef parent, String name)
    {
        return addContentNode(nodeService, parent, name, ContentModel.ASSOC_CONTAINS);
    }

    /**
     * Add new node into Alfresco repository with specified parameters. Node content isn't added.
     * 
     * @param nodeService Alfresco Node Service
     * @param parent Parent node
     * @param name Name of the new node
     * @param assocType Association type that should be set between parent node and the new one.
     * @return Reference to created node
     */
    private NodeRef addContentNode(NodeService nodeService, NodeRef parent, String name, QName assocType)
    {
        Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
        contentProps.put(ContentModel.PROP_NAME, name);
        ChildAssociationRef associationRef = nodeService.createNode(parent, assocType, QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, name), ContentModel.TYPE_CONTENT,
                contentProps);
        return associationRef.getChildRef();
    }

    /**
     * Adds new node into Alfresco repository and mark its as an attachment.
     * 
     * @param nodeService Alfresco Node Service.
     * @param folder Space/Folder to add.
     * @param mainContentNode Main content node. Any mail is added into Alfresco as one main content node and several its attachments. Each attachment related with its main node.
     * @param fileName File name for the attachment.
     * @return Reference to created node.
     */
    private NodeRef addAttachment(NodeService nodeService, NodeRef folder, NodeRef mainContentNode, String fileName)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Adding attachment node (name=" + fileName + ").");
        }
        
        NodeRef attachmentNode = addContentNode(nodeService, folder, fileName);

        // Add attached aspect
        Map<QName, Serializable> attachedProps = new HashMap<QName, Serializable>();
        nodeService.addAspect(attachmentNode, EmailServerModel.ASPECT_ATTACHED, attachedProps);
        nodeService.createAssociation(attachmentNode, mainContentNode, EmailServerModel.ASSOC_ATTACHMENT);
        
        if (log.isDebugEnabled())
        {
            log.debug("Attachment has been added.");
        }
        return attachmentNode;
    }

    /**
     * Adds titled aspect to the specified node.
     * 
     * @param nodeRef Target node.
     * @param title Title
     */
    private void addTitledAspect(NodeRef nodeRef, String title)
    {
        Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>();
        titledProps.put(ContentModel.PROP_TITLE, title);
        titledProps.put(ContentModel.PROP_DESCRIPTION, "Received by SMTP");
        getNodeService().addAspect(nodeRef, ContentModel.ASPECT_TITLED, titledProps);
        
        if (log.isDebugEnabled())
        {
            log.debug("Titled aspect has been added.");
        }
    }
}
