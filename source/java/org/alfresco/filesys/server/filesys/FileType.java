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
 * http://www.alfresco.com/legal/licensing" */
package org.alfresco.filesys.server.filesys;

/*
 * FileType.java
 *
 * Copyright (c) Starlasoft 2006. All rights reserved.
 */

/**
 * File Type Class
 * 
 * <p>File type constants.
 * 
 * @author GKSpencer
 */
public class FileType {

	// File types
	  
	public static final int RegularFile   = 1;
	public static final int Directory     = 2;
	public static final int SymbolicLink  = 3;
	public static final int HardLink      = 4;
	public static final int Device        = 5;
  
  /**
	 * Return a file type as a string
	 * 
	 * @param typ int
	 * @return String
	 */
	public final static String asString(int typ) {

		String typStr = "Unknown";

		switch (typ) {
		case RegularFile:
			typStr = "File";
			break;
		case Directory:
			typStr = "Directory";
			break;
		case SymbolicLink:
			typStr = "SymbolicLink";
			break;
		case HardLink:
			typStr = "HardLink";
			break;
		case Device:
			typStr = "Device";
			break;
		}

		return typStr;
	}
}
