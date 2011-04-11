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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.email.server.EmailServerModel;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ImapModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.email.EmailMessage;
import org.alfresco.service.cmr.email.EmailMessagePart;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.star.auth.InvalidArgumentException;

/**
 * Abstract class implements common logic for processing email messages.
 * 
 * @author maxim
 * @since 2.2
 */
public abstract class AbstractEmailMessageHandler implements EmailMessageHandler
{
    private static final Log log = LogFactory.getLog(EmailMessageHandler.class);

    private DictionaryService dictionaryService;
    private NodeService nodeService;
    private SearchService searchService;
    private ContentService contentService;
    private MimetypeService mimetypeService;

    /**
     * @return Alfresco Content Service.
     */
    protected ContentService getContentService()
    {
        return contentService;
    }

    /**
     * @param contentService Alfresco Content Service.
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * @return                      the Alfresco dictionary service
     */
    protected DictionaryService getDictionaryService()
    {
        return dictionaryService;
    }

    /**
     * @param dictionaryService     Alfresco dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @return Alfresco Node Service.
     */
    protected NodeService getNodeService()
    {
        return nodeService;
    }

    /**
     * @param nodeService Alfresco Node Service.
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param searchService Alfresco Search Service.
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * @return      the service used to determine mimeypte and encoding
     */
    protected MimetypeService getMimetypeService()
    {
        return mimetypeService;
    }

