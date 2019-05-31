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
import org.apache.commons.logging.Log;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This class recreates the json format used in ACS 6.1 where we just had an array of transformers and each
 * transformer has a list of transform options. The idea of this code is that it replaces the references with the
 * actual transform options that have been separated out into their own section.<p>
 *
 * The T-Router and T-Engines return the format with the transform option separated into their own section. Pipeline
 * definitions used by the LocalTransformServiceRegistry may use transform reference options defined in the json
 * returned by T-Engines.  with the actual definitions from the transform options
 * reference section. It also combines multiple json sources into a single jsonNode structure that can be parsed as
 * before.
 */
public class JsonConverter
{
    public static final String NAME = "name";
    private final Log log;

    private static final String TRANSFORM_OPTIONS = "transformOptions";
    private static final String GROUP = "group";
    private static final String TRANSFORMERS = "transformers";

    private Map<String, ArrayNode> allTransformOptions = new HashMap<>();
    private List<ObjectNode> allTransforms = new ArrayList<>();
    private ObjectMapper jsonObjectMapper = new ObjectMapper();

    JsonConverter(Log log)
    {
        this.log = log;
    }

    void addJsonSource(String path) throws IOException
    {
        final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        if (jarFile.isFile())
        {
            JarFile jar = new JarFile(jarFile);
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
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
                log.debug("Reading resource "+name);
                addJsonSource(new InputStreamReader(getResourceAsStream(name)));
            }

            jar.close();
        }
        else
        {
            URL url = getClass().getClassLoader().getResource(path);
            if (url != null)
            {
                File root = new File(url.getPath());
                if (root.isDirectory())
                {
                    File[] files = root.listFiles();
                    Arrays.sort(files, (file1, file2) -> file1.getName().compareTo(file2.getName()));
                    for (File file: files)
                    {
                        log.debug("Reading dir file "+file.getPath());
                        addJsonSource(new FileReader(file));
                    }
                }
                else
                {
                    log.debug("Reading file "+root.getPath());
                    addJsonSource(new FileReader(root));
                }
            }
        }
    }

    private InputStream getResourceAsStream(String resource)
    {
        final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        return in == null ? getClass().getResourceAsStream(resource) : in;
    }

    private void addJsonSource(Reader reader) throws IOException
    {
        JsonNode jsonNode = jsonObjectMapper.readValue(reader, new TypeReference<JsonNode>() {});

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
                    allTransforms.add((ObjectNode)transformer);
                }
            }
        }
    }

    public List<Transformer> getTransformers() throws IOException
    {
        List<Transformer> transformers = new ArrayList<>();

        // After all json input has been loaded build the output with the options in place.
        ArrayNode transformersNode = jsonObjectMapper.createArrayNode();
        for (ObjectNode transform : allTransforms)
        {
            try
            {
                ArrayNode transformOptions = (ArrayNode) transform.get(TRANSFORM_OPTIONS);
                if (transformOptions != null)
                {

                    ArrayNode options;
                    int size = transformOptions.size();
                    if (size == 1)
                    {
                        // If there is a single transform option reference, we can just use it.
                        int i = 0;
                        options = getTransformOptions(transformOptions, i, transform);
                    }
                    else
                    {
                        // If there are many transform option references (typically in a pipeline), then each element
                        // has a group for each set of transform options.
                        options = jsonObjectMapper.createArrayNode();
                        for (int i = size - 1; i >= 0; i--)
                        {
                            JsonNode referencedTransformOptions = getTransformOptions(transformOptions, i, transform);
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
                        transform.remove(TRANSFORM_OPTIONS);
                    }
                    else
                    {
                        transform.set(TRANSFORM_OPTIONS, options);
                    }
                }

                try
                {
                    Transformer transformer = jsonObjectMapper.convertValue(transform, Transformer.class);
                    transformers.add(transformer);
                }
                catch (IllegalArgumentException e)
                {
                    log.error("Invalid transformer "+getTransformName(transform)+" "+e.getMessage());
                }
                transformersNode.add(transform);
            }
            catch (IllegalArgumentException e)
            {
                String transformString = jsonObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(transform);
                log.error(e.getMessage()+"\n"+ transformString);
            }
        }

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
        JsonNode nameNode = transform.get(NAME);
        if (nameNode.isTextual())
        {
            name = '"'+nameNode.asText()+'"';
        }
        return name;
    }
}
