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

import org.alfresco.jlan.oncrpc.RpcAuthenticator;

// TODO: Auto-generated Javadoc
/**
 * The Class NFSConfigBean.
 * 
 * @author dward
 */
public class NFSConfigBean
{

    /** The server enabled. */
    private boolean serverEnabled;

    /** The port mapper enabled. */
    private boolean portMapperEnabled;

    /** The thread pool. */
    private Integer threadPool;

    /** The packet pool. */
    private Integer packetPool;

    /** The port mapper port. */
    private Integer portMapperPort;

    /** The mount server port. */
    private Integer mountServerPort;

    /** The NFS server port. */
    private Integer NFSServerPort;

    /** The debug flags. */
    private String debugFlags;

    /** The mount server debug. */
    private boolean mountServerDebug;

    /** The port mapper debug. */
    private boolean portMapperDebug;

    /** The rpc authenticator. */
    private RpcAuthenticator rpcAuthenticator;

    /**
     * Checks if is server enabled.
     * 
     * @return true, if is server enabled
     */
    public boolean getServerEnabled()
    {
        return serverEnabled;
    }

    /**
     * Sets the server enabled.
     * 
     * @param serverEnabled
     *            the new server enabled
     */
    public void setServerEnabled(boolean serverEnabled)
    {
        this.serverEnabled = serverEnabled;
    }

    /**
     * Checks if is port mapper enabled.
     * 
     * @return true, if is port mapper enabled
     */
    public boolean getPortMapperEnabled()
    {
        return portMapperEnabled;
    }

    /**
     * Sets the port mapper enabled.
     * 
     * @param portMapperEnabled
     *            the new port mapper enabled
     */
    public void setPortMapperEnabled(boolean portMapperEnabled)
    {
        this.portMapperEnabled = portMapperEnabled;
    }

    /**
     * Gets the thread pool.
     * 
     * @return the thread pool
     */
    public Integer getThreadPool()
    {
        return threadPool;
    }

    /**
     * Sets the thread pool.
     * 
     * @param threadPool
     *            the new thread pool
     */
    public void setThreadPool(Integer threadPool)
    {
        this.threadPool = threadPool;
    }

    /**
     * Gets the packet pool.
     * 
     * @return the packet pool
     */
    public Integer getPacketPool()
    {
        return packetPool;
    }

    /**
     * Sets the packet pool.
     * 
     * @param packetPool
     *            the new packet pool
     */
    public void setPacketPool(Integer packetPool)
    {
        this.packetPool = packetPool;
    }

    /**
     * Gets the port mapper port.
     * 
     * @return the port mapper port
     */
    public Integer getPortMapperPort()
    {
        return portMapperPort;
    }

    /**
     * Sets the port mapper port.
     * 
     * @param portMapperPort
     *            the new port mapper port
     */
    public void setPortMapperPort(Integer portMapperPort)
    {
        this.portMapperPort = portMapperPort;
    }

    /**
     * Gets the mount server port.
     * 
     * @return the mount server port
     */
    public Integer getMountServerPort()
    {
        return mountServerPort;
    }

    /**
     * Sets the mount server port.
     * 
     * @param mountServerPort
     *            the new mount server port
     */
    public void setMountServerPort(Integer mountServerPort)
    {
        this.mountServerPort = mountServerPort;
    }

    /**
     * Gets the nFS server port.
     * 
     * @return the nFS server port
     */
    public Integer getNfsServerPort()
    {
        return NFSServerPort;
    }

    /**
     * Sets the nFS server port.
     * 
     * @param serverPort
     *            the new nFS server port
     */
    public void setNfsServerPort(Integer serverPort)
    {
        NFSServerPort = serverPort;
    }

    /**
     * Gets the debug flags.
     * 
     * @return the debug flags
     */
    public String getDebugFlags()
    {
        return debugFlags;
    }

    /**
     * Sets the debug flags.
     * 
     * @param debugFlags
     *            the new debug flags
     */
    public void setDebugFlags(String debugFlags)
    {
        this.debugFlags = debugFlags;
    }

    /**
     * Checks if is mount server debug.
     * 
     * @return true, if is mount server debug
     */
    public boolean getMountServerDebug()
    {
        return mountServerDebug;
    }

    /**
     * Sets the mount server debug.
     * 
     * @param mountServerDebug
     *            the new mount server debug
     */
    public void setMountServerDebug(boolean mountServerDebug)
    {
        this.mountServerDebug = mountServerDebug;
    }

    /**
     * Checks if is port mapper debug.
     * 
     * @return true, if is port mapper debug
     */
    public boolean getPortMapperDebug()
    {
        return portMapperDebug;
    }

    /**
     * Sets the port mapper debug.
     * 
     * @param portMapperDebug
     *            the new port mapper debug
     */
    public void setPortMapperDebug(boolean portMapperDebug)
    {
        this.portMapperDebug = portMapperDebug;
    }

    /**
     * Gets the rpc authenticator.
     * 
     * @return the rpc authenticator
     */
    public RpcAuthenticator getRpcAuthenticator()
    {
        return rpcAuthenticator;
    }

    /**
     * Sets the rpc authenticator.
     * 
     * @param rpcAuthenticator
     *            the new rpc authenticator
     */
    public void setRpcAuthenticator(RpcAuthenticator rpcAuthenticator)
    {
        this.rpcAuthenticator = rpcAuthenticator;
    }

}
