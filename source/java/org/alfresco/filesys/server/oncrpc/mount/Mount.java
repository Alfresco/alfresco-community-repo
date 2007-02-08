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
package org.alfresco.filesys.server.oncrpc.mount;

/**
 * Mount Server Constants Class
 * 
 * @author GKSpencer
 */
public final class Mount {

	// Program and version id

	public static final int ProgramId 		= 100005;
	public static final int VersionId1 		= 1;
	public static final int VersionId3 		= 3;

	// RPC procedure ids (version 1)

	public static final int ProcNull1 		= 0;
	public static final int ProcMnt1 		= 1;
	public static final int ProcDump1 		= 2;
	public static final int ProcUMnt1 		= 3;
	public static final int ProcUMntAll1 	= 4;
	public static final int ProcExport1 	= 5;
	public static final int ProcExportAll1 	= 6;
	public static final int ProcMax1 		= 6;

	// RPC procedure ids (version 3)

	public static final int ProcNull3 		= 0;
	public static final int ProcMnt3 		= 1;
	public static final int ProcDump3 		= 2;
	public static final int ProcUMnt3 		= 3;
	public static final int ProcUMntAll3 	= 4;
	public static final int ProcExport3 	= 5;
	public static final int ProcMax3 		= 5;

	// Mount server status codes

	public static final int StsSuccess 		= 0;
	public static final int StsPerm 		= 1;
	public static final int StsNoEnt 		= 2;
	public static final int StsIO 			= 5;
	public static final int StsAccess 		= 13;
	public static final int StsNotDir 		= 20;
	public static final int StsInval 		= 22;
	public static final int StsNameTooLong 	= 63;
	public static final int StsNotSupp 		= 10004;
	public static final int StsServerFault 	= 10006;

	// Data structure limits

	public static final int FileHandleSize1 = 32;
	public static final int FileHandleSize3 = 32; // can be 64 for v3

	// RPC procedure names

	private static final String[] _procNames = { "Null", "Mount", "Dump",
			"UnMount", "UnMountAll", "Export", "ExportAll" };

	/**
	 * Return a procedure id as a name
	 * 
	 * @param id
	 *            int
	 * @return String
	 */
	public final static String getProcedureName(int id) {
		if (id < 0 || id > ProcMax1)
			return null;
		return _procNames[id];
	}
}
