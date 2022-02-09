/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2019 - 2022 Alfresco Software Limited
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

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.transform.client.model.config.CoreFunction;
import org.alfresco.transform.client.model.config.TransformOptionGroup;
import org.alfresco.transform.client.registry.CombinedConfig;
import org.alfresco.transform.client.model.config.TransformOption;
import org.alfresco.transform.client.registry.TransformServiceRegistryImpl;
import org.alfresco.transform.client.model.config.TransformStep;
import org.alfresco.transform.client.model.config.Transformer;
import org.alfresco.transform.client.registry.TransformServiceRegistry;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import static java.util.Collections.emptySet;

/**
 * Implements {@link TransformServiceRegistry} providing a mechanism of validating if a local transformation
 * (based on {@link LocalTransform} request is supported.
 *
 * @author adavis
 */
public class LocalTransformServiceRegistry extends TransformServiceRegistryImpl implements InitializingBean
{
    private static final Log log = LogFactory.getLog(LocalTransformServiceRegistry.class);

    public static final String LOCAL_TRANSFORMER = "localTransform.";
    public static final String URL = ".url";
    static final String STRICT_MIMETYPE_CHECK_WHITELIST_MIMETYPES = "transformer.strict.mimetype.check.whitelist.mimetypes";

    public class LocalData extends TransformServiceRegistryImpl.Data
    {
        private Map<String, LocalTransform> localTransforms = new HashMap<>();
    }

    private String pipelineConfigDir;
    private Properties properties;
    private MimetypeService mimetypeService;
    private TransformerDebug transformerDebug;
    private boolean strictMimeTypeCheck;
    private Map<String, Set<String>> strictMimetypeExceptions;
    private boolean retryTransformOnDifferentMimeType;

    public void setPipelineConfigDir(String pipelineConfigDir)
    {
        this.pipelineConfigDir = pipelineConfigDir;
    }

    public String getPipelineConfigDir()
    {
        return pipelineConfigDir;
    }

