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

/**
 * SMB error text class.
 * <p>
 * The SMBErrorText is a static class that converts SMB error class/error codes into their
 * appropriate error message strings. The class is used by the SMBException class when outputting an
 * SMB exception as a string.
 * <p>
 * SMB error classes and error codes are declared in the SMBStatus class.
 */

public final class SMBErrorText
{

    /**
     * Return the error string associated with the SMB error class/code
     * 
     * @param errclass Error class.
     * @param errcode Error code.
     * @return Error string.
     */
    public final static String ErrorString(int errclass, int errcode)
    {

        // Determine the error class

        String errtext = null;

        switch (errclass)
        {

        // Success class

        case SMBStatus.Success:
            errtext = "The request was successful";
            break;

        // DOS error class

        case SMBStatus.ErrDos:
            errtext = DOSErrorText(errcode);
            break;

        // Server error class

        case SMBStatus.ErrSrv:
            errtext = ServerErrorText(errcode);
            break;

        // Hardware error class

        case SMBStatus.ErrHrd:
            errtext = HardwareErrorText(errcode);
            break;

        // Network error codes, returned by transaction requests

        case SMBStatus.NetErr:
            errtext = NetworkErrorText(errcode);
            break;

        // JLAN error codes

        case SMBStatus.JLANErr:
            errtext = JLANErrorText(errcode);
            break;

        // NT 32-bit error codes

        case SMBStatus.NTErr:
            errtext = NTErrorText(errcode);
            break;

        // Win32 error codes

        case SMBStatus.Win32Err:
            errtext = Win32ErrorText(errcode);
            break;

        // DCE/RPC error

        case SMBStatus.DCERPCErr:
            errtext = DCERPCErrorText(errcode);
            break;

        // Bad SMB command

        case SMBStatus.ErrCmd:
            errtext = "Command was not in the SMB format";
            break;
        }

        if (errtext == null)
            errtext = "[Unknown error status/class: " + errclass + "," + errcode + "]";

        // Return the error text

        return errtext;
    }

    /**
     * Return a DOS error string.
     * 
     * @param errcode DOS error code.
     * @return DOS error string.
     */

    private static String DOSErrorText(int errcode)
    {

        // Convert the DOS error code to a text string

        String errtext = null;

        switch (errcode)
        {
        case 1:
            errtext = "Invalid function. Server did not recognize/perform system call";
            break;
        case 2:
            errtext = "File not found";
            break;
        case 3:
            errtext = "Directory invalid";
            break;
        case 4:
            errtext = "Too many open files";
            break;
        case 5:
            errtext = "Access denied";
            break;
        case 6:
            errtext = "Invalid file handle";
            break;
        case 7:
            errtext = "Memory control blocks destroyed";
            break;
        case 8:
            errtext = "Insufficient server memory to perform function";
            break;
        case 9:
            errtext = "Invalid memory block address";
            break;
        case 10:
            errtext = "Invalid environment";
            break;
        case 11:
            errtext = "Invalid format";
            break;
        case 12:
            errtext = "Invalid open mode";
            break;
        case 13:
            errtext = "Invalid data, in server IOCTL call";
            break;
        case 15:
            errtext = "Invalid drive specified";
            break;
        case 16:
            errtext = "Delete directory attempted to delete servers directory";
            break;
        case 17:
            errtext = "Not same device";
            break;
        case 18:
            errtext = "No more files";
            break;
        case 32:
            errtext = "File sharing mode conflict";
            break;
        case 33:
            errtext = "Lock request conflicts with existing lock";
            break;
        case 66:
            errtext = "IPC not supported";
            break;
        case 80:
            errtext = "File already exists";
            break;
        case 110:
            errtext = "Cannot open the file specified";
            break;
        case 124:
            errtext = "Unknown information level";
            break;
        case SMBStatus.DOSDirectoryNotEmpty:
            errtext = "Directory not empty";
            break;
        case 230:
            errtext = "Named pipe invalid";
            break;
        case 231:
            errtext = "All instances of pipe are busy";
            break;
        case 232:
            errtext = "Named pipe close in progress";
            break;
        case 233:
            errtext = "No process on other end of named pipe";
            break;
        case 234:
            errtext = "More data to be returned";
            break;
        case 267:
            errtext = "Invalid directory name in path";
            break;
        case 275:
            errtext = "Extended attributes did not fit";
            break;
        case 282:
            errtext = "Extended attributes not supported";
            break;
        case 2142:
            errtext = "Unknown IPC";
            break;
        }

        // Return the error string

        return errtext;
    }

