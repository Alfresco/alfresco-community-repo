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
