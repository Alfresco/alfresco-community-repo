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
package org.alfresco.filesys.smb.server;

/**
 * <p>
 * Contains the various states that an SMB server session will go through during the session
 * lifetime.
 */
public class SMBSrvSessionState
{

    // NetBIOS session has been closed.

    public static final int NBHANGUP = 5;

    // NetBIOS session request state.

    public static final int NBSESSREQ = 0;

    // SMB session closed down.

    public static final int SMBCLOSED = 4;

    // Negotiate SMB dialect.

    public static final int SMBNEGOTIATE = 1;

    // SMB session is initialized, ready to receive/handle standard SMB requests.

    public static final int SMBSESSION = 3;

    // SMB session setup.

    public static final int SMBSESSSETUP = 2;

    // State name strings

    private static final String _stateName[] = {
            "NBSESSREQ",
            "SMBNEGOTIATE",
            "SMBSESSSETUP",
            "SMBSESSION",
            "SMBCLOSED",
            "NBHANGUP" };

    /**
     * Return the specified SMB state as a string.
     */
    public static String getStateAsString(int state)
    {
        if (state < _stateName.length)
            return _stateName[state];
        return "[UNKNOWN]";
    }
}