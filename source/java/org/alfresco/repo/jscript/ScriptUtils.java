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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.jscript;

import java.util.Date;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.ISO8601DateFormat;

/**
 * Place for general and miscellaneous utility functions not already found in generic JavaScript. 
 * 
 * @author Kevin Roast
 */
public final class ScriptUtils extends BaseScopableProcessorExtension
{
    private final static String NAMESPACE_BEGIN = "" + QName.NAMESPACE_BEGIN;

    /** Services */
    private ServiceRegistry services;
    
    /**
     * Sets the service registry
     * 
     * @param services  the service registry
     */
    public void setServiceRegistry(ServiceRegistry services)
    {
        this.services = services;
    }
    
    /**
     * Function to pad a string with zero '0' characters to the required length
     * 
     * @param s     String to pad with leading zero '0' characters
     * @param len   Length to pad to
     * 
     * @return padded string or the original if already at >=len characters 
     */
    public String pad(String s, int len)
    {
       String result = s;
       for (int i=0; i<(len - s.length()); i++)
       {
           result = "0" + result;
       }
       return result;
    }
    
    /**
     * Gets a JS node object from a string noderef
     * 
     * @param nodeRefString     string reference to a node
     * @return                  a JS node object
     */
    public ScriptNode getNodeFromString(String nodeRefString)
    {
        NodeRef nodeRef = new NodeRef(nodeRefString);
        return (ScriptNode)new ValueConverter().convertValueForScript(this.services, getScope(), null, nodeRef);
    }
    
    /**
     * Gets a boolean value from a string
     * 
     * @see Boolean#parseBoolean(String)
     * 
     * @param booleanString  boolean string 
     * @return boolean      the boolean value
     */
    public boolean toBoolean(String booleanString)
    {
        return Boolean.parseBoolean(booleanString);
    }
    
    /**
     * Function to check if a module is installed
     * 
     * @param moduleName	module name (e.g. "org.alfresco.module.foo")
     * @return boolean      true if the module is currently installed
     */
    public boolean moduleInstalled(String moduleName)
    {
        ModuleService moduleService = (ModuleService)this.services.getService(QName.createQName(NamespaceService.ALFRESCO_URI, "ModuleService"));
        if (moduleService != null)
        {
            ModuleDetails moduleDetail = (ModuleDetails)moduleService.getModule(moduleName);
            return (moduleDetail != null);
        }
        return false;
    }
    
    /**
     * Format timeInMillis to ISO 8601 formatted string
     * 
     * @param timeInMillis
     * @return
     */
    public String toISO8601(long timeInMillis)
    {
        return ISO8601DateFormat.format(new Date(timeInMillis));
    }
    
    /**
     * Format date to ISO 8601 formatted string
     * 
     * @param date
     * @return
     */
    public String toISO8601(Date date)
    {
        return ISO8601DateFormat.format(date);
    }
    
    /**
     * Parse date from ISO formatted string
     * 
     * @param isoDateString
     * @return
     */
    public Date fromISO8601(String isoDateString)
    {
        return ISO8601DateFormat.parse(isoDateString);
    }
    
    /**
     * Given a long-form QName string, this method uses the namespace service to create a
     * short-form QName string.
     * 
     * @param s   Fully qualified QName string
     * @return the short form of the QName string, e.g. "cm:content"
     */
    public String shortQName(String s)
    {
        return createQName(s).toPrefixString(services.getNamespaceService());
    }

    /**
     * Helper to create a QName from either a fully qualified or short-name QName string
     * 
     * @param s    Fully qualified or short-name QName string
     * 
     * @return QName
     */
    private QName createQName(String s)
    {
        QName qname;
        if (s.indexOf(NAMESPACE_BEGIN) != -1)
        {
            qname = QName.createQName(s);
        }
        else
        {
            qname = QName.createQName(s, this.services.getNamespaceService());
        }
        return qname;
    }
}
