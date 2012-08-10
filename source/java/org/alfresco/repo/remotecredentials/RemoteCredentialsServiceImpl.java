/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.remotecredentials;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.query.EmptyPagingResults;
import org.alfresco.query.ListBackedPagingResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.node.SystemNodeUtils;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.remotecredentials.BaseCredentialsInfo;
import org.alfresco.service.cmr.remotecredentials.RemoteCredentialsService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An Implementation of the {@link RemoteCredentialsService}
 * 
 * @author Nick Burch
 * @since Odin
 */
public class RemoteCredentialsServiceImpl implements RemoteCredentialsService
{
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(RemoteCredentialsServiceImpl.class);
    
    /**
     * The name of the System Container used to hold Shared Credentials.
     * This isn't final, as unit tests change it to avoid trampling on
     *  the real credentials.
     */
    private static String SHARED_CREDENTIALS_CONTAINER_NAME = "remote_credentials";

    private Repository repositoryHelper;
    private NodeService nodeService;
    private NamespaceService namespaceService;
    private PermissionService permissionService;
    private DictionaryService dictionaryService;
    
    /**
     * Controls which Factory will be used to create {@link BaseCredentialsInfo}
     *  instances for a given node, based on the type.
     * eg rc:passwordCredentials -> PasswordCredentialsFactory
     */
    private Map<QName,RemoteCredentialsInfoFactory> credentialsFactories = new HashMap<QName, RemoteCredentialsInfoFactory>();

    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    public void setRepositoryHelper(Repository repositoryHelper)
    {
        this.repositoryHelper = repositoryHelper;
    }

    /**
     * Registers a number of new factories
     */
    public void setCredentialsFactories(Map<String,RemoteCredentialsInfoFactory> factories)
    {
        // Convert, eg rc:passwordCredentials -> qname version, then register
        for (String type : factories.keySet())
        {
            RemoteCredentialsInfoFactory factory = factories.get(type);
            QName typeQ = QName.createQName(type, namespaceService);
            registerCredentialsFactory(typeQ, factory);
        }
    }
    
    /**
     * Registers a new Factory to produce {@link BaseCredentialsInfo} objects
     *  for a given data type.
     * This provides an alternative to {@link #setCredentialsFactories(Map)}
     *  to allow the registering of a new type without overriding all of them.
     *  
     * @param credentialsType The object type
     * @param factory The Factory to use to create this type with
     */
    public void registerCredentialsFactory(QName credentialsType, RemoteCredentialsInfoFactory factory)
    {
        // Check the hierarchy is valid
        if (! dictionaryService.isSubClass(credentialsType, RemoteCredentialsModel.TYPE_CREDENTIALS_BASE))
        {
            logger.warn("Unable to register credentials factory for " + credentialsType + 
                        " as that type doesn't inherit from " + RemoteCredentialsModel.TYPE_CREDENTIALS_BASE);
            return;
        }
        
        // Log the new type
        if (logger.isDebugEnabled())
            logger.debug("Registering credentials factory for " + credentialsType + " of " + factory);

        // Store it
        credentialsFactories.put(credentialsType, factory);
    }
    
    /**
     * Provides a read only copy of the credentials factories, useful in unit tests
     */
    protected Map<QName,RemoteCredentialsInfoFactory> getCredentialsFactories()
    {
        return Collections.unmodifiableMap(credentialsFactories);
    }
    
    // --------------------------------------------------------
    
