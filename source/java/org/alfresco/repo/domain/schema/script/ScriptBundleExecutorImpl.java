/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.domain.schema.script;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link ScriptBundleExecutor} implementation. Uses the supplied {@link ScriptExecutor}
 * to invoke multiple SQL scripts in a particular directory.
 * 
 * @author Matt Ward
 */
public class ScriptBundleExecutorImpl implements ScriptBundleExecutor
{
    private ScriptExecutor scriptExecutor;
    protected Log log = LogFactory.getLog(ScriptBundleExecutorImpl.class);
    
    public ScriptBundleExecutorImpl(ScriptExecutor scriptExecutor)
    {
        this.scriptExecutor = scriptExecutor;
    }

    @Override
    public void exec(String dir, String... scripts)
    {
        for (String name : scripts)
        {
            File file = new File(dir, name);
            try
            {
                scriptExecutor.executeScriptUrl(file.getPath());
            }
            catch (Throwable e)
            {
                log.error("Unable to run SQL script: dir=" + dir + ", name="+name, e);
                // Do not run any more scripts.
                break;
            }
        }
    }
    
    @Override
    public void execWithPostScript(String dir, String postScript, String... scripts)
    {
        try
        {
            exec(dir, scripts);
        }
        finally
        {            
            // Always run the post-script.
            exec(dir, postScript);
        }
    }
}
