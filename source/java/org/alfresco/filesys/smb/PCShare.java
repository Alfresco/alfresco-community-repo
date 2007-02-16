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
package org.alfresco.filesys.smb;

/**
 * PC share class.
 * <p>
 * The PC share class holds the details of a network share, including the required username and
 * password access control.
 */
public final class PCShare
{

    // Domain name

    private String m_domain = null;

    // Node name string.

    private String m_nodename = null;

    // Remote share name string.

    private String m_shrname = null;

    // User name access control string.

    private String m_username = null;

    // Password access control string.

    private String m_password = null;

    // Remote path, relative to the share.

    private String m_path = null;

    // File name string.

    private String m_fname = null;

    // Primary and secondary protocols to try connection on

    private int m_primaryProto = Protocol.UseDefault;
    private int m_secondaryProto = Protocol.UseDefault;

    // Extended security negotiation flags
    
    private int m_extendedSecFlags;
    
    /**
     * Construct an empty PCShare object.
     */
    public PCShare()
    {
    }

    /**
     * Construct a PCShare using the supplied UNC path.
     * 
     * @param netpath Network path of the remote server, in UNC format ie. \\node\\share.
     * @exception InvalidUNCPathException If the network path is invalid.
     */
    public PCShare(String netpath) throws InvalidUNCPathException
    {
        setNetworkPath(netpath);

        // If the user name has not been set, use the guest account

        if (m_username == null)
            setUserName("GUEST");
    }

    /**
     * Construct a PCShare using the specified remote server and access control details.
     * 
     * @param nname Node name of the remote server.
     * @param shr Share name on the remote server.
     * @param uname User name used to access the remote share.
     * @param pwd Password used to access the remote share.
     */
    public PCShare(String nname, String shr, String uname, String pwd)
    {
        setNodeName(nname);
        setShareName(shr);
        setUserName(uname);
        setPassword(pwd);
    }

    /**
     * Build a share relative path using the supplied working directory and file name.
     * 
     * @param workdir Working directory string, relative to the root of the share.
     * @param fname File name string.
     * @return Share relative path string.
     */
    public static String makePath(String workdir, String fname)
    {

        // Create a string buffer to build the share relative path

        StringBuffer pathStr = new StringBuffer();

        // Make sure there is a leading '\' on the path string

        if (!workdir.startsWith("\\"))
            pathStr.append("\\");
        pathStr.append(workdir);

        // Make sure the path ends with '\'

        if (pathStr.charAt(pathStr.length() - 1) != '\\')
            pathStr.append("\\");

        // Add the file name to the path string

        pathStr.append(fname);

        // Return share relative the path string

        return pathStr.toString();
    }

    /**
     * Return the domain for the share.
     * 
     * @return java.lang.String
     */
    public final String getDomain()
    {
        return m_domain;
    }

    /**
     * Determine if extended security flags have been set
     * 
     * @return boolean
     */
    public final boolean hasExtendedSecurityFlags()
    {
        return m_extendedSecFlags != 0 ? true : false;
    }
    
    /**
     * Return the extended security flags
     * 
     * @return int
     */
    public final int getExtendedSecurityFlags()
    {
        return m_extendedSecFlags;
    }
    
    /**
     * Get the remote file name string.
     * 
     * @return Remote file name string.
     */
    public final String getFileName()
    {
        return m_fname;
    }

    /**
     * Return the full UNC path for this PC share object.
     * 
     * @return Path string of the remote share/path/file in UNC format, ie. \\node\share\path\file.
     */
    public final String getNetworkPath()
    {

        // Create a string buffer to build up the full network path

        StringBuffer strBuf = new StringBuffer(128);

        // Add the node name and share name

        strBuf.append("\\\\");
        strBuf.append(getNodeName());
        strBuf.append("\\");
        strBuf.append(getShareName());

        // Add the path, if there is one

        if (getPath() != null && getPath().length() > 0)
        {
            if (getPath().charAt(0) != '\\')
            {
                strBuf.append("\\");
            }
            strBuf.append(getPath());
        }

        // Add the file name if there is one

        if (getFileName() != null && getFileName().length() > 0)
        {
            if (strBuf.charAt(strBuf.length() - 1) != '\\')
            {
                strBuf.append("\\");
            }
            strBuf.append(getFileName());
        }

        // Return the network path

        return strBuf.toString();
    }

    /**
     * Get the remote node name string.
     * 
     * @return Node name string.
     */
    public final String getNodeName()
    {
        return m_nodename;
    }

    /**
     * Get the remote password required to access the remote share.
     * 
     * @return Remote password string.
     */
    public final String getPassword()
    {
        return m_password;
    }

    /**
     * Get the share relative path string.
     * 
     * @return Share relative path string.
     */
    public final String getPath()
    {
        return m_path != null ? m_path : "\\";
    }

    /**
     * Return the share relative path for this PC share object.
     * 
     * @return Path string of the remote share/path/file relative to the share, ie. \path\file.
     */
    public final String getRelativePath()
    {

        // Create a string buffer to build up the full network path

        StringBuffer strBuf = new StringBuffer(128);

        // Add the path, if there is one

        if (getPath().length() > 0)
        {
            if (getPath().charAt(0) != '\\')
            {
                strBuf.append("\\");
            }
            strBuf.append(getPath());
        }

        // Add the file name if there is one

        if (getFileName().length() > 0)
        {
            if (strBuf.charAt(strBuf.length() - 1) != '\\')
            {
                strBuf.append("\\");
            }
            strBuf.append(getFileName());
        }

        // Return the network path

        return strBuf.toString();
    }

