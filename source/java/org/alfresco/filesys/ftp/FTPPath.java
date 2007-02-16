/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.filesys.ftp;

import org.alfresco.filesys.server.filesys.*;
import org.alfresco.filesys.server.core.*;

/**
 * FTP Path Class
 * <p>
 * Converts FTP paths to share/share relative paths.
 * 
 * @author GKSpencer
 */
public class FTPPath
{

    // FTP directory seperator

    private static final String FTP_SEPERATOR = "/";
    private static final char FTP_SEPERATOR_CHAR = '/';

    // Share relative path directory seperator

    private static final String DIR_SEPERATOR = "\\";
    private static final char DIR_SEPERATOR_CHAR = '\\';

    // FTP path

    private String m_ftpPath;

    // Share name nad share relative path

    private String m_shareName;
    private String m_sharePath;

    // Shared device

    private DiskSharedDevice m_shareDev;

    // Flag to indicate if this is a directory or file path

    private boolean m_dir = true;

    /**
     * Default constructor
     */
    public FTPPath()
    {
        try
        {
            setFTPPath(null);
        }
        catch (Exception ex)
        {
        }
    }

    /**
     * Class constructor
     * 
     * @param ftpPath String
     * @exception InvalidPathException
     */
    public FTPPath(String ftpPath) throws InvalidPathException
    {
        setFTPPath(ftpPath);
    }

    /**
     * Class constructor
     * 
     * @param shrName String
     * @param shrPath String
     * @exception InvalidPathException
     */
    public FTPPath(String shrName, String shrPath) throws InvalidPathException
    {
        setSharePath(shrName, shrPath);
    }

    /**
     * Copy constructor
     * 
     * @param ftpPath FTPPath
     */
    public FTPPath(FTPPath ftpPath)
    {
        try
        {
            setFTPPath(ftpPath.getFTPPath());
            m_shareDev = ftpPath.getSharedDevice();
        }
        catch (Exception ex)
        {
        }
    }

    /**
     * Determine if the current FTP path is the root path
     * 
     * @return boolean
     */
    public final boolean isRootPath()
    {
        return m_ftpPath.compareTo(FTP_SEPERATOR) == 0 ? true : false;
    }

    /**
     * Determine if the path is for a directory or file
     * 
     * @return boolean
     */
    public final boolean isDirectory()
    {
        return m_dir;
    }

    /**
     * Check if the FTP path is valid
     * 
     * @return boolean
     */
    public final boolean hasFTPPath()
    {
        return m_ftpPath != null ? true : false;
    }

    /**
     * Return the FTP path
     * 
     * @return String
     */
    public final String getFTPPath()
    {
        return m_ftpPath;
    }

    /**
     * Check if the share name is valid
     * 
     * @return boolean
     */
    public final boolean hasShareName()
    {
        return m_shareName != null ? true : false;
    }

    /**
     * Return the share name
     * 
     * @return String
     */
    public final String getShareName()
    {
        return m_shareName;
    }

    /**
     * Check if the share path is the root path
     * 
     * @return boolean
     */
    public final boolean isRootSharePath()
    {
        if (m_sharePath == null || m_sharePath.compareTo(DIR_SEPERATOR) == 0)
            return true;
        return false;
    }

    /**
     * Check if the share path is valid
     * 
     * @reutrn boolean
     */
    public final boolean hasSharePath()
    {
        return m_sharePath != null ? true : false;
    }

    /**
     * Return the share relative path
     * 
     * @reutrn String
     */
    public final String getSharePath()
    {
        return m_sharePath;
    }

    /**
     * Check if the shared device has been set
     * 
     * @return boolean
     */
    public final boolean hasSharedDevice()
    {
        return m_shareDev != null ? true : false;
    }

    /**
     * Return the shared device
     * 
     * @return DiskSharedDevice
     */
    public final DiskSharedDevice getSharedDevice()
    {
        return m_shareDev;
    }

