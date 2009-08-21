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
import java.io.File;
import java.io.FileInputStream;
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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.audit.extractor.DataExtractor;
import org.alfresco.repo.audit.generator.DataGenerator;
import org.alfresco.repo.audit.model._3.Application;
import org.alfresco.repo.audit.model._3.Audit;
import org.alfresco.repo.audit.model._3.DataExtractors;
import org.alfresco.repo.audit.model._3.DataGenerators;
import org.alfresco.repo.domain.audit.AuditDAO;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Component used to store audit model definitions.  It ensures that duplicate application and converter
 * definitions are detected and provides a single lookup for code using the Audit model.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class AuditModelRegistry
{
    private AuditDAO auditDAO;
    
    private final ReentrantReadWriteLock.ReadLock readLock;
    private final ReentrantReadWriteLock.WriteLock writeLock;
    
    private final Set<URL> auditModelUrls;
    private final List<Audit> auditModels;
    private final Map<String, DataExtractor> dataExtractorsByName;
    private final Map<String, DataGenerator> dataGeneratorsByName;
    /**
     * Used to lookup the audit application java hierarchy 
     */
    private final Map<String, Application> auditApplicationsByName;
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
        
        auditModelUrls = new HashSet<URL>(7);
        auditModels = new ArrayList<Audit>(7);
        dataExtractorsByName = new HashMap<String, DataExtractor>(13);
        dataGeneratorsByName = new HashMap<String, DataGenerator>(13);
        auditApplicationsByName = new HashMap<String, Application>(7);
        auditModelIdsByApplicationsName = new HashMap<String, Long>(7);
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
                throw new AlfrescoRuntimeException(
                        "An audit model has already been registered at URL " + auditModelUrl);
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
     * Method to load audit models into memory.  This method is also responsible for persisting
     * the audit models for later retrieval.  Models are loaded from the locations given by the
     * {@link #registerModel(URL) register} methods.
     */
    public void loadAuditModels()
    {
        writeLock.lock();
        try
        {
            // Load models from the URLs
            for (URL auditModelUrl : auditModelUrls)
            {
                Audit audit = AuditModelRegistry.unmarshallModel(auditModelUrl);
                // That worked, so now get an input stream and write the model
                Long auditModelId = auditDAO.getOrCreateAuditModel(auditModelUrl).getFirst();
                // Now cache it (eagerly)
                cacheAuditElements(auditModelId, audit);
            }
            // NOTE: If we support other types of loading, then that will have to go here, too
        }
        finally
        {
            writeLock.unlock();
        }
    }
    
    /**
     * Get the ID of the persisted audit model for the given application name
     * 
     * @param application           the name of the audited application
     * @return                      the unique ID of the persisted model (<tt>null</tt> if not found)
     */
    public Long getAuditModelId(String application)
    {
        readLock.lock();
        try
        {
            return auditModelIdsByApplicationsName.get(application);
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
            File file = new File(configUrl.getFile());
            if (!file.exists())
            {
                throw new AlfrescoRuntimeException("The Audit model XML was not found: " + configUrl);
            }
            
            // Load it
            InputStream is = new BufferedInputStream(new FileInputStream(file));
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
    private static Audit unmarshallModel(InputStream is, String source)
    {
        try
        {
            JAXBContext jaxbCtx = JAXBContext.newInstance("org.alfresco.repo.audit.model._3");
            Unmarshaller jaxbUnmarshaller = jaxbCtx.createUnmarshaller();
            @SuppressWarnings("unchecked")
            JAXBElement<Audit> auditElement = (JAXBElement<Audit>) jaxbUnmarshaller.unmarshal(is);
            Audit audit = auditElement.getValue();
            // Done
            return audit;
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException(
                    "Failed to read Audit model XML: \n" +
                    "   Source: " + source + "\n" +
                    "   Error:  " + e.getMessage(),
                    e);
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
        List<org.alfresco.repo.audit.model._3.DataExtractor> converterElements = extractorsElement.getDataExtractor();
        for (org.alfresco.repo.audit.model._3.DataExtractor converterElement : converterElements)
        {
            String name = converterElement.getName();
            if (dataExtractorsByName.containsKey(name))
            {
                throw new AuditModelException("Audit data extractor '" + name + "' has already been defined.");
            }
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
            dataExtractorsByName.put(name, dataExtractor);
        }
        // Get the data generators and check for duplicates
        DataGenerators generatorsElement = audit.getDataGenerators();
        List<org.alfresco.repo.audit.model._3.DataGenerator> generatorElements = generatorsElement.getDataGenerator();
        for (org.alfresco.repo.audit.model._3.DataGenerator generatorElement : generatorElements)
        {
            String name = generatorElement.getName();
            if (dataGeneratorsByName.containsKey(name))
            {
                throw new AuditModelException("Audit data generator '" + name + "' has already been defined.");
            }
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
            auditApplicationsByName.put(name, application);
            auditModelIdsByApplicationsName.put(name, auditModelId);
        }
        // Store the model itself
        auditModels.add(audit);
    }
}
