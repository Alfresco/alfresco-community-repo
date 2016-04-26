package org.alfresco.util;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private static final Log logger = LogFactory.getLog(OpenOfficeCommandLine.class);
    private Map<String, List<String>> map = new HashMap<String, List<String>>();
    private OpenOfficeVariant variant = new OpenOfficeVariant();
    
    public OpenOfficeCommandLine(String exe, String port, String user) throws IOException
    {
        File executable = variant.findExecutable(exe);
        File officeHome = variant.getOfficeHome(executable);

        List<String> command = new ArrayList<String>();
        String acceptValue = "socket,host=127.0.0.1,port="+port+";urp;StarOffice.ServiceManager";
        String userInstallation = new OpenOfficeURI(user).toString();
        command.add(executable == null ? exe : executable.getAbsolutePath());
        if (variant.isLibreOffice3Dot5(officeHome))
        {
            command.add("--accept=" + acceptValue);
            if (variant.isMac() && !variant.isLibreOffice3Dot6(officeHome))
            {
                command.add("--env:UserInstallation=" + userInstallation);
            }
            else
            {
                command.add("-env:UserInstallation=" + userInstallation);
            }
            command.add("--headless");
            command.add("--nocrashreport");
            //command.add("--nodefault"); included by JOD
            command.add("--nofirststartwizard");
            //command.add("--nolockcheck"); included by JOD
            command.add("--nologo");
            command.add("--norestore");
            logger.info("Using GNU based LibreOffice "+
                    (variant.isLibreOffice3Dot6(officeHome) ? "3.6" : "3.5")+" command"+
                    (variant.isMac() ? " on Mac" : "")+": "+command);
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
    
    @Override
    public Set<java.util.Map.Entry<String, List<String>>> entrySet()
    {
        return map.entrySet();
    }
}
