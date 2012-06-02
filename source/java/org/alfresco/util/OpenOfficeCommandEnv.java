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
