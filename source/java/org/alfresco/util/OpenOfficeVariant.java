/* Copyright (C) 2005-2012 Alfresco Software Limited.
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

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides OpenOffice and LibreOffice variant information.
 * 
 * @author Alan Davis
 */
public class OpenOfficeVariant
{
    private static final String[] EXTENSIONS = new String[] {"", ".exe", ".com", ".bat", ".cmd"};
    private static final Log logger = LogFactory.getLog(OpenOfficeCommandLine.class);
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    
    private final boolean windows = isWindows();
    
    public File getOfficeHome(File executable)
    {
        // Get the grandparent
        File officeHome = executable;
        for (int i=1; officeHome != null && i <= 2; i++)
        {
            officeHome = officeHome.getParentFile();
        }
        
        if (officeHome == null && executable != null)
        {
            throw new AlfrescoRuntimeException("Did not find OppenOffice home from executable "+executable.getAbsolutePath());
        }
    
        return officeHome;
    }

    public File findExecutable(String executableName)
    {
        File file = new File(executableName);
        if (file.isAbsolute())
        {
            file = canExecute(file);
        }
        else
        {
            file = findExecutableOnPath(executableName);
        }
        return file;
    }

    private File findExecutableOnPath(String executableName)
    {  
        String systemPath = System.getenv("PATH");  
        systemPath = systemPath == null ? System.getenv("path") : systemPath;
        String[] pathDirs = systemPath.split(File.pathSeparator);  
    
        File fullyQualifiedExecutable = null;  
        for (String pathDir : pathDirs)  
        {
            File file = canExecute(new File(pathDir, executableName));
            if (file != null)
            {
                fullyQualifiedExecutable = file;
                break;
            }
        }  
        return fullyQualifiedExecutable;  
    }

    private File canExecute(File file)
    {
        File fullyQualifiedExecutable = null;
        File dir = file.getParentFile();
        String name = file.getName();
        for (String ext: EXTENSIONS)
        {
            file = new File(dir, name+ext);
            if (file.canExecute())  
            {
                fullyQualifiedExecutable = file;  
                break;
            }
            if (!windows)
            {
                break;
            }
        }
        return fullyQualifiedExecutable;
    }

    public boolean isLibreOffice3Dot5(File officeHome)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("System.getProperty(\"os.name\")="+System.getProperty("os.name"));
            logger.debug("officeHome="+(officeHome == null ? null : "'"+officeHome.getAbsolutePath()+"'"));
            logger.debug("basis-link:"+new File(officeHome, "basis-link").isFile());
            logger.debug("basis-link:"+new File(officeHome, "basis-link").isDirectory());
            logger.debug("  ure-link:"+new File(officeHome, "ure-link").isFile());
            logger.debug("  ure-link:"+new File(officeHome, "ure-link").isDirectory());
            logger.debug("    NOTICE:"+new File(officeHome, "NOTICE").isFile());
        }
        return
            officeHome != null &&
            !new File(officeHome, "basis-link").isFile() &&
            (new File(officeHome, "ure-link").isFile() || new File(officeHome, "ure-link").isDirectory());
    }

    public boolean isLibreOffice3Dot6(File officeHome)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("    NOTICE:"+new File(officeHome, "NOTICE").isFile());
        }
        return isLibreOffice3Dot5(officeHome) && new File(officeHome, "NOTICE").isFile();
    }

    public boolean isLinux()
    {
        return OS_NAME.startsWith("linux");
    }

    public boolean isMac()
    {
        return OS_NAME.startsWith("mac");
    }

    public boolean isWindows()
    {
        return OS_NAME.startsWith("windows");
    }
}