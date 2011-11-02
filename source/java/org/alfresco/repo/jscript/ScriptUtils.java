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
package org.alfresco.repo.jscript;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ScriptPagingDetails;
import org.springframework.extensions.surf.util.I18NUtil;
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
     * Given a short-form QName string, this method returns the fully qualified QName string.
     * 
     * @param s   Short form QName string, e.g. "cm:content"
     * @return Fully qualified QName string
     */
    public String longQName(String s)
    {
        return createQName(s).toString();
    }
    
    /**
     * Builds a paging object, from the supplied
     *  Max Items and Skip Count
     */
    public ScriptPagingDetails createPaging(int maxItems, int skipCount)
    {
        return new ScriptPagingDetails(maxItems, skipCount);
    }

    /**
     * Builds a paging object, from the supplied
     *  Max Items, Skip Count and Query Execution ID
     */
    public ScriptPagingDetails createPaging(int maxItems, int skipCount, String queryExecutionId)
    {
        return new ScriptPagingDetails(maxItems, skipCount, queryExecutionId);
    }

    /**
     * Builds a paging object, from the supplied Args object.
     * Requires that the parameters have their standard names,
     *  i.e. "maxItems" and "skipCount"
     */
    public ScriptPagingDetails createPaging(Map<String, String> args)
    {
        int maxItems = -1;
        int skipCount = -1;
        String queryId = null;
        
        if(args.containsKey("maxItems"))
        {
            try
            {
                maxItems = Integer.parseInt(args.get("maxItems"));
            }
            catch(NumberFormatException e)
            {}
        }
        if(args.containsKey("skipCount"))
        {
            try
            {
                skipCount = Integer.parseInt(args.get("skipCount"));
            }
            catch(NumberFormatException e)
            {}
        }
        
        if(args.containsKey("queryId"))
        {
            queryId = args.get("queryId");
        }
        else if(args.containsKey("queryExecutionId"))
        {
            queryId = args.get("queryExecutionId");
        }
        
        return new ScriptPagingDetails(maxItems, skipCount, queryId);
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
    
    /**
     * Get a localized message string, parameterized using standard MessageFormatter.
     * 
     * @param messageKey message key
     * @param params format parameters
     * @return the localized string, null if not found
     */
    public String toLocalizedString(String messageId, Object... params)
    {
        return I18NUtil.getMessage(messageId, params);
    }
    
    /**
     * Disable rule execution for this thread
     */
    public void disableRules()
    {
        services.getRuleService().disableRules();
    }
    
    /**
     * Enable rule execution for this thread
     */
    public void enableRules()
    {
        services.getRuleService().enableRules();
    }
    
    /**
     * Sets current Locale from string
     */
    public void setLocale(String localeStr)
    {
        Locale newLocale = I18NUtil.parseLocale(localeStr);
        I18NUtil.setLocale(newLocale);
    }
    
    /**
     * Returns current thread's locale
     */
    public String getLocale()
    {
        return I18NUtil.getLocale().toString();
    }
}
