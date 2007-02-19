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
package org.alfresco.filesys.smb.mailslot;

import org.alfresco.filesys.util.DataPacker;

/**
 * Mail slot constants class.
 */
public final class MailSlot
{

    // Mail slot opcodes

    public static final int WRITE = 0x01;

    // Mail slot classes

    public static final int UNRELIABLE = 0x02;

    // Mailslot \MAILSLOT\BROWSE opcodes

    public static final int HostAnnounce = 1;
    public static final int AnnouncementRequest = 2;
    public static final int RequestElection = 8;
    public static final int GetBackupListReq = 9;
    public static final int GetBackupListResp = 10;
    public static final int BecomeBackup = 11;
    public static final int DomainAnnouncement = 12;
    public static final int MasterAnnouncement = 13;
    public static final int LocalMasterAnnouncement = 15;

    /**
     * Create a host announcement mailslot structure
     * 
     * @param buf byte[]
     * @param off int
     * @param host String
     * @param comment String
     * @param typ int
     * @param interval int
     * @param upd int
     * @return int
     */
    public final static int createHostAnnouncement(byte[] buf, int off, String host, String comment, int typ,
            int interval, int upd)
    {

        // Set the command code and update count

        buf[off] = MailSlot.HostAnnounce;
        buf[off + 1] = 0; // (byte) (upd & 0xFF);

        // Set the announce interval, in minutes

        DataPacker.putIntelInt(interval * 60000, buf, off + 2);

        // Pack the host name

        byte[] hostByt = host.getBytes();
        for (int i = 0; i < 16; i++)
        {
            if (i < hostByt.length)
                buf[off + 6 + i] = hostByt[i];
            else
                buf[off + 6 + i] = 0;
        }

        // Major/minor version number

        buf[off + 22] = 5; // major version
        buf[off + 23] = 1; // minor version

        // Set the server type flags

        DataPacker.putIntelInt(typ, buf, off + 24);

        // Browser election version and browser constant

        DataPacker.putIntelShort(0x010F, buf, off + 28);
        DataPacker.putIntelShort(0xAA55, buf, off + 30);

        // Add the server comment string, or a null string

        int pos = off + 33;

        if (comment != null)
            pos = DataPacker.putString(comment, buf, off + 32, true);

        // Return the end of data position

        return pos;
    }

    /**
     * Create an announcement request mailslot structure
     * 
     * @param buf byte[]
     * @param off int
     * @param host String
     * @return int
     */
    public final static int createAnnouncementRequest(byte[] buf, int off, String host)
    {

        // Set the command code

        buf[off] = MailSlot.AnnouncementRequest;
        buf[off + 1] = 0;

        // Pack the host name

        byte[] hostByt = host.getBytes();
        for (int i = 0; i < 16; i++)
        {
            if (i < hostByt.length)
                buf[off + 2 + i] = hostByt[i];
            else
                buf[off + 2 + i] = 0;
        }

        // Return the end of buffer position

        return off + 17;
    }
}