/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.descriptor;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;


/**
 * Implementation of Descriptor Service
 * 
 * @author David Caruana
 */
public class DescriptorServiceImpl implements DescriptorService, ApplicationListener, InitializingBean
{
    private static Log logger = LogFactory.getLog(DescriptorServiceImpl.class);
    
    private Properties serverProperties;
    
    private ImporterBootstrap systemBootstrap;
    private NamespaceService namespaceService;
    private NodeService nodeService;
    private SearchService searchService;
    private TransactionService transactionService;

    private Descriptor serverDescriptor;
    private Descriptor installedRepoDescriptor;
    
    
    
    /**
     * Sets the server descriptor from a resource file
     * 
     * @param descriptorResource  resource containing server descriptor meta-data
     * @throws IOException
     */
    public void setDescriptor(Resource descriptorResource)
        throws IOException
    {
        this.serverProperties = new Properties();
        this.serverProperties.load(descriptorResource.getInputStream());
    }

    /**
     * @param systemBootstrap  system bootstrap
     */
    public void setSystemBootstrap(ImporterBootstrap systemBootstrap)
    {
        this.systemBootstrap = systemBootstrap;
    }
    
    /**
     * @param transactionService  transaction service 
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    /**
     * @param namespaceService  namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @param nodeService  node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @param searchService  search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.descriptor.DescriptorService#getDescriptor()
     */
    public Descriptor getServerDescriptor()
    {
        return serverDescriptor;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.descriptor.DescriptorService#getRepositoryDescriptor()
     */
    public Descriptor getInstalledRepositoryDescriptor()
    {
        return installedRepoDescriptor;
    }

    /**
     * @param event
     */
    public void onApplicationEvent(ApplicationEvent event)
    {
        if (event instanceof ContextRefreshedEvent)
        {
            // initialise the repository descriptor
            // note: this requires that the repository schema has already been initialised
            TransactionWork<Descriptor> createDescriptorWork = new TransactionUtil.TransactionWork<Descriptor>()
            {
                public Descriptor doWork()
                {
                    // persist the server descriptor values
                    updateCurrentRepositoryDescriptor(serverDescriptor);
                    
                    // return the repository installed descriptor
                    return createInstalledRepositoryDescriptor();
                }
            };
            installedRepoDescriptor = TransactionUtil.executeInUserTransaction(transactionService, createDescriptorWork);
        }
    }

    /**
     * Initialise Descriptors
     */
    public void afterPropertiesSet() throws Exception
    {
        // initialise server descriptor
        serverDescriptor = createServerDescriptor();
    }

    /**
     * Create server descriptor
     * 
     * @return  descriptor
     */
    private Descriptor createServerDescriptor()
    {
        return new ServerDescriptor();
    }
    
    /**
     * Create repository descriptor
     * 
     * @return  descriptor
     */
    private Descriptor createInstalledRepositoryDescriptor()
    {
        // retrieve system descriptor location
        StoreRef storeRef = systemBootstrap.getStoreRef();
        Properties systemProperties = systemBootstrap.getConfiguration();
        String path = systemProperties.getProperty("system.descriptor.childname");

        // retrieve system descriptor
        NodeRef descriptorNodeRef = getDescriptorNodeRef(storeRef, path, false);
        // create appropriate descriptor
        if (descriptorNodeRef != null)
        {
            Map<QName, Serializable> properties = nodeService.getProperties(descriptorNodeRef);
            return new RepositoryDescriptor(properties);
        }
        else
        {
            // descriptor cannot be found
            return new UnknownDescriptor();
        }
    }
    
    /**
     * Push the current server descriptor properties into persistence.
     * 
     * @param serverDescriptor the current server descriptor
     */
    private void updateCurrentRepositoryDescriptor(Descriptor serverDescriptor)
    {
        // retrieve system descriptor location
        StoreRef storeRef = systemBootstrap.getStoreRef();
        Properties systemProperties = systemBootstrap.getConfiguration();
        String path = systemProperties.getProperty("system.descriptor.current.childname");

        // retrieve system descriptor
        NodeRef currentDescriptorNodeRef = getDescriptorNodeRef(storeRef, path, true);
        // if the node is missing but it should have been created
        if (currentDescriptorNodeRef == null)
        {
            return;
        }
        // set the properties
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(17);
        properties.put(ContentModel.PROP_SYS_VERSION_MAJOR, serverDescriptor.getVersionMajor());
        properties.put(ContentModel.PROP_SYS_VERSION_MINOR, serverDescriptor.getVersionMinor());
        properties.put(ContentModel.PROP_SYS_VERSION_REVISION, serverDescriptor.getVersionRevision());
        properties.put(ContentModel.PROP_SYS_VERSION_LABEL, serverDescriptor.getVersionLabel());
        properties.put(ContentModel.PROP_SYS_VERSION_SCHEMA, serverDescriptor.getSchema());
        nodeService.setProperties(currentDescriptorNodeRef, properties);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Updated current repository descriptor properties: \n" +
                    "   node: " + currentDescriptorNodeRef + "\n" +
                    "   descriptor: " + serverDescriptor);
        }
    }
    
    /**
     * 
     * @param storeRef the store to search
     * @param path the path below the root node to search
     * @param create true if the node must be created if missing.  No properties will be set.
     * @return Returns the node for the path, or null
     */
    private NodeRef getDescriptorNodeRef(StoreRef storeRef, String path, boolean create)
    {
        // check for the store and create if necessary
        if (!nodeService.exists(storeRef) && create)
        {
            storeRef = nodeService.createStore(storeRef.getProtocol(), storeRef.getIdentifier());
        }
        
        String searchPath = "/" + path;
        NodeRef descriptorNodeRef = null;
        NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
        List<NodeRef> nodeRefs = searchService.selectNodes(rootNodeRef, searchPath, null, namespaceService, false);
        if (nodeRefs.size() == 1)
        {
            descriptorNodeRef = nodeRefs.get(0);
        }
        else if (nodeRefs.size() == 0)
        {
        }
        else if (nodeRefs.size() > 1)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Multiple descriptors: \n" +
                        "   store: " + storeRef + "\n" +
                        "   path: " + searchPath);
            }
            // get the first one
            descriptorNodeRef = nodeRefs.get(0);
        }
        
        if (descriptorNodeRef == null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Descriptor not found: \n" +
                        "   store: " + storeRef + "\n" +
                        "   path: " + searchPath);
            }
            // create if necessary
            if (create)
            {
                descriptorNodeRef = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        QName.createQName(path, namespaceService),
                        QName.createQName("sys:descriptor", namespaceService)).getChildRef();
                if (logger.isDebugEnabled())
                {
                    logger.debug("Created missing descriptor node: " + descriptorNodeRef);
                }
            }
        }
        return descriptorNodeRef;
    }
    
    /**
     * Unknown descriptor
     * 
     * @author David Caruana
     */    
    private class UnknownDescriptor implements Descriptor
    {
        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionMajor()
         */
        public String getVersionMajor()
        {
            return "Unknown";
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionMinor()
         */
        public String getVersionMinor()
        {
            return "Unknown";
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionRevision()
         */
        public String getVersionRevision()
        {
            return "Unknown";
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionLabel()
         */
        public String getVersionLabel()
        {
            return "Unknown";
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersion()
         */
        public String getVersion()
        {
            return "Unknown (pre 1.0.0 RC2)";
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getEdition()
         */
        public String getEdition()
        {
            return "Unknown";
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getSchema()
         */
        public int getSchema()
        {
            return 0;
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getDescriptorKeys()
         */
        public String[] getDescriptorKeys()
        {
            return new String[0];
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getDescriptor(java.lang.String)
         */
        public String getDescriptor(String key)
        {
            return null;
        }
    }
    
    /**
     * Repository Descriptor whose meta-data is retrieved from the repository store
     */
    private class RepositoryDescriptor implements Descriptor
    {
        private Map<QName, Serializable> properties;
        
        
        /**
         * Construct
         * 
         * @param properties  system descriptor properties
         */
        private RepositoryDescriptor(Map<QName, Serializable> properties)
        {
            this.properties = properties;
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionMajor()
         */
        public String getVersionMajor()
        {
            return getDescriptor("sys:versionMajor");
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionMinor()
         */
        public String getVersionMinor()
        {
            return getDescriptor("sys:versionMinor");
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionRevision()
         */
        public String getVersionRevision()
        {
            return getDescriptor("sys:versionRevision");
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionLabel()
         */
        public String getVersionLabel()
        {
            return getDescriptor("sys:versionLabel");
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersion()
         */
        public String getVersion()
        {
            String version = getVersionMajor() + "." + getVersionMinor() + "." + getVersionRevision();
            String label = getVersionLabel();
            if (label != null && label.length() > 0)
            {
                version += " (" + label + ")";
            }
            return version;
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getEdition()
         */
        public String getEdition()
        {
            return null;
        }
        
        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getSchema()
         */
        public int getSchema()
        {
            String schemaStr = getDescriptor("sys:versionSchema");
            if (schemaStr == null)
            {
                return 0;
            }
            try
            {
                int schema = Integer.parseInt(schemaStr);
                if (schema < 0)
                {
                    throw new NumberFormatException();
                }
                return schema;
            }
            catch (NumberFormatException e)
            {
                throw new AlfrescoRuntimeException("'version.schema' must be a positive integer");
            }
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getDescriptorKeys()
         */
        public String[] getDescriptorKeys()
        {
            String[] keys = new String[properties.size()];
            properties.keySet().toArray(keys);
            return keys;
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getDescriptor(java.lang.String)
         */
        public String getDescriptor(String key)
        {
            String strValue = null;
            QName qname = QName.createQName(key, namespaceService);
            Serializable value = properties.get(qname);
            if (value != null)
            {
                strValue = value.toString();
            }
            return strValue;
        }
    }
    
    /**
     * Server Descriptor whose meta-data is retrieved from run-time environment 
     */
    private class ServerDescriptor implements Descriptor
    {
        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionMajor()
         */
        public String getVersionMajor()
        {
            return serverProperties.getProperty("version.major");
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionMinor()
         */
        public String getVersionMinor()
        {
            return serverProperties.getProperty("version.minor");
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionRevision()
         */
        public String getVersionRevision()
        {
            return serverProperties.getProperty("version.revision");
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionLabel()
         */
        public String getVersionLabel()
        {
            return serverProperties.getProperty("version.label");
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersion()
         */
        public String getVersion()
        {
            String version = getVersionMajor() + "." + getVersionMinor() + "." + getVersionRevision();
            String label = getVersionLabel();
            if (label != null && label.length() > 0)
            {
                version += " (" + label + ")";
            }
            return version;
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getEdition()
         */
        public String getEdition()
        {
            return serverProperties.getProperty("version.edition");
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getSchema()
         */
        public int getSchema()
        {
            String schemaStr = serverProperties.getProperty("version.schema");
            if (schemaStr == null)
            {
                return 0;
            }
            try
            {
                int schema = Integer.parseInt(schemaStr);
                if (schema < 0)
                {
                    throw new NumberFormatException();
                }
                return schema;
            }
            catch (NumberFormatException e)
            {
                throw new AlfrescoRuntimeException("'version.schema' must be a positive integer");
            }
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getDescriptorKeys()
         */
        public String[] getDescriptorKeys()
        {
            String[] keys = new String[serverProperties.size()];
            serverProperties.keySet().toArray(keys);
            return keys;
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getDescriptor(java.lang.String)
         */
        public String getDescriptor(String key)
        {
            return serverProperties.getProperty(key, "");
        }
    }

}
