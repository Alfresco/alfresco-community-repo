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
package org.alfresco.filesys.smb.dcerpc;

/**
 * DCE/RPC Command Codes
 */
public class DCECommand
{

    // DCE/RPC Packet Types

    public final static byte REQUEST    = 0x00;
    public final static byte RESPONSE   = 0x02;
    public final static byte FAULT      = 0x03;
    public final static byte BIND       = 0x0B;
    public final static byte BINDACK    = 0x0C;
    public final static byte ALTCONT    = 0x0E;
    public final static byte AUTH3      = 0x0F;
    public final static byte BINDCONT   = 0x10;

    /**
     * Convert the command type to a string
     * 
     * @param cmd int
     * @return String
     */
    public final static String getCommandString(int cmd)
    {

        // Determine the PDU command type

        String ret = "";
        switch (cmd)
        {
        case REQUEST:
            ret = "Request";
            break;
        case RESPONSE:
            ret = "Repsonse";
            break;
        case FAULT:
            ret = "Fault";
            break;
        case BIND:
            ret = "Bind";
            break;
        case BINDACK:
            ret = "BindAck";
            break;
        case ALTCONT:
            ret = "AltCont";
            break;
        case AUTH3:
            ret = "Auth3";
            break;
        case BINDCONT:
            ret = "BindCont";
            break;
        }
        return ret;
    }
}