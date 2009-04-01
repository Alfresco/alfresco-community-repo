/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.filesys.config;

// TODO: Auto-generated Javadoc
/**
 * The Class TcpipSMBConfigBean.
 * 
 * @author dward
 */
public class TcpipSMBConfigBean
{

    /** The platforms. */
    private String platforms;

    /** The port. */
    private Integer port;

    /** The ipv6 enabled. */
    private boolean ipv6Enabled;

    /**
     * Gets the platforms.
     * 
     * @return the platforms
     */
    public String getPlatforms()
    {
        return platforms;
    }

    /**
     * Sets the platforms.
     * 
     * @param platforms
     *            the new platforms
     */
    public void setPlatforms(String platforms)
    {
        this.platforms = platforms;
    }

    /**
     * Gets the port.
     * 
     * @return the port
     */
    public Integer getPort()
    {
        return port;
    }

    /**
     * Sets the port.
     * 
     * @param port
     *            the new port
     */
    public void setPort(Integer port)
    {
        this.port = port;
    }

    /**
     * Checks if is ipv6 enabled.
     * 
     * @return true, if is ipv6 enabled
     */
    public boolean getIpv6Enabled()
    {
        return ipv6Enabled;
    }

    /**
     * Sets the ipv6 enabled.
     * 
     * @param ipv6Enabled
     *            the new ipv6 enabled
     */
    public void setIpv6Enabled(boolean ipv6Enabled)
    {
        this.ipv6Enabled = ipv6Enabled;
    }

}
