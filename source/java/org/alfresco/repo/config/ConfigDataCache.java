/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.cache.AbstractAsynchronouslyRefreshedCache;
import org.alfresco.repo.config.ConfigDataCache.ConfigData;
import org.alfresco.repo.config.xml.RepoXMLConfigService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.ConfigDeployment;
import org.springframework.extensions.config.ConfigImpl;
import org.springframework.extensions.config.ConfigSection;
import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.config.evaluator.Evaluator;
import org.springframework.extensions.config.xml.elementreader.ConfigElementReader;

import com.sun.star.uno.RuntimeException;

/**
 * An innder class that uses the {@link RepoXMLConfigService config service} to asynchronously
 * refresh tenant config data.
 * <p/>
 * Cluster-wide invalidation messages mean that we have to be very careful about only making
 * updates to caches when entries really change.  However, the nature of the {@link ConfigService}
 * hierarchy makes it very difficult to do this in a deterministic manner without performing
 * write locks that run across tenants.  By receiving asynchronous messages to refresh we no
 * longer have to rely on a cluster-aware cache.
 * 
 * @author Derek Hulley
 * @author Andy Hind
 * @since 4.1.5
 */
public class ConfigDataCache extends AbstractAsynchronouslyRefreshedCache<ConfigData>
{
    private static Log logger = LogFactory.getLog(ConfigDataCache.class);
    
    private RepoXMLConfigService repoXMLConfigService;

    /**
     * Set the config service.  Depending on the order of injection, this might have to
     * be done by code.
     */
    public void setRepoXMLConfigService(RepoXMLConfigService repoXMLConfigService)
    {
        this.repoXMLConfigService = repoXMLConfigService;
    }

