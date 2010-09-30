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
package org.alfresco.repo.audit.model;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.repo.audit.extractor.DataExtractor;
import org.alfresco.repo.audit.generator.DataGenerator;
import org.alfresco.repo.audit.model._3.Application;
import org.alfresco.repo.audit.model._3.Audit;
import org.alfresco.repo.audit.model._3.DataExtractors;
import org.alfresco.repo.audit.model._3.DataGenerators;
import org.alfresco.repo.audit.model._3.ObjectFactory;
import org.alfresco.repo.audit.model._3.PathMap;
import org.alfresco.repo.audit.model._3.PathMappings;
import org.alfresco.repo.domain.audit.AuditDAO;
import org.alfresco.repo.domain.audit.AuditDAO.AuditApplicationInfo;
import org.alfresco.repo.management.subsystems.AbstractPropertyBackedBean;
import org.alfresco.repo.management.subsystems.PropertyBackedBeanState;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PathMapper;
import org.alfresco.util.ResourceFinder;
import org.alfresco.util.registry.NamedObjectRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.alfresco.util.PropertyCheck;
import org.springframework.util.ResourceUtils;
import org.xml.sax.SAXParseException;

/**
 * Component used to store audit model definitions. It ensures that duplicate application and converter definitions are
 * detected and provides a single lookup for code using the Audit model. It is factored as a subsystem and exposes a
 * global enablement property plus enablement properties for each individual audit application.
 * 
 * @author Derek Hulley
 * @author dward
 * @since 3.2
 */
public class AuditModelRegistryImpl extends AbstractPropertyBackedBean implements AuditModelRegistry
{
    private static final Log logger = LogFactory.getLog(AuditModelRegistryImpl.class);
    
    private String[] searchPath;
    private TransactionService transactionService;
    private AuditDAO auditDAO;
    private NamedObjectRegistry<DataExtractor> dataExtractors;
    private NamedObjectRegistry<DataGenerator> dataGenerators;
    private final ObjectFactory objectFactory;
    
    /**
     * Default constructor.
     */
    public AuditModelRegistryImpl()
    {        
        objectFactory = new ObjectFactory();        
    }
    
    /**
     * Sets the search path for config files.
     */
    public void setSearchPath(String[] searchPath)
    {
        this.searchPath = searchPath;
    }

    /**
     * Service to ensure DAO calls are transactionally wrapped.
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * Set the DAO used to persisted the registered audit models.
     */
    public void setAuditDAO(AuditDAO auditDAO)
    {
        this.auditDAO = auditDAO;
    }

    /**
     * Set the registry of {@link DataExtractor data extractors}.
     */
    public void setDataExtractors(NamedObjectRegistry<DataExtractor> dataExtractors)
    {
        this.dataExtractors = dataExtractors;
    }

