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
package org.alfresco.web.bean.jmx;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

import javax.faces.context.FacesContext;
import javax.management.MBeanServerConnection;

import org.alfresco.repo.management.JmxDumpUtil;
import org.alfresco.web.app.servlet.FacesHelper;

/**
 * Backing bean to allow an admin user to dump all JMX Beans and their properties.
 */
public class JmxDumperBean implements Serializable
{

    private static final long serialVersionUID = -8690237774052781181L;

    // supporting repository services

    /** The MBean server. */
    transient private MBeanServerConnection mbeanServer;

    /**
     * Sets the mbean server.
     * 
     * @param mbeanServer
     *            the mbeanServer to set
     */
    public void setMbeanServer(MBeanServerConnection mbeanServer)
    {
        this.mbeanServer = mbeanServer;
    }

    /**
     * Gets the mbean server.
     * 
     * @return the mbeanServer
     */
    private MBeanServerConnection getMbeanServer()
    {
        if (this.mbeanServer == null)
        {
            this.mbeanServer = (MBeanServerConnection) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(),
                    "alfrescoMBeanServer");
        }
        return this.mbeanServer;
    }

    /**
     * Gets the command result.
     * 
     * @return the result
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public String getResult() throws IOException
    {
        StringWriter result = new StringWriter();
        PrintWriter out = new PrintWriter(result);
        JmxDumpUtil.dumpConnection(getMbeanServer(), out);
        out.close();
        return result.toString();
    }
}