    /**
     * Get the remote share name string.
     * 
     * @return Remote share name string.
     */

    public final String getShareName()
    {
        return m_shrname;
    }

    /**
     * Get the remote user name string.
     * 
     * @return Remote user name string required to access the remote share.
     */

    public final String getUserName()
    {
        return m_username != null ? m_username : "";
    }

    /**
     * Get the primary protocol to connect with
     * 
     * @return int
     */
    public final int getPrimaryProtocol()
    {
        return m_primaryProto;
    }

    /**
     * Get the secondary protocol to connect with
     * 
     * @return int
     */
    public final int getSecondaryProtocol()
    {
        return m_secondaryProto;
    }

    /**
     * Determine if the share has a domain specified.
     * 
     * @return boolean
     */
    public final boolean hasDomain()
    {
        return m_domain == null ? false : true;
    }

    /**
     * Set the domain to be used during the session setup.
     * 
     * @param domain java.lang.String
     */
    public final void setDomain(String domain)
    {
        m_domain = domain;
        if (m_domain != null)
            m_domain = m_domain.toUpperCase();
    }

    /**
     * Set the remote file name string.
     * 
     * @param fn Remote file name string.
     */

    public final void setFileName(String fn)
    {
        m_fname = fn;
    }

    /**
     * Set the PC share from the supplied UNC path string.
     * 
     * @param netpath UNC format remote file path.
     */

    public final void setNetworkPath(String netpath) throws InvalidUNCPathException
    {

        // Take a copy of the network path

        StringBuffer path = new StringBuffer(netpath);
        for (int i = 0; i < path.length(); i++)
        {

            // Convert forward slashes to back slashes

            if (path.charAt(i) == '/')
                path.setCharAt(i, '\\');
        }
        String npath = path.toString();

        // UNC path starts with '\\'

        if (!npath.startsWith("\\\\") || npath.length() < 5)
            throw new InvalidUNCPathException(npath);

        // Extract the node name from the network path

        int pos = 2;
        int endpos = npath.indexOf("\\", pos);

        if (endpos == -1)
            throw new InvalidUNCPathException(npath);

        setNodeName(npath.substring(pos, endpos));
        pos = endpos + 1;

        // Extract the share name from the network path

        endpos = npath.indexOf("\\", pos);

        if (endpos == -1)
        {

            // Share name is the last part of the UNC path

            setShareName(npath.substring(pos));

            // Set the root path and clear the file name

            setPath("\\");
            setFileName("");
        }
        else
        {
            setShareName(npath.substring(pos, endpos));

            pos = endpos + 1;

            // Extract the share relative path from the network path

            endpos = npath.lastIndexOf("\\");

            if (endpos != -1 && endpos > pos)
            {

                // Set the share relative path, and update the current position index

                setPath(npath.substring(pos, endpos));

                // File name is the rest of the UNC path

                setFileName(npath.substring(endpos + 1));
            }
            else
            {

                // Set the share relative path to the root path

                setPath("\\");

                // Set the file name string

                if (npath.length() > pos)
                    setFileName(npath.substring(pos));
                else
                    setFileName("");
            }
        }

        // Check if the share name contains embedded access control

        pos = m_shrname.indexOf("%");
        if (pos != -1)
        {

            // Find the end of the user name

            endpos = m_shrname.indexOf(":", pos);
            if (endpos != -1)
            {

                // Extract the user name and password strings

                setUserName(m_shrname.substring(pos + 1, endpos));
                setPassword(m_shrname.substring(endpos + 1));
            }
            else
            {

                // Extract the user name string

                setUserName(m_shrname.substring(pos + 1));
            }

            // Reset the share name string, to remove the access control

            setShareName(m_shrname.substring(0, pos));
        }

        // Check if the path has been set, if not then use the root path

        if (m_path == null || m_path.length() == 0)
            m_path = "\\";
    }

    /**
     * Set the extended security negotiation flags
     * 
     * @param extFlags int
     */
    public final void setExtendedSecurityFlags(int extFlags)
    {
        m_extendedSecFlags = extFlags;
    }
    
    /**
     * Set the remote node name string.
     * 
     * @param nname Remote node name string.
     */

    public final void setNodeName(String nname)
    {
        m_nodename = nname;
    }

    /**
     * Set the remote password string.
     * 
     * @param pwd Remote password string, required to access the remote share.
     */

    public final void setPassword(String pwd)
    {
        m_password = pwd;
    }

    /**
     * Set the share relative path string.
     * 
     * @param pth Share relative path string.
     */

    public final void setPath(String pth)
    {
        m_path = pth;
    }

    /**
     * Set the remote share name string.
     * 
     * @param shr Remote share name string.
     */

    public final void setShareName(String shr)
    {
        m_shrname = shr;
    }

    /**
     * Set the remote user name string.
     * 
     * @param uname Remote user name string.
     */

    public final void setUserName(String uname)
    {
        m_username = uname;
    }

    /**
     * Set the primary and secondary protocol order that is used to connect to the remote host.
     * 
     * @param pri int
     * @param sec int
     */
    public final void setProtocolOrder(int pri, int sec)
    {
        m_primaryProto = pri;
        m_secondaryProto = sec;
    }

    /**
     * Return the PCShare object as a string
     * 
     * @return PCShare string.
     */

    public final String toString()
    {
        return getNetworkPath();
    }
}