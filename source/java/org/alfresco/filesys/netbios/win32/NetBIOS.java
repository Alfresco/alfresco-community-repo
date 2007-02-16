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
package org.alfresco.filesys.netbios.win32;

/**
 * NetBIOS API Constants Class
 */
public class NetBIOS
{
    // NetBIOS command codes

    public final static int NCBCall = 0x10;
    public final static int NCBListen = 0x11;
    public final static int NCBHangup = 0x12;
    public final static int NCBSend = 0x14;
    public final static int NCBRecv = 0x15;
    public final static int NCBRecvAny = 0x16;
    public final static int NCBChainSend = 0x17;
    public final static int NCBDGSend = 0x20;
    public final static int NCBDGRecv = 0x21;
    public final static int NCBDGSendBc = 0x22;
    public final static int NCBDGRecvBc = 0x23;
    public final static int NCBAddName = 0x30;
    public final static int NCBDelName = 0x31;
    public final static int NCBReset = 0x32;
    public final static int NCBAStat = 0x33;
    public final static int NCBSStat = 0x34;
    public final static int NCBCancel = 0x35;
    public final static int NCBAddGrName = 0x36;
    public final static int NCBEnum = 0x37;
    public final static int NCBUnlink = 0x70;
    public final static int NCBSendNA = 0x71;
    public final static int NCBChainSendNA = 0x72;
    public final static int NCBLANStAlert = 0x73;
    public final static int NCBAction = 0x77;
    public final static int NCBFindName = 0x78;
    public final static int NCBTrace = 0x79;

    public final static int Asynch = 0x80;

    // Status codes

    public final static int NRC_GoodRet = 0x00;
    public final static int NRC_BufLen = 0x01;
    public final static int NRC_IllCmd = 0x03;
    public final static int NRC_CmdTmo = 0x05;
    public final static int NRC_Incomp = 0x06;
    public final static int NRC_Baddr = 0x07;
    public final static int NRC_SNumOut = 0x08;
    public final static int NRC_NoRes = 0x09;
    public final static int NRC_SClosed = 0x0A;
    public final static int NRC_CmdCan = 0x0B;
    public final static int NRC_DupName = 0x0D;
    public final static int NRC_NamTFul = 0x0E;
    public final static int NRC_ActSes = 0x0F;
    public final static int NRC_LocTFul = 0x11;
    public final static int NRC_RemTFul = 0x12;
    public final static int NRC_IllNN = 0x13;
    public final static int NRC_NoCall = 0x14;
    public final static int NRC_NoWild = 0x15;
    public final static int NRC_InUse = 0x16;
    public final static int NRC_NamErr = 0x17;
    public final static int NRC_SAbort = 0x18;
    public final static int NRC_NamConf = 0x19;
    public final static int NRC_IfBusy = 0x21;
    public final static int NRC_TooMany = 0x22;
    public final static int NRC_Bridge = 0x23;
    public final static int NRC_CanOccr = 0x24;
    public final static int NRC_Cancel = 0x26;
    public final static int NRC_DupEnv = 0x30;
    public final static int NRC_EnvNotDef = 0x34;
    public final static int NRC_OSResNotAv = 0x35;
    public final static int NRC_MaxApps = 0x36;
    public final static int NRC_NoSaps = 0x37;
    public final static int NRC_NoResources = 0x38;
    public final static int NRC_InvAddress = 0x39;
    public final static int NRC_InvDDid = 0x3B;
    public final static int NRC_LockFail = 0x3C;
    public final static int NRC_OpenErr = 0x3F;
    public final static int NRC_System = 0x40;
    public final static int NRC_Pending = 0xFF;

    // Various constants

    public final static int NCBNameSize = 16;
    public final static int MaxLANA = 254;

    public final static int NameFlagsMask = 0x87;

