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

import java.io.IOException;

/**
 * Media Offline Exception Class
 * <p>
 * This exception may be thrown by a disk interface when a file/folder is not available due to the
 * storage media being offline, repository being unavailable, database unavailable or inaccessible
 * or similar condition.
 */
public class MediaOfflineException extends IOException
{
    private static final long serialVersionUID = 3544956554064704306L;

    /**
     * Class constructor.
     */
    public MediaOfflineException()
    {
        super();
    }

    /**
     * Class constructor.
     * 
     * @param s java.lang.String
     */
    public MediaOfflineException(String s)
    {
        super(s);
    }
}
