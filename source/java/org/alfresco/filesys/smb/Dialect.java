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
 * SMB dialect class.
 * <p>
 * This class contains the available SMB protocol dialects that may be negotiated when an SMB
 * session is setup.
 */

public final class Dialect
{

    // SMB dialect strings, encoded into the SMB session setup packet.

    private static final String[] protList = {
            "PC NETWORK PROGRAM 1.0",
            "MICROSOFT NETWORKS 1.03",
            "MICROSOFT NETWORKS 3.0",
            "DOS LANMAN1.0",
            "LANMAN1.0",
            "DOS LM1.2X002",
            "LM1.2X002",
            "DOS LANMAN2.1",
            "LANMAN2.1",
            "Samba",
            "NT LM 0.12",
            "NT LANMAN 1.0" };

    // SMB dialect type strings

    private static final String[] protType = {
            "Core",
            "CorePlus",
            "DOS LANMAN 1.0",
            "LANMAN1.0",
            "DOS LANMAN 2.1",
            "LM1.2X002",
            "LANMAN2.1",
            "NT LM 0.12" };

    // Dialect constants

    public static final int Core = 0;
    public static final int CorePlus = 1;
    public static final int DOSLanMan1 = 2;
    public static final int LanMan1 = 3;
    public static final int DOSLanMan2 = 4;
    public static final int LanMan2 = 5;
    public static final int LanMan2_1 = 6;
    public static final int NT = 7;
    public static final int Max = 8;

    public static final int Unknown = -1;

    // SMB dialect type to string conversion array

    private static final int[] protIdx = {
            Core,
            CorePlus,
            DOSLanMan1,
            DOSLanMan1,
            LanMan1,
            DOSLanMan2,
            LanMan2,
            LanMan2_1,
            LanMan2_1,
            NT,
            NT,
            NT };

    // SMB dialect type to string conversion array length

    public static final int SMB_PROT_MAXSTRING = protIdx.length;

    // Table that maps SMB commands to the minimum required SMB dialect

    private static final int[] cmdtable = {
            Core, // CreateDirectory
            Core, // DeleteDirectory
            Core, // OpenFile
            Core, // CreateFile
            Core, // CloseFile
            Core, // FlushFile
            Core, // DeleteFile
            Core, // RenameFile
            Core, // QueryFileInfo
            Core, // SetFileInfo
            Core, // Read
            Core, // Write
            Core, // LockFile
            Core, // UnlockFile
            Core, // CreateTemporary
            Core, // CreateNew
            Core, // CheckDirectory
            Core, // ProcessExit
            Core, // SeekFile
            LanMan1, // LockAndRead
            LanMan1, // WriteAndUnlock
            0, // Unused
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            LanMan1, // ReadRaw
            LanMan1, // WriteMpxSecondary
            LanMan1, // WriteRaw
            LanMan1, // WriteMpx
            0, // Unused
            LanMan1, // WriteComplete
            0, // Unused
            LanMan1, // SetInformation2
            LanMan1, // QueryInformation2
            LanMan1, // LockingAndX
            LanMan1, // Transaction
            LanMan1, // TransactionSecondary
            LanMan1, // Ioctl
            LanMan1, // Ioctl2
            LanMan1, // Copy
            LanMan1, // Move
            LanMan1, // Echo
            LanMan1, // WriteAndClose
            LanMan1, // OpenAndX
            LanMan1, // ReadAndX
            LanMan1, // WriteAndX
            0, // Unused
            LanMan1, // CloseAndTreeDisconnect
            LanMan2, // Transaction2
            LanMan2, // Transaction2Secondary
            LanMan2, // FindClose2
            LanMan1, // FindNotifyClose
            0, // Unused
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            Core, // TreeConnect
            Core, // TreeDisconnect
            Core, // Negotiate
            Core, // SessionSetupAndX
            LanMan1, // LogoffAndX
            LanMan1, // TreeConnectAndX
            0, // Unused
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            Core, // DiskInformation
            Core, // Search
            LanMan1, // Find
            LanMan1, // FindUnique
            0, // Unused
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            NT, // NTTransact
            NT, // NTTransactSecondary
            NT, // NTCreateAndX
            NT, // NTCancel
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            Core, // OpenPrintFile
            Core, // WritePrintFile
            Core, // ClosePrintFile
            Core, // GetPrintQueue
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            0, // ..
            -1, // SendMessage
            -1, // SendBroadcast
            -1, // SendForward
            -1, // CancelForward
            -1, // GetMachineName
            -1, // SendMultiStart
            -1, // SendMultiEnd
            -1 // SendMultiText
    };

    /**
     * Return the required SMB dialect string.
     * 
     * @param i SMB dialect string index.
     * @return SMB dialect string.
     */

    public static String DialectString(int i)
    {

        // Validate the dialect index

        if (i >= protList.length)
            return null;
        return protList[i];
    }

    /**
     * Determine if the SMB dialect supports the SMB command
     * 
     * @return boolean
     * @param dialect int SMB dialect type.
     * @param cmd int SMB command code.
     */
    public final static boolean DialectSupportsCommand(int dialect, int cmd)
    {
        // Range check the command

        if (cmd > cmdtable.length)
            return false;

        // Check if the SMB dialect supports the SMB command.

        if (cmdtable[cmd] <= dialect)
            return true;
        return false;
    }

    /**
     * Return the SMB dialect type for the specified SMB dialect string index.
     * 
     * @param i SMB dialect type.
     * @return SMB dialect string index.
     */

    public static int DialectType(int i)
    {
        return protIdx[i];
    }

    /**
     * Return the SMB dialect type for the specified string.
     * 
     * @return int
     * @param diastr java.lang.String
     */
    public static int DialectType(String diastr)
    {

        // Search the protocol string list

        int i = 0;

        while (i < protList.length && protList[i].compareTo(diastr) != 0)
            i++;

        // Return the protocol id

        if (i < protList.length)
            return DialectType(i);
        else
            return Unknown;
    }

    /**
     * Return the dialect type as a string.
     * 
     * @param dia SMB dialect type.
     * @return SMB dialect type string.
     */

    public static String DialectTypeString(int dia)
    {
        return protType[dia];
    }

    /**
     * Return the number of available SMB dialect strings.
     * 
     * @return Number of available SMB dialect strings.
     */

    public static int NumberOfDialects()
    {
        return protList.length;
    }
}