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
 * Helper class to provide information about exception stacks.
 * 
 * @author Derek Hulley
 */
public class ExceptionStackUtil
{
    private static final String JAVASCRIPT_EXCEPTION = "org.mozilla.javascript.JavaScriptException";
    private static final String EXCEPTION_DELIMITER = ":";

    /**
     * Searches through the exception stack of the given throwable to find any instance
     * of the possible cause.  The top-level throwable will also be tested.
     * 
     * @param throwable         the exception condition to search
     * @param possibleCauses    the types of the exception conditions of interest
     * @return                  Returns the first instance that matches one of the given
     *                          possible types, or null if there is nothing in the stack
     */
    public static Throwable getCause(Throwable throwable, Class<?> ... possibleCauses)
    {
        while (throwable != null)
        {
            Class<?> throwableClass = throwable.getClass();

            boolean isJavaScriptException = throwableClass.getName().contains(JAVASCRIPT_EXCEPTION);
            String throwableMsg = throwable.getMessage() != null ? throwable.getMessage() : "";

            for (Class<?> possibleCauseClass : possibleCauses)
            {
                String possibleCauseClassName = possibleCauseClass.getName();

                if (possibleCauseClass.isAssignableFrom(throwableClass)
                        || (isJavaScriptException && throwableMsg.contains(possibleCauseClassName + EXCEPTION_DELIMITER)))
                {
                    // We have a match
                    return throwable;
                }
            }
            // There was no match, so dig deeper
            Throwable cause = throwable.getCause();
            throwable = (throwable == cause) ? null : cause;
        }
        // Nothing found
        return null;
    }
}
