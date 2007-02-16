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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.filesys.smb;

/**
 *  SMB packet type class
 */
public class PacketType
{
    // SMB packet types

    public static final int CreateDirectory 	= 0x00;
    public static final int DeleteDirectory 	= 0x01;
    public static final int OpenFile					= 0x02;
    public static final int CreateFile				= 0x03;
    public static final int CloseFile					= 0x04;
    public static final int FlushFile					= 0x05;
    public static final int DeleteFile				= 0x06;
    public static final int RenameFile				= 0x07;
    public static final int GetFileAttributes	= 0x08;
    public static final int SetFileAttributes = 0x09;
    public static final int ReadFile					= 0x0A;
    public static final int WriteFile					= 0x0B;
    public static final int LockFile					= 0x0C;
    public static final int UnLockFile				= 0x0D;
    public static final int CreateTemporary   = 0x0E;
    public static final int CreateNew         = 0x0F;
    public static final int CheckDirectory		= 0x10;

    public static final int ProcessExit       = 0x11;
    public static final int SeekFile					= 0x12;
    public static final int LockAndRead       = 0x13;
    public static final int WriteAndUnlock    = 0x14;
    public static final int ReadRaw           = 0x1A;
    public static final int ReadMpx           = 0x1B;
    public static final int ReadMpxSecondary  = 0x1C;
    public static final int WriteRaw          = 0x1D;
    public static final int WriteMpx          = 0x1E;
    public static final int WriteComplete     = 0x20;
    public static final int SetInformation2   = 0x22;
    public static final int QueryInformation2 = 0x23;
    public static final int LockingAndX       = 0x24;
    public static final int Transaction       = 0x25;
    public static final int TransactionSecond = 0x26;
    public static final int IOCtl             = 0x27;
    public static final int IOCtlSecondary    = 0x28;
    public static final int Copy              = 0x29;
    public static final int Move              = 0x2A;
    public static final int Echo              = 0x2B;
    public static final int WriteAndClose     = 0x2C;
    public static final int OpenAndX          = 0x2D;
    public static final int ReadAndX          = 0x2E;
    public static final int WriteAndX         = 0x2F;
    public static final int CloseAndTreeDisc  = 0x31;
    public static final int Transaction2      = 0x32;
    public static final int Transaction2Second= 0x33;
    public static final int FindClose2        = 0x34;
    public static final int FindNotifyClose   = 0x35;

    public static final int TreeConnect				= 0x70;
    public static final int TreeDisconnect  	= 0x71;
    public static final int Negotiate					= 0x72;
    public static final int SessionSetupAndX  = 0x73;
    public static final int LogoffAndX        = 0x74;
    public static final int TreeConnectAndX   = 0x75;
  
    public static final int DiskInformation   = 0x80;
    public static final int Search						= 0x81;
    public static final int Find              = 0x82;
    public static final int FindUnique        = 0x83;

    public static final int NTTransact        = 0xA0;
    public static final int NTTransactSecond  = 0xA1;
    public static final int NTCreateAndX      = 0xA2;
    public static final int NTCancel          = 0xA4;

    public static final int OpenPrintFile     = 0xC0;
    public static final int WritePrintFile    = 0xC1;
    public static final int ClosePrintFile    = 0xC2;
    public static final int GetPrintQueue     = 0xC3;

    //	Send message codes

    public static final int SendMessage				= 0xD0;
    public static final int SendBroadcast			= 0xD1;
    public static final int SendForward				= 0xD2;
    public static final int CancelForward			= 0xD3;
    public static final int GetMachineName		= 0xD4;
    public static final int SendMultiStart		= 0xD5;
    public static final int SendMultiEnd			= 0xD6;
    public static final int SendMultiText			= 0xD7;

    //  Transaction2 operation codes

    public static final int Trans2Open        = 0x00;
    public static final int Trans2FindFirst   = 0x01;
    public static final int Trans2FindNext    = 0x02;
    public static final int Trans2QueryFileSys= 0x03;
    public static final int Trans2QueryPath   = 0x05;
    public static final int Trans2SetPath     = 0x06;
    public static final int Trans2QueryFile   = 0x07;
    public static final int Trans2SetFile     = 0x08;
    public static final int Trans2CreateDir   = 0x0D;
    public static final int Trans2SessSetup   = 0x0E;

