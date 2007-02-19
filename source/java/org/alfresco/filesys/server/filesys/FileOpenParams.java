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
package org.alfresco.filesys.server.filesys;

import org.alfresco.filesys.smb.SharingMode;
import org.alfresco.filesys.smb.WinNT;

/**
 * File Open Parameters Class
 * <p>
 * Contains the details of a file open request.
 */
public class FileOpenParams
{
    // Constants

    public final static String StreamSeparator = ":";

    // Conversion array for Core/LanMan open actions to NT open action codes

    private static int[] _NTToLMOpenCode = {
            FileAction.TruncateExisting + FileAction.CreateNotExist,
            FileAction.OpenIfExists,
            FileAction.CreateNotExist,
            FileAction.OpenIfExists + FileAction.CreateNotExist,
            FileAction.TruncateExisting,
            FileAction.TruncateExisting + FileAction.CreateNotExist };

    // File open mode strings

    private static String[] _openMode = { "Supersede", "Open", "Create", "OpenIf", "Overwrite", "OverwriteIf" };

    // File/directory to be opened

    private String m_path;

    // Stream name

    private String m_stream;

    // File open action

    private int m_openAction;

    // Desired access mode

    private int m_accessMode;

    // File attributes

    private int m_attr;

    // Allocation size

    private long m_allocSize;

    // Shared access flags

    private int m_sharedAccess = SharingMode.READWRITE;

    // Creation date/time

    private long m_createDate;

    // Root directory file id, zero if not specified

    private int m_rootFID;

    // Create options

    private int m_createOptions;

    // Security impersonation level, -1 if not set

    private int m_secLevel;

    // Security flags

    private int m_secFlags;

    // Owner group and user id

    private int m_gid = -1;
    private int m_uid = -1;

    // Unix mode

    private int m_mode = -1;

    //  File type and symbolic name
    
    private int m_fileType;
    private String m_symName;
    
    /**
     * Class constructor for Core SMB dialect Open SMB requests
     * 
     * @param path String
     * @param openAction int
     * @param accessMode int
     * @param fileAttr int
     */
    public FileOpenParams(String path, int openAction, int accessMode, int fileAttr)
    {

        // Parse the file path, split into file name and stream if specified

        parseFileName(path);

        m_openAction = convertToNTOpenAction(openAction);
        m_accessMode = convertToNTAccessMode(accessMode);
        m_attr = fileAttr;

        // Check if the diectory attribute is set

        if (FileAttribute.isDirectory(m_attr))
            m_createOptions = WinNT.CreateDirectory;

        // No security settings

        m_secLevel = -1;
    }

    /**
     * Class constructor for Core SMB dialect Open SMB requests
     * 
     * @param path String
     * @param openAction int
     * @param accessMode int
     * @param fileAttr int
     * @param gid int
     * @param uid int
     * @param mode int
     */
    public FileOpenParams(String path, int openAction, int accessMode, int fileAttr, int gid, int uid, int mode)
    {

        // Parse the file path, split into file name and stream if specified

        parseFileName(path);

        m_openAction = convertToNTOpenAction(openAction);
        m_accessMode = convertToNTAccessMode(accessMode);
        m_attr = fileAttr;

        // Check if the diectory attribute is set

        if (FileAttribute.isDirectory(m_attr))
            m_createOptions = WinNT.CreateDirectory;

        // No security settings

        m_secLevel = -1;

        m_gid = gid;
        m_uid = uid;
        m_mode = mode;
    }

    /**
     * Class constructor for LanMan SMB dialect OpenAndX requests
     * 
     * @param path String
     * @param openAction int
     * @param accessMode int
     * @param searchAttr int
     * @param fileAttr int
     * @param allocSize int
     * @param createDate long
     */
    public FileOpenParams(String path, int openAction, int accessMode, int searchAttr, int fileAttr, int allocSize,
            long createDate)
    {

        // Parse the file path, split into file name and stream if specified

        parseFileName(path);

        m_openAction = convertToNTOpenAction(openAction);
        m_accessMode = convertToNTAccessMode(accessMode);
        m_attr = fileAttr;
        m_sharedAccess = convertToNTSharedMode(accessMode);
        m_allocSize = (long) allocSize;
        m_createDate = createDate;

        // Check if the diectory attribute is set

        if (FileAttribute.isDirectory(m_attr))
            m_createOptions = WinNT.CreateDirectory;

        // No security settings

        m_secLevel = -1;
    }

