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
 * Thrown when an attempt is made to write to a file that is read-only or the user only has read
 * access to, or open a file that is actually a directory.
 */
public class AccessDeniedException extends java.io.IOException
{
    private static final long serialVersionUID = 3688785881968293433L;

    /**
     * AccessDeniedException constructor
     */
    public AccessDeniedException()
    {
        super();
    }

    /**
     * AccessDeniedException constructor.
     * 
     * @param s java.lang.String
     */
    public AccessDeniedException(String s)
    {
        super(s);
    }
}