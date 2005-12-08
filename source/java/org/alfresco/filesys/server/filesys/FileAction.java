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
 * <p>
 * The file actions are sent in OpenAndX and NTCreateAndX request/response SMBs.
 */
public final class FileAction
{
    // File open action request codes

    public static final int FailIfExists = 0x0000;
    public static final int OpenIfExists = 0x0001;
    public static final int TruncateExisting = 0x0002;
    public static final int CreateNotExist = 0x0010;

    // File open action response codes

    public static final int FileExisted = 0x0001;
    public static final int FileCreated = 0x0002;
    public static final int FileTruncated = 0x0003;

    // NT file/device open action codes

    public final static int NTSupersede = 0; // supersede if exists, else create a new file
    public final static int NTOpen = 1; // only open if the file exists
    public final static int NTCreate = 2; // create if file does not exist, else fail
    public final static int NTOpenIf = 3; // open if exists else create
    public final static int NTOverwrite = 4; // overwrite if exists, else fail
    public final static int NTOverwriteIf = 5; // overwrite if exists, else create

    /**
     * Check if the file action value indicates that the file should be created if the file does not
     * exist.
     * 
     * @return boolean
     * @param action int
     */
    public final static boolean createNotExists(int action)
    {
        if ((action & CreateNotExist) != 0)
            return true;
        return false;
    }

    /**
     * Check if the open file if exists action is set.
     * 
     * @return boolean
     * @param action int
     */
    public final static boolean openIfExists(int action)
    {
        if ((action & OpenIfExists) != 0)
            return true;
        return false;
    }

    /**
     * Check if the existing file should be truncated.
     * 
     * @return boolean
     * @param action int
     */
    public final static boolean truncateExistingFile(int action)
    {
        if ((action & TruncateExisting) != 0)
            return true;
        return false;
    }

    /**
     * Convert the file exists action flags to a string
     * 
     * @param flags int
     * @return String
     */
    public final static String asString(int flags)
    {
        StringBuffer str = new StringBuffer();

        str.append("[0x");
        str.append(Integer.toHexString(flags));
        str.append(":");

        if (openIfExists(flags))
            str.append("OpenExists|");

        if (truncateExistingFile(flags))
            str.append("Truncate|");

        if (createNotExists(flags))
            str.append("CreateNotExist");

        str.append("]");

        return str.toString();
    }
}