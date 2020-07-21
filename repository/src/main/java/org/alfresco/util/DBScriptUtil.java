/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
package org.alfresco.util;

import java.io.IOException;
import java.io.LineNumberReader;

import org.springframework.core.io.support.EncodedResource;

public abstract class DBScriptUtil
{
    private static final String DEFAULT_SCRIPT_COMMENT_PREFIX = "--";

    /**
     * Read a script from the provided EncodedResource and build a String containing
     * the lines.
     *
     * @param resource
     *            the resource (potentially associated with a specific encoding) to
     *            load the SQL script from
     * @return a String containing the script lines
     */
    public static String readScript(EncodedResource resource) throws IOException
    {
        return readScript(resource, DEFAULT_SCRIPT_COMMENT_PREFIX);
    }

    /**
     * Read a script from the provided EncodedResource, using the supplied line
     * comment prefix, and build a String containing the lines.
     *
     * @param resource
     *            the resource (potentially associated with a specific encoding) to
     *            load the SQL script from
     * @param lineCommentPrefix
     *            the prefix that identifies comments in the SQL script (typically
     *            "--")
     * @return a String containing the script lines
     */
    private static String readScript(EncodedResource resource, String lineCommentPrefix) throws IOException
    {
        LineNumberReader lineNumberReader = new LineNumberReader(resource.getReader());
        try
        {
            return readScript(lineNumberReader, lineCommentPrefix);
        }
        finally
        {
            lineNumberReader.close();
        }
    }

    /**
     * Read a script from the provided LineNumberReader, using the supplied line
     * comment prefix, and build a String containing the lines.
     *
     * @param lineNumberReader
     *            the LineNumberReader containing the script to be processed
     * @param lineCommentPrefix
     *            the prefix that identifies comments in the SQL script (typically
     *            "--")
     * @return a String containing the script lines
     */
    private static String readScript(LineNumberReader lineNumberReader, String lineCommentPrefix) throws IOException
    {
        String statement = lineNumberReader.readLine();
        StringBuilder scriptBuilder = new StringBuilder();
        while (statement != null)
        {
            if (lineCommentPrefix != null && !statement.startsWith(lineCommentPrefix))
            {
                if (scriptBuilder.length() > 0)
                {
                    scriptBuilder.append('\n');
                }
                scriptBuilder.append(statement);
            }
            statement = lineNumberReader.readLine();
        }

        return scriptBuilder.toString();
    }

}
