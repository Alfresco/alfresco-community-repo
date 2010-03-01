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
