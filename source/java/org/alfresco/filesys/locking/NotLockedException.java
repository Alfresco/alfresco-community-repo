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
package org.alfresco.filesys.locking;

import java.io.IOException;

/**
 * Not Locked Exception Class
 * <p>
 * Thrown when an unlock request is received that has not active lock on a file.
 */
public class NotLockedException extends IOException
{
    private static final long serialVersionUID = 3834594296543261488L;

    /**
     * Class constructor.
     */
    public NotLockedException()
    {
        super();
    }

    /**
     * Class constructor.
     * 
     * @param s java.lang.String
     */
    public NotLockedException(String s)
    {
        super(s);
    }
}
