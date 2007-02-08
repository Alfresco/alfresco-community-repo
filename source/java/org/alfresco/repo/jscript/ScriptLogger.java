/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.jscript;

import org.apache.log4j.Logger;

/**
 * @author Kevin Roast
 */
public final class ScriptLogger extends BaseScriptImplementation
{
    private static final Logger logger = Logger.getLogger(ScriptLogger.class);
    
    public boolean isLoggingEnabled()
    {
        return logger.isDebugEnabled();
    }
    
    public boolean jsGet_isLoggingEnabled()
    {
        return isLoggingEnabled();
    }
    
    public void log(String str)
    {
        logger.debug(str);
    }
}
