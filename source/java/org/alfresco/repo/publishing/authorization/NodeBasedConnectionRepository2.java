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

package org.alfresco.repo.publishing.authorization;

import static org.alfresco.model.ContentModel.ASSOC_CONTAINS;
import static org.alfresco.repo.publishing.PublishingModel.TYPE_PUBLISHING_CONNECTION;
import java.util.Collections;
import java.util.List;

import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.Function;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class NodeBasedConnectionRepository2 implements ConnectionRepository
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
    private FileFolderService fileFolderService;
    private SearchService searchService;
    private Repository repositoryHelper;

    // TODO Replace this with doing it properly...
    private NodeRef authRootNode;

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param fileFolderService the fileFolderService to set
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }
    
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setRepositoryHelper(Repository repositoryHelper)
    {
        this.repositoryHelper = repositoryHelper;
    }

    /**
    * {@inheritDoc}
    */
    public MultiValueMap<String, Connection<?>> findConnections()
    {
        MultiValueMap<String, Connection<?>> results = new LinkedMultiValueMap<String, Connection<?>>();
        for (String providerId : getAllProviderIds())
        {
            List<Connection<?>> connections = findConnectionsToProvider(providerId);
            if(connections!=null && connections.isEmpty() == false)
            {
                results.put(providerId, connections);
            }
        }
        return results;
    }

    /**
    * {@inheritDoc}
    */
    public List<Connection<?>> findConnectionsToProvider(String providerId)
    {
        NodeRef providerNode = getProviderNode(providerId);
        List<ChildAssociationRef> connectionNodes = nodeService.getChildAssocs(providerNode, Collections.singleton(TYPE_PUBLISHING_CONNECTION));
        return convertConnections(connectionNodes);
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public MultiValueMap<String, Connection<?>> findConnectionsForUsers(MultiValueMap<String, String> providerUserIds)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public Connection<?> findConnection(ConnectionKey connectionKey)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public <A> Connection<A> findPrimaryConnectionToApi(Class<A> apiType)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public <A> Connection<A> findConnectionToApiForUser(Class<A> apiType, String providerUserId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public <A> List<Connection<A>> findConnectionsToApi(Class<A> apiType)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void addConnection(Connection<?> connection)
    {
        // TODO Auto-generated method stub
        
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void updateConnection(Connection<?> connection)
    {
        // TODO Auto-generated method stub
        
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void removeConnectionsToProvider(String providerId)
    {
        // TODO Auto-generated method stub
        
    }

    /**
    * {@inheritDoc}
    */
    public void removeConnection(ConnectionKey connectionKey)
    {
        // TODO Auto-generated method stub
        
    }

    private List<String> getAllProviderIds()
    {
        return CollectionUtils.transform(fileFolderService.listFolders(getAuthentiactionRoot()), new Function<FileInfo, String>()
        {
            public String apply(FileInfo value)
            {
                return value.getName();
            }
        });
    }
    
    private NodeRef getProviderNode(String providerId)
    {
        return nodeService.getChildByName(getAuthentiactionRoot(), ASSOC_CONTAINS, providerId);
    }
    
    private NodeRef getAuthentiactionRoot()
    {
        //TODO
        return null;
    }
    
    private List<Connection<?>> convertConnections(List<ChildAssociationRef> connectionNodes)
    {
        //TODO
        return null;
    }


}
