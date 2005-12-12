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
package org.alfresco.repo.importer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.view.ImporterBinding;
import org.alfresco.service.cmr.view.ImporterException;
import org.alfresco.service.cmr.view.ImporterProgress;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.cmr.view.ImporterBinding.UUID_BINDING;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bootstrap Repository store.
 * 
 * @author David Caruana
 */
public class ImporterBootstrap
{
    // View Properties (used in setBootstrapViews)
    public static final String VIEW_PATH_PROPERTY = "path";
    public static final String VIEW_CHILDASSOCTYPE_PROPERTY = "childAssocType";
    public static final String VIEW_MESSAGES_PROPERTY = "messages";
    public static final String VIEW_LOCATION_VIEW = "location";
    public static final String VIEW_ENCODING = "encoding";
    
    // Logger
    private static final Log logger = LogFactory.getLog(ImporterBootstrap.class);

    // Dependencies
    private boolean allowWrite = true;
    private TransactionService transactionService;
    private NamespaceService namespaceService;
    private NodeService nodeService;
    private ImporterService importerService;
    private List<Properties> bootstrapViews;
    private StoreRef storeRef = null;
    private List<String> mustNotExistStoreUrls = null;
    private Properties configuration = null;
    private String strLocale = null;
    private Locale locale = null;
    private AuthenticationComponent authenticationComponent;
    
    /**
     * Set whether we write or not
     * 
     * @param write true (default) if the import must go ahead, otherwise no import will occur
     */
    public void setAllowWrite(boolean write)
    {
        this.allowWrite = write;
    }

    /**
     * Sets the Transaction Service
     * 
     * @param userTransaction the transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    /**
     * Sets the namespace service
     * 
     * @param namespaceService the namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * Sets the node service
     * 
     * @param nodeService the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Sets the importer service
     * 
     * @param importerService the importer service
     */
    public void setImporterService(ImporterService importerService)
    {
        this.importerService = importerService;
    }
        
