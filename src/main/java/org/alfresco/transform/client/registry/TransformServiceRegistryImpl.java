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
package org.alfresco.transform.client.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.util.ConfigScheduler;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.ShutdownIndicator;
import org.apache.commons.logging.Log;
import org.quartz.CronExpression;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.alfresco.transform.client.registry.TransformRegistryHelper.retrieveTransformListBySize;

/**
 * Used by clients to work out if a transformation is supported by the Transform Service.
 */
public abstract class TransformServiceRegistryImpl extends AbstractTransformRegistry implements InitializingBean
{
    public static class Data extends TransformCache
    {
        private int tEngineCount = 0;
        private int fileCount;
        boolean firstTime = true;

        @Override
        public String toString()
        {
            return transformerCount == 0 && transformCount == 0 && tEngineCount == 0 && fileCount == 0
                    ? ""
                    : "(transformers: "+transformerCount+" transforms: "+transformCount+" t-engines: "+tEngineCount+" files: "+fileCount+")";
        }

        public void setTEngineCount(int tEngineCount)
        {
            this.tEngineCount = tEngineCount;
        }

        public void setFileCount(int fileCount)
        {
            this.fileCount = fileCount;
        }
    }

    protected boolean enabled = true;
    private ObjectMapper jsonObjectMapper;
    private CronExpression cronExpression;
    private CronExpression initialAndOnErrorCronExpression;

    private ConfigScheduler<Data> configScheduler = new ConfigScheduler<Data>(this) // Don't change to <> as the release:prepare fails!
    {
        @Override
        public boolean readConfig() throws IOException
        {
            return TransformServiceRegistryImpl.this.readConfig();
        }

        @Override
        public Data createData()
        {
            return TransformServiceRegistryImpl.this.createData();
        }
    };

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
        PropertyCheck.mandatory(this, "jsonObjectMapper", jsonObjectMapper);
        // If we have a cronExpression it indicates that we will schedule reading.
        if (cronExpression != null)
        {
            PropertyCheck.mandatory(this, "initialAndOnErrorCronExpression", initialAndOnErrorCronExpression);
        }

        Log log = getLog();
        configScheduler.run(enabled, log, cronExpression, initialAndOnErrorCronExpression);
    }

    public Data createData()
    {
        return new Data();
    }

    public Data getData()
    {
        return configScheduler.getData();
    }

    public abstract boolean readConfig() throws IOException;

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        setFirstTime(true);
    }

    protected void setFirstTime(boolean firstTime)
    {
        getData().firstTime = firstTime;
    }

    protected boolean getFirstTime()
    {
        return getData().firstTime;
    }

    protected abstract Log getLog();

    @Override
    protected void logError(String msg)
    {
        getLog().error(msg);
    }

    /**
     * Works out an ordered list of transformer that will be used to transform content of a given source mimetype
     * into a target mimetype given a list of actual transform option names and values (Strings) plus the data contained
     * in the Transform objects registered with this class. These are ordered by size and priority.
     *
     * @param sourceMimetype    the mimetype of the source content
     * @param targetMimetype    the mimetype of the target
     * @param actualOptions     the actual name value pairs available that could be passed to the Transform Service.
     * @param renditionName     (optional) name for the set of options and target mimetype. If supplied is used to cache
     *                          results to avoid having to work out if a given transformation is supported a second time.
     *                          The sourceMimetype and sourceSizeInBytes may still change. In the case of ACS this is the
     *                          rendition name.
     */
    public List<SupportedTransform> findTransformers(final String sourceMimetype, final String targetMimetype,
                                                     final Map<String, String> actualOptions,
                                                     final String renditionName)
    {
        return retrieveTransformListBySize(getData(), sourceMimetype, targetMimetype, actualOptions,
                renditionName);
    }
}
