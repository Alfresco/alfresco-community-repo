/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.extensions.config.ConfigSource;
import org.springframework.extensions.config.source.ClassPathConfigSource;
import org.springframework.extensions.config.source.FileConfigSource;
import org.springframework.extensions.config.xml.XMLConfigService;

/**
 * Base class for all JUnit tests
 * 
 * @author gavinc, Neil McErlean
 */
public abstract class BaseTest extends TestCase
{
    protected String resourcesDir;
    
    protected boolean loadFromClasspath;
    
    public BaseTest()
    {
        // GC: Added this to allow flexible test resources folder configuration
        // Try to get resources dir from a system property otherwise uses the default hardcoded
        // backward compatible
        String resourcesDir = null;
        // This allows subclasses to override the getResourcesDir method (e.g. by passing classpath: )
        if(getResourcesDir()==null)
        {
            resourcesDir = System.getProperty("alfresco.test.resources.dir");
            if(resourcesDir == null || resourcesDir.equals(""))
            {
                // defaults to source/test-resources
                resourcesDir = System.getProperty("user.dir") + File.separator + "source" + File.separator + "test-resources";
            }
        }
        else
        {
            resourcesDir = getResourcesDir();
        }
        loadFromClasspath = resourcesDir.startsWith("classpath:");
        // Returns the resources dir with trailing separator or just the classpath: string in case that was specified  
        this.resourcesDir = resourcesDir + ((loadFromClasspath) ? "" : File.separator); 
    }

    /**
     * Override this method to pass a custom resource dir. 
     * Valid values are a file system path or the string "classpath:"
     * @return
     */
    public String getResourcesDir()
    {
        return this.resourcesDir;
    }
   
    /**
     * Checks for validity of the resource location. 
     * 
     * In case of file resource, provide the full file path
     * In case of classpath resources, please pass the full resource URI, prepended with the classpath: string 
     * @param fullFileName
     */
    protected void assertFileIsValid(String fullFileName)
    {
        if(loadFromClasspath)
        {
            // if we load from classpath, we need to remove the "classpath:" trailing string
            String resourceName = fullFileName.substring(fullFileName.indexOf(":") + 1);
            assertNotNull(resourceName);
            URL configResourceUrl = getClass().getClassLoader().getResource(resourceName);
            assertNotNull(configResourceUrl);            
        }
        else
        {
            File f = new File(fullFileName);
            assertTrue("Required file missing: " + fullFileName, f.exists());
            assertTrue("Required file not readable: " + fullFileName, f.canRead());
        }
    }
    
    /**
     * Loads a config file from filesystem or classpath
     *  
     * In case of file resource, just provide the file name relative to resourceDir
     * In case of classpath resources, just provide the resource URI, without with the prepending classpath: string 
     * @param xmlConfigFile
     * @return
     */
    protected XMLConfigService initXMLConfigService(String xmlConfigFile)
    {
        XMLConfigService svc = null;
        if(loadFromClasspath)
        {
            svc = new XMLConfigService(new ClassPathConfigSource(xmlConfigFile));
        }
        else
        {
            String fullFileName = getResourcesDir() + xmlConfigFile;
            assertFileIsValid(fullFileName);
            svc = new XMLConfigService(new FileConfigSource(fullFileName));
        }
        svc.initConfig();
        return svc;
    }
    
    protected XMLConfigService initXMLConfigService(String xmlConfigFile, String overridingXmlConfigFile)
    {
        List<String> files = new ArrayList<String>(2);
        files.add(xmlConfigFile);
        files.add(overridingXmlConfigFile);
        return initXMLConfigService(files);
    }

    protected XMLConfigService initXMLConfigService(List<String> xmlConfigFilenames)
    {
        List<String> configFiles = new ArrayList<String>();
        for (String filename : xmlConfigFilenames)
        {
            // if we load from classpath then no need to prepend the resources dir (which will be .equals("classpath:")
            String path = ((loadFromClasspath) ? "" : getResourcesDir()) + filename;
            assertFileIsValid(path);
            configFiles.add(path);
        }
        ConfigSource configSource = (loadFromClasspath) ? new ClassPathConfigSource(configFiles) : new FileConfigSource(configFiles);
        XMLConfigService svc = new XMLConfigService(configSource);
        svc.initConfig();
        return svc;
    }
}
