/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.admin;

import java.util.Enumeration;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.jmx.HierarchyDynamicMBean;
import org.apache.log4j.spi.LoggerRepository;

/**
 * Initialises Log4j's HierarchyDynamicMBean (refer to core-services-context.xml)
 *
 */
public class Log4JHierarchyInit
{
	private HierarchyDynamicMBean log4jHierarchy;
	
	public void setLog4jHierarchy(HierarchyDynamicMBean log4jHierarchy)
	{
		this.log4jHierarchy = log4jHierarchy;
	}
	
	@SuppressWarnings("unchecked")
	public void init()
	{
	    // Add each logger (that has a level set) from the Log4J Repository to the Hierarchy MBean	    
	    LoggerRepository r = LogManager.getLoggerRepository();
	
	    Enumeration loggers = r.getCurrentLoggers();
	    Logger logger = null;
	   
	    while (loggers.hasMoreElements())
	    {
	    	logger = (Logger)loggers.nextElement();
	    	if (logger.getLevel() != null)
	    	{
	    		log4jHierarchy.addLoggerMBean(logger.getName());
	    	}
	    }
	}
}
