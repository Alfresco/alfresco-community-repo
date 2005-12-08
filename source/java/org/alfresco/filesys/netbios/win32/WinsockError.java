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
package org.alfresco.filesys.netbios.win32;

/**
 * Winsock Error Codes Class
 *
 * <p>Contains a list of the error codes that the Win32 Winsock calls may generate, and a method to convert
 * to an error text string.
 * 
 * @author GKSpencer
 */
public class WinsockError
{
    // Winsock error code constants
    
    public static final int WsaEIntr        = 10004;
    public static final int WsaEAcces       = 10013;
    public static final int WsaEFault       = 10014;
    public static final int WsaEInval       = 10022;
    public static final int WsaEMfile       = 10024;
    public static final int WsaEWouldBlock  = 10035;
    public static final int WsaEInProgress  = 10036;
    public static final int WsaEAlready     = 10037;
    public static final int WsaENotSock     = 10038;
    public static final int WsaEDestAddrReq = 10039;
    public static final int WsaEMsgSize     = 10040;
    public static final int WsaEPrototype   = 10041;
    public static final int WsaENoProtoOpt  = 10042;
    public static final int WsaEProtoNoSupp = 10043;
    public static final int WsaESocktNoSupp = 10044;
    public static final int WsaEOpNotSupp   = 10045;
    public static final int WsaEPFNoSupport = 10046;
    public static final int WsaEAFNoSupport = 10047;
    public static final int WsaEAddrInUse   = 10048;
    public static final int WsaEAddrNotAvail= 10049;
    public static final int WsaENetDown     = 10050;
    public static final int WsaENetUnReach  = 10051;
    public static final int WsaENetReset    = 10052;
    public static final int WsaEConnAborted = 10053;
    public static final int WsaEConnReset   = 10054;
    public static final int WsaENoBufs      = 10055;
    public static final int WsaEIsConn      = 10056;
    public static final int WsaENotConn     = 10057;
    public static final int WsaEShutdown    = 10058;
    public static final int WsaETimedout    = 10060;
    public static final int WsaEConnRefused = 10061;
    public static final int WsaEHostDown    = 10064;
    public static final int WsaEHostUnreach = 10065;
    public static final int WsaEProcLim     = 10067;
    public static final int WsaSysNotReady  = 10091;
    public static final int WsaVerNotSupp   = 10092;
    public static final int WsaNotInit      = 10093;
    public static final int WsaEDiscon      = 10101;
    public static final int WsaTypeNotFound = 10109;
    public static final int WsaHostNotFound = 11001;
    public static final int WsaTryAgain     = 11002;
    public static final int WsaNoRecovery   = 11003;
    public static final int WsaNoData       = 11004;
    
    /**
     * Convert a Winsock error code to a text string
     * 
     * @param sts int
     * @return String
     */
    public static final String asString(int sts)
    {
        String errText = null;
        
        switch ( sts)
        {
        case WsaEIntr:
            errText = "Interrupted function call";
            break;
        case WsaEAcces:
            errText = "Permission denied";
            break;
        case WsaEFault:
            errText = "Bad address";
            break;
        case WsaEInval:
            errText = "Invalid argument";
            break;
        case WsaEMfile:
            errText = "Too many open files";
            break;
        case WsaEWouldBlock:
            errText = "Resource temporarily unavailable";
            break;
        case WsaEInProgress:
            errText = "Operation now in progress";
            break;
        case WsaEAlready:
            errText = "Operation already in progress";
            break;
        case WsaENotSock:
            errText = "Socket operation on nonsocket";
            break;
        case WsaEDestAddrReq:
            errText = "Destination address required";
            break;
        case WsaEMsgSize:
            errText = "Message too long";
            break;
        case WsaEPrototype:
            errText = "Protocol wrong type for socket";
            break;
        case WsaENoProtoOpt:
            errText = "Bad protocol option";
            break;
        case WsaEProtoNoSupp:
            errText = "Protocol not supported";
            break;
        case WsaESocktNoSupp:
            errText = "Socket type not supported";
            break;
        case WsaEOpNotSupp:
            errText = "Operation not supported";
            break;
        case WsaEPFNoSupport:
            errText = "Protocol family not supported";
            break;
        case WsaEAFNoSupport:
            errText = "Address family not supported by protocol family";
            break;
        case WsaEAddrInUse:
            errText = "Address already in use";
            break;
        case WsaEAddrNotAvail:
            errText = "Cannot assign requested address";
            break;
        case WsaENetDown:
            errText = "Network is down";
            break;
        case WsaENetUnReach:
            errText = "Network is unreachable";
            break;
        case WsaENetReset:
            errText = "Network dropped connection on reset";
            break;
        case WsaEConnAborted:
            errText = "Software caused connection abort";
            break;
        case WsaEConnReset:
            errText = "Connection reset by peer";
            break;
        case WsaENoBufs:
            errText = "No buffer space available";
            break;
        case WsaEIsConn:
            errText = "Socket is already connected";
            break;
        case WsaENotConn:
            errText = "Socket is not connected";
            break;
        case WsaEShutdown:
            errText = "Cannot send after socket shutdown";
            break;
        case WsaETimedout:
            errText = "Connection timed out";
            break;
        case WsaEConnRefused:
            errText = "Connection refused";
            break;
        case WsaEHostDown:
            errText = "Host is down";
            break;
        case WsaEHostUnreach:
            errText = "No route to host";
            break;
        case WsaEProcLim:
            errText = "Too many processes";
            break;
        case WsaSysNotReady:
            errText = "Network subsystem is unavailable";
            break;
        case WsaVerNotSupp:
            errText = "Winsock.dll version out of range";
            break;
        case WsaNotInit:
            errText = "Successful WSAStartup not yet performed";
            break;
        case WsaEDiscon:
            errText = "Graceful shutdown in progress";
            break;
        case WsaTypeNotFound:
            errText = "Class type not found";
            break;
        case WsaHostNotFound:
            errText = "Host not found";
            break;
        case WsaTryAgain:
            errText = "Nonauthoritative host not found";
            break;
        case WsaNoRecovery:
            errText = "This is a nonrecoverable error";
            break;
        case WsaNoData:
            errText = "Valid name, no data record of requested type";
            break;
        default:
            errText = "Unknown Winsock error 0x" + Integer.toHexString(sts);
            break;
        }
        
        return errText;
    }
}
