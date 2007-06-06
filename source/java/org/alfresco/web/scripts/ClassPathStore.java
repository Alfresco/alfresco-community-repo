/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.scripts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.ScriptLocation;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;


/**
 * ClassPath based Web Script Store
 * 
 * @author davidc
 */
public class ClassPathStore implements WebScriptStore, InitializingBean
{
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    protected boolean mustExist = false;
    protected String classPath;
    protected File fileDir;

    
    /**
     * Sets whether the class path must exist
     * 
     * If it must exist, but it doesn't exist, an exception is thrown
     * on initialisation of the store
     * 
     * @param mustExist
     */
    public void setMustExist(boolean mustExist)
    {
        this.mustExist = mustExist;
    }
    
    /**
     * Sets the class path
     * 
     * @param classPath  classpath
     */
    public void setClassPath(String classPath)
    {
        this.classPath = classPath;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet()
        throws Exception
    {
        ClassPathResource resource = new ClassPathResource(classPath);
        if (resource.exists())
        {
            fileDir = resource.getFile();
        }
        else if (mustExist)
        {
            throw new WebScriptException("Web Script Store classpath:" + classPath + " must exist; it was not found");
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptStore#exists()
     */
    public boolean exists()
    {
        return (fileDir != null);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptStore#getBasePath()
     */
    public String getBasePath()
    {
        return "classpath:" + classPath;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptStore#getDescriptionDocumentPaths()
     */
    public String[] getDescriptionDocumentPaths()
    {
        String[] paths;

        try
        {
            int filePathLength = fileDir.getAbsolutePath().length() +1;
            List<String> documentPaths = new ArrayList<String>();
            Resource[] resources = resolver.getResources("classpath*:" + classPath + "/**/*.desc.xml");
            for (Resource resource : resources)
            {
                if (resource instanceof FileSystemResource)
                {
                    String documentPath = resource.getFile().getAbsolutePath().substring(filePathLength);
                    documentPath = documentPath.replace('\\', '/');
                    documentPaths.add(documentPath);
                }
            }
            paths = documentPaths.toArray(new String[documentPaths.size()]);
        }
        catch(IOException e)
        {
            // Note: Ignore: no service description documents found
            paths = new String[0];
        }
        
        return paths;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptStore#getDescriptionDocument(java.lang.String)
     */
    public InputStream getDescriptionDocument(String documentPath)      
        throws IOException
    {
        File document = new File(fileDir, documentPath);
        if (!document.exists())
        {
            throw new IOException("Description document " + documentPath + " does not exist.");
        }
        return new FileInputStream(document);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptStore#getTemplateLoader()
     */
    public TemplateLoader getTemplateLoader()
    {
        FileTemplateLoader loader = null;
        try
        {
            loader = new FileTemplateLoader(fileDir);
        }
        catch (IOException e)
        {
            // Note: Can't establish loader, so return null
        }
        return loader;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptStore#getScriptLoader()
     */
    public ScriptLoader getScriptLoader()
    {
        return new ClassPathScriptLoader();
    }        
    
    /**
     * Class path based script loader
     * 
     * @author davidc
     */
    private class ClassPathScriptLoader implements ScriptLoader
    {

        /* (non-Javadoc)
         * @see org.alfresco.web.scripts.ScriptLoader#getScriptLocation(java.lang.String)
         */
        public ScriptLocation getScriptLocation(String path)
        {
            ScriptLocation location = null;
            File scriptPath = new File(fileDir, path);
            if (scriptPath.exists())
            {
                location = new ClassPathScriptLocation(scriptPath);
            }
            return location;
        }
    }

    /**
     * Class path script location
     * 
     * @author davidc
     */
    private static class ClassPathScriptLocation implements ScriptLocation
    {
        private File location;

        /**
         * Construct
         * 
         * @param location
         */
        public ClassPathScriptLocation(File location)
        {
            this.location = location;
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.repository.ScriptLocation#getInputStream()
         */
        public InputStream getInputStream()
        {
            try
            {
                return new FileInputStream(location);
            }
            catch (FileNotFoundException e)
            {
                throw new WebScriptException("Unable to retrieve input stream for script " + location.getAbsolutePath());
            }
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.repository.ScriptLocation#getReader()
         */
        public Reader getReader()
        {
            return new InputStreamReader(getInputStream());
        }

        @Override
        public String toString()
        {
            return location.getAbsolutePath();
        }
    }
    
}
