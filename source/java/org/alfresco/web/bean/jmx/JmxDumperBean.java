/*
 * #%L
 * Alfresco Repository WAR Community
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
