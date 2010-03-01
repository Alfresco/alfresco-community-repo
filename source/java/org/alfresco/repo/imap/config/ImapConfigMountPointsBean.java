/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.imap.config;

import org.alfresco.repo.imap.AlfrescoImapConst.ImapViewMode;
import org.alfresco.util.config.RepositoryFolderConfigBean;

/**
 * Provides the parameters for an IMAP mount point (a mapping from an Alfresco node path to an IMAP folder name).
 * 
 * @since 3.2
 */
public class ImapConfigMountPointsBean extends RepositoryFolderConfigBean
{
    private String mountPointName;
    private ImapViewMode mode;

    /**
     * Gets the IMAP mount-point name.
     * 
     * @return the IMAP folder name
     */
    public String getMountPointName()
    {
        return this.mountPointName;
    }

    /**
     * @param folderName        the name of the IMAP folder
     */
    public void setMountPointName(String folderName)
    {
        this.mountPointName = folderName;
    }

    /**
     * Gets the mode.
     * 
     * @return the mode (virtual, mixed or archive)
     */
    public ImapViewMode getMode()
    {
        return this.mode;
    }

    /**
     * @return          Returns the string value of the mode
     */
    public String getModeName()
    {
        return mode == null ? null : mode.toString();
    }

    /**
     * Sets the mode.
     * 
     * @param mode
     *            the new mode (virtual or archive)
     * @see MountPointMode
     */
    public void setModeName(String mode)
    {
        this.mode = ImapViewMode.valueOf(mode);
    }

}
