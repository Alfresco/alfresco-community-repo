/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.descriptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.descriptor.DescriptorServiceImpl.BaseDescriptor;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.license.LicenseException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Manages persistence and retrieval of Descriptors whose meta-data are retrieved from the repository stores.
 * 
 * @author dward
 */
public class RepositoryDescriptorDAOImpl implements DescriptorDAO
{

    /** The logger. */
    private static Log logger = LogFactory.getLog(RepositoryDescriptorDAOImpl.class);

    /** The name. */
    private String name;

    /** The node service. */
    private NodeService nodeService;

    /** The content service. */
    private ContentService contentService;

    /** The search service. */
    private SearchService searchService;

    /** The namespace service. */
    private NamespaceService namespaceService;

    /** The system bootstrap. */
    private ImporterBootstrap systemBootstrap;

    /** The transaction service. */
    private TransactionService transactionService;

    /**
     * Sets the name.
     * 
     * @param name
     *            the new name
     */
    public void setName(final String name)
    {
        this.name = name;
    }

    /**
     * Sets the node service.
     * 
     * @param nodeService
     *            the new node service
     */
    public void setNodeService(final NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Sets the content service.
     * 
     * @param contentService
     *            the new content service
     */
    public void setContentService(final ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * Sets the search service.
     * 
     * @param searchService
     *            the new search service
     */
    public void setSearchService(final SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * Sets the namespace service.
     * 
     * @param namespaceService
     *            the new namespace service
     */
    public void setNamespaceService(final NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * Sets the system bootstrap.
     * 
     * @param systemBootstrap
     *            the new system bootstrap
     */
    public void setSystemBootstrap(final ImporterBootstrap systemBootstrap)
    {
        this.systemBootstrap = systemBootstrap;
    }

    /**
     * Sets the transaction service.
     * 
     * @param transactionService
     *            the new transaction service
     */
    public void setTransactionService(final TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.descriptor.DescriptorPersistence#getDescriptor()
     */
    public Descriptor getDescriptor()
    {
        // retrieve system descriptor
        final NodeRef descriptorNodeRef = getDescriptorNodeRef(false);

        // create appropriate descriptor
        if (descriptorNodeRef != null)
        {
            final Map<QName, Serializable> properties = this.nodeService.getProperties(descriptorNodeRef);
            return new RepositoryDescriptor(properties);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.descriptor.DescriptorPersistence#updateDescriptor(org.alfresco.service.descriptor.Descriptor)
     */
    public Descriptor updateDescriptor(final Descriptor serverDescriptor)
    {
        final NodeRef currentDescriptorNodeRef = getDescriptorNodeRef(true);
        // if the node is missing but it should have been created
        if (currentDescriptorNodeRef == null)
        {
            return null;
        }
        // set the properties
        if (!this.transactionService.isReadOnly())
        {
            Map<QName, Serializable> props = new HashMap<QName, Serializable>(11);
            props.put(ContentModel.PROP_SYS_NAME, serverDescriptor.getName());
            props.put(ContentModel.PROP_SYS_VERSION_MAJOR, serverDescriptor.getVersionMajor());
            props.put(ContentModel.PROP_SYS_VERSION_MINOR, serverDescriptor.getVersionMinor());
            props.put(ContentModel.PROP_SYS_VERSION_REVISION, serverDescriptor.getVersionRevision());
            props.put(ContentModel.PROP_SYS_VERSION_LABEL, serverDescriptor.getVersionLabel());
            props.put(ContentModel.PROP_SYS_VERSION_BUILD, serverDescriptor.getVersionBuild());
            props.put(ContentModel.PROP_SYS_VERSION_SCHEMA, serverDescriptor.getSchema());
            this.nodeService.addProperties(currentDescriptorNodeRef, props);

            // The version edition property may already have been overwritten with a license, so only set the property
            // if it doesn't already contain ContentData
            final Serializable value = this.nodeService.getProperty(currentDescriptorNodeRef,
                    ContentModel.PROP_SYS_VERSION_EDITION);
            if (!(value instanceof Collection) || ((Collection<?>) value).isEmpty()
                    || ((Collection<?>) value).iterator().next() instanceof String)
            {
                final Collection<String> editions = new ArrayList<String>();
                editions.add(serverDescriptor.getEdition());
                this.nodeService.setProperty(currentDescriptorNodeRef, ContentModel.PROP_SYS_VERSION_EDITION,
                        (Serializable) editions);
            }

            // done
            if (RepositoryDescriptorDAOImpl.logger.isDebugEnabled())
            {
                RepositoryDescriptorDAOImpl.logger.debug("Updated current repository descriptor properties: \n"
                        + "   node: " + currentDescriptorNodeRef + "\n" + "   descriptor: " + serverDescriptor);
            }
        }

        final Map<QName, Serializable> properties = this.nodeService.getProperties(currentDescriptorNodeRef);
        return new RepositoryDescriptor(properties);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.descriptor.DescriptorPersistence#getLicenseKey()
     */
    public byte[] getLicenseKey()
    {
        byte[] key = null;

        try
        {
            final NodeRef descriptorRef = getDescriptorNodeRef(true);
            if (descriptorRef == null)
            {
                throw new LicenseException("Failed to find system descriptor");
            }
            final ContentReader reader = this.contentService.getReader(descriptorRef,
                    ContentModel.PROP_SYS_VERSION_EDITION);
            if (reader != null && reader.exists())
            {
                final ByteArrayOutputStream os = new ByteArrayOutputStream();
                reader.getContent(os);
                key = os.toByteArray();
            }
        }
        catch (final Exception e)
        {
            throw new LicenseException("Failed to load license", e);
        }
        return key;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.descriptor.DescriptorPersistence#updateLicenseKey(byte[])
     */
    public void updateLicenseKey(final byte[] key)
    {
        try
        {
            final NodeRef descriptorRef = getDescriptorNodeRef(true);
            if (descriptorRef == null)
            {
                throw new LicenseException("Failed to find system descriptor");
            }
            if (key == null)
            {
                this.nodeService.setProperty(descriptorRef, ContentModel.PROP_SYS_VERSION_EDITION, null);
            }
            else
            {
                final ContentWriter writer = this.contentService.getWriter(descriptorRef,
                        ContentModel.PROP_SYS_VERSION_EDITION, true);
                final InputStream is = new ByteArrayInputStream(key);
                writer.setMimetype(MimetypeMap.MIMETYPE_BINARY);
                writer.putContent(is);
            }
        }
        catch (final Exception e)
        {
            throw new LicenseException("Failed to save license", e);
        }
    }

    /**
     * Gets the descriptor node ref.
     * 
     * @param create
     *            the create
     * @return the descriptor node ref
     */
    private NodeRef getDescriptorNodeRef(final boolean create)
    {
        // retrieve system descriptor location
        StoreRef storeRef = this.systemBootstrap.getStoreRef();
        final Properties systemProperties = this.systemBootstrap.getConfiguration();
        final String path = systemProperties.getProperty(this.name);

        NodeRef descriptorNodeRef = null;
        final String searchPath = "/" + path;

        // check for the store
        if (this.nodeService.exists(storeRef))
        {
            final NodeRef rootNodeRef = this.nodeService.getRootNode(storeRef);
            final List<NodeRef> nodeRefs = this.searchService.selectNodes(rootNodeRef, searchPath, null,
                    this.namespaceService, false);
            if (nodeRefs.size() == 1)
            {
                descriptorNodeRef = nodeRefs.get(0);
            }
            else if (nodeRefs.size() == 0)
            {
            }
            else if (nodeRefs.size() > 1)
            {
                if (RepositoryDescriptorDAOImpl.logger.isDebugEnabled())
                {
                    RepositoryDescriptorDAOImpl.logger.debug("Multiple descriptors: \n" + "   store: " + storeRef
                            + "\n" + "   path: " + searchPath);
                }
                // get the first one
                descriptorNodeRef = nodeRefs.get(0);
            }
        }

        if (descriptorNodeRef == null)
        {
            if (RepositoryDescriptorDAOImpl.logger.isDebugEnabled())
            {
                RepositoryDescriptorDAOImpl.logger.debug("Descriptor not found: \n" + "   store: " + storeRef + "\n"
                        + "   path: " + searchPath);
            }

            // create if necessary
            if (create)
            {
                storeRef = this.nodeService.createStore(storeRef.getProtocol(), storeRef.getIdentifier());
                final NodeRef rootNodeRef = this.nodeService.getRootNode(storeRef);
                descriptorNodeRef = this.nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                        QName.createQName(path, this.namespaceService),
                        QName.createQName("sys:descriptor", this.namespaceService)).getChildRef();
                if (RepositoryDescriptorDAOImpl.logger.isDebugEnabled())
                {
                    RepositoryDescriptorDAOImpl.logger.debug("Created missing descriptor node: " + descriptorNodeRef);
                }
            }
        }
        return descriptorNodeRef;
    }

    /**
     * Repository Descriptor whose meta-data is retrieved from the repository store.
     */
    private class RepositoryDescriptor extends BaseDescriptor
    {

        /** The properties. */
        private final Map<QName, Serializable> properties;

        /**
         * Construct.
         * 
         * @param properties
         *            system descriptor properties
         */
        private RepositoryDescriptor(final Map<QName, Serializable> properties)
        {
            this.properties = properties;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getId()
         */
        public String getId()
        {
            return getDescriptor("sys:node-uuid");
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getName()
         */
        public String getName()
        {
            return getDescriptor("sys:name");
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionMajor()
         */
        public String getVersionMajor()
        {
            return getDescriptor("sys:versionMajor");
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionMinor()
         */
        public String getVersionMinor()
        {
            return getDescriptor("sys:versionMinor");
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionRevision()
         */
        public String getVersionRevision()
        {
            return getDescriptor("sys:versionRevision");
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionLabel()
         */
        public String getVersionLabel()
        {
            return getDescriptor("sys:versionLabel");
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionBuild()
         */
        public String getVersionBuild()
        {
            return getDescriptor("sys:versionBuild");
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getEdition()
         */
        public String getEdition()
        {
            return getDescriptor("sys:versionEdition");
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getSchema()
         */
        public int getSchema()
        {
            return getSchema(getDescriptor("sys:versionSchema"));
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getDescriptorKeys()
         */
        public String[] getDescriptorKeys()
        {
            final String[] keys = new String[this.properties.size()];
            this.properties.keySet().toArray(keys);
            return keys;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getDescriptor(java.lang.String)
         */
        public String getDescriptor(final String key)
        {
            String strValue = null;
            final QName qname = QName.createQName(key, RepositoryDescriptorDAOImpl.this.namespaceService);
            final Serializable value = this.properties.get(qname);
            if (value != null)
            {
                if (value instanceof Collection)
                {
                    final Collection<?> coll = (Collection<?>) value;
                    if (coll.size() > 0)
                    {
                        strValue = coll.iterator().next().toString();
                    }
                }
                else
                {
                    strValue = value.toString();
                }
            }
            return strValue;
        }
    }

}
