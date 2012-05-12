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
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.OpenOfficeURI;
import org.alfresco.util.exec.RuntimeExec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A map giving the open office start up command, which depends on the version of
 * OpenOffice or LibreOffice.
 * 
 * @author Alan Davis
 */
public class OpenOfficeCommandLine extends AbstractMap<String, List<String>>
{
    private static final String[] EXTENSIONS = new String[] {"", ".exe", ".com", ".bat", ".cmd"};

    private static final Log logger = LogFactory.getLog(OpenOfficeCommandLine.class);

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();

    private Map<String, List<String>> map = new HashMap<String, List<String>>();
    
    private boolean windows;
    
    public OpenOfficeCommandLine(String exe, String port, String user) throws IOException
    {
        windows = isWindows();
        File executable = findExecutable(exe);
        File officeHome = getOfficeHome(executable);

        List<String> command = new ArrayList<String>();
        String acceptValue = "socket,host=127.0.0.1,port="+port+";urp;StarOffice.ServiceManager";
        String userInstallation = new OpenOfficeURI(user).toString();
        command.add(executable == null ? exe : executable.getAbsolutePath());
        if (isLibreOffice3Dot5(officeHome))
        {
            command.add("--accept=" + acceptValue);
            command.add("-env:UserInstallation=" + userInstallation);
            command.add("--headless");
            command.add("--nocrashreport");
            //command.add("--nodefault"); included by JOD
            command.add("--nofirststartwizard");
            //command.add("--nolockcheck"); included by JOD
            command.add("--nologo");
            command.add("--norestore");
            logger.info("Using GNU based LibreOffice command: "+command);
        }
        else
        {
            command.add("-accept=" + acceptValue);
            command.add("-env:UserInstallation=" + userInstallation);
            command.add("-headless");
            command.add("-nocrashreport");
            //command.add("-nodefault"); included by JOD
            command.add("-nofirststartwizard");
            //command.add("-nolockcheck");  included by JOD
            command.add("-nologo");
            command.add("-norestore");
            logger.info("Using original OpenOffice command: "+command);
        }
        map.put(RuntimeExec.KEY_OS_DEFAULT, command);
    }
    
    private File getOfficeHome(File executable)
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
    
    private File findExecutable(String executableName)
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

    private boolean isLibreOffice3Dot5(File officeHome)
    {
        return
            officeHome != null &&
            !new File(officeHome, "basis-link").isFile() &&
             new File(officeHome, "ure-link").isFile();
    }

    private static boolean isLinux()
    {
        return OS_NAME.startsWith("linux");
    }

    private static boolean isMac()
    {
        return OS_NAME.startsWith("mac");
    }

    private static boolean isWindows()
    {
        return OS_NAME.startsWith("windows");
    }

    @Override
    public Set<java.util.Map.Entry<String, List<String>>> entrySet()
    {
        return map.entrySet();
    }
}
