/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.filesys.repo;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Cache Lookup Search Context Class
 * 
 * ContentSearchContext with DotDot methods.
 * 
 */
public class DotDotContentSearchContext extends ContentSearchContext
{

    // Debug logging

    private static final Log logger = LogFactory.getLog(DotDotContentSearchContext.class);

    // File information for the '.' and '..' pseduo entries, returned during a wildcard search

    private FileInfo m_dotInfo;
    private FileInfo m_dotDotInfo;

    /**
     * Class constructor
     * 
     * @param cifsHelper
     *            Filesystem helper class
     * @param results
     *            List of file/folder nodes that match the search pattern
     * @param searchStr
     *            Search path
     * @param relPath
     *            Relative path being searched
     * @param lockedFilesAsOffline
     *            set state
     */
    protected DotDotContentSearchContext(
            CifsHelper cifsHelper,
            List<NodeRef> results,
            String searchStr,
            String relPath,
            boolean lockedFilesAsOffline)

    {
        super(cifsHelper, results, searchStr, relPath, lockedFilesAsOffline);
        super.setSearchString(searchStr);
    }

    /**
     * Check if the '.' and '..' pseudo file entries are available
     * 
     * @return boolean
     */
    public boolean hasDotFiles()
    {
        return (m_dotInfo != null && m_dotDotInfo != null) ? true : false;
    }

    /**
     * Return the '.' pseudo file entry details
     * 
     * @param finfo
     *            FileInfo
     * @return boolean
     */
    public boolean getDotInfo(FileInfo finfo)
    {

        // Check if the '.' file information is valid

        if (m_dotInfo != null)
        {
            finfo.copyFrom(m_dotInfo);
            return true;
        }

        // File information not valid

        return false;
    }

    /**
     * Return the '..' pseudo file entry details
     * 
     * @param finfo
     *            FileInfo
     * @return boolean
     */
    public boolean getDotDotInfo(FileInfo finfo)
    {

        // Check if the '..' file information is valid

        if (m_dotDotInfo != null)
        {
            finfo.copyFrom(m_dotDotInfo);
            return true;
        }

        // File information not valid

        return false;
    }

    /**
     * Set the '.' pseudo file entry details
     * 
     * @param finfo
     *            FileInfo
     */
    protected void setDotInfo(FileInfo finfo)
    {
        m_dotInfo = finfo;
        if (m_dotInfo != null)
            m_dotInfo.setFileName(".");
    }

    /**
     * Set the '..' pseudo file entry details
     * 
     * @param finfo
     *            FileInfo
     */
    protected void setDotDotInfo(FileInfo finfo)
    {
        m_dotDotInfo = finfo;
        if (m_dotDotInfo != null)
            m_dotDotInfo.setFileName("..");
    }

    /**
     * Return the search as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(60);

        sb.append("[DotDotContextSearch searchStr=");
        sb.append(getSearchString());
        sb.append(", resultCount=");
        sb.append(getResultsSize());
        if (m_dotInfo != null)
            sb.append(",Dot");
        if (m_dotDotInfo != null)
            sb.append(",DotDot");
        sb.append("]");

        return sb.toString();
    }
}
