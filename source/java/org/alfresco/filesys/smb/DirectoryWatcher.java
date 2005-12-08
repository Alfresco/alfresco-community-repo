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
package org.alfresco.filesys.smb;

/**
 * Directory Watcher Interface
 */
public interface DirectoryWatcher
{

    // Notification event types

    public final static int FileActionUnknown = -1;
    public final static int FileNoAction = 0;
    public final static int FileAdded = 1;
    public final static int FileRemoved = 2;
    public final static int FileModified = 3;
    public final static int FileRenamedOld = 4;
    public final static int FileRenamedNew = 5;

    /**
     * Directory change occurred
     * 
     * @param typ int
     * @param fname String
     */
    public void directoryChanged(int typ, String fname);
}
