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
package org.alfresco.repo.imap;

import static org.alfresco.repo.imap.AlfrescoImapConst.MIME_VERSION;
import static org.alfresco.repo.imap.AlfrescoImapConst.X_ALF_NODEREF_ID;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.imap.ImapService.EmailBodyFormat;
import org.alfresco.repo.site.SiteModel;
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
        this.isMessageInSitesLibrary = imapService.isNodeInSitesLibrary(messageFileInfo.getNodeRef());
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
     * @param The parameter instance to check.
     * @param The name of the parameter.
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
     * @param nodeRef NodeRef of the target content.
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
        if (isMessageInSitesLibrary)
        {
            String pathFromSites = getPathFromSites(parent);
            StringBuilder parsedPath = new StringBuilder();
            String[] pathParts = pathFromSites.split("/");
            if (pathParts.length > 2)
            {
                parsedPath.append(pathParts[0]).append("/").append(pathParts[1]);
                parsedPath.append("?filter=path|");
                for (int i = 2; i < pathParts.length; i++)
                {
                    parsedPath.append("/").append(pathParts[i]);
                }

            }
            else
            {
                parsedPath.append(pathFromSites);
            }
            model.put("shareContextUrl", new String(imapService.getShareApplicationContextUrl()));
            model.put("parentPathFromSites", parsedPath.toString());
        }
        return model;
    }

    private String getPathFromSites(NodeRef ref)
    {
        NodeService nodeService = serviceRegistry.getNodeService();
        String name = ((String) nodeService.getProperty(ref, ContentModel.PROP_NAME)).toLowerCase();
        if (nodeService.getType(ref).equals(SiteModel.TYPE_SITE))
        {
            return name;
        }
        else
        {
            NodeRef parent = nodeService.getPrimaryParent(ref).getParentRef();
            return getPathFromSites(parent) + "/" + name;
        }
    }

    protected void updateMessageID() throws MessagingException
    {
        setHeader("Message-ID", "<" + this.messageFileInfo.getNodeRef().getId() + DEFAULT_SUFFIX + ">");
    }   
}
