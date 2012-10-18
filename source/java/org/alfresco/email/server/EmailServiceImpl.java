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
package org.alfresco.email.server;

import java.util.Collection;
import java.util.Map;

import javax.mail.internet.InternetAddress;

import org.alfresco.email.server.handler.EmailMessageHandler;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.email.EmailDelivery;
import org.alfresco.service.cmr.email.EmailMessage;
import org.alfresco.service.cmr.email.EmailMessageException;
import org.alfresco.service.cmr.email.EmailService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.ParameterCheck;

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
    private static final String ERR_INVALID_NODE_ADDRESS = "email.server.err.invalid_node_address";
    private static final String ERR_HANDLER_NOT_FOUND = "email.server.err.handler_not_found";
    
    private NamespaceService namespaceService;
    private NodeService nodeService;
    private SearchService searchService;
    private RetryingTransactionHelper retryingTransactionHelper;
    private AuthorityService authorityService;
    private DictionaryService dictionaryService;
    private AttributeService attributeService;
    
    /**
     * The authority that needs to contain the users and groups 
     * who are allowed to contribute email.
     */
    private String emailContributorsAuthority="EMAIL_CONTRIBUTORS";
    
    private static Log logger = LogFactory.getLog(EmailServiceImpl.class);

    private boolean emailInboundEnabled;
    /** Login of user that is set as unknown. */
    private String unknownUser;
    /** List of message handlers */
    private Map<String, EmailMessageHandler> emailMessageHandlerMap;
    
    public void init()
    {
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);
        PropertyCheck.mandatory(this, "dictionaryService", getDictionaryService());
        PropertyCheck.mandatory(this, "searchService", searchService);
        PropertyCheck.mandatory(this, "authorityService", authorityService);
        PropertyCheck.mandatory(this, "emailMessageHandlerMap", emailMessageHandlerMap);
        PropertyCheck.mandatory(this, "attributeService", getAttributeService());
    }

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
     * @param authorityService Alfresco authority service
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
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
    public void importMessage(EmailDelivery delivery, EmailMessage message)
    {
        processMessage(delivery, null, message);
    }

    /**
     * {@inheritDoc}
     */
    public void importMessage(EmailDelivery delivery, NodeRef nodeRef, EmailMessage message)
    {
        processMessage(delivery, nodeRef, message);
    }

    /**
     * Process the message. Method is called after filtering by sender's address.
     * @param delivery - who gets the message and who is it from (may be different from the contents of the message)
     * @param nodeRef Addressed node (target node).
     * @param message Email message
     * @throws EmailMessageException Any exception occured inside the method will be converted and thrown as <code>EmailMessageException</code>
     */
    private void processMessage(final EmailDelivery delivery, final NodeRef nodeRef, final EmailMessage message)
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
                    String userName = null;
                                        
                    userName = getUsername(delivery.getFrom());   
                    if(userName == null)
                    {
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("unable to find user for from: " + delivery.getFrom() + ",trying message.from next");
                        }
                        userName = getUsername(message.getFrom());
                    }
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("userName = : " + userName);
                    }

                    if (userName == null)
                    {
                        if(unknownUser.isEmpty())
                        {
                            if(logger.isDebugEnabled())
                            {
                                logger.debug("unable to find user for from: " + message.getFrom());
                            }
                            throw new EmailMessageException(ERR_UNKNOWN_SOURCE_ADDRESS, message.getFrom());
                        }
                        else
                        {
                            if(logger.isDebugEnabled())
                            {
                                logger.debug("unable to find user for from - return anonymous: ");
                            }
                            userName = unknownUser;
                        }
                    }

                    // Ensure that the user is part of the Email Contributors group
                    if (userName == null || !isEmailContributeUser(userName))
                    {
                        throw new EmailMessageException(ERR_USER_NOT_EMAIL_CONTRIBUTOR, userName);
                    }
                      
                    return userName;
                }
            };
            RunAsWork<String> getUsernameRunAsWork = new RunAsWork<String>()
            {
                public String doWork() throws Exception
                {
                    return retryingTransactionHelper.doInTransaction(getUsernameCallback, false);
                }
            };
            
             
            String username;
            if(delivery.getAuth() != null)
            {
                // The user has authenticated.
                username = delivery.getAuth();
                logger.debug("user has already authenticated as:" + username);
            }
            else
            {
                // Need to faff with old message stuff.
                username = AuthenticationUtil.runAs(getUsernameRunAsWork, AuthenticationUtil.SYSTEM_USER_NAME);
            }
            
            // Process the message using the username's account
            final RetryingTransactionCallback<Object> processMessageCallback = new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Throwable
                {
                    //String recipient = message.getTo();
                    String recipient = delivery.getRecipient();
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
            throw new EmailMessageException(ERR_ACCESS_DENIED, delivery.getFrom(), delivery.getRecipient());
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
        
        if( handler == null)
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("did not find a handler for type:" + prefixedNodeTypeStr);
            }
            
            // not a direct match on type
            // need to check the super-types (if any) of the target node
            TypeDefinition typeDef = dictionaryService.getType(nodeTypeQName);
            while(typeDef != null)
            {
                QName parentName = typeDef.getParentName();
                if(parentName != null)
                {
                    String prefixedSubTypeStr = parentName.toPrefixString(namespaceService);
                    handler = emailMessageHandlerMap.get(prefixedSubTypeStr);
                    if(handler != null)
                    {
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("found a handler for a subtype:" + prefixedSubTypeStr);
                        }
                        return handler;
                    }
               } 
               typeDef = dictionaryService.getType(parentName); 
            }
             
        }
        
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
        if (logger.isDebugEnabled())
        {
            logger.debug("getTarget node for" + recipient);
        }
        if (recipient == null || recipient.length() == 0)
        {
            throw new EmailMessageException(ERR_INVALID_NODE_ADDRESS, recipient);
        }
        String[] parts = recipient.split("@");
        if (parts.length != 2)
        {
            throw new EmailMessageException(ERR_INVALID_NODE_ADDRESS, recipient);
        }
        
        String alias = parts[0];
        
        /*
         * First lookup via the attributes service
         * 
         * Then lookup by search service - may be old data prior to attributes service
         * 
         * Then see if we can find a node by dbid
         */
        
        // Lookup via the attributes service
        NodeRef ref = (NodeRef)getAttributeService().getAttribute(AliasableAspect.ALIASABLE_ATTRIBUTE_KEY_1, AliasableAspect.ALIASABLE_ATTRIBUTE_KEY_2, AliasableAspect.normaliseAlias(alias));
        
        if(ref != null)
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("found email alias via attribute service alias =" + alias);
            }
            return ref;
        }

        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        
