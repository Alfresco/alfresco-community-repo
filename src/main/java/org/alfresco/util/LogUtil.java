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
package org.alfresco.util;

import org.springframework.extensions.surf.util.I18NUtil;
import org.apache.commons.logging.Log;

/**
 * Utility class to assist with I18N of log messages.
 * <p>
 * Calls to this class should still be wrapped with the appropriate log level checks:
 * <pre>
 * if (logger.isDebugEnabled())
 * {
 *     LogUtil.debug(logger, MSG_EXECUTING_STATEMENT, sql);
 * }
 * </pre>
 * 
 * @see org.springframework.extensions.surf.util.I18NUtil
 * @since 2.1
 * 
 * @author Derek Hulley
 */
public class LogUtil
{
    /**
     * Log an I18Nized message to DEBUG.
     * 
     * @param logger        the logger to use
     * @param messageKey    the message key
     * @param args          the required message arguments
     */
    public static final void debug(Log logger, String messageKey, Object ... args)
    {
        logger.debug(I18NUtil.getMessage(messageKey, args));
    }

    /**
     * Log an I18Nized message to INFO.
     * 
     * @param logger        the logger to use
     * @param messageKey    the message key
     * @param args          the required message arguments
     */
    public static final void info(Log logger, String messageKey, Object ... args)
    {
        logger.info(I18NUtil.getMessage(messageKey, args));
    }
    
    /**
     * Log an I18Nized message to WARN.
     * 
     * @param logger        the logger to use
     * @param messageKey    the message key
     * @param args          the required message arguments
     */
    public static final void warn(Log logger, String messageKey, Object ... args)
    {
        logger.warn(I18NUtil.getMessage(messageKey, args));
    }
    
    /**
     * Log an I18Nized message to ERROR.
     * 
     * @param logger        the logger to use
     * @param messageKey    the message key
     * @param args          the required message arguments
     */
    public static final void error(Log logger, String messageKey, Object ... args)
    {
        logger.error(I18NUtil.getMessage(messageKey, args));
    }
    
    /**
     * Log an I18Nized message to ERROR with a given source error.
     * 
     * @param logger        the logger to use
     * @param e             the exception cause of the issue
     * @param messageKey    the message key
     * @param args          the required message arguments
     */
    public static final void error(Log logger, Throwable e, String messageKey, Object ... args)
    {
        logger.error(I18NUtil.getMessage(messageKey, args), e);
    }
}
