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
package org.alfresco.repo.usage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.TransactionServiceImpl;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.usage.UsageService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User Usage Tracking Component - to allow user usages to be collapsed or re-calculated
 * 
 * - used by UserUsageCollapseJob to collapse usage deltas.
 * - used by UserUsageBootstrapJob to either clear all usages or (re-)calculate all missing usages.
 */
public class UserUsageTrackingComponent
{
    private static Log logger = LogFactory.getLog(UserUsageTrackingComponent.class);
    
    private static boolean busy = false;
    
    private boolean bootstrap = false;
    
    private NodeDaoService nodeDaoService;
    private TransactionServiceImpl transactionService;
    private ContentUsageImpl contentUsageImpl;
    
    private PersonService personService;
    private NodeService nodeService;   
    private UsageService usageService;
    
    private boolean enabled = true;
    
    
    public void setNodeDaoService(NodeDaoService nodeDaoService)
    {
        this.nodeDaoService = nodeDaoService;
    }
    
    public void setTransactionService(TransactionServiceImpl transactionService)
    {
        this.transactionService = transactionService;
    }
    
    public void setContentUsageImpl(ContentUsageImpl contentUsageImpl)
    {
        this.contentUsageImpl = contentUsageImpl;
    }
    
    public void setBootstrap(boolean bootstrap)
    {
        this.bootstrap = bootstrap;
    }
    
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setUsageService(UsageService usageService)
    {
        this.usageService = usageService;
    }
    
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
    
    
    public void execute()
    {
        try
        {
            if (! busy && ! enabled)
            {
                busy = true;
                
                // disabled - remove all usages
                if (bootstrap == true)
                {
                    if (logger.isDebugEnabled()) 
                    {
                        logger.debug("Disabled - clear usages for all users ...");
                    }

                    RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
                    
                    // wrap to make the request in a transaction
                    RetryingTransactionCallback<Integer> clearAllUsages = new RetryingTransactionCallback<Integer>()
                    {
                        public Integer execute() throws Throwable
                        {
                            Set<NodeRef> allPeople = personService.getAllPeople();
                            
                            for (NodeRef personNodeRef : allPeople)
                            {
                                nodeService.setProperty(personNodeRef, ContentModel.PROP_SIZE_CURRENT, null);
                                usageService.deleteDeltas(personNodeRef);
                            }
                            return allPeople.size();
                        }
                    };
                    // execute in txn
                    int count = txnHelper.doInTransaction(clearAllUsages, false);
         
                    if (logger.isDebugEnabled()) 
                    {
                        logger.debug("... cleared usage for " + count + " users");
                    }
                }
            }
            else if (! busy && enabled)
            {
                busy = true;

                if (bootstrap == true)
                {
                    if (logger.isDebugEnabled()) 
                    {
                        logger.debug("Enabled - calculate usages for all users (without usage) ...");
                    }

                    RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
                    
                    // wrap to make the request in a transaction
                    RetryingTransactionCallback<Set<String>> getAllPeople = new RetryingTransactionCallback<Set<String>>()
                    {
                        public Set<String> execute() throws Throwable
                        {
                            Set<NodeRef> allPeople = personService.getAllPeople();
                            Set<String> userNames = new HashSet<String>();
                            
                            for (NodeRef personNodeRef : allPeople)
                            {
                                Long currentUsage = (Long)nodeService.getProperty(personNodeRef, ContentModel.PROP_SIZE_CURRENT);
                                if (currentUsage == null)
                                {
                                    String userName = (String)nodeService.getProperty(personNodeRef, ContentModel.PROP_USERNAME);
                                    userNames.add(userName);
                                }
                            }
                            return userNames;
                        }
                    };
                    // execute in READ-ONLY txn
                    final Set<String> userNames = txnHelper.doInTransaction(getAllPeople, true);
                    
                    for (String userName : userNames)
                    {
                        recalculateUsage(userName);
                    }
         
                    if (logger.isDebugEnabled()) 
                    {
                        logger.debug("... calculated usage for " + userNames.size() + " users");
                    }
                }
                else
                {
                    // Collapse usage deltas (if a person has initial usage set)
                    RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
                    
                    // wrap to make the request in a transaction
                    RetryingTransactionCallback<Object> collapseUsages = new RetryingTransactionCallback<Object>()
                    {
                        public Object execute() throws Throwable
                        {
                            // Get distinct candidates
                            Set<NodeRef> usageNodeRefs = usageService.getUsageDeltaNodes();
                            
                            for(NodeRef usageNodeRef : usageNodeRefs)
                            {                            
                                QName nodeType = nodeService.getType(usageNodeRef);
                                
                                if (nodeType.equals(ContentModel.TYPE_PERSON))
                                {
                                    NodeRef personNodeRef = usageNodeRef;
                                    String userName = (String)nodeService.getProperty(personNodeRef, ContentModel.PROP_USERNAME);
                                    
                                    long currentUsage = contentUsageImpl.getUserStoredUsage(personNodeRef);
                                    if (currentUsage != -1)
                                    {
                                        // collapse the usage deltas
                                        currentUsage = contentUsageImpl.getUserUsage(userName);                                 
                                        usageService.deleteDeltas(personNodeRef);
                                        contentUsageImpl.setUserStoredUsage(personNodeRef, currentUsage);
                                        
                                        if (logger.isDebugEnabled()) 
                                        {
                                            logger.debug("Collapsed usage: username=" + userName + ", usage=" + currentUsage);
                                        }
                                    }
                                    else
                                    {
                                        if (logger.isWarnEnabled())
                                        {
                                            logger.warn("Initial usage for user has not yet been calculated: " + userName);
                                        }
                                    }
                                }
                            }  
                            return null;
                        }
                    };
                    
                    txnHelper.doInTransaction(collapseUsages, false);
                }
            }
        }
        finally
        {
            busy = false;
        }
    }

