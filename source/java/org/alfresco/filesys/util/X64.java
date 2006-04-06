/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
