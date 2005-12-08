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
package org.alfresco.filesys.smb.dcerpc.info;

import org.alfresco.filesys.smb.dcerpc.DCEBuffer;
import org.alfresco.filesys.smb.dcerpc.DCEBufferException;
import org.alfresco.filesys.smb.dcerpc.DCEReadable;
import org.alfresco.filesys.smb.dcerpc.DCEWriteable;

/**
 * Share Information Class
 * <p>
 * Holds the details of a share from a DCE/RPC request/response.
 */
public class ShareInfo implements DCEWriteable, DCEReadable
{

    // Information levels supported

    public static final int InfoLevel0 = 0;
    public static final int InfoLevel1 = 1;
    public static final int InfoLevel2 = 2;
    public static final int InfoLevel502 = 502;
    public static final int InfoLevel1005 = 1005;

    // Share types

    public static final int Disk = 0x00000000;
    public static final int PrintQueue = 0x00000001;
    public static final int Device = 0x00000002;
    public static final int IPC = 0x00000003;
    public static final int Hidden = 0x80000000;

    // Share permission flags

    public static final int Read = 0x01;
    public static final int Write = 0x02;
    public static final int Create = 0x04;
    public static final int Execute = 0x08;
    public static final int Delete = 0x10;
    public static final int Attrib = 0x20;
    public static final int Perm = 0x40;
    public static final int All = 0x7F;

    // Information level

    private int m_infoLevel;

    // Share details

    private String m_name;
    private int m_type;
    private String m_comment;

    private int m_permissions;
    private int m_maxUsers;
    private int m_curUsers;
    private String m_path;
    private String m_password;

    private int m_flags;

    /**
     * Class constructor
     */
    public ShareInfo()
    {
    }

    /**
     * Class constructor
     * 
     * @param lev int
     */
    public ShareInfo(int lev)
    {
        m_infoLevel = lev;
    }

    /**
     * Class constructor
     * 
     * @param lev int
     * @param name String
     * @param typ int
     * @param comment String
     */
    public ShareInfo(int lev, String name, int typ, String comment)
    {
        m_infoLevel = lev;
        m_name = name;
        m_type = typ;
        m_comment = comment;
    }

    /**
     * Return the information level
     * 
     * @return int
     */
    public final int getInformationLevel()
    {
        return m_infoLevel;
    }

    /**
     * Return the share name
     * 
     * @return String
     */
    public final String getName()
    {
        return m_name;
    }

    /**
     * Return the share type
     * 
     * @return int
     */
    public final int getType()
    {
        return m_type;
    }

    /**
     * Get the share flags
     * 
     * @return int
     */
    public final int getFlags()
    {
        return m_flags;
    }

    /**
     * Check if this share is a hidden/admin share
     * 
     * @return boolean
     */
    public final boolean isHidden()
    {
        return (m_type & Hidden) != 0 ? true : false;
    }

    /**
     * Check if this is a disk share
     * 
     * @return boolean
     */
    public final boolean isDisk()
    {
        return (m_type & 0x0000FFFF) == Disk ? true : false;
    }

    /**
     * Check if this is a printer share
     * 
     * @return boolean
     */
    public final boolean isPrinter()
    {
        return (m_type & 0x0000FFFF) == PrintQueue ? true : false;
    }

    /**
     * Check if this is a device share
     * 
     * @return boolean
     */
    public final boolean isDevice()
    {
        return (m_type & 0x0000FFFF) == Device ? true : false;
    }

    /**
     * Check if this is a named pipe share
     * 
     * @return boolean
     */
    public final boolean isNamedPipe()
    {
        return (m_type & 0x0000FFFF) == IPC ? true : false;
    }

    /**
     * Return the share permissions
     * 
     * @return int
     */
    public final int getPermissions()
    {
        return m_permissions;
    }

    /**
     * Return the maximum number of users allowed
     * 
     * @return int
     */
    public final int getMaximumUsers()
    {
        return m_maxUsers;
    }

    /**
     * Return the current number of users
     * 
     * @return int
     */
    public final int getCurrentUsers()
    {
        return m_curUsers;
    }

