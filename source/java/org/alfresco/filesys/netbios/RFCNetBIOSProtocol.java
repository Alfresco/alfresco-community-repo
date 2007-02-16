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
package org.alfresco.filesys.netbios;

/**
 * RFC NetBIOS constants.
 */
public final class RFCNetBIOSProtocol
{

    // RFC NetBIOS default port/socket

    public static final int PORT = 139;

    // RFC NetBIOS datagram port

    public static final int DATAGRAM = 138;

    // RFC NetBIOS default name lookup datagram port

    public static final int NAME_PORT = 137;

    // RFC NetBIOS default socket timeout

    public static final int TMO = 30000; // 30 seconds, in milliseconds

    // RFC NetBIOS message types.

    public static final int SESSION_MESSAGE = 0x00;
    public static final int SESSION_REQUEST = 0x81;
    public static final int SESSION_ACK = 0x82;
    public static final int SESSION_REJECT = 0x83;
    public static final int SESSION_RETARGET = 0x84;
    public static final int SESSION_KEEPALIVE = 0x85;

    // RFC NetBIOS packet header length, and various message lengths.

    public static final int HEADER_LEN = 4;
    public static final int SESSREQ_LEN = 72;
    public static final int SESSRESP_LEN = 9;

    // Maximum packet size that RFC NetBIOS can handle (17bit value)

    public static final int MaxPacketSize = 0x01FFFF + HEADER_LEN;
}