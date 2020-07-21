/*
 * Copyright (C) 2013 Alfresco Software Limited.
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

import org.apache.commons.logging.Log;

/**
 * Utility class to split or 'tee' two {@link Log} classes.
 * 
 * @since 4.2
 * 
 * @author Alan Davis
 */
public class LogTee extends LogAdapter
{
    protected Log log2;
    
    public LogTee(Log log1, Log log2)
    {
        super(log1);
        this.log2 = log2;
    }
    
    @Override
    public void trace(Object arg0, Throwable arg1)
    {
        log.trace(arg0, arg1);
        log2.trace(arg0, arg1);
    }

    @Override
    public void debug(Object arg0, Throwable arg1)
    {
        log.debug(arg0, arg1);
        log2.debug(arg0, arg1);
    }

    @Override
    public void info(Object arg0, Throwable arg1)
    {
        log.info(arg0, arg1);
        log2.info(arg0, arg1);
    }

    @Override
    public void warn(Object arg0, Throwable arg1)
    {
        log.warn(arg0, arg1);
        log2.warn(arg0, arg1);
    }

    @Override
    public void error(Object arg0, Throwable arg1)
    {
        log.error(arg0, arg1);
        log2.error(arg0, arg1);
    }

    @Override
    public void fatal(Object arg0, Throwable arg1)
    {
        log.fatal(arg0, arg1);
        log2.fatal(arg0, arg1);
    }

    @Override
    public boolean isTraceEnabled()
    {
        return log.isTraceEnabled() || log2.isTraceEnabled();
    }

    @Override
    public boolean isDebugEnabled()
    {
        return log.isDebugEnabled() || log2.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled()
    {
        return log.isInfoEnabled() || log2.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled()
    {
        return log.isWarnEnabled() || log2.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled()
    {
        return log.isErrorEnabled() || log2.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled()
    {
        return log.isFatalEnabled() || log2.isFatalEnabled();
    }
}