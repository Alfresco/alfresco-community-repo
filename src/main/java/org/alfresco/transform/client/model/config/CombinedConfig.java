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
package org.alfresco.transform.client.model.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This class recreates the json format used in ACS 6.1 where we just had an array of transformers and each
 * transformer has a list of node options. The idea of this code is that it replaces the references with the
 * actual node options that have been separated out into their own section.<p>
 *
 * The T-Router and T-Engines return the format with the node option separated into their own section. Pipeline
 * definitions used by the LocalTransformServiceRegistry may use node reference options defined in the json
 * returned by T-Engines.  with the actual definitions from the node options
 * reference section. It also combines multiple json sources into a single jsonNode structure that can be parsed as
 * before.
 */
public class CombinedConfig
{
    private static final String TRANSFORMER_NAME = "transformerName";
    private static final String TRANSFORM_CONFIG = "/transform/config";
    private static final String TRANSFORM_OPTIONS = "transformOptions";
    private static final String GROUP = "group";
    private static final String TRANSFORMERS = "transformers";

    private final Log log;
    private Map<String, ArrayNode> allTransformOptions = new HashMap<>();
    private List<TransformNodeAndItsOrigin> allTransforms = new ArrayList<>();
    private ObjectMapper jsonObjectMapper = new ObjectMapper();

    static class TransformNodeAndItsOrigin
    {
        final ObjectNode node;
        final String baseUrl;
        final String readFrom;

        TransformNodeAndItsOrigin(ObjectNode node, String baseUrl, String readFrom)
        {
            this.node = node;
            this.baseUrl = baseUrl;
            this.readFrom = readFrom;
        }
    }

    static class TransformAndItsOrigin
    {
        final Transformer transform;
        final String baseUrl;
        final String readFrom;

        TransformAndItsOrigin(Transformer transform, String baseUrl, String readFrom)
        {
            this.transform = transform;
            this.baseUrl = baseUrl;
            this.readFrom = readFrom;
        }
    }

    public CombinedConfig(Log log)
    {
        this.log = log;
    }

    public boolean addRemoteConfig(List<String> urls, String remoteType)
    {
        boolean successReadingRemoteConfig = true;
        for (String url : urls)
        {
            if (!addRemoteConfig(url, remoteType))
            {
                successReadingRemoteConfig = false;
            }
        }
        return successReadingRemoteConfig;
    }

