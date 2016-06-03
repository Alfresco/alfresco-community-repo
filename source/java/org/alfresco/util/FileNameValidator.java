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