    //  Remote admin protocol (RAP) codes

    public static final int RAPShareEnum      = 0;
    public static final int RAPShareGetInfo   = 1;
    public static final int RAPSessionEnum		= 6;
    public static final int RAPServerGetInfo  = 13;
    public static final int NetServerDiskEnum	= 15;
    public static final int NetGroupEnum			= 47;
    public static final int RAPUserGetInfo    = 56;
    public static final int RAPWkstaGetInfo   = 63;
    public static final int RAPServerEnum     = 94;
    public static final int RAPServerEnum2    = 104;
    public static final int RAPWkstaUserLogon = 132;
    public static final int RAPWkstaUserLogoff= 133;
    public static final int RAPChangePassword = 214;

    //	Service information/control codes

    public static final int NetServiceEnum		= 39;
    public static final int NetServiceInstall	= 40;
    public static final int NetServiceControl	= 41;

    //	User/group information codes

    public static final int NetGroupGetUsers	= 52;
    public static final int NetUserEnum				= 53;
    public static final int NetUserGetGroups  = 59;

    //  Printer/print queue admin codes

    public static final int NetPrintQEnum     = 69;
    public static final int NetPrintQGetInfo  = 70;
    public static final int NetPrintQSetInfo	= 71;
    public static final int NetPrintQAdd			= 72;
    public static final int NetPrintQDel			= 73;
    public static final int NetPrintQPause		= 74;
    public static final int NetPrintQContinue	= 75;
    public static final int NetPrintJobEnum   = 76;
    public static final int NetPrintJobGetInfo= 77;
    public static final int NetPrintJobSetInfo= 78;
    public static final int NetPrintJobDelete = 81;
    public static final int NetPrintJobPause  = 82;
    public static final int NetPrintJobContinue = 83;
    public static final int NetPrintDestEnum	= 84;
    public static final int NetPrintDestGetInfo = 85;
    public static final int NetPrintDestControl = 86;

    //  Transaction named pipe sub-commands

    public static final int CallNamedPipe       = 0x54;
    public static final int WaitNamedPipe       = 0x53;
    public static final int PeekNmPipe          = 0x23;
    public static final int QNmPHandState       = 0x21;
    public static final int SetNmPHandState     = 0x01;
    public static final int QNmPipeInfo         = 0x22;
    public static final int TransactNmPipe      = 0x26;
    public static final int RawReadNmPipe       = 0x11;
    public static final int RawWriteNmPipe      = 0x31;

    //	Miscellaneous codes

    public static final int NetBIOSEnum					= 92;
  
    //	NT transaction function codes
  
    public static final int NTTransCreate							= 1;
    public static final int NTTransIOCtl							= 2;
    public static final int NTTransSetSecurityDesc		= 3;
    public static final int NTTransNotifyChange				= 4;
    public static final int NTTransRename							= 5;
    public static final int NTTransQuerySecurityDesc	= 6;
	public static final int NTTransGetUserQuota				= 7;
	public static final int NTTransSetUserQuota				= 8;
  
    //	Flag to indicate no chained AndX command
  
    public static final int NoChainedCommand					= 0xFF;

    //	SMB command names (block 1)
  
    private static String[] _cmdNames1 = { "CreateDirectory",
					  														 "DeleteDirectory",
					  														 "OpenFile",
					  														 "CreateFile",
					  														 "CloseFile",
					  														 "FlushFile",
					  														 "DeleteFile",
					  														 "RenameFile",
					  														 "GetFileAttributes",
					  														 "SetFileAttributes",
					  														 "ReadFile",
					  														 "WriteFile",
					  														 "LockFile",
					  														 "UnLockFile",
					  														 "CreateTemporary",
					  														 "CreateNew",
					  														 "CheckDirectory",
					  														 "ProcessExit",
					  														 "SeekFile",
					  														 "LockAndRead",
					  														 "WriteAndUnlock",
					  														 null,
					  														 null,
					  														 null,
					  														 null,
					  														 null,
					  														 "ReadRaw",
					  														 "ReadMpx",
					  														 "ReadMpxSecondary",
					  														 "WriteRaw",
					  														 "WriteMpx",
					  														 null,
					  														 "WriteComplete",
					  														 null,
					  														 "SetInformation2",
					  														 "QueryInformation2",
					  														 "LockingAndX",
					  														 "Transaction",
					  														 "TransactionSecond",
					  														 "IOCtl",
					  														 "IOCtlSecondary",
					  														 "Copy",
					  														 "Move",
					  														 "Echo",
					  														 "WriteAndClose",
					  														 "OpenAndX",
					  														 "ReadAndX",
					  														 "WriteAndX",
					  														 null,
					  														 "CloseAndTreeDisconnect",
					  														 "Transaction2",
					  														 "Transaction2Secondary",
					  														 "FindClose2",
					  														 "FindNotifyClose"
    };

