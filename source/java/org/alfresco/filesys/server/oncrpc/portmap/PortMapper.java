/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.filesys.server.oncrpc.portmap;

/**
 * PortMapper RPC Service Constants Class
 * 
 * @author GKSpencer
 */
public class PortMapper {

	// Default port mapper port

	public static final int DefaultPort = 111;

	// Program and version id

	public static final int ProgramId 	= 100000;
	public static final int VersionId 	= 2;

	// RPC procedure ids

	public static final int ProcNull 	= 0;
	public static final int ProcSet 	= 1;
	public static final int ProcUnSet 	= 2;
	public static final int ProcGetPort = 3;
	public static final int ProcDump 	= 4;
	public static final int ProcMax 	= 4;

	// RPC procedure names

	private static final String[] _procNames = { "Null", "Set", "UnSet",
			"GetPort", "Dump" };

	/**
	 * Return a procedure id as a name
	 * 
	 * @param id
	 *            int
	 * @return String
	 */
	public final static String getProcedureName(int id) {
		if (id < 0 || id > ProcMax)
			return null;
		return _procNames[id];
	}
}
