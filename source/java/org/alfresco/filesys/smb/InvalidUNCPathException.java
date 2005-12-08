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
 * Invalid UNC path exception class
 * <p>
 * The InvalidUNCPathException indicates that a UNC path has an invalid format.
 * 
 * @see PCShare
 */
public class InvalidUNCPathException extends Exception
{
    private static final long serialVersionUID = 3257567304241066297L;

    /**
     * Default invalid UNC path exception constructor.
     */

    public InvalidUNCPathException()
    {
    }

    /**
     * Invalid UNC path exception constructor, with additional details string.
     */

    public InvalidUNCPathException(String msg)
    {
        super(msg);
    }
}