    /**
     * Return the share local path
     * 
     * @return String
     */
    public final String getPath()
    {
        return m_path;
    }

    /**
     * Return the share password
     * 
     * @return String
     */
    public final String getPassword()
    {
        return m_password;
    }

    /**
     * Return the share type as a string
     * 
     * @return String
     */
    public final String getTypeAsString()
    {

        String typ = "";
        switch (getType() & 0xFF)
        {
        case Disk:
            typ = "Disk";
            break;
        case PrintQueue:
            typ = "Printer";
            break;
        case Device:
            typ = "Device";
            break;
        case IPC:
            typ = "IPC";
            break;
        }

        return typ;
    }

    /**
     * Return the comment
     * 
     * @return String
     */
    public final String getComment()
    {
        return m_comment;
    }

    /**
     * Set the information level
     * 
     * @param lev int
     */
    public final void setInformationLevel(int lev)
    {
        m_infoLevel = lev;
    }

    /**
     * Set the share type
     * 
     * @param int typ
     */
    public final void setType(int typ)
    {
        m_type = typ;
    }

    /**
     * Set the share flags
     * 
     * @param flags int
     */
    public final void setFlags(int flags)
    {
        m_flags = flags;
    }

    /**
     * Set the share name
     * 
     * @param name String
     */
    public final void setName(String name)
    {
        m_name = name;
    }

    /**
     * Set the share comment
     * 
     * @param str String
     */
    public final void setComment(String str)
    {
        m_comment = str;
    }

    /**
     * Set the share permissions
     * 
     * @param perm int
     */
    public final void setPermissions(int perm)
    {
        m_permissions = perm;
    }

    /**
     * Set the maximum number of users
     * 
     * @param maxUsers int
     */
    public final void setMaximumUsers(int maxUsers)
    {
        m_maxUsers = maxUsers;
    }

    /**
     * Set the current number of users
     * 
     * @param curUsers int
     */
    public final void setCurrentUsers(int curUsers)
    {
        m_curUsers = curUsers;
    }

    /**
     * Set the local path
     * 
     * @param path String
     */
    public final void setPath(String path)
    {
        m_path = path;
    }

    /**
     * Clear all string values
     */
    protected final void clearStrings()
    {

        // Clear the string values

        m_name = null;
        m_comment = null;
        m_path = null;
        m_password = null;
    }

    /**
     * Read the share information from the DCE/RPC buffer
     * 
     * @param buf DCEBuffer
     * @exception DCEBufferException
     */
    public void readObject(DCEBuffer buf) throws DCEBufferException
    {

        // Clear all existing strings

        clearStrings();

        // Unpack the share information

        switch (getInformationLevel())
        {

        // Information level 0

        case InfoLevel0:
            m_name = buf.getPointer() != 0 ? "" : null;
            break;

        // Information level 1

        case InfoLevel1:
            m_name = buf.getPointer() != 0 ? "" : null;
            m_type = buf.getInt();
            m_comment = buf.getPointer() != 0 ? "" : null;
            break;

        // Information level 2

        case InfoLevel2:
            m_name = buf.getPointer() != 0 ? "" : null;
            m_type = buf.getInt();
            m_comment = buf.getPointer() != 0 ? "" : null;
            m_permissions = buf.getInt();
            m_maxUsers = buf.getInt();
            m_curUsers = buf.getInt();
            m_path = buf.getPointer() != 0 ? "" : null;
            m_password = buf.getPointer() != 0 ? "" : null;
            break;

        // Information level 502

        case InfoLevel502:
            m_name = buf.getPointer() != 0 ? "" : null;
            m_type = buf.getInt();
            m_comment = buf.getPointer() != 0 ? "" : null;
            m_permissions = buf.getInt();
            m_maxUsers = buf.getInt();
            m_curUsers = buf.getInt();
            m_path = buf.getPointer() != 0 ? "" : null;
            m_password = buf.getPointer() != 0 ? "" : null;

            buf.skipBytes(4); // Reserved value

            // Security descriptor
            break;
        }
    }

