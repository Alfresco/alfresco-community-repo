/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
 * Platform Class
 *
 * <p>Determine the platform type that we are runnng on.
 * 
 * @author gkspencer
 */
public class Platform {

	// Platform types

    public enum Type
    {
        Unchecked, Unknown, WINDOWS, LINUX, SOLARIS, MACOSX
    };

    // Platform type we are running on
    
    private static Type _platformType = Type.Unchecked;
    
    /**
     * Determine the platform type
     * 
     * @return Type
     */
    public static final Type isPlatformType()
    {
    	// Check if the type has been set
    	
    	if ( _platformType == Type.Unchecked)
    	{
            // Get the operating system type

            String osName = System.getProperty("os.name");

            if (osName.startsWith("Windows"))
                _platformType = Type.WINDOWS;
            else if (osName.equalsIgnoreCase("Linux"))
                _platformType = Type.LINUX;
            else if (osName.startsWith("Mac OS X"))
                _platformType = Type.MACOSX;
            else if (osName.startsWith("Solaris") || osName.startsWith("SunOS"))
                _platformType = Type.SOLARIS;
    	}
    	
    	// Return the current platform type
    	
    	return _platformType;
    }    	
}
