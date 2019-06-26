/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.transform.client.model.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.alfresco.repo.rendition2.RenditionDefinition2.TIMEOUT;

/**
 * Used by clients to work out if a transformation is supported by the Transform Service.
 */
public abstract class TransformServiceRegistryImpl implements TransformServiceRegistry, InitializingBean
{
    public static class Data
    {
        ConcurrentMap<String, ConcurrentMap<String, List<SupportedTransform>>> transformers = new ConcurrentHashMap<>();
        ConcurrentMap<String, ConcurrentMap<String, List<SupportedTransform>>> cachedSupportedTransformList = new ConcurrentHashMap<>();
        private int transformerCount = 0;
        private int transformCount = 0;
        boolean firstTime = true;
        boolean successReadingRemoteConfig = true;
    }

    static class SupportedTransform
    {
        TransformOptionGroup transformOptions;
        long maxSourceSizeBytes;
        private String name;
        private int priority;

        public SupportedTransform(Data data, String name, List<TransformOption> transformOptions, long maxSourceSizeBytes, int priority)
        {
            // Logically the top level TransformOptionGroup is required, so that child options are optional or required
            // based on their own setting.
            this.transformOptions = new TransformOptionGroup(true, transformOptions);
            this.maxSourceSizeBytes = maxSourceSizeBytes;
            this.name = name;
            this.priority = priority;
            data.transformCount++;
        }
    }

    public static class TransformServiceRegistryJob implements Job
    {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException
        {
            JobDataMap dataMap = context.getJobDetail().getJobDataMap();
            TransformServiceRegistryImpl registry = (TransformServiceRegistryImpl)dataMap.get("registry");
            registry.readConfigAndReplace();
        }
    }

    protected boolean enabled = true;
    protected Data data;
    private ObjectMapper jsonObjectMapper;
    private Scheduler scheduler;
    private CronExpression cronExpression;
    private CronExpression initialAndOnErrorCronExpression;
    private boolean normalCronSchedule;

    public void setJsonObjectMapper(ObjectMapper jsonObjectMapper)
    {
        this.jsonObjectMapper = jsonObjectMapper;
    }

    public synchronized Scheduler getScheduler()
    {
        return scheduler;
    }

    public synchronized void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
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

    @Override
    public void afterPropertiesSet() throws Exception
    {
        if (jsonObjectMapper == null)
        {
            throw new IllegalStateException("jsonObjectMapper has not been set");
        }
        PropertyCheck.mandatory(this, "cronExpression", cronExpression);
        PropertyCheck.mandatory(this, "initialAndOnErrorCronExpression", initialAndOnErrorCronExpression);

        setData(null);
        if (enabled)
        {
            schedule();
        }
    }

    private synchronized void schedule()
    {
        // Don't do an initial readConfigAndReplace() as the first scheduled read can be done almost instantly and
        // there is little point doing two in the space of a few seconds. If however the scheduler is already running
        // we do need to run it (this is only from test cases).
        if (scheduler == null)
        {
            StdSchedulerFactory sf = new StdSchedulerFactory();
            String jobName = getClass().getName()+"Job";
            try
            {
                scheduler = sf.getScheduler();

                JobDetail job = JobBuilder.newJob()
                        .withIdentity(jobName)
                        .ofType(TransformServiceRegistryJob.class)
                        .build();
                job.getJobDataMap().put("registry", this);
                CronExpression cronExpression = normalCronSchedule ? this.cronExpression : initialAndOnErrorCronExpression;
                CronTrigger trigger = TriggerBuilder.newTrigger()
                        .withIdentity(jobName+"Trigger", Scheduler.DEFAULT_GROUP)
                        .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                        .build();
                scheduler.startDelayed(0);
                scheduler.scheduleJob(job, trigger);
            }
            catch (SchedulerException e)
            {
                getLog().error("Failed to start "+jobName+" "+e.getMessage());
            }
        }
        else
        {
            readConfigAndReplace();
        }
    }

    protected void readConfigAndReplace()
    {
        boolean successReadingRemoteConfig = true;
        Log log = getLog();
        log.debug("Config read started");
        try
        {
            Data data = readConfig();
            successReadingRemoteConfig = data.successReadingRemoteConfig;
            setData(data);
            log.debug("Config read finished "+getCounts());
        }
        catch (Exception e)
        {
            successReadingRemoteConfig = false;
            log.error("Config read failed. "+e.getMessage(), e);
        }

        // Switch schedule sequence if we were on the normal schedule and we now have problems or if
        // we are on the initial/error schedule and there were no errors.
        if (normalCronSchedule && !successReadingRemoteConfig ||
            !normalCronSchedule && successReadingRemoteConfig)
        {
            normalCronSchedule = !normalCronSchedule;
            if (scheduler != null)
            {
                try
                {
                    CronExpression cronExpression = normalCronSchedule ? this.cronExpression : initialAndOnErrorCronExpression;
                    scheduler.clear();
                    scheduler = null;
                    schedule();
                }
                catch (SchedulerException e)
                {
                    getLog().error("Problem stopping scheduler for transformer configuration "+e.getMessage());
                }
            }
            else
            {
                System.out.println("Switch schedule "+normalCronSchedule+" WITHOUT new schedule");
            }
        }
    }