    /**
     * Return a hardware error string.
     * 
     * @param errcode Hardware error code.
     * @return Hardware error string.
     */
    private final static String HardwareErrorText(int errcode)
    {

        // Convert the hardware error code to a text string

        String errtext = null;

        switch (errcode)
        {
        case 19:
            errtext = "Attempt to write on write protected media";
            break;
        case 20:
            errtext = "Unknown unit";
            break;
        case 21:
            errtext = "Drive not ready";
            break;
        case 22:
            errtext = "Unknown command";
            break;
        case 23:
            errtext = "Data error (CRC)";
            break;
        case 24:
            errtext = "Bad request structure length";
            break;
        case 25:
            errtext = "Seek error";
            break;
        case 26:
            errtext = "Unknown media type";
            break;
        case 27:
            errtext = "Sector not found";
            break;
        case 28:
            errtext = "Printer out of paper";
            break;
        case 29:
            errtext = "Write fault";
            break;
        case 30:
            errtext = "Read fault";
            break;
        case 31:
            errtext = "General failure";
            break;
        case 32:
            errtext = "Open conflicts with existing open";
            break;
        case 33:
            errtext = "Lock request conflicted with existing lock";
            break;
        case 34:
            errtext = "Wrong disk was found in a drive";
            break;
        case 35:
            errtext = "No FCBs are available to process request";
            break;
        case 36:
            errtext = "A sharing buffer has been exceeded";
            break;
        }

        // Return the error string

        return errtext;
    }

    /**
     * Return a JLAN error string.
     * 
     * @return java.lang.String
     * @param errcode int
     */
    private static String JLANErrorText(int errcode)
    {

        // Convert the JLAN error code to a text string

        String errtext = null;

        switch (errcode)
        {
        case SMBStatus.JLANUnsupportedDevice:
            errtext = "Invalid device type for dialect";
            break;
        case SMBStatus.JLANNoMoreSessions:
            errtext = "No more sessions available";
            break;
        case SMBStatus.JLANSessionNotActive:
            errtext = "Session is not active";
            break;
        case SMBStatus.JLANInvalidSMBReceived:
            errtext = "Invalid SMB response received";
            break;
        case SMBStatus.JLANLargeFilesNotSupported:
            errtext = "Large files not supported";
            break;
        case SMBStatus.JLANInvalidFileInfo:
            errtext = "Invalid file information for level";
            break;
        case SMBStatus.JLANDceRpcNotSupported:
            errtext = "Server does not support DCE/RPC requests";
            break;
        }
        return errtext;
    }

