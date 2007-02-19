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
package org.alfresco.filesys.smb.dcerpc;

/**
 * <p>
 * Defines the special DCE/RPC pipe names.
 */
public class DCEPipeType
{

    //  IPC$ client pipe names
    
    private static final String[] _pipeNames = { "\\PIPE\\srvsvc",
                                                                                 "\\PIPE\\samr",
                                                                                 "\\PIPE\\winreg",
                                                                                 "\\PIPE\\wkssvc",
                                                                                 "\\PIPE\\NETLOGON",
                                                                                 "\\PIPE\\lsarpc",
                                                                                 "\\PIPE\\spoolss",
                                                                                 "\\PIPE\\netdfs",
                                                                                 "\\PIPE\\svcctl",
                                                                                 "\\PIPE\\EVENTLOG",
                                                                                 "\\PIPE\\NETLOGON"
    };

    //  IPC$ server pipe names
    
    private static final String[] _srvNames = { "\\PIPE\\ntsvcs",
                                                                                        "\\PIPE\\lsass",
                                                                                        "\\PIPE\\winreg",
                                                                                        "\\PIPE\\ntsvcs",
                                                                                        "\\PIPE\\lsass",
                                                                                        "\\PIPE\\lsass",
                                                                                        "\\PIPE\\spoolss",
                                                                                        "\\PIPE\\netdfs",
                                                                                        "\\PIPE\\svcctl",
                                                                                        "\\PIPE\\EVENTLOG"
    };
    
    //  IPC$ pipe ids
    
    public static final int PIPE_SRVSVC     = 0;
    public static final int PIPE_SAMR           = 1;
    public static final int PIPE_WINREG     = 2;
    public static final int PIPE_WKSSVC     = 3;
    public static final int PIPE_NETLOGON = 4;
    public static final int PIPE_LSARPC     = 5;
    public static final int PIPE_SPOOLSS    = 6;
    public static final int PIPE_NETDFS     = 7;
    public static final int PIPE_SVCCTL     = 8;
    public static final int PIPE_EVENTLOG   = 9;
    public static final int PIPE_NETLOGON1= 10;
    
    //  IPC$ pipe UUIDs
    
    private static UUID _uuidNetLogon = new UUID("8a885d04-1ceb-11c9-9fe8-08002b104860", 2);
    private static UUID _uuidWinReg   = new UUID("338cd001-2244-31f1-aaaa-900038001003", 1);
    private static UUID _uuidSvcCtl   = new UUID("367abb81-9844-35f1-ad32-98f038001003", 2);
    private static UUID _uuidLsaRpc   = new UUID("12345678-1234-abcd-ef00-0123456789ab", 0);
    private static UUID _uuidSrvSvc   = new UUID("4b324fc8-1670-01d3-1278-5a47bf6ee188", 3);
    private static UUID _uuidWksSvc   = new UUID("6bffd098-a112-3610-9833-46c3f87e345a", 1);
    private static UUID _uuidSamr     = new UUID("12345778-1234-abcd-ef00-0123456789ac", 1);
    private static UUID _uuidSpoolss  = new UUID("12345778-1234-abcd-ef00-0123456789ab", 1);
    private static UUID _uuidSvcctl     = new UUID("367abb81-9844-35f1-ad32-98f038001003", 2);
    private static UUID _uuidEventLog   = new UUID("82273FDC-E32A-18C3-3F78-827929DC23EA", 0);
    private static UUID _uuidNetLogon1= new UUID("12345678-1234-abcd-ef00-01234567cffb", 1);

//  private static UUID _uuidAtSvc    = new UUID("1ff70682-0a51-30e8-076d-740be8cee98b", 1);

    /**
     * Convert a pipe name to a type
     * 
     * @param name String
     * @return int
     */
    public final static int getNameAsType(String name)
    {
        for (int i = 0; i < _pipeNames.length; i++)
        {
            if (_pipeNames[i].equals(name))
                return i;
        }
        return -1;
    }

    /**
     * Convert a pipe type to a name
     * 
     * @param typ int
     * @return String
     */
    public final static String getTypeAsString(int typ)
    {
        if (typ >= 0 && typ < _pipeNames.length)
            return _pipeNames[typ];
        return null;
    }

    /**
     * Convert a pipe type to a short name
     * 
     * @param typ int
     * @return String
     */
    public final static String getTypeAsStringShort(int typ)
    {
        if (typ >= 0 && typ < _pipeNames.length)
        {
            String name = _pipeNames[typ];
            return name.substring(5);
        }
        return null;
    }

    /**
     * Return the UUID for the pipe type
     * 
     * @param typ int
     * @return UUID
     */
    public final static UUID getUUIDForType(int typ)
    {
        UUID ret = null;

        switch (typ)
        {
        case PIPE_NETLOGON:
            ret = _uuidNetLogon;
            break;
        case PIPE_NETLOGON1:
            ret = _uuidNetLogon1;
            break;
        case PIPE_WINREG:
            ret = _uuidWinReg;
            break;
        case PIPE_LSARPC:
            ret = _uuidLsaRpc;
            break;
        case PIPE_WKSSVC:
            ret = _uuidWksSvc;
            break;
        case PIPE_SAMR:
            ret = _uuidSamr;
            break;
        case PIPE_SRVSVC:
            ret = _uuidSrvSvc;
            break;
        case PIPE_SPOOLSS:
            ret = _uuidSpoolss;
            break;
        case PIPE_SVCCTL:
            ret = _uuidSvcCtl;
            break;
        case PIPE_EVENTLOG:
            ret = _uuidEventLog;
            break;
        }
        return ret;
    }

    /**
     * Get the server-side pipe name for the specified pipe
     * 
     * @param typ int
     * @return String
     */
    public final static String getServerPipeName(int typ)
    {
        if (typ >= 0 && typ < _srvNames.length)
            return _srvNames[typ];
        return null;
    }
}
