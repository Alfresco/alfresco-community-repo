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
package org.alfresco.repo.module;

import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Module component that logs a message on startup.  The log category
 * used will be the ID of the module that contains the component and the
 * name of the component itself.  For example:
 * <pre>
 * log4j.logger.org.alfresco.modules.MyModule.DumpMessageComponent=INFO
 * </pre>
 * 
 * @author Derek Hulley
 * @since 2.1
 */
public class LoggerModuleComponent extends AbstractModuleComponent
{
    private enum LogLevel
    {
        INFO, WARN, ERROR;
    }
    
    private LogLevel logLevel;
    private String message;

    public LoggerModuleComponent()
    {
        logLevel = LogLevel.INFO;
    }

    /**
     * Set the level at which the bean must log the message.
     * @param logLevel      One of the {@link LogLevel values}.
     *                      The default is {@link LogLevel#INFO}.
     */
    public void setLogLevel(String logLevel)
    {
        this.logLevel = LogLevel.valueOf(logLevel);
    }

    /**
     * Set the message that must be logged.  This can be a message string
     * or an ID of an internationalized string.
     * 
     * @param message       a message to log at the {@link #setLogLevel(String) log level}
     */
    public void setMessage(String message)
    {
        this.message = message;
    }

    @Override
    protected void checkProperties()
    {
        PropertyCheck.mandatory(this, "message", message);
        // fulfil contract of override
        super.checkProperties();
    }

    @Override
    protected void executeInternal() throws Throwable
    {
        String moduleId = super.getModuleId();
        String name = super.getName();
        Log logger = LogFactory.getLog(moduleId + "." + name);
        switch (logLevel)
        {
        case INFO:
            logger.info(message);
            break;
        case WARN:
            logger.warn(message);
            break;
        case ERROR:
            logger.error(message);
            break;
        }
    }
}