    /**
     * Recalculate content usage for given user. Required if upgrading an existing Alfresco, for users that
     * have not had their initial usage calculated. In a future release, could also be called explicitly by
     * a SysAdmin, eg. via a JMX operation.
     * 
     * @param userName
     */
    public void recalculateUsage(final String userName)
    { 
        final StoreRef storeRef = ContentUsageImpl.SPACES_STOREREF;
        
        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        
        // wrap to make the request in a transaction
        RetryingTransactionCallback<Long> calculatePersonCurrentUsage = new RetryingTransactionCallback<Long>()
        {
            public Long execute() throws Throwable
            {
                // get nodes for which user is owner
                Collection<Node> ownerNodes = nodeDaoService.getNodesWithPropertyStringValueForStore(storeRef, ContentModel.PROP_OWNER, userName);
                
                long totalUsage = 0;
                for (Node ownerNode : ownerNodes)
                {
                    if (ownerNode.getTypeQName().equals(ContentModel.TYPE_CONTENT))
                    {
                        ContentData contentData = ContentData.createContentProperty(ownerNode.getProperties().get(ContentModel.PROP_CONTENT).getStringValue());
                        totalUsage = totalUsage + contentData.getSize();
                    }
                }
                
                // get nodes for which user is creator, and then filter out those that have an owner
                Collection<Node> creatorNodes = nodeDaoService.getNodesWithPropertyStringValueForStore(storeRef, ContentModel.PROP_CREATOR, userName);
                
                for (Node creatorNode : creatorNodes)
                {
                    if (creatorNode.getTypeQName().equals(ContentModel.TYPE_CONTENT) &&
                        creatorNode.getProperties().get(ContentModel.PROP_OWNER) == null)
                    {
                        ContentData contentData = ContentData.createContentProperty(creatorNode.getProperties().get(ContentModel.PROP_CONTENT).getStringValue());
                        totalUsage = totalUsage + contentData.getSize();
                    }
                }                   
                
                if (logger.isDebugEnabled()) 
                {
                    long quotaSize = contentUsageImpl.getUserQuota(userName);
                    logger.debug("Recalc usage ("+ userName+") totalUsage="+totalUsage+", quota="+quotaSize);
                }
                		
                return totalUsage;
            }
        };
        // execute in READ-ONLY txn
        final Long currentUsage = txnHelper.doInTransaction(calculatePersonCurrentUsage, true);
        
        // wrap to make the request in a transaction
        RetryingTransactionCallback<Object> setUserCurrentUsage = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                NodeRef personNodeRef = personService.getPerson(userName);
                contentUsageImpl.setUserStoredUsage(personNodeRef, currentUsage);
                usageService.deleteDeltas(personNodeRef);
                return null;
            }
        };
        txnHelper.doInTransaction(setUserCurrentUsage, false);           
    }
}
