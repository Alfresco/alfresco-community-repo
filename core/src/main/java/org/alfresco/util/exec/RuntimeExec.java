/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.util.exec;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This acts as a session similar to the <code>java.lang.Process</code>, but
 * logs the system standard and error streams.
 * <p>
 * The bean can be configured to execute a command directly, or be given a map
 * of commands keyed by the <i>os.name</i> Java system property.  In this map,
 * the default key that is used when no match is found is the
 * <b>{@link #KEY_OS_DEFAULT *}</b> key.
 * <p>
 * Use the {@link #setProcessDirectory(String) processDirectory} property to change the default location
 * from which the command executes.  The process's environment can be configured using the
 * {@link #setProcessProperties(Map) processProperties} property.
 * <p>
 * Commands may use placeholders, e.g.
 * <pre><code>
 *    find
 *    -name
 *    ${filename}
 * </code></pre>
 * The <b>filename</b> property will be substituted for any supplied value prior to
 * each execution of the command.  Currently, no checks are made to get or check the
 * properties contained within the command string.  It is up to the client code to
 * dynamically extract the properties required if the required properties are not
 * known up front.
 * <p>
 * Sometimes, a variable may contain several arguments.  .  In this case, the arguments
 * need to be tokenized using a standard <tt>StringTokenizer</tt>.  To force tokenization
 * of a value, use:
 * <pre><code>
 *    SPLIT:${userArgs}
 * </code></pre>
 * You should not use this just to split up arguments that are known to require tokenization
 * up front.  The <b>SPLIT:</b> directive works for the entire argument and will not do anything
 * if it is not at the beginning of the argument.  Do not use <b>SPLIT:</b> to break up arguments
 * that are fixed, so avoid doing this:
 * <pre><code>
 *    SPLIT:ls -lih
 * </code></pre>
 * Instead, break the command up explicitly:
 * <pre><code>
 *    ls
 *    -lih
 * </code></pre>
 * 
 * Tokenization of quoted parameter values is handled by {@link ExecParameterTokenizer}, which
 * describes the support in more detail.
 * 
 * @author Derek Hulley
 */
public class RuntimeExec
{
    /** the key to use when specifying a command for any other OS: <b>*</b> */
    public static final String KEY_OS_DEFAULT = "*";
    
    private static final String KEY_OS_NAME = "os.name";
    private static final int BUFFER_SIZE = 1024;
    private static final String VAR_OPEN = "${";
    private static final String VAR_CLOSE = "}";
    private static final String DIRECTIVE_SPLIT = "SPLIT:";

    private static Log logger = LogFactory.getLog(RuntimeExec.class);
    private static Log transformerDebugLogger = LogFactory.getLog("org.alfresco.repo.content.transform.TransformerDebug");

    private String[] command;
    private Charset charset;
    private boolean waitForCompletion;
    private Map<String, String> defaultProperties;
    private String[] processProperties;
    private File processDirectory;
    private Set<Integer> errCodes;
    private Timer timer = new Timer(true);

    /**
     * Default constructor.  Initialize this instance by setting individual properties.
     */
    public RuntimeExec()
    {
        this.charset = Charset.defaultCharset();
        this.waitForCompletion = true;
        defaultProperties = Collections.emptyMap();
        processProperties = null;
        processDirectory = null;
        
        // set default error codes
        this.errCodes = new HashSet<Integer>(2);
        errCodes.add(1);
        errCodes.add(2);
    }
    
    public String toString()
    {
        
        StringBuffer sb = new StringBuffer(256);
        sb.append("RuntimeExec:\n")
          .append("   command:    ");
        if (command == null)
        {
            // command is 'null', so there's nothing to toString
            sb.append("'null'\n");
        }
        else
        {
            for (String cmdStr : command)
            {
                sb.append(cmdStr).append(" ");
            }
            sb.append("\n");
        }
        sb.append("   env props:  ").append(Arrays.toString(processProperties)).append("\n")
          .append("   dir:        ").append(processDirectory).append("\n")
          .append("   os:         ").append(System.getProperty(KEY_OS_NAME)).append("\n");
        return sb.toString();
    }
    
    /**
     * Set the command to execute regardless of operating system
     * 
     * @param command       an array of strings representing the command (first entry) and arguments
     * 
     * @since 3.0
     */
    public void setCommand(String[] command)
    {
        this.command = command;
    }

    /**
     * Sets the assumed charset of OUT and ERR streams generated by the executed command.
     * This defaults to the system default charset: {@link Charset#defaultCharset()}.
     * 
     * @param charsetCode                           a supported character set code
     * @throws UnsupportedCharsetException          if the characterset code is not recognised by Java
     */
    public void setCharset(String charsetCode)
    {
        this.charset = Charset.forName(charsetCode);
    }

    /**
     * Set whether to wait for completion of the command or not.  If there is no wait for completion,
     * then the return value of <i>out</i> and <i>err</i> buffers cannot be relied upon as the
     * command may still be in progress.  Failure is therefore not possible unless the calling thread
     * waits for execution.
     * 
     * @param waitForCompletion     <tt>true</tt> (default) is to wait for the command to exit,
     *                              or <tt>false</tt> to just return an exit code of 0 and whatever
     *                              output is available at that point.
     * 
     * @since 2.1
     */
    public void setWaitForCompletion(boolean waitForCompletion)
    {
        this.waitForCompletion = waitForCompletion;
    }

    /**
     * Supply a choice of commands to execute based on a mapping from the <i>os.name</i> system
     * property to the command to execute.  The {@link #KEY_OS_DEFAULT *} key can be used
     * to get a command where there is not direct match to the operating system key.
     * <p>
     * Each command is an array of strings, the first of which represents the command and all subsequent
     * entries in the array represent the arguments.  All elements of the array will be checked for
     * the presence of any substitution parameters (e.g. '{dir}').  The parameters can be set using the
     * {@link #setDefaultProperties(Map) defaults} or by passing the substitution values into the
     * {@link #execute(Map)} command.
     * <p>
     * If parameters passed may be multiple arguments, or if the values provided in the map are themselves
     * collections of arguments (not recommended), then prefix the value with <b>SPLIT:</b> to ensure that
     * the value is tokenized before being passed to the command.  Any values that are not split, will be
     * passed to the command as single arguments.  For example:<br>
     * '<b>SPLIT: dir . ..</b>' becomes '<b>dir</b>', '<b>.</b>' and '<b>..</b>'.<br>
     * '<b>SPLIT: dir ${path}</b>' (if path is '<b>. ..</b>') becomes '<b>dir</b>', '<b>.</b>' and '<b>..</b>'.<br>
     * The splitting occurs post-subtitution.  Where the arguments are known, it is advisable to avoid
     * <b>SPLIT:</b>.
     * 
     * @param commandsByOS          a map of command string arrays, keyed by operating system names
     * 
     * @see #setDefaultProperties(Map)
     * 
     * @since 3.0
     */
    public void setCommandsAndArguments(Map<String, String[]> commandsByOS)
    {
        // get the current OS
        String serverOs = System.getProperty(KEY_OS_NAME);
        // attempt to find a match
        String[] command = commandsByOS.get(serverOs);
        if (command == null)
        {
            // go through the commands keys, looking for one that matches by regular expression matching
            for (String osName : commandsByOS.keySet())
            {
                // Ignore * options.  It is dealt with later.
                if (osName.equals(KEY_OS_DEFAULT))
                {
                    continue;
                }
                // Do regex match
                if (serverOs.matches(osName))
                {
                    command = commandsByOS.get(osName);
                    break;
                }
            }
            // if there is still no command, then check for the wildcard
            if (command == null)
            {
                command = commandsByOS.get(KEY_OS_DEFAULT);
            }
        }
        // check
        if (command == null)
        {
            throw new AlfrescoRuntimeException(
                    "No command found for OS " + serverOs + " or '" + KEY_OS_DEFAULT + "': \n" +
                    "   commands: " + commandsByOS);
        }
        this.command = command;
    }
    
    /**
     * Supply a choice of commands to execute based on a mapping from the <i>os.name</i> system
     * property to the command to execute.  The {@link #KEY_OS_DEFAULT *} key can be used
     * to get a command where there is not direct match to the operating system key.
     * 
     * @param commandsByOS a map of command string keyed by operating system names
     * 
     * @deprecated          Use {@link #setCommandsAndArguments(Map)}
     */
    public void setCommandMap(Map<String, String> commandsByOS)
    {
        // This is deprecated, so issue a warning
        logger.warn(
                "The bean RuntimeExec property 'commandMap' has been deprecated;" +
                " use 'commandsAndArguments' instead.  See https://issues.alfresco.com/jira/browse/ETHREEOH-579.");
        Map<String, String[]> fixed = new LinkedHashMap<String, String[]>(7);
        for (Map.Entry<String, String> entry : commandsByOS.entrySet())
        {
            String os = entry.getKey();
            String unparsedCmd = entry.getValue();
            StringTokenizer tokenizer = new StringTokenizer(unparsedCmd);
            String[] cmd = new String[tokenizer.countTokens()];
            for (int i = 0; i < cmd.length; i++)
            {
                cmd[i] = tokenizer.nextToken();
            }
            fixed.put(os, cmd);
        }
        setCommandsAndArguments(fixed);
    }
    
    /**
     * Set the default command-line properties to use when executing the command.
     * These are properties that substitute variables defined in the command string itself.
     * Properties supplied during execution will overwrite the default properties.
     * <p>
     * <code>null</code> properties will be treated as an empty string for substitution
     * purposes.
     * 
     * @param defaultProperties property values
     */
    public void setDefaultProperties(Map<String, String> defaultProperties)
    {
        this.defaultProperties = defaultProperties;
    }

    /**
     * Set additional runtime properties (environment properties) that will used
     * by the executing process.
     * <p>
     * Any keys or properties that start and end with <b>${...}</b> will be removed on the assumption
     * that these are unset properties.  <tt>null</tt> values are translated to empty strings.
     * All keys and values are trimmed of leading and trailing whitespace.
     * 
     * @param processProperties     Runtime process properties
     * 
     * @see Runtime#exec(String, String[], java.io.File)
     */
    public void setProcessProperties(Map<String, String> processProperties)
    {
        ArrayList<String> processPropList = new ArrayList<String>(processProperties.size());
        boolean hasPath = false;
        String systemPath = System.getenv("PATH");
        for (Map.Entry<String, String> entry : processProperties.entrySet())
        {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key == null)
            {
                continue;
            }
            if (value == null)
            {
                value = "";
            }
            key = key.trim();
            value = value.trim();
            if (key.startsWith(VAR_OPEN) && key.endsWith(VAR_CLOSE))
            {
                continue;
            }
            if (value.startsWith(VAR_OPEN) && value.endsWith(VAR_CLOSE))
            {
                continue;
            }
            // If a path is specified, prepend it to the existing path
            if (key.equals("PATH"))
            {
                if (systemPath != null && systemPath.length() > 0)
                {
                    processPropList.add(key + "=" + value + File.pathSeparator + systemPath);                    
                }
                else
                {                    
                    processPropList.add(key + "=" + value);
                }
                hasPath = true;
            }
            else
            {
                processPropList.add(key + "=" + value);                
            }
        }
        // If a path was not specified, inherit the current one
        if (!hasPath && systemPath != null && systemPath.length() > 0)
        {
            processPropList.add("PATH=" + systemPath);                                
        }
        this.processProperties = processPropList.toArray(new String[processPropList.size()]);
    }
    
    /**
     * Adds a property to existed processProperties. 
     * Property should not be null or empty.
     * If property with the same value already exists then no change is made.
     * If property exists with a different value then old value is replaced with the new one. 
     * @param name - property name
     * @param value - property value 
     */
    public void setProcessProperty(String name, String value)
    {
        boolean set = false;
        
        if (name == null || value == null) 
            return;
        
        name = name.trim();
        value = value.trim();
        
        if (name.isEmpty() || value.isEmpty()) 
            return; 
        
        String property = name + "=" + value;
             
        for (String prop : this.processProperties)
        {
            if (prop.equals(property))
            {    
                set = true;
                break;
            }    
            
            if (prop.startsWith(name))
            {
                String oldValue = prop.split("=")[1];  
                prop.replace(oldValue, value);
                set = true;
            }
        }
        
        if (!set)
        {
          String[] existedProperties = this.processProperties;
          int epl = existedProperties.length; 
          String[] newProperties = Arrays.copyOf(existedProperties, epl + 1);
          newProperties[epl] = property;
          this.processProperties = newProperties;      
          set = true;
        }           
    }
    
    
    /**
     * Set the runtime location from which the command is executed.
     * <p>
     * If the value is an unsubsititued variable (<b>${...}</b>) then it is ignored.
     * If the location is not visible at the time of setting, a warning is issued only.
     * 
     * @param processDirectory          the runtime location from which to execute the command
     */
    public void setProcessDirectory(String processDirectory)
    {
        if (processDirectory.startsWith(VAR_OPEN) && processDirectory.endsWith(VAR_CLOSE))
        {
            this.processDirectory = null;
        }
        else
        {
            this.processDirectory = new File(processDirectory);
            if (!this.processDirectory.exists())
            {
                logger.warn(
                        "The runtime process directory is not visible when setting property 'processDirectory': \n" +
                        this);
            }
        }
    }

    /**
     * A comma or space separated list of values that, if returned by the executed command,
     * indicate an error value.  This defaults to <b>"1, 2"</b>.
     * 
     * @param errCodesStr the error codes for the execution
     */
    public void setErrorCodes(String errCodesStr)
    {
        errCodes.clear();
        StringTokenizer tokenizer = new StringTokenizer(errCodesStr, " ,");
        while(tokenizer.hasMoreElements())
        {
            String errCodeStr = tokenizer.nextToken();
            // attempt to convert it to an integer
            try
            {
                int errCode = Integer.parseInt(errCodeStr);
                this.errCodes.add(errCode);
            }
            catch (NumberFormatException e)
            {
                throw new AlfrescoRuntimeException(
                        "Property 'errorCodes' must be comma-separated list of integers: " + errCodesStr);
            }
        }
    }
    
    /**
     * Executes the command using the default properties
     * 
     * @see #execute(Map)
     */
    public ExecutionResult execute()
    {
        return execute(defaultProperties);
    }

    /**
     * Executes the statement that this instance was constructed with.
     * 
     * @param properties the properties that the command might be executed with.
     * <code>null</code> properties will be treated as an empty string for substitution
     * purposes.
     * 
     * @return Returns the full execution results
     */
    public ExecutionResult execute(Map<String, String> properties)
    {
        return execute(properties, -1);
    }

    /**
     * Executes the statement that this instance was constructed with an optional
     * timeout after which the command is asked to 
     * 
     * @param properties the properties that the command might be executed with.
     * <code>null</code> properties will be treated as an empty string for substitution
     * purposes.
     * @param timeoutMs a timeout after which {@link Process#destroy()} is called.
     *        ignored if less than or equal to zero. Note this method does not guarantee
     *        to terminate the process (it is not a kill -9).
     * 
     * @return Returns the full execution results
     */
    public ExecutionResult execute(Map<String, String> properties, final long timeoutMs)
    {
        int defaultFailureExitValue = errCodes.size() > 0 ? ((Integer)errCodes.toArray()[0]) : 1;
        
        // check that the command has been set
        if (command == null)
        {
            throw new AlfrescoRuntimeException("Runtime command has not been set: \n" + this);
        }
        
        // create the properties
        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        String[] commandToExecute = null;
        try
        {
            // execute the command with full property replacement
            commandToExecute = getCommand(properties);
            final Process thisProcess = runtime.exec(commandToExecute, processProperties, processDirectory);
            process = thisProcess;
            if (timeoutMs > 0)
            {
                final String[] command = commandToExecute;
                timer.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        // Only try to kill the process if it is still running
                        try
                        {
                            thisProcess.exitValue();
                        }
                        catch (IllegalThreadStateException stillRunning)
                        {
                            if (transformerDebugLogger.isDebugEnabled())
                            {
                                transformerDebugLogger.debug("Process has taken too long ("+
                                    (timeoutMs/1000)+" seconds). Killing process "+
                                    Arrays.deepToString(command));
                            }
                            thisProcess.destroy();
                        }
                    }
                }, timeoutMs);
            }
        }
        catch (IOException e)
        {
            // The process could not be executed here, so just drop out with an appropriate error state
            String execOut = "";
            String execErr = e.getMessage();
            int exitValue = defaultFailureExitValue;
            ExecutionResult result = new ExecutionResult(null, commandToExecute, errCodes, exitValue, execOut, execErr);
            logFullEnvironmentDump(result);
            return result;
        }

        // create the stream gobblers
        InputStreamReaderThread stdOutGobbler = new InputStreamReaderThread(process.getInputStream(), charset);
        InputStreamReaderThread stdErrGobbler = new InputStreamReaderThread(process.getErrorStream(), charset);

        // start gobbling
        stdOutGobbler.start();
        stdErrGobbler.start();

        // wait for the process to finish
        int exitValue = 0;
        try
        {
            if (waitForCompletion)
            {
                exitValue = process.waitFor();
            }
        }
        catch (InterruptedException e)
        {
            // process was interrupted - generate an error message
            stdErrGobbler.addToBuffer(e.toString());
            exitValue = defaultFailureExitValue;
        }

        if (waitForCompletion)
        {
            // ensure that the stream gobblers get to finish
            stdOutGobbler.waitForCompletion();
            stdErrGobbler.waitForCompletion();
        }

        // get the stream values
        String execOut = stdOutGobbler.getBuffer();
        String execErr = stdErrGobbler.getBuffer();
        
        // construct the return value
        ExecutionResult result = new ExecutionResult(process, commandToExecute, errCodes, exitValue, execOut, execErr);

        // done
        logFullEnvironmentDump(result);
        return result;
    }

    /**
     * Dump the full environment in debug mode
     */
    private void logFullEnvironmentDump(ExecutionResult result)
    {
        if (logger.isTraceEnabled())
        {
            StringBuilder sb = new StringBuilder();
            sb.append(result);

            // Environment variables modified by Alfresco
            if (processProperties != null && processProperties.length > 0)
            {
                sb.append("\n   modified environment: ");
                for (int i=0; i<processProperties.length; i++)
                {
                    String property = processProperties[i];
                    sb.append("\n        ");
                    sb.append(property);
                }
            }

            // Dump the full environment
            sb.append("\n   existing environment: ");
            Map<String, String> envVariables = System.getenv();
            for (Map.Entry<String, String> entry : envVariables.entrySet())
            {
                String name = entry.getKey();
                String value = entry.getValue();
                sb.append("\n        ");
                sb.append(name + "=" + value);
            }

            logger.trace(sb);
        }
        else if (logger.isDebugEnabled())
        {
            logger.debug(result);
        }
        
        // close output stream (connected to input stream of native subprocess) 
    }

    /**
     * @return Returns the command that will be executed if no additional properties
     *      were to be supplied
     */
    public String[] getCommand()
    {
        return getCommand(defaultProperties);
    }
    
    /**
     * Get the command that will be executed post substitution.
     * <p>
     * <code>null</code> properties will be treated as an empty string for substitution
     * purposes.
     * 
     * @param properties the properties that the command might be executed with
     * @return Returns the command that will be executed should the additional properties
     *      be supplied
     */
    public String[] getCommand(Map<String, String> properties)
    {
        Map<String, String> execProperties = null;
        if (properties == defaultProperties)
        {
            // we are just using the default properties
            execProperties = defaultProperties;
        }
        else
        {
            execProperties = new HashMap<String, String>(defaultProperties);
            // overlay the supplied properties
            execProperties.putAll(properties);
        }
        // Perform the substitution for each element of the command
        ArrayList<String> adjustedCommandElements = new ArrayList<String>(20);
        for (int i = 0; i < command.length; i++)
        {
            StringBuilder sb = new StringBuilder(command[i]);
            for (Map.Entry<String, String> entry : execProperties.entrySet())
            {
                String key = entry.getKey();
                String value = entry.getValue();
                // ignore null
                if (value == null)
                {
                    value = "";
                }
                // progressively replace the property in the command
                key = (VAR_OPEN + key + VAR_CLOSE);
                int index = sb.indexOf(key);
                while (index > -1)
                {
                    // replace
                    sb.replace(index, index + key.length(), value);
                    // get the next one
                    index = sb.indexOf(key, index + 1);
                }
            }
            String adjustedValue = sb.toString();
            // Now SPLIT: it
            if (adjustedValue.startsWith(DIRECTIVE_SPLIT))
            {
                String unsplitAdjustedValue = sb.substring(DIRECTIVE_SPLIT.length());
                
                // There may be quoted arguments here (see ALF-7482)
                ExecParameterTokenizer quoteAwareTokenizer = new ExecParameterTokenizer(unsplitAdjustedValue);
                List<String> tokens = quoteAwareTokenizer.getAllTokens();
                adjustedCommandElements.addAll(tokens);
            }
            else
            {
                adjustedCommandElements.add(adjustedValue);
            }
        }
        // done
        return adjustedCommandElements.toArray(new String[adjustedCommandElements.size()]);
    }
    
    /**
     * Object to carry the results of an execution to the caller.
     * 
     * @author Derek Hulley
     */
    public static class ExecutionResult
    {
        private final Process process;
        private final String[] command;
        private final Set<Integer> errCodes;
        private final int exitValue;
        private final String stdOut;
        private final String stdErr;
       
        /**
         * 
         * @param process           the process attached to Java - <tt>null</tt> is allowed
         */
        private ExecutionResult(
                final Process process,
                final String[] command,
                final Set<Integer> errCodes,
                final int exitValue,
                final String stdOut,
                final String stdErr)
        {
            this.process = process;
            this.command = command;
            this.errCodes = errCodes;
            this.exitValue = exitValue;
            this.stdOut = stdOut;
            this.stdErr = stdErr;
        }
        
        @Override
        public String toString()
        {
            String out = stdOut.length() > 250 ? stdOut.substring(0, 250) : stdOut;
            String err = stdErr.length() > 250 ? stdErr.substring(0, 250) : stdErr;
            
            StringBuilder sb = new StringBuilder(128);
            sb.append("Execution result: \n")
              .append("   os:         ").append(System.getProperty(KEY_OS_NAME)).append("\n")
              .append("   command:    ");appendCommand(sb, command).append("\n")
              .append("   succeeded:  ").append(getSuccess()).append("\n")
              .append("   exit code:  ").append(exitValue).append("\n")
              .append("   out:        ").append(out).append("\n")
              .append("   err:        ").append(err);
            return sb.toString();
        }
        
        /**
         * Appends the command in a form that make running from the command line simpler.
         * It is not a real attempt at making a command given all the operating system 
         * and shell options, but makes copy, paste and edit a bit simpler.
         */
        private StringBuilder appendCommand(StringBuilder sb, String[] command)
        {
            boolean arg = false;
            for (String element: command)
            {
                if (element == null)
                {
                    continue;
                }
                
                if (arg)
                {
                    sb.append(' ');
                }
                else
                {
                    arg = true;
                }

                boolean escape = element.indexOf(' ') != -1 || element.indexOf('>') != -1;
                if (escape)
                {
                    sb.append("\"");
                }
                sb.append(element);
                if (escape)
                {
                    sb.append("\"");
                }
            }
            return sb;
        }
        
        /**
         * A helper method to force a kill of the process that generated this result.  This is
         * useful in cases where the process started is not expected to exit, or doesn't exit
         * quickly.  If the {@linkplain RuntimeExec#setWaitForCompletion(boolean) "wait for completion"}
         * flag is <tt>false</tt> then the process may still be running when this result is returned.
         * 
         * @return
         *      <tt>true</tt> if the process was killed, otherwise <tt>false</tt>
         */
        public boolean killProcess()
        {
            if (process == null)
            {
                return true;
            }
            try
            {
                process.destroy();
                return true;
            }
            catch (Throwable e)
            {
                logger.warn(e.getMessage());
                return false;
            }
        }
        
        /**
         * @param exitValue the command exit value
         * @return Returns true if the code is a listed failure code
         * 
         * @see #setErrorCodes(String)
         */
        private boolean isFailureCode(int exitValue)
        {
            return errCodes.contains((Integer)exitValue);
        }
        
        /**
         * @return Returns true if the command was deemed to be successful according to the
         *      failure codes returned by the execution.
         */
        public boolean getSuccess()
        {
            return !isFailureCode(exitValue);
        }

        public int getExitValue()
        {
            return exitValue;
        }
        
        public String getStdOut()
        {
            return stdOut;
        }
    
        public String getStdErr()
        {
            return stdErr;
        }
    }

    /**
     * Gobbles an <code>InputStream</code> and writes it into a
     * <code>StringBuffer</code>
     * <p>
     * The reading of the input stream is buffered.
     */
    public static class InputStreamReaderThread extends Thread
    {
        private final InputStream is;
        private final Charset charset;
        private final StringBuffer buffer;          // we require the synchronization
        private boolean completed;

        /**
         * @param is an input stream to read - it will be wrapped in a buffer
         *        for reading
         */
        public InputStreamReaderThread(InputStream is, Charset charset)
        {
            super();
            setDaemon(true); // must not hold up the VM if it is terminating
            this.is = is;
            this.charset = charset;
            this.buffer = new StringBuffer(BUFFER_SIZE);
            this.completed = false;
        }

        public synchronized void run()
        {
            completed = false;

            byte[] bytes = new byte[BUFFER_SIZE];
            InputStream tempIs = null;
            try
            {
                tempIs = new BufferedInputStream(is, BUFFER_SIZE);
                int count = -2;
                while (count != -1)
                {
                    // do we have something previously read?
                    if (count > 0)
                    {
                        String toWrite = new String(bytes, 0, count, charset.name());
                        buffer.append(toWrite);
                    }
                    // read the next set of bytes
                    count = tempIs.read(bytes);
                }
                // done
            }
            catch (IOException e)
            {
                throw new AlfrescoRuntimeException("Unable to read stream", e);
            }
            finally
            {
                // close the input stream
                if (tempIs != null)
                {
                    try
                    {
                        tempIs.close();
                    }
                    catch (Exception e)
                    {
                    }
                }
                // The thread has finished consuming the stream
                completed = true;
                // Notify waiters
                this.notifyAll();       // Note: Method is synchronized
            }
        }

        /**
         * Waits for the run to complete.
         * <p>
         * <b>Remember to <code>start</code> the thread first
         */
        public synchronized void waitForCompletion()
        {
            while (!completed)
            {
                try
                {
                    // release our lock and wait a bit
                    this.wait(1000L); // 200 ms
                }
                catch (InterruptedException e)
                {
                }
            }
        }
        
        /**
         * @param msg the message to add to the buffer
         */
        public void addToBuffer(String msg)
        {
            buffer.append(msg);
        }

        public boolean isComplete()
        {
            return completed;
        }

        /**
         * @return Returns the current state of the buffer
         */
        public String getBuffer()
        {
            return buffer.toString();
        }
    }
}
