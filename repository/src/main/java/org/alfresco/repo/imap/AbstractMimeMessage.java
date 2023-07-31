/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.repo.imap;

import static org.alfresco.repo.imap.AlfrescoImapConst.MIME_VERSION;
import static org.alfresco.repo.imap.AlfrescoImapConst.X_ALF_NODEREF_ID;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import jakarta.mail.Flags;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import org.alfresco.model.ImapModel;
import org.alfresco.repo.imap.ImapService.EmailBodyFormat;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Arseny Kovalchuk
 */
public abstract class AbstractMimeMessage extends MimeMessage
{    
    protected static final String DEFAULT_SUFFIX = "@alfresco.org";

    protected static int MAX_RETRIES = 1;

    private Log logger = LogFactory.getLog(AbstractMimeMessage.class);
    
    protected boolean generateBody = true;
    
    protected ServiceRegistry serviceRegistry;
    protected ImapService imapService;
    protected FileInfo messageFileInfo;
    protected MimeMessage wrappedMessage;
    protected boolean isMessageInSitesLibrary;

    protected AbstractMimeMessage(Session session)
    {
        super(session);
    }

    protected AbstractMimeMessage(FileInfo fileInfo, ServiceRegistry serviceRegistry, boolean generateBody) throws MessagingException
    {
        super(Session.getDefaultInstance(new Properties()));
        this.generateBody = generateBody;
        buildMessage(fileInfo, serviceRegistry);
    }

    protected void buildMessage(FileInfo fileInfo, ServiceRegistry serviceRegistry) throws MessagingException
    {
        checkParameter(serviceRegistry, "ServiceRegistry");
        this.content = null;
        this.serviceRegistry = serviceRegistry;
        this.imapService = serviceRegistry.getImapService();
        this.messageFileInfo = fileInfo;
        this.isMessageInSitesLibrary = imapService.getNodeSiteContainer(messageFileInfo.getNodeRef()) != null ? true : false;
        RetryingTransactionHelper txHelper = serviceRegistry.getTransactionService().getRetryingTransactionHelper();
        txHelper.setMaxRetries(MAX_RETRIES);
        txHelper.setReadOnly(false);
        txHelper.doInTransaction(new RetryingTransactionCallback<Object>() {
            public Object execute() throws Throwable
            {
                buildMessageInternal();
                return null;
            }
        }, false);
        
    }
    
    /**
     * Method must be implemented in subclasses. It usually should be used to generate message body.
     * 
     * @throws MessagingException
     */
    public abstract void buildMessageInternal() throws MessagingException;
    
    /**
     * Method that checks mandatory parameter.
     * @param parameter The parameter instance to check.
     * @param name The name of the parameter.
     */
    protected void checkParameter(Object parameter, String name)
    {
        if (parameter == null)
        {
            throw new IllegalArgumentException(name + " parameter is null.");
        }
    }
    
    protected void setMessageHeaders() throws MessagingException
    {
        setHeader(MIME_VERSION, "1.0");
        // Optional headers for further implementation of multiple Alfresco server support.
        setHeader(X_ALF_NODEREF_ID, messageFileInfo.getNodeRef().getId());
        // setHeader(X_ALF_SERVER_UID, imapService.getAlfrescoServerUID());
        
        setPersistedHeaders();
    }
    
    private void setPersistedHeaders() throws MessagingException
    {
        NodeService nodeService = serviceRegistry.getNodeService();
        if (nodeService.hasAspect(messageFileInfo.getNodeRef(), ImapModel.ASPECT_IMAP_MESSAGE_HEADERS))
        {
            @SuppressWarnings("unchecked")
            List<String> messageHeaders = (List<String>)nodeService.getProperty(messageFileInfo.getNodeRef(), ImapModel.PROP_MESSAGE_HEADERS);
            
            if (messageHeaders == null)
            {
                return;
            }
            
            for (String header : messageHeaders)
            {
                String headerValue = header.substring(header.indexOf(ImapModel.MESSAGE_HEADER_TO_PERSIST_SPLITTER) + 1);
                String headerName  = header.substring(0, header.indexOf(ImapModel.MESSAGE_HEADER_TO_PERSIST_SPLITTER));
                
                setHeader(headerName, headerValue);
            }
        }
    }

  
    /**
     * Returns {@link FileInfo} object representing message in Alfresco.
     * 
     * @return reference to the {@link FileInfo} object.
     */
    public FileInfo getMessageInfo()
    {
        return messageFileInfo;
    }

    /**
     * Returns message flags.
     * 
     * @return {@link Flags}
     */
    @Override
    public Flags getFlags()
    {
        return imapService.getFlags(messageFileInfo);
    }

    
    /**
     * Sets message flags.
     * 
     * @param flags - {@link Flags} object.
     * @param value - flags value.
     */
    @Override
    public void setFlags(Flags flags, boolean value) throws MessagingException
    {
        imapService.setFlags(messageFileInfo, flags, value);
    }

    /**
     * Returns the text representing email body for ContentModel node.
     * 
     * @param type The type of the returned body. May be the one of {@link EmailBodyFormat}.
     * @return Text representing email body for ContentModel node.
     */
    public String getEmailBodyText(EmailBodyFormat type)
    {
        return serviceRegistry.getTemplateService().processTemplate(
                imapService.getDefaultEmailBodyTemplate(type),
                createEmailTemplateModel(messageFileInfo.getNodeRef()));
    }



    /**
     * Builds default email template model for TemplateProcessor
     * 
     * @param ref NodeRef of the target content.
     * @return Map that includes template model objects.
     */
    private Map<String, Object> createEmailTemplateModel(NodeRef ref)
    {
        Map<String, Object> model = new HashMap<String, Object>(8, 1.0f);
        TemplateNode tn = new TemplateNode(ref, serviceRegistry, null);
        model.put("document", tn);
        NodeRef parent = serviceRegistry.getNodeService().getPrimaryParent(ref).getParentRef();
        model.put("space", new TemplateNode(parent, serviceRegistry, null));
        model.put("date", new Date());
        model.put("contextUrl", new String(imapService.getWebApplicationContextUrl()));
        model.put("alfTicket", new String(serviceRegistry.getAuthenticationService().getCurrentTicket()));
        String contentFolderUrl = imapService.getContentFolderUrl(ref);
        model.put("shareContextUrl", new String(imapService.getShareApplicationContextUrl()));
        model.put("contentFolderUrl", contentFolderUrl);
        return model;
    }

    protected void updateMessageID() throws MessagingException
    {
        setHeader("Message-ID", "<" + this.messageFileInfo.getNodeRef().getId() + DEFAULT_SUFFIX + ">");
    }   
}
