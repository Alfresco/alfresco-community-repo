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
package org.alfresco.filesys.smb;

/**
 * Protocol Class
 * <p>
 * Declares constants for the available SMB protocols (TCP/IP NetBIOS and native TCP/IP SMB)
 */
public class Protocol
{

    // Available protocol types

    public final static int TCPNetBIOS = 1;
    public final static int NativeSMB = 2;

    // Protocol control constants

    public final static int UseDefault = 0;
    public final static int None = -1;

    /**
     * Return the protocol type as a string
     * 
     * @param typ int
     * @return String
     */
    public static final String asString(int typ)
    {
        String ret = "";
        if (typ == TCPNetBIOS)
            ret = "TCP/IP NetBIOS";
        else if (typ == NativeSMB)
            ret = "Native SMB (port 445)";

        return ret;
    }
}
