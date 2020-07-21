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
package org.alfresco.error;


/**
 * Helper class around outputting stack traces.
 * 
 * @author Derek Hulley
 */
public class StackTraceUtil
{
    /**
     * Builds a message with the stack trace of the form:
     * <pre>
     *    SOME MESSAGE:
     *       Started at:
     *          com.package...
     *          com.package...
     *          ...
     * </pre>
     * 
     * @param msg the initial error message
     * @param stackTraceElements the stack trace elements
     * @param sb the buffer to append to
     * @param maxDepth the maximum number of trace elements to output.  0 or less means output all.
     */
    public static void buildStackTrace(
            String msg,
            StackTraceElement[] stackTraceElements,
            StringBuilder sb,
            int maxDepth)
    {
        String lineEnding = System.getProperty("line.separator", "\n");

        sb.append(msg).append(" ").append(lineEnding)
          .append("   Started at: ").append(lineEnding);
        for (int i = 0; i < stackTraceElements.length; i++)
        {
            if (i > maxDepth && maxDepth > 0)
            {
                sb.append("      ...");
                break;
            }
            sb.append("      ").append(stackTraceElements[i]);
            if (i < stackTraceElements.length - 1)
            {
                sb.append(lineEnding);
            }
        }
    }
}
