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
package org.alfresco.repo.management;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.alfresco.util.exec.RuntimeExec;
import org.alfresco.util.exec.RuntimeExec.ExecutionResult;


/**
 * A utility class providing a method to dump a local or remote MBeanServer's entire object tree for support purposes.
 * Nested arrays and CompositeData objects in MBean attribute values are handled.
 * 
 * @author dward
 */
public class JmxDumpUtil
{
    /** Table header for attribute names. */
    private static final String NAME_HEADER = "Attribute Name";

    /** Table header for attribute values. */
    private static final String VALUE_HEADER = "Attribute Value";

    /** Place holder for nulls. */
    private static final String NULL_VALUE = "<null>";

    /** Place holder for protected values (generally passwords). */
    public static final String PROTECTED_VALUE = "********";

    private static final String OS_NAME = "os.name";

    private static final String INPUT_ARGUMENTS = "InputArguments";

    private static final String[] REDACTED_INPUTS = {"password","token","pwd"};
    
    /**
     * Dumps a local or remote MBeanServer's entire object tree for support purposes. Nested arrays and CompositeData
     * objects in MBean attribute values are handled.
     * 
     * @param connection
     *            the server connection (or server itself)
     * @param out
     *            PrintWriter to write the output to
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void dumpConnection(MBeanServerConnection connection, PrintWriter out) throws IOException
    {
        JmxDumpUtil.showStartBanner(out);
        
        // Get all the object names
        Set<ObjectName> objectNames = connection.queryNames(null, null);

        // Sort the names (don't assume ObjectName implements Comparable in JDK 1.5)
        Set<ObjectName> newObjectNames = new TreeSet<ObjectName>(new Comparator<ObjectName>()
        {
            public int compare(ObjectName o1, ObjectName o2)
            {
                return o1.toString().compareTo(o2.toString());
            }
        });
        newObjectNames.addAll(objectNames);
        objectNames = newObjectNames;

        
        // Dump each MBean
        for (ObjectName objectName : objectNames)
        {
            try
            {
                printMBeanInfo(connection, objectName, out);
            }
            catch (JMException e)
            {
                // Sometimes beans can disappear while we are examining them
            }
        }
    }

    /**
     * Dumps the details of a single MBean.
     * 
     * @param connection
     *            the server connection (or server itself)
     * @param objectName
     *            the object name
     * @param out
     *            PrintWriter to write the output to
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws JMException
     *             Signals a JMX error
     */
    private static void printMBeanInfo(MBeanServerConnection connection, ObjectName objectName, PrintWriter out)
            throws IOException, JMException
    {
        Map<String, Object> attributes = new TreeMap<String, Object>();
        MBeanInfo info = connection.getMBeanInfo(objectName);
        attributes.put("** Object Name", objectName.toString());
        attributes.put("** Object Type", info.getClassName());
        for (MBeanAttributeInfo element : info.getAttributes())
        {
            Object value;
            if (element.isReadable())
            {
                try
                {
                    value = connection.getAttribute(objectName, element.getName());
                }
                catch (Exception e)
                {
                    value = JmxDumpUtil.PROTECTED_VALUE;
                }
            }
            else
            {
                value = JmxDumpUtil.PROTECTED_VALUE;
            }
            attributes.put(element.getName(), value);
        }
        if (objectName.getCanonicalName().equals("Alfresco:Name=SystemProperties"))
        {
            String osName = (String) attributes.get(OS_NAME);
            if (osName != null && osName.toLowerCase().startsWith("linux"))
            {
                attributes.put(OS_NAME, updateOSNameAttributeForLinux(osName));
            }
        }
        if (objectName.getCanonicalName().equals("java.lang:type=Runtime"))
        {
            String[] commandInputs = (String[]) attributes.get(INPUT_ARGUMENTS);
            if(commandInputs != null)
            {
                try 
                {
                    attributes.put(INPUT_ARGUMENTS, cleanPasswordsFromInputArguments(commandInputs,REDACTED_INPUTS));
                } catch (IllegalArgumentException e) 
                {
                    attributes.put(INPUT_ARGUMENTS, commandInputs);
                }
            }
        }
        tabulate(JmxDumpUtil.NAME_HEADER, JmxDumpUtil.VALUE_HEADER, attributes, out, 0);
    }

    /**
     * Replaces strings with JmxDumpUtil.PROTECTED_VALUE, 
     * if any of the string that contains a string from redactedInputs.
     * 
     * @see #cleanPasswordFromInputArgument
     * @param commandInputs one or more strings of input arguments
     * @param redactedInputs one or more strings, that end input arguments that are to be redacted
     * @return commandInputs with any arguments ending in redactedInputs with redacted values
     */
    static String[] cleanPasswordsFromInputArguments(String[] commandInputs, String[] redactedInputs)
    {
        Pattern passwordRedactPattern = Pattern.compile(createPasswordFindRegexString(redactedInputs));
        List<String> cleanInputs = new ArrayList<String>();
        for (String input : commandInputs) 
        {
            input = cleanPasswordFromInputArgument(input, passwordRedactPattern);
            cleanInputs.add(input);
        }
        
        return cleanInputs.toArray(new String[commandInputs.length]);
    }

