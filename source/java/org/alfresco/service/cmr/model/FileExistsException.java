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
package org.alfresco.service.cmr.model;

/**
 * Common, checked exception thrown when an operation fails because
 * of a name clash.
 * 
 * @author Derek Hulley
 */
public class FileExistsException extends Exception
{
    private static final long serialVersionUID = -4133713912784624118L;
    
    private FileInfo existing;

    public FileExistsException(FileInfo existing)
    {
        super("" +
                (existing.isFolder() ? "Folder " : "File ") +
                existing.getName() +
                " already exists");
        this.existing = existing;
    }

    public FileInfo getExisting()
    {
        return existing;
    }
}