    /**
     * Class constructor for NT SMB dialect NTCreateAndX requests
     * 
     * @param path String
     * @param openAction int
     * @param accessMode int
     * @param attr int
     * @param sharedAccess int
     * @param allocSize long
     * @param createOption int
     * @param rootFID int
     * @param secLevel int
     * @param secFlags int
     */
    public FileOpenParams(String path, int openAction, int accessMode, int attr, int sharedAccess, long allocSize,
            int createOption, int rootFID, int secLevel, int secFlags)
    {

        // Parse the file path, split into file name and stream if specified

        parseFileName(path);

        m_openAction = openAction;
        m_accessMode = accessMode;
        m_attr = attr;
        m_sharedAccess = sharedAccess;
        m_allocSize = allocSize;
        m_createOptions = createOption;
        m_rootFID = rootFID;
        m_secLevel = secLevel;
        m_secFlags = secFlags;

        // Make sure the directory attribute is set if the create directory option is set

        if ((createOption & WinNT.CreateDirectory) != 0 && (m_attr & FileAttribute.Directory) == 0)
            m_attr += FileAttribute.Directory;
    }

    /**
     * Return the path to be opened/created
     * 
     * @return String
     */
    public final String getPath()
    {
        return m_path;
    }

    /**
     * Return the full path to be opened/created, including the stream
     * 
     * @return String
     */
    public final String getFullPath()
    {
        if (isStream())
            return m_path + m_stream;
        else
            return m_path;
    }

    /**
     * Return the file attributes
     * 
     * @return int
     */
    public final int getAttributes()
    {
        return m_attr;
    }

    /**
     * Return the allocation size, or zero if not specified
     * 
     * @return long
     */
    public final long getAllocationSize()
    {
        return m_allocSize;
    }

    /**
     * Determine if a creation date/time has been specified
     * 
     * @return boolean
     */
    public final boolean hasCreationDateTime()
    {
        return m_createDate != 0L ? true : false;
    }

    /**
     * Return the file creation date/time
     * 
     * @return long
     */
    public final long getCreationDateTime()
    {
        return m_createDate;
    }

    /**
     * Return the open/create file/directory action All actions are mapped to the FileAction.NTxxx
     * action codes.
     * 
     * @return int
     */
    public final int getOpenAction()
    {
        return m_openAction;
    }

    /**
     * Return the root directory file id, or zero if not specified
     * 
     * @return int
     */
    public final int getRootDirectoryFID()
    {
        return m_rootFID;
    }

    /**
     * Return the stream name
     * 
     * @return String
     */
    public final String getStreamName()
    {
        return m_stream;
    }

    /**
     * Check if the specified create option is enabled, specified in the WinNT class.
     * 
     * @param flag int
     * @return boolean
     */
    public final boolean hasCreateOption(int flag)
    {
        return (m_createOptions & flag) != 0 ? true : false;
    }

    /**
     * Check if a file stream has been specified in the path to be created/opened
     * 
     * @return boolean
     */
    public final boolean isStream()
    {
        return m_stream != null ? true : false;
    }

    /**
     * Determine if the file is to be opened read-only
     * 
     * @return boolean
     */
    public final boolean isReadOnlyAccess()
    {
        // Check if read-only or execute access has been requested
        
        if (( m_accessMode & AccessMode.NTReadWrite) == AccessMode.NTRead ||
                (m_accessMode & AccessMode.NTExecute) != 0)
            return true;
        return false;
    }

    /**
     * Determine if the file is to be opened write-only
     * 
     * @return boolean
     */
    public final boolean isWriteOnlyAccess()
    {
        return (m_accessMode & AccessMode.NTReadWrite) == AccessMode.NTWrite ? true : false;
    }

    /**
     * Determine if the file is to be opened read/write
     * 
     * @return boolean
     */
    public final boolean isReadWriteAccess()
    {
        return (m_accessMode & AccessMode.NTReadWrite) == AccessMode.NTReadWrite ? true : false;
    }