    protected abstract Data readConfig() throws IOException;

    private synchronized void setData(Data data)
    {
        this.data = data;
    }

    protected synchronized Data getData()
    {
        if (data == null)
        {
            data = createData();
        }
        return data;
    }

    protected Data createData()
    {
        return new Data();
    }

    protected void setSuccessReadingRemoteConfig(Data data, boolean successReadingRemoteConfig)
    {
        data.successReadingRemoteConfig = successReadingRemoteConfig;
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

    public void register(Data data, Reader reader, String readFrom) throws IOException
    {
        List<Transformer> transformers = jsonObjectMapper.readValue(reader, new TypeReference<List<Transformer>>(){});
        transformers.forEach(t -> register(data, t, null, readFrom));
    }

    protected void register(Data data, Transformer transformer, String baseUrl, String readFrom)
    {
        data.transformerCount++;
        transformer.getSupportedSourceAndTargetList().forEach(
            e -> data.transformers.computeIfAbsent(e.getSourceMediaType(),
                k -> new ConcurrentHashMap<>()).computeIfAbsent(e.getTargetMediaType(),
                k -> new ArrayList<>()).add(
                    new SupportedTransform(data, transformer.getTransformerName(),
                            transformer.getTransformOptions(), e.getMaxSourceSizeBytes(), e.getPriority())));
    }

    protected String getCounts()
    {
        return "("+getData().transformerCount+":"+getData().transformCount+")";
    }

    @Override
    public boolean isSupported(String sourceMimetype, long sourceSizeInBytes, String targetMimetype,
                               Map<String, String> actualOptions, String renditionName)
    {
        long maxSize = getMaxSize(sourceMimetype, targetMimetype, actualOptions, renditionName);
        return maxSize != 0 && (maxSize == -1L || maxSize >= sourceSizeInBytes);
    }

    /**
     * Works out the name of the transformer (might not map to an actual transformer) that will be used to transform
     * content of a given source mimetype and size into a target mimetype given a list of actual transform option names
     * and values (Strings) plus the data contained in the {@Transform} objects registered with this class.
     * @param sourceMimetype the mimetype of the source content
     * @param sourceSizeInBytes the size in bytes of the source content. Ignored if negative.
     * @param targetMimetype the mimetype of the target
     * @param actualOptions the actual name value pairs available that could be passed to the Transform Service.
     * @param renditionName (optional) name for the set of options and target mimetype. If supplied is used to cache
     *                      results to avoid having to work out if a given transformation is supported a second time.
     *                      The sourceMimetype and sourceSizeInBytes may still change. In the case of ACS this is the
     *                      rendition name.
     */
    protected String getTransformerName(String sourceMimetype, long sourceSizeInBytes, String targetMimetype, Map<String, String> actualOptions, String renditionName)
    {
        List<SupportedTransform> supportedTransforms = getTransformListBySize(sourceMimetype, targetMimetype, actualOptions, renditionName);
        for (SupportedTransform supportedTransform : supportedTransforms)
        {
            if (supportedTransform.maxSourceSizeBytes == -1 || supportedTransform.maxSourceSizeBytes >= sourceSizeInBytes)
            {
                return supportedTransform.name;
            }
        }

        return null;
    }

    @Override
    public long getMaxSize(String sourceMimetype, String targetMimetype,
                           Map<String, String> actualOptions, String renditionName)
    {
        List<SupportedTransform> supportedTransforms = getTransformListBySize(sourceMimetype, targetMimetype, actualOptions, renditionName);
        return supportedTransforms.isEmpty() ? 0 : supportedTransforms.get(supportedTransforms.size()-1).maxSourceSizeBytes;
    }

    // Returns transformers in increasing supported size order, where lower priority transformers for the same size have
    // been discarded.
    private List<SupportedTransform> getTransformListBySize(String sourceMimetype, String targetMimetype,
                                                            Map<String, String> actualOptions, String renditionName)
    {
        if (actualOptions == null)
        {
            actualOptions = Collections.EMPTY_MAP;
        }
        if (renditionName != null && renditionName.trim().isEmpty())
        {
            renditionName = null;
        }


        Data data = getData();
        List<SupportedTransform> transformListBySize = renditionName == null ? null
                : data.cachedSupportedTransformList.computeIfAbsent(renditionName, k -> new ConcurrentHashMap<>()).get(sourceMimetype);
        if (transformListBySize != null)
        {
            return transformListBySize;
        }

        // Remove the "timeout" property from the actualOptions as it is not used to select a transformer.
        if (actualOptions.containsKey(TIMEOUT))
        {
            actualOptions = new HashMap(actualOptions);
            actualOptions.remove(TIMEOUT);
        }

        transformListBySize = new ArrayList<>();
        ConcurrentMap<String, List<SupportedTransform>> targetMap = data.transformers.get(sourceMimetype);
        if (targetMap !=  null)
        {
            List<SupportedTransform> supportedTransformList = targetMap.get(targetMimetype);
            if (supportedTransformList != null)
            {
                for (SupportedTransform supportedTransform : supportedTransformList)
                {
                    TransformOptionGroup transformOptions = supportedTransform.transformOptions;
                    Map<String, Boolean> possibleTransformOptions = new HashMap<>();
                    addToPossibleTransformOptions(possibleTransformOptions, transformOptions, true, actualOptions);
                    if (isSupported(possibleTransformOptions, actualOptions))
                    {
                        addToSupportedTransformList(transformListBySize, supportedTransform);
                    }
                }
            }
        }

        if (renditionName != null)
        {
            data.cachedSupportedTransformList.get(renditionName).put(sourceMimetype, transformListBySize);
        }

        return transformListBySize;
    }

    // Add newTransform to the transformListBySize in increasing size order and discards lower priority (numerically
    // higher) transforms with a smaller or equal size.
    private void addToSupportedTransformList(List<SupportedTransform> transformListBySize, SupportedTransform newTransform)
    {
        for (int i=0; i < transformListBySize.size(); i++)
        {
            SupportedTransform existingTransform = transformListBySize.get(i);
            int added = -1;
            int compare = compare(newTransform.maxSourceSizeBytes, existingTransform.maxSourceSizeBytes);
            if (compare < 0)
            {
                transformListBySize.add(i, newTransform);
                added = i;
            }
            else if (compare == 0)
            {
                if (newTransform.priority < existingTransform.priority)
                {
                    transformListBySize.set(i, newTransform);
                    added = i;
                }
            }
            if (added == i)
            {
                for (i--; i >= 0; i--)
                {
                    existingTransform = transformListBySize.get(i);
                    if (newTransform.priority <= existingTransform.priority)
                    {
                        transformListBySize.remove(i);
                    }
                }
                return;
            }
        }
        transformListBySize.add(newTransform);
    }

    // compare where -1 is unlimited.
    private int compare(long a, long b)
    {
        return a == -1
                ? b == -1 ? 0 : 1
                : b == -1 ? -1
                : a == b ? 0
                : a > b ? 1 : -1;
    }

    /**
     * Flatten out the transform options by adding them to the supplied possibleTransformOptions.</p>
     *
     * If possible discards options in the supplied transformOptionGroup if the group is optional and the actualOptions
     * don't provide any of the options in the group. Or to put it another way:<p/>
     *
     * It adds individual transform options from the transformOptionGroup to possibleTransformOptions if the group is
     * required or if the actualOptions include individual options from the group. As a result it is possible that none
     * of the group are added if it is optional. It is also possible to add individual transform options that are
     * themselves required but not in the actualOptions. In this the isSupported method will return false.
     * @return true if any options were added. Used by nested call parents to determine if an option was added from a
     * nested sub group.
     */
    boolean addToPossibleTransformOptions(Map<String, Boolean> possibleTransformOptions,
                                          TransformOptionGroup transformOptionGroup,
                                          Boolean parentGroupRequired, Map<String, String> actualOptions)
    {
        boolean added = false;
        boolean required = false;

        List<TransformOption> optionList = transformOptionGroup.getTransformOptions();
        if (optionList != null && !optionList.isEmpty())
        {
            // We need to avoid adding options from a group that is required but its parents are not.
            boolean transformOptionGroupRequired = transformOptionGroup.isRequired() && parentGroupRequired;

            // Check if the group contains options in actualOptions. This will add any options from sub groups.
            for (TransformOption transformOption : optionList)
            {
                if (transformOption instanceof TransformOptionGroup)
                {
                    added = addToPossibleTransformOptions(possibleTransformOptions, (TransformOptionGroup) transformOption,
                            transformOptionGroupRequired, actualOptions);
                    required |= added;
                }
                else
                {
                    String name = ((TransformOptionValue) transformOption).getName();
                    if (actualOptions.containsKey(name))
                    {
                        required = true;
                    }
                }
            }

            if (required || transformOptionGroupRequired)
            {
                for (TransformOption transformOption : optionList)
                {
                    if (transformOption instanceof TransformOptionValue)
                    {
                        added = true;
                        TransformOptionValue transformOptionValue = (TransformOptionValue) transformOption;
                        String name = transformOptionValue.getName();
                        boolean optionValueRequired = transformOptionValue.isRequired();
                        possibleTransformOptions.put(name, optionValueRequired);
                    }
                }
            }
        }

        return added;
    }

    boolean isSupported(Map<String, Boolean> transformOptions, Map<String, String> actualOptions)
    {
        boolean supported = true;

        // Check all required transformOptions are supplied
        for (Map.Entry<String, Boolean> transformOption : transformOptions.entrySet())
        {
            Boolean required = transformOption.getValue();
            if (required)
            {
                String name = transformOption.getKey();
                if (!actualOptions.containsKey(name))
                {
                    supported = false;
                    break;
                }
            }
        }

        if (supported)
        {
            // Check there are no extra unused actualOptions
            for (String actualOption : actualOptions.keySet())
            {
                if (!transformOptions.containsKey(actualOption))
                {
                    supported = false;
                    break;
                }
            }
        }
        return supported;
    }
}
