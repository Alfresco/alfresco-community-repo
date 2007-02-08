/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
