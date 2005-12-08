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
 * File Access Class
 * <p>
 * Contains a list of the available file permissions that may be applied to a share, directory or
 * file.
 */
public final class FileAccess
{
    // Permissions

    public static final int NoAccess = 0;
    public static final int ReadOnly = 1;
    public static final int Writeable = 2;

    /**
     * Return the file permission as a string.
     * 
     * @param perm int
     * @return java.lang.String
     */
    public final static String asString(int perm)
    {
        String permStr = "";

        switch (perm)
        {
        case NoAccess:
            permStr = "NoAccess";
            break;
        case ReadOnly:
            permStr = "ReadOnly";
            break;
        case Writeable:
            permStr = "Writeable";
            break;
        }
        return permStr;
    }
}