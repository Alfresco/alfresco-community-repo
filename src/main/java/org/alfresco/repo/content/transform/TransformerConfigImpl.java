/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.content.transform;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.rendition2.LegacySynchronousTransformClient;
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.service.cmr.repository.MalformedNodeRefException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TransformationOptionLimits;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.descriptor.DescriptorService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Provides access to transformer configuration and current performance data.
 * 
 * @author Alan Davis
 *
 * @deprecated The RenditionService is being replace by the simpler async RenditionService2.
 */
@Deprecated
public class TransformerConfigImpl extends AbstractLifecycleBean implements TransformerConfig
{
    private static final Log logger = LogFactory.getLog(TransformerConfigImpl.class);

    private MimetypeService mimetypeService;
    
    private LegacySynchronousTransformClient legacySynchronousTransformClient;

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
    
    // Blacklist
    private TransformerConfigProperty blacklist;
    
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
    
    private ModuleService moduleService;
    
    private DescriptorService descriptorService;
    
    private TransformerProperties transformerProperties;
    
    private TransformerConfigDynamicTransformers dynamicTransformers;

    private Map<String, Set<String>> strictMimetypeExceptions;

    /**
     * Sets of the mimetype service.
     * 
     * @param mimetypeService
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    public void setLegacySynchronousTransformClient(LegacySynchronousTransformClient legacySynchronousTransformClient)
    {
        this.legacySynchronousTransformClient = legacySynchronousTransformClient;
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

    public void setModuleService(ModuleService moduleService)
    {
        this.moduleService = moduleService;
    }
    
    public void setDescriptorService(DescriptorService descriptorService)
    {
        this.descriptorService = descriptorService;
    }

    /**
     * Called by spring after bean is initialised.
     */
    public void initialise()
    {
        ChildApplicationContextFactory subsystem = getSubsystem();
        transformerProperties = new TransformerProperties(subsystem, globalProperties);
        
        dynamicTransformers = new TransformerConfigDynamicTransformers(this, transformerProperties, mimetypeService,
                legacySynchronousTransformClient, transformerRegistry, transformerDebug, moduleService, descriptorService, globalProperties);
        statistics= new TransformerConfigStatistics(this, mimetypeService);
        limits = new TransformerConfigLimits(transformerProperties, mimetypeService);
        supported = new TransformerConfigSupported(transformerProperties, mimetypeService);
        priorities = new TransformerConfigProperty(transformerProperties, mimetypeService, PRIORITY, Integer.toString(PRIORITY_DEFAULT));
        blacklist = new TransformerConfigProperty(transformerProperties, mimetypeService, BLACKLIST, "");
        thresholdCounts = new TransformerConfigProperty(transformerProperties, mimetypeService, THRESHOLD_COUNT, "3");
        errorTimes = new TransformerConfigProperty(transformerProperties, mimetypeService, ERROR_TIME, "120000");
        initialAverageTimes = new TransformerConfigProperty(transformerProperties, mimetypeService, INITIAL_TIME, "0");
        initialCounts = new TransformerConfigProperty(transformerProperties, mimetypeService, INITIAL_COUNT, "100000");
        propertySetter = new TransformerPropertySetter(transformerProperties, mimetypeService, transformerRegistry);
        strictMimetypeExceptions = getStrictMimetypeExceptions(transformerProperties);
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
        return statistics.getStatistics(transformer, stdMimetype(sourceMimetype), stdMimetype(targetMimetype), createNew);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public TransformationOptionLimits getLimits(ContentTransformer transformer, String sourceMimetype,
            String targetMimetype, String use)
    {
        return limits.getLimits(transformer, stdMimetype(sourceMimetype), stdMimetype(targetMimetype), use);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSupportedTransformation(ContentTransformer transformer, String sourceMimetype,
            String targetMimetype, TransformationOptions options)
    {
        return supported.isSupportedTransformation(transformer, stdMimetype(sourceMimetype), stdMimetype(targetMimetype), options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority(ContentTransformer transformer, String sourceMimetype, String targetMimetype)
    {
        try
        {
            return priorities.getInt(transformer, stdMimetype(sourceMimetype), stdMimetype(targetMimetype));
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
    public List<NodeRef> getBlacklist(ContentTransformer transformer, String sourceMimetype, String targetMimetype)
    {
        try
        {
            return blacklist.getNodeRefs(transformer, stdMimetype(sourceMimetype), stdMimetype(targetMimetype));
        }
        catch (MalformedNodeRefException e1)
        {
            try
            {
                return priorities.getNodeRefs(null, null, null);
            }
            catch (MalformedNodeRefException e2)
            {
                return null;
            }
        }
    }
    
    // Build up a Map keyed on declared source node mimetype to a Set of detected mimetypes that should allow
    // the transformation to take place. i.e. The cases that Tika gets wrong.
    private Map<String, Set<String>> getStrictMimetypeExceptions(TransformerProperties transformerProperties2)
    {
        Map<String, Set<String>> strictMimetypeExceptions = new HashMap<>();
        
        String whitelist = getProperty(STRICT_MIMETYPE_CHECK_WHITELIST_MIMETYPES);
        whitelist = whitelist == null ? "" : whitelist.trim();
        if (whitelist.length() > 0)
        {
            String[] mimetypes = whitelist.split(";");
            
            if (mimetypes.length % 2 != 0)
            {
                logger.error(STRICT_MIMETYPE_CHECK_WHITELIST_MIMETYPES+" should have an even number of mimetypes as a ; separated list.");
            }
            else
            {
                Set<String> detectedMimetypes = null;
                for (String mimetype: mimetypes)
                {
                    mimetype = mimetype.trim();
                    if (mimetype.isEmpty())
                    {
                        logger.error(STRICT_MIMETYPE_CHECK_WHITELIST_MIMETYPES+" contains a blank mimetype.");
                        // Still okay to use it in the map though, but it will be ignored.
                    }

                    if (detectedMimetypes == null)
                    {
                        detectedMimetypes = strictMimetypeExceptions.get(mimetype);
                        if (detectedMimetypes == null)
                        {
                            detectedMimetypes = new HashSet<>();
                            strictMimetypeExceptions.put(mimetype, detectedMimetypes);
                        }
                    }
                    else
                    {
                        detectedMimetypes.add(mimetype);
                        detectedMimetypes = null;
                    }
                }
            }
        }

        return strictMimetypeExceptions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean strictMimetypeCheck(String declaredMimetype, String detectedMimetype)
    {
        if (detectedMimetype == null)
        {
            return true;
        }
        
        Set<String> detectedMimetypes = strictMimetypeExceptions.get(declaredMimetype);
        return detectedMimetypes != null && detectedMimetypes.contains(detectedMimetype);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getThresholdCount(ContentTransformer transformer, String sourceMimetype, String targetMimetype)
    {
        try
        {
            return thresholdCounts.getInt(transformer, stdMimetype(sourceMimetype), stdMimetype(targetMimetype));
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
            return errorTimes.getLong(transformer, stdMimetype(sourceMimetype), stdMimetype(targetMimetype));
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
            return initialAverageTimes.getLong(transformer, stdMimetype(sourceMimetype), stdMimetype(targetMimetype));
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
            return initialCounts.getInt(transformer, stdMimetype(sourceMimetype), stdMimetype(targetMimetype));
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
    
    // Returns the main or standard mimetype. Needed were multiple mimetypes share the same extension or are unknown so binary. 
    private String stdMimetype(String mimetype)
    {
    	return mimetypeService.getMimetype(mimetypeService.getExtension(mimetype));
    }
}
