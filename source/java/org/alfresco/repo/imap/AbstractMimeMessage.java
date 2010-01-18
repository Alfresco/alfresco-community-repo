/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.imap;

import static org.alfresco.repo.imap.AlfrescoImapConst.CLASSPATH_TEXT_HTML_TEMPLATE;
import static org.alfresco.repo.imap.AlfrescoImapConst.CLASSPATH_TEXT_PLAIN_TEMPLATE;
import static org.alfresco.repo.imap.AlfrescoImapConst.DICTIONARY_TEMPLATE_PREFIX;
import static org.alfresco.repo.imap.AlfrescoImapConst.MIME_VERSION;
import static org.alfresco.repo.imap.AlfrescoImapConst.X_ALF_NODEREF_ID;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Arseny Kovalchuk
 */
public abstract class AbstractMimeMessage extends MimeMessage
{
    /** Used if imapHelper.getDefaultFromAddress is not set */
    protected static final String DEFAULT_EMAIL_FROM = "alfresco@alfresco.org";
    protected static final String DEFAULT_EMAIL_TO = DEFAULT_EMAIL_FROM;
    
    protected static int MAX_RETRIES = 1;

    private Log logger = LogFactory.getLog(AbstractMimeMessage.class);
    
    protected boolean generateBody = true;
    
    protected ServiceRegistry serviceRegistry;
    protected ImapService imapService;
    protected FileInfo messageFileInfo;
    protected MimeMessage wrappedMessage;

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

    public static enum EmailBodyType
    {
        TEXT_PLAIN, TEXT_HTML;

        public String getSubtype()
        {
            return name().toLowerCase().substring(5);
        }

        public String getTypeSubtype()
        {
            return name().toLowerCase().replaceAll("_", "");
        }

        public String getMimeType()
        {
            return name().toLowerCase().replaceAll("_", "/");
        }

    }

