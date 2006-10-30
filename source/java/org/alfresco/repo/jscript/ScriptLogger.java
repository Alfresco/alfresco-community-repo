/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
