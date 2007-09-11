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
package org.alfresco.email.server;

import java.util.Collection;
import java.util.Map;

import org.alfresco.email.server.handler.EmailMessageHandler;
import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.email.EmailMessage;
import org.alfresco.service.cmr.email.EmailMessageException;
import org.alfresco.service.cmr.email.EmailService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Concrete email service implementation.  This is responsible for routing the
 * emails into the server.
 * @since 2.2
 */
public class EmailServiceImpl implements EmailService
{
    private static final Log log = LogFactory.getLog(EmailServiceImpl.class);

    private NodeService nodeService;
    private SearchService searchService;
    private RetryingTransactionHelper retryingTransactionHelper;

    private boolean mailInboundEnabled;
    /** Login of user that is set as unknown. */
    private String unknownUser;
    /** List of message handlers */
    private Map<String, EmailMessageHandler> emailMessageHandlerMap;

    /**
     * @param nodeService Alfresco Node Service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param searchService Alfresco Search Service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * @param retryingTransactionHelper Alfresco RetryingTransactionHelper
     */
    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }
    
    /**
     * @return Map of message handlers
     */
    public Map<String, EmailMessageHandler> getEmailMessageHandlerMap()
    {
        return emailMessageHandlerMap;
    }

    /**
     * @param emailMessageHandlerMap Map of message handlers
     */
    public void setEmailMessageHandlerMap(Map<String, EmailMessageHandler> emailMessageHandlerMap)
    {
        this.emailMessageHandlerMap = emailMessageHandlerMap;
    }

    /**
     * @param unknownUser Login of user that should be set as unknown.
     */
    public void setUnknownUser(String unknownUser)
    {
        this.unknownUser = unknownUser;
    }

    public void setMailInboundEnabled(boolean mailInboundEnabled)
    {
        this.mailInboundEnabled = mailInboundEnabled;
    }
    
    /**
     * {@inheritDoc}
     */
    public void importMessage(EmailMessage message)
    {
        processMessage(null, message);
    }

    /**
     * {@inheritDoc}
     */
    public void importMessage(NodeRef nodeRef, EmailMessage message)
    {
        processMessage(nodeRef, message);
    }

    /**
     * Process the message. Method is called after filtering by sender's address.
     * 
     * @param nodeRef Addressed node (target node).
     * @param message Email message
     * @throws EmailMessageException Any exception occured inside the method will be converted and thrown as <code>EmailMessageException</code>
     */
    private void processMessage(final NodeRef nodeRef, final EmailMessage message)
    {
        if (!mailInboundEnabled) 
        {
            throw new EmailMessageException(I18NUtil.getMessage("email.server.mail-inbound-disabled"));
        }
        try
        {
            RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
            {
                public Object execute()
                {
                    final String userName = authenticate(message.getFrom());

                    AuthenticationUtil.runAs(new RunAsWork<Object>()
                    {
                        public Object doWork() throws Exception
                        {
                            String recepient = message.getTo();
                            NodeRef targetNodeRef = null;
                            if (nodeRef == null)
                            {
                                targetNodeRef = getTargetNode(recepient);
                            }
                            else
                            {
                                targetNodeRef = nodeRef;
                            }
                            EmailMessageHandler messageHandler = getMessageHandler(targetNodeRef);
                            messageHandler.processMessage(targetNodeRef, message);
                            return null;
                        }

                    }, userName);
                    return null;
                }
            };
            retryingTransactionHelper.doInTransaction(callback, false);
        }
        catch (Throwable e)
        {
            log.error("Error process email message", e);
            throw new EmailMessageException(e.getMessage());
        }
    }

    /**
     * @param nodeRef Target node
     * @return Handler that can process message addressed to specific node (target node).
     * @throws EmailMessageException Exception is thrown if <code>nodeRef</code> is <code>null</code> or suitable message handler isn't found.
     */
    private EmailMessageHandler getMessageHandler(NodeRef nodeRef)
    {
        if (nodeRef == null)
        {
            throw new EmailMessageException(I18NUtil.getMessage("email.server.incorrect-node-ref"));
        }
        String nodeTypeLocalName = nodeService.getType(nodeRef).getLocalName();
        EmailMessageHandler handler = emailMessageHandlerMap.get(nodeTypeLocalName);
        if (handler == null)
        {
            throw new EmailMessageException(I18NUtil.getMessage("email.server.handler-not-found"));
        }
        return handler;
    }

    /**
     * Method determines target node by recepient e-mail address.
     * 
     * @param recepient An e-mail address of a receipient
     * @return Referance to the target node.
     * @exception EmailMessageException Exception is thrown if the target node couldn't be determined by some reasons.
     */
    private NodeRef getTargetNode(String recepient)
    {
        if (recepient == null || recepient.length() == 0)
        {
            throw new EmailMessageException(I18NUtil.getMessage("email.server.incorrect-node-address"));
        }
        String[] parts = recepient.split("@");
        if (parts.length != 2)
        {
            throw new EmailMessageException(I18NUtil.getMessage("email.server.incorrect-node-address"));
        }

        // Ok, address looks well, let's try to find related alias
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        String query = String.format(AliasableAspect.SEARCH_TEMPLATE, parts[0]);
        ResultSet resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, query);

        // Sometimes result contains trash. For example if we look for node with alias='target' after searching,
        // we will get all nodes wich contain word 'target' in them alias property.
        for (int i = 0; i < resultSet.length(); i++)
        {
            NodeRef resRef = resultSet.getNodeRef(i);
            Object alias = nodeService.getProperty(resRef, EmailServerModel.PROP_ALIAS);
            if (parts[0].equals(alias))
            {
                return resRef;
            }
        }

        // Ok, alias wasn't found, let's try to interpret recepient address as 'node-bdid' value
        query = "@sys\\:node-dbid:" + parts[0];
        resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, query);
        if (resultSet.length() > 0)
        {
            return resultSet.getNodeRef(0);
        }
        throw new EmailMessageException(I18NUtil.getMessage("email.server.incorrect-node-address"));
    }

    /**
     * Authenticate in Alfresco repository by sender's e-mail address.
     * 
     * @param from Sender's email address
     * @return User name
     * @throws EmailMessageException Exception will be thrown if authentication is failed.
     */
    private String authenticate(String from)
    {
        String userName = null;
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        String query = "TYPE:cm\\:person +@cm\\:email:\"" + from + "\"";

        ResultSet resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, query);

        if (resultSet.length() == 0)
        {
            userName = unknownUser;
            if (userName == null || !isEmailContributeUser(userName))
            {
                throw new EmailMessageException(I18NUtil.getMessage("email.server.unknown-user"));
            }
        }
        else
        {
            NodeRef userNode = resultSet.getNodeRef(0);
            if (nodeService.exists(userNode))
            {
                userName = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(userNode, ContentModel.PROP_USERNAME));
                if (!isEmailContributeUser(userName))
                {
                    throw new EmailMessageException(I18NUtil.getMessage("email.server.not-contribute-user", userName));
                }
            }
            else
            {
                throw new EmailMessageException(I18NUtil.getMessage("email.server.contribute-group-not-exist"));
            }
        }
        return userName;
    }

    /**
     * Check that the user is the member in <b>EMAIL_CONTRIBUTORS</b> group
     * 
     * @param userName User name
     * @return True if the user is member of the group
     * @exception EmailMessageException Exception will be thrown if the <b>EMAIL_CONTRIBUTORS</b> group isn't found
     */
    private boolean isEmailContributeUser(String userName)
    {
        String searchQuery = "TYPE:\"{http://www.alfresco.org/model/user/1.0}authorityContainer\"  +@usr\\:authorityName:\"GROUP_EMAIL_CONTRIBUTORS\"";
        StoreRef storeRef = new StoreRef("user", "alfrescoUserStore");
        ResultSet resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, searchQuery);

        if (resultSet.length() == 0)
        {
            throw new EmailMessageException(I18NUtil.getMessage("email.server.contribute-group-not-exist"));
        }

        NodeRef groupNode = resultSet.getNodeRef(0);

        Collection<String> memberCollection = DefaultTypeConverter.INSTANCE.getCollection(String.class, nodeService.getProperty(groupNode, ContentModel.PROP_MEMBERS));

        if (memberCollection.contains(userName))
            return true;

        return false;
    }
}
