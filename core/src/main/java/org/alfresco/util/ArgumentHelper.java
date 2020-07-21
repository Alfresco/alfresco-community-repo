/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to assist in extracting program arguments.
 * 
 * @author Derek Hulley
 * @since V2.1-A
 */
public class ArgumentHelper
{
    private String usage;
    private Map<String, String> args;
    
    public static Map<String, String> ripArgs(String ... args)
    {
        Map<String, String> argsMap = new HashMap<String, String>(5);
        for (String arg : args)
        {
            int index = arg.indexOf('=');
            if (!arg.startsWith("--") || index < 0 || index == arg.length() - 1)
            {
                // Ignore it
                continue;
            }
            String name = arg.substring(2, index);
            String value = arg.substring(index + 1, arg.length());
            argsMap.put(name, value);
        }
        return argsMap;
    }
    
    public ArgumentHelper(String usage, String[] args)
    {
        this.usage = usage;
        this.args = ArgumentHelper.ripArgs(args);
    }
    
    /**
     * @throws IllegalArgumentException if the argument doesn't match the requirements.
     */
    public String getStringValue(String arg, boolean mandatory, boolean nonEmpty)
    {
        String value = args.get(arg);
        if (value == null && mandatory)
        {
            throw new IllegalArgumentException("Argument '" + arg + "' is required.");
        }
        else if (value != null && value.length() == 0 && nonEmpty)
        {
            throw new IllegalArgumentException("Argument '" + arg + "' may not be empty.");
        }
        return value;
    }
    
    /**
     * @return          Returns the value assigned or the minimum value if the parameter was not present
     * @throws IllegalArgumentException if the argument doesn't match the requirements.
     */
    public int getIntegerValue(String arg, boolean mandatory, int minValue, int maxValue)
    {
        String valueStr = args.get(arg);
        if (valueStr == null)
        {
            if (mandatory)
            {
                throw new IllegalArgumentException("Argument '" + arg + "' is required.");
            }
            else
            {
                return minValue;
            }
        }
        // Now convert
        try
        {
            int value = Integer.parseInt(valueStr);
            if (value < minValue || value > maxValue)
            {
                throw new IllegalArgumentException("Argument '" + arg + "' must be in range " + minValue + " to " + maxValue + ".");
            }
            return value;
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Argument '" + arg + "' must be a valid integer.");
        }
    }
    
    public void printUsage()
    {
        System.out.println(usage);
    }
}
