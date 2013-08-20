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
package org.alfresco.repo.content.transform;

import java.util.Properties;

import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.TransformationOptionLimits;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Provides access to transformer configuration and current performance data.
 * 
 * @author Alan Davis
 */
public class TransformerConfigImpl extends AbstractLifecycleBean implements TransformerConfig
{
    private MimetypeService mimetypeService;
    
    private ContentService contentService;

    private ContentTransformerRegistry transformerRegistry;
    
    private TransformerDebug transformerDebug;

    // Log
    private TransformerLog transformerLog;
    
    // Log Debug
    private TransformerDebugLog transformerDebugLog;

    // Holds statistics about each transformer, sourceMimeType and targetMimetype combination.
    // A null transformer is the system wide value. Null sourceMimeType and targetMimetype values are
    // transformer wide summaries.
    private TransformerConfigStatistics statistics;

    // Transformer limits.
    private TransformerConfigLimits limits;
    
    // Supported and unsupported transformations.
    private TransformerConfigSupported supported;
    
    // Priorities
    private TransformerConfigProperty priorities;
    
    // Threshold counts - Initially there will only be the system wide value, but
    // having this structure provides flexibility.
    private TransformerConfigProperty thresholdCounts;
    
    // Times to be recorded if there is an error - Initially there will only be the system wide value, but
    // having this structure provides flexibility.
    private TransformerConfigProperty errorTimes;
    
    // For backward compatibility where priority could not be set, with AMPs that need to have their
    // transformer used rather than an inbuilt one. Achieved by making the inbuilt transformers look
    // poor. Generally contains no entries, other than the system wide 0 values.
    private TransformerConfigProperty initialAverageTimes;
    private TransformerConfigProperty initialCounts;
    
    private TransformerPropertySetter propertySetter;
    
    // Needed to read properties.
    private ChildApplicationContextFactory subsystemFactory;
    
    // Needed to read global properties.
    private Properties globalProperties;
    
    private TransformerProperties transformerProperties;
    
    private TransformerConfigDynamicTransformers dynamicTransformers;

    /**
     * Sets of the mimetype service.
     * 
     * @param mimetypeService
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public void setContentTransformerRegistry(ContentTransformerRegistry transformerRegistry)
    {
        this.transformerRegistry = transformerRegistry;
    }

    public void setTransformerDebug(TransformerDebug transformerDebug)
    {
        this.transformerDebug = transformerDebug;
    }

    public void setTransformerLog(TransformerLog transformerLog)
    {
        this.transformerLog = transformerLog;
    }

    public void setTransformerDebugLog(TransformerDebugLog transformerDebugLog)
    {
        this.transformerDebugLog = transformerDebugLog;
    }

    public void setGlobalProperties(Properties globalProperties)
    {
        this.globalProperties = globalProperties;
    }

    /**
     * Called by spring after bean is initialised.
     */
    public void initialise()
    {
        ChildApplicationContextFactory subsystem = getSubsystem();
        transformerProperties = new TransformerProperties(subsystem, globalProperties);
        
        dynamicTransformers = new TransformerConfigDynamicTransformers(this, transformerProperties, mimetypeService,
                contentService, transformerRegistry, transformerDebug);
        statistics= new TransformerConfigStatistics(this, mimetypeService);
        limits = new TransformerConfigLimits(transformerProperties, mimetypeService);
        supported = new TransformerConfigSupported(transformerProperties, mimetypeService);
        priorities = new TransformerConfigProperty(transformerProperties, mimetypeService, PRIORITY, Integer.toString(PRIORITY_DEFAULT));
        thresholdCounts = new TransformerConfigProperty(transformerProperties, mimetypeService, THRESHOLD_COUNT, "3");
        errorTimes = new TransformerConfigProperty(transformerProperties, mimetypeService, ERROR_TIME, "120000");
        initialAverageTimes = new TransformerConfigProperty(transformerProperties, mimetypeService, INITIAL_TIME, "0");
        initialCounts = new TransformerConfigProperty(transformerProperties, mimetypeService, INITIAL_COUNT, "100000");
        propertySetter = new TransformerPropertySetter(transformerProperties, mimetypeService, transformerRegistry);
    }
    
