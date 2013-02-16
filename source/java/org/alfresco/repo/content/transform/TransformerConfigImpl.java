/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.TransformationOptionLimits;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Provides access to transformer configuration and current performance data.
 * 
 * @author Alan Davis
 */
public class TransformerConfigImpl extends AbstractLifecycleBean implements TransformerConfig
{
    /** The logger. */
    private static Log logger = LogFactory.getLog(TransformerConfigImpl.class);
    
    private MimetypeService mimetypeService;

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
    
    // Needed to read properties.
    private ChildApplicationContextFactory subsystemFactory;
    
    /**
     * Sets of the mimetype service.
     * 
     * @param mimetypeService
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    /**
     * Called by spring after bean is initialised.
     */
    public void initialise()
    {
        statistics= new TransformerConfigStatistics(this, mimetypeService);
        limits = new TransformerConfigLimits(getSubsystem(), mimetypeService);
        supported = new TransformerConfigSupported(getSubsystem(), mimetypeService);
        priorities = new TransformerConfigProperty(getSubsystem(), mimetypeService, PRIORITY);
        thresholdCounts = new TransformerConfigProperty(getSubsystem(), mimetypeService, THRESHOLD_COUNT);
        errorTimes = new TransformerConfigProperty(getSubsystem(), mimetypeService, ERROR_TIME);
        initialAverageTimes = new TransformerConfigProperty(getSubsystem(), mimetypeService, INITIAL_TIME);
        initialCounts = new TransformerConfigProperty(getSubsystem(), mimetypeService, INITIAL_COUNT);
    }
    
    /**
     * Returns the 'transformers' subsystem which among other things holds transformer properties.  
     */
    private synchronized ChildApplicationContextFactory getSubsystem()
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
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getProperty(String name)
    {
        return getSubsystem().getProperty(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperty(String propertyNameAndValue)
    {
        int i = propertyNameAndValue.indexOf('=');
        String name = i != -1 ? propertyNameAndValue.substring(0, i) : propertyNameAndValue;
        String value = i != -1 ? propertyNameAndValue.substring(i+1) : "";
        getSubsystem().setProperty(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransformerStatistics getStatistics(ContentTransformer transformer, String sourceMimetype, String targetMimetype)
    {
        return statistics.getStatistics(transformer, sourceMimetype, targetMimetype);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public TransformationOptionLimits getLimits(ContentTransformer transformer, String sourceMimetype,
            String targetMimetype)
    {
        return limits.getLimits(transformer, sourceMimetype, targetMimetype);
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
        return priorities.getInt(transformer, sourceMimetype, targetMimetype);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getThresholdCount(ContentTransformer transformer, String sourceMimetype, String targetMimetype)
    {
        return thresholdCounts.getInt(transformer, sourceMimetype, targetMimetype);
    }
    
    /**
     * Gets the time to be recorded if there is an error.
     */
    long getErrorTime(ContentTransformer transformer, String sourceMimetype, String targetMimetype)
    {
        return errorTimes.getLong(transformer, sourceMimetype, targetMimetype);
    }
    
    /**
     * Gets the initial average time to be set for a transformer. Used historically to set the priority of transformers in AMPs.
     * The initial count value may also obtained via {@link #getInitialCount(ContentTransformer, String, String)}.
     */
    long getInitialAverageTime(ContentTransformer transformer, String sourceMimetype, String targetMimetype)
    {
        return initialAverageTimes.getLong(transformer, sourceMimetype, targetMimetype);
    }
    
    /**
     * Gets the initial transformer count to be set for a transformer. Used historically to set the priority of transformers in AMPs.
     * Only called if {@link #getInitialAverageTime(ContentTransformer, String, String)} returns a value larger than 0.
     */
    int getInitialCount(ContentTransformer transformer, String sourceMimetype, String targetMimetype)
    {
        return initialCounts.getInt(transformer, sourceMimetype, targetMimetype);
    }
}
