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
