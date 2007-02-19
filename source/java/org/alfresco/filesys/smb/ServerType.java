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
package org.alfresco.filesys.smb;

import org.alfresco.filesys.util.*;

/**
 * Server Type Flags Class
 */
public class ServerType
{

    // Server type flags

    public static final int WorkStation     = 0x00000001;
    public static final int Server          = 0x00000002;
    public static final int SQLServer       = 0x00000004;
    public static final int DomainCtrl      = 0x00000008;
    public static final int DomainBakCtrl   = 0x00000010;
    public static final int TimeSource      = 0x00000020;
    public static final int AFPServer       = 0x00000040;
    public static final int NovellServer    = 0x00000080;
    public static final int DomainMember    = 0x00000100;
    public static final int PrintServer     = 0x00000200;
    public static final int DialinServer    = 0x00000400;
    public static final int UnixServer      = 0x00000800;
    public static final int NTServer        = 0x00001000;
    public static final int WfwServer       = 0x00002000;
    public static final int MFPNServer      = 0x00004000;
    public static final int NTNonDCServer   = 0x00008000;
    public static final int PotentialBrowse = 0x00010000;
    public static final int BackupBrowser   = 0x00020000;
    public static final int MasterBrowser   = 0x00040000;
    public static final int DomainMaster    = 0x00080000;
    public static final int OSFServer       = 0x00100000;
    public static final int VMSServer       = 0x00200000;
    public static final int Win95Plus       = 0x00400000;
    public static final int DFSRoot         = 0x00800000;
    public static final int NTCluster       = 0x01000000;
    public static final int TerminalServer  = 0x02000000;
    public static final int DCEServer       = 0x10000000;
    public static final int AlternateXport  = 0x20000000;
    public static final int LocalListOnly   = 0x40000000;

    public static final int DomainEnum      = 0x80000000;

    // Server type strings

    private static final String[] _srvType = {
            "Workstation",
            "Server",
            "SQLServer",
            "DomainController",
            "BackupDomainController",
            "TimeSource",
            "AFPServer",
            "NovellServer",
            "DomainMember",
            "PrintServer",
            "DialinServer",
            "UnixServer",
            "NTServer",
            "WfwServer",
            "MFPNServer",
            "NtNonDCServer",
            "PotentialBrowse",
            "BackupBrowser",
            "MasterBrowser",
            "DomainMaster",
            "OSFServer",
            "VMSServer",
            "Win95Plus",
            "DFSRoot",
            "NTCluster",
            "TerminalServer",
            "",
            "",
            "DCEServer" };

    /**
     * Convert server type flags to a list of server type strings
     * 
     * @param typ int
     * @return StringList
     */
    public static final StringList TypeAsStrings(int typ)
    {
        // Allocate the vector for the strings

        StringList strs = new StringList();

        // Test each type bit and add the appropriate type string

        for (int i = 0; i < _srvType.length; i++)
        {
            // Check the current type flag

            int mask = 1 << i;
            if ((typ & mask) != 0)
                strs.addString(_srvType[i]);
        }

        // Return the list of type strings

        return strs;
    }

    /**
     * Check if the workstation flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isWorkStation(int typ)
    {
        return (typ & WorkStation) != 0 ? true : false;
    }

    /**
     * Check if the server flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isServer(int typ)
    {
        return (typ & Server) != 0 ? true : false;
    }

    /**
     * Check if the SQL server flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isSQLServer(int typ)
    {
        return (typ & SQLServer) != 0 ? true : false;
    }

    /**
     * Check if the domain controller flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isDomainController(int typ)
    {
        return (typ & DomainCtrl) != 0 ? true : false;
    }

    /**
     * Check if the backup domain controller flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isBackupDomainController(int typ)
    {
        return (typ & DomainBakCtrl) != 0 ? true : false;
    }

    /**
     * Check if the time source flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isTimeSource(int typ)
    {
        return (typ & TimeSource) != 0 ? true : false;
    }

    /**
     * Check if the AFP server flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isAFPServer(int typ)
    {
        return (typ & AFPServer) != 0 ? true : false;
    }

    /**
     * Check if the Novell server flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isNovellServer(int typ)
    {
        return (typ & NovellServer) != 0 ? true : false;
    }

    /**
     * Check if the domain member flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isDomainMember(int typ)
    {
        return (typ & DomainMember) != 0 ? true : false;
    }

    /**
     * Check if the print server flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isPrintServer(int typ)
    {
        return (typ & PrintServer) != 0 ? true : false;
    }

    /**
     * Check if the dialin server flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isDialinServer(int typ)
    {
        return (typ & DialinServer) != 0 ? true : false;
    }

    /**
     * Check if the Unix server flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isUnixServer(int typ)
    {
        return (typ & UnixServer) != 0 ? true : false;
    }

    /**
     * Check if the NT server flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isNTServer(int typ)
    {
        return (typ & NTServer) != 0 ? true : false;
    }

    /**
     * Check if the WFW server flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isWFWServer(int typ)
    {
        return (typ & WfwServer) != 0 ? true : false;
    }

    /**
     * Check if the MFPN server flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isMFPNServer(int typ)
    {
        return (typ & MFPNServer) != 0 ? true : false;
    }

    /**
     * Check if the NT non-domain controller server flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isNTNonDomainServer(int typ)
    {
        return (typ & NTNonDCServer) != 0 ? true : false;
    }

    /**
     * Check if the potential browse master flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isPotentialBrowseMaster(int typ)
    {
        return (typ & PotentialBrowse) != 0 ? true : false;
    }

    /**
     * Check if the backup browser flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isBackupBrowser(int typ)
    {
        return (typ & BackupBrowser) != 0 ? true : false;
    }

    /**
     * Check if the browse master flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isBrowserMaster(int typ)
    {
        return (typ & MasterBrowser) != 0 ? true : false;
    }

    /**
     * Check if the domain master flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isDomainMaster(int typ)
    {
        return (typ & DomainMaster) != 0 ? true : false;
    }

    /**
     * Check if the OSF server flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isOSFServer(int typ)
    {
        return (typ & OSFServer) != 0 ? true : false;
    }

    /**
     * Check if the VMS server flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isVMSServer(int typ)
    {
        return (typ & VMSServer) != 0 ? true : false;
    }

    /**
     * Check if the Win95 plus flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isWin95Plus(int typ)
    {
        return (typ & Win95Plus) != 0 ? true : false;
    }

    /**
     * Check if the DFS root flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isDFSRoot(int typ)
    {
        return (typ & DFSRoot) != 0 ? true : false;
    }

    /**
     * Check if the NT cluster flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isNTCluster(int typ)
    {
        return (typ & NTCluster) != 0 ? true : false;
    }

    /**
     * Check if the terminal server flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isTerminalServer(int typ)
    {
        return (typ & TerminalServer) != 0 ? true : false;
    }

    /**
     * Check if the DCE server flag is set
     * 
     * @param typ int
     * @return boolean
     */
    public static final boolean isDCEServer(int typ)
    {
        return (typ & DCEServer) != 0 ? true : false;
    }
}