    /**
     * Return a network error string.
     * 
     * @param errcode Network error code.
     * @return Network error string.
     */
    private final static String NetworkErrorText(int errcode)
    {

        // Convert the network error code to a text string

        String errtext = null;

        switch (errcode)
        {
        case SMBStatus.NETAccessDenied:
            errtext = "Access denied";
            break;
        case SMBStatus.NETInvalidHandle:
            errtext = "Invalid handle";
            break;
        case SMBStatus.NETUnsupported:
            errtext = "Function not supported";
            break;
        case SMBStatus.NETBadDeviceType:
            errtext = "Bad device type";
            break;
        case SMBStatus.NETBadNetworkName:
            errtext = "Bad network name";
            break;
        case SMBStatus.NETAlreadyAssigned:
            errtext = "Already assigned";
            break;
        case SMBStatus.NETInvalidPassword:
            errtext = "Invalid password";
            break;
        case SMBStatus.NETInvParameter:
            errtext = "Incorrect parameter";
            break;
        case SMBStatus.NETContinued:
            errtext = "Transaction continued ...";
            break;
        case SMBStatus.NETNoMoreItems:
            errtext = "No more items";
            break;
        case SMBStatus.NETInvalidAddress:
            errtext = "Invalid address";
            break;
        case SMBStatus.NETServiceDoesNotExist:
            errtext = "Service does not exist";
            break;
        case SMBStatus.NETBadDevice:
            errtext = "Bad device";
            break;
        case SMBStatus.NETNoNetOrBadPath:
            errtext = "No network or bad path";
            break;
        case SMBStatus.NETExtendedError:
            errtext = "Extended error";
            break;
        case SMBStatus.NETNoNetwork:
            errtext = "No network";
            break;
        case SMBStatus.NETCancelled:
            errtext = "Cancelled";
            break;
        case SMBStatus.NETSrvNotRunning:
            errtext = "Server service is not running";
            break;
        case SMBStatus.NETBufferTooSmall:
            errtext = "Supplied buffer is too small";
            break;
        case SMBStatus.NETNoTransactions:
            errtext = "Server is not configured for transactions";
            break;
        case SMBStatus.NETInvQueueName:
            errtext = "Invalid queue name";
            break;
        case SMBStatus.NETNoSuchPrintJob:
            errtext = "Specified print job could not be located";
            break;
        case SMBStatus.NETNotResponding:
            errtext = "Print process is not responding";
            break;
        case SMBStatus.NETSpoolerNotStarted:
            errtext = "Spooler is not started on the remote server";
            break;
        case SMBStatus.NETCannotPerformOp:
            errtext = "Operation cannot be performed on the print job in it's current state";
            break;
        case SMBStatus.NETErrLoadLogonScript:
            errtext = "Error occurred running/loading logon script";
            break;
        case SMBStatus.NETLogonNotValidated:
            errtext = "Logon was not validated by any server";
            break;
        case SMBStatus.NETLogonSrvOldSoftware:
            errtext = "Logon server is running old software version, cannot validate logon";
            break;
        case SMBStatus.NETUserNameNotFound:
            errtext = "User name was not found";
            break;
        case SMBStatus.NETUserLgnWkNotAllowed:
            errtext = "User is not allowed to logon from this computer";
            break;
        case SMBStatus.NETUserLgnTimeNotAllowed:
            errtext = "USer is not allowed to logon at this time";
            break;
        case SMBStatus.NETUserPasswordExpired:
            errtext = "User password has expired";
            break;
        case SMBStatus.NETPasswordCannotChange:
            errtext = "Password cannot be changed";
            break;
        case SMBStatus.NETPasswordTooShort:
            errtext = "Password is too short";
            break;
        }

        // Return the error string

        return errtext;
    }

    /**
     * Return a server error string.
     * 
     * @param errcode Server error code.
     * @return Server error string.
     */
    private final static String ServerErrorText(int errcode)
    {

        // Convert the server error code to a text string

        String errtext = null;
        switch (errcode)
        {
        case 1:
            errtext = "Non-specific error";
            break;
        case 2:
            errtext = "Bad password";
            break;
        case 4:
            errtext = "Client does not have access rights";
            break;
        case 5:
            errtext = "Invalid TID";
            break;
        case 6:
            errtext = "Invalid network name";
            break;
        case 7:
            errtext = "Invalid device";
            break;
        case 49:
            errtext = "Print queue full (files)";
            break;
        case 50:
            errtext = "Print queue full (space)";
            break;
        case 51:
            errtext = "EOF on print queue dump";
            break;
        case 52:
            errtext = "Invalid print file FID";
            break;
        case 64:
            errtext = "Server did not recognize the command received";
            break;
        case 65:
            errtext = "Internal server error";
            break;
        case 67:
            errtext = "FID and pathname combination invalid";
            break;
        case 69:
            errtext = "Invalid access permission";
            break;
        case 71:
            errtext = "Invalid attribute mode";
            break;
        case 81:
            errtext = "Server is paused";
            break;
        case 82:
            errtext = "Not receiving messages";
            break;
        case 83:
            errtext = "No room to buffer message";
            break;
        case 87:
            errtext = "Too many remote user names";
            break;
        case 88:
            errtext = "Operation timed out";
            break;
        case 89:
            errtext = "No resources available for request";
            break;
        case 90:
            errtext = "Too many UIDs active on session";
            break;
        case 91:
            errtext = "Invalid UID";
            break;
        case 250:
            errtext = "Unable to support RAW, use MPX";
            break;
        case 251:
            errtext = "Unable to support RAW, use standard read/write";
            break;
        case 252:
            errtext = "Continue in MPX mode";
            break;
        case 65535:
            errtext = "Function not supported";
            break;
        }

        // Return the error string

        return errtext;
    }

