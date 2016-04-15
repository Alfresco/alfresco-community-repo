/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.jscript;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.repo.nodelocator.SharedHomeNodeLocator;
import org.alfresco.repo.nodelocator.SitesHomeNodeLocator;
import org.alfresco.repo.nodelocator.UserHomeNodeLocator;
import org.alfresco.repo.nodelocator.XPathNodeLocator;
import org.alfresco.repo.security.permissions.noop.PermissionServiceNOOPImpl;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
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
public class ScriptUtils extends BaseScopableProcessorExtension
{
    private final static String NAMESPACE_BEGIN = "" + QName.NAMESPACE_BEGIN;
    
    /** Services */
    protected ServiceRegistry services;
    
    private NodeService unprotNodeService;
    
    
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
     * @param nodeService   the NodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.unprotNodeService = nodeService;
    }
    
    /**
     * Function to return the cm:name display path for a node with minimum performance overhead.
     *  
     * @param node  ScriptNode
     * @return cm:name based human readable display path for the give node.
     */
    public String displayPath(ScriptNode node)
    {
        return this.unprotNodeService.getPath(node.nodeRef).toDisplayPath(
                    this.unprotNodeService, new PermissionServiceNOOPImpl());
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
     * Use the Node Locator Service to find the a node reference from a number of possible locator types.
     * This method is responsible for determining the locator type and then calling the Service as the
     * Service does not know how to guess which locator to use.
     * <p>
     * This service supports 'virtual' nodes including the following:
     * <p>
     * alfresco://company/home      The Company Home root node<br>
     * alfresco://user/home         The User Home node under Company Home<br>
     * alfresco://company/shared    The Shared node under Company Home<br>
     * alfresco://sites/home        The Sites home node under Company Home<br>
     * workspace://.../...          Any standard NodeRef<br>
     * /app:company_home/cm:...     XPath QName style node reference<br>
     * 
     * @param reference     The node reference - See above for list of possible node references supported.
     * 
     * @return ScriptNode representing the node or null if not found
     */
    public ScriptNode resolveNodeReference(final String reference)
    {
        if (reference == null)
        {
            throw new IllegalArgumentException("Node 'reference' argument is mandatory.");
        }
        
        final NodeLocatorService locatorService = this.services.getNodeLocatorService();
        
        NodeRef nodeRef = null;
        
        switch (reference)
        {
            case "alfresco://company/home":
                nodeRef = locatorService.getNode(CompanyHomeNodeLocator.NAME, null, null);
                break;
            case "alfresco://user/home":
                nodeRef = locatorService.getNode(UserHomeNodeLocator.NAME, null, null);
                break;
            case "alfresco://company/shared":
                nodeRef = locatorService.getNode(SharedHomeNodeLocator.NAME, null, null);
                break;
            case "alfresco://sites/home":
                nodeRef = locatorService.getNode(SitesHomeNodeLocator.NAME, null, null);
                break;
            default:
                if (reference.indexOf("://") > 0)
                {
                    NodeRef ref = new NodeRef(reference);
                    if (this.services.getNodeService().exists(ref) && 
                        this.services.getPermissionService().hasPermission(ref, PermissionService.READ) == AccessStatus.ALLOWED)
                    {
                        nodeRef = ref;
                    }
                }
                else if (reference.startsWith("/"))
                {
                    final Map<String, Serializable> params = new HashMap<>(1, 1.0f);
                    params.put(XPathNodeLocator.QUERY_KEY, reference);
                    nodeRef = locatorService.getNode(XPathNodeLocator.NAME, null, params);
                }
                break;
        }
        
        return nodeRef != null ? (ScriptNode)new ValueConverter().convertValueForScript(this.services, getScope(), null, nodeRef) : null;
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
     * @param timeInMillis long
     * @return String
     */
    public String toISO8601(long timeInMillis)
    {
        return ISO8601DateFormat.format(new Date(timeInMillis));
    }
    
    /**
     * Format date to ISO 8601 formatted string
     * 
     * @param date Date
     * @return String
     */
    public String toISO8601(Date date)
    {
        return ISO8601DateFormat.format(date);
    }
    
    /**
     * Parse date from ISO formatted string
     * 
     * @param isoDateString String
     * @return Date
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
     *  
     * @param args  Mandatory hash of paging arguments<p>
     *              Possible arguments include:<p>
     *              maxItems - max count of items to return, default -1 (all)<br>
     *              skipCount - number of items to skip, default -1 (none)<br>
     *              queryId<br>
     *              queryExecutionId
     */
    public ScriptPagingDetails createPaging(Map<String, String> args)
    {
        int maxItems = -1;
        int skipCount = -1;
        String queryId = null;
        
        if (args.containsKey("maxItems"))
        {
            try
            {
                maxItems = Integer.parseInt(args.get("maxItems"));
            }
            catch(NumberFormatException e)
            {}
        }
        if (args.containsKey("skipCount"))
        {
            try
            {
                skipCount = Integer.parseInt(args.get("skipCount"));
            }
            catch(NumberFormatException e)
            {}
        }
        
        if (args.containsKey("queryId"))
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
