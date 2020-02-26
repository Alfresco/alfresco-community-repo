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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private int fileCount;

    public ConfigFileFinder(ObjectMapper jsonObjectMapper)
    {
        this.jsonObjectMapper = jsonObjectMapper;
    }

    public int getFileCount()
    {
        return fileCount;
    }

    public void setFileCount(int fileCount)
    {
        this.fileCount = fileCount;
    }

    public boolean readFiles(String path, Log log)
    {
        AtomicBoolean successReadingConfig = new AtomicBoolean(true);
        try
        {
            AtomicBoolean somethingRead = new AtomicBoolean(false);

            // Try reading resources in a jar
            final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            if (jarFile.isFile())
            {
                readFromJar(jarFile, path, log, successReadingConfig, somethingRead);
            }
            else
            {
                // Try reading resources from disk
                Iterator<URL> pathUrls = getClass().getClassLoader().getResources(path).asIterator();
                while(pathUrls.hasNext())
                {
                    URL url = pathUrls.next();
                    if (url != null)
                    {
                        String urlPath = url.getPath();
                        readFromDisk(urlPath, log, successReadingConfig, somethingRead);
                    }
                }
            }

            if (!somethingRead.get() && new File(path).exists())
            {
                // Try reading files from disk
                readFromDisk(path, log, successReadingConfig, somethingRead);
            }

            if (!somethingRead.get())
            {
                log.debug("No config read from "+path);
            }
        }
        catch (IOException | URISyntaxException e)
        {
            log.error("Error reading from "+path, e);
            successReadingConfig.set(false);
        }
        return successReadingConfig.get();
    }

    private void readFromJar(File jarFile, String path, Log log, AtomicBoolean successReadingConfig, AtomicBoolean somethingRead) throws IOException
    {
        JarFile jar = new JarFile(jarFile);
        try
        {
            Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries in jar
            String prefix = path + "/";
            List<String> names = new ArrayList<>();
            while (entries.hasMoreElements())
            {
                final String name = entries.nextElement().getName();
                if ((name.startsWith(prefix) && name.length() > prefix.length()) ||
                    (name.equals(path)))
                {
                    names.add(name);
                }
            }
            Collections.sort(names);
            for (String name : names)
            {
                InputStreamReader reader = new InputStreamReader(getResourceAsStream(name));
                readFromReader(successReadingConfig, somethingRead, reader, "resource", name, null, log);
            }
        }
        finally
        {
            jar.close();
        }
    }

    private void readFromDisk(String path, Log log, AtomicBoolean successReadingConfig, AtomicBoolean somethingRead) throws FileNotFoundException
    {
        File root = new File(path);
        if (root.isDirectory())
        {
            File[] files = root.listFiles();
            Arrays.sort(files, (file1, file2) -> file1.getName().compareTo(file2.getName()));
            for (File file : files)
            {
                // Only read files in the config directory
                if (!file.isDirectory())
                {
                    FileReader reader = new FileReader(file);
                    String filePath = file.getPath();
                    readFromReader(successReadingConfig, somethingRead, reader, "file", filePath, null, log);
                }
                else
                {
                    log.debug("Skipping directory " + file.getName() + " in " + path);
                }
            }
        }
        else
        {
            FileReader reader = new FileReader(root);
            String filePath = root.getPath();
            readFromReader(successReadingConfig, somethingRead, reader, "file", filePath, null, log);
        }
    }

    private InputStream getResourceAsStream(String resource)
    {
        final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        return in == null ? getClass().getResourceAsStream(resource) : in;
    }

    private void readFromReader(AtomicBoolean successReadingConfig, AtomicBoolean somethingRead,
                               Reader reader, String readFrom, String path, String baseUrl, Log log)
    {
        somethingRead.set(true);
        boolean success = readFile(reader, readFrom, path, null, log);
        if (success)
        {
            fileCount++;
        }
        boolean newSuccessReadingConfig = successReadingConfig.get();
        newSuccessReadingConfig &= success;
        successReadingConfig.set(newSuccessReadingConfig);
    }

    public boolean readFile(Reader reader, String readFrom, String path, String baseUrl, Log log)
    {
        // At the moment it is assumed the file is Json, but that does not need to be the case.
        // We have the path including extension.
        boolean successReadingConfig = true;
        try
        {
            JsonNode jsonNode = jsonObjectMapper.readValue(reader, JsonNode.class);
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
            log.error("Error reading "+path, e);
            successReadingConfig = false;
        }
        return successReadingConfig;
    }

    protected abstract void readJson(JsonNode jsonNode, String readFromMessage, String baseUrl) throws IOException;
}
