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
 * http://www.alfresco.com/legal/licensing
 */

package org.alfresco.repo.avm;

import java.io.Serializable;

import org.alfresco.util.MD5;

/**
 * Implementation of entry for tracking layered nodes which were 
 * snapshotted in a particular Version.
 * @author britt
 */
public class VersionLayeredNodeEntryImpl implements VersionLayeredNodeEntry, Serializable
{
    private static final long serialVersionUID = -5222079271680056311L;

    private VersionRoot fVersion;
    
    private String fMD5Sum;
    
    private String fPath;
    
    public VersionLayeredNodeEntryImpl()
    {
    }

    public VersionLayeredNodeEntryImpl(VersionRoot version,
                                       String path)
    {
        fVersion = version;
        fMD5Sum = MD5.Digest(path.getBytes());
        fPath = path;
    }
    
    public void setPath(String path)
    {
        fPath = path;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.VersionLayeredNodeEntry#getPath()
     */
    public String getPath()
    {
        return fPath;
    }

    public void setVersion(VersionRoot version)
    {
        fVersion = version;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.VersionLayeredNodeEntry#getVersion()
     */
    public VersionRoot getVersion()
    {
        return fVersion;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof VersionLayeredNodeEntry))
        {
            return false;
        }
        VersionLayeredNodeEntry other = (VersionLayeredNodeEntry)obj;
        return fVersion.equals(other.getVersion()) && 
               fMD5Sum.equals(other.getMd5Sum());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return fVersion.hashCode() + fMD5Sum.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[VersionLayeredNodeEntry:");
        builder.append(fVersion.toString());
        builder.append(':');
        builder.append(fPath);
        builder.append(']');
        return builder.toString();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.VersionLayeredNodeEntry#getMd5Sum()
     */
    public String getMd5Sum()
    {
        return fMD5Sum;
    }
    
    public void setMd5Sum(String sum)
    {
        fMD5Sum = sum;
    }
}
