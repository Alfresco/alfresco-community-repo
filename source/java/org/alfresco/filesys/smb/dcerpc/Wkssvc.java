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
 * Wkssvc Operation Ids Class
 */
public class Wkssvc
{
    // Wkssvc opcodes

    public static final int NetWkstaGetInfo = 0x00;

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
        case NetWkstaGetInfo:
            ret = "NetWkstaGetInfo";
            break;
        }
        return ret;
    }
}
