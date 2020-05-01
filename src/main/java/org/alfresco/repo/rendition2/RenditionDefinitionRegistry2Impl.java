/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.rendition2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.transform.client.registry.TransformServiceRegistry;
import org.alfresco.util.ConfigFileFinder;
import org.alfresco.util.ConfigScheduler;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.ShutdownIndicator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronExpression;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A registry of rendition definitions.
 *
 * @author adavis
 */
public class RenditionDefinitionRegistry2Impl implements RenditionDefinitionRegistry2, InitializingBean
{
    private static final Log log = LogFactory.getLog(RenditionDefinitionRegistry2Impl.class);

    static class Data
    {
        Map<String, RenditionDefinition2> renditionDefinitions = new HashMap();
        Map<String, Set<Pair<String, Long>>> renditionsFor = new HashMap<>();
        private int fileCount;
        private int staticCount;

        /**
         * @param currentData (optional) the registry's current data. Used to preload renditions that have been loaded
         *                    from a source other than one of the dynamically loadable JSON files. Normally static
         *                    Spring beans. These must be added again as the registry does not know where to look for
         *                    them.
         */
        public Data(Data currentData)
        {
            if (currentData != null)
            {
                currentData.renditionDefinitions.forEach((renditionName, renditionDefinition) ->
                {
                    if (renditionDefinition instanceof RenditionDefinition2Impl &&
                        !((RenditionDefinition2Impl)renditionDefinition).isDynamicallyLoaded())
                    {
                        log.debug("Adding static rendition "+renditionName+" back into the registry");
                        renditionDefinitions.put(renditionName, renditionDefinition);
                        staticCount++;
                    }
                });
            }
        }

        @Override
        public String toString()
        {
            int renditionCount = renditionDefinitions.size();
            return "(renditions: "+renditionCount+" files: "+fileCount+" static: "+staticCount+")";
        }

        public void setFileCount(int fileCount)
        {
            this.fileCount = fileCount;
        }
    }

    static class RenditionDef
    {
        private String renditionName;
        private String targetMediaType;
        private Set<RenditionOpt> options;

        public void setRenditionName(String renditionName)
        {
            this.renditionName = renditionName;
        }

        public void setTargetMediaType(String targetMediaType)
        {
            this.targetMediaType = targetMediaType;
        }

        public void setOptions(Set<RenditionOpt> options)
        {
            this.options = options;
        }
    }

    static class RenditionOpt
    {
        private String name;
        private String value;

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getValue()
        {
            return value;
        }

        public void setValue(String value)
        {
            this.value = value;
        }
    }

    private TransformServiceRegistry transformServiceRegistry;
    private String renditionConfigDir;
    private String timeoutDefault;
    private ObjectMapper jsonObjectMapper;
    private CronExpression cronExpression;
    private CronExpression initialAndOnErrorCronExpression;
    private boolean firstTime = true;

    private ConfigScheduler<Data> configScheduler = new ConfigScheduler(this)
    {
        @Override
        public boolean readConfig() throws IOException
        {
            if (configFileFinder != null)
            {
                configFileFinder.setFileCount(0);
            }
            return RenditionDefinitionRegistry2Impl.this.readConfig();
        }

        @Override
        public Object createData()
        {
            return RenditionDefinitionRegistry2Impl.this.createData();
        }
    };

    // Only for use in testing
    void reloadRegistry()
    {
        configScheduler.readConfigAndReplace(false);
    }

    private ConfigFileFinder configFileFinder;

    public void setTransformServiceRegistry(TransformServiceRegistry transformServiceRegistry)
    {
        this.transformServiceRegistry = transformServiceRegistry;
    }

    public void setRenditionConfigDir(String renditionConfigDir)
    {
        this.renditionConfigDir = renditionConfigDir;
    }

    public void setTimeoutDefault(String timeoutDefault)
    {
        this.timeoutDefault = timeoutDefault;
    }

    public void setJsonObjectMapper(ObjectMapper jsonObjectMapper)
    {
        this.jsonObjectMapper = jsonObjectMapper;
    }

    public CronExpression getCronExpression()
    {
        return cronExpression;
    }

    public void setCronExpression(CronExpression cronExpression)
    {
        this.cronExpression = cronExpression;
    }

    public CronExpression getInitialAndOnErrorCronExpression()
    {
        return initialAndOnErrorCronExpression;
    }

    public void setInitialAndOnErrorCronExpression(CronExpression initialAndOnErrorCronExpression)
    {
        this.initialAndOnErrorCronExpression = initialAndOnErrorCronExpression;
    }

