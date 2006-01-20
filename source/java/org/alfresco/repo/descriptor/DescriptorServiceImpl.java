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
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.service.cmr.repository.InvalidStoreRefException;
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
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;


/**
 * Implementation of Descriptor Service
 * 
 * @author David Caruana
 */
public class DescriptorServiceImpl implements DescriptorService, ApplicationListener
{
    private Properties serverProperties;
    
    private ImporterBootstrap systemBootstrap;
    private NamespaceService namespaceService;
    private NodeService nodeService;
    private SearchService searchService;
    private TransactionService transactionService;

    private Descriptor serverDescriptor;
    private Descriptor repoDescriptor;
    
    
    // Logger
    private static final Log logger = LogFactory.getLog(DescriptorService.class);

    
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
    public Descriptor getRepositoryDescriptor()
    {
        return repoDescriptor;
    }

    /**
     * Initialise Descriptors
     */
    public void init()
    {
        // initialise descriptors
        serverDescriptor = createServerDescriptor();
        repoDescriptor = TransactionUtil.executeInUserTransaction(transactionService, new TransactionUtil.TransactionWork<Descriptor>()
        {
            public Descriptor doWork()
            {
                return createRepositoryDescriptor();
            }
        });
    }
    
    /**
     * @param event
     */
    public void onApplicationEvent(ApplicationEvent event)
    {
        if (event instanceof ContextRefreshedEvent)
        {
            if (serverDescriptor != null)
            {
                // log output of VM stats
                Map properties = System.getProperties();
                String version = (properties.get("java.runtime.version") == null) ? "unknown" : (String)properties.get("java.runtime.version");
                long maxHeap = Runtime.getRuntime().maxMemory();
                float maxHeapMB = maxHeap / 1024l;
                maxHeapMB = maxHeapMB / 1024l;
                if (logger.isInfoEnabled())
                {
                    logger.info(String.format("Alfresco JVM - v%s; maximum heap size %.3fMB", version, maxHeapMB));
                }
                if (logger.isWarnEnabled())
                {
                    if (version.startsWith("1.2") || version.startsWith("1.3") || version.startsWith("1.4"))
                    {
                        logger.warn(String.format("Alfresco JVM - WARNING - v1.5 is required; currently using v%s", version));
                    }
                    if (maxHeapMB < 500)
                    {
                        logger.warn(String.format("Alfresco JVM - WARNING - maximum heap size %.3fMB is less than recommended 512MB", maxHeapMB));
                    }
                }
                
                // log output of version initialised
                if (logger.isInfoEnabled())
                {
                    String serverEdition = serverDescriptor.getEdition();
                    String serverVersion = serverDescriptor.getVersion();
                    String repoVersion = repoDescriptor.getVersion();
                    int schemaVersion = repoDescriptor.getSchema();
                    logger.info(String.format("Alfresco started (%s) - v%s; repository v%s; schema %d",
                            serverEdition, serverVersion, repoVersion, schemaVersion));
                }
            }
        }
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
    private Descriptor createRepositoryDescriptor()
    {
        // retrieve system descriptor location
        StoreRef storeRef = systemBootstrap.getStoreRef();
        Properties systemProperties = systemBootstrap.getConfiguration();
        String path = systemProperties.getProperty("system.descriptor.childname");

        // retrieve system descriptor
        NodeRef descriptorRef = null;
        try
        {
            NodeRef rootNode = nodeService.getRootNode(storeRef);
            List<NodeRef> nodeRefs = searchService.selectNodes(rootNode, "/" + path, null, namespaceService, false);
            if (nodeRefs.size() > 0)
            {
                descriptorRef = nodeRefs.get(0);
            }
        }
        catch(InvalidStoreRefException e)
        {
            // handle as system descriptor not found
        }
        
        // create appropriate descriptor
        if (descriptorRef != null)
        {
            Map<QName, Serializable> properties = nodeService.getProperties(descriptorRef);
            return new RepositoryDescriptor(properties);
        }

        // descriptor cannot be found
        return new UnknownDescriptor(); 
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
            return "unknown";
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionMinor()
         */
        public String getVersionMinor()
        {
            return "unknown";
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionRevision()
         */
        public String getVersionRevision()
        {
            return "unknown";
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionLabel()
         */
        public String getVersionLabel()
        {
            return "unknown";
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersion()
         */
        public String getVersion()
        {
            return "unknown (pre 1.0.0 RC2)";
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getEdition()
         */
        public String getEdition()
        {
            return "unknown";
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
