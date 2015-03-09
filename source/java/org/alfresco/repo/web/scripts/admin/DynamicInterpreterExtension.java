/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.admin;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.alfresco.repo.admin.BaseInterpreter;
import org.alfresco.repo.processor.BaseProcessorExtension;

/**
 * Console Interpeter script extension - dynamically binds to the configured BaseInterpreter instance.
 * This avoids the need for a specific bean class per script interpreter.
 * 
 * @see org.alfresco.repo.admin.BaseInterpreter
 * See script beans configured in 'web-scripts-application-context.xml'.
 * 
 * @author Kevin Roast
 * @since 5.1
 */
public class DynamicInterpreterExtension extends BaseProcessorExtension
{
    private BaseInterpreter interpreter;
    private long duration;
    private String result = "";
    private String command = "";
    
    /**
     * Set the BaseInterpreter to use when executing commands and retrieving the command result.
     * 
     * @param interpreter   For example, repoAdminInterpreter
     */
    public void setInterpreter(BaseInterpreter interpreter)
    {
        this.interpreter = interpreter;
    }
    
    private BaseInterpreter getInterpreter()
    {
        return this.interpreter;
    }
    
    /**
     * Script execute command gateway.
     * 
     * @param command string to execute
     */
    public void executeCmd(String command)
    {
        this.command = command;
        this.interpretCommand(command);
    }
    
    /**
     * @return the command duration
     */
    public long getDuration()
    {
        return this.duration;
    }

    /**
     * @return the command result
     */
    public String getResult()
    {
        return this.result;
    }

    /**
     * @return the command last executed
     */
    public String getCommand()
    {
        return this.command;
    }

    /**
     * Interpret console command using the configured Interpreter
     *
     * @param command  command
     */
    private void interpretCommand(String command)
    {
        try
        {
            long startms = System.currentTimeMillis();
            this.result = getInterpreter().interpretCommand(command);
            this.duration = System.currentTimeMillis() - startms;
        }
        catch (Throwable e)
        {
            StringWriter stackTrace = new StringWriter();
            e.printStackTrace(new PrintWriter(stackTrace));
            this.result = stackTrace.toString();
        }
    }
}