    /**
     * Set the paths using the specified FTP path
     * 
     * @param path String
     * @exception InvalidPathException
     */
    public final void setFTPPath(String path) throws InvalidPathException
    {

        // Check for a null path or the root path

        if (path == null || path.length() == 0 || path.compareTo(FTP_SEPERATOR) == 0)
        {
            m_ftpPath = FTP_SEPERATOR;
            m_shareName = null;
            m_sharePath = null;
            m_shareDev = null;
            return;
        }

        // Check if the path starts with the FTP seperator

        if (path.startsWith(FTP_SEPERATOR) == false)
            throw new InvalidPathException("Invalid FTP path, should start with " + FTP_SEPERATOR);

        // Save the FTP path

        m_ftpPath = path;

        // Get the first level directory from the path, this maps to the share name

        int pos = path.indexOf(FTP_SEPERATOR, 1);
        if (pos != -1)
        {
            m_shareName = path.substring(1, pos);
            if (path.length() > pos)
                m_sharePath = path.substring(pos).replace(FTP_SEPERATOR_CHAR, DIR_SEPERATOR_CHAR);
            else
                m_sharePath = DIR_SEPERATOR;
        }
        else
        {
            m_shareName = path.substring(1);
            m_sharePath = DIR_SEPERATOR;
        }

        // Check if the share has changed

        if (m_shareDev != null && m_shareName != null && m_shareDev.getName().compareTo(m_shareName) != 0)
            m_shareDev = null;
    }

    /**
     * Set the paths using the specified share and share relative path
     * 
     * @param shr String
     * @param path String
     * @exception InvalidPathException
     */
    public final void setSharePath(String shr, String path) throws InvalidPathException
    {

        // Save the share name and path

        m_shareName = shr;
        m_sharePath = path != null ? path : DIR_SEPERATOR;

        // Build the FTP style path

        StringBuffer ftpPath = new StringBuffer();

        ftpPath.append(FTP_SEPERATOR);
        if (hasShareName())
            ftpPath.append(getShareName());

        if (hasSharePath())
        {

            // Convert the share relative path to an FTP style path

            String ftp = getSharePath().replace(DIR_SEPERATOR_CHAR, FTP_SEPERATOR_CHAR);
            ftpPath.append(ftp);
        }
        else
            ftpPath.append(FTP_SEPERATOR);

        // Update the FTP path

        m_ftpPath = ftpPath.toString();
    }

    /**
     * Set the shared device
     * 
     * @param shareList SharedDeviceList
     * @param sess FTPSrvSession
     * @return boolean
     */
    public final boolean setSharedDevice(SharedDeviceList shareList, FTPSrvSession sess)
    {

        // Clear the current shared device

        m_shareDev = null;

        // Check if the share name is valid

        if (hasShareName() == false || shareList == null)
            return false;

        // Find the required disk share

        SharedDevice shr = shareList.findShare(getShareName());

        if (shr != null && shr instanceof DiskSharedDevice)
            m_shareDev = (DiskSharedDevice) shr;

        // Return the status

        return m_shareDev != null ? true : false;
    }

    /**
     * Set the shared device
     * 
     * @param share DiskSharedDevice
     * @return boolean
     */
    public final boolean setSharedDevice(DiskSharedDevice share)
    {
        m_shareDev = share;
        return true;
    }
    
    /**
     * Build an FTP path to the specified file
     * 
     * @param fname String
     * @return String
     */
    public final String makeFTPPathToFile(String fname)
    {

        // Build the FTP path to a file

        StringBuffer path = new StringBuffer(256);
        path.append(m_ftpPath);
        if (m_ftpPath.endsWith(FTP_SEPERATOR) == false)
            path.append(FTP_SEPERATOR);
        path.append(fname);

        return path.toString();
    }

    /**
     * Build a share relative path to the specified file
     * 
     * @param fname String
     * @return String
     */
    public final String makeSharePathToFile(String fname)
    {

        // Build the share relative path to a file

        StringBuilder path = new StringBuilder(256);
        path.append(m_sharePath);
        if (m_sharePath.endsWith(DIR_SEPERATOR) == false)
            path.append(DIR_SEPERATOR);
        path.append(fname);

        return path.toString();
    }

