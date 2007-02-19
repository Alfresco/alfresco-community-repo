/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.filesys.server.filesys;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.StringTokenizer;

/**
 * <p>
 * Provides utility methods for manipulating file names.
 */
public final class FileName
{

    // DOS file name seperator

    public static final char DOS_SEPERATOR = '\\';
    public static final String DOS_SEPERATOR_STR = "\\";

    // NTFS Stream seperator

    public static final String NTFSStreamSeperator = ":";

    /**
     * Build a path using the specified components.
     * 
     * @param dev java.lang.String
     * @param path java.lang.String
     * @param filename java.lang.String
     * @param sep char
     * @return java.lang.String
     */
    public static String buildPath(String dev, String path, String filename, char sep)
    {

        // Build the path string

        StringBuffer fullPath = new StringBuffer();

        // Check for a device name

        if (dev != null)
        {

            // Add the device name

            fullPath.append(dev);

            // Check if the device name has a file seperator

            if (dev.length() > 0 && dev.charAt(dev.length() - 1) != sep)
                fullPath.append(sep);
        }

        // Check for a path

        if (path != null)
        {

            // Add the path

            if (fullPath.length() > 0 && path.length() > 0
                    && (path.charAt(0) == sep || path.charAt(0) == DOS_SEPERATOR))
                fullPath.append(path.substring(1));
            else
                fullPath.append(path);

            // Add a trailing seperator, if required

            if (path.length() > 0 && path.charAt(path.length() - 1) != sep && filename != null)
                fullPath.append(sep);
        }

        // Check for a file name

        if (filename != null)
        {

            // Add the file name

            if (fullPath.length() > 0 && filename.length() > 0
                    && (filename.charAt(0) == sep || filename.charAt(0) == DOS_SEPERATOR))
                fullPath.append(filename.substring(1));
            else
                fullPath.append(filename);
        }

        // Convert the file seperator characters in the path if we are not using the normal
        // DOS file seperator character.

        if (sep != DOS_SEPERATOR)
            return convertSeperators(fullPath.toString(), sep);
        return fullPath.toString();
    }

    /**
     * Convert the file seperators in a path to the specified path seperator character.
     * 
     * @param path java.lang.String
     * @param sep char
     * @return java.lang.String
     */
    public static String convertSeperators(String path, char sep)
    {

        // Check if the path contains any DOS seperators

        if (path.indexOf(DOS_SEPERATOR) == -1)
            return path;

        // Convert DOS path seperators to the specified seperator

        StringBuffer newPath = new StringBuffer();
        int idx = 0;

        while (idx < path.length())
        {

            // Get the current character from the path and check if it is a DOS path
            // seperator character.

            char ch = path.charAt(idx++);
            if (ch == DOS_SEPERATOR)
                newPath.append(sep);
            else
                newPath.append(ch);
        }

        // Return the new path string

        return newPath.toString();
    }

