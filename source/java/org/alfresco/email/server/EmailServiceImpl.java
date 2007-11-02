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
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
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
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;

/**
 * Concrete email service implementation.  This is responsible for routing the
 * emails into the server.
 * 
 * @since 2.2
 */
public class EmailServiceImpl implements EmailService
{
    private static final String ERR_INBOUND_EMAIL_DISABLED = "email.server.err.inbound_mail_disabled";
    private static final String ERR_INVALID_SUBJECT = "email.server.err.invalid_subject";
    private static final String ERR_ACCESS_DENIED = "email.server.err.access_denied";
    private static final String ERR_UNKNOWN_SOURCE_ADDRESS = "email.server.err.unknown_source_address";
    private static final String ERR_USER_NOT_EMAIL_CONTRIBUTOR = "email.server.err.user_not_email_contributor";
    private static final String ERR_NO_EMAIL_CONTRIBUTOR_GROUP = "email.server.err.no_email_contributor_group";
    private static final String ERR_INVALID_NODE_ADDRESS = "email.server.err.invalid_node_address";
    private static final String ERR_HANDLER_NOT_FOUND = "email.server.err.handler_not_found";
    
    private NamespaceService namespaceService;
    private NodeService nodeService;
    private SearchService searchService;
    private RetryingTransactionHelper retryingTransactionHelper;

    private boolean emailInboundEnabled;
    /** Login of user that is set as unknown. */
    private String unknownUser;
    /** List of message handlers */
    private Map<String, EmailMessageHandler> emailMessageHandlerMap;

    /**
     * 
     * @param namespaceService      the service to resolve namespace prefixes
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

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

    public void setEmailInboundEnabled(boolean mailInboundEnabled)
    {
        this.emailInboundEnabled = mailInboundEnabled;
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
        if (!emailInboundEnabled) 
        {
            throw new EmailMessageException(ERR_INBOUND_EMAIL_DISABLED);
        }
        try
        {
            // Get the username for the process using the system account
            final RetryingTransactionCallback<String> getUsernameCallback = new RetryingTransactionCallback<String>()
            {
                public String execute() throws Throwable
                {
                    String from = message.getFrom();
                    return getUsername(from);
                }
            };
            RunAsWork<String> getUsernameRunAsWork = new RunAsWork<String>()
            {
                public String doWork() throws Exception
                {
                    return retryingTransactionHelper.doInTransaction(getUsernameCallback, false);
                }
            };
            String username = AuthenticationUtil.runAs(getUsernameRunAsWork, AuthenticationUtil.SYSTEM_USER_NAME);
            
            // Process the message using the username's account
            final RetryingTransactionCallback<Object> processMessageCallback = new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Throwable
                {
                    String recipient = message.getTo();
                    NodeRef targetNodeRef = null;
                    if (nodeRef == null)
                    {
                        targetNodeRef = getTargetNode(recipient);
                    }
                    else
                    {
                        targetNodeRef = nodeRef;
                    }
                    EmailMessageHandler messageHandler = getMessageHandler(targetNodeRef);
                    messageHandler.processMessage(targetNodeRef, message);
                    return null;
                }
            };
            RunAsWork<Object> processMessageRunAsWork = new RunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    return retryingTransactionHelper.doInTransaction(processMessageCallback, false);
                }
            };
            AuthenticationUtil.runAs(processMessageRunAsWork, username);
        }
        catch (EmailMessageException e)
        {
            // These are email-specific errors
            throw e;
        }
        catch (AccessDeniedException e)
        {
            throw new EmailMessageException(ERR_ACCESS_DENIED, message.getFrom(), message.getTo());
        }
        catch (IntegrityException e)
        {
            throw new EmailMessageException(ERR_INVALID_SUBJECT);
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Email message processing failed", e);
        }
    }

    /**
     * @param nodeRef           Target node
     * @return                  Handler that can process message addressed to specific node (target node).
     * @throws                  EmailMessageException is thrown if a suitable message handler isn't found.
     */
    private EmailMessageHandler getMessageHandler(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        
        QName nodeTypeQName = nodeService.getType(nodeRef);
        String prefixedNodeTypeStr = nodeTypeQName.toPrefixString(namespaceService);
        EmailMessageHandler handler = emailMessageHandlerMap.get(prefixedNodeTypeStr);
        if (handler == null)
        {
            throw new EmailMessageException(ERR_HANDLER_NOT_FOUND, prefixedNodeTypeStr);
        }
        return handler;
    }

    /**
     * Method determines target node by recipient e-mail address.
     * 
     * @param recipient         An e-mail address of a recipient
     * @return                  Reference to the target node
     * @throws                  EmailMessageException is thrown if the target node couldn't be determined by some reasons.
     */
    private NodeRef getTargetNode(String recipient)
    {
        if (recipient == null || recipient.length() == 0)
        {
            throw new EmailMessageException(ERR_INVALID_NODE_ADDRESS, recipient);
        }
        String[] parts = recipient.split("@");
        if (parts.length != 2)
        {
            throw new EmailMessageException(ERR_INVALID_NODE_ADDRESS, recipient);
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

        // Ok, alias wasn't found, let's try to interpret recipient address as 'node-bdid' value
        query = "@sys\\:node-dbid:" + parts[0];
        resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, query);
        if (resultSet.length() > 0)
        {
            return resultSet.getNodeRef(0);
        }
        throw new EmailMessageException(ERR_INVALID_NODE_ADDRESS, recipient);
    }

    /**
     * Authenticate in Alfresco repository by sender's e-mail address.
     * 
     * @param from Sender's email address
     * @return User name
     * @throws EmailMessageException Exception will be thrown if authentication is failed.
     */
    private String getUsername(String from)
    {
        String userName = null;
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        String query = "TYPE:cm\\:person +@cm\\:email:\"" + from + "\"";

        ResultSet resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, query);

        if (resultSet.length() == 0)
        {
            if (unknownUser == null || unknownUser.length() == 0)
            {
                throw new EmailMessageException(ERR_UNKNOWN_SOURCE_ADDRESS, from);
            }
            else
            {
                userName = unknownUser;
            }
        }
        else
        {
            NodeRef userNode = resultSet.getNodeRef(0);
            if (nodeService.exists(userNode))
            {
                userName = DefaultTypeConverter.INSTANCE.convert(
                        String.class,
                        nodeService.getProperty(userNode, ContentModel.PROP_USERNAME));
            }
            else
            {
                // The Lucene index returned a dead result
                throw new EmailMessageException(ERR_UNKNOWN_SOURCE_ADDRESS, from);
            }
        }
        // Ensure that the user is part of the Email Contributors group
        if (userName == null || !isEmailContributeUser(userName))
        {
            throw new EmailMessageException(ERR_USER_NOT_EMAIL_CONTRIBUTOR, userName);
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
            throw new EmailMessageException(ERR_NO_EMAIL_CONTRIBUTOR_GROUP);
        }

        NodeRef groupNode = resultSet.getNodeRef(0);

        Collection<String> memberCollection = DefaultTypeConverter.INSTANCE.getCollection(
                String.class,
                nodeService.getProperty(groupNode, ContentModel.PROP_MEMBERS));

        if (memberCollection.contains(userName))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
