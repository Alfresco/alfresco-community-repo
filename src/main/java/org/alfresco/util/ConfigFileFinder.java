/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
package org.alfresco.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Used to find configuration files as resources from the jar file or from some external location. The path supplied
 * to {@link #readFiles(String, Log)} may be a directory name. Normally used by ConfigScheduler.
 *
 * @author adavis
 */
public abstract class ConfigFileFinder
{
    private final ObjectMapper jsonObjectMapper;

    public ConfigFileFinder(ObjectMapper jsonObjectMapper)
    {
        this.jsonObjectMapper = jsonObjectMapper;
    }

    public boolean readFiles(String path, Log log)
    {
        boolean successReadingConfig = true;
        try
        {
            boolean somethingRead = false;
            final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
            if (jarFile.isFile())
            {
                Enumeration<JarEntry> entries = new JarFile(jarFile).entries(); // gives ALL entries in jar
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
                    successReadingConfig &= readFile(new InputStreamReader(getResourceAsStream(name)),"resource", name, null, log);
                }

                new JarFile(jarFile).close();
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
                            successReadingConfig &= readFile(new FileReader(file), "file", file.getPath(), null, log);
                        }
                    }
                    else
                    {
                        somethingRead = true;
                        successReadingConfig = readFile(new FileReader(root), "file", rootPath, null, log);
                    }
                }
            }

            if (!somethingRead)
            {
                log.warn("No config read from "+path);
            }
        }
        catch (IOException e)
        {
            log.error("Error reading from "+path);
            successReadingConfig = false;
        }
        return successReadingConfig;
    }

    private InputStream getResourceAsStream(String resource)
    {
        final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        return in == null ? getClass().getResourceAsStream(resource) : in;
    }

    public boolean readFile(Reader reader, String readFrom, String path, String baseUrl, Log log)
    {
        // At the moment it is assumed the file is Json, but that does not need to be the case.
        // We have the path including extension.
        boolean successReadingConfig = true;
        try
        {
            JsonNode jsonNode = jsonObjectMapper.readValue(reader, new TypeReference<JsonNode>() {});
            String readFromMessage = readFrom + ' ' + path;
            if (log.isTraceEnabled())
            {
                log.trace(readFromMessage + " config is: " + jsonNode);
            }
            else
            {
                log.debug(readFromMessage + " config read");
            }
            readJson(jsonNode, readFromMessage, baseUrl);
        }
        catch (Exception e)
        {
            log.error("Error reading "+path);
            successReadingConfig = false;
        }
        return successReadingConfig;
    }

    protected abstract void readJson(JsonNode jsonNode, String readFromMessage, String baseUrl) throws IOException;
}