    /**
     * Return an NT error string.
     * 
     * @param errcode NT error code.
     * @return NT error string.
     */
    private final static String NTErrorText(int errcode)
    {

        // Convert the NT error code to a text string

        String errtext = "";

        switch (errcode)
        {
        case SMBStatus.NTSuccess:
            errtext = "The request was successful";
            break;
        case SMBStatus.NTAccessDenied:
            errtext = "Access denied";
            break;
        case SMBStatus.NTObjectNotFound:
            errtext = "Object not found";
            break;
        case SMBStatus.Win32InvalidHandle:
            errtext = "Invalid handle";
            break;
        case SMBStatus.Win32BadDeviceType:
            errtext = "Bad device type";
            break;
        case SMBStatus.Win32BadNetworkName:
            errtext = "Bad network name";
            break;
        case SMBStatus.Win32AlreadyAssigned:
            errtext = "Already assigned";
            break;
        case SMBStatus.Win32InvalidPassword:
            errtext = "Invalid password";
            break;
        case SMBStatus.NTInvalidParameter:
            errtext = "Invalid parameter";
            break;
        case SMBStatus.Win32MoreData:
            errtext = "More data available";
            break;
        case SMBStatus.Win32NoMoreItems:
            errtext = "No more items";
            break;
        case SMBStatus.Win32InvalidAddress:
            errtext = "Invalid address";
            break;
        case SMBStatus.Win32ServiceDoesNotExist:
            errtext = "Service does not exist";
            break;
        case SMBStatus.Win32BadDevice:
            errtext = "Bad device";
            break;
        case SMBStatus.Win32NoNetOrBadPath:
            errtext = "No network or bad path";
            break;
        case SMBStatus.Win32ExtendedError:
            errtext = "Extended error";
            break;
        case SMBStatus.Win32NoNetwork:
            errtext = "No network";
            break;
        case SMBStatus.NTCancelled:
            errtext = "Cancelled";
            break;
        case SMBStatus.NTBufferOverflow:
            errtext = "Buffer overflow";
            break;
        case SMBStatus.NTNoSuchFile:
            errtext = "No such file";
            break;
        case SMBStatus.NTInvalidDeviceRequest:
            errtext = "Invalid device request";
            break;
        case SMBStatus.NTMoreProcessingRequired:
            errtext = "More processing required";
            break;
        case SMBStatus.NTInvalidSecDescriptor:
            errtext = "Invalid security descriptor";
            break;
        case SMBStatus.NTNotSupported:
            errtext = "Not supported";
            break;
        case SMBStatus.NTBadDeviceType:
            errtext = "Bad device type";
            break;
        case SMBStatus.NTObjectPathNotFound:
            errtext = "Object path not found";
            break;
        case SMBStatus.NTLogonFailure:
            errtext = "Logon failure";
            break;
        case SMBStatus.NTAccountDisabled:
            errtext = "Account disabled";
            break;
        case SMBStatus.NTNoneMapped:
            errtext = "None mapped";
            break;
        case SMBStatus.NTInvalidInfoClass:
            errtext = "Invalid information class";
            break;
        case SMBStatus.NTObjectNameCollision:
            errtext = "Object name collision";
            break;
        case SMBStatus.NTNotImplemented:
            errtext = "Not implemented";
            break;
        case SMBStatus.NTFileOffline:
            errtext = "File is offline";
            break;
        case SMBStatus.NTSharingViolation:
            errtext = "Sharing violation";
            break;
        case SMBStatus.NTBadNetName:
            errtext = "Bad network name";
            break;
        case SMBStatus.NTBufferTooSmall:
            errtext = "Buffer too small";
            break;
        case SMBStatus.NTLockConflict:
            errtext = "Lock conflict";
            break;
        case SMBStatus.NTLockNotGranted:
            errtext = "Lock not granted";
            break;
        case SMBStatus.NTRangeNotLocked:
            errtext = "Range not locked";
            break;
        case SMBStatus.NTDiskFull:
            errtext = "Disk full";
            break;
        case SMBStatus.NTTooManyOpenFiles:
            errtext = "Too many open files";
            break;
        case SMBStatus.NTRequestNotAccepted:
            errtext = "Request not accepted";
            break;
        case SMBStatus.NTNoSuchDomain:
            errtext = "No such domain";
            break;
        case SMBStatus.NTNoMoreFiles:
            errtext = "No more files";
            break;
        case SMBStatus.NTObjectNameInvalid:
            errtext = "Object name invalid";
            break;
        case SMBStatus.NTPipeBusy:
            errtext = "Pipe is busy";
            break;
        default:
            errtext = "Unknown NT status 0x" + Integer.toHexString(errcode);
            break;
        }
        return errtext;
    }

