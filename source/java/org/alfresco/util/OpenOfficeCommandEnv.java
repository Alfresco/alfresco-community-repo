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
 * A map giving the environment openoffice or libreoffice commands require to start.
 * 
 * @author Alan Davis
 */
public class OpenOfficeCommandEnv extends AbstractMap<String, String>
{
    private static final Log logger = LogFactory.getLog(OpenOfficeCommandLine.class);
    private static final String DYLD_LIBRARY_PATH = "DYLD_LIBRARY_PATH";
    
    private Map<String, String> map = new HashMap<String, String>(System.getenv());
    private OpenOfficeVariant variant = new OpenOfficeVariant();
    
    public OpenOfficeCommandEnv(String exe) throws IOException
    {
        if (variant.isMac())
        {
            map.remove(DYLD_LIBRARY_PATH);
            logger.debug("Removing $DYLD_LIBRARY_PATH from the environment so that LibreOffice/OpenOffice will start on Mac.");
        }
    }

    @Override
    public Set<java.util.Map.Entry<String, String>> entrySet()
    {
        return map.entrySet();
    }
}
