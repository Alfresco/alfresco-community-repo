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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.rules.ExternalResource;
import org.springframework.context.ApplicationContext;

/**
 * A JUnit rule designed to help with the automatic cleanup of temporary test nodes and to ake it easier to
 * create common test content with JUnit code.
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
        final DictionaryService dictionaryService         = springContext.getBean("DictionaryService", DictionaryService.class);
        final NodeService nodeService                     = springContext.getBean("NodeService", NodeService.class);
        final SiteService siteService                     = springContext.getBean("SiteService", SiteService.class);
        final VersionService versionService               = springContext.getBean("VersionService", VersionService.class);
        
        // Run as system to ensure all non-system nodes can be deleted irrespective of which user created them.
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
                                
                                // If it's been versioned, then we need to clean out the version history too.
                                if (versionService.isVersioned(node))
                                {
                                    log.debug("Deleting version history of temporary node " + nodeService.getProperty(node, ContentModel.PROP_NAME));
                                    versionService.deleteVersionHistory(node);
                                }
                                
                                log.debug("Deleting temporary node " + nodeService.getProperty(node, ContentModel.PROP_NAME));
                                
                                // Site nodes are a special case which must be deleted through the SiteService.
                                final QName nodeType = nodeService.getType(node);
                                if (nodeType.equals(SiteModel.TYPE_SITE) || dictionaryService.isSubClass(nodeType, SiteModel.TYPE_SITE))
                                {
                                    SiteInfo siteInfo = siteService.getSite(node);
                                    siteService.deleteSite(siteInfo.getShortName());
                                }
                                else
                                {
                                    nodeService.deleteNode(node);
                                }
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
    
    /**
     * This method creates a cm:folder NodeRef and adds it to the internal list of NodeRefs to be tidied up by the rule.
     * This method will be run in its own transaction and will be run with the specified user as the fully authenticated user,
     * thus ensuring the named user is the cm:creator of the new node.
     * 
     * @param parentNode the parent node
     * @param nodeCmName the cm:name of the new node
     * @param nodeCreator the username of the person who will create the node
     * @return the newly created NodeRef.
     */
    public NodeRef createFolder(final NodeRef parentNode, final String nodeCmName, final String nodeCreator)
    {
        final RetryingTransactionHelper transactionHelper = (RetryingTransactionHelper) appContextRule.getApplicationContext().getBean("retryingTransactionHelper");
        
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(nodeCreator);
        
        NodeRef newNodeRef = transactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                final NodeRef result = createNode(nodeCmName, parentNode, ContentModel.TYPE_FOLDER);
                
                return result;
            }
        });
        
        AuthenticationUtil.popAuthentication();
        
        this.temporaryNodeRefs.add(newNodeRef);
        return newNodeRef;
    }
    
    /**
     * This method creates a cm:content NodeRef whose content is taken from an Alfresco 'quick' file and adds it to the internal
     * list of NodeRefs to be tidied up by the rule.
     * This method will be run in its own transaction and will be run with the specified user as the fully authenticated user,
     * thus ensuring the named user is the cm:creator of the new node.
     * 
     * @param mimetype the MimeType of the content to put in the new node.
     * @param parentNode the parent node
     * @param nodeCmName the cm:name of the new node
     * @param nodeCreator the username of the person who will create the node
     * @return the newly created NodeRef.
     * @since 4.1.7
     */
    public NodeRef createQuickFile(final String mimetype, final NodeRef parentNode, final String nodeCmName, final String nodeCreator)
    {
        return createQuickFile(mimetype, parentNode, nodeCmName, nodeCreator, false);
    }
    
    /**
     * This method creates a cm:content NodeRef whose content is taken from an Alfresco 'quick' file and adds it to the internal
     * list of NodeRefs to be tidied up by the rule.
     * This method will be run in its own transaction and will be run with the specified user as the fully authenticated user,
     * thus ensuring the named user is the cm:creator of the new node.
     * 
     * @param mimetype the MimeType of the content to put in the new node.
     * @param parentNode the parent node
     * @param nodeCmName the cm:name of the new node
     * @param nodeCreator the username of the person who will create the node
     * @param isVersionable should the new node be {@link ContentModel#ASPECT_VERSIONABLE versionable}?
     * @return the newly created NodeRef.
     */
    public NodeRef createQuickFile(final String mimetype, final NodeRef parentNode, final String nodeCmName, final String nodeCreator, final boolean isVersionable)
    {
        final RetryingTransactionHelper transactionHelper = (RetryingTransactionHelper) appContextRule.getApplicationContext().getBean("retryingTransactionHelper");
        
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(nodeCreator);
        
        NodeRef newNodeRef = transactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                final NodeRef result = createNode(nodeCmName, parentNode, ContentModel.TYPE_CONTENT);
                
                if (isVersionable)
                {
                    NodeService nodeService = appContextRule.getApplicationContext().getBean("nodeService", NodeService.class);
                    nodeService.addAspect(result, ContentModel.ASPECT_VERSIONABLE, null);
                }
                
                File quickFile = AbstractContentTransformerTest.loadNamedQuickTestFile(getQuickResource(mimetype));
                
                ContentService contentService = appContextRule.getApplicationContext().getBean("contentService", ContentService.class);
                ContentWriter writer = contentService.getWriter(result, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(mimetype);
                writer.setEncoding("UTF-8");
                writer.putContent(quickFile);
                
                return result;
            }
        });
        
        AuthenticationUtil.popAuthentication();
        
        this.temporaryNodeRefs.add(newNodeRef);
        return newNodeRef;
    }
    
    /**
     * This method creates a cm:content NodeRef whose content is taken from the named Alfresco 'quick' file and adds it to the internal
     * list of NodeRefs to be tidied up by the rule.
     * This method will be run in its own transaction and will be run with the specified user as the fully authenticated user,
     * thus ensuring the named user is the cm:creator of the new node.
     * 
     * @param parentNode the parent node
     * @param nodeCmName the file name of the quick file - will also be the cm:name of the new node.
     * @param nodeCreator the username of the person who will create the node
     * @return the newly created NodeRef.
     * @since 4.1.4
     */
    public NodeRef createQuickFileByName(final String quickFileName, final NodeRef parentNode, final String nodeCreator)
    {
        final MimetypeMap mimetypeService = (MimetypeMap) appContextRule.getApplicationContext().getBean("mimetypeService");
        final RetryingTransactionHelper transactionHelper = (RetryingTransactionHelper) appContextRule.getApplicationContext().getBean("retryingTransactionHelper");
        
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(nodeCreator);
        
        NodeRef newNodeRef = transactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                final NodeRef result = createNode(quickFileName, parentNode, ContentModel.TYPE_CONTENT);
                
                File quickFile = AbstractContentTransformerTest.loadNamedQuickTestFile(quickFileName);
                
                ContentService contentService = appContextRule.getApplicationContext().getBean("contentService", ContentService.class);
                ContentWriter writer = contentService.getWriter(result, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(mimetypeService.guessMimetype(quickFileName));
                writer.setEncoding("UTF-8");
                writer.putContent(quickFile);
                
                return result;
            }
        });
        
        AuthenticationUtil.popAuthentication();
        
        this.temporaryNodeRefs.add(newNodeRef);
        return newNodeRef;
    }
    
    private NodeRef createNode(String cmName, NodeRef parentNode, QName nodeType)
    {
        final NodeService nodeService = (NodeService) appContextRule.getApplicationContext().getBean("nodeService");
        
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, cmName);
        ChildAssociationRef childAssoc = nodeService.createNode(parentNode,
                    ContentModel.ASSOC_CONTAINS,
                    ContentModel.ASSOC_CONTAINS,
                    nodeType,
                    props);
        
        return childAssoc.getChildRef();
    }
    
    /**
     * Gets the resource name for the Alfresco 'quick file' associated with the given mime type.
     * @param mimetype the MIME type e.g. {@link MimetypeMap#MIMETYPE_IMAGE_JPEG}
     * @return the resource path e.g. "quick/quick.jpg"
     */
    private String getQuickResource(String mimetype)
    {
        final MimetypeMap mimetypeService = (MimetypeMap) appContextRule.getApplicationContext().getBean("mimetypeService");
        final String extension = mimetypeService.getExtension(mimetype);
        
        if (extension == null)
        {
            throw new UnsupportedOperationException("No 'quick' file for unrecognised mimetype: " + mimetype);
        }
        
        return "quick." + extension;
    }
    
}