    /**
     * The Alfresco global properties.
     */
    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }

    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    public MimetypeService getMimetypeService()
    {
        return mimetypeService;
    }

    public void setTransformerDebug(TransformerDebug transformerDebug)
    {
        this.transformerDebug = transformerDebug;
    }

    public void setStrictMimeTypeCheck(boolean strictMimeTypeCheck)
    {
        this.strictMimeTypeCheck = strictMimeTypeCheck;
    }

    public void setRetryTransformOnDifferentMimeType(boolean retryTransformOnDifferentMimeType)
    {
        this.retryTransformOnDifferentMimeType = retryTransformOnDifferentMimeType;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "mimetypeService", mimetypeService);
        PropertyCheck.mandatory(this, "properties", properties);
        PropertyCheck.mandatory(this, "transformerDebug", transformerDebug);
        strictMimetypeExceptions = getStrictMimetypeExceptions();
        super.afterPropertiesSet();
    }

    @Override
    public boolean readConfig() throws IOException
    {
        CombinedConfig combinedConfig = new CombinedConfig(getLog(), this);
        List<String> urls = getTEngineUrlsSortedByName();
        boolean successReadingConfig = combinedConfig.addRemoteConfig(urls, "T-Engine");
        successReadingConfig &= combinedConfig.addLocalConfig("alfresco/transforms");
        if (pipelineConfigDir != null && !pipelineConfigDir.isBlank())
        {
            successReadingConfig &= combinedConfig.addLocalConfig(pipelineConfigDir);
        }
        combinedConfig.addPassThroughTransformer(mimetypeService, this);
        combinedConfig.register(this);
        return successReadingConfig;
    }

    @Override
    public LocalData getData()
    {
        return (LocalData)super.getData();
    }

    @Override
    public Data createData()
    {
        return new LocalData();
    }

    @Override
    protected void register(Transformer transformer, Map<String, Set<TransformOption>> transformOptions,
                            String baseUrl, String readFrom)
    {
        try
        {
            String name = transformer.getTransformerName();
            LocalData data = getData();
            Map<String, LocalTransform> localTransforms = data.localTransforms;

            Set<TransformOption> transformsTransformOptions = lookupTransformOptions(
                    transformer.getTransformOptions(), transformOptions, readFrom, this::logError);

            LocalTransform localTransform;
            List<TransformStep> pipeline = transformer.getTransformerPipeline();
            List<String> failover = transformer.getTransformerFailover();
            boolean isPipeline = pipeline != null && !pipeline.isEmpty();
            boolean isFailover = failover != null && !failover.isEmpty();
            if (name.equals(LocalPassThroughTransform.NAME))
            {
                localTransform = new LocalPassThroughTransform(name, transformerDebug, mimetypeService,
                        strictMimeTypeCheck, strictMimetypeExceptions, retryTransformOnDifferentMimeType,
                        transformsTransformOptions, this);
            }
            else if (!isPipeline && !isFailover)
            {
                int startupRetryPeriodSeconds = getStartupRetryPeriodSeconds(name);
                localTransform = new LocalTransformImpl(name, transformerDebug, mimetypeService,
                         strictMimeTypeCheck, strictMimetypeExceptions, retryTransformOnDifferentMimeType,
                        transformsTransformOptions, this, baseUrl, startupRetryPeriodSeconds);
            }
            else if (isPipeline)
            {
                int transformerCount = pipeline.size();
                if (transformerCount <= 1)
                {
                    throw new IllegalArgumentException("Local pipeline transformer " + name +
                            " must have more than one intermediate transformer defined."+
                            " Read from "+readFrom);
                }

                localTransform = new LocalPipelineTransform(name, transformerDebug, mimetypeService,
                        strictMimeTypeCheck, strictMimetypeExceptions, retryTransformOnDifferentMimeType,
                        transformsTransformOptions, this);
                for (int i=0; i < transformerCount; i++)
                {
                    TransformStep intermediateTransformerStep = pipeline.get(i);
                    String intermediateTransformerName = intermediateTransformerStep.getTransformerName();
                    LocalTransform intermediateTransformer = localTransforms.get(intermediateTransformerName);
                    if (intermediateTransformer == null)
                    {
                        throw new IllegalArgumentException("Local pipeline transformer " + name +
                                " specified an intermediate transformer " +
                                intermediateTransformerName + " that has not been defined."+
                                " Read from "+readFrom);
                    }

                    String targetMimetype = intermediateTransformerStep.getTargetMediaType();
                    if (i == transformerCount-1)
                    {
                        if (targetMimetype != null)
                        {
                            throw new IllegalArgumentException("Local pipeline transformer " + name +
                                    " must not specify targetMimetype for the final intermediate transformer, " +
                                    "as this is defined via the supportedSourceAndTargetList."+
                                    " Read from "+readFrom);
                        }
                    }
                    else
                    {
                        if (targetMimetype == null)
                        {
                            throw new IllegalArgumentException("Local pipeline transformer " + name + " must specify " +
                                    "targetMimetype for all intermediate transformers except for the final one."+
                                    " Read from "+readFrom);
                        }
                    }
                    ((LocalPipelineTransform) localTransform).addIntermediateTransformer(intermediateTransformer, targetMimetype);
                }
            }
            else // if (isFailover)
            {
                int transformerCount = failover.size();
                if (transformerCount <= 1)
                {
                    throw new IllegalArgumentException("Local failover transformer " + name +
                            " must have more than one transformer defined."+
                            " Read from "+readFrom);
                }

                localTransform = new LocalFailoverTransform(name, transformerDebug, mimetypeService,
                        strictMimeTypeCheck, strictMimetypeExceptions, retryTransformOnDifferentMimeType,
                        transformsTransformOptions, this);

                for (String transformerStepName : failover)
                {
                    LocalTransform stepTransformer = localTransforms.get(transformerStepName);
                    if (stepTransformer == null)
                    {
                        throw new IllegalArgumentException("Local failover transformer " + name +
                                " specified an intermediate transformer " +
                                transformerStepName + " that has not been defined."+
                                " Read from "+readFrom);
                    }

                    ((LocalFailoverTransform) localTransform).addStepTransformer(stepTransformer);
                }
            }
            localTransforms.put(name, localTransform);
            super.register(transformer, transformOptions, baseUrl, readFrom);
        }
        catch (IllegalArgumentException e)
        {
            String msg = e.getMessage();
            getLog().error(msg);
        }
    }

    /**
     * Returns the set of TransformOptions for this transform. In the JSON structure, each transform lists the names
     * of each set of transform options it uses. In the case of pipelines and failovers transforms, there is typically
     * more than one set. Typically there is one for each child transform.
     * @param transformOptionNames the names of the transform options used by this transform.
     * @param transformOptions a map keyed on transform option name of all the TransformOptions
     * @param readFrom used in debug messages to indicate where the transformer config was read from.
     * @param logError used to log an error message if a transformOptionName is invalid.
     */
    private static Set<TransformOption> lookupTransformOptions(final Set<String> transformOptionNames,
                                                       final Map<String, Set<TransformOption>> transformOptions,
                                                       final String readFrom,
                                                       final Consumer<String> logError)
    {
        if (transformOptionNames == null)
        {
            return emptySet();
        }

        final Set<TransformOption> options = new HashSet<>();
        for (String name : transformOptionNames)
        {
            final Set<TransformOption> oneSetOfTransformOptions = transformOptions.get(name);
            if (oneSetOfTransformOptions == null)
            {
                logError.accept("transformOptions in " + readFrom + " with the name " + name +
                        " does not exist. Ignored");
                continue;
            }
            options.add(new TransformOptionGroup(false, oneSetOfTransformOptions));
        }

        // If there is only one transform name, the set from the holding TransformOptionGroup can be returned,
        // rather than having a nested structure.
        return options.size() == 1 ?
                ((TransformOptionGroup) options.iterator().next()).getTransformOptions() :
                options;
    }

    @Override
    protected Log getLog()
    {
        return log;
    }

    /**
     * @return urls from System or Alfresco global properties that match of localTransform.<name>.url=<url> sorted
     *         in <name> order.
     */
    List<String> getTEngineUrlsSortedByName()
    {
        // T-Engines are sorted by name so they are in the same order as in the all-in-one transformer and the
        // T-Router. See AIOCustomConfig#getTEnginesSortedByName and TransformersConfigRegistry#retrieveRemoteConfig.
        return getKeySet().stream()
                .filter(key -> key instanceof String)
                .filter(key -> key.startsWith(LOCAL_TRANSFORMER) && key.endsWith(URL))
                .sorted()
                .map(key -> getProperty(key, null))
                .filter(url -> url instanceof String)
                .map(url -> url.trim())
                .filter(url -> !url.isEmpty())
                .collect(Collectors.toList());
    }

    private int getStartupRetryPeriodSeconds(String name)
    {
        String startupRetryPeriodSecondsName = LOCAL_TRANSFORMER + name + ".startupRetryPeriodSeconds";
        String property = getProperty(startupRetryPeriodSecondsName, "60");
        int startupRetryPeriodSeconds;
        try
        {
            startupRetryPeriodSeconds = Integer.parseInt(property);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Local transformer property " + startupRetryPeriodSecondsName +
                    " should be an integer");
        }
        return startupRetryPeriodSeconds;
    }

    private Map<String, Set<String>> getStrictMimetypeExceptions()
    {
        Map<String, Set<String>> strictMimetypeExceptions = new HashMap<>();

        String whitelist = getProperty(STRICT_MIMETYPE_CHECK_WHITELIST_MIMETYPES, "").trim();
        if (!whitelist.isEmpty())
        {
            String[] mimetypes = whitelist.split(";");

            if (mimetypes.length % 2 != 0)
            {
                getLog().error(STRICT_MIMETYPE_CHECK_WHITELIST_MIMETYPES+" should have an even number of mimetypes as a ; separated list.");
            }
            else
            {
                Set<String> detectedMimetypes = null;
                for (String mimetype: mimetypes)
                {
                    mimetype = mimetype.trim();
                    if (mimetype.isEmpty())
                    {
                        getLog().error(STRICT_MIMETYPE_CHECK_WHITELIST_MIMETYPES+" contains a blank mimetype.");
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
     * @return the set of property keys and System keys.
     */
    private Set<String> getKeySet()
    {
        Set<Object> systemKeys = System.getProperties().keySet();
        Set<Object> alfrescoGlobalKeys = this.properties.keySet();
        Set<String> keys = new HashSet<>(systemKeys.size()+alfrescoGlobalKeys.size());
        addStrings(keys, systemKeys);
        addStrings(keys, alfrescoGlobalKeys);
        return keys;
    }

    private void addStrings(Set<String> setOfStrings, Set<Object> objects)
    {
        objects.forEach(object->{
            if (object instanceof String)
            {
                setOfStrings.add((String)object);
            }
        });
    }

    /**
     * Gets a property from an alfresco global property but falls back to a System property with the same name to
     * allow dynamic creation of transformers without having to have an AMP to add the alfresco global property.
     */
    protected String getProperty(String name, String defaultValue)
    {
        String value = properties.getProperty(name);
        if (value == null || value.isEmpty())
        {
            value = System.getProperty(name);
            if (value != null && value.isEmpty())
            {
                value = null;
            }
        }
        return value == null ? defaultValue : value;
    }

    @Override
    public long findMaxSize(String sourceMimetype, String targetMimetype, Map<String, String> options, String renditionName)
    {
        // This message is not logged if placed in afterPropertiesSet
        if (getFirstTime())
        {
            setFirstTime(false);
            transformerDebug.debug("Local transforms "+getData()+" are " + (enabled ? "enabled" : "disabled"));
        }

        return super.findMaxSize(sourceMimetype, targetMimetype, options, renditionName);
    }

    public LocalTransform getLocalTransform(String sourceMimetype, long sourceSizeInBytes, String targetMimetype, Map<String, String> actualOptions, String renditionName)
    {
        if (!enabled)
        {
            return null;
        }
        LocalTransform localTransform = null;
        String name = findTransformerName(sourceMimetype, sourceSizeInBytes, targetMimetype, actualOptions, renditionName);
        if (name != null)
        {
            LocalData data = getData();
            Map<String, LocalTransform> localTransforms = data.localTransforms;
            localTransform = localTransforms.get(name);
        }
        return localTransform;
    }

    /**
     * Returns {@code true} if the {@code function} is supported by the transform. Not all transforms are
     * able to support all functionality, as newer features may have been introduced into the core t-engine code since
     * it was released.
     * @param function to be checked.
     * @param transform the local transform.
     * @return {@code true} is supported, {@code false} otherwise.
     */
    boolean isSupported(CoreFunction function, LocalTransform transform)
    {
        return isSupported(function, transform.getName());
    }
}
