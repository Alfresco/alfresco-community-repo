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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.service.cmr.avmsync;

import java.io.Serializable;

/**
 * Represents the difference between corresponding nodes in parallel avm node trees. It it indicates for the difference
 * whether the source is older, newer, or in conflict with the destination.
 * 
 * @author britt
 */
public class AVMDifference implements Serializable, Comparable<AVMDifference>
{
    private static final long serialVersionUID = -589722861571724954L;

    public static final int NEWER = 0;

    public static final int OLDER = 1;

    public static final int CONFLICT = 2;

    public static final int DIRECTORY = 3;

    public static final int SAME = 4;

    /**
     * Version number of the source node.
     */
    private int fSourceVersion;

    /**
     * Path of the source node.
     */
    private String fSourcePath;

    /**
     * Version number of the destination node.
     */
    private int fDestVersion;

    /**
     * Path of the destination node.
     */
    private String fDestPath;

    /**
     * The difference code.
     */
    private int fDiffCode;

    /**
     * Make one up.
     * 
     * @param srcVersion
     *            The source version.
     * @param srcPath
     *            the source path.
     * @param dstVersion
     *            The destination version.
     * @param dstPath
     *            The destination path.
     * @param diffCode
     *            The difference code, NEWER, OLDER, CONFLICT
     */
    public AVMDifference(int srcVersion, String srcPath, int dstVersion, String dstPath, int diffCode)
    {
        fSourceVersion = srcVersion;
        fSourcePath = srcPath;
        fDestVersion = dstVersion;
        fDestPath = dstPath;
        fDiffCode = diffCode;
    }

    /**
     * Get the source version number.
     * 
     * @return The source version number.
     */
    public int getSourceVersion()
    {
        return fSourceVersion;
    }

    /**
     * Get the source path.
     * 
     * @return The source path.
     */
    public String getSourcePath()
    {
        return fSourcePath;
    }

    /**
     * Get the destination version number.
     * 
     * @return The destination version number.
     */
    public int getDestinationVersion()
    {
        return fDestVersion;
    }

    /**
     * Get the destination path.
     * 
     * @return The destination path.
     */
    public String getDestinationPath()
    {
        return fDestPath;
    }

    /**
     * Get the difference code, NEWER, OLDER, CONFLICT.
     * 
     * @return The difference code.
     */
    public int getDifferenceCode()
    {
        return fDiffCode;
    }

    /**
     * Check for improperly initialized instances.
     * 
     * @return Whether source and destination are non null.
     */
    public boolean isValid()
    {
        return fSourcePath != null && fDestPath != null;
    }

    /**
     * Get as String.
     * 
     * @return A String representation of this.
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(fSourcePath);
        builder.append("[");
        builder.append(fSourceVersion);
        builder.append("] ");
        switch (fDiffCode)
        {
        case SAME:
            builder.append("= ");
            break;
        case NEWER:
            builder.append("> ");
            break;
        case OLDER:
            builder.append("< ");
            break;
        case CONFLICT:
            builder.append("<> ");
            break;
        case DIRECTORY:
            builder.append("| ");
            break;
        default:
            builder.append("? ");
        }
        builder.append(fDestPath);
        builder.append("[");
        builder.append(fDestVersion);
        builder.append("]");
        return builder.toString();
    }

    public int compareTo(AVMDifference other)
    {
        return this.getOrderValue() - other.getOrderValue();
    }

    public int getOrderValue()
    {
        switch (fDiffCode)
        {
        case CONFLICT:
            return 0;
        case NEWER:
            return 1;
        case OLDER:
            return 2;
        case DIRECTORY:
            return 3;
        case SAME:
            return 4;
        default:
            return 100;
        }
    }
}
