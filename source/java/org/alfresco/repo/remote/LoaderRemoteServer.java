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
package org.alfresco.repo.remote;

import java.util.List;

import net.sf.acegisecurity.Authentication;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.remote.LoaderRemote;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Server side implementation of the <code>LoaderServiceTransport</code> transport
 * layer.  This is the class that gets exported remotely as it contains the
 * explicit ticket arguments.
 * 
 * @author Derek Hulley
 * @since 2.2
 */
public class LoaderRemoteServer implements LoaderRemote
{
    private static Log logger = LogFactory.getLog(LoaderRemoteServer.class);
    
    /** The association for the working root node: <b>sys:LoaderServiceWorkingRoot</b> */
    private static final QName ASSOC_WORKING_ROOT = QName.createQName(
            NamespaceService.SYSTEM_MODEL_1_0_URI,
            "LoaderServiceWorkingRoot");
    
    private RetryingTransactionHelper retryingTransactionHelper;
    private AuthenticationService authenticationService;
    private NodeService nodeService;
    private NodeDaoService nodeDaoService;

    /**
     * @param transactionService            provides transactional support and retrying
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.retryingTransactionHelper = transactionService.getRetryingTransactionHelper();
    }

    /**
     * @param authenticationService         the service that will validate the tickets
     */
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    /**
     * @param nodeService                   the service that will do the work
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param nodeDaoService                the DAO for node queries
     */
    public void setNodeDaoService(NodeDaoService nodeDaoService)
    {
        this.nodeDaoService = nodeDaoService;
    }

    /**
     * {@inheritDoc}
     */
    public String authenticate(String username, String password)
    {
        // We need to authenticate
        Authentication authentication = AuthenticationUtil.getCurrentAuthentication();
        try
        {
            authenticationService.authenticate(username, password.toCharArray());
            String ticket = authenticationService.getCurrentTicket();
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug("Authenticated: " + username);
            }
            return ticket;
        }
        finally
        {
            AuthenticationUtil.setCurrentAuthentication(authentication);
        }
    }

    /**
     * {@inheritDoc}
     */
    public NodeRef getOrCreateWorkingRoot(String ticket, final StoreRef storeRef)
    {
        Authentication authentication = AuthenticationUtil.getCurrentAuthentication();
        try
        {
            authenticationService.validate(ticket);
            // Make the call
            RetryingTransactionCallback<NodeRef> callback = new RetryingTransactionCallback<NodeRef>()
            {
                public NodeRef execute() throws Throwable
                {
                    NodeRef rootNodeRef = null;
                    // Check if the store exists
                    if (!nodeService.exists(storeRef))
                    {
                        nodeService.createStore(storeRef.getProtocol(), storeRef.getIdentifier());
                    }
                    rootNodeRef = nodeService.getRootNode(storeRef);
                    // Look for the working root
                    List<ChildAssociationRef> assocRefs = nodeService.getChildAssocs(
                            rootNodeRef,
                            ContentModel.ASSOC_CHILDREN,
                            ASSOC_WORKING_ROOT);
                    NodeRef workingRootNodeRef = null;
                    if (assocRefs.size() > 0)
                    {
                        // Just use the first one
                        workingRootNodeRef = assocRefs.get(0).getChildRef();
                    }
                    else
                    {
                        String username = authenticationService.getCurrentUserName();
                        // We have to make it.  Touch the root node to act as an optimistic lock.
                        nodeService.setProperty(rootNodeRef, ContentModel.PROP_AUTHOR, username);
                        // Add a cm:folder below the root
                        PropertyMap properties = new PropertyMap();
                        properties.put(ContentModel.PROP_NAME, "Loader Application Root");
                        workingRootNodeRef = nodeService.createNode(
                                rootNodeRef,
                                ContentModel.ASSOC_CHILDREN,
                                ASSOC_WORKING_ROOT,
                                ContentModel.TYPE_FOLDER,
                                properties).getChildRef();
                    }
                    // Done
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Got working root node: " + workingRootNodeRef);
                    }
                    return workingRootNodeRef;
                }
            };
            return retryingTransactionHelper.doInTransaction(callback, false, true);
        }
        finally
        {
            AuthenticationUtil.setCurrentAuthentication(authentication);
        }
    }

    /**
     * {@inheritDoc}
     */
    public int getNodeCount(String ticket)
    {
        Authentication authentication = AuthenticationUtil.getCurrentAuthentication();
        try
        {
            authenticationService.validate(ticket);
            // Make the call
            RetryingTransactionCallback<Integer> callback = new RetryingTransactionCallback<Integer>()
            {
                public Integer execute() throws Throwable
                {
                    return nodeDaoService.getNodeCount();
                }
            };
            return retryingTransactionHelper.doInTransaction(callback, false, true);
        }
        finally
        {
            AuthenticationUtil.setCurrentAuthentication(authentication);
        }
    }

    /**
     * {@inheritDoc}
     */
    public int getNodeCount(String ticket, final StoreRef storeRef)
    {
        Authentication authentication = AuthenticationUtil.getCurrentAuthentication();
        try
        {
            authenticationService.validate(ticket);
            // Make the call
            RetryingTransactionCallback<Integer> callback = new RetryingTransactionCallback<Integer>()
            {
                public Integer execute() throws Throwable
                {
                    return nodeDaoService.getNodeCount(storeRef);
                }
            };
            return retryingTransactionHelper.doInTransaction(callback, false, true);
        }
        finally
        {
            AuthenticationUtil.setCurrentAuthentication(authentication);
        }
    }
}
