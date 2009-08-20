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

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.audit.extractor.DataExtractor;
import org.alfresco.repo.audit.generator.DataGenerator;
import org.alfresco.repo.audit.model._3.Application;
import org.alfresco.repo.audit.model._3.Audit;
import org.alfresco.repo.audit.model._3.DataExtractors;
import org.alfresco.repo.audit.model._3.DataGenerators;

/**
 * Component used to store audit model definitions.  It ensures that duplicate application and converter
 * definitions are detected and provides a single lookup for code using the Audit model.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class AuditModelRegistry
{
    private final Map<URL, Audit> auditModelsByUrl;
    private final Map<String, DataExtractor> dataExtractorsByName;
    private final Map<String, DataGenerator> dataGeneratorsByName;
    private final Map<String, Application> auditApplicationsByName;
    
    public AuditModelRegistry()
    {
        auditModelsByUrl = new HashMap<URL, Audit>(7);
        dataExtractorsByName = new HashMap<String, DataExtractor>(13);
        dataGeneratorsByName = new HashMap<String, DataGenerator>(13);
        auditApplicationsByName = new HashMap<String, Application>(7);
    }
    
    /**
     * Register an audit model.
     * 
     * @param configurationUrl              the source of the configuration
     * @param audit                         the unmarshalled instance tree
     */
    public void registerModel(URL configurationUrl, Audit audit)
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
        }
        // Store the model
        auditModelsByUrl.put(configurationUrl, audit);
    }
}