    public final static int GroupName = 0x80;
    public final static int UniqueName = 0x00;
    public final static int Registering = 0x00;
    public final static int Registered = 0x04;
    public final static int Deregistered = 0x05;
    public final static int Duplicate = 0x06;
    public final static int DuplicateDereg = 0x07;
    public final static int ListenOutstanding = 0x01;
    public final static int CallPending = 0x02;
    public final static int SessionEstablished = 0x03;
    public final static int HangupPending = 0x04;
    public final static int HangupComplete = 0x05;
    public final static int SessionAborted = 0x06;

    public final static String AllTransports = "M\0\0\0";

    // Maximum receive size (16bits)
    //
    // Multiple receives must be issued to receive data packets over this size

    public final static int MaxReceiveSize = 0xFFFF;

    /**
     * Return the status string for a NetBIOS error code
     * 
     * @param nbError int
     * @return String
     */
    public final static String getErrorString(int nbError)
    {

        String str = "";

        switch (nbError)
        {
        case NRC_GoodRet:
            str = "Success status";
            break;
        case NRC_BufLen:
            str = "Illegal buffer length";
            break;
        case NRC_IllCmd:
            str = "Illegal command";
            break;
        case NRC_CmdTmo:
            str = "Command timed out";
            break;
        case NRC_Incomp:
            str = "Message incomplete, issue another command";
            break;
        case NRC_Baddr:
            str = "Illegal buffer address";
            break;
        case NRC_SNumOut:
            str = "Session number out of range";
            break;
        case NRC_NoRes:
            str = "No resource available";
            break;
        case NRC_SClosed:
            str = "Session closed";
            break;
        case NRC_CmdCan:
            str = "Command cancelled";
            break;
        case NRC_DupName:
            str = "Duplicate name";
            break;
        case NRC_NamTFul:
            str = "Name table full";
            break;
        case NRC_ActSes:
            str = "No deletions, name has active sessions";
            break;
        case NRC_LocTFul:
            str = "Local session table full";
            break;
        case NRC_RemTFul:
            str = "Remote session table full";
            break;
        case NRC_IllNN:
            str = "Illegal name number";
            break;
        case NRC_NoCall:
            str = "No callname";
            break;
        case NRC_NoWild:
            str = "Cannot put * in ncb_name";
            break;
        case NRC_InUse:
            str = "Name in use on remote adapter";
            break;
        case NRC_NamErr:
            str = "Name deleted";
            break;
        case NRC_SAbort:
            str = "Session ended abnormally";
            break;
        case NRC_NamConf:
            str = "Name conflict detected";
            break;
        case NRC_IfBusy:
            str = "Interface busy, IRET before retrying";
            break;
        case NRC_TooMany:
            str = "Too many commands outstanding, try later";
            break;
        case NRC_Bridge:
            str = "ncb_lana_num field invalid";
            break;
        case NRC_CanOccr:
            str = "Command completed whilst cancel occurring";
            break;
        case NRC_Cancel:
            str = "Command not valid to cancel";
            break;
        case NRC_DupEnv:
            str = "Name defined by another local process";
            break;
        case NRC_EnvNotDef:
            str = "Environment undefined, RESET required";
            break;
        case NRC_OSResNotAv:
            str = "Require OS resources exhausted";
            break;
        case NRC_MaxApps:
            str = "Max number of applications exceeded";
            break;
        case NRC_NoSaps:
            str = "No saps available for NetBIOS";
            break;
        case NRC_NoResources:
            str = "Requested resources not available";
            break;
        case NRC_InvAddress:
            str = "Invalid ncb address or length";
            break;
        case NRC_InvDDid:
            str = "Ivalid NCB DDID";
            break;
        case NRC_LockFail:
            str = "Lock of user area failed";
            break;
        case NRC_OpenErr:
            str = "NetBIOS not loaded";
            break;
        case NRC_System:
            str = "System error";
            break;
        case NRC_Pending:
            str = "Asyncrhonous command pending";
            break;
        }

        return str;
    }
}