    /**
     * Check for a particular access mode
     * 
     * @param mode int
     * @return boolean
     */
    public final boolean hasAccessMode(int mode)
    {
        return (m_accessMode & mode) == mode ? true : false;
    }
    
    /**
     * Determine if the target of the create/open is a directory
     * 
     * @return boolean
     */
    public final boolean isDirectory()
    {
        return hasCreateOption(WinNT.CreateDirectory);
    }

    /**
     * Determine if the file will be accessed sequentially only
     * 
     * @return boolean
     */
    public final boolean isSequentialAccessOnly()
    {
        return hasCreateOption(WinNT.CreateSequential);
    }

    /**
     * Determine if the file should be deleted when closed
     * 
     * @return boolean
     */
    public final boolean isDeleteOnClose()
    {
        return hasCreateOption(WinNT.CreateDeleteOnClose);
    }

    /**
     * Determine if write-through mode is enabled (buffering is not allowed if enabled)
     * 
     * @return boolean
     */
    public final boolean isWriteThrough()
    {
        return hasCreateOption(WinNT.CreateWriteThrough);
    }

    /**
     * Determine if the open mode should overwrite/truncate an existing file
     * 
     * @return boolean
     */
    public final boolean isOverwrite()
    {
        if (getOpenAction() == FileAction.NTSupersede || getOpenAction() == FileAction.NTOverwrite
                || getOpenAction() == FileAction.NTOverwriteIf)
            return true;
        return false;
    }

    /**
     * Return the file type
     * 
     * @return int
     */
    public final int isFileType()
    {
    	return m_fileType;
    }
    
    /**
     * determine if the target of the create/open is a symbolic link
     * 
     * @return boolean
     */
    public final boolean isSymbolicLink()
    {
    	return isFileType() == FileType.SymbolicLink;
    }
    
    /**
     * Return the symbolic link name
     * 
     * @return String
     */
    public final String getSymbolicLinkName()
    {
    	return m_symName;
    }
    
    /**
     * Return the shared access mode, zero equals allow any shared access
     * 
     * @return int
     */
    public final int getSharedAccess()
    {
        return m_sharedAccess;
    }

    /**
     * Determine if security impersonation is enabled
     * 
     * @return boolean
     */
    public final boolean hasSecurityLevel()
    {
        return m_secLevel != -1 ? true : false;
    }

    /**
     * Return the security impersonation level. Levels are defined in the WinNT class.
     * 
     * @return int
     */
    public final int getSecurityLevel()
    {
        return m_secLevel;
    }

    /**
     * Determine if the security context tracking flag is enabled
     * 
     * @return boolean
     */
    public final boolean hasSecurityContextTracking()
    {
        return (m_secFlags & WinNT.SecurityContextTracking) != 0 ? true : false;
    }

    /**
     * Determine if the security effective only flag is enabled
     * 
     * @return boolean
     */
    public final boolean hasSecurityEffectiveOnly()
    {
        return (m_secFlags & WinNT.SecurityEffectiveOnly) != 0 ? true : false;
    }

    /**
     * Determine if the group id has been set
     * 
     * @return boolean
     */
    public final boolean hasGid()
    {
        return m_gid != -1 ? true : false;
    }

    /**
     * Return the owner group id
     * 
     * @return int
     */
    public final int getGid()
    {
        return m_gid;
    }

    /**
     * Determine if the user id has been set
     * 
     * @return boolean
     */
    public final boolean hasUid()
    {
        return m_uid != -1 ? true : false;
    }

    /**
     * Return the owner user id
     * 
     * @return int
     */
    public final int getUid()
    {
        return m_uid;
    }

    /**
     * Determine if the mode has been set
     * 
     * @return boolean
     */
    public final boolean hasMode()
    {
        return m_mode != -1 ? true : false;
    }

    /**
     * Return the Unix mode
     * 
     * @return int
     */
    public final int getMode()
    {
        return m_mode;
    }

    /**
     * Set the Unix mode
     * 
     * @param mode int
     */
    public final void setMode(int mode)
    {
        m_mode = mode;
    }