    /**
     * Return a Win32 error string.
     * 
     * @param errcode Win32 error code.
     * @return Win32 error string.
     */
    private final static String Win32ErrorText(int errcode)
    {

        // Convert the Win32 error code to a text string

        String errtext = "";

        switch (errcode)
        {
        case SMBStatus.Win32FileNotFound:
            errtext = "File not found";
            break;
        case SMBStatus.Win32PathNotFound:
            errtext = "Path not found";
            break;
        case SMBStatus.Win32AccessDenied:
            errtext = "Access denied";
            break;
        case SMBStatus.Win32InvalidHandle:
            errtext = "Invalid handle";
            break;
        case SMBStatus.Win32BadDeviceType:
            errtext = "Bad device type";
            break;
        case SMBStatus.Win32BadNetworkName:
            errtext = "Bad network name";
            break;
        case SMBStatus.Win32AlreadyAssigned:
            errtext = "Already assigned";
            break;
        case SMBStatus.Win32InvalidPassword:
            errtext = "Invalid password";
            break;
        case SMBStatus.Win32MoreEntries:
            errtext = "More entries";
            break;
        case SMBStatus.Win32MoreData:
            errtext = "More data";
            break;
        case SMBStatus.Win32NoMoreItems:
            errtext = "No more items";
            break;
        case SMBStatus.Win32InvalidAddress:
            errtext = "Invalid address";
            break;
        case SMBStatus.Win32ServiceDoesNotExist:
            errtext = "Service does not exist";
            break;
        case SMBStatus.Win32ServiceMarkedForDelete:
            errtext = "Service marked for delete";
            break;
        case SMBStatus.Win32ServiceExists:
            errtext = "Service already exists";
            break;
        case SMBStatus.Win32ServiceDuplicateName:
            errtext = "Duplicate service name";
            break;
        case SMBStatus.Win32BadDevice:
            errtext = "Bad device";
            break;
        case SMBStatus.Win32NoNetOrBadPath:
            errtext = "No network or bad path";
            break;
        case SMBStatus.Win32ExtendedError:
            errtext = "Extended error";
            break;
        case SMBStatus.Win32NoNetwork:
            errtext = "No network";
            break;
        default:
            errtext = "Unknown Win32 status 0x" + Integer.toHexString(errcode);
            break;
        }
        return errtext;
    }

    /**
     * Return a DCE/RPC error string.
     * 
     * @param errcode DCE/RPC error code
     * @return DCE/RPC error string.
     */
    private final static String DCERPCErrorText(int errcode)
    {

        // Convert the DCE/RPC error code to a text string

        if (errcode == SMBStatus.DCERPC_Fault)
            return "DCE/RPC Fault";
        return "DCE/RPC Error 0x" + Integer.toHexString(errcode);
    }
}