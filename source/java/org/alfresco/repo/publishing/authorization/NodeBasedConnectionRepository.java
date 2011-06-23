/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.publishing.authorization;

import static org.alfresco.repo.publishing.PublishingModel.PROP_ACCESS_SECRET;
import static org.alfresco.repo.publishing.PublishingModel.PROP_ACCESS_TOKEN;
import static org.alfresco.repo.publishing.PublishingModel.PROP_ACCOUNT_ID;
import static org.alfresco.repo.publishing.PublishingModel.PROP_PROVIDER_ACCOUNT_ID;
import static org.alfresco.repo.publishing.PublishingModel.PROP_PROVIDER_ID;
import static org.alfresco.repo.publishing.PublishingModel.PROP_REFRESH_TOKEN;
import static org.alfresco.repo.publishing.PublishingModel.TYPE_PUBLISHING_CONNECTION;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.oauth1.OAuthToken;
import org.springframework.social.twitter.api.TwitterApi;
import org.springframework.social.twitter.connect.TwitterConnectionFactory;

/**
 * A node-backed Social Connection Repository, that stores the
 *  credentials and auth tokens in the Data Dictionary.
 * 
 * @author Nick Burch
 * @author Nick Smith
 */
public class NodeBasedConnectionRepository
{
    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 3258131523636186548L;

    /** Reference to the auth store space node */
    private static final StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
    protected static final NodeRef SOCIAL_PUBLISHING_AUTHORISATION_ROOT_NODE_REF = 
        new NodeRef(SPACES_STORE, "social_publishing_authorisation_space");

    private NodeService nodeService;
    private SearchService searchService;
    private Repository repositoryHelper;

    // TODO Replace this with doing it properly...
    private NodeRef authRootNode;

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setRepositoryHelper(Repository repositoryHelper)
    {
        this.repositoryHelper = repositoryHelper;
    }

    private void findAuthRootNode() {
        if(authRootNode != null) return;
        
        // TODO Replace this with doing it properly...
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                if(! nodeService.exists(SOCIAL_PUBLISHING_AUTHORISATION_ROOT_NODE_REF))
                {
                    NodeRef dataDictionary = nodeService.getChildByName( 
                            repositoryHelper.getCompanyHome(),
                            ContentModel.ASSOC_CONTAINS,
                            "dictionary"
                    );
                    authRootNode = nodeService.getChildByName(
                            dataDictionary,
                            ContentModel.ASSOC_CONTAINS,
                            SOCIAL_PUBLISHING_AUTHORISATION_ROOT_NODE_REF.getId()
                    );
                    if(authRootNode == null)
                    {
                        authRootNode = nodeService.createNode(
                                dataDictionary,
                                ContentModel.ASSOC_CONTAINS,
                                QName.createQName("{}"+SOCIAL_PUBLISHING_AUTHORISATION_ROOT_NODE_REF.getId()),
                                ContentModel.TYPE_FOLDER
                        ).getChildRef();
                    }
                }
                else
                {
                    authRootNode = SOCIAL_PUBLISHING_AUTHORISATION_ROOT_NODE_REF;
                }
                return null;
            }
        }, AuthenticationUtil.getAdminUserName());
    }
    
    private NodeRef findProvider(final String providerId, boolean autoCreate)
    {
        NodeRef node = nodeService.getChildByName(
                authRootNode, ContentModel.ASSOC_CONTAINS, providerId
        );
        if(autoCreate && node == null)
        {
            node = AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
                   {
                      @Override
                      public NodeRef doWork() throws Exception
                      {
                          return nodeService.createNode(
                                  authRootNode, ContentModel.ASSOC_CONTAINS,
                                  QName.createQName("{}" + providerId),
                                  ContentModel.TYPE_FOLDER
                          ).getChildRef();
                      }
                   } , AuthenticationUtil.getAdminUserName()
            );
        }
        return node;
    }
    
    private NodeRef findAccount(Serializable accountId, String providerId) 
    {
        findAuthRootNode();
        NodeRef folder = findProvider(providerId, false);
        if(folder == null)
        {
            return null;
        }
        
        NodeRef account = nodeService.getChildByName(
                folder, ContentModel.ASSOC_CONTAINS, accountId.toString()
        );
        return account;
    }
    
    private Connection<?> buildConnection(NodeRef node)
    {
//        Map<QName,Serializable> props = nodeService.getProperties(node);
//        return new Connection(
//                (long)-1,
//                (String)props.get(PROP_ACCESS_TOKEN),
//                (String)props.get(PROP_ACCESS_SECRET),
//                (String)props.get(PROP_REFRESH_TOKEN),
//                (String)props.get(PROP_PROVIDER_ACCOUNT_ID)
//        );
        return null;
    }
    
    public boolean isConnected(Serializable accountId, String providerId) 
    {
        NodeRef account = findAccount(accountId, providerId);
        return (account != null);
    }

    public Serializable findAccountIdByConnectionAccessToken(String providerId,
            String accessToken) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<Serializable> findAccountIdsForProviderAccountIds(
            String providerId, List<String> providerAccountIds) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<Connection<?>> findConnections(Serializable accountId,
            String providerId) {
        NodeRef account = findAccount(accountId, providerId);
        if(account == null)
        {
            return Collections.emptyList();
        }
        
        List<Connection<?>> connections = new ArrayList<Connection<?>>();
        connections.add( buildConnection(account) );
        return connections;
    }

    public void removeConnection(Serializable accountId, String providerId,
            Long connectionId) 
    {
        NodeRef account = findAccount(accountId, providerId);
        if(account != null)
        {
            nodeService.deleteNode(account);
        }
    }

    public Connection saveConnection(Serializable accountId, String providerId,
            Connection connection) 
    {
//        Map<QName, Serializable> args = new HashMap<QName, Serializable>();
//        args.put(ContentModel.PROP_NAME, accountId);
//        args.put(PROP_ACCOUNT_ID, accountId);
//        args.put(PROP_PROVIDER_ID, providerId);
//        args.put(PROP_PROVIDER_ACCOUNT_ID, connection.getProviderAccountId());
//        args.put(PROP_ACCESS_TOKEN, connection.getAccessToken());
//        args.put(PROP_ACCESS_SECRET, connection.getSecret());
//        args.put(PROP_REFRESH_TOKEN, connection.getRefreshToken());
//        
//        findAuthRootNode();
//        NodeRef folder = findProvider(providerId, true);
//        NodeRef node = nodeService.createNode(
//                folder, ContentModel.ASSOC_CONTAINS,
//                QName.createQName("{}" + accountId.toString()),
//                TYPE_PUBLISHING_CONNECTION,
//                args
//        ).getChildRef();
//        
//        return buildConnection(node);
        return null;
    }
}