    /**
     * @param mimetypeService       the the service to determine mimetype and encoding
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    /**
     * @param to Email address which user part specifies node-dbid
     * @return Referance to requested node.
     * @throws InvalidArgumentException The exception is thrown if input string has incorrect format or empty.
     */
    protected NodeRef getTargetNode(String to) throws InvalidArgumentException
    {
        if (to == null || to.length() == 0)
        {
            throw new InvalidArgumentException("Input string has to contain email address.");
        }
        String[] parts = to.split("@");
        if (parts.length != 2)
        {
            throw new InvalidArgumentException("Incorrect email address format.");
        }
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        String query = "@sys\\:node-dbid:" + parts[0];
        ResultSet resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, query);
        try
        {
            if (resultSet.length() == 1)
            {
                return resultSet.getNodeRef(0);
            }
        }
        finally
        {
            resultSet.close();
        }
        return null;
    }

    /**
     * Write the content to the node 
     * 
     * @param nodeRef Target node
     * @param content Content
     */
    protected void writeContent(NodeRef nodeRef, String content)
    {
        writeContent(nodeRef, content, MimetypeMap.MIMETYPE_TEXT_PLAIN);
    }

    /**
     * Write the string as content to the node.
     * 
     * @param nodeRef Target node.
     * @param content Text for writting.
     * @param mimetype MIME content type. For exaple you can set this parameter to "text/html" or "text/xml", etc.
     */
    protected void writeContent(NodeRef nodeRef, String content, String mimetype)
    {
        try
        {
            InputStream inputStream = new ByteArrayInputStream(content.getBytes("UTF-8"));
            writeContent(nodeRef, inputStream, mimetype, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new AlfrescoRuntimeException("Failed to write content", e);
        }
    }

    /**
     * Write content to the node from InputStream.
     * 
     * @param nodeRef Target node.
     * @param content Content stream.
     * @param mimetype MIME content type.
     * @param encoding Encoding. Can be null for non text based content.
     */
    protected void writeContent(NodeRef nodeRef, InputStream content, String mimetype, String encoding)
    {
        InputStream bis = new BufferedInputStream(content, 4092);
        
        // Guess the encoding if it is text
        if (mimetypeService.isText(mimetype))
        {
            ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
            encoding = charsetFinder.getCharset(bis, mimetype).name();
        }
        else if (encoding == null)
        {
            encoding = "UTF-8";
        }
        
        if (log.isDebugEnabled())
        {
            log.debug("Write content (MimeType=\"" + mimetype + "\", Encoding=\"" + encoding + "\"");
        }
        
        
        ContentService contentService = getContentService();
        ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(mimetype);
        writer.setEncoding(encoding);
        writer.putContent(bis);
    }
    
    /**
     * Add emailed aspect to the specified node.
     * 
     * @param nodeService Alfresco Node Service.
     * @param nodeRef Target node.
     * @param mailParser Mail message that will be used for extracting necessary information
     */
    protected void addEmailedAspect(NodeRef nodeRef, EmailMessage message) 
    {

    /*
     * TODO - get rid of this and use the RFC822 metadata extractor instead.
     */
        Map<QName, Serializable> emailProps = new HashMap<QName, Serializable>();
        emailProps.put(ContentModel.PROP_SENTDATE, message.getSentDate());
        emailProps.put(ContentModel.PROP_ORIGINATOR, message.getFrom());
        emailProps.put(ContentModel.PROP_ADDRESSEE, message.getTo());
        emailProps.put(ContentModel.PROP_ADDRESSEES, (Serializable)message.getCC());
        emailProps.put(ContentModel.PROP_SUBJECT, message.getSubject());
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_EMAILED, emailProps);

        /*
         * MER 
         * Can't add IMAP_CONTENT here since that means the body of the message is a mime message. 
         */
        //Map<QName, Serializable> imapProps = new HashMap<QName, Serializable>();
        //emailProps.put(ImapModel.PROP_MESSAGE_FROM, message.getFrom());
        //emailProps.put(ImapModel.PROP_MESSAGE_TO, message.getTo());
        //emailProps.put(ImapModel.PROP_MESSAGE_CC, (Serializable)message.getCC());
        //emailProps.put(ImapModel.PROP_MESSAGE_SUBJECT, message.getSubject());
        //nodeService.addAspect(nodeRef, ImapModel.ASPECT_IMAP_CONTENT, imapProps);
        
        if (log.isDebugEnabled())
        {
            log.debug("Emailed aspect has been added.");
        }
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
    protected NodeRef addContentNode(NodeService nodeService, NodeRef parent, String name, QName assocType)
    {
        NodeRef childNodeRef = nodeService.getChildByName(parent, assocType, name);
        if (childNodeRef != null)
        {
            // The node is present already.  Make sure the name csae is correct
            nodeService.setProperty(childNodeRef, ContentModel.PROP_NAME, name);
        }
        else
        {
            Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
            contentProps.put(ContentModel.PROP_NAME, name);
            ChildAssociationRef associationRef = nodeService.createNode(
                    parent,
                    assocType,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                    ContentModel.TYPE_CONTENT,
                    contentProps);
            childNodeRef = associationRef.getChildRef();
        }
        return childNodeRef;
    }

    /**
     * Add new node into Alfresco repository with specified parameters.
     * Node content isn't added. New node will be created with ContentModel.ASSOC_CONTAINS association with parent.
     * 
     * @param nodeService Alfresco Node Service
     * @param parent Parent node
     * @param name Name of the new node
     * @return Reference to created node
     */
    protected NodeRef addContentNode(NodeService nodeService, NodeRef parent, String name)
    {
        return addContentNode(nodeService, parent, name, ContentModel.ASSOC_CONTAINS);
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
    protected NodeRef addAttachment(NodeService nodeService, NodeRef folder, NodeRef mainContentNode, String fileName)
    {
        fileName = getAppropriateNodeName(folder, fileName, ContentModel.ASSOC_CONTAINS);
        
        if (log.isDebugEnabled())
        {
            log.debug("Adding attachment node (name=" + fileName + ").");
        }
        
        NodeRef attachmentNode = addContentNode(nodeService, folder, fileName);
        
        // Add attached aspect
        nodeService.addAspect(mainContentNode, ContentModel.ASPECT_ATTACHABLE, null);
        // Add the association
        nodeService.createAssociation(mainContentNode, attachmentNode, ContentModel.ASSOC_ATTACHMENTS);
        
        if (log.isDebugEnabled())
        {
            log.debug("Attachment has been added.");
        }
        return attachmentNode;
    }
    
    /**
     * Return unique content name in passed folder based on provided name
     * 
     * @param parent parent folder
     * @param name name of node
     * @param assocType assocType between parent and child  
     * @return Original name or name in format {name}({number})
     */
    private String getAppropriateNodeName(NodeRef parent, String name, QName assocType)
    {
        if (nodeService.getChildByName(parent, assocType, name) != null)
        {
            name = name + "(1)";
            while (nodeService.getChildByName(parent, assocType, name) != null)
            {

                int index = name.lastIndexOf("(");
                if ((index > 0) && name.charAt(name.length() - 1) == ')')
                {
                    String posibleNumber = name.substring(index + 1, name.length() - 1);
                    long num = Long.parseLong(posibleNumber) + 1;
                    name = name.substring(0, index) + "(" + num + ")";
                }

            }
        }
        return name;
    }
    
    /**
     * Extracts the attachments from the given message and adds them to the space.  All attachments
     * are linked back to the original node that they are attached to.
     * 
     * @param spaceNodeRef      the space to add the documents into
     * @param nodeRef           the node to which the documents will be attached
     * @param message           the email message
     */
    protected void addAttachments(NodeRef spaceNodeRef, NodeRef nodeRef, EmailMessage message)
    {
        // Add attachments
        EmailMessagePart[] attachments = message.getAttachments();
        for (EmailMessagePart attachment : attachments)
        {
            String fileName = attachment.getFileName();

            InputStream contentIs = attachment.getContent();
            
            MimetypeService mimetypeService = getMimetypeService();
            String mimetype = mimetypeService.guessMimetype(fileName);
            String encoding = attachment.getEncoding();

            NodeRef attachmentNode = addAttachment(getNodeService(), spaceNodeRef, nodeRef, fileName);
            writeContent(attachmentNode, contentIs, mimetype, encoding);
        }
    }
}
