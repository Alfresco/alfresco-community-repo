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

import org.alfresco.api.AlfrescoPublicApi;     

/**
 * Utility class to adapt a {@link Log} class.
 * 
 * @since 4.2
 * 
 * @author Alan Davis
 */
@AlfrescoPublicApi
public abstract class LogAdapter implements Log
{
    final protected Log log;
    
    /**
     * Constructor of an optional wrapped {@link Log}.
     * @param log
     */
    protected LogAdapter(Log log)
    {
        this.log = log;
    }

    @Override
    public void trace(Object arg0)
    {
        trace(arg0, null);
    }
    
    @Override
    public void trace(Object arg0, Throwable arg1)
    {
        if (log != null)
        {
            log.trace(arg0, arg1);
        }
    }

    @Override
    public void debug(Object arg0)
    {
        debug(arg0, null);
    }

    @Override
    public void debug(Object arg0, Throwable arg1)
    {
        if (log != null)
        {
            log.debug(arg0, arg1);
        }
    }

    @Override
    public void info(Object arg0)
    {
        info(arg0, null);
    }

    @Override
    public void info(Object arg0, Throwable arg1)
    {
        if (log != null)
        {
            log.info(arg0, arg1);
        }
    }

    @Override
    public void warn(Object arg0)
    {
        warn(arg0, null);
    }

    @Override
    public void warn(Object arg0, Throwable arg1)
    {
        if (log != null)
        {
            log.warn(arg0, arg1);
        }
    }

    @Override
    public void error(Object arg0)
    {
        error(arg0, null);
    }

    @Override
    public void error(Object arg0, Throwable arg1)
    {
        if (log != null)
        {
            log.error(arg0, arg1);
        }
    }

    @Override
    public void fatal(Object arg0)
    {
        fatal(arg0, null);
    }

    @Override
    public void fatal(Object arg0, Throwable arg1)
    {
        if (log != null)
        {
            log.fatal(arg0, arg1);
        }
    }

    @Override
    public boolean isTraceEnabled()
    {
        return log != null && log.isTraceEnabled();
    }

    @Override
    public boolean isDebugEnabled()
    {
        return log != null && log.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled()
    {
        return log != null && log.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled()
    {
        return log != null && log.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled()
    {
        return log != null && log.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled()
    {
        return log != null && log.isFatalEnabled();
    }
}