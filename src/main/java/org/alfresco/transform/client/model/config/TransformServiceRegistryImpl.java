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

/**
 * Used by clients work out if a transformation is supported by the Transform Service.
 */
public class TransformServiceRegistryImpl implements TransformServiceRegistry, InitializingBean
{
    class SupportedTransform
    {
        private TransformOptionGroup transformOptions;
        private long maxSourceSizeBytes;

        public SupportedTransform(List<TransformOption> transformOptions, long maxSourceSizeBytes)
        {
            // Logically the top level TransformOptionGroup is required, so that child options are optional or required
            // based on their own setting.
            this.transformOptions = new TransformOptionGroup(true, transformOptions);
            this.maxSourceSizeBytes = maxSourceSizeBytes;
        }
    }

    private ObjectMapper jsonObjectMapper;
    private ExtensionMap extensionMap;

    ConcurrentMap<String, ConcurrentMap<String, List<SupportedTransform>>> transformers = new ConcurrentHashMap<>();
    ConcurrentMap<String, ConcurrentMap<String, Long>> cachedMaxSizes = new ConcurrentHashMap<>();

    public void setJsonObjectMapper(ObjectMapper jsonObjectMapper)
    {
        this.jsonObjectMapper = jsonObjectMapper;
    }

    public void setExtensionMap(ExtensionMap extensionMap)
    {
        this.extensionMap = extensionMap;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        if (jsonObjectMapper == null)
        {
            throw new IllegalStateException("jsonObjectMapper has not been set");
        }
        if (extensionMap == null)
        {
            throw new IllegalStateException("extensionMap has not been set");
        }
    }

    private String toMimetype(String ext)
    {
        String mimetype = extensionMap.toMimetype(ext);
        if (mimetype == null)
        {
            throw new IllegalArgumentException("The mimetype for the file extension "+ext+" cannot be looked up by: "+
                    extensionMap.getClass().getName());
        }
        return mimetype;
    }

    public void register(Transformer transformer)
    {
        transformer.getSupportedSourceAndTargetList().forEach(
            e -> transformers.computeIfAbsent(toMimetype(e.getSourceExt()),
                k -> new ConcurrentHashMap<>()).computeIfAbsent(toMimetype(e.getTargetExt()),
                k -> new ArrayList<>()).add(
                    new SupportedTransform(transformer.getTransformOptions(), e.getMaxSourceSizeBytes())));
    }

    public void register(Reader reader) throws IOException
    {
        List<Transformer> transformers = jsonObjectMapper.readValue(reader, new TypeReference<List<Transformer>>(){});
        transformers.forEach(t -> register(t));
    }

    @Override
    public boolean isSupported(String sourceMimetype, long sourceSizeInBytes, String targetMimetype,
                               Map<String, String> actualOptions, String transformName)
    {
        long maxSize = getMaxSize(sourceMimetype, targetMimetype, actualOptions, transformName);
        return maxSize != 0 && (maxSize == -1L || maxSize >= sourceSizeInBytes);
    }

    @Override
    public long getMaxSize(String sourceMimetype, String targetMimetype,
                           Map<String, String> actualOptions, String transformName)
    {
        if (actualOptions == null)
        {
            actualOptions = Collections.EMPTY_MAP;
        }
        if (transformName != null && transformName.trim().isEmpty())
        {
            transformName = null;
        }

        Long maxSize = transformName == null ? null : cachedMaxSizes.computeIfAbsent(transformName, k -> new ConcurrentHashMap<>()).get(sourceMimetype);
        if (maxSize != null)
        {
            return maxSize.longValue();
        }

        long calculatedMaxSize = 0;
        ConcurrentMap<String, List<SupportedTransform>> targetMap = transformers.get(sourceMimetype);
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
                        if (supportedTransform.maxSourceSizeBytes < 0)
                        {
                            calculatedMaxSize = -1;
                            break;
                        }

                        calculatedMaxSize = Math.max(calculatedMaxSize, supportedTransform.maxSourceSizeBytes);
                    }
                }
            }
        }

        if (transformName != null)
        {
            cachedMaxSizes.get(transformName).put(sourceMimetype, calculatedMaxSize);
        }

        return calculatedMaxSize;
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