    /**
     * Map the input path to a real path, this may require changing the case of various parts of the
     * path. The base path is not checked, it is assumed to exist.
     * 
     * @param base java.lang.String
     * @param path java.lang.String
     * @return java.lang.String
     * @exception java.io.FileNotFoundException The path could not be mapped to a real path.
     */
    public static final String mapPath(String base, String path) throws java.io.FileNotFoundException
    {

        // Split the path string into seperate directory components

        String pathCopy = path;
        if (pathCopy.length() > 0 && pathCopy.startsWith(DOS_SEPERATOR_STR))
            pathCopy = pathCopy.substring(1);

        StringTokenizer token = new StringTokenizer(pathCopy, "\\/");
        int tokCnt = token.countTokens();

        // The mapped path string, if it can be mapped

        String mappedPath = null;

        if (tokCnt > 0)
        {

            // Allocate an array to hold the directory names

            String[] dirs = new String[token.countTokens()];

            // Get the directory names

            int idx = 0;
            while (token.hasMoreTokens())
                dirs[idx++] = token.nextToken();

            // Check if the path ends with a directory or file name, ie. has a trailing '\' or not

            int maxDir = dirs.length;

            if (path.endsWith(DOS_SEPERATOR_STR) == false)
            {

                // Ignore the last token as it is a file name

                maxDir--;
            }

            // Build up the path string and validate that the path exists at each stage.

            StringBuffer pathStr = new StringBuffer(base);
            if (base.endsWith(java.io.File.separator) == false)
                pathStr.append(java.io.File.separator);

            int lastPos = pathStr.length();
            idx = 0;
            File lastDir = null;
            if (base != null && base.length() > 0)
                lastDir = new File(base);
            File curDir = null;

            while (idx < maxDir)
            {

                // Append the current directory to the path

                pathStr.append(dirs[idx]);
                pathStr.append(java.io.File.separator);

                // Check if the current path exists

                curDir = new File(pathStr.toString());

                if (curDir.exists() == false)
                {

                    // Check if there is a previous directory to search

                    if (lastDir == null)
                        throw new FileNotFoundException();

                    // Search the current path for a matching directory, the case may be different

                    String[] fileList = lastDir.list();
                    if (fileList == null || fileList.length == 0)
                        throw new FileNotFoundException();

                    int fidx = 0;
                    boolean foundPath = false;

                    while (fidx < fileList.length && foundPath == false)
                    {

                        // Check if the current file name matches the required directory name

                        if (fileList[fidx].equalsIgnoreCase(dirs[idx]))
                        {

                            // Use the current directory name

                            pathStr.setLength(lastPos);
                            pathStr.append(fileList[fidx]);
                            pathStr.append(java.io.File.separator);

                            // Check if the path is valid

                            curDir = new File(pathStr.toString());
                            if (curDir.exists())
                            {
                                foundPath = true;
                                break;
                            }
                        }

                        // Update the file name index

                        fidx++;
                    }

                    // Check if we found the required directory

                    if (foundPath == false)
                        throw new FileNotFoundException();
                }

                // Set the last valid directory file

                lastDir = curDir;

                // Update the end of valid path location

                lastPos = pathStr.length();

                // Update the current directory index

                idx++;
            }

            // Check if there is a file name to be added to the mapped path

            if (path.endsWith(DOS_SEPERATOR_STR) == false)
            {

                // Map the file name

                String[] fileList = lastDir.list();
                String fileName = dirs[dirs.length - 1];

                // Check if the file list is valid, if not then the path is not valid

                if (fileList == null)
                    throw new FileNotFoundException(path);

                // Search for the required file

                idx = 0;
                boolean foundFile = false;

                while (idx < fileList.length && foundFile == false)
                {
                    if (fileList[idx].compareTo(fileName) == 0)
                        foundFile = true;
                    else
                        idx++;
                }

                // Check if we found the file name, if not then do a case insensitive search

                if (foundFile == false)
                {

                    // Search again using a case insensitive search

                    idx = 0;

                    while (idx < fileList.length && foundFile == false)
                    {
                        if (fileList[idx].equalsIgnoreCase(fileName))
                        {
                            foundFile = true;
                            fileName = fileList[idx];
                        }
                        else
                            idx++;
                    }
                }

                // Append the file name

                pathStr.append(fileName);
            }

            // Set the new path string

            mappedPath = pathStr.toString();
        }

        // Return the mapped path string, if successful.

        return mappedPath;
    }

    /**
     * Remove the file name from the specified path string.
     * 
     * @param path java.lang.String
     * @return java.lang.String
     */
    public final static String removeFileName(String path)
    {

        // Find the last path seperator

        int pos = path.lastIndexOf(DOS_SEPERATOR);
        if (pos != -1)
            return path.substring(0, pos);

        // Return an empty string, no path seperators

        return "";
    }

    /**
     * Split the path into seperate directory path and file name strings.
     * 
     * @param path Full path string.
     * @param sep Path seperator character.
     * @return java.lang.String[]
     */
    public static String[] splitPath(String path)
    {
        return splitPath(path, DOS_SEPERATOR, null);
    }

    /**
     * Split the path into seperate directory path and file name strings.
     * 
     * @param path Full path string.
     * @param sep Path seperator character.
     * @return java.lang.String[]
     */
    public static String[] splitPath(String path, char sep)
    {
        return splitPath(path, sep, null);
    }

    /**
     * Split the path into seperate directory path and file name strings.
     * 
     * @param path Full path string.
     * @param sep Path seperator character.
     * @param list String list to return values in, or null to allocate
     * @return java.lang.String[]
     */
    public static String[] splitPath(String path, char sep, String[] list)
    {
        if (path == null)
            throw new IllegalArgumentException("Path may not be null");

        // Create an array of strings to hold the path and file name strings
        String[] pathStr = list;
        if (pathStr == null)
            pathStr = new String[] {"", ""};

        // Check if the path is valid
        if (path.length() > 0)
        {
            // Check if the path has a trailing seperator, if so then there is no file name.
            int pos = path.lastIndexOf(sep);
            if (pos == -1 || pos == (path.length() - 1))
            {
                // Set the path string in the returned string array
                pathStr[0] = path;
            }
            else
            {
                // Split the path into directory list and file name strings
                pathStr[1] = path.substring(pos + 1);

                if (pos == 0)
                    pathStr[0] = path.substring(0, pos + 1);
                else
                    pathStr[0] = path.substring(0, pos);
            }
        }

        // Return the path strings
        return pathStr;
    }

