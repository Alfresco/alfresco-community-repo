/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.filesys.server.filesys;

/**
 * Notify Change Transaction Class
 */
public class NotifyChange
{

    // Change notification filter flags

    public final static int FileName = 0x0001;
    public final static int DirectoryName = 0x0002;
    public final static int Attributes = 0x0004;
    public final static int Size = 0x0008;
    public final static int LastWrite = 0x0010;
    public final static int LastAccess = 0x0020;
    public final static int Creation = 0x0040;
    public final static int Security = 0x0100;

    // Change notification actions

    public final static int ActionAdded = 1;
    public final static int ActionRemoved = 2;
    public final static int ActionModified = 3;
    public final static int ActionRenamedOldName = 4;
    public final static int ActionRenamedNewName = 5;
    public final static int ActionAddedStream = 6;
    public final static int ActionRemovedStream = 7;
    public final static int ActionModifiedStream = 8;

    // Change notification action names

    private final static String[] _actnNames = { "Added", "Removed", "Modified", "RenamedOldName", "RenamedNewName",
            "AddedStream", "RemovedStream", "ModifiedStream" };

    /**
     * Return the change notification action as a string
     * 
     * @param action int
     * @return String
     */
    public static final String getActionAsString(int action)
    {

        // Range check the action

        if (action <= 0 || action > _actnNames.length)
            return "Unknown";

        // Return the action as a string

        return _actnNames[action - 1];
    }

    /**
     * Return the change notification filter flag as a string. This method assumes a single flag is
     * set.
     * 
     * @param filter int
     * @return String
     */
    public static final String getFilterAsString(int filter)
    {

        // Check if there are any flags set

        if (filter == 0)
            return "";

        // Determine the filter type

        String filtStr = null;

        switch (filter)
        {
        case FileName:
            filtStr = "FileName";
            break;
        case DirectoryName:
            filtStr = "DirectoryName";
            break;
        case Attributes:
            filtStr = "Attributes";
            break;
        case Size:
            filtStr = "Size";
            break;
        case LastWrite:
            filtStr = "LastWrite";
            break;
        case LastAccess:
            filtStr = "LastAccess";
            break;
        case Creation:
            filtStr = "Creation";
            break;
        case Security:
            filtStr = "Security";
            break;
        }

        // Return the filter type string

        return filtStr;
    }

    /**
     * Return the change notification filter flags as a string.
     * 
     * @param filter int
     * @return String
     */
    public static final String getFilterFlagsAsString(int filter)
    {

        // Check if there are any flags set

        if (filter == 0)
            return "";

        // Build the filter flags string

        StringBuffer filtStr = new StringBuffer();
        int i = 0x0001;

        while (i < Security)
        {

            // Check if the current filter flag is set

            if ((filter & i) != 0)
            {

                // Get the filter flag name

                String name = getFilterAsString(i);
                if (name != null)
                {
                    if (filtStr.length() > 0)
                        filtStr.append(",");
                    filtStr.append(name);
                }
            }

            // Update the filter flag mask

            i = i << 1;
        }

        // Return the filter flags string

        return filtStr.toString();
    }
}