    /**
     * Set the authentication component
     * 
     * @param authenticationComponent
     */
    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }

    /**
     * Sets the bootstrap views
     * 
     * @param bootstrapViews
     */
    public void setBootstrapViews(List<Properties> bootstrapViews)
    {
        this.bootstrapViews = bootstrapViews;
    }

    /**
     * Sets the Store Ref to bootstrap into
     * 
     * @param storeUrl
     */
    public void setStoreUrl(String storeUrl)
    {
        this.storeRef = new StoreRef(storeUrl);
    }

    /**
     * If any of the store urls exist, the bootstrap does not take place
     * 
     * @param storeUrls  the list of store urls to check
     */
    public void setMustNotExistStoreUrls(List<String> storeUrls)
    {
        this.mustNotExistStoreUrls = storeUrls;
    }
    
    /**
     * Gets the Store Reference
     * 
     * @return store reference
     */
    public StoreRef getStoreRef()
    {
        return this.storeRef;
    }
    
    /**
     * Sets the Configuration values for binding place holders
     * 
     * @param configuration
     */
    public void setConfiguration(Properties configuration)
    {
        this.configuration = configuration;
    }

    /**
     * Gets the Configuration values for binding place holders
     * 
     * @return configuration
     */
    public Properties getConfiguration()
    {
        return configuration;
    }
    
    /**
     * Sets the Locale
     * 
     * @param locale  (language_country_variant)
     */
    public void setLocale(String locale)
    {
        // construct locale
        StringTokenizer t = new StringTokenizer(locale, "_");
        int tokens = t.countTokens();
        if (tokens == 1)
        {
           this.locale = new Locale(locale);
        }
        else if (tokens == 2)
        {
           this.locale = new Locale(t.nextToken(), t.nextToken());
        }
        else if (tokens == 3)
        {
           this.locale = new Locale(t.nextToken(), t.nextToken(), t.nextToken());
        }        

        // store original
        strLocale = locale;
    }

    /**
     * Get Locale
     * 
     * @return  locale
     */
    public String getLocale()
    {
        return strLocale; 
    }
    
    /**
     * Boostrap the Repository
     */
    public void bootstrap()
    {
        if (transactionService == null)
        {
            throw new ImporterException("Transaction Service must be provided");
        }
        if (namespaceService == null)
        {
            throw new ImporterException("Namespace Service must be provided");
        }
        if (nodeService == null)
        {
            throw new ImporterException("Node Service must be provided");
        }
        if (importerService == null)
        {
            throw new ImporterException("Importer Service must be provided");
        }
        if (storeRef == null)
        {
            throw new ImporterException("Store URL must be provided");
        }
        
        UserTransaction userTransaction = transactionService.getUserTransaction();
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        try
        {
            userTransaction.begin();
        
            // check the repository exists, create if it doesn't
            if (!performBootstrap())
            {
                if (logger.isDebugEnabled())
                    logger.debug("Store exists - bootstrap ignored: " + storeRef);
                
                userTransaction.rollback();
            }
            else if (!allowWrite)
            {
                // we're in read-only node
                logger.warn("Store does not exist, but mode is read-only: " + storeRef);
                userTransaction.rollback();
            }
            else
            {
                // create the store           
                storeRef = nodeService.createStore(storeRef.getProtocol(), storeRef.getIdentifier());
       
                if (logger.isDebugEnabled())
                    logger.debug("Created store: " + storeRef);
    
                // bootstrap the store contents
                if (bootstrapViews != null)
                {
                    for (Properties bootstrapView : bootstrapViews)
                    {
                        // Create input stream reader onto view file
                        String view = bootstrapView.getProperty(VIEW_LOCATION_VIEW);
                        if (view == null || view.length() == 0)
                        {
                            throw new ImporterException("View file location must be provided");
                        }
                        String encoding = bootstrapView.getProperty(VIEW_ENCODING);
                        Reader viewReader = getReader(view, encoding);
                        
                        // Create import location
                        Location importLocation = new Location(storeRef);
                        String path = bootstrapView.getProperty(VIEW_PATH_PROPERTY);
                        if (path != null && path.length() > 0)
                        {
                            importLocation.setPath(path);
                        }
                        String childAssocType = bootstrapView.getProperty(VIEW_CHILDASSOCTYPE_PROPERTY);
                        if (childAssocType != null && childAssocType.length() > 0)
                        {
                            importLocation.setChildAssocType(QName.createQName(childAssocType, namespaceService));
                        }
                        
                        // Create import binding
                        BootstrapBinding binding = new BootstrapBinding();
                        binding.setConfiguration(configuration);
                        String messages = bootstrapView.getProperty(VIEW_MESSAGES_PROPERTY);
                        if (messages != null && messages.length() > 0)
                        {
                            Locale bindingLocale = (locale == null) ? I18NUtil.getLocale() : locale;
                            ResourceBundle bundle = ResourceBundle.getBundle(messages, bindingLocale);
                            binding.setResourceBundle(bundle);
                        }
            
                        // Now import...
                        importerService.importView(viewReader, importLocation, binding, new BootstrapProgress());
                    }
                }
                
                userTransaction.commit();
            }
        }
        catch(Throwable e)
        {
            // rollback the transaction
            try { if (userTransaction != null) {userTransaction.rollback();} } catch (Exception ex) {}
            try {authenticationComponent.clearCurrentSecurityContext(); } catch (Exception ex) {}
            throw new AlfrescoRuntimeException("Bootstrap failed", e);
        }
        finally
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
    }
    
    /**
     * Get a Reader onto an XML view
     * 
     * @param view  the view location
     * @param encoding  the encoding of the view
     * @return  the reader
     */
    private Reader getReader(String view, String encoding)
    {
        // Get Input Stream
        InputStream viewStream = getClass().getClassLoader().getResourceAsStream(view);
        if (viewStream == null)
        {
            throw new ImporterException("Could not find view file " + view);
        }

        // Wrap in buffered reader
        try
        {
            InputStreamReader inputReader = (encoding == null) ? new InputStreamReader(viewStream) : new InputStreamReader(viewStream, encoding);
            BufferedReader reader = new BufferedReader(inputReader);
            return reader;
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ImporterException("Could not create reader for view " + view + " as encoding " + encoding + " is not supported");
        }
    }
    
    /**
     * Bootstrap Binding
     */
    private class BootstrapBinding implements ImporterBinding
    {
        private Properties configuration = null;
        private ResourceBundle resourceBundle = null;
        
        /**
         * Set Import Configuration
         * 
         * @param configuration
         */
        public void setConfiguration(Properties configuration)
        {
            this.configuration = configuration;
        }

        /**
         * Get Import Configuration
         * 
         * @return  configuration
         */
        public Properties getConfiguration()
        {
            return this.configuration;
        }
        
        /**
         * Set Resource Bundle
         * 
         * @param resourceBundle
         */
        public void setResourceBundle(ResourceBundle resourceBundle)
        {
            this.resourceBundle = resourceBundle;
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterBinding#getValue(java.lang.String)
         */
        public String getValue(String key)
        {
            String value = null;
            if (configuration != null)
            {
                value = configuration.getProperty(key);
            }
            if (value == null && resourceBundle != null)
            {
                value = resourceBundle.getString(key);
            }
            return value;
        }

        /*
         *  (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterBinding#getUUIDBinding()
         */
        public UUID_BINDING getUUIDBinding()
        {
            // always use create new strategy for bootstrap import
            return UUID_BINDING.CREATE_NEW;
        }
    }
    
    /**
     * Bootstrap Progress (debug logging)
     */
    private class BootstrapProgress implements ImporterProgress
    {
        /* (non-Javadoc)
         * @see org.alfresco.repo.importer.Progress#nodeCreated(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, org.alfresco.service.namespace.QName)
         */
        public void nodeCreated(NodeRef nodeRef, NodeRef parentRef, QName assocName, QName childName)
        {
            if (logger.isDebugEnabled())
                logger.debug("Created node " + nodeRef + " (child name: " + childName + ") within parent " + parentRef + " (association type: " + assocName + ")");
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.importer.Progress#contentCreated(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
         */
        public void contentCreated(NodeRef nodeRef, String sourceUrl)
        {
            if (logger.isDebugEnabled())
                logger.debug("Imported content from " + sourceUrl + " into node " + nodeRef);
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.importer.Progress#propertySet(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.io.Serializable)
         */
        public void propertySet(NodeRef nodeRef, QName property, Serializable value)
        {
            if (logger.isDebugEnabled())
                logger.debug("Property " + property + " set to value " + value + " on node " + nodeRef);
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.importer.Progress#aspectAdded(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
         */
        public void aspectAdded(NodeRef nodeRef, QName aspect)
        {
            if (logger.isDebugEnabled())
                logger.debug("Added aspect " + aspect + " to node " + nodeRef);
        }

    }

    /**
     * Determine if bootstrap should take place
     * 
     * @return  true => yes, it should
     */
    private boolean performBootstrap()
    {
        if (nodeService.exists(storeRef))
        {
            return false;
        }
        
        if (mustNotExistStoreUrls != null)
        {
            for (String storeUrl : mustNotExistStoreUrls)
            {
                StoreRef storeRef = new StoreRef(storeUrl);
                if (nodeService.exists(storeRef))
                {
                    return false;
                }
            }
        }
        
        return true;
    }
    
}
