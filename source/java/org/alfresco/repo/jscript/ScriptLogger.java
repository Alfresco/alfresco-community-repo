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

import org.alfresco.repo.processor.BaseProcessorExtension;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Kevin Roast
 */
public final class ScriptLogger extends BaseProcessorExtension
{
    private static final Log logger = LogFactory.getLog(ScriptLogger.class);
    private static final SystemOut systemOut = new SystemOut();
    
    public boolean isLoggingEnabled()
    {
        return isDebugLoggingEnabled();
    }
    
    public void log(String str)
    {
        debug(str);
    }
    
    public boolean isDebugLoggingEnabled()
    {
        return logger.isDebugEnabled();
    }
    
    public void debug(String str)
    {
        logger.debug(str);
    }
    
    public boolean isInfoLoggingEnabled()
    {
        return logger.isInfoEnabled();
    }
    
    public void info(String str)
    {
        logger.info(str);
    }
    
    public boolean isWarnLoggingEnabled()
    {
        return logger.isWarnEnabled();
    }
    
    public void warn(String str)
    {
        logger.warn(str);
    }
    
    public boolean isErrorLoggingEnabled()
    {
        return logger.isErrorEnabled();
    }
    
    public void error(String str)
    {
        logger.error(str);
    }

    public SystemOut getSystem()
    {
        return systemOut;
    }
    
    public static class SystemOut
    {
        public void out(Object str)
        {
            System.out.println(str);
        }
    }
}