    private static String[] _cmdNames2 = {	"TreeConnect",
      																		"TreeDisconnect",
      																		"Negotiate",
      																		"SessionSetupAndX",
      																		"LogoffAndX",
      																		"TreeConnectAndX"
    };
  
    private static String[] _cmdNames3 = { "DiskInformation",
      																	 "Search",
      																	 "Find",
      																	 "FindUnique"
    };

    private static String[] _cmdNames4 = { "NTTransact",
      																	 "NTTransactSecondary",
      																	 "NTCreateAndX",
      																	 null,
      																	 "NTCancel"
    };

    private static String[] _cmdNames5 = { "OpenPrintFile",
      																	 "WritePrintFile",
      																	 "ClosePrintFile",
      																	 "GetPrintQueue"
    };

    private static String[] _cmdNames6 = { "SendMessage",
      																	 "SendBroadcast",
      																	 "SendForward",
      																	 "CancelForward",
      																	 "GetMachineName",
      																	 "SendMultiStart",
      																	 "SendMultiEnd",
      																	 "SendMultiText"
  };

  //  Transaction2 operation code names

    private static String[] _transNames = { "Trans2Open",
      																		"Trans2FindFirst",
      																		"Trans2FindNext",
      																		"Trans2QueryFileSys",
      																		"Trans2QueryPath",
      																		"Trans2SetPath",
      																		"Trans2QueryFile",
      																		"Trans2SetFile",
      																		"Trans2CreateDirectory",
      																		"Trans2SessionSetup"
  };

  //	NT transaction operation code names
  
    private static String[] _ntTranNames = { "",		//	zero not used
      																		 "NTTransCreate",
      																		 "NTTransIOCtl",
      																		 "NTTransSetSecurityDesc",
      																		 "NTTransNotifyChange",
      																		 "NTTransRename",
      																		 "NTTransQuerySecurityDesc",
      																		 "NTTransGetUserQuota",
      																		 "NTTransSetUserQuota"
  };
  
  /**
     * Return an SMB command as a string
     * 
     * @param cmd int
     * @return String
     */
    public static final String getCommandName(int cmd)
    {

        // Get the command name

        String cmdName = "";

        if (cmd >= 0 && cmd < _cmdNames1.length)
        {

            // Get the command name from the main name table

            cmdName = _cmdNames1[cmd];
        }
        else
        {

            // Mask the command to determine the command table to index

            int cmdTop = cmd & 0x00F0;

            switch (cmd & 0x00F0)
            {
            case 0x70:
                cmdName = _cmdNames2[cmd - 0x70];
                break;
            case 0x80:
                cmdName = _cmdNames3[cmd - 0x80];
                break;
            case 0xA0:
                cmdName = _cmdNames4[cmd - 0xA0];
                break;
            case 0xC0:
                cmdName = _cmdNames5[cmd - 0xC0];
                break;
            case 0xD0:
                cmdName = _cmdNames6[cmd - 0xD0];
                break;
            default:
                cmdName = "0x" + Integer.toHexString(cmd);
                break;
            }
        }

        // Return the command name string

        return cmdName;
    }

    /**
     * Return a transaction code as a string
     * 
     * @param opcode int
     * @return String
     */
    public final static String getTransactionName(int opcode)
    {

        // Range check the opcode

        String opcodeName = "";

        if (opcode >= 0 && opcode < _transNames.length)
            opcodeName = _transNames[opcode];
        return opcodeName;
    }

    /**
     * Return an NT transation code as a string
     * 
     * @param opcode int
     * @return String
     */
    public final static String getNTTransationName(int opcode)
    {

        //	Range check the opcode

        String opcodeName = "";

        if (opcode >= 0 && opcode < _ntTranNames.length)
            opcodeName = _ntTranNames[opcode];
        return opcodeName;
    }
}