    /**
     * Add a directory to the end of the current path
     * 
     * @param dir String
     */
    public final void addDirectory(String dir)
    {

        // Check if the directory has a trailing seperator

        if (dir.length() > 1 && dir.endsWith(FTP_SEPERATOR) || dir.endsWith(DIR_SEPERATOR))
            dir = dir.substring(0, dir.length() - 1);

        // Append the directory to the FTP path

        StringBuilder str = new StringBuilder(256);
        str.append(m_ftpPath);

        if (m_ftpPath.endsWith(FTP_SEPERATOR) == false)
            str.append(FTP_SEPERATOR);
        str.append(dir);
        if (m_ftpPath.endsWith(FTP_SEPERATOR) == false)
            str.append(FTP_SEPERATOR);

        m_ftpPath = str.toString();

        // Check if there are any incorrect seperators in the FTP path

        if (m_ftpPath.indexOf(DIR_SEPERATOR) != -1)
            m_ftpPath = m_ftpPath.replace(FTP_SEPERATOR_CHAR, DIR_SEPERATOR_CHAR);

        // Append the directory to the share relative path

        str.setLength(0);
        str.append(m_sharePath);
        if (m_sharePath.endsWith(DIR_SEPERATOR) == false)
            str.append(DIR_SEPERATOR);
        str.append(dir);

        m_sharePath = str.toString();

        // Check if there are any incorrect seperators in the share relative path

        if (m_sharePath.indexOf(FTP_SEPERATOR) != -1)
            m_sharePath = m_sharePath.replace(FTP_SEPERATOR_CHAR, DIR_SEPERATOR_CHAR);

        // Indicate that the path is to a directory

        setDirectory(true);
    }

    /**
     * Add a file to the end of the current path
     * 
     * @param file String
     */
    public final void addFile(String file)
    {

        // Append the file name to the FTP path

        StringBuilder str = new StringBuilder(256);
        str.append(m_ftpPath);

        if (m_ftpPath.endsWith(FTP_SEPERATOR) == false)
            str.append(FTP_SEPERATOR);
        str.append(file);

        m_ftpPath = str.toString();

        // Check if there are any incorrect seperators in the FTP path

        if (m_ftpPath.indexOf(DIR_SEPERATOR) != -1)
            m_ftpPath = m_ftpPath.replace(FTP_SEPERATOR_CHAR, DIR_SEPERATOR_CHAR);

        // Append the file name to the share relative path

        str.setLength(0);
        str.append(m_sharePath);
        if (m_sharePath.endsWith(DIR_SEPERATOR) == false)
            str.append(DIR_SEPERATOR);
        str.append(file);

        m_sharePath = str.toString();

        // Check if there are any incorrect seperators in the share relative path

        if (m_sharePath.indexOf(FTP_SEPERATOR) != -1)
            m_sharePath = m_sharePath.replace(FTP_SEPERATOR_CHAR, DIR_SEPERATOR_CHAR);

        // Indicate that the path is to a file

        setDirectory(false);
    }

    /**
     * Remove the last directory from the end of the path
     */
    public final void removeDirectory()
    {

        // Check if the FTP path has a directory to remove

        if (m_ftpPath != null && m_ftpPath.length() > 1)
        {

            // Find the last directory in the FTP path

            int pos = m_ftpPath.length() - 1;
            if (m_ftpPath.endsWith(FTP_SEPERATOR))
                pos--;

            while (pos > 0 && m_ftpPath.charAt(pos) != FTP_SEPERATOR_CHAR)
                pos--;

            // Set the new FTP path

            m_ftpPath = m_ftpPath.substring(0, pos);

            // Indicate that the path is to a directory

            setDirectory(true);

            // Reset the share/share path

            try
            {
                setFTPPath(m_ftpPath);
            }
            catch (InvalidPathException ex)
            {
            }
        }
    }

    /**
     * Set/clear the directory path flag
     * 
     * @param dir boolean
     */
    protected final void setDirectory(boolean dir)
    {
        m_dir = dir;
    }

    /**
     * Check if an FTP path string contains multiple directories
     * 
     * @param path String
     * @return boolean
     */
    public final static boolean hasMultipleDirectories(String path)
    {
        if (path == null)
            return false;

        if (path.startsWith(FTP_SEPERATOR))
            return true;
        return false;
    }

    /**
     * Check if the FTP path is a relative path, ie. does not start with a leading slash
     * 
     * @param path String
     * @return boolean
     */
    public final static boolean isRelativePath(String path)
    {
        if (path == null)
            return false;
        return path.startsWith(FTP_SEPERATOR) ? false : true;
    }

    /**
     * Return the FTP path as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuilder str = new StringBuilder();

        str.append("[");
        str.append(getFTPPath());
        str.append("=");
        str.append(getShareName());
        str.append(",");
        str.append(getSharePath());
        str.append("]");

        return str.toString();
    }
}