    /**
     * Set a create option flag
     * 
     * @param flag int
     */
    public final void setCreateOption(int flag)
    {
        m_createOptions = m_createOptions | flag;
    }

    /**
     * Set the file type
     * 
     * @param typ int
     */
    public final void setFileType(int typ)
    {
    	m_fileType = typ;
    }
    
    /**
     * Set the symbolic link name
     * 
     * @param name String
     */
    public final void setSymbolicLink(String name)
    {
    	m_symName  = name;
    	m_fileType = FileType.SymbolicLink;
    }
    
    /**
     * Convert a Core/LanMan access mode to an NT access mode
     * 
     * @param accessMode int
     * @return int
     */
    private final int convertToNTAccessMode(int accessMode)
    {

        // Convert the Core/LanMan SMB dialect format access mode value to an NT access mode

        int mode = 0;

        switch (AccessMode.getAccessMode(accessMode))
        {
        case AccessMode.ReadOnly:
            mode = AccessMode.NTRead;
            break;
        case AccessMode.WriteOnly:
            mode = AccessMode.NTWrite;
            break;
        case AccessMode.ReadWrite:
            mode = AccessMode.NTReadWrite;
            break;
        }
        return mode;
    }

    /**
     * Convert a Core/LanMan open action to an NT open action
     * 
     * @param openAction int
     * @return int
     */
    private final int convertToNTOpenAction(int openAction)
    {

        // Convert the Core/LanMan SMB dialect open action to an NT open action

        int action = FileAction.NTOpen;

        for (int i = 0; i < _NTToLMOpenCode.length; i++)
        {
            if (_NTToLMOpenCode[i] == openAction)
                action = i;
        }
        return action;
    }

    /**
     * Convert a Core/LanMan shared access to NT sharing flags
     * 
     * @param sharedAccess int
     * @return int
     */
    private final int convertToNTSharedMode(int sharedAccess)
    {

        // Get the shared access value from the access mask

        int shr = AccessMode.getSharingMode(sharedAccess);
        int ret = SharingMode.READWRITE;

        switch (shr)
        {
        case AccessMode.Exclusive:
            ret = SharingMode.NOSHARING;
            break;
        case AccessMode.DenyRead:
            ret = SharingMode.WRITE;
            break;
        case AccessMode.DenyWrite:
            ret = SharingMode.READ;
            break;
        }
        return ret;
    }

    /**
     * Parse a file name to split the main file name/path and stream name
     * 
     * @param fileName String
     */
    private final void parseFileName(String fileName)
    {

        // Check if the file name contains a stream name

        int pos = fileName.indexOf(StreamSeparator);
        if (pos == -1)
        {
            m_path = fileName;
            return;
        }

        // Split the main file name and stream name

        m_path = fileName.substring(0, pos);
        m_stream = fileName.substring(pos);
    }

    /**
     * Return the file open parameters as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();
        str.append("[");

        str.append(getPath());

        str.append(",");
        str.append(_openMode[getOpenAction()]);
        str.append(",acc=0x");
        str.append(Integer.toHexString(m_accessMode));
        str.append(",attr=0x");
        str.append(Integer.toHexString(getAttributes()));
        str.append(",alloc=");
        str.append(getAllocationSize());
        str.append(",share=0x");
        str.append(Integer.toHexString(getSharedAccess()));

        if (getRootDirectoryFID() != 0)
        {
            str.append(",fid=");
            str.append(getRootDirectoryFID());
        }

        if (hasCreationDateTime())
        {
            str.append(",cdate=");
            str.append(getCreationDateTime());
        }

        if (m_createOptions != 0)
        {
            str.append(",copt=0x");
            str.append(Integer.toHexString(m_createOptions));
        }

        if (hasSecurityLevel())
        {
            str.append(",seclev=");
            str.append(getSecurityLevel());
            str.append(",secflg=0x");
            str.append(Integer.toHexString(m_secFlags));
        }
        str.append("]");

        if (hasGid() || hasUid())
        {
            str.append(",gid=");
            str.append(getGid());
            str.append(",uid=");
            str.append(getUid());
            str.append(",mode=");
            str.append(getMode());
        }
        return str.toString();
    }
}