    @Override
    protected ConfigData buildCache(String tenantId)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Received request to rebuild config data for tenant: " + tenantId);
        }
        ConfigData configData = repoXMLConfigService.getRepoConfig(tenantId);
        if (!(configData instanceof ImmutableConfigData))
        {
            throw new RuntimeException("ConfigData must be immutable for the cache.");
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("Rebuilt config data for tenant: " + tenantId + " (" + configData + ").");
        }
        return configData;
    }
    
    /**
     * Data containing all a tenant's required UI configuration
     * 
     * @author various
     */
    public static class ConfigData
    {
        private ConfigImpl globalConfig;   
        private Map<String, Evaluator> evaluators;
        private Map<String, List<ConfigSection>> sectionsByArea;
        private List<ConfigSection> sections;
        private Map<String, ConfigElementReader> elementReaders;
        
        private List<ConfigDeployment> configDeployments;
        
        public ConfigData()
        {
        }
        
        public ConfigImpl getGlobalConfig()
        {
            return globalConfig;
        }
        public void setGlobalConfig(ConfigImpl globalConfig)
        {
            this.globalConfig = globalConfig;
        }
        public Map<String, Evaluator> getEvaluators()
        {
            return evaluators;
        }
        public void setEvaluators(Map<String, Evaluator> evaluators)
        {
            this.evaluators = evaluators;
        }
        public Map<String, List<ConfigSection>> getSectionsByArea()
        {
            return sectionsByArea;
        }
        public void setSectionsByArea(Map<String, List<ConfigSection>> sectionsByArea)
        {
            this.sectionsByArea = sectionsByArea;
        }
        public List<ConfigSection> getSections()
        {
            return sections;
        }
        public void setSections(List<ConfigSection> sections)
        {
            this.sections = sections;
        }
        public Map<String, ConfigElementReader> getElementReaders()
        {
            return elementReaders;
        }
        public void setElementReaders(Map<String, ConfigElementReader> elementReaders)
        {
            this.elementReaders = elementReaders;
        }
        public List<ConfigDeployment> getConfigDeployments()
        {
            return configDeployments;
        }
        public void setConfigDeployments(List<ConfigDeployment> configDeployments)
        {
            this.configDeployments = configDeployments;
        }
    }
    
    /**
     * Immutable version of {@link ConfigData} to ensure cast-iron safety of data
     * being put into the caches.
     * 
     * @author Derek Hulley
     * @since 4.1.5
     */
    public static class ImmutableConfigData extends ConfigData
    {
        /**
         * Local variable to allow setter use during construction
         */
        private boolean locked = false;
        
        /**
         * Copy constructor that prevents any data from being changed.
         * 
         * @param configData            the config to copy
         */
        public ImmutableConfigData(ConfigData configData)
        {
            /*
             * Each member is copied or protected in some way to ensure immutability
             */
            
            List<ConfigDeployment> configDeployments = configData.getConfigDeployments();
            if (configDeployments != null)
            {
                List<ConfigDeployment> configDeploymentsLocked = Collections.unmodifiableList(configDeployments);
                setConfigDeployments(configDeploymentsLocked);
            }
            else
            {
                setConfigDeployments(Collections.<ConfigDeployment>emptyList());
            }
            
            Map<String, ConfigElementReader> elementReaders = configData.getElementReaders();
            if (elementReaders != null)
            {
                Map<String, ConfigElementReader> elementReadersLocked = Collections.unmodifiableMap(elementReaders);
                setElementReaders(elementReadersLocked);
            }
            else
            {
                setElementReaders(Collections.<String, ConfigElementReader>emptyMap());
            }
            
            Map<String, Evaluator> evaluators = configData.getEvaluators();
            if (evaluators != null)
            {
                Map<String, Evaluator> evaluatorsLocked = Collections.unmodifiableMap(evaluators);
                setEvaluators(evaluatorsLocked);
            }
            else
            {
                setEvaluators(Collections.<String, Evaluator>emptyMap());
            }
            
            ConfigImpl globalConfig = configData.getGlobalConfig();
            ImmutableConfig globalConfigLocked = new ImmutableConfig(globalConfig);
            setGlobalConfig(globalConfigLocked);
            
            List<ConfigSection> sections = configData.getSections();
            if (sections != null)
            {
                List<ConfigSection> sectionsLocked = Collections.unmodifiableList(sections);
                setSections(sectionsLocked);
            }
            else
            {
                setSections(Collections.<ConfigSection>emptyList());
            }
            
            Map<String, List<ConfigSection>> sectionsByArea = configData.getSectionsByArea();
            if (sectionsByArea != null)
            {
                Map<String, List<ConfigSection>> sectionsByAreaLocked = Collections.unmodifiableMap(sectionsByArea);
                setSectionsByArea(sectionsByAreaLocked);
            }
            else
            {
                setSectionsByArea(Collections.<String, List<ConfigSection>>emptyMap());
            }
            
            // Now prevent setters from being used
            locked = true;
        }

        @Override
        public void setGlobalConfig(ConfigImpl globalConfig)
        {
            if (locked)
            {
                throw new IllegalStateException("ConfigData has been locked.");
            }
            super.setGlobalConfig(globalConfig);
        }

        @Override
        public void setEvaluators(Map<String, Evaluator> evaluators)
        {
            if (locked)
            {
                throw new IllegalStateException("ConfigData has been locked.");
            }
            super.setEvaluators(evaluators);
        }

        @Override
        public void setSectionsByArea(Map<String, List<ConfigSection>> sectionsByArea)
        {
            if (locked)
            {
                throw new IllegalStateException("ConfigData has been locked.");
            }
            super.setSectionsByArea(sectionsByArea);
        }

        @Override
        public void setSections(List<ConfigSection> sections)
        {
            if (locked)
            {
                throw new IllegalStateException("ConfigData has been locked.");
            }
            super.setSections(sections);
        }

        @Override
        public void setElementReaders(Map<String, ConfigElementReader> elementReaders)
        {
            if (locked)
            {
                throw new IllegalStateException("ConfigData has been locked.");
            }
            super.setElementReaders(elementReaders);
        }

        @Override
        public void setConfigDeployments(List<ConfigDeployment> configDeployments)
        {
            if (locked)
            {
                throw new IllegalStateException("ConfigData has been locked.");
            }
            super.setConfigDeployments(configDeployments);
        }
    }
}