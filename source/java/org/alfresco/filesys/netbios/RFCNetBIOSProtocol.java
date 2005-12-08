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