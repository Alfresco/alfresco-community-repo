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
