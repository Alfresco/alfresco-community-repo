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
package org.alfresco.util;

import java.util.regex.Pattern;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Static checker for valid file names.
 * 
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public class FileNameValidator
{
    /**
     * The bad file name pattern.
     */
    public static final String FILENAME_ILLEGAL_REGEX = "[\\\"\\*\\\\\\>\\<\\?\\/\\:\\|]";
    private static final Pattern FILENAME_ILLEGAL_PATTERN_REPLACE = Pattern.compile(FILENAME_ILLEGAL_REGEX);
    
    public static boolean isValid(String name)
    {
        return !FILENAME_ILLEGAL_PATTERN_REPLACE.matcher(name).find();
    }
    
    /**
     * Replaces illegal filename characters with '_'
     */
    public static String getValidFileName(String fileName)
    {
        if (fileName == null || fileName.length() == 0)
        {
            throw new IllegalArgumentException("File name cannot be corrected if it is null or empty.");
        }
        return FILENAME_ILLEGAL_PATTERN_REPLACE.matcher(fileName).replaceAll("_");
    }
}
