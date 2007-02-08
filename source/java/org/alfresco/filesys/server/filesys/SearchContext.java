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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.filesys.server.filesys;

/**
 * <p>
 * The search context represents the state of an active search by a disk interface based class. The
 * context is used to continue a search across multiple requests.
 */
public abstract class SearchContext
{

    // Maximum number of files to return per search request.

    private int m_maxFiles;

    // Tree identifier that this search is associated with

    private int m_treeId;

    // Search string

    private String m_searchStr;

    // Flags

    private int m_flags;

    /**
     * Default constructor.
     */
    public SearchContext()
    {
    }

    /**
     * Construct a new search context.
     * 
     * @param maxFiles int
     * @param treeId int
     */
    protected SearchContext(int maxFiles, int treeId)
    {
        m_maxFiles = maxFiles;
        m_treeId = treeId;
    }

    /**
     * Close the search.
     */
    public void closeSearch()
    {
    }

    /**
     * Return the search context flags.
     * 
     * @return int
     */
    public final int getFlags()
    {
        return m_flags;
    }

    /**
     * Return the maximum number of files that should be returned per search request.
     * 
     * @return int
     */
    public final int getMaximumFiles()
    {
        return m_maxFiles;
    }

    /**
     * Return the resume id for the current file/directory in the search.
     * 
     * @return int
     */
    public abstract int getResumeId();

    /**
     * Return the search string, used for resume keys in some SMB dialects.
     * 
     * @return java.lang.String
     */
    public final String getSearchString()
    {
        return m_searchStr != null ? m_searchStr : "";
    }

    /**
     * Return the tree identifier of the tree connection that this search is associated with.
     * 
     * @return int
     */
    public final int getTreeId()
    {
        return m_treeId;
    }

    /**
     * Determine if there are more files for the active search.
     * 
     * @return boolean
     */
    public abstract boolean hasMoreFiles();

    /**
     * Return file information for the next file in the active search. Returns false if the search
     * is complete.
     * 
     * @param info FileInfo to return the file information.
     * @return true if the file information is valid, else false
     */
    public abstract boolean nextFileInfo(FileInfo info);

    /**
     * Return the file name of the next file in the active search. Returns null is the search is
     * complete.
     * 
     * @return java.lang.String
     */
    public abstract String nextFileName();

    /**
     * Return the total number of file entries for this search if known, else return -1
     * 
     * @return int
     */
    public int numberOfEntries()
    {
        return -1;
    }

    /**
     * Restart a search at the specified resume point.
     * 
     * @param resumeId Resume point id.
     * @return true if the search can be restarted, else false.
     */
    public abstract boolean restartAt(int resumeId);

    /**
     * Restart the current search at the specified file.
     * 
     * @param info File to restart the search at.
     * @return true if the search can be restarted, else false.
     */
    public abstract boolean restartAt(FileInfo info);

    /**
     * Set the search context flags.
     * 
     * @param flg int
     */
    public final void setFlags(int flg)
    {
        m_flags = flg;
    }

    /**
     * Set the maximum files to return per request packet.
     * 
     * @param maxFiles int
     */
    public final void setMaximumFiles(int maxFiles)
    {
        m_maxFiles = maxFiles;
    }

    /**
     * Set the search string.
     * 
     * @param str java.lang.String
     */
    public final void setSearchString(String str)
    {
        m_searchStr = str;
    }

    /**
     * Set the tree connection id that the search is associated with.
     * 
     * @param id int
     */
    public final void setTreeId(int id)
    {
        m_treeId = id;
    }

    /**
     * Return the search context as a string.
     * 
     * @return java.lang.String
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();
        str.append("[");
        str.append(getSearchString());
        str.append(":");
        str.append(getMaximumFiles());
        str.append(",");
        str.append("0x");
        str.append(Integer.toHexString(getFlags()));
        str.append("]");

        return str.toString();
    }
}