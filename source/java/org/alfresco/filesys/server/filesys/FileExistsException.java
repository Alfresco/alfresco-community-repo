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
 * This exception may be thrown by a disk interface when an attempt to create a new file fails
 * because the file already exists.
 */
public class FileExistsException extends java.io.IOException
{
    private static final long serialVersionUID = 3258408439242895670L;

    /**
     * FileExistsException constructor.
     */
    public FileExistsException()
    {
        super();
    }

    /**
     * FileExistsException constructor.
     * 
     * @param s java.lang.String
     */
    public FileExistsException(String s)
    {
        super(s);
    }
}