    private boolean addRemoteConfig(String baseUrl, String remoteType)
    {
        String url = baseUrl + TRANSFORM_CONFIG;
        HttpGet httpGet = new HttpGet(url);
        boolean successReadingRemoteConfig = true;
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
                                    int transformCount = allTransforms.size();
                                    addJsonSource(reader, baseUrl, remoteType+" on "+baseUrl);
                                    if (transformCount == allTransforms.size())
                                    {
                                        successReadingRemoteConfig = false;
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
            log.error(e.getMessage());
            successReadingRemoteConfig = false;
        }
        return successReadingRemoteConfig;
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

    public void addLocalConfig(String path) throws IOException
    {
        boolean somethingRead = false;
        final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        if (jarFile.isFile())
        {
            JarFile jar = new JarFile(jarFile);
            Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries in jar
            String prefix = path + "/";
            List<String> names = new ArrayList<>();
            while (entries.hasMoreElements())
            {
                final String name = entries.nextElement().getName();
                if (name.startsWith(prefix) && name.length() > prefix.length())
                {
                    names.add(name);
                }
            }
            Collections.sort(names);
            for (String name : names)
            {
                somethingRead = true;
                addJsonSource(new InputStreamReader(getResourceAsStream(name)), null,
                        name+" from jar "+jarFile.getName());
            }

            jar.close();
        }
        else
        {
            URL url = getClass().getClassLoader().getResource(path);
            if (url != null)
            {
                File root = new File(url.getPath());
                String rootPath = root.getPath();
                if (root.isDirectory())
                {
                    File[] files = root.listFiles();
                    Arrays.sort(files, (file1, file2) -> file1.getName().compareTo(file2.getName()));
                    for (File file: files)
                    {
                        somethingRead = true;
                        addJsonSource(new FileReader(file), null,"File " + file.getPath());
                    }
                }
                else
                {
                    somethingRead = true;
                    addJsonSource(new FileReader(root), null, "File " + rootPath);
                }
            }
        }

        if (!somethingRead)
        {
            log.warn("No config read from "+path);
        }
    }

    private InputStream getResourceAsStream(String resource)
    {
        final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        return in == null ? getClass().getResourceAsStream(resource) : in;
    }

    private void addJsonSource(Reader reader, String baseUrl, String readFrom) throws IOException
    {
        JsonNode jsonNode = jsonObjectMapper.readValue(reader, new TypeReference<JsonNode>() {});
        if (log.isTraceEnabled())
        {
            log.trace(readFrom+" config is: "+jsonNode);
        }
        else
        {
            log.debug(readFrom+" config read");
        }

        JsonNode transformOptions = jsonNode.get(TRANSFORM_OPTIONS);
        if (transformOptions != null && transformOptions.isObject())
        {
            Iterator<Map.Entry<String, JsonNode>> iterator = transformOptions.fields();
            while (iterator.hasNext())
            {
                Map.Entry<String, JsonNode> entry = iterator.next();

                JsonNode options = entry.getValue();
                if (options.isArray())
                {
                    String optionsName = entry.getKey();
                    allTransformOptions.put(optionsName, (ArrayNode)options);
                }
            }
        }

        JsonNode transformers = jsonNode.get(TRANSFORMERS);
        if (transformers != null && transformers.isArray())
        {
            for (JsonNode transformer : transformers)
            {
                if (transformer.isObject())
                {
                    allTransforms.add(new TransformNodeAndItsOrigin((ObjectNode)transformer, baseUrl, readFrom));
                }
            }
        }
    }

    public void register(TransformServiceRegistryImpl.Data data, TransformServiceRegistryImpl registry) throws IOException
    {
        List<TransformAndItsOrigin> transformers = getTransforms();
        transformers.forEach(t->registry.register(data, t.transform, t.baseUrl, t.readFrom));
    }

    public List<TransformAndItsOrigin> getTransforms() throws IOException
    {
        List<TransformAndItsOrigin> transforms = new ArrayList<>();

        // After all json input has been loaded build the output with the options in place.
        ArrayNode transformersNode = jsonObjectMapper.createArrayNode();
        for (TransformNodeAndItsOrigin entity : allTransforms)
        {
            transformersNode.add(entity.node);

            try
            {
                ArrayNode transformOptions = (ArrayNode) entity.node.get(TRANSFORM_OPTIONS);
                if (transformOptions != null)
                {

                    ArrayNode options;
                    int size = transformOptions.size();
                    if (size == 1)
                    {
                        // If there is a single node option reference, we can just use it.
                        int i = 0;
                        options = getTransformOptions(transformOptions, i, entity.node);
                    }
                    else
                    {
                        // If there are many node option references (typically in a pipeline), then each element
                        // has a group for each set of node options.
                        options = jsonObjectMapper.createArrayNode();
                        for (int i = size - 1; i >= 0; i--)
                        {
                            JsonNode referencedTransformOptions = getTransformOptions(transformOptions, i, entity.node);
                            if (referencedTransformOptions != null)
                            {
                                ObjectNode element = jsonObjectMapper.createObjectNode();
                                options.add(element);

                                ObjectNode group = jsonObjectMapper.createObjectNode();
                                group.set(TRANSFORM_OPTIONS, referencedTransformOptions);
                                element.set(GROUP, group);
                            }
                        }
                    }
                    if (options == null || options.size() == 0)
                    {
                        entity.node.remove(TRANSFORM_OPTIONS);
                    }
                    else
                    {
                        entity.node.set(TRANSFORM_OPTIONS, options);
                    }
                }

                try
                {
                    Transformer transform = jsonObjectMapper.convertValue(entity.node, Transformer.class);
                    transforms.add(new TransformAndItsOrigin(transform, entity.baseUrl, entity.readFrom));
                }
                catch (IllegalArgumentException e)
                {
                    log.error("Invalid transformer "+getTransformName(entity.node)+" "+e.getMessage()+" baseUrl="+entity.baseUrl);
                }
            }
            catch (IllegalArgumentException e)
            {
                String transformString = jsonObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(entity.node);
                log.error(e.getMessage());
                log.debug(transformString);
            }
        }
        if (log.isTraceEnabled())
        {
            log.trace("Combined config:\n"+jsonObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(transformersNode));
        }

        transforms = sortTransformers(transforms);
        return transforms;
    }

    // Sort transformers so there are no forward references, if that is possible.
    private List<TransformAndItsOrigin> sortTransformers(List<TransformAndItsOrigin> original)
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
                String name = entry.transform.getTransformerName();
                List<TransformStep> pipeline = entry.transform.getTransformerPipeline();
                boolean addEntry = true;
                if (pipeline != null && !pipeline.isEmpty())
                {
                    for (TransformStep step : pipeline)
                    {
                        String stepName = step.getTransformerName();
                        if (!transformerNames.contains(stepName))
                        {
                            todo.add(entry);
                            addEntry = false;
                            break;
                        }
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

    private ArrayNode getTransformOptions(ArrayNode transformOptions, int i, ObjectNode transform)
    {
        ArrayNode options = null;
        JsonNode optionName = transformOptions.get(i);
        if (optionName.isTextual())
        {
            String name = optionName.asText();
            options = allTransformOptions.get(name);
            if (options == null)
            {
                String message = "Reference to \"transformOptions\": \"" + name + "\" not found. Transformer " +
                        getTransformName(transform) + " ignored.";
                throw new IllegalArgumentException(message);
            }
        }
        return options;
    }

    private String getTransformName(ObjectNode transform)
    {
        String name = "Unknown";
        JsonNode nameNode = transform.get(TRANSFORMER_NAME);
        if (nameNode != null && nameNode.isTextual())
        {
            name = '"'+nameNode.asText()+'"';
        }
        return name;
    }
}