    protected void buildMessage(FileInfo fileInfo, ServiceRegistry serviceRegistry) throws MessagingException
    {
        checkParameter(serviceRegistry, "ServiceRegistry");
        this.content = null;
        this.serviceRegistry = serviceRegistry;
        this.imapService = serviceRegistry.getImapService();
        this.messageFileInfo = fileInfo;
        RetryingTransactionHelper txHelper = serviceRegistry.getTransactionService().getRetryingTransactionHelper();
        txHelper.setMaxRetries(MAX_RETRIES);
        txHelper.doInTransaction(new RetryingTransactionCallback<Object>() {
            public Object execute() throws Throwable
            {
                buildMessageInternal();
                return null;
            }
        });
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
     * Builds the InternetAddress from the Content Author name if provided. If name not specified, it takes Content Creator name. If content creator does not exists, the default
     * from address will be returned.
     * 
     * @param contentAuthor The content author full name.
     * @return Generated InternetAddress[] array.
     * @throws AddressException
     */
    protected InternetAddress[] buildSenderFromAddress() throws AddressException
    {
        // Generate FROM address (Content author)
        InternetAddress[] addressList = null;
        Map<QName, Serializable> properties = messageFileInfo.getProperties();
        String prop = (String) properties.get(ContentModel.PROP_AUTHOR);
        String defaultFromAddress = imapService.getDefaultFromAddress();
        defaultFromAddress = defaultFromAddress == null ? DEFAULT_EMAIL_FROM : defaultFromAddress;
        try
        {

            if (prop != null)
            {
                StringBuilder contentAuthor = new StringBuilder();
                contentAuthor.append("\"").append(prop).append("\" <").append(defaultFromAddress).append(">");
                addressList = InternetAddress.parse(contentAuthor.toString());
            }
            else
            {
                prop = (String) properties.get(ContentModel.PROP_CREATOR);
                if (prop != null)
                {
                    StringBuilder creator = new StringBuilder();
                    creator.append("\"").append(prop).append("\" <").append(defaultFromAddress).append(">");
                    addressList = InternetAddress.parse(creator.toString());
                }
                else
                {
                    throw new AddressException(I18NUtil.getMessage("imap.server.error.properties_dont_exist"));
                }
            }
        }
        catch (AddressException e)
        {
            addressList = InternetAddress.parse(DEFAULT_EMAIL_FROM);
        }
        return addressList;
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
     * @param type The type of the returned body. May be the one of {@link EmailBodyType}.
     * @return Text representing email body for ContentModel node.
     */
    public String getEmailBodyText(EmailBodyType type)
    {
        return serviceRegistry.getTemplateService().processTemplate(
                getDefaultEmailBodyTemplate(type),
                createEmailTemplateModel(messageFileInfo.getNodeRef()));
    }

    /**
     * TODO USE CASE 2: "The To/addressee will be the first email alias found in the parent folders or a default one (TBD)".
     * It seems to be more informative as alike {@code <user>@<current.domain>}...
     * 
     * @return Generated TO address {@code <user>@<current.domain>}
     * @throws AddressException
     */
    protected InternetAddress[] buildRecipientToAddress() throws AddressException
    {
        InternetAddress[] result = null;
        String defaultEmailTo = null;
        final String escapedUserName = AuthenticationUtil.getFullyAuthenticatedUser().replaceAll("[/,\\,@]", ".");
        final String userDomain = DEFAULT_EMAIL_TO.split("@")[1];
        defaultEmailTo = escapedUserName + "@" + userDomain;
        try
        {
            result = InternetAddress.parse(defaultEmailTo);
        }
        catch (AddressException e)
        {
            logger.error(String.format("Wrong email address '%s'.", defaultEmailTo), e);
            result = InternetAddress.parse(DEFAULT_EMAIL_TO);
        }
        return result;
    }

    protected void addFromInternal(String addressesString) throws MessagingException
    {
        if (addressesString != null)
        {
            addFrom(InternetAddress.parse(addressesString));
        }
        else
        {
            addFrom(new Address[] { new InternetAddress(DEFAULT_EMAIL_FROM) });
        }
    }

    /**
     * Returns default email body template. This method trying to find a template on the path in the repository first
     * e.g. {@code "Data Dictionary > IMAP Templates >"}. This path should be set as the property of the "imapHelper" bean.
     * In this case it returns {@code NodeRef.toString()} of the template. If there are no template in the repository it
     * returns a default template on the classpath.
     * 
     * @param type One of the {@link EmailBodyType}.
     * @return String representing template classpath path or NodeRef.toString().
     */
    private String getDefaultEmailBodyTemplate(EmailBodyType type)
    {
        String result = null;
        switch (type)
        {
        case TEXT_HTML:
            result = CLASSPATH_TEXT_HTML_TEMPLATE;
            break;
        case TEXT_PLAIN:
            result = CLASSPATH_TEXT_PLAIN_TEMPLATE;
            break;
        }
        final StringBuilder templateName = new StringBuilder(DICTIONARY_TEMPLATE_PREFIX).append("-").append(type.getTypeSubtype()).append(".ftl");
        final String repositoryTemplatePath = imapService.getRepositoryTemplatePath();
        int indexOfStoreDelim = repositoryTemplatePath.indexOf(StoreRef.URI_FILLER);
        if (indexOfStoreDelim == -1)
        {
            logger.error("Bad path format, " + StoreRef.URI_FILLER + " not found");
            return result;
        }
        indexOfStoreDelim += StoreRef.URI_FILLER.length();
        int indexOfPathDelim = repositoryTemplatePath.indexOf("/", indexOfStoreDelim);
        if (indexOfPathDelim == -1)
        {
            logger.error("Bad path format, / not found");
            return result;
        }
        final String storePath = repositoryTemplatePath.substring(0, indexOfPathDelim);
        final String rootPathInStore = repositoryTemplatePath.substring(indexOfPathDelim);
        final String query = String.format("+PATH:\"%1$s/*\" +@cm\\:name:\"%2$s\"", rootPathInStore, templateName.toString());
        if (logger.isDebugEnabled())
        {
            logger.debug("Using template path :" + repositoryTemplatePath + "/" + templateName);
            logger.debug("Query: " + query);
        }
        StoreRef storeRef = new StoreRef(storePath);
        ResultSet resultSet = serviceRegistry.getSearchService().query(storeRef, "lucene", query);
        if (resultSet == null || resultSet.length() == 0)
        {
            logger.error(String.format("IMAP message template '%1$s' does not exist in the path '%2$s'.", templateName, repositoryTemplatePath));
            return result;
        }
        result = resultSet.getNodeRef(0).toString();
        resultSet.close();
        return result;
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
        return model;
    }


    
}