    /**
     * Split the path into all the component directories and filename
     * 
     * @param path String
     * @return String[]
     */
    public static String[] splitAllPaths(String path)
    {

        // Check if the path is valid

        if (path == null || path.length() == 0)
            return null;

        // Determine the number of components in the path

        StringTokenizer token = new StringTokenizer(path, DOS_SEPERATOR_STR);
        String[] names = new String[token.countTokens()];

        // Split the path

        int i = 0;

        while (i < names.length && token.hasMoreTokens())
            names[i++] = token.nextToken();

        // Return the path components

        return names;
    }

    /**
     * Split a path string into directory path, file name and stream name components
     * 
     * @param path Full path string.
     * @return java.lang.String[]
     */
    public static String[] splitPathStream(String path)
    {

        // Allocate the return list

        String[] pathStr = new String[3];

        // Split the path into directory path and file/stream name

        FileName.splitPath(path, DOS_SEPERATOR, pathStr);
        if (pathStr[1] == null)
            return pathStr;

        // Split the file name into file and stream names

        int pos = pathStr[1].indexOf(NTFSStreamSeperator);

        if (pos != -1)
        {

            // Split the file/stream name

            pathStr[2] = pathStr[1].substring(pos);
            pathStr[1] = pathStr[1].substring(0, pos);
        }

        // Return the path components list

        return pathStr;
    }

    /**
     * Test if a file name contains an NTFS stream name
     * 
     * @param path String
     * @return boolean
     */
    public static boolean containsStreamName(String path)
    {

        // Check if the path contains the stream name seperator character

        if (path.indexOf(NTFSStreamSeperator) != -1)
            return true;
        return false;
    }

    /**
     * Normalize the path to uppercase the directory names and keep the case of the file name.
     * 
     * @param path String
     * @return String
     */
    public final static String normalizePath(String path)
    {

        // Split the path into directories and file name, only uppercase the directories to
        // normalize
        // the path.

        String normPath = path;

        if (path.length() > 3)
        {

            // Split the path to seperate the folders/file name

            int pos = path.lastIndexOf(DOS_SEPERATOR);
            if (pos != -1)
            {

                // Get the path and file name parts, normalize the path

                String pathPart = path.substring(0, pos).toUpperCase();
                String namePart = path.substring(pos);

                // Rebuild the path string

                normPath = pathPart + namePart;
            }
        }

        // Return the normalized path

        return normPath;
    }

    /**
     * Make a path relative to the base path for the specified path.
     * 
     * @param basePath String
     * @param fullPath String
     * @return String
     */
    public final static String makeRelativePath(String basePath, String fullPath)
    {

        // Check if the base path is the root path

        if (basePath.length() == 0 || basePath.equals(DOS_SEPERATOR_STR))
        {

            // Return the full path, strip any leading seperator

            if (fullPath.length() > 0 && fullPath.charAt(0) == DOS_SEPERATOR)
                return fullPath.substring(1);
            return fullPath;
        }

        // Split the base and full paths into seperate components

        String[] baseNames = splitAllPaths(basePath);
        String[] fullNames = splitAllPaths(fullPath);

        // Check that the full path is actually within the base path tree

        if (baseNames != null && baseNames.length > 0 && fullNames != null && fullNames.length > 0
                && baseNames[0].equalsIgnoreCase(fullNames[0]) == false)
            return null;

        // Match the path names

        int idx = 0;

        while (idx < baseNames.length && idx < fullNames.length && baseNames[idx].equalsIgnoreCase(fullNames[idx]))
            idx++;

        // Build the relative path

        StringBuffer relPath = new StringBuffer(128);

        while (idx < fullNames.length)
        {
            relPath.append(fullNames[idx++]);
            if (idx < fullNames.length)
                relPath.append(DOS_SEPERATOR);
        }

        // Return the relative path

        return relPath.toString();
    }
}