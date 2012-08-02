/*
 * Copyright (C) 2005-2012
 Alfresco Software Limited.
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
package org.alfresco.util.test.junitrules;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.rules.ExternalResource;
import org.springframework.context.ApplicationContext;

/**
 * A JUnit rule designed to help with the automatic cleanup of temporary test nodes.
 * 
 * @author Neil Mc Erlean
 * @since 4.1
 */
public class TemporaryNodes extends ExternalResource
{
    private static final Log log = LogFactory.getLog(TemporaryNodes.class);
    
    private final ApplicationContextInit appContextRule;
    private List<NodeRef> temporaryNodeRefs = new ArrayList<NodeRef>();
    
    /**
     * Constructs the rule with a reference to a {@link ApplicationContextInit rule} which can be used to retrieve the ApplicationContext.
     * 
     * @param appContextRule a rule which can be used to retrieve the spring app context.
     */
    public TemporaryNodes(ApplicationContextInit appContextRule)
    {
        this.appContextRule = appContextRule;
    }
    
    
    @Override protected void before() throws Throwable
    {
        // Intentionally empty
    }
    
    @Override protected void after()
    {
        final ApplicationContext springContext = appContextRule.getApplicationContext();
        
        final RetryingTransactionHelper transactionHelper = springContext.getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
        final CheckOutCheckInService cociService          = springContext.getBean("CheckOutCheckInService", CheckOutCheckInService.class);
        final NodeService nodeService                     = springContext.getBean("NodeService", NodeService.class);
        
        // Run as admin to ensure all non-system nodes can be deleted irrespecive of which user created them.
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override public Void doWork() throws Exception
            {
                transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
                {
                    @Override public Void execute() throws Throwable
                    {
                        // Although we loop through all nodes, this is a cascade-delete and so we may only need to delete the first node.
                        for (NodeRef node : temporaryNodeRefs)
                        {
                            // If it's already been deleted, don't worry about it.
                            if (nodeService.exists(node))
                            {
                                // If it has been checked out, cancel the checkout before deletion.
                                if (cociService.isCheckedOut(node))
                                {
                                    log.debug("Cancelling checkout of temporary node " + nodeService.getProperty(node, ContentModel.PROP_NAME));
                                    NodeRef workingCopy = cociService.getWorkingCopy(node);
                                    cociService.cancelCheckout(workingCopy);
                                }
                                log.debug("Deleting temporary node " + nodeService.getProperty(node, ContentModel.PROP_NAME));
                                nodeService.deleteNode(node);
                            }
                        }
                        
                        return null;
                    }
                });
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    /**
     * Add a specified NodeRef to the list of NodeRefs to be deleted by this rule.
     * 
     * @param temporaryNodeRef a NodeRef
     */
    public void addNodeRef(NodeRef temporaryNodeRef)
    {
        this.temporaryNodeRefs.add(temporaryNodeRef);
    }
    
    /**
     * This method creates a NodeRef and adds it to the internal list of NodeRefs to be tidied up by the rule.
     * This method will be run in its own transaction and will be run with the specified user as the fully authenticated user,
     * thus ensuring the named user is the cm:creator of the new node.
     * 
     * @param parentNode the parent node
     * @param nodeCmName the cm:name of the new node
     * @param nodeType   the type of the new node
     * @param nodeCreator the username of the person who will create the node
     * @return the newly created NodeRef.
     */
    public NodeRef createNode(final NodeRef parentNode, final String nodeCmName, final QName nodeType, final String nodeCreator)
    {
        return this.createNodeWithTextContent(parentNode, nodeCmName, nodeType, nodeCreator, null);
    }
    
    /**
     * This method creates a NodeRef with some text/plain, UTF-8 content and adds it to the internal list of NodeRefs to be tidied up by the rule.
     * This method will be run in its own transaction and will be run with the specified user as the fully authenticated user,
     * thus ensuring the named user is the cm:creator of the new node.
     * 
     * @param parentNode the parent node
     * @param nodeCmName the cm:name of the new node
     * @param nodeType   the type of the new node
     * @param nodeCreator the username of the person who will create the node
     * @param textContent the text/plain, UTF-8 content that will be stored in the node's content. <code>null</code> content will not be written.
     * @return the newly created NodeRef.
     */
    public NodeRef createNodeWithTextContent(final NodeRef parentNode, final String nodeCmName, final QName nodeType, final String nodeCreator, final String textContent)
    {
        QName childName = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, nodeCmName);
        return createNodeWithTextContent(parentNode, childName, nodeCmName, nodeType, nodeCreator, textContent);
    }
    
    /**
     * This method creates a NodeRef with some text/plain, UTF-8 content and adds it to the internal list of NodeRefs to be tidied up by the rule.
     * This method will be run in its own transaction and will be run with the specified user as the fully authenticated user,
     * thus ensuring the named user is the cm:creator of the new node.
     * 
     * @param parentNode the parent node
     * @param nodeCmName the cm:name of the new node
     * @param nodeType   the type of the new node
     * @param nodeCreator the username of the person who will create the node
     * @param textContent the text/plain, UTF-8 content that will be stored in the node's content. <code>null</code> content will not be written.
     * @return the newly created NodeRef.
     */
    public NodeRef createNodeWithTextContent(final NodeRef parentNode, final QName childName, final String nodeCmName, final QName nodeType, final String nodeCreator, final String textContent)
    {
        final RetryingTransactionHelper transactionHelper = (RetryingTransactionHelper) appContextRule.getApplicationContext().getBean("retryingTransactionHelper");
        
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(nodeCreator);
        
        NodeRef newNodeRef = transactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                final NodeService nodeService = (NodeService) appContextRule.getApplicationContext().getBean("nodeService");
                
                Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                props.put(ContentModel.PROP_NAME, nodeCmName);
                ChildAssociationRef childAssoc = nodeService.createNode(parentNode,
                            ContentModel.ASSOC_CONTAINS,
                            childName,
                            nodeType,
                            props);
                
                // If there is any content, add it.
                if (textContent != null)
                {
                    ContentService contentService = appContextRule.getApplicationContext().getBean("contentService", ContentService.class);
                    ContentWriter writer = contentService.getWriter(childAssoc.getChildRef(), ContentModel.PROP_CONTENT, true);
                    writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                    writer.setEncoding("UTF-8");
                    writer.putContent(textContent);
                }
                return childAssoc.getChildRef();
            }
        });
        
        AuthenticationUtil.popAuthentication();
        
        this.temporaryNodeRefs.add(newNodeRef);
        return newNodeRef;
    }
}