    /**
     * Returns the 'transformers' subsystem which among other things holds transformer properties.  
     */
    synchronized ChildApplicationContextFactory getSubsystem()
    {
        if (subsystemFactory == null)
        {
            subsystemFactory = getApplicationContext().getBean("Transformers", ChildApplicationContextFactory.class);
        }
        return subsystemFactory;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
    }
    
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        dynamicTransformers.removeTransformers(transformerRegistry);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getProperty(String name)
    {
        return transformerProperties.getProperty(name);
    }
    
    @Override
    public String getProperties(boolean changesOnly)
    {
        return new TransformerPropertyGetter(changesOnly, transformerProperties, mimetypeService,
                transformerRegistry, transformerLog, transformerDebugLog).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int setProperties(String propertyNamesAndValues)
    {
        return propertySetter.setProperties(propertyNamesAndValues);
    }

    @Override
    public int removeProperties(String propertyNames)
    {
        return propertySetter.removeProperties(propertyNames);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public TransformerStatistics getStatistics(ContentTransformer transformer, String sourceMimetype, String targetMimetype, boolean createNew)
    {
        return statistics.getStatistics(transformer, sourceMimetype, targetMimetype, createNew);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public TransformationOptionLimits getLimits(ContentTransformer transformer, String sourceMimetype,
            String targetMimetype, String use)
    {
        return limits.getLimits(transformer, sourceMimetype, targetMimetype, use);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSupportedTransformation(ContentTransformer transformer, String sourceMimetype,
            String targetMimetype, TransformationOptions options)
    {
        return supported.isSupportedTransformation(transformer, sourceMimetype, targetMimetype, options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority(ContentTransformer transformer, String sourceMimetype, String targetMimetype)
    {
        try
        {
            return priorities.getInt(transformer, sourceMimetype, targetMimetype);
        }
        catch (NumberFormatException e1)
        {
            try
            {
                return priorities.getInt(null, null, null);
            }
            catch (NumberFormatException e2)
            {
                return 0;
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getThresholdCount(ContentTransformer transformer, String sourceMimetype, String targetMimetype)
    {
        try
        {
            return thresholdCounts.getInt(transformer, sourceMimetype, targetMimetype);
        }
        catch (NumberFormatException e1)
        {
            try
            {
                return thresholdCounts.getInt(null, null, null);
            }
            catch (NumberFormatException e2)
            {
                return 0;
            }
        }
    }
    
    /**
     * Gets the time to be recorded if there is an error.
     */
    long getErrorTime(ContentTransformer transformer, String sourceMimetype, String targetMimetype)
    {
        try
        {
            return errorTimes.getLong(transformer, sourceMimetype, targetMimetype);
        }
        catch (NumberFormatException e1)
        {
            try
            {
                return errorTimes.getInt(null, null, null);
            }
            catch (NumberFormatException e2)
            {
                return 0;
            }
        }
    }
    
    /**
     * Gets the initial average time to be set for a transformer. Used historically to set the priority of transformers in AMPs.
     * The initial count value may also obtained via {@link #getInitialCount(ContentTransformer, String, String)}.
     */
    long getInitialAverageTime(ContentTransformer transformer, String sourceMimetype, String targetMimetype)
    {
        try
        {
            return initialAverageTimes.getLong(transformer, sourceMimetype, targetMimetype);
        }
        catch (NumberFormatException e1)
        {
            try
            {
                return initialAverageTimes.getInt(null, null, null);
            }
            catch (NumberFormatException e2)
            {
                return 0;
            }
        }
    }
    
    /**
     * Gets the initial transformer count to be set for a transformer. Used historically to set the priority of transformers in AMPs.
     * Only called if {@link #getInitialAverageTime(ContentTransformer, String, String)} returns a value larger than 0.
     */
    int getInitialCount(ContentTransformer transformer, String sourceMimetype, String targetMimetype)
    {
        try
        {
            return initialCounts.getInt(transformer, sourceMimetype, targetMimetype);
        }
        catch (NumberFormatException e1)
        {
            try
            {
                return initialCounts.getInt(null, null, null);
            }
            catch (NumberFormatException e2)
            {
                return 0;
            }
        }
    }
}
