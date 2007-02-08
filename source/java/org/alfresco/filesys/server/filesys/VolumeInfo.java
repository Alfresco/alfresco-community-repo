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

import java.util.Date;

/**
 * Disk Volume Information Class
 */
public class VolumeInfo
{

    // Volume label

    private String m_label;

    // Serial number

    private int m_serno = -1;

    // Creation date/time

    private Date m_created;

    /**
     * Default constructor
     */
    public VolumeInfo()
    {
    }

    /**
     * Class constructor
     * 
     * @param label String
     */
    public VolumeInfo(String label)
    {
        setVolumeLabel(label);
    }

    /**
     * Class constructor
     * 
     * @param label String
     * @param serno int
     * @param created Date
     */
    public VolumeInfo(String label, int serno, Date created)
    {
        setVolumeLabel(label);
        setSerialNumber(serno);
        setCreationDateTime(created);
    }

    /**
     * Return the volume label
     * 
     * @return String
     */
    public final String getVolumeLabel()
    {
        return m_label;
    }

    /**
     * Determine if the serial number is valid
     * 
     * @return boolean
     */
    public final boolean hasSerialNumber()
    {
        return m_serno != -1 ? true : false;
    }

    /**
     * Return the serial number
     * 
     * @return int
     */
    public final int getSerialNumber()
    {
        return m_serno;
    }

    /**
     * Determine if the creation date/time is valid
     * 
     * @return boolean
     */
    public final boolean hasCreationDateTime()
    {
        return m_created != null ? true : false;
    }

    /**
     * Return the volume creation date/time
     * 
     * @return Date
     */
    public final Date getCreationDateTime()
    {
        return m_created;
    }

    /**
     * Set the volume label
     * 
     * @param label
     */
    public final void setVolumeLabel(String label)
    {
        m_label = label;
    }

    /**
     * Set the serial number
     * 
     * @param serno int
     */
    public final void setSerialNumber(int serno)
    {
        m_serno = serno;
    }

    /**
     * Set the volume creation date/time
     * 
     * @param created Date
     */
    public final void setCreationDateTime(Date created)
    {
        m_created = created;
    }

    /**
     * Return the volume information as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();

        str.append("[");
        str.append(getVolumeLabel());
        str.append(",");
        str.append(getSerialNumber());
        str.append(",");
        str.append(getCreationDateTime());
        str.append("]");

        return str.toString();
    }
}