    /**
     * Read the strings for this share from the DCE/RPC buffer
     * 
     * @param buf DCEBuffer
     * @exception DCEBufferException
     */
    public void readStrings(DCEBuffer buf) throws DCEBufferException
    {

        // Read the strings for this share information

        switch (getInformationLevel())
        {

        // Information level 0

        case InfoLevel0:
            if (getName() != null)
                m_name = buf.getString(DCEBuffer.ALIGN_INT);
            break;

        // Information level 1

        case InfoLevel1:
            if (getName() != null)
                m_name = buf.getString(DCEBuffer.ALIGN_INT);
            if (getComment() != null)
                m_comment = buf.getString(DCEBuffer.ALIGN_INT);
            break;

        // Information level 2 and 502

        case InfoLevel2:
        case InfoLevel502:
            if (getName() != null)
                m_name = buf.getString(DCEBuffer.ALIGN_INT);
            if (getComment() != null)
                m_comment = buf.getString(DCEBuffer.ALIGN_INT);
            if (getPath() != null)
                m_path = buf.getString(DCEBuffer.ALIGN_INT);
            if (getPassword() != null)
                m_password = buf.getString(DCEBuffer.ALIGN_INT);
            break;
        }
    }

    /**
     * Write the share information to the DCE buffer
     * 
     * @param buf DCEBuffer
     * @param strBuf DCEBuffer
     */
    public void writeObject(DCEBuffer buf, DCEBuffer strBuf)
    {

        // Pack the share information

        switch (getInformationLevel())
        {

        // Information level 0

        case InfoLevel0:
            buf.putPointer(true);
            strBuf.putString(getName(), DCEBuffer.ALIGN_INT, true);
            break;

        // Information level 1

        case InfoLevel1:
            buf.putPointer(true);
            buf.putInt(getType());
            buf.putPointer(true);

            strBuf.putString(getName(), DCEBuffer.ALIGN_INT, true);
            strBuf.putString(getComment() != null ? getComment() : "", DCEBuffer.ALIGN_INT, true);
            break;

        // Information level 2

        case InfoLevel2:
            buf.putPointer(true);
            buf.putInt(getType());
            buf.putPointer(true);
            buf.putInt(getPermissions());
            buf.putInt(getMaximumUsers());
            buf.putInt(getCurrentUsers());
            buf.putPointer(getPath() != null);
            buf.putPointer(getPassword() != null);

            strBuf.putString(getName(), DCEBuffer.ALIGN_INT, true);
            strBuf.putString(getComment() != null ? getComment() : "", DCEBuffer.ALIGN_INT, true);
            if (getPath() != null)
                strBuf.putString(getPath(), DCEBuffer.ALIGN_INT, true);
            if (getPassword() != null)
                strBuf.putString(getPassword(), DCEBuffer.ALIGN_INT, true);
            break;

        // Information level 502

        case InfoLevel502:
            buf.putPointer(true);
            buf.putInt(getType());
            buf.putPointer(true);
            buf.putInt(getPermissions());
            buf.putInt(getMaximumUsers());
            buf.putInt(getCurrentUsers());
            buf.putPointer(getPath() != null);
            buf.putPointer(getPassword() != null);
            buf.putInt(0); // Reserved, must be zero
            buf.putPointer(false); // Security descriptor

            strBuf.putString(getName(), DCEBuffer.ALIGN_INT, true);
            strBuf.putString(getComment() != null ? getComment() : "", DCEBuffer.ALIGN_INT, true);
            if (getPath() != null)
                strBuf.putString(getPath(), DCEBuffer.ALIGN_INT, true);
            if (getPassword() != null)
                strBuf.putString(getPassword(), DCEBuffer.ALIGN_INT, true);
            break;

        // Information level 1005

        case InfoLevel1005:
            buf.putInt(getFlags());
            break;
        }
    }

    /**
     * Return the share information as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();

        str.append("[");
        str.append(getName());
        str.append(":");
        str.append(getInformationLevel());
        str.append(":");

        if (getInformationLevel() == 1)
        {
            str.append("0x");
            str.append(Integer.toHexString(getType()));
            str.append(",");
            str.append(getComment());
        }

        str.append("]");
        return str.toString();
    }
}