    /**
     * Removes any characters the word/s provided in redactedInputs
     * and replaces them with JmxDumpUtil.PROTECTED_VALUE
     * <p>
     * Example: 
     * <p>
     * Input:   -Ddb.password=alfresco
     * <p>
     * Output:  -Ddb.password=********
     * </p>
     * 
     * @param input String
     * @param redactedInputs String[]
     * @return password redacted string if input matches a string in redactedInputs, an un-altered string will be returned if it does not match.
     */
    static String cleanPasswordFromInputArgument(String input, Pattern redactedInputPattern)
    {
        //Replace the whole string with just capture group 1 to remove the desired value and concat the protected value.
        String output = redactedInputPattern.matcher(input).replaceAll("$1"+PROTECTED_VALUE);
        return output;
    }

    /**
     * Creates a regular expression that will select a string that contains one of the values provided in argEndings, proceeding an "=" and defines two capture groups:
     * <ul>
     * <li>Group 1: An argEnding that is followed by an "=", including the "=" and all character prior to the argEnding.
     * <li>Group 2: The characters that follow group 1, to the end of the string or new line.
     * </ul>
     * <p>
     * The argEnding can be the whole Input argument or the common characters proceeding the = sign.
     * Example argEndings:
     * <ul>
     * <li>    -Ddb.password   This will select the values passed as -Ddb.password
     * <li>    password        This will select any potential values that end in the word password
     * </ul>
     * <p>
     * Example usage: 
     * <p>
     * argEndings={"password", "pwd"}
     * <p>
     * This will create a regex that will match a string that contains either argEndings. 
     * The following will be matched by the resulting regex:
     * <p>
     * "-Ddb.password=my_password"
     * <p>
     * For this example: group 1="-Ddb.password=" group 2="my_password"
     * 
     * 
     * @param argEndings Strings that will end the input argument you wish to select
     * @return Regex pattern for selecting the characters following the strings passed as argEndings
     */
    static String createPasswordFindRegexString(String[] argEndings) throws IllegalArgumentException
    {
        if(argEndings.length<1)
        {
            IllegalArgumentException e = new IllegalArgumentException("Arguments are required");
            throw e;
        }

        StringJoiner argJoiner = new StringJoiner("|");

        for (String argEnding : argEndings) 
        {
            argJoiner.add(escapeRegexMetaChars(argEnding)+"=");
        }

        String regex = String.format("%s%s%s%s%s", 
                        "(?i)(.*(", argJoiner.toString(),"))((?<=",argJoiner.toString(), ").*+)");
        return regex;
    }

    /**
     * Places an escape character in front of any regex meta charater: | ? * + .
     * 
     * @param input
     * @return
     */
    static String escapeRegexMetaChars (String input)
    {
        String pattern = "(\\||\\?|\\*|\\+|\\.)";
        String output = input.replaceAll(pattern, "\\\\$1");
        return output;
    }

    /**
     * Adds a Linux version
     * 
     * @param osName os.name attribute
     * @return String
     */
    public static String updateOSNameAttributeForLinux(String osName)
    {
        RuntimeExec exec = new RuntimeExec();
        Map<String, String[]> commandMap = new HashMap<String, String[]>(3, 1.0f);
        commandMap.put("Linux", new String[] { "lsb_release", "-d" });
        exec.setCommandsAndArguments(commandMap);
        ExecutionResult ret = exec.execute();
        if (ret.getSuccess())
        {
            osName += " (" + ret.getStdOut().replace("\n", "") + ")";
        }
        else
        {
            commandMap.put("Linux", new String[] { "uname", "-a" });
            exec.setCommandsAndArguments(commandMap);
            ret = exec.execute();
            if (ret.getSuccess())
            {
                osName += " (" + ret.getStdOut().replace("\n", "") + ")";
            }
            else
            {
                osName += " (Unknown)";
            }
        }
        return osName;
    }

