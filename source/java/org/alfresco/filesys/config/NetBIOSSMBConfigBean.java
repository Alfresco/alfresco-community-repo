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
 * The Class NetBIOSSMBConfigBean.
 * 
 * @author dward
 */
public class NetBIOSSMBConfigBean
{

    /** The platforms. */
    private String platforms;

    /** The bind to. */
    private String bindTo;

    /** The session port. */
    private Integer sessionPort;

    /** The name port. */
    private Integer namePort;

    /** The datagram port. */
    private Integer datagramPort;

    /** The adapter. */
    private String adapter;

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
     * Gets the bind to.
     * 
     * @return the bind to
     */
    public String getBindTo()
    {
        return bindTo;
    }

    /**
     * Sets the bind to.
     * 
     * @param bindTo
     *            the new bind to
     */
    public void setBindTo(String bindTo)
    {
        this.bindTo = bindTo;
    }

    /**
     * Gets the session port.
     * 
     * @return the session port
     */
    public Integer getSessionPort()
    {
        return sessionPort;
    }

    /**
     * Sets the session port.
     * 
     * @param sessionPort
     *            the new session port
     */
    public void setSessionPort(Integer sessionPort)
    {
        this.sessionPort = sessionPort;
    }

    /**
     * Gets the name port.
     * 
     * @return the name port
     */
    public Integer getNamePort()
    {
        return namePort;
    }

    /**
     * Sets the name port.
     * 
     * @param namePort
     *            the new name port
     */
    public void setNamePort(Integer namePort)
    {
        this.namePort = namePort;
    }

    /**
     * Gets the datagram port.
     * 
     * @return the datagram port
     */
    public Integer getDatagramPort()
    {
        return datagramPort;
    }

    /**
     * Sets the datagram port.
     * 
     * @param datagramPort
     *            the new datagram port
     */
    public void setDatagramPort(Integer datagramPort)
    {
        this.datagramPort = datagramPort;
    }

    /**
     * Gets the adapter.
     * 
     * @return the adapter
     */
    public String getAdapter()
    {
        return adapter;
    }

    /**
     * Sets the adapter.
     * 
     * @param adapter
     *            the new adapter
     */
    public void setAdapter(String adapter)
    {
        this.adapter = adapter;
    }

}