    public void setShutdownIndicator(ShutdownIndicator shutdownIndicator)
    {
        configScheduler.setShutdownIndicator(shutdownIndicator);
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "transformServiceRegistry", transformServiceRegistry);
        PropertyCheck.mandatory(this, "timeoutDefault", timeoutDefault);
        PropertyCheck.mandatory(this, "jsonObjectMapper", jsonObjectMapper);
        // If we have a cronExpression it indicates that we will schedule reading.
        if (cronExpression != null)
        {
            PropertyCheck.mandatory(this, "initialAndOnErrorCronExpression", initialAndOnErrorCronExpression);
        }
        configFileFinder = new ConfigFileFinder(jsonObjectMapper)
        {
            @Override
            protected void readJson(JsonNode jsonNode, String readFromMessage, String baseUrl) throws IOException
            {
                try
                {
                    JsonNode renditions = jsonNode.get("renditions");
                    if (renditions != null && renditions.isArray())
                    {
                        for (JsonNode rendition : renditions)
                        {
                            RenditionDef def = jsonObjectMapper.convertValue(rendition, RenditionDef.class);
                            Map<String, String> map = new HashMap<>();
                            if (def.options != null)
                            {
                                def.options.forEach(o -> map.put(o.name, o.value));
                            }
                            if (!map.containsKey(RenditionDefinition2.TIMEOUT))
                            {
                                map.put(RenditionDefinition2.TIMEOUT, timeoutDefault);
                            }
                            RenditionDefinition2 original = getRenditionDefinition(def.renditionName);
                            new RenditionDefinition2Impl(def.renditionName, def.targetMediaType, map, true,
                                    RenditionDefinitionRegistry2Impl.this);
                            if (original != null)
                            {
                                log.debug(readFromMessage+" replaced the rendition "+def.renditionName);
                            }
                        }
                    }
                }
                catch (IllegalArgumentException e)
                {
                    log.error("Error reading "+readFromMessage+" "+e.getMessage());
                }
            }
        };
        configScheduler.run(true, log, cronExpression, initialAndOnErrorCronExpression);
    }

    public Data createData()
    {
        Data currentData = null;
        if (firstTime)
        {
            firstTime = false;
        }
        else
        {
            currentData = getData();
        }
        return new Data(currentData);
    }

    public Data getData()
    {
        return configScheduler.getData();
    }

    public boolean readConfig()
    {
        boolean successReadingConfig = configFileFinder.readFiles("alfresco/renditions", log);
        if (renditionConfigDir != null && !renditionConfigDir.isBlank())
        {
            successReadingConfig &= configFileFinder.readFiles(renditionConfigDir, log);
        }
        return successReadingConfig;
    }

    public boolean isEnabled()
    {
        return true;
    }

    /**
     * Obtains a {@link RenditionDefinition2} by name.
     * @param renditionName to be returned
     * @return the {@link RenditionDefinition2} or null if not registered.
     * @deprecated use {@link #getRenditionDefinition(String)}
     */
    public RenditionDefinition2 getDefinition(String renditionName)
    {
        return getRenditionDefinition(renditionName);
    }

    public void register(RenditionDefinition2 renditionDefinition)
    {
        String renditionName = renditionDefinition.getRenditionName();
        Data data = getData();
        // There may already be a rendition defined, but an extension may replace it.
        // This is logged in a caller of this method were the file name is known.
        data.renditionDefinitions.put(renditionName, renditionDefinition);

        if (renditionDefinition instanceof RenditionDefinition2Impl &&
            !((RenditionDefinition2Impl)renditionDefinition).isDynamicallyLoaded())
        {
            log.debug("Adding static rendition "+renditionName+" into the registry");
            data.staticCount++;
        }
        data.setFileCount(configFileFinder == null ? 0 : configFileFinder.getFileCount());
    }

    public void unregister(String renditionName)
    {
        Data data = getData();
        if (data.renditionDefinitions.remove(renditionName) == null)
        {
            throw new IllegalArgumentException("RenditionDefinition "+renditionName+" was not registered.");
        }
    }

    @Override
    public Set<String> getRenditionNames()
    {
        return getData().renditionDefinitions.keySet();
    }

    @Override
    public Set<String> getRenditionNamesFrom(String sourceMimetype, long size)
    {
        Set<Pair<String, Long>> renditionNamesWithMaxSize;
        Data data = getData();
        synchronized (data.renditionsFor)
        {
            renditionNamesWithMaxSize = data.renditionsFor.get(sourceMimetype);
            if (renditionNamesWithMaxSize == null)
            {
                renditionNamesWithMaxSize = getRenditionNamesWithMaxSize(sourceMimetype);
                data.renditionsFor.put(sourceMimetype, renditionNamesWithMaxSize);
            }
        }

        if (renditionNamesWithMaxSize.isEmpty())
        {
            return Collections.emptySet();
        }

        Set<String> renditionNames = new HashSet<>();
        for (Pair<String, Long> pair : renditionNamesWithMaxSize)
        {
            Long maxSize = pair.getSecond();
            if (maxSize != 0 && (maxSize == -1L || maxSize >= size))
            {
                String renditionName = pair.getFirst();
                renditionNames.add(renditionName);
            }
        }
        return renditionNames;
    }

    // Gets a list of rendition names that can be created from the given sourceMimetype.
    // Includes the maxSize for each.
    private Set<Pair<String,Long>> getRenditionNamesWithMaxSize(String sourceMimetype)
    {
        Set<Pair<String,Long>> renditions = new HashSet();
        Data data = getData();
        for (Map.Entry<String, RenditionDefinition2> entry : data.renditionDefinitions.entrySet())
        {
            RenditionDefinition2 renditionDefinition2 = entry.getValue();
            String targetMimetype = renditionDefinition2.getTargetMimetype();
            String renditionName = renditionDefinition2.getRenditionName();
            Map<String, String> options = renditionDefinition2.getTransformOptions();
            Long maxSize = transformServiceRegistry.findMaxSize(sourceMimetype, targetMimetype, options, renditionName);
            if (maxSize != null)
            {
                String renditionNameMaxSizePair = entry.getKey();
                Pair<String, Long> pair = new Pair<>(renditionNameMaxSizePair, maxSize);
                renditions.add(pair);
            }
        }
        return renditions;
    }

    @Override
    public RenditionDefinition2 getRenditionDefinition(String renditionName)
    {
        Data data = getData();
        return data.renditionDefinitions.get(renditionName);
    }
}