    /**
     * Set the registry of {@link DataGenerator data generators}.
     */
    public void setDataGenerators(NamedObjectRegistry<DataGenerator> dataGenerators)
    {
        this.dataGenerators = dataGenerators;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception
    {    
        PropertyCheck.mandatory(this, "searchPath", searchPath);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "auditDAO", auditDAO);
        PropertyCheck.mandatory(this, "dataExtractors", dataExtractors);
        PropertyCheck.mandatory(this, "dataGenerators", dataGenerators);        
        super.afterPropertiesSet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized AuditModelRegistryState getState(boolean start)
    {
        return (AuditModelRegistryState)super.getState(start);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, AuditApplication> getAuditApplications()
    {
        return getState(true).getAuditApplications();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuditApplication getAuditApplicationByKey(String key)
    {
        return getState(true).getAuditApplicationByKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuditApplication getAuditApplicationByName(String applicationName)
    {
        return getState(true).getAuditApplicationByName(applicationName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathMapper getAuditPathMapper()
    {
        return getState(true).getAuditPathMapper();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadAuditModels()
    {
        stop();
        start();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAuditEnabled()
    {
        String value = getProperty(AUDIT_PROPERTY_AUDIT_ENABLED);
        return value != null && value.equalsIgnoreCase("true");
    }

    /**
     * Enables audit and registers an audit model at a given URL. Does not register across the cluster and should only
     * be used for unit test purposes.
     * 
     * @param auditModelUrl             the source of the model
     */
    public synchronized void registerModel(URL auditModelUrl)
    {
        stop();
        setProperty(AUDIT_PROPERTY_AUDIT_ENABLED, "true");
        getState(false).registerModel(auditModelUrl);
    }
        
    /**
     * A class encapsulating the disposable/resettable state of the audit model registry.
     */
    public class AuditModelRegistryState implements PropertyBackedBeanState
    {        
        /** The audit models. */
        private final Map<URL, Audit> auditModels;
        /** Used to lookup path translations. */
        private PathMapper auditPathMapper;
        /** Used to lookup the audit application java hierarchy. */
        private Map<String, AuditApplication> auditApplicationsByKey;
        /** Used to lookup the audit application java hierarchy. */
        private Map<String, AuditApplication> auditApplicationsByName;
        /** The exposed configuration properties. */
        private final Map<String, Boolean> properties;
        
        /**
         * Instantiates a new audit model registry state.
         */
        public AuditModelRegistryState()
        {
            auditModels = new LinkedHashMap<URL, Audit>(7);
            properties = new HashMap<String, Boolean>(7);
            
            // Default value for global enabled property
            properties.put(AUDIT_PROPERTY_AUDIT_ENABLED, false);

            // Let's search for config files in the appropriate places. The individual applications they contain can still
            // be enabled/disabled by the bean properties
            ResourceFinder resourceFinder = new ResourceFinder(getParent());
            try
            {
                for (Resource resource : resourceFinder.getResources(searchPath))
                {
                    registerModel(resource.getURL());
                }
            }
            catch (IOException e)
            {
                throw new AlfrescoRuntimeException("Failed to find audit resources", e);
            }                    
        }

        /**
         * Register an audit model at a given URL.
         * 
         * @param auditModelUrl         the source of the model
         */
        public void registerModel(URL auditModelUrl)
        {
            try
            {
                if (auditModels.containsKey(auditModelUrl))
                {
                    logger.warn("An audit model has already been registered at URL " + auditModelUrl);
                }
                Audit audit = AuditModelRegistryImpl.unmarshallModel(auditModelUrl);

                // Store the model itself
                auditModels.put(auditModelUrl, audit);

                // Cache property enabling each application by default
                List<Application> applications = audit.getApplication();
                for (Application application : applications)
                {
                    properties.put(getEnabledProperty(application.getKey()), true);
                }
            }
            catch (Throwable e)
            {
                throw new AuditModelException("Failed to load audit model: " + auditModelUrl, e);
            }
        }

        /**
         * Helper method to convert an application key into a <b>enabled-disabled</b> property.
         * 
         * @param key                   an application key e.g. for "My App" the key might be "myapp",
         *                              but is defined in the audit model config.
         * @return                      the property name of the for "audit.myapp.enabled"
         */
        private String getEnabledProperty(String key)
        {
            return "audit." + key.toLowerCase() + ".enabled";
        }
        
        /**
         * Checks if an application key is enabled.  Each application has a name and a root key
         * value.  It is the key (which will be used as the root of all logged paths) that is
         * used here.
         * 
         * @param key                   the application key
         * @return                      <tt>true</tt> if the application key is enabled
         */
        private boolean isApplicationEnabled(String key)
        {
            Boolean enabled = properties.get(getEnabledProperty(key));
            return enabled != null && enabled;
        }

        /**
         * @see org.alfresco.repo.management.subsystems.PropertyBackedBeanState#getProperty(java.lang.String)
         */
        public String getProperty(String name)
        {
            return String.valueOf(properties.get(name));
        }

        /**
         * @see org.alfresco.repo.management.subsystems.PropertyBackedBeanState#getPropertyNames()
         */
        public Set<String> getPropertyNames()
        {
            return properties.keySet();
        }

        /**
         * @see org.alfresco.repo.management.subsystems.PropertyBackedBeanState#setProperty(java.lang.String, java.lang.String)
         */
        public void setProperty(String name, String value)
        {
            properties.put(name, Boolean.parseBoolean(value));
        }

        /**
         * @see org.alfresco.repo.management.subsystems.PropertyBackedBeanState#start()
         */
        public void start()
        {
            auditApplicationsByKey = new TreeMap<String, AuditApplication>();
            auditApplicationsByName = new TreeMap<String, AuditApplication>();
            auditPathMapper = new PathMapper();

            // If we are globally disabled, skip processing the models
            Boolean enabled = properties.get(AUDIT_PROPERTY_AUDIT_ENABLED);
            if (enabled != null && enabled)
            {
                final RetryingTransactionCallback<Void> loadModelsCallback = new RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        // Load models from the URLs
                        for (Map.Entry<URL, Audit> entry : auditModels.entrySet())
                        {
                            URL auditModelUrl = entry.getKey();
                            Audit audit = entry.getValue();
                            try
                            {
                                // Get an input stream and write the model
                                Long auditModelId = auditDAO.getOrCreateAuditModel(auditModelUrl).getFirst();

                                // Now cache it (eagerly)
                                cacheAuditElements(auditModelId, audit);
                            }
                            catch (Throwable e)
                            {
                                throw new AuditModelException("Failed to load audit model: " + auditModelUrl, e);
                            }
                        }
                        // NOTE: If we support other types of loading, then that will have to go here, too

                        // Done
                        return null;
                    }
                };

                // Run as system user to avoid cyclical dependency in bootstrap read-only checking (and we are anyway!)
                AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        transactionService.getRetryingTransactionHelper().doInTransaction(
                                loadModelsCallback,
                                transactionService.isReadOnly(),
                                true);
                        return null;
                    }
                }, AuthenticationUtil.getSystemUserName());
            }

            auditPathMapper.lock();
        }

        /**
         * {@inheritDoc}
         */
        public void stop()
        {
            auditPathMapper = null;
        }
        
        /**
         * Gets all audit applications keyed by name.
         * @see org.alfresco.repo.audit.model.AuditModelRegistry#getAuditApplications()
         */
        public Map<String, AuditApplication> getAuditApplications()
        {
            return auditApplicationsByName;
        }
        
        /**
         * Gets an audit application by key.
         * @see org.alfresco.repo.audit.model.AuditModelRegistry#getAuditApplicationByKey(java.lang.String)
         */
        public AuditApplication getAuditApplicationByKey(String key)
        {
            return auditApplicationsByKey.get(key);
        }
        
        /**
         * Gets an audit application by name.
         * @see org.alfresco.repo.audit.model.AuditModelRegistry#getAuditApplicationByName(java.lang.String)
         */
        public AuditApplication getAuditApplicationByName(String applicationName)
        {
            return auditApplicationsByName.get(applicationName);
        }
        
        /**
         * Gets the audit path mapper.
         * 
         * @return                      the audit path mapper
         * @see org.alfresco.repo.audit.model.AuditModelRegistry#getAuditPathMapper()
         */
        public PathMapper getAuditPathMapper()
        {
            return auditPathMapper;
        }
        
        /**
         * Caches audit elements from a model.
         */
        private void cacheAuditElements(Long auditModelId, Audit audit)
        {
            Map<String, DataExtractor> dataExtractorsByName = new HashMap<String, DataExtractor>(13);
            Map<String, DataGenerator> dataGeneratorsByName = new HashMap<String, DataGenerator>(13);

            // Get the data extractors and check for duplicates
            DataExtractors extractorsElement = audit.getDataExtractors();
            if (extractorsElement == null)
            {
                extractorsElement = objectFactory.createDataExtractors();
            }
            List<org.alfresco.repo.audit.model._3.DataExtractor> extractorElements = extractorsElement.getDataExtractor();
            for (org.alfresco.repo.audit.model._3.DataExtractor extractorElement : extractorElements)
            {
                String name = extractorElement.getName();
                // If the name is taken, make sure that they are equal
                if (dataExtractorsByName.containsKey(name))
                {
                    throw new AuditModelException(
                            "Audit data extractor '" + name + "' has already been defined.");
                }
                // Construct the converter
                final DataExtractor dataExtractor;
                if (extractorElement.getClazz() != null)
                {
                    try
                    {
                        Class<?> dataExtractorClazz = Class.forName(extractorElement.getClazz());
                        dataExtractor = (DataExtractor) dataExtractorClazz.newInstance();
                    }
                    catch (ClassNotFoundException e)
                    {
                        throw new AuditModelException(
                                "Audit data extractor '" + name + "' class not found: " + extractorElement.getClazz());
                    }
                    catch (Exception e)
                    {
                        throw new AuditModelException(
                                "Audit data extractor '" + name + "' could not be constructed: " + extractorElement.getClazz());
                    }
                }
                else if (extractorElement.getRegisteredName() != null)
                {
                    String registeredName = extractorElement.getRegisteredName();
                    dataExtractor = dataExtractors.getNamedObject(registeredName);
                    if (dataExtractor == null)
                    {
                        throw new AuditModelException(
                                "No registered audit data extractor exists for '" + registeredName + "'.");
                    }
                }
                else
                {
                    throw new AuditModelException(
                            "Audit data extractor has no class or registered name: " + name);
                }
                // Store
                dataExtractorsByName.put(name, dataExtractor);
            }
            // Get the data generators and check for duplicates
            DataGenerators generatorsElement = audit.getDataGenerators();
            if (generatorsElement == null)
            {
                generatorsElement = objectFactory.createDataGenerators();
            }
            List<org.alfresco.repo.audit.model._3.DataGenerator> generatorElements = generatorsElement.getDataGenerator();
            for (org.alfresco.repo.audit.model._3.DataGenerator generatorElement : generatorElements)
            {
                String name = generatorElement.getName();
                // If the name is taken, make sure that they are equal
                if (dataGeneratorsByName.containsKey(name))
                {
                    throw new AuditModelException(
                            "Audit data generator '" + name + "' has already been defined.");
                }
                // Construct the generator
                final DataGenerator dataGenerator;
                if (generatorElement.getClazz() != null)
                {
                    try
                    {
                        Class<?> dataGeneratorClazz = Class.forName(generatorElement.getClazz());
                        dataGenerator = (DataGenerator) dataGeneratorClazz.newInstance();
                    }
                    catch (ClassNotFoundException e)
                    {
                        throw new AuditModelException(
                                "Audit data generator '" + name + "' class not found: " + generatorElement.getClazz());
                    }
                    catch (Exception e)
                    {
                        throw new AuditModelException(
                                "Audit data generator '" + name + "' could not be constructed: " + generatorElement.getClazz());
                    }
                }
                else if (generatorElement.getRegisteredName() != null)
                {
                    String registeredName = generatorElement.getRegisteredName();
                    dataGenerator = dataGenerators.getNamedObject(registeredName);
                    if (dataGenerator == null)
                    {
                        throw new AuditModelException(
                                "No registered audit data generator exists for '" + registeredName + "'.");
                    }
                }
                else
                {
                    throw new AuditModelException(
                            "Audit data generator has no class or registered name: " + name);
                }
                // Store
                dataGeneratorsByName.put(name, dataGenerator);
            }
            // Get the application and check for duplicates
            List<Application> applications = audit.getApplication();
            for (Application application : applications)
            {
                String key = application.getKey();
                if (!isApplicationEnabled(key))
                {
                    continue;
                }

                if (auditApplicationsByKey.containsKey(key))
                {
                    throw new AuditModelException(
                            "Audit application key '" + key + "' is used by: " + auditApplicationsByKey.get(key));
                }
                
                String name = application.getName();
                if (auditApplicationsByName.containsKey(name))
                {
                    throw new AuditModelException(
                            "Audit application '" + name + "' is used by: " + auditApplicationsByName.get(name));
                }
                
                // Get the ID of the application
                AuditApplicationInfo appInfo = auditDAO.getAuditApplication(name);
                if (appInfo == null)
                {
                    appInfo = auditDAO.createAuditApplication(name, auditModelId);
                }
                else
                {
                    // Update it with the new model ID
                    auditDAO.updateAuditApplicationModel(appInfo.getId(), auditModelId);
                }
                
                AuditApplication wrapperApp = new AuditApplication(
                        dataExtractorsByName,
                        dataGeneratorsByName,
                        application,
                        appInfo.getId(),
                        appInfo.getDisabledPathsId());
                auditApplicationsByName.put(name, wrapperApp);
                auditApplicationsByKey.put(key, wrapperApp);
            }
            
            // Pull out all the audit path maps
            buildAuditPathMap(audit);            
        }
        
        /**
         * Construct the reverse lookup maps for quick conversion of data to target maps.
         */
        private void buildAuditPathMap(Audit audit)
        {
            PathMappings pathMappings = audit.getPathMappings();
            if (pathMappings == null)
            {
                pathMappings = objectFactory.createPathMappings();
            }
            for (PathMap pathMap : pathMappings.getPathMap())
            {
                String sourcePath = pathMap.getSource();
                String targetPath = pathMap.getTarget();

                // Extract the application key from the root of the path
                int keyStart = targetPath.charAt(0) == '/' ? 1 : 0;
                int keyEnd = targetPath.indexOf('/', keyStart);
                String key = keyEnd == -1 ? targetPath.substring(keyStart) : targetPath.substring(keyStart, keyEnd);

                // Only add the path if the application is not disabled
                if (isApplicationEnabled(key))
                {
                    auditPathMapper.addPathMap(sourcePath, targetPath);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PropertyBackedBeanState createInitialState() throws IOException
    {
        return new AuditModelRegistryState();
    }

    /**
     * Unmarshalls the Audit model from the URL.
     * 
     * @param configUrl                     the config url
     * @return                              the audit model
     * @throws AlfrescoRuntimeException     if an IOException occurs
     */
    public static Audit unmarshallModel(URL configUrl)
    {
        try
        {
            // Load it
            InputStream is = new BufferedInputStream(configUrl.openStream());
            return unmarshallModel(is, configUrl.toString());
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("The Audit model XML failed to load: " + configUrl, e);
        }
    }

    /**
     * Unmarshalls the Audit model from a stream.
     */
    private static Audit unmarshallModel(InputStream is, final String source)
    {
        final Schema schema;
        final JAXBContext jaxbCtx;
        final Unmarshaller jaxbUnmarshaller;
        try
        {
            SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schema = sf.newSchema(ResourceUtils.getURL(AUDIT_SCHEMA_LOCATION));
            jaxbCtx = JAXBContext.newInstance("org.alfresco.repo.audit.model._3");
            jaxbUnmarshaller = jaxbCtx.createUnmarshaller();
            jaxbUnmarshaller.setSchema(schema);
            jaxbUnmarshaller.setEventHandler(new ValidationEventHandler()
            {
                public boolean handleEvent(ValidationEvent ve)
                {
                    if (ve.getSeverity() == ValidationEvent.FATAL_ERROR || ve.getSeverity() == ValidationEvent.ERROR)
                    {
                        ValidationEventLocator locator = ve.getLocator();
                        logger.error("Invalid Audit XML: \n" +
                                "   Source:   " + source + "\n" +
                                "   Location: Line " + locator.getLineNumber() + " column " + locator.getColumnNumber() + "\n" +
                                "   Error:    " + ve.getMessage());
                    }
                    return false;
                }
            });
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException(
                    "Failed to load Alfresco Audit Schema from " + AUDIT_SCHEMA_LOCATION, e);
        }
        try
        {
            // Unmarshall with validation
            @SuppressWarnings("unchecked")
            JAXBElement<Audit> auditElement = (JAXBElement<Audit>) jaxbUnmarshaller.unmarshal(is);

            Audit audit = auditElement.getValue();
            // Done
            return audit;
        }
        catch (Throwable e)
        {
            // Dig out a SAXParseException, if there is one
            Throwable saxError = ExceptionStackUtil.getCause(e, SAXParseException.class);
            if (saxError != null)
            {
                e = saxError;
            }
            throw new AuditModelException(
                    "Failed to read Audit model XML: \n" +
                    "   Source: " + source + "\n" +
                    "   Error:  " + e.getMessage());
        }
        finally
        {
            try { is.close(); } catch (IOException e) {}
        }
    }    
}
