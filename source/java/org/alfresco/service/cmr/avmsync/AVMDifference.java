/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */

package org.alfresco.service.cmr.avmsync;

import java.io.Serializable;

/**
 * Represents the difference between corresponding nodes
 * in parallel avm node trees.  It it indicates for the difference
 * whether the source is older, newer, or in conflict with the destination.
 * @author britt
 */
public class AVMDifference implements Serializable
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
     * @param srcVersion The source version.
     * @param srcPath the source path.
     * @param dstVersion The destination version.
     * @param dstPath The destination path. 
     * @param diffCode The difference code, NEWER, OLDER, CONFLICT
     */
    public AVMDifference(int srcVersion, String srcPath,
                         int dstVersion, String dstPath, int diffCode)
    {
        fSourceVersion = srcVersion;
        fSourcePath = srcPath;
        fDestVersion = dstVersion;
        fDestPath = dstPath;
        fDiffCode = diffCode;
    }
    
    /**
     * Get the source version number.
     * @return The source version number.
     */
    public int getSourceVersion()
    {
        return fSourceVersion;
    }
    
    /**
     * Get the source path.
     * @return The source path.
     */
    public String getSourcePath()
    {
        return fSourcePath;
    }
    
    /**
     * Get the destination version number.
     * @return The destination version number.
     */
    public int getDestinationVersion()
    {
        return fDestVersion;
    }
    
    /**
     * Get the destination path.
     * @return The destination path.
     */
    public String getDestinationPath()
    {
        return fDestPath;
    }

    /**
     * Get the difference code, NEWER, OLDER, CONFLICT.
     * @return The difference code.
     */
    public int getDifferenceCode()
    {
        return fDiffCode;
    }
    
    /**
     * Check for improperly initialized instances.
     * @return Whether source and destination are non null.
     */
    public boolean isValid()
    {
        return fSourcePath != null && fDestPath != null;
    }
    
    /**
     * Get as String.
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
            case SAME :
                builder.append("= ");
                break;
            case NEWER :
                builder.append("> ");
                break;
            case OLDER :
                builder.append("< ");
                break;
            case CONFLICT :
                builder.append("<> ");
                break;
            case DIRECTORY :
                builder.append("| ");
                break;
            default :
                builder.append("? ");
        }
        builder.append(fDestPath);
        builder.append("[");
        builder.append(fDestVersion);
        builder.append("]");
        return builder.toString();
    }
}
