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
 * Srvsvc Operation Ids Class
 */
public class Srvsvc
{

    // Srvsvc opcodes

    public static final int NetrServerGetInfo =     0x15;
    public static final int NetrServerSetInfo =     0x16;
    public static final int NetrShareEnum =         0x0F;
    public static final int NetrShareEnumSticky =   0x24;
    public static final int NetrShareGetInfo =      0x10;
    public static final int NetrShareSetInfo =      0x11;
    public static final int NetrShareAdd =          0x0E;
    public static final int NetrShareDel =          0x12;
    public static final int NetrSessionEnum =       0x0C;
    public static final int NetrSessionDel =        0x0D;
    public static final int NetrConnectionEnum =    0x08;
    public static final int NetrFileEnum =          0x09;
    public static final int NetrRemoteTOD =         0x1C;

    /**
     * Convert an opcode to a function name
     * 
     * @param opCode int
     * @return String
     */
    public final static String getOpcodeName(int opCode)
    {

        String ret = "";
        switch (opCode)
        {
        case NetrServerGetInfo:
            ret = "NetrServerGetInfo";
            break;
        case NetrServerSetInfo:
            ret = "NetrServerSetInfo";
            break;
        case NetrShareEnum:
            ret = "NetrShareEnum";
            break;
        case NetrShareEnumSticky:
            ret = "NetrShareEnumSticky";
            break;
        case NetrShareGetInfo:
            ret = "NetrShareGetInfo";
            break;
        case NetrShareSetInfo:
            ret = "NetrShareSetInfo";
            break;
        case NetrShareAdd:
            ret = "NetrShareAdd";
            break;
        case NetrShareDel:
            ret = "NetrShareDel";
            break;
        case NetrSessionEnum:
            ret = "NetrSessionEnum";
            break;
        case NetrSessionDel:
            ret = "NetrSessionDel";
            break;
        case NetrConnectionEnum:
            ret = "NetrConnectionEnum";
            break;
        case NetrFileEnum:
            ret = "NetrFileEnum";
            break;
        case NetrRemoteTOD:
            ret = "NetrRemoteTOD";
            break;
        }
        return ret;
    }
}
