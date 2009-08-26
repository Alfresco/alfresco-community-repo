/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.audit.model;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
import org.alfresco.repo.domain.audit.AuditDAO;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ResourceUtils;
import org.xml.sax.SAXParseException;

/**
 * Component used to store audit model definitions.  It ensures that duplicate application and converter
 * definitions are detected and provides a single lookup for code using the Audit model.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class AuditModelRegistry
{
    public static final String AUDIT_SCHEMA_LOCATION = "classpath:alfresco/audit/alfresco-audit-3.2.xsd";
    public static final String AUDIT_RESERVED_KEY_USERNAME = "username";
    public static final String AUDIT_RESERVED_KEY_SYSTEMTIME = "systemTime";
    
    private static final Log logger = LogFactory.getLog(AuditModelRegistry.class);
    
    private TransactionService transactionService;
    private AuditDAO auditDAO;
    
    private final ReentrantReadWriteLock.ReadLock readLock;
    private final ReentrantReadWriteLock.WriteLock writeLock;
    private final ObjectFactory objectFactory;
    
    private final Set<URL> auditModelUrls;
    private final List<Audit> auditModels;
    private final Map<String, DataExtractor> dataExtractorsByName;
    private final Map<String, DataGenerator> dataGeneratorsByName;
    /**
     * Used to lookup the audit application java hierarchy 
     */
    private final Map<String, AuditApplication> auditApplicationsByName;
    /**
     * Used to lookup a reference to the persisted config binary for an application
     */
    private final Map<String, Long> auditModelIdsByApplicationsName;
    
    /**
     * Default constructor
     */
    public AuditModelRegistry()
    {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
        
        objectFactory = new ObjectFactory();
        
        auditModelUrls = new HashSet<URL>(7);
        auditModels = new ArrayList<Audit>(7);
        dataExtractorsByName = new HashMap<String, DataExtractor>(13);
        dataGeneratorsByName = new HashMap<String, DataGenerator>(13);
        auditApplicationsByName = new HashMap<String, AuditApplication>(7);
        auditModelIdsByApplicationsName = new HashMap<String, Long>(7);
    }

    /**
     * Service to ensure DAO calls are transactionally wrapped
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * Set the DAO used to persisted the registered audit models
     */
    public void setAuditDAO(AuditDAO auditDAO)
    {
        this.auditDAO = auditDAO;
    }

    /**
     * Register an audit model at a given URL.
     * 
     * @param auditModelUrl             the source of the model
     */
    public void registerModel(URL auditModelUrl)
    {
        writeLock.lock();
        try
        {
            if (auditModelUrls.contains(auditModelUrl))
            {
                logger.warn("An audit model has already been registered at URL " + auditModelUrl);
            }
            auditModelUrls.add(auditModelUrl);
        }
        finally
        {
            writeLock.unlock();
        }
    }
    
    /**
     * Register an audit model at a given node reference.
     * 
     * @param auditModelNodeRef         the source of the audit model
     */
    public void registerModel(NodeRef auditModelNodeRef)
    {
        writeLock.lock();
        try
        {
            throw new UnsupportedOperationException();
        }
        finally
        {
            writeLock.unlock();
        }
    }
    
    /**
     * Cleans out all derived data
     */
    private void clearCaches()
    {
        auditModels.clear();
        dataExtractorsByName.clear();
        dataGeneratorsByName.clear();;
        auditApplicationsByName.clear();
        auditModelIdsByApplicationsName.clear();
    }
    
    /**
     * Method to load audit models into memory.  This method is also responsible for persisting
     * the audit models for later retrieval.  Models are loaded from the locations given by the
     * {@link #registerModel(URL) register} methods.
     * <p/>
     * Note, the models are loaded in a new transaction.
     */
    public void loadAuditModels()
    {
        RetryingTransactionCallback<Void> loadModelsCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // Load models from the URLs
                Set<URL> auditModelUrlsInner = new HashSet<URL>(auditModelUrls);
                for (URL auditModelUrl : auditModelUrlsInner)
                {
                    Audit audit = AuditModelRegistry.unmarshallModel(auditModelUrl);
                    // That worked, so now get an input stream and write the model
                    Long auditModelId = auditDAO.getOrCreateAuditModel(auditModelUrl).getFirst();
                    try
                    {
                        // Now cache it (eagerly)
                        cacheAuditElements(auditModelId, audit);
                    }
                    catch (Throwable e)
                    {
                        // Mainly for test purposes, we clear out the failed URL
                        auditModelUrls.remove(auditModelUrl);
                        clearCaches();
                        
                        throw new AuditModelException(
                                "Failed to load audit model: " + auditModelUrl + "\n" +
                                e.getMessage());
                    }
                }
                // NOTE: If we support other types of loading, then that will have to go here, too
                
                // Done
                return null;
            }
        };

        writeLock.lock();
        // Drop all cached data
        clearCaches();
        try
        {
            transactionService.getRetryingTransactionHelper().doInTransaction(loadModelsCallback);
        }
        finally
        {
            writeLock.unlock();
        }
    }
    
    /**
     * Get the ID of the persisted audit model for the given application name
     * 
     * @param applicationName       the name of the audited application
     * @return                      the unique ID of the persisted model (<tt>null</tt> if not found)
     */
    public Long getAuditModelId(String applicationName)
    {
        readLock.lock();
        try
        {
            return auditModelIdsByApplicationsName.get(applicationName);
        }
        finally
        {
            readLock.unlock();
        }
    }
    
    /**
     * Get the application model for the given application name
     * 
     * @param applicationName       the name of the audited application
     * @return                      the java model (<tt>null</tt> if not found)
     */
    public AuditApplication getAuditApplication(String applicationName)
    {
        readLock.lock();
        try
        {
            return auditApplicationsByName.get(applicationName);
        }
        finally
        {
            readLock.unlock();
        }
    }
    
    /**
     * Unmarshalls the Audit model from the URL.
     * 
     * @throws AlfrescoRuntimeException         if an IOException occurs
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
     * Unmarshalls the Audit model from a stream
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
                    return true;
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
    
    private void cacheAuditElements(Long auditModelId, Audit audit)
    {
        // Get the data extractors and check for duplicates
        DataExtractors extractorsElement = audit.getDataExtractors();
        if (extractorsElement == null)
        {
            extractorsElement = objectFactory.createDataExtractors();
        }
        List<org.alfresco.repo.audit.model._3.DataExtractor> converterElements = extractorsElement.getDataExtractor();
        for (org.alfresco.repo.audit.model._3.DataExtractor converterElement : converterElements)
        {
            String name = converterElement.getName();
            // Construct the converter
            final DataExtractor dataExtractor;
            try
            {
                Class<?> dataExtractorClazz = Class.forName(converterElement.getClazz());
                dataExtractor = (DataExtractor) dataExtractorClazz.newInstance();
            }
            catch (ClassNotFoundException e)
            {
                throw new AuditModelException(
                        "Audit data extractor '" + name + "' class not found: " + converterElement.getClazz());
            }
            catch (Exception e)
            {
                throw new AuditModelException(
                        "Audit data extractor '" + name + "' could not be constructed: " + converterElement.getClazz());
            }
            // If the name is taken, make sure that they are equal
            if (dataExtractorsByName.containsKey(name))
            {
                DataExtractor existing = dataExtractorsByName.get(name);
                if (!existing.equals(dataExtractor))
                {
                    throw new AuditModelException(
                            "Audit data extractor '" + name + "' is incompatible with an existing instance.");
                }
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
            // Construct the converter
            final DataGenerator dataGenerator;
            try
            {
                Class<?> dataExtractorClazz = Class.forName(generatorElement.getClazz());
                dataGenerator = (DataGenerator) dataExtractorClazz.newInstance();
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
            // If the name is taken, make sure that they are equal
            if (dataGeneratorsByName.containsKey(name))
            {
                DataGenerator existing = dataGeneratorsByName.get(name);
                if (!existing.equals(dataGenerator))
                {
                    throw new AuditModelException(
                            "Audit data generator '" + name + "' is incompatible with an existing instance.");
                }
            }
            // Store
            dataGeneratorsByName.put(name, dataGenerator);
        }
        // Get the application and check for duplicates
        List<Application> applications = audit.getApplication();
        for (Application application : applications)
        {
            String name = application.getName();
            if (auditApplicationsByName.containsKey(name))
            {
                throw new AuditModelException("Audit application '" + name + "' has already been defined.");
            }
            AuditApplication wrapperApp = new AuditApplication(dataExtractorsByName, dataGeneratorsByName, application);
            auditApplicationsByName.put(name, wrapperApp);
            auditModelIdsByApplicationsName.put(name, auditModelId);
        }
        // Store the model itself
        auditModels.add(audit);
    }
}