//        // Ok, alias wasn't found, let's try to interpret recipient address as 'node-bdid' value
//        try 
//        {
//            Long nodeId = Long.parseLong(parts[0]);
//       
//            NodeRef byNodeId = nodeService.getNodeRef(nodeId);
//            
//            if(byNodeId != null)
//            {
//                if(logger.isDebugEnabled())
//                {
//                    logger.debug("found email alias via node service =" + alias);
//                }
//                return byNodeId;
//            }
//        }
//        catch (NumberFormatException ne)
//        {
//        }

        // Ok, alias wasn't found, let's try to interpret recipient address as 'node-bdid' value
        ResultSet resultSet = null;
        try
        {
            String query = "@sys\\:node-dbid:" + parts[0];
            resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, query);
            if (resultSet.length() > 0)
            {
                return resultSet.getNodeRef(0);
            }
        }
        finally
        {
            if(resultSet != null)
            {
                resultSet.close();
            }
        }
        throw new EmailMessageException(ERR_INVALID_NODE_ADDRESS, recipient);
    }

    /**
     * Authenticate in Alfresco repository by sender's e-mail address.
     * 
     * @param from Sender's email address
     * @return User name or null if the user does not exist.
     * @throws EmailMessageException Exception will be thrown if authentication is failed.
     */
    private String getUsername(String from)
    {
        String userName = null;
        
        if(from == null || from.length()==0)
        {
            return null;
        }
        
        if(logger.isDebugEnabled())
        {
            logger.debug("getUsername from: " + from);
        }
        
        
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        String query = "TYPE:cm\\:person +@cm\\:email:\"" + from + "\"";

        ResultSet resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, query);
        try
        {
            if (resultSet.length() == 0)
            {
                return null;
            }
            else
            {
                NodeRef userNode = resultSet.getNodeRef(0);
                if (nodeService.exists(userNode))
                {
                    userName = DefaultTypeConverter.INSTANCE.convert(
                            String.class,
                            nodeService.getProperty(userNode, ContentModel.PROP_USERNAME));
                    
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("found username: " + userName);
                    }
                }
                else
                {
                    // The Lucene index returned a dead result
                    throw new EmailMessageException(ERR_UNKNOWN_SOURCE_ADDRESS, from);
                }
            }
        }
        finally
        {
            if(resultSet != null)
            {
                resultSet.close();
            }
        }

        return userName;
    }
    
    /**
     * Check that the user is the member in <b>EMAIL_CONTRIBUTORS</b> group
     * 
     * @param userName Alfresco user name
     * @return True if the user is member of the group
     * @exception EmailMessageException Exception will be thrown if the <b>EMAIL_CONTRIBUTORS</b> group isn't found
     */
    private boolean isEmailContributeUser(String userName)
    {
        return this.authorityService.getAuthoritiesForUser(userName).contains(
                authorityService.getName(AuthorityType.GROUP, getEmailContributorsAuthority()));
    }

    public void setEmailContributorsAuthority(
            String emailContributorsAuthority)
    {
        this.emailContributorsAuthority = emailContributorsAuthority;
    }

    public String getEmailContributorsAuthority()
    {
        return emailContributorsAuthority;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public DictionaryService getDictionaryService()
    {
        return dictionaryService;
    }

    public void setAttributeService(AttributeService attributeService)
    {
        this.attributeService = attributeService;
    }

    public AttributeService getAttributeService()
    {
        return attributeService;
    }
}