    private static QName SHARED_CREDENTIALS_CONTAINER_QNAME = 
        QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, SHARED_CREDENTIALS_CONTAINER_NAME); 
    /**
     * Gets the NodeRef of the holder of shared credentials remote systems.
     * 
     * This is stored under system
     * 
     * Protected, so that unit tests can make use of it
     */
    protected NodeRef getSharedContainerNodeRef(boolean required)
    {
        // Get the container, if available
        NodeRef container = SystemNodeUtils.getSystemChildContainer(SHARED_CREDENTIALS_CONTAINER_QNAME, nodeService, repositoryHelper);
        
        // If it's needed, have it created
        if (container == null && required)
        {
            // Lock and create
            Pair<NodeRef,Boolean> details = null;
            synchronized (this)
            {
                details = SystemNodeUtils.getOrCreateSystemChildContainer(SHARED_CREDENTIALS_CONTAINER_QNAME, nodeService, repositoryHelper);
            }
            container = details.getFirst();
            
            // If created, set permissions
            // Note - these must be kept in sync with the bootstrap file
            if (details.getSecond())
            {
                final NodeRef containerF = container;
                AuthenticationUtil.runAsSystem(new RunAsWork<Void>() {
                    @Override
                    public Void doWork() throws Exception
                    {
                        // Add the aspect
                        nodeService.addAspect(containerF, RemoteCredentialsModel.ASPECT_REMOTE_CREDENTIALS_SYSTEM_CONTAINER, null);

                        // Set up the default permissions on the container
                        // By default, anyone can add children, and read, but not edit other's credentials
                        // (These can be changed later if needed by an administrator)
                        permissionService.setInheritParentPermissions(containerF, false);
                        permissionService.setPermission(
                                containerF, PermissionService.ALL_AUTHORITIES,
                                PermissionService.ADD_CHILDREN, true);
                        permissionService.setPermission(
                                containerF, PermissionService.ALL_AUTHORITIES,
                                PermissionService.READ, true);

                        permissionService.setPermission(
                                containerF, PermissionService.OWNER_AUTHORITY,
                                PermissionService.FULL_CONTROL, true);
                        
                        return null;
                    }
                });
            }
        }

        if (container == null)
        {
            if (logger.isInfoEnabled())
                logger.info("Required System Folder " + SHARED_CREDENTIALS_CONTAINER_QNAME + " not yet created, will be lazy created on write");
            return null;
        }
        return container;
    }
    
    /**
     * Gets, creating as needed, the person credentials container for the given system
     */
    private NodeRef getPersonContainer(String remoteSystem, boolean lazyCreate)
    {
        // Get the person node
        NodeRef person = repositoryHelper.getPerson();
        if (person == null)
        {
            // Something's rather broken, the service security ought to prevent this 
            throw new IllegalStateException("Person details required but none found! Running as " + AuthenticationUtil.getRunAsUser());
        }
        
        // If we're in edit mode, ensure the correct aspect is applied
        if (lazyCreate)
        {
            ensureCredentialsSystemContainer(person);
        }
        
        // Find the container
        return findRemoteSystemContainer(person, remoteSystem, lazyCreate);
    }
    /**
     * Gets, creating as needed, the shared credentials container for the given system
     */
    private NodeRef getSharedContainer(String remoteSystem, boolean lazyCreate)
    {
        // Find the shared credentials container, under system
        NodeRef systemContainer = getSharedContainerNodeRef(lazyCreate);
        if (systemContainer == null) return null;
        
        // If we're in edit mode, ensure the correct aspect is applied
        if (lazyCreate)
        {
            ensureCredentialsSystemContainer(systemContainer);
        }
        
        // Find the container
        return findRemoteSystemContainer(systemContainer, remoteSystem, lazyCreate);
    }
    
    /**
     * Ensure the appropriate aspect is applied to the node which
     *  will hold the Remote Credentials System 
     */
    private void ensureCredentialsSystemContainer(final NodeRef nodeRef)
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>() {
            @Override
            public Void doWork() throws Exception
            {
                if (!nodeService.hasAspect(nodeRef, RemoteCredentialsModel.ASPECT_REMOTE_CREDENTIALS_SYSTEM_CONTAINER))
                {
                    // Add the aspect
                    nodeService.addAspect(nodeRef, RemoteCredentialsModel.ASPECT_REMOTE_CREDENTIALS_SYSTEM_CONTAINER, null);
                    
                    if (logger.isDebugEnabled())
                        logger.debug("Added the Credentials Container aspect to " + nodeRef);
                }
                return null;
            }
        });
    }
    private NodeRef findRemoteSystemContainer(NodeRef nodeRef, String remoteSystem, boolean lazyCreate)
    {
        QName remoteSystemQName = QName.createQName(remoteSystem);
        List<ChildAssociationRef> systems = nodeService.getChildAssocs(
                nodeRef, RemoteCredentialsModel.ASSOC_CREDENTIALS_SYSTEM, remoteSystemQName);
        
        NodeRef system = null;
        if (systems.size() > 0)
        {
            system = systems.get(0).getChildRef();
            
            if (logger.isDebugEnabled())
                logger.debug("Resolved Remote Credentials Container for " + remoteSystem + " of " + system + " in parent " + nodeRef);
        }
        else
        {
            if (lazyCreate)
            {
                // Create, as the current user
                system = nodeService.createNode(
                        nodeRef, RemoteCredentialsModel.ASSOC_CREDENTIALS_SYSTEM,
                        QName.createQName(remoteSystem), RemoteCredentialsModel.TYPE_REMOTE_CREDENTIALS_SYSTEM
                ).getChildRef();
                
                if (logger.isDebugEnabled())
                    logger.debug("Lazy created Remote Credentials Container for " + remoteSystem + 
                                 " in parent " + nodeRef + ", new container is " + system);
            }
            else
            {
                if (logger.isDebugEnabled())
                    logger.debug("No Remote Credentials Container for " + remoteSystem + " found in " +
                                 nodeRef + ", will be lazy created on write");
            }
        }
        
        return system;
    }

    // --------------------------------------------------------

    @Override
    public void deleteCredentials(BaseCredentialsInfo credentialsInfo)
    {
        if (credentialsInfo.getNodeRef() == null)
        {
            throw new IllegalArgumentException("Cannot delete Credentials which haven't been persisted yet!");
        }
        nodeService.deleteNode(credentialsInfo.getNodeRef());
        
        if (logger.isDebugEnabled())
            logger.debug("Deleted credentials " + credentialsInfo + " from " + credentialsInfo.getNodeRef() + 
                         " from Remote System " + credentialsInfo.getRemoteSystemName());
        
        // Leave the Remote System Container, in case special permissions
        //  were previously applied to it that should be retained
    }

    @Override
    public BaseCredentialsInfo createPersonCredentials(String remoteSystem, BaseCredentialsInfo credentials)
    {
        NodeRef personContainer = getPersonContainer(remoteSystem, true);
        return createCredentials(remoteSystem, personContainer, credentials);
    }
    @Override
    public BaseCredentialsInfo createSharedCredentials(String remoteSystem, BaseCredentialsInfo credentials)
    {
        NodeRef shared = getSharedContainer(remoteSystem, true);
        return createCredentials(remoteSystem, shared, credentials);
    }
    private BaseCredentialsInfo createCredentials(String remoteSystem, NodeRef remoteSystemNodeRef, BaseCredentialsInfo credentials)
    {
        if (credentials.getNodeRef() != null)
        {
            throw new IllegalArgumentException("Cannot create Credentials which have already been persisted!");
        }
        
        // Check we know about the type
        RemoteCredentialsInfoFactory factory = credentialsFactories.get(credentials.getCredentialsType());
        if (factory == null)
        {
            throw new TypeConversionException("No Factory registered for type " + credentials.getCredentialsType());
        }
        
        // Build the properties
        Map<QName,Serializable> properties = RemoteCredentialsInfoFactory.FactoryHelper.getCoreCredentials(credentials);
        properties.putAll( factory.serializeCredentials(credentials) );
        
        // Generate a name for it, which will be unique and doesn't need updating
        QName name = QName.createQName(GUID.generate()); 
        
        // Add the node
        NodeRef nodeRef = nodeService.createNode(
                remoteSystemNodeRef, RemoteCredentialsModel.ASSOC_CREDENTIALS,
                name, credentials.getCredentialsType(), properties
        ).getChildRef();
        
        if (logger.isDebugEnabled())
            logger.debug("Created new credentials at " + nodeRef + " for " + remoteSystem + " in " + 
                         remoteSystemNodeRef + " of " + credentials);
        
        // Return the new object
        return factory.createCredentials(
                credentials.getCredentialsType(), nodeRef,
                remoteSystem, remoteSystemNodeRef,
                nodeService.getProperties(nodeRef)
        );
    }


    @Override
    public BaseCredentialsInfo getPersonCredentials(String remoteSystem)
    {
        NodeRef personContainer = getPersonContainer(remoteSystem, false);
        if (personContainer == null) return null;

        // Grab the children
        List<ChildAssociationRef> credentials = 
            nodeService.getChildAssocs(personContainer, RemoteCredentialsModel.ASSOC_CREDENTIALS, RegexQNamePattern.MATCH_ALL);
        if (credentials.size() > 0)
        {
            NodeRef nodeRef = credentials.get(0).getChildRef();
            return loadCredentials(remoteSystem, personContainer, nodeRef);
        }
        return null;
    }
    private BaseCredentialsInfo loadCredentials(String remoteSystem, NodeRef remoteSystemNodeRef, NodeRef credentialsNodeRef)
    {
        QName type = nodeService.getType(credentialsNodeRef);
        RemoteCredentialsInfoFactory factory = credentialsFactories.get(type);
        if (factory == null)
        {
            throw new TypeConversionException("No Factory registered for type " + type);
        }
        
        // Wrap as an object
        return factory.createCredentials(
                type, credentialsNodeRef,
                remoteSystem, remoteSystemNodeRef,
                nodeService.getProperties(credentialsNodeRef)
        );
    }


    @Override
    public BaseCredentialsInfo updateCredentials(BaseCredentialsInfo credentials)
    {
        if (credentials.getNodeRef() == null)
        {
            throw new IllegalArgumentException("Cannot update Credentials which haven't been persisted yet!");
        }
        
        RemoteCredentialsInfoFactory factory = credentialsFactories.get(credentials.getCredentialsType());
        if (factory == null)
        {
            throw new TypeConversionException("No Factory registered for type " + credentials.getCredentialsType());
        }
        
        // Grab the current set of properties
        Map<QName,Serializable> oldProps = nodeService.getProperties(credentials.getNodeRef());
        
        // Overwrite them with the credentials ones
        Map<QName,Serializable> props = new HashMap<QName,Serializable>(oldProps);
        props.putAll( RemoteCredentialsInfoFactory.FactoryHelper.getCoreCredentials(credentials) );
        props.putAll( factory.serializeCredentials(credentials) );
        
        // Store
        nodeService.setProperties(credentials.getNodeRef(), props);
        
        // For now, return as-is
        return credentials;
    }

    @Override
    public BaseCredentialsInfo updateCredentialsAuthenticationSucceeded(boolean succeeded, BaseCredentialsInfo credentials)
    {
        // We can't help with credentials that have never been stored
        if (credentials.getNodeRef() == null)
        {
            throw new IllegalArgumentException("Cannot update Credentials which haven't been persisted yet!");
        }
        
        // Return quickly if the credentials are already in the correct state 
        if (succeeded == credentials.getLastAuthenticationSucceeded())
        {
            return credentials;
        }

        
        // Do the update 
        nodeService.setProperty(credentials.getNodeRef(), RemoteCredentialsModel.PROP_LAST_AUTHENTICATION_SUCCEEDED, succeeded);
        
        // Update the object if we can
        if (credentials instanceof AbstractCredentialsImpl)
        {
            ((AbstractCredentialsImpl)credentials).setLastAuthenticationSucceeded(succeeded);
            return credentials;
        }
        else
        {
            // Need to re-load
            return loadCredentials(credentials.getRemoteSystemName(),
                    credentials.getRemoteSystemContainerNodeRef(), credentials.getNodeRef());
        }
    }
    
    
    @Override
    public PagingResults<String> listAllRemoteSystems(PagingRequest paging)
    {
        return listRemoteSystems(true, true, paging);
    }
    @Override
    public PagingResults<String> listPersonRemoteSystems(PagingRequest paging)
    {
        return listRemoteSystems(true, false, paging);
    }
    @Override
    public PagingResults<String> listSharedRemoteSystems(PagingRequest paging)
    {
        return listRemoteSystems(false, true, paging);
    }
    private PagingResults<String> listRemoteSystems(boolean people, boolean shared, PagingRequest paging)
    {
        List<NodeRef> search = new ArrayList<NodeRef>();

        if (people)
        {
            // Only search if it has the marker aspect
            NodeRef person = repositoryHelper.getPerson();
            if (nodeService.hasAspect(person, RemoteCredentialsModel.ASPECT_REMOTE_CREDENTIALS_SYSTEM_CONTAINER))
            {
                search.add(person);
            }
        }
        if (shared)
        {
            NodeRef system = getSharedContainerNodeRef(false);
            if (system != null)
            {
                search.add(system);
            }
        }
        
        // If no suitable nodes were given, bail out
        if (search.isEmpty())
        {
            return new EmptyPagingResults<String>();
        }
        
        // Look for nodes
        // Because all the information we need is held on the association, we don't
        //  really need to use a Canned Query for this
        Set<String> systems = new HashSet<String>();
        for (NodeRef nodeRef : search)
        {
            List<ChildAssociationRef> refs = 
                nodeService.getChildAssocs(nodeRef, RemoteCredentialsModel.ASSOC_CREDENTIALS_SYSTEM, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef ref : refs)
            {
                // System Name is the association name, no namespace
                systems.add( ref.getQName().getLocalName() );
            }
        }
        
        // Sort, then wrap as paged results
        List<String> sortedSystems = new ArrayList<String>(systems);
        Collections.sort(sortedSystems);
        return new ListBackedPagingResults<String>(sortedSystems, paging);
    }

    @Override
    public PagingResults<? extends BaseCredentialsInfo> listSharedCredentials(String remoteSystem,
            QName credentialsType, PagingRequest paging)
    {
        // Get the container for that system
        NodeRef container = getSharedContainer(remoteSystem, false);
        if (container == null)
        {
            return new EmptyPagingResults<BaseCredentialsInfo>();
        }
        return listCredentials(new NodeRef[] {container}, remoteSystem, credentialsType, paging);
    }
    @Override
    public PagingResults<? extends BaseCredentialsInfo> listPersonCredentials(String remoteSystem,
            QName credentialsType, PagingRequest paging)
    {
        // Get the container for that system
        NodeRef container = getPersonContainer(remoteSystem, false);
        if (container == null)
        {
            return new EmptyPagingResults<BaseCredentialsInfo>();
        }
        return listCredentials(new NodeRef[] {container}, remoteSystem, credentialsType, paging);
    }
    @Override
    public PagingResults<? extends BaseCredentialsInfo> listAllCredentials(String remoteSystem, QName credentialsType,
            PagingRequest paging)
    {
        NodeRef personContainer = getPersonContainer(remoteSystem, false);
        NodeRef systemContainer = getSharedContainer(remoteSystem, false);
        if (personContainer == null && systemContainer == null)
        {
            return new EmptyPagingResults<BaseCredentialsInfo>();
        }
        return listCredentials(new NodeRef[] {personContainer, systemContainer}, remoteSystem, credentialsType, paging);
    }
    /**
     * TODO This would probably be better done as a dedicated Canned Query
     * We want to filter by Assoc Type and Child Node Type, and the node service
     *  currently only allows you to do one or the other 
     */
    private PagingResults<? extends BaseCredentialsInfo> listCredentials(NodeRef[] containers, String remoteSystem, 
            QName credentialsType, PagingRequest paging)
    {
        // NodeService wants an exhaustive list of the types
        // Expand our single Credentials Type to cover all subtypes of it too
        Set<QName> types = null;
        if (credentialsType != null)
        {
            types = new HashSet<QName>( dictionaryService.getSubTypes(credentialsType, true) );
            
            if (logger.isDebugEnabled())
                logger.debug("Searching for credentials of " + credentialsType + " as types " + types);
        }
        
        // Find all the credentials
        List<ChildAssociationRef> credentials = new ArrayList<ChildAssociationRef>();
        for (NodeRef nodeRef : containers)
        {
            if (nodeRef != null)
            {
                // Find the credentials in the node
                List<ChildAssociationRef> allCreds = nodeService.getChildAssocs(
                        nodeRef, RemoteCredentialsModel.ASSOC_CREDENTIALS, RegexQNamePattern.MATCH_ALL);
                
                // Filter them by type, if needed
                if (types == null || types.isEmpty())
                {
                    // No type filtering needed
                    credentials.addAll(allCreds);
                }
                else
                {
                    // Check the type of each one, and add if it matches
                    for (ChildAssociationRef ref : allCreds)
                    {
                        NodeRef credNodeRef = ref.getChildRef();
                        QName credType = nodeService.getType(credNodeRef);
                        if (types.contains(credType))
                        {
                            // Matching type, accept
                            credentials.add(ref);
                        }
                    }
                }
            }
        }
        
        // Did we find any?
        if (credentials.isEmpty())
        {
            return new EmptyPagingResults<BaseCredentialsInfo>();
        }
        
        // Excerpt
        int start = paging.getSkipCount();
        int end = Math.min(credentials.size(), start + paging.getMaxItems());
        if (paging.getMaxItems() == 0)
        {
            end = credentials.size();
        }
        boolean hasMore = (end < credentials.size());
        
        List<ChildAssociationRef> wanted = credentials.subList(start, end);
        
        // Wrap and return
        return new CredentialsPagingResults(wanted, credentials.size(), hasMore, remoteSystem); 
    }
    
    // --------------------------------------------------------
    
    private class CredentialsPagingResults implements PagingResults<BaseCredentialsInfo>
    {
        private List<BaseCredentialsInfo> results;
        private boolean hasMore;
        private int size;
        
        private CredentialsPagingResults(List<ChildAssociationRef> refs, int size, boolean hasMore, String remoteSystem)
        {
            this.size = size;
            this.hasMore = hasMore; 
            
            this.results = new ArrayList<BaseCredentialsInfo>(refs.size());
            for (ChildAssociationRef ref : refs)
            {
                this.results.add( loadCredentials(remoteSystem, ref.getParentRef(), ref.getChildRef()) );
            }
        }

        @Override
        public List<BaseCredentialsInfo> getPage()
        {
            return results; 
        }

        @Override
        public Pair<Integer, Integer> getTotalResultCount()
        {
            return new Pair<Integer,Integer>(size,size);
        }

        @Override
        public boolean hasMoreItems()
        {
            return hasMore; 
        }

        @Override
        public String getQueryExecutionId()
        {
            return null;
        }
    }
    
    // --------------------------------------------------------

    /** Unit testing use only! */
    protected static String getSharedCredentialsSystemContainerName()
    {
        return SHARED_CREDENTIALS_CONTAINER_NAME;
    }
    protected static QName getSharedCredentialsSystemContainerQName()
    {
        return SHARED_CREDENTIALS_CONTAINER_QNAME;
    }
    /** Unit testing use only! Used to avoid tests affecting the real system container */
    protected static void setSharedCredentialsSystemContainerName(String container)
    {
        SHARED_CREDENTIALS_CONTAINER_NAME = container;
        SHARED_CREDENTIALS_CONTAINER_QNAME = 
            QName.createQName(RemoteCredentialsModel.REMOTE_CREDENTIALS_MODEL_URL, SHARED_CREDENTIALS_CONTAINER_NAME);
    }
}