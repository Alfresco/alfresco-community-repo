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
package org.alfresco.repo.descriptor;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
import org.alfresco.service.license.LicenseDescriptor;
import org.alfresco.service.license.LicenseException;
import org.alfresco.service.license.LicenseService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.AbstractLifecycleBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.io.Resource;


/**
 * Implementation of Descriptor Service
 * 
 * @author David Caruana
 */
public class DescriptorServiceImpl extends AbstractLifecycleBean implements DescriptorService, InitializingBean
{
    private static Log logger = LogFactory.getLog(DescriptorServiceImpl.class);
    
    private Properties serverProperties;
    
    private ImporterBootstrap systemBootstrap;
    private NamespaceService namespaceService;
    private NodeService nodeService;
    private SearchService searchService;
    private TransactionService transactionService;
    private LicenseService licenseService = null;

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

    /* (non-Javadoc)
     * @see org.alfresco.service.descriptor.DescriptorService#getLicenseDescriptor()
     */
    public LicenseDescriptor getLicenseDescriptor()
    {
        return (licenseService == null) ? null : licenseService.getLicense();
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // initialise the repository descriptor
        // note: this requires that the repository schema has already been initialised
        TransactionWork<Descriptor> createDescriptorWork = new TransactionUtil.TransactionWork<Descriptor>()
        {
            public Descriptor doWork()
            {
                // initialise license service (if installed)
                initialiseLicenseService();
                
                // verify license, but only if license component is installed
                licenseService.verifyLicense();
                
                // persist the server descriptor values
                updateCurrentRepositoryDescriptor(serverDescriptor);

                // return the repository installed descriptor
                return createInstalledRepositoryDescriptor();
            }
        };
        installedRepoDescriptor = TransactionUtil.executeInUserTransaction(transactionService, createDescriptorWork);
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
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
        nodeService.setProperty(currentDescriptorNodeRef, ContentModel.PROP_SYS_VERSION_MAJOR, serverDescriptor.getVersionMajor());
        nodeService.setProperty(currentDescriptorNodeRef, ContentModel.PROP_SYS_VERSION_MINOR, serverDescriptor.getVersionMinor());
        nodeService.setProperty(currentDescriptorNodeRef, ContentModel.PROP_SYS_VERSION_REVISION, serverDescriptor.getVersionRevision());
        nodeService.setProperty(currentDescriptorNodeRef, ContentModel.PROP_SYS_VERSION_LABEL, serverDescriptor.getVersionLabel());
        nodeService.setProperty(currentDescriptorNodeRef, ContentModel.PROP_SYS_VERSION_BUILD, serverDescriptor.getVersionBuild());
        nodeService.setProperty(currentDescriptorNodeRef, ContentModel.PROP_SYS_VERSION_SCHEMA, serverDescriptor.getSchema());
        
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
        NodeRef descriptorNodeRef = null;
        String searchPath = "/" + path;

        // check for the store
        if (nodeService.exists(storeRef))
        {
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
                storeRef = nodeService.createStore(storeRef.getProtocol(), storeRef.getIdentifier());
                NodeRef rootNodeRef = nodeService.getRootNode(storeRef);                
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
     * Initialise License Service
     */
    private void initialiseLicenseService()
    {
        try
        {
            // NOTE: We could tie in the License Component via Spring configuration, but then it could
            //       be declaratively taken out in an installed environment.
            Class licenseComponentClass = Class.forName("org.alfresco.license.LicenseComponent");
            Constructor constructor = licenseComponentClass.getConstructor(new Class[] { ApplicationContext.class} );
            licenseService = (LicenseService)constructor.newInstance(new Object[] { getApplicationContext() });            
        }
        catch (ClassNotFoundException e)
        {
            licenseService = new NOOPLicenseService();
        }
        catch (SecurityException e)
        {
            throw new AlfrescoRuntimeException("Failed to initialise license service", e);
        }
        catch (IllegalArgumentException e)
        {
            throw new AlfrescoRuntimeException("Failed to initialise license service", e);
        }
        catch (NoSuchMethodException e)
        {
            throw new AlfrescoRuntimeException("Failed to initialise license service", e);
        }
        catch (InvocationTargetException e)
        {
            throw new AlfrescoRuntimeException("Failed to initialise license service", e);
        }
        catch (InstantiationException e)
        {
            throw new AlfrescoRuntimeException("Failed to initialise license service", e);
        }
        catch (IllegalAccessException e)
        {
            throw new AlfrescoRuntimeException("Failed to initialise license service", e);
        }
    }

    /**
     * Dummy License Service
     */
    private class NOOPLicenseService implements LicenseService
    {
        /* (non-Javadoc)
         * @see org.alfresco.service.license.LicenseService#verify()
         */
        public void verifyLicense() throws LicenseException
        {
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.license.LicenseService#getLicense()
         */
        public LicenseDescriptor getLicense() throws LicenseException
        {
            return null;
        }
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
         * @see org.alfresco.service.descriptor.Descriptor#getVersionBuild()
         */
        public String getVersionBuild()
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
     * Base class for Descriptor implementations, provides a 
     * default getVersion() implementation.
     * 
     * @author gavinc
     */
    public abstract class BaseDescriptor implements Descriptor
    {
        /* (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersion()
         */
        public String getVersion()
        {
            StringBuilder version = new StringBuilder(getVersionMajor());
            version.append(".");
            version.append(getVersionMinor());
            version.append(".");
            version.append(getVersionRevision());
            
            String label = getVersionLabel();
            String build = getVersionBuild();
            
            boolean hasLabel = (label != null && label.length() > 0);
            boolean hasBuild = (build != null && build.length() > 0);
            
            // add opening bracket if either a label or build number is present
            if (hasLabel || hasBuild)
            {
               version.append(" (");
            }
            
            // add label if present
            if (hasLabel)
            {
               version.append(label);
            }
            
            // add build number is present
            if (hasBuild)
            {
               // if there is also a label we need a separating space
               if (hasLabel)
               {
                  version.append(" ");
               }
               
               version.append(build);
            }
            
            // add closing bracket if either a label or build number is present
            if (hasLabel || hasBuild)
            {
               version.append(")");
            }
            
            return version.toString();
        }
        
        /**
         * Returns the int representation of the given schema string
         * 
         * @param schemaStr The schema number as a string
         * @return The schema number as an int
         */
        protected int getSchema(String schemaStr)
        {
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
                throw new AlfrescoRuntimeException("Schema must be a positive integer '" + schemaStr + "' is not!");
            }
        }
    }
    
    /**
     * Repository Descriptor whose meta-data is retrieved from the repository store
     */
    private class RepositoryDescriptor extends BaseDescriptor
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
         * @see org.alfresco.service.descriptor.Descriptor#getVersionBuild()
         */
        public String getVersionBuild()
        {
            return getDescriptor("sys:versionBuild");
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
            return getSchema(getDescriptor("sys:versionSchema"));
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
    private class ServerDescriptor extends BaseDescriptor
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
         * @see org.alfresco.service.descriptor.Descriptor#getVersionBuild()
         */
        public String getVersionBuild()
        {
            return serverProperties.getProperty("version.build");
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
            return getSchema(serverProperties.getProperty("version.schema"));
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