    /**
     * Dumps the details of a single CompositeData object.
     * 
     * @param composite
     *            the composite object
     * @param out
     *            PrintWriter to write the output to
     * @param nestLevel
     *            the nesting level
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @SuppressWarnings("unchecked")
    private static void printCompositeInfo(CompositeData composite, PrintWriter out, int nestLevel) throws IOException
    {
        Map<String, Object> attributes = new TreeMap<String, Object>();
        for (String key : (Set<String>)composite.getCompositeType().keySet())
        {
            Object value;
            try
            {
                value = composite.get(key);
            }
            catch (Exception e)
            {
                value = JmxDumpUtil.PROTECTED_VALUE;
            }
            attributes.put(key, value);
        }
        tabulate(JmxDumpUtil.NAME_HEADER, JmxDumpUtil.VALUE_HEADER, attributes, out, nestLevel);
    }

    /**
     * Tabulates a given String -> Object Map.
     * 
     * @param keyHeader
     *            the key header
     * @param valueHeader
     *            the value header
     * @param rows
     *            Map containing key value pairs forming the rows
     * @param out
     *            PrintWriter to write the output to
     * @param nestLevel
     *            the nesting level
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static void tabulate(String keyHeader, String valueHeader, Map<String, Object> rows, PrintWriter out,
            int nestLevel) throws IOException
    {
        if (rows.isEmpty())
        {
            return;
        }
        // Calculate column lengths
        int maxKeyLength = keyHeader.length(), maxValLength = valueHeader.length();
        for (Map.Entry<String, Object> entry : rows.entrySet())
        {
            maxKeyLength = Math.max(maxKeyLength, entry.getKey().length());
            maxValLength = Math.max(maxValLength, getValueLength(entry.getValue()));
        }
        // Output Header
        outputRow(out, maxKeyLength, keyHeader, valueHeader, nestLevel);
        indent(out, nestLevel);
        for (int col = 0; col < maxKeyLength; col++)
        {
            out.print('-');
        }
        out.print(' ');
        for (int col = 0; col < maxValLength; col++)
        {
            out.print('-');
        }
        out.println();

        // Output Body
        for (Map.Entry<String, Object> entry : rows.entrySet())
        {
            outputRow(out, maxKeyLength, entry.getKey(), entry.getValue(), nestLevel);
        }
        out.println();
    }

    /**
     * Outputs spaces in the left hand margin appropriate for the given nesting level.
     * 
     * @param out
     *            PrintWriter to write the output to
     * @param nestLevel
     *            the nesting level
     */
    private static void indent(PrintWriter out, int nestLevel)
    {
        int size = nestLevel * 3;
        for (int i = 0; i < size; i++)
        {
            out.print(' ');
        }
    }

    /**
     * Outputs a single row in a two-column table. The first column is padded with spaces so that the second column is
     * aligned.
     * 
     * @param out
     *            PrintWriter to write the output to
     * @param maxKeyLength
     *            maximum number of characters in the first column
     * @param key
     *            the first column value
     * @param value
     *            the second column value
     * @param nestLevel
     *            the nesting level
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static void outputRow(PrintWriter out, int maxKeyLength, String key, Object value, int nestLevel)
            throws IOException
    {
        indent(out, nestLevel);
        out.print(key);
        for (int i = key.length() - 1; i < maxKeyLength; i++)
        {
            out.print(' ');
        }
        outputValue(out, value, nestLevel);
    }

    /**
     * Outputs a single value, dealing with nested arrays and CompositeData objects.
     * 
     * @param out
     *            PrintWriter to write the output to
     * @param value
     *            the value to output
     * @param nestLevel
     *            the nesting level
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static void outputValue(PrintWriter out, Object value, int nestLevel) throws IOException
    {
        if (value == null)
        {
            out.println(JmxDumpUtil.NULL_VALUE);
        }
        else if (value.getClass().isArray())
        {
            int length = Array.getLength(value);
            if (length == 0)
            {
                out.println("[]");
            }
            else
            {
                out.println();
                indent(out, nestLevel + 1);
                out.println('[');
                for (int i = 0; i < length; i++)
                {
                    indent(out, nestLevel + 2);
                    outputValue(out, Array.get(value, i), nestLevel + 2);
                    if (i + 1 < length)
                    {
                        indent(out, nestLevel + 1);
                        out.println(',');
                    }
                }
                indent(out, nestLevel + 1);
                out.println(']');
            }
        }
        else if (value instanceof CompositeData)
        {
            out.println();
            indent(out, nestLevel + 1);
            out.println('[');
            printCompositeInfo((CompositeData) value, out, nestLevel + 2);
            indent(out, nestLevel + 1);
            out.println(']');
        }
        else
        {
            out.println(value.toString());
        }
    }

    /**
     * Gets the number of characters required to encode a value.
     * 
     * @param value
     *            the value to be encoded
     * @return the number of characters
     */
    private static int getValueLength(Object value)
    {
        if (value == null)
        {
            return JmxDumpUtil.NULL_VALUE.length();
        }
        else if (value.getClass().isArray() || value instanceof CompositeData)
        {
            // We continue arrays and composites on a new line
            return 0;
        }
        else
        {
            return value.toString().length();
        }
    }
    
    /**
     * Show a message stating the JmxDumper has been started, with the current date and time. 
     */
    private static void showStartBanner(PrintWriter out)
    {
        DateFormat df = SimpleDateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
        out.println(JmxDumpUtil.class.getSimpleName() + " started: " + df.format(new Date()));
        out.println();
    }
}
