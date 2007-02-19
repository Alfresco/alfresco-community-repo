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

package org.alfresco.filesys.util;

/**
 * X64 Class
 * 
 * <p>Check if the platform is a 64bit operating system.
 * 
 * @author gkspencer
 */
public class X64
{
    /**
     * Check if we are running on a Windows 64bit system
     * 
     * @return boolean
     */
    public static boolean isWindows64()
    {
        // Check for Windows
        
        String prop = System.getProperty("os.name");
        if ( prop == null || prop.startsWith("Windows") == false)
            return false;

        // Check the OS architecture
        
        prop = System.getProperty("os.arch");
        if ( prop != null && prop.equalsIgnoreCase("amd64"))
            return true;
        
        // Check the VM name
        
        prop = System.getProperty("java.vm.name");
        if ( prop != null && prop.indexOf("64-Bit") != -1)
            return true;

        // Check the data model
        
        prop = System.getProperty("sun.arch.data.model");
        if ( prop != null && prop.equals("64"))
            return true;
        
        // Not 64 bit Windows
        
        return false;
    }
}
