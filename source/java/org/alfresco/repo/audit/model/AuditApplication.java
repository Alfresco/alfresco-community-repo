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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.audit.extractor.DataExtractor;
import org.alfresco.repo.audit.generator.DataGenerator;
import org.alfresco.repo.audit.generator.DataGenerator.DataGeneratorScope;
import org.alfresco.repo.audit.model._3.Application;
import org.alfresco.repo.audit.model._3.AuditPath;
import org.alfresco.repo.audit.model._3.GenerateValue;
import org.alfresco.repo.audit.model._3.RecordValue;
import org.alfresco.repo.audit.model._3.ScopeAttribute;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper class that wraps the {@link Application audit application}.
 * Once wrapped, client code doesn't need access to any of the generated
 * model-driven classes.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class AuditApplication
{
    public static final String AUDIT_PATH_SEPARATOR = "/";
    
    private static final Log logger = LogFactory.getLog(AuditApplication.class);

    private final String applicationName;
    private final String applicationKey;
    
    private final Map<String, DataExtractor> dataExtractorsByName;
    private final Map<String, DataGenerator> dataGeneratorsByName;
    @SuppressWarnings("unused")
    private final Application application;

    /** Derived expaned map for fast lookup */
    private Map<String, Map<String, DataExtractor>> dataExtractors = new HashMap<String, Map<String, DataExtractor>>(11);
    /** Derived expaned map for fast lookup */
    private Map<String, Map<String, DataGenerator>> dataGenerators = new HashMap<String, Map<String, DataGenerator>>(11);
    /** Derived expaned map for fast lookup */
    private Map<String, DataGeneratorScope> dataGeneratorScopes = new HashMap<String, DataGeneratorScope>(11);
    
    /**
     * @param application           the application that will be wrapped
     * @param dataExtractorsByName  data extractors to use
     * @param dataGeneratorsByName  data generators to use
     */
    /* package */ AuditApplication(
            Map<String, DataExtractor> dataExtractorsByName,
            Map<String, DataGenerator> dataGeneratorsByName,
            Application application)
    {
        this.dataExtractorsByName = dataExtractorsByName;
        this.dataGeneratorsByName = dataGeneratorsByName;
        this.application = application;

        this.applicationName = application.getName();
        this.applicationKey = application.getKey();
        
        buildAuditPaths(application);
    }
    
    @Override
    public int hashCode()
    {
        return applicationName.hashCode();
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof AuditApplication)
        {
            AuditApplication that = (AuditApplication) obj;
            return this.applicationName.equals(that.applicationName);
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(56);
        sb.append("AuditApplication")
          .append("[ name=").append(applicationName)
          .append("]");
        return sb.toString();
    }
    
    /**
     * Get the application name
     */
    public String getApplicationName()
    {
        return applicationName;
    }

    /**
     * Get the key (root path) for the application
     */
    public String getApplicationKey()
    {
        return applicationKey;
    }
    
    /**
     * Helper method to check that a path is correct for this application instance
     * 
     * @param path              the path in format <b>/app-key/x/y/z</b>
     * @throws AuditModelException      if the path is invalid 
     */
    public void checkPath(String path)
    {
        if (path == null || path.length() == 0)
        {
            generateException(path, "Invalid audit path.");
        }
        if (!path.startsWith(AUDIT_PATH_SEPARATOR))
        {
            generateException(
                    path,
                    "An audit path must always start with the separator '" + AUDIT_PATH_SEPARATOR + "'.");
        }
        if (path.indexOf(applicationKey, 0) != 1)
        {
            generateException(
                    path,
                    "An audit path's first element must be the application's key i.e. '" + applicationKey + "'.");
        }
        if (path.endsWith(AUDIT_PATH_SEPARATOR))
        {
            generateException(
                    path,
                    "An audit path may not end with the separator '" + AUDIT_PATH_SEPARATOR + "'.");
        }
        if (!path.toLowerCase().equals(path))
        {
            generateException(
                    path,
                    "An audit path may only contain lowercase letters, '-' and '.' i.e. regex ([a-z]|[0-9]|\\-|\\.)*");
        }
    }
    
    /**
     * Get all data extractors applicable to a given path and scope.
     * 
     * @param path              the audit path
     * @return                  Returns all data extractors mapped to their key-path
     */
    public Map<String, DataExtractor> getDataExtractors(String path)
    {
        Map<String, DataExtractor> extractors = dataExtractors.get(path);
        if (extractors == null)
        {
            // Don't give back a null
            extractors = Collections.emptyMap();
        }
        else
        {
            // we don't want to give back a modifiable map
            extractors = Collections.unmodifiableMap(extractors);
        }
        
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Looked up data extractors: \n" +
                    "   Path:  " + path + "\n" +
                    "   Found: " + extractors);
        }
        return extractors;
    }
    
    /**
     * Get all data generators applicable to a given path and scope.
     * 
     * @param path              the audit path
     * @param scope             the audit scope (e.g. SESSION or AUDIT)
     * @return                  Returns all data generators mapped to their key-path
     */
    public Map<String, DataGenerator> getDataGenerators(String path, DataGeneratorScope scope)
    {
        Map<String, DataGenerator> generators = dataGenerators.get(path);
        if (generators == null)
        {
            // Don't give back a null
            generators = new HashMap<String, DataGenerator>(0);
        }
        else
        {
            // Copy the map so that (a) we can modify it during iteration and (b) we return
            // something that the client can't mess up.
            generators = new HashMap<String, DataGenerator>(generators);
        }
        
        if (scope != DataGeneratorScope.ALL)
        {
            // Go through them and eliminate the ones in the wrong scope
            Iterator<Map.Entry<String, DataGenerator>> iterator = generators.entrySet().iterator();
            while (iterator.hasNext())
            {
                Map.Entry<String, DataGenerator> entry = iterator.next();
                String generatorPath = entry.getKey();
                DataGeneratorScope generatorScope = dataGeneratorScopes.get(generatorPath);
                if (generatorScope == DataGeneratorScope.ALL)
                {
                    // This one always applies
                    continue;
                }
                else if (generatorScope != scope)
                {
                    // Wrong scope
                    iterator.remove();
                }
            }
        }
        
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Looked up data generators: \n" +
                    "   Path:  " + path + "\n" +
                    "   Scope: " + scope + "\n" +
                    "   Found: " + generators);
        }
        return generators;
    }
    
    /**
     * Internal helper method to kick off generator and extractor path mappings
     */
    private void buildAuditPaths(AuditPath auditPath)
    {
        buildAuditPaths(
                auditPath,
                "",
                new HashSet<String>(37),
                new HashMap<String, DataExtractor>(13),
                new HashMap<String, DataGenerator>(13));
    }
    /**
     * Recursive method to build generator and extractor mappings
     */
    private void buildAuditPaths(
            AuditPath auditPath,
            String currentPath,
            Set<String> existingPaths,
            Map<String, DataExtractor> upperExtractorsByPath,
            Map<String, DataGenerator> upperGeneratorsByPath)
    {
        // Clone the upper maps to prevent pollution
        upperExtractorsByPath = new HashMap<String, DataExtractor>(upperExtractorsByPath);
        upperGeneratorsByPath = new HashMap<String, DataGenerator>(upperGeneratorsByPath);
        
        // Append the current audit path to the current path
        currentPath = (currentPath + AUDIT_PATH_SEPARATOR + auditPath.getKey());
        // Make sure we have not processed it before
        if (!existingPaths.add(currentPath))
        {
            generateException(currentPath, "The audit path already exists.");
        }
        // Make sure that the path is all lowercase
        checkPathCase(currentPath);
        
        // Get the data extractors declared for this key
        for (RecordValue element : auditPath.getRecordValue())
        {
            String key = element.getKey();
            String extractorPath = (currentPath + AUDIT_PATH_SEPARATOR + key);
            if (!existingPaths.add(extractorPath))
            {
                generateException(extractorPath, "The audit path already exists.");
            }
            // Make sure that the path is all lowercase
            checkPathCase(extractorPath);
            
            String extractorName = element.getDataExtractor();
            DataExtractor extractor = dataExtractorsByName.get(extractorName);
            if (extractor == null)
            {
                generateException(extractorPath, "No data extractor exists for name: " + extractorName);
            }
            // All generators that occur earlier in the path will also be applicable here
            upperExtractorsByPath.put(extractorPath, extractor);
        }
        // All the extractors apply to the current path
        dataExtractors.put(currentPath, upperExtractorsByPath);

        // Get the data generators declared for this key
        for (GenerateValue element : auditPath.getGenerateValue())
        {
            String key = element.getKey();
            String generatorPath = (currentPath + AUDIT_PATH_SEPARATOR + key);
            if (!existingPaths.add(generatorPath))
            {
                generateException(generatorPath, "The audit path already exists.");
            }
            // Make sure that the path is all lowercase
            checkPathCase(generatorPath);
            
            String generatorName = element.getDataGenerator();
            DataGenerator generator = dataGeneratorsByName.get(generatorName);
            if (generator == null)
            {
                generateException(generatorPath, "No data generator exists for name: " + generatorName);
            }
            // Store the scope
            ScopeAttribute scopeAttribute = element.getScope();
            if (scopeAttribute == null)
            {
                generateException(generatorPath, "No scope defined for generator: " + generatorName);
            }
            String scopeStr = scopeAttribute.value();
            if (scopeStr == null)
            {
                scopeStr = DataGeneratorScope.AUDIT.toString();
            }
            try
            {
                DataGeneratorScope scope = DataGeneratorScope.valueOf(scopeStr);
                dataGeneratorScopes.put(generatorPath, scope);
            }
            catch (Throwable e)
            {
                generateException(generatorPath, "Illegal generator scope value: " + scopeStr);
            }
            // All generators that occur earlier in the path will also be applicable here
            upperGeneratorsByPath.put(generatorPath, generator);
        }
        // All the generators apply to the current path
        dataGenerators.put(currentPath, upperGeneratorsByPath);
        
        // Find all sub audit paths and recurse
        for (AuditPath element : auditPath.getAuditPath())
        {
            buildAuditPaths(element, currentPath, existingPaths, upperExtractorsByPath, upperGeneratorsByPath);
        }
    }
    
    private void checkPathCase(String path)
    {
        if (!path.equals(path.toLowerCase()))
        {
            generateException(path, "Audit key entries may only be in lowercase to ensure case-insensitivity.");
        }
    }
    
    private void generateException(String path, String msg) throws AuditModelException
    {
        throw new AuditModelException("" +
                msg + "\n" +
                "   Application: " + applicationName + "\n" +
                "   Path:        " + path);
    }
}
