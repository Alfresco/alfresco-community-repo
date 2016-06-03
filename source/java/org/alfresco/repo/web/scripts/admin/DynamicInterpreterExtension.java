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
