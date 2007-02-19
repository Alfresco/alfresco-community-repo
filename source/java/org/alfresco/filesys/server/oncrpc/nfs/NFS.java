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
package org.alfresco.filesys.server.oncrpc.nfs;

/**
 * NFS Server Constants Class
 * 
 * @author GKSpencer
 */
public final class NFS {

	// Default NFS server port

	public static final int DefaultPort = 2049;

	// Program and version id

	public static final int ProgramId = 100003;
	public static final int VersionId = 3;

	// RPC procedure ids

	public static final int ProcNull 		= 0;
	public static final int ProcGetAttr 	= 1;
	public static final int ProcSetAttr 	= 2;
	public static final int ProcLookup 		= 3;
	public static final int ProcAccess 		= 4;
	public static final int ProcReadLink 	= 5;
	public static final int ProcRead 		= 6;
	public static final int ProcWrite 		= 7;
	public static final int ProcCreate 		= 8;
	public static final int ProcMkDir 		= 9;
	public static final int ProcSymLink 	= 10;
	public static final int ProcMkNode 		= 11;
	public static final int ProcRemove 		= 12;
	public static final int ProcRmDir 		= 13;
	public static final int ProcRename 		= 14;
	public static final int ProcLink 		= 15;
	public static final int ProcReadDir 	= 16;
	public static final int ProcReadDirPlus = 17;
	public static final int ProcFsStat 		= 18;
	public static final int ProcFsInfo 		= 19;
	public static final int ProcPathConf 	= 20;
	public static final int ProcCommit 		= 21;
	public static final int ProcMax 		= 21;

	// NFS server status codes

	public static final int StsSuccess 		= 0;
	public static final int StsPerm 		= 1;
	public static final int StsNoEnt 		= 2;
	public static final int StsIO 			= 5;
	public static final int StsNxIO 		= 6;
	public static final int StsAccess 		= 13;
	public static final int StsExist 		= 17;
	public static final int StsXDev 		= 18;
	public static final int StsNoDev 		= 19;
	public static final int StsNotDir 		= 20;
	public static final int StsIsDir 		= 21;
	public static final int StsInVal 		= 22;
	public static final int StsFBig 		= 27;
	public static final int StsNoSpc 		= 28;
	public static final int StsROFS 		= 30;
	public static final int StsMLink 		= 31;
	public static final int StsNameTooLong 	= 63;
	public static final int StsNotEmpty 	= 66;
	public static final int StsDQuot 		= 69;
	public static final int StsStale 		= 70;
	public static final int StsRemote 		= 71;
	public static final int StsBadHandle 	= 10001;
	public static final int StsNotSync 		= 10002;
	public static final int StsBadCookie 	= 10003;
	public static final int StsNotSupp 		= 10004;
	public static final int StsTooSmall 	= 10005;
	public static final int StsServerFault 	= 10006;
	public static final int StsBadType 		= 10007;
	public static final int StsJukeBox 		= 10008;

	// Data structure limits

	public static final int FileHandleSize = 32; // can be 64 for NFS v3
	public static final int WriteVerfSize  = 8;
	public static final int CreateVerfSize = 8;
	public static final int CookieVerfSize = 8;

	// File types

	public static final int FileTypeReg  = 1;
	public static final int FileTypeDir  = 2;
	public static final int FileTypeBlk  = 3;
	public static final int FileTypeChr  = 4;
	public static final int FileTypeLnk  = 5;
	public static final int FileTypeSock = 6;
	public static final int FileTypeFifo = 7;

	// Filesystem properties

	public static final int FileSysLink 		= 0x0001; // supports hard links
	public static final int FileSysSymLink 		= 0x0002; // supports symbolic links
	public static final int FileSysHomogeneuos 	= 0x0004; // PATHCONF valid for all files
	public static final int FileSysCanSetTime 	= 0x0008; // can set time on server side

	// Access mask

	public static final int AccessRead 		= 0x0001;
	public static final int AccessLookup 	= 0x0002;
	public static final int AccessModify 	= 0x0004;
	public static final int AccessExtend 	= 0x0008;
	public static final int AccessDelete 	= 0x0010;
	public static final int AccessExecute 	= 0x0020;
	public static final int AccessAll 		= 0x003F;

	// Create mode values

	public static final int CreateUnchecked = 1;
	public static final int CreateGuarded 	= 2;
	public static final int CreateExclusive = 3;

	// Write request stable values

	public static final int WriteUnstable = 0;
	public static final int WriteDataSync = 1;
	public static final int WriteFileSync = 2;

	// Set attributes file timestamp settings

	public static final int DoNotSetTime 	= 0;
	public static final int SetTimeServer 	= 1;
	public static final int SetTimeClient 	= 2;

	// RPC procedure names

	private static final String[] _procNames = { "Null", "GetAttr", "SetAttr",
			"Lookup", "Access", "ReadLink", "Read", "Write", "Create", "MkDir",
			"SymLink", "MkNode", "Remove", "RmDir", "Rename", "Link",
			"ReadDir", "ReadDirPlus", "FsStat", "FsInfo", "PathConf", "Commit" };

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

	/**
	 * Return an error status string for the specified status code
	 * 
	 * @param sts
	 *            int
	 * @return String
	 */
	public static final String getStatusString(int sts) {
		String str = null;

		switch (sts) {
		case StsSuccess:
			str = "Success status";
			break;
		case StsAccess:
			str = "Access denied";
			break;
		case StsBadCookie:
			str = "Bad cookie";
			break;
		case StsBadHandle:
			str = "Bad handle";
			break;
		case StsBadType:
			str = "Bad type";
			break;
		case StsDQuot:
			str = "Quota exceeded";
			break;
		case StsPerm:
			str = "No permission";
			break;
		case StsExist:
			str = "Already exists";
			break;
		case StsFBig:
			str = "File too large";
			break;
		case StsInVal:
			str = "Invalid argument";
			break;
		case StsIO:
			str = "I/O error";
			break;
		case StsIsDir:
			str = "Is directory";
			break;
		case StsJukeBox:
			str = "Jukebox";
			break;
		case StsMLink:
			str = "Too many hard links";
			break;
		case StsNameTooLong:
			str = "Name too long";
			break;
		case StsNoDev:
			str = "No such device";
			break;
		case StsNoEnt:
			str = "No entity";
			break;
		case StsNoSpc:
			str = "No space left on device";
			break;
		case StsNotSync:
			str = "Update synchronization mismatch";
			break;
		case StsNotDir:
			str = "Not directory";
			break;
		case StsNotEmpty:
			str = "Not empty";
			break;
		case StsNotSupp:
			str = "Not supported";
			break;
		case StsNxIO:
			str = "Nxio";
			break;
		case StsRemote:
			str = "Too many levels of remote in path";
			break;
		case StsROFS:
			str = "Readonly filesystem";
			break;
		case StsServerFault:
			str = "Server fault";
			break;
		case StsStale:
			str = "Stale";
			break;
		case StsTooSmall:
			str = "Too small";
			break;
		case StsXDev:
			str = "Cross device hard link attempted";
			break;
		}

		return str;
	}
}
