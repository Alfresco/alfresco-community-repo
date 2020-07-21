/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2019 Alfresco Software Limited
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.transform.LocalPassThroughTransform;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.transform.client.model.config.SupportedSourceAndTarget;
import org.alfresco.transform.client.model.config.TransformConfig;
import org.alfresco.transform.client.model.config.TransformOption;
import org.alfresco.transform.client.model.config.TransformStep;
import org.alfresco.transform.client.model.config.Transformer;
import org.alfresco.util.ConfigFileFinder;
import org.apache.commons.logging.Log;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class reads multiple T-Engine config and local files and registers them all with a registry as if they were all
 * in one file. Transform options are shared between all sources.<p>
 *
 * The caller should make calls to {@link #addRemoteConfig(List, String)} and {@link #addLocalConfig(String)} followed
 * by a call to {@link #register(TransformServiceRegistryImpl)}.
 *
 * @author adavis
 */
public class CombinedConfig
{
    private final Log log;

    static class TransformAndItsOrigin
    {
        final Transformer transformer;
        final String baseUrl;
        final String readFrom;

        TransformAndItsOrigin(Transformer transformer, String baseUrl, String readFrom)
        {
            this.transformer = transformer;
            this.baseUrl = baseUrl;
            this.readFrom = readFrom;
        }

        public Transformer getTransformer()
        {
            return transformer;
        }
    }

    Map<String, Set<TransformOption>> combinedTransformOptions = new HashMap<>();
    List<TransformAndItsOrigin> combinedTransformers = new ArrayList<>();

    private ObjectMapper jsonObjectMapper = new ObjectMapper();
    private ConfigFileFinder configFileFinder;
    private int tEngineCount;

    public CombinedConfig(Log log)
    {
        this.log = log;

        configFileFinder = new ConfigFileFinder(jsonObjectMapper)
        {
            @Override
            protected void readJson(JsonNode jsonNode, String readFrom, String baseUrl)
            {
                TransformConfig transformConfig = jsonObjectMapper.convertValue(jsonNode, TransformConfig.class);
                transformConfig.getTransformOptions().forEach((key, map) -> combinedTransformOptions.put(key, map));
                transformConfig.getTransformers().forEach(transformer -> combinedTransformers.add(
                        new TransformAndItsOrigin(transformer, baseUrl, readFrom)));
            }
        };
    }

    public boolean addLocalConfig(String path)
    {
        return configFileFinder.readFiles(path, log);
    }

    public boolean addRemoteConfig(List<String> urls, String remoteType)
    {
        boolean successReadingConfig = true;
        for (String url : urls)
        {
            if (addRemoteConfig(url, remoteType))
            {
                tEngineCount++ ;
            }
            else
            {
                successReadingConfig = false;
            }
        }
        return successReadingConfig;
    }

    private boolean addRemoteConfig(String baseUrl, String remoteType)
    {
        String url = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "transform/config";
        HttpGet httpGet = new HttpGet(url);
        boolean successReadingConfig = true;
        boolean logAsDebug = false;
        try
        {
            try (CloseableHttpClient httpclient = HttpClients.createDefault())
            {
                try (CloseableHttpResponse response = execute(httpclient, httpGet))
                {
                    StatusLine statusLine = response.getStatusLine();
                    if (statusLine == null)
                    {
                        throw new AlfrescoRuntimeException(remoteType+" on " + url+" returned no status ");
                    }
                    HttpEntity resEntity = response.getEntity();
                    if (resEntity != null)
                    {
                        int statusCode = statusLine.getStatusCode();
                        if (statusCode == 200)
                        {
                            try
                            {
                                String content = getContent(resEntity);
                                try (StringReader reader = new StringReader(content))
                                {
                                    int transformCount = combinedTransformers.size();
                                    configFileFinder.readFile(reader, remoteType+" on "+baseUrl, "json", baseUrl, log);
                                    if (transformCount == combinedTransformers.size())
                                    {
                                        successReadingConfig = false;
                                    }
                                }

                                EntityUtils.consume(resEntity);
                            }
                            catch (IOException e)
                            {
                                throw new AlfrescoRuntimeException("Failed to read the returned content from "+
                                        remoteType+" on " + url, e);
                            }
                        }
                        else
                        {
                            String message = getErrorMessage(resEntity);
                            throw new AlfrescoRuntimeException(remoteType+" on " + url+" returned a " + statusCode +
                                    " status " + message);
                        }
                    }
                    else
                    {
                        throw new AlfrescoRuntimeException(remoteType+" on " + url+" did not return an entity " + url);
                    }
                }
                catch (IOException e)
                {
                    logAsDebug = true;
                    throw new AlfrescoRuntimeException("Failed to connect or to read the response from "+remoteType+
                            " on " + url, e);
                }
            }
            catch (IOException e)
            {
                throw new AlfrescoRuntimeException(remoteType+" on " + url+" failed to create an HttpClient", e);
            }
        }
        catch (AlfrescoRuntimeException e)
        {
            String message = e.getMessage();
            if (logAsDebug)
            {
                log.debug(message);
            }
            else
            {
                log.error(message);
            }
            successReadingConfig = false;
        }
        return successReadingConfig;
    }

    // Tests mock the return values
    CloseableHttpResponse execute(CloseableHttpClient httpclient, HttpGet httpGet) throws IOException
    {
        return httpclient.execute(httpGet);
    }

    // Tests mock the return values
    String getContent(HttpEntity resEntity) throws IOException
    {
        return EntityUtils.toString(resEntity);
    }

    // Strip out just the error message in the response
    private String getErrorMessage(HttpEntity resEntity) throws IOException
    {
        String message = "";
        String content = getContent(resEntity);
        int i = content.indexOf("\"message\":\"");
        if (i != -1)
        {
            int j = content.indexOf("\",\"path\":", i);
            if (j != -1)
            {
                message = content.substring(i+11, j);
            }
        }
        return message;
    }

    /**
     * Adds a PassThrough transform where the source and target mimetypes are identical, or transforms to "text/plain"
     * from selected text based types.
     * @param mimetypeService to find all the mimetypes
     */
    public void addPassThroughTransformer(MimetypeService mimetypeService)
    {
        List<String> mimetypes = mimetypeService.getMimetypes();
        Transformer transformer = LocalPassThroughTransform.getConfig(mimetypes);
        combinedTransformers.add(new TransformAndItsOrigin(transformer, null, "based on mimetype list"));
    }

    public void register(TransformServiceRegistryImpl registry)
    {
        TransformServiceRegistryImpl.Data data = registry.getData();
        data.setTEngineCount(tEngineCount);
        data.setFileCount(configFileFinder.getFileCount());

        combinedTransformers = sortTransformers(combinedTransformers);

        addWildcardSupportedSourceAndTarget(combinedTransformers);

        combinedTransformers.forEach(transformer ->
            registry.register(transformer.transformer, combinedTransformOptions,
                    transformer.baseUrl, transformer.readFrom));
    }

    // Sort transformers so there are no forward references, if that is possible.
    private static List<TransformAndItsOrigin> sortTransformers(List<TransformAndItsOrigin> original)
    {
        List<TransformAndItsOrigin> transformers = new ArrayList<>(original.size());
        List<TransformAndItsOrigin> todo = new ArrayList<>(original.size());
        Set<String> transformerNames = new HashSet<>();
        boolean added;
        do
        {
            added = false;
            for (TransformAndItsOrigin entry : original)
            {
                String name = entry.transformer.getTransformerName();
                List<TransformStep> pipeline = entry.transformer.getTransformerPipeline();
                Set<String> referencedTransformerNames = new HashSet<>();
                boolean addEntry = true;
                if (pipeline != null)
                {
                    for (TransformStep step : pipeline)
                    {
                        String stepName = step.getTransformerName();
                        referencedTransformerNames.add(stepName);
                    }
                }
                List<String> failover = entry.transformer.getTransformerFailover();
                if (failover != null)
                {
                    referencedTransformerNames.addAll(failover);
                }

                for (String referencedTransformerName : referencedTransformerNames)
                {
                    if (!transformerNames.contains(referencedTransformerName))
                    {
                        todo.add(entry);
                        addEntry = false;
                        break;
                    }
                }

                if (addEntry)
                {
                    transformers.add(entry);
                    added = true;
                    if (name != null)
                    {
                        transformerNames.add(name);
                    }
                }
            }
            original.clear();
            original.addAll(todo);
            todo.clear();
        }
        while (added && !original.isEmpty());

        transformers.addAll(todo);

        return transformers;
    }

    private void addWildcardSupportedSourceAndTarget(List<TransformAndItsOrigin> combinedTransformers)
    {
        Map<String, Transformer> transformers = new HashMap<>();
        combinedTransformers.forEach(ct -> transformers.put(ct.transformer.getTransformerName(), ct.transformer));

        combinedTransformers.forEach(transformAndItsOrigin ->
        {
            Transformer transformer = transformAndItsOrigin.transformer;

            // If there are no SupportedSourceAndTarget, then work out all the wildcard combinations.
            if (transformer.getSupportedSourceAndTargetList().isEmpty())
            {
                List<TransformStep> pipeline = transformer.getTransformerPipeline();
                List<String> failover = transformer.getTransformerFailover();
                boolean isPipeline = pipeline != null && !pipeline.isEmpty();
                boolean isFailover = failover != null && !failover.isEmpty();
                if (isFailover)
                {
                    // Copy all SupportedSourceAndTarget values from each step transformer
                    Set<SupportedSourceAndTarget> supportedSourceAndTargets = failover.stream().flatMap(
                            name -> transformers.get(name).getSupportedSourceAndTargetList().stream()).
                            collect(Collectors.toSet());
                    transformer.setSupportedSourceAndTargetList(supportedSourceAndTargets);
                }
                else if (isPipeline)
                {
                    // Build up SupportedSourceAndTarget values. The list of source types and max sizes will come from the
                    // initial step transformer that have a target mimetype that matches the first intermediate mimetype.
                    // We then step through all intermediate transformers checking the next intermediate type is supported.
                    // When we get to the last step transformer, it provides all the target mimetypes based on the previous
                    // intermediate mimeype. Any combinations supported by the first transformer are excluded.
                    boolean first = true;
                    String sourceMediaType = null;
                    Set<SupportedSourceAndTarget> sourceMediaTypesAndMaxSizes = null;
                    Set<String> firstTransformOptions = null;
                    for (TransformStep step : pipeline)
                    {
                        String name = step.getTransformerName();
                        Transformer stepTransformer = transformers.get(name);
                        if (stepTransformer == null)
                        {
                            break;
                        }

                        String stepTrg = step.getTargetMediaType();
                        if (first)
                        {
                            first = false;
                            sourceMediaTypesAndMaxSizes = stepTransformer.getSupportedSourceAndTargetList().stream().
                                    filter(s -> stepTrg.equals(s.getTargetMediaType())).
                                    collect(Collectors.toSet());
                            sourceMediaType = stepTrg;
                            firstTransformOptions = stepTransformer.getTransformOptions();
                        }
                        else
                        {
                            final String src = sourceMediaType;
                            if (stepTrg == null) // if final step
                            {
                                // Create a cartesian product of sourceMediaType,MaxSourceSize and TargetMediaType where
                                // the source matches the last intermediate.
                                Set<SupportedSourceAndTarget>  supportedSourceAndTargets = sourceMediaTypesAndMaxSizes.stream().
                                        flatMap(s -> stepTransformer.getSupportedSourceAndTargetList().stream().
                                                filter(st -> st.getSourceMediaType().equals(src)).
                                                map(t -> t.getTargetMediaType()).
                                                map(trg -> SupportedSourceAndTarget.builder().
                                                        withSourceMediaType(s.getSourceMediaType()).
                                                        withMaxSourceSizeBytes(s.getMaxSourceSizeBytes()).
                                                        withPriority(s.getPriority()).
                                                        withTargetMediaType(trg).build())).
                                        collect(Collectors.toSet());

                                // Exclude duplicates with the first transformer, if it has the same options.
                                // There is no point doing more work.
                                Set<String> transformOptions = transformer.getTransformOptions();
                                if (sameOptions(transformOptions, firstTransformOptions))
                                {
                                    supportedSourceAndTargets.removeAll(sourceMediaTypesAndMaxSizes);
                                }

                                transformer.setSupportedSourceAndTargetList(supportedSourceAndTargets);
                            }
                            else // if intermediate step
                            {
                                // Check source to target is supported (it normally is)
                                if (!stepTransformer.getSupportedSourceAndTargetList().stream().
                                        anyMatch(st -> st.getSourceMediaType().equals(src) &&
                                                       st.getTargetMediaType().equals(stepTrg)))
                                {
                                    break;
                                }

                                sourceMediaType = stepTrg;
                            }
                        }
                    }
                }
            }
        });
    }

    private boolean sameOptions(Set<String> transformOptionNames1, Set<String> transformOptionNames2)
    {
        // They have the same names
        if (transformOptionNames1.equals(transformOptionNames2))
        {
            return true;
        }

        // Check the actual options.
        Set<TransformOption> transformOptions1 = getTransformOptions(transformOptionNames1);
        Set<TransformOption> transformOptions2 = getTransformOptions(transformOptionNames2);
        return transformOptions1.equals(transformOptions2);
    }

    private Set<TransformOption> getTransformOptions(Set<String> transformOptionNames)
    {
        Set<TransformOption> transformOptions = new HashSet<>();
        transformOptionNames.forEach(name->transformOptions.addAll(combinedTransformOptions.get(name)));
        return transformOptions;
    }